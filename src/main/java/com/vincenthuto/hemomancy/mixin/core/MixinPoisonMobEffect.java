package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect", remap = false)
public abstract class MixinPoisonMobEffect {
	@Redirect(
			method = "applyEffectTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
			remap = false)
	private boolean hemomancy$scalePoisonDamage(LivingEntity target, DamageSource source, float amount) {
		return target.hurt(source, MixinHooks.scalePoisonDamage(target, amount));
	}
}
