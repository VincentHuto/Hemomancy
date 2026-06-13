package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.effect.ChummedWatersAreaManager;
import com.vincenthuto.hemomancy.common.effect.ChummedWatersRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook {
	@Shadow(remap = false)
	private int timeUntilLured;

	@Inject(method = "catchingFish", at = @At("TAIL"), require = 0, remap = false)
	private void hemomancy$accelerateChummedWaters(BlockPos pos, CallbackInfo ci) {
		Entity owner = ((FishingHook) (Object) this).getOwner();
		if (!(owner instanceof Player player)) {
			return;
		}
		FishingHook hook = (FishingHook) (Object) this;
		if (!ChummedWatersAreaManager.isChummed(player.level(), hook.position())) {
			return;
		}
		this.timeUntilLured = ChummedWatersRules.adjustedLureTime(this.timeUntilLured, 0);
	}
}
