package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants the player poison immunity and a small
 * movement speed bonus, embodying the centipede's venomous resilience.
 * Applied by the centipede morphling while it is attached to the player.
 * The speed bonus is applied via attribute modifier in EffectInit.
 * Poison immunity is handled by removing the Poison effect each tick.
 */
public class VenomousResilienceEffect extends MobEffect {

	public VenomousResilienceEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return true;
		// Passive poison immunity: strip the Poison effect if present
		if (entity.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
			entity.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
		}
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.venomous_resilience");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}

