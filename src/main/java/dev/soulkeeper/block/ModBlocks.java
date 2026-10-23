package dev.soulkeeper.block;

import dev.soulkeeper.Soulkeeper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
	public static final Block SOUL_ANCHOR = Registry.register(
		Registries.BLOCK,
		Soulkeeper.id("soul_anchor"),
		new SoulAnchorBlock(AbstractBlock.Settings.create()
			.strength(3.0f, 1200.0f)
			.sounds(BlockSoundGroup.SOUL_SOIL)
			.luminance(state -> 11))
	);

	public static final Item SOUL_ANCHOR_ITEM = Registry.register(
		Registries.ITEM,
		Soulkeeper.id("soul_anchor"),
		new BlockItem(SOUL_ANCHOR, new Item.Settings())
	);

	private ModBlocks() {
	}

	public static void register() {
	}
}
