package dev.soulkeeper.mixin;

import dev.soulkeeper.death.DeathHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
	private void soulkeeper$keepSoulboundInventory(CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayerEntity player && DeathHandler.shouldCancelInventoryDrop(player)) {
			// Soulkeeper already moved the normal loot into a grave and kept the
			// Soulbound items in the inventory. Vanilla must not drop them.
			ci.cancel();
		}
	}
}
