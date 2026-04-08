package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that periodically grants the player absorption hearts,
 * simulating a parasitic organism anchoring into the host's circulatory system.
 * Applied by the lamprey morphling while it is attached to the player. The
 * absorption amount scales with the amplifier.
 */
public class ParasiticAnchorEffect extends MobEffect {

	public ParasiticAnchorEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return;

		// Grant absorption hearts periodically (caps at 4 + amplifier * 2)
		float maxAbsorption = 4.0f + amplifier * 2.0f;
		if (entity.getAbsorptionAmount() < maxAbsorption) {
			entity.setAbsorptionAmount(Math.min(entity.getAbsorptionAmount() + 1.0f, maxAbsorption));
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.parasitic_anchor");
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
