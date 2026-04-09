package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A buff granted after casting an MnA spell with Blood affinity components.
 * While active, the next Hemomancy blood manipulation used will cost less blood.
 * This is a marker effect — the actual cost reduction is checked by the
 * manipulation system when calculating blood cost.
 * <p>
 * Part of the Spell → Manipulation combo system (MnA compatibility feature 6d).
 */
public class ArcaneResonanceEffect extends MobEffect {

	public ArcaneResonanceEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		// Marker effect — no tick behavior. Cost reduction is checked at manipulation time.
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.arcane_resonance");
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
