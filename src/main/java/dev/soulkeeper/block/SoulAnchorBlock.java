package dev.soulkeeper.block;

import dev.soulkeeper.block.entity.SoulAnchorBlockEntity;
import dev.soulkeeper.item.SoulCompassItem;
import dev.soulkeeper.SoulkeeperConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoulAnchorBlock extends Block implements BlockEntityProvider {
	public SoulAnchorBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SoulAnchorBlockEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : (BlockEntityTicker<T>) (BlockEntityTicker<SoulAnchorBlockEntity>) SoulAnchorBlockEntity::tick;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof SoulCompassItem) {
			// Let the compass handle the interaction so it can be bound to this anchor.
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		if (!world.isClient && world.getBlockEntity(pos) instanceof SoulAnchorBlockEntity anchor) {
			if (player.isSneaking() && stack.isOf(Items.IRON_INGOT) && SoulkeeperConfig.protectAnchors() && anchor.isOwner(player)) {
				anchor.setPublic(!anchor.isPublic());
				player.sendMessage(Text.translatable(anchor.isPublic() ? "block.soulkeeper.soul_anchor.public" : "block.soulkeeper.soul_anchor.private"), true);
				world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.8f, anchor.isPublic() ? 1.2f : 0.9f);
				return ItemActionResult.success(world.isClient);
			}

			player.sendMessage(anchor.getStatusText(), false);
			world.playSound(null, pos, SoundEvents.BLOCK_SOUL_SOIL_STEP, SoundCategory.BLOCKS, 0.8f, 1.15f);
		}

		return ItemActionResult.success(world.isClient);
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof SoulAnchorBlockEntity anchor) {
			anchor.releaseContents(player);
		}

		return super.onBreak(world, pos, state, player);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && !world.isClient && world.getBlockEntity(pos) instanceof SoulAnchorBlockEntity anchor) {
			anchor.releaseContents(null);
		}

		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
