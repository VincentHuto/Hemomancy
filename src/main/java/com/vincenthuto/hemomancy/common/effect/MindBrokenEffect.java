package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

/**
 * Mind Broken: a Silentium's will snapped back out of a possession. It weakens the body, draws the Will and the hive's
 * whispers, and bars the rite until it passes. Nothing cures it: not milk, not a totem.
 */
public class MindBrokenEffect extends MobEffect {
	public MindBrokenEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance instance) {
		// No cures: the debuff is the possession rite's cooldown.
	}
}
