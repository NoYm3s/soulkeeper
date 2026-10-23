package dev.soulkeeper.block;

import dev.soulkeeper.SoulkeeperConfig;
import dev.soulkeeper.block.entity.SoulAnchorBlockEntity;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;

public final class AnchorProtection {
	private AnchorProtection() {
	}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (!(blockEntity instanceof SoulAnchorBlockEntity anchor)) {
				return true;
			}

			if (!SoulkeeperConfig.protectAnchors()) {
				return true;
			}

			if (anchor.isOwner(player) || anchor.isPublic()) {
				return true;
			}

			player.sendMessage(Text.translatable(
				"block.soulkeeper.soul_anchor.protected",
				anchor.getOwnerName()
			), true);
			return false;
		});
	}
}
