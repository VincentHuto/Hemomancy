package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Well Rested (core-loop Track 10): a full night's sleep. It does nothing on its own tick; while it lasts the blood
 * regeneration tick reads it ({@code BloodRegenRules.multiplier}) and the body recovers faster.
 */
public final class WellRestedEffect extends MobEffect {
	public WellRestedEffect(MobEffectCategory category, int color) {
		super(category, color);
	}
}
