package com.huto.hemomancy.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class EffectTest extends Effect {

	public EffectTest(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Test Effect");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return super.isReady(duration, amplifier);
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier,
			double health) {
		super.affectEntity(source, indirectSource, entityLivingBaseIn, amplifier, health);

	}

	@Override
	public void performEffect(LivingEntity entity, int amplifier) {
		super.performEffect(entity, amplifier);
	}

}
