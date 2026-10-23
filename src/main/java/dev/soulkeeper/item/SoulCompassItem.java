package dev.soulkeeper.item;

import dev.soulkeeper.block.entity.SoulAnchorBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class SoulCompassItem extends Item {
	public SoulCompassItem(Settings settings) {
		super(settings);
	}

	public static void bind(ItemStack stack, RegistryKey<World> dimension, BlockPos pos) {
		stack.set(DataComponentTypes.LODESTONE_TRACKER,
			new LodestoneTrackerComponent(Optional.of(new GlobalPos(dimension, pos.toImmutable())), false));
	}

	public static Optional<GlobalPos> getTarget(ItemStack stack) {
		LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
		return tracker == null ? Optional.empty() : tracker.target();
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();

		if (world.getBlockEntity(pos) instanceof SoulAnchorBlockEntity) {
			PlayerEntity player = context.getPlayer();
			ItemStack stack = context.getStack();

			bind(stack, world.getRegistryKey(), pos);

			if (player != null) {
				player.sendMessage(Text.translatable("item.soulkeeper.soul_compass.bound"), true);
			}
			world.playSound(player, pos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.8f, 1.2f);

			return ActionResult.success(world.isClient);
		}

		return ActionResult.PASS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (world.isClient) {
			return TypedActionResult.success(stack);
		}

		Optional<GlobalPos> target = getTarget(stack);

		if (target.isEmpty()) {
			user.sendMessage(Text.translatable("item.soulkeeper.soul_compass.unbound"), true);
			return TypedActionResult.success(stack);
		}

		GlobalPos globalPos = target.get();

		if (!globalPos.dimension().equals(world.getRegistryKey())) {
			Identifier dimension = globalPos.dimension().getValue();
			user.sendMessage(Text.translatable(
				"item.soulkeeper.soul_compass.other_dimension",
				Text.translatable("dimension." + dimension.getNamespace() + "." + dimension.getPath())
			), true);
			return TypedActionResult.success(stack);
		}

		BlockPos pos = globalPos.pos();
		double dx = pos.getX() + 0.5 - user.getX();
		double dz = pos.getZ() + 0.5 - user.getZ();
		double distance = Math.sqrt(dx * dx + dz * dz);
		double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
		double delta = MathHelper.wrapDegrees(targetYaw - user.getYaw());

		String arrow;
		if (delta >= -22.5 && delta < 22.5) {
			arrow = "\u2191";
		} else if (delta >= 22.5 && delta < 67.5) {
			arrow = "\u2197";
		} else if (delta >= 67.5 && delta < 112.5) {
			arrow = "\u2192";
		} else if (delta >= 112.5 && delta < 157.5) {
			arrow = "\u2198";
		} else if (delta >= 157.5 || delta < -157.5) {
			arrow = "\u2193";
		} else if (delta >= -157.5 && delta < -112.5) {
			arrow = "\u2199";
		} else if (delta >= -112.5 && delta < -67.5) {
			arrow = "\u2190";
		} else {
			arrow = "\u2196";
		}

		user.sendMessage(Text.translatable(
			"item.soulkeeper.soul_compass.point",
			arrow,
			Math.round(distance),
			pos.getX(),
			pos.getY(),
			pos.getZ()
		), true);

		world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.6f, 1.4f);
		user.getItemCooldownManager().set(this, 20);

		return TypedActionResult.success(stack);
	}
}
