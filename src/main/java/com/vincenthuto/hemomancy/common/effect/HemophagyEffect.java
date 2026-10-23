package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hemophagy — applied by the Hollow Vessel's Empty Pulse. While active, the
 * victim's healing sources are severely dampened. The heal reduction is the
 * {@link HealModifier#HEMOPHAGY} entry of the heal-modifier table.
 */
public class HemophagyEffect extends MobEffect {

	public HemophagyEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
	
		return true;
	}
}
