package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants increased mining speed and bonus health
 * regeneration when underground (below Y=50), simulating a mole's natural
 * tunneling instincts. Applied by the mole morphling while it is attached
 * to the player. The mining speed bonus is applied via attribute modifier
 * in EffectInit. Night vision is also periodically granted underground.
 */
public class BurrowersInstinctEffect extends MobEffect {

	/** Y-level below which the underground bonuses apply. */
	private static final double UNDERGROUND_THRESHOLD = 50.0;

	public BurrowersInstinctEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return;

		// Bonus healing when underground (below Y=50)
		if (entity.getY() < UNDERGROUND_THRESHOLD && entity.getHealth() < entity.getMaxHealth()) {
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
		return Component.translatable("effect.hemomancy.burrowers_instinct");
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
