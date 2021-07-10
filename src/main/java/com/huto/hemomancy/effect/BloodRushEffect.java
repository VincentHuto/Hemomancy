package com.huto.hemomancy.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class BloodRushEffect extends Effect {

	public BloodRushEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Blood Rush");
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
