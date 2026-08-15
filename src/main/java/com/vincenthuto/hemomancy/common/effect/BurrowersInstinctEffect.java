package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants increased mining speed and bonus health
 * regeneration when underground (below Y=50), simulating Irontooth's natural
 * tunneling instincts. Applied by the Irontooth Morphling while it is attached
 * to the player. The mining speed bonus is applied via attribute modifier
 * in EffectInit. Night vision is also periodically granted underground.
 */
public class BurrowersInstinctEffect extends MobEffect {
	private final String displayKey;

	/** Y-level below which the underground bonuses apply. */
	private static final double UNDERGROUND_THRESHOLD = 50.0;

	public BurrowersInstinctEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.burrowers_instinct");
	}

	public BurrowersInstinctEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;

		// Bonus healing when underground (below Y=50)
		if (entity.getY() < UNDERGROUND_THRESHOLD && entity.getHealth() < entity.getMaxHealth()) {
			float healAmount = 0.5f + amplifier * 0.25f;
			entity.heal(healAmount);
		}
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(displayKey);
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

}

