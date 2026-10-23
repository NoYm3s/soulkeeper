package dev.soulkeeper.item;

import dev.soulkeeper.Soulkeeper;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item SOUL_COMPASS = Registry.register(
		Registries.ITEM,
		Soulkeeper.id("soul_compass"),
		new SoulCompassItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
	);

	private ModItems() {
	}

	public static void register() {
	}
}
