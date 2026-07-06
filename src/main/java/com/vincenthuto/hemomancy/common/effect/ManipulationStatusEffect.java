package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ManipulationStatusEffect extends MobEffect {
	private final String id;

	public ManipulationStatusEffect(String id, MobEffectCategory category, int color) {
		super(category, color);
		this.id = id;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy." + id);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return false;
	}
}
