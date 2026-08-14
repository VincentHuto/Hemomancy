package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Verdigris Aura — an anti-blood area-of-effect for Unstained players who
 * have unlocked Clarity. Generates a copper-green field around the player
 * that weakens nearby hemomancy-tagged mobs (slowness + minor wither) and
 * emits teal-green particles.
 * <p>
 * Radius scales with amplifier: {@code 6 + amplifier * 2} blocks.
 * The aura pulses every 3 seconds (60 ticks).
 */
public class VerdigrisAuraEffect extends MobEffect {

	/** Base radius in blocks. */
	private static final double BASE_RADIUS = 6.0;
	/** Extra radius per amplifier level. */
	private static final double RADIUS_PER_AMP = 2.0;
	/** Tick interval between pulses (3 seconds). */
	private static final int PULSE_INTERVAL = 60;
	/** Duration of debuffs applied to targets (4 seconds). */
	private static final int DEBUFF_DURATION = 80;

	public VerdigrisAuraEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.level().isClientSide) return true;
		if (entity.tickCount % PULSE_INTERVAL != 0) return true;

		double radius = BASE_RADIUS + amplifier * RADIUS_PER_AMP;
		ServerLevel serverLevel = (ServerLevel) entity.level();

		// Spawn ambient verdigris particles in a ring
		int particleCount = 8 + amplifier * 4;
		for (int i = 0; i < particleCount; i++) {
			double angle = (2.0 * Math.PI * i) / particleCount;
			double px = entity.getX() + Math.cos(angle) * radius * 0.7;
			double pz = entity.getZ() + Math.sin(angle) * radius * 0.7;
			serverLevel.sendParticles(ParticleTypes.SCRAPE,
					px, entity.getY() + 0.5, pz,
					1, 0.2, 0.3, 0.2, 0.0);
		}

		// Apply debuffs to hemomancy mobs in range
		AABB area = entity.getBoundingBox().inflate(radius);
		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, target -> {
			if (target == entity) return false;
			if (!target.isAlive()) return false;
			// Only affect hemomancy-tagged mobs
			return target.getType().is(EntityInit.HEMOMANCY_MOB);
		});

		for (LivingEntity target : targets) {
			// Slowness I — copper patina corroding their movement
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION, 0, false, true, false));
			// Weakness I — blood magic disrupted
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION, 0, false, true, false));
		}
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.verdigris_aura");
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

