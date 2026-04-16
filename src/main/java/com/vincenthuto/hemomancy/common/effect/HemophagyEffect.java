package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hemophagy — applied by the Hollow Vessel's Empty Pulse. While active, the
 * victim's healing sources are severely dampened. The actual heal reduction is
 * enforced via LivingHealEvent in HollowVesselEntity.
 */
public class HemophagyEffect extends MobEffect {

	public HemophagyEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.hemophagy");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return false;
	}
}
