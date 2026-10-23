package dev.soulkeeper.death;

import dev.soulkeeper.Soulkeeper;
import dev.soulkeeper.block.ModBlocks;
import dev.soulkeeper.block.entity.SoulAnchorBlockEntity;
import dev.soulkeeper.item.ModItems;
import dev.soulkeeper.item.SoulCompassItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class DeathHandler {
	private static final Set<UUID> HANDLED = new HashSet<>();
	private static final Map<UUID, GlobalPos> PENDING_MESSAGES = new HashMap<>();
	private static final RegistryKey<Enchantment> SOULBOUND =
		RegistryKey.of(RegistryKeys.ENCHANTMENT, Soulkeeper.id("soulbound"));

	private DeathHandler() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				handleDeath(player);
			}
			return true;
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			HANDLED.remove(newPlayer.getUuid());

			if (!alive) {
				restoreKeptItems(oldPlayer, newPlayer);

				GlobalPos grave = PENDING_MESSAGES.remove(newPlayer.getUuid());
				if (grave != null) {
					Identifier dimension = grave.dimension().getValue();
					newPlayer.sendMessage(Text.translatable(
						"death.soulkeeper.grave",
						Text.translatable("dimension." + dimension.getNamespace() + "." + dimension.getPath()),
						grave.pos().getX(),
						grave.pos().getY(),
						grave.pos().getZ()
					), false);
				}
			}
		});

		// If a dead player reconnects, vanilla may respawn them before the JOIN
		// event; in that case AFTER_RESPAWN already did the work. This only makes
		// sure a stale UUID can never suppress a future grave.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> HANDLED.remove(handler.getPlayer().getUuid()));
	}

	private static void restoreKeptItems(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
		// copyFrom(old, false) does not clone the inventory unless
		// keepInventory is on or the old player was a spectator.
		boolean copiedByVanilla = newPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator();
		if (copiedByVanilla) {
			return;
		}

		for (int i = 0; i < oldPlayer.getInventory().size(); i++) {
			ItemStack stack = oldPlayer.getInventory().getStack(i);
			if (stack.isEmpty()) {
				continue;
			}

			newPlayer.getInventory().offerOrDrop(stack);
			oldPlayer.getInventory().setStack(i, ItemStack.EMPTY);
		}
	}

	public static boolean shouldCancelInventoryDrop(ServerPlayerEntity player) {
		return HANDLED.contains(player.getUuid());
	}

	private static void handleDeath(ServerPlayerEntity player) {
		if (player.getWorld().isClient || player.isSpectator()) {
			return;
		}

		if (player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			return;
		}

		if (!HANDLED.add(player.getUuid())) {
			return;
		}

		ServerWorld world = (ServerWorld) player.getWorld();
		int xp = player.totalExperience;
		List<ItemStack> stored = new ArrayList<>();
		Optional<RegistryEntry.Reference<Enchantment>> soulbound = getEnchantment(world, SOULBOUND);
		Optional<RegistryEntry.Reference<Enchantment>> vanishing = getEnchantment(world, Enchantments.VANISHING_CURSE);

		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (vanishing.isPresent() && EnchantmentHelper.getLevel(vanishing.get(), stack) > 0) {
				player.getInventory().setStack(i, ItemStack.EMPTY);
				continue;
			}

			if (soulbound.isPresent() && EnchantmentHelper.getLevel(soulbound.get(), stack) > 0) {
				// Soulbound stacks stay in the old player's inventory. The mixin
				// cancels dropInventory, and AFTER_RESPAWN moves them to the new body.
				damageKeptItem(stack);
				continue;
			}

			stored.add(stack.copy());
			player.getInventory().setStack(i, ItemStack.EMPTY);
		}

		if (stored.isEmpty() && xp <= 0) {
			return;
		}

		player.totalExperience = 0;
		player.experienceLevel = 0;
		player.experienceProgress = 0.0f;

		BlockPos gravePos = findGravePos(world, player);
		boolean placed = world.setBlockState(gravePos, ModBlocks.SOUL_ANCHOR.getDefaultState(), Block.NOTIFY_ALL);

		if (placed && world.getBlockEntity(gravePos) instanceof SoulAnchorBlockEntity anchor) {
			anchor.setContents(player, stored, xp);
			PENDING_MESSAGES.put(player.getUuid(), new GlobalPos(world.getRegistryKey(), gravePos));

			ItemStack compass = new ItemStack(ModItems.SOUL_COMPASS);
			SoulCompassItem.bind(compass, world.getRegistryKey(), gravePos);
			player.getInventory().offerOrDrop(compass);
		} else {
			for (ItemStack stack : stored) {
				ItemScatterer.spawn(world, player.getX(), player.getY() + 0.5, player.getZ(), stack);
			}

			if (xp > 0) {
				ExperienceOrbEntity.spawn(world, player.getPos().add(0, 0.5, 0), xp);
			}
		}
	}

	private static Optional<RegistryEntry.Reference<Enchantment>> getEnchantment(ServerWorld world, RegistryKey<Enchantment> key) {
		Registry<Enchantment> registry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
		return registry.getEntry(key);
	}

	private static void damageKeptItem(ItemStack stack) {
		if (!stack.isDamageable()) {
			return;
		}

		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 1) {
			return;
		}

		int penalty = Math.max(1, maxDamage / 10);
		stack.setDamage(Math.min(maxDamage - 1, stack.getDamage() + penalty));
	}

	private static BlockPos findGravePos(ServerWorld world, ServerPlayerEntity player) {
		BlockPos origin = player.getBlockPos();

		if (origin.getY() < world.getBottomY() + 4) {
			origin = world.getSpawnPos();
		}

		int minY = world.getBottomY() + 1;
		int maxY = world.getTopY() - 2;
		BlockPos.Mutable start = origin.mutableCopy();
		if (start.getY() < minY) {
			start.setY(minY);
		}
		if (start.getY() > maxY) {
			start.setY(maxY);
		}

		for (int pass = 0; pass < 2; pass++) {
			BlockPos.Mutable cursor = start.mutableCopy();

			for (int i = 0; i < 32 && cursor.getY() <= maxY; i++) {
				BlockState state = world.getBlockState(cursor);
				boolean replaceable = state.isAir() || state.isReplaceable();

				if (pass == 0) {
					replaceable = replaceable && state.getFluidState().isEmpty();
				}

				if (replaceable) {
					return cursor.toImmutable();
				}

				cursor.move(Direction.UP);
			}
		}

		return start.toImmutable();
	}
}
