package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A buff granted after using a Hemomancy blood manipulation.
 * While active, the next MnA spell cast will cost less mana.
 * This is a marker effect — the actual cost reduction is applied via
 * the {@code CalculatingManaCostEvent} handler in {@code BloodTitheHandler}.
 * <p>
 * Part of the Spell → Manipulation combo system (MnA compatibility feature 6d).
 */
public class SanguineClarityEffect extends MobEffect {

	public SanguineClarityEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		// Marker effect — no tick behavior. Cost reduction is checked at spell cast time.
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.sanguine_clarity");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return false;
	}
}
