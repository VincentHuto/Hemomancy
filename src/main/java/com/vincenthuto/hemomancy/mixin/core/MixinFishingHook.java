package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.world.chum.ChumRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A hook in a chum pool is lured faster (core-loop Track 7): after vanilla counts its lure down for the tick, the pool
 * counts it down a little more, by its strength ({@link ChumRules#adjustedLureTime}). Restored from the cut Blood Chum
 * item (2026-11-03, cut as an untaught convenience, not for compatibility); NeoForge has no lure event.
 */
@Mixin(FishingHook.class)
public abstract class MixinFishingHook {
	@Shadow(remap = false)
	private int timeUntilLured;

	@Inject(method = "catchingFish", at = @At("TAIL"), remap = false)
	private void hemomancy$chumLuresFaster(BlockPos pos, CallbackInfo ci) {
		FishingHook hook = (FishingHook) (Object) this;
		if (timeUntilLured <= 0 || !(hook.level() instanceof ServerLevel level)) return;
		double strength = ChumPools.strengthAt(level, hook.position());
		if (strength > 0.0D) timeUntilLured = ChumRules.adjustedLureTime(timeUntilLured, strength);
	}
}
