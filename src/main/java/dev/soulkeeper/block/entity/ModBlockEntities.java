package dev.soulkeeper.block.entity;

import dev.soulkeeper.Soulkeeper;
import dev.soulkeeper.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<SoulAnchorBlockEntity> SOUL_ANCHOR = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		Soulkeeper.id("soul_anchor"),
		BlockEntityType.Builder.create(SoulAnchorBlockEntity::new, ModBlocks.SOUL_ANCHOR).build(null)
	);

	private ModBlockEntities() {
	}

	public static void register() {
	}
}
