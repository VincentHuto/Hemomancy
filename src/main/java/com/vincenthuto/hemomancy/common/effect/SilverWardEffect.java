package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Silver Ward — a passive beneficial effect for followers of the Unstained
 * path. Provides resistance to blood magic (via attribute modifiers defined
 * in EffectInit), a subtle armor bonus that scales with amplifier level,
 * and a visible teal particle aura every few seconds.
 * <p>
 * The armor bonus is applied via attribute modifier in EffectInit registration.
 * Per-tick logic spawns ambient particles to show the ward is active.
 */
public class SilverWardEffect extends MobEffect {

	/** Particle spawn interval in ticks (every 2 seconds). */
	private static final int PARTICLE_INTERVAL = 40;

	public SilverWardEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// Spawn ambient teal particles around the entity to visualize the ward
		if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
			if (entity.tickCount % PARTICLE_INTERVAL == 0) {
				int count = 3 + amplifier * 2;
				serverLevel.sendParticles(ParticleTypes.END_ROD,
						entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
						count, 0.5, 0.8, 0.5, 0.01);
			}
		}
	
		return true;
	}

	/**
	 * Returns the damage reduction multiplier for the Silver Ward at the given
	 * amplifier. Blood-sourced damage is reduced by 15% per amplifier level.
	 * <p>
	 * Call this from damage event handlers: {@code newDamage = damage * (1 - getReduction(amplifier))}
	 *
	 * @param amplifier the effect amplifier (0-based)
	 * @return reduction fraction (e.g. 0.15 for amplifier 0, 0.30 for amplifier 1)
	 */
	public static float getBloodDamageReduction(int amplifier) {
		return Math.min(0.60f, 0.15f * (amplifier + 1));
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.silver_ward");
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
