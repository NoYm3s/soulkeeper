package dev.soulkeeper;

import dev.soulkeeper.block.AnchorProtection;
import dev.soulkeeper.block.ModBlocks;
import dev.soulkeeper.block.entity.ModBlockEntities;
import dev.soulkeeper.death.DeathHandler;
import dev.soulkeeper.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Soulkeeper implements ModInitializer {
	public static final String MOD_ID = "soulkeeper";
	public static final Logger LOGGER = LoggerFactory.getLogger("Soulkeeper");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		SoulkeeperConfig.load();
		ModBlocks.register();
		ModBlockEntities.register();
		ModItems.register();
		AnchorProtection.register();
		DeathHandler.register();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
			entries.add(ModItems.SOUL_COMPASS);
			entries.add(ModBlocks.SOUL_ANCHOR_ITEM);
		});

		LOGGER.info("Soulkeeper loaded: death now leaves a Soul Anchor behind.");
	}
}
