package com.huto.hemomancy.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BloodRushEffect extends MobEffect {

	public BloodRushEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("Blood Rush");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier,
			double health) {
		super.affectEntity(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public void performEffect(LivingEntity entity, int amplifier) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(0.5F);
		}
	}

}
