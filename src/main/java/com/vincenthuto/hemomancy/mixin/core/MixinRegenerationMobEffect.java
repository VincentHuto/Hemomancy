package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect", remap = false)
public abstract class MixinRegenerationMobEffect {
	@Redirect(
			method = "applyEffectTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"),
			remap = false)
	private void hemomancy$scaleRegeneration(LivingEntity target, float amount) {
		target.heal(MixinHooks.scalePotionResponse(target, amount));
	}
}
