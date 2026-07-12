package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect", remap = false)
public abstract class MixinHealOrHarmMobEffect {
	@Redirect(
			method = { "applyEffectTick", "applyInstantenousEffect" },
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"),
			remap = false)
	private void hemomancy$scaleInstantHealth(LivingEntity target, float amount) {
		target.heal(MixinHooks.scalePotionResponse(target, amount));
	}
}
