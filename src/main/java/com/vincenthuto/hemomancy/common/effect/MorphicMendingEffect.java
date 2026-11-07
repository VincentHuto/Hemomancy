package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * The Morphic Tonic's mending: flesh and blood regrow together. Health is healed here; the blood half is passive
 * income in {@code BloodVolumeEvents} at {@link MorphicTonicRules#BLOOD_PER_TICK}.
 */
public class MorphicMendingEffect extends MobEffect {
	public MorphicMendingEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(MorphicTonicRules.HEALTH_PER_PULSE);
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % MorphicTonicRules.HEALTH_PULSE_TICKS == 0;
	}
}
