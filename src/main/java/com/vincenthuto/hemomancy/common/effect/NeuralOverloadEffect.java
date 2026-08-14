package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Neural Overload — a harmful neurotic-tendency effect representing
 * escalating nervous system disruption. Stacks with repeated hits.
 * 
 * Amp 0: Movement slowdown (nerve signal delay)
 * Amp 1: + Nausea (sensory scrambling)
 * Amp 2+: + Weakness (motor nerve failure) + increased slowdown
 * 
 * Each tick spawns electric spark particles around the victim.
 */
public class NeuralOverloadEffect extends MobEffect {

	public NeuralOverloadEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return true;

		// Escalating secondary effects based on amplifier (stack level)
		if (amplifier >= 1) {
			// Amp 1+: sensory scrambling (nausea)
			if (!entity.hasEffect(MobEffects.CONFUSION)) {
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
			}
		}
		if (amplifier >= 2) {
			// Amp 2+: motor nerve failure (weakness)
			if (!entity.hasEffect(MobEffects.WEAKNESS)) {
				entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, amplifier - 2, false, false));
			}
		}

		// Spawn electric spark particles on the server
		if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
			double x = entity.getX();
			double y = entity.getY() + entity.getBbHeight() * 0.5;
			double z = entity.getZ();
			int particleCount = 1 + amplifier;
			serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
					particleCount, 0.3, 0.4, 0.3, 0.02);
		}
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.neural_overload");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		// Tick every 20 ticks (1 second)
		return duration % 20 == 0;
	}

}

