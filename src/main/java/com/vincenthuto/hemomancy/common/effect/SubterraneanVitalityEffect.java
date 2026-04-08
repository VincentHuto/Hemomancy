package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants increased attack speed (simulating Haste /
 * mining efficiency) and bonus health regeneration when underground (below
 * Y=50). Applied by the worm morphling while it is attached to the player.
 * The attack speed bonus is applied via attribute modifier in EffectInit.
 */
public class SubterraneanVitalityEffect extends MobEffect {

	public SubterraneanVitalityEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return;

		// Bonus healing when underground (below Y=50)
		if (entity.getY() < 50.0 && entity.getHealth() < entity.getMaxHealth()) {
			float healAmount = 0.5f + amplifier * 0.25f;
			entity.heal(healAmount);
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.subterranean_vitality");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

}
