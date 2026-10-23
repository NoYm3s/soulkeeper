package dev.soulkeeper.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SoulAnchorBlockEntity extends BlockEntity {
	private static final int SLOT_COUNT = 41;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
	private int storedXp;
	private String ownerName = "";
	@Nullable
	private UUID ownerUuid;
	private boolean publicAccess;
	private boolean released;

	public SoulAnchorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SOUL_ANCHOR, pos, state);
	}

	public void setContents(PlayerEntity player, List<ItemStack> storedItems, int xp) {
		ownerName = player.getName().getString();
		ownerUuid = player.getUuid();
		storedXp = xp;
		released = false;

		for (int i = 0; i < items.size(); i++) {
			items.set(i, i < storedItems.size() ? storedItems.get(i) : ItemStack.EMPTY);
		}

		markDirty();
	}

	public boolean isOwner(PlayerEntity player) {
		return ownerUuid == null || ownerUuid.equals(player.getUuid());
	}

	public String getOwnerName() {
		return ownerName;
	}

	public boolean isPublic() {
		return publicAccess;
	}

	public void setPublic(boolean value) {
		this.publicAccess = value;
		markDirty();
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.getChunkManager().markForUpdate(pos);
		}
	}

	public Text getStatusText() {
		int stacks = 0;
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				stacks++;
			}
		}

		if (stacks == 0 && storedXp <= 0) {
			return Text.translatable("block.soulkeeper.soul_anchor.empty");
		}

		return Text.translatable("block.soulkeeper.soul_anchor.status",
			ownerName, stacks, storedXp, pos.getX(), pos.getY(), pos.getZ());
	}

	public void releaseContents(@Nullable PlayerEntity player) {
		if (released || world == null || world.isClient) {
			return;
		}

		released = true;

		for (ItemStack stack : items) {
			if (stack.isEmpty()) {
				continue;
			}

			if (player != null) {
				player.getInventory().offerOrDrop(stack.copy());
			} else {
				ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy());
			}
		}

		for (int i = 0; i < items.size(); i++) {
			items.set(i, ItemStack.EMPTY);
		}

		if (storedXp > 0) {
			if (player != null) {
				player.addExperience(storedXp);
			} else if (world instanceof ServerWorld serverWorld) {
				ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(pos), storedXp);
			}
			storedXp = 0;
		}

		markDirty();
		world.playSound(null, pos, SoundEvents.BLOCK_SOUL_SOIL_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		Inventories.writeNbt(nbt, items, registries);
		nbt.putInt("StoredXp", storedXp);
		nbt.putString("OwnerName", ownerName);
		if (ownerUuid != null) {
			nbt.putUuid("OwnerUuid", ownerUuid);
		}
		nbt.putBoolean("Public", publicAccess);
	}

	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);

		for (int i = 0; i < items.size(); i++) {
			items.set(i, ItemStack.EMPTY);
		}
		Inventories.readNbt(nbt, items, registries);

		storedXp = nbt.getInt("StoredXp");
		ownerName = nbt.getString("OwnerName");
		ownerUuid = nbt.containsUuid("OwnerUuid") ? nbt.getUuid("OwnerUuid") : null;
		publicAccess = nbt.getBoolean("Public");
	}

	public static void tick(World world, BlockPos pos, BlockState state, SoulAnchorBlockEntity anchor) {
		if (world instanceof ServerWorld serverWorld && world.getTime() % 12 == 0) {
			serverWorld.spawnParticles(
				ParticleTypes.SOUL,
				pos.getX() + 0.5,
				pos.getY() + 1.05,
				pos.getZ() + 0.5,
				1,
				0.25,
				0.05,
				0.25,
				0.002
			);
		}
	}
}
