package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Sanguine Fertility. On an animal (a dose of a Harbinger's blood by plain Projection, core-loop Track 5) it works
 * on that animal alone: an adult that can fall in love does, with the usual breeding cooldown, and a baby grows. On
 * anything else (a player who drank the potion) it reaches the animals around them: adults are put in love and
 * babies grow, faster with the amplifier. Affected animals show red (breeding) and green (growth) glow.
 */
public class SanguineFertilityEffect extends MobEffect {

	public SanguineFertilityEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return true;
		Level level = entity.level();
		if (entity instanceof Animal dosed) {
			if (!level.isClientSide) {
				if (dosed.isBaby()) dosed.ageUp(2 + amplifier);
				else if (dosed.getAge() == 0 && dosed.canFallInLove() && !dosed.isInLove()) dosed.setInLove(null);
			}
			return true;
		}

		double radius = 6.0 + amplifier * 2.0;
		AABB area = entity.getBoundingBox().inflate(radius);
		List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);

		// Ticks between breed attempts — higher amplifier = more frequent
		int breedInterval = Math.max(40, 100 - amplifier * 20);
		// Growth ticks added per effect tick for babies — scales with amplifier
		int growthBoost = 2 + amplifier;

		for (Animal animal : animals) {
			double ax = animal.getX();
			double ay = animal.getY() + animal.getBbHeight() * 0.5;
			double az = animal.getZ();

			if (animal.isBaby()) {
				// Server: accelerate growth
				if (!level.isClientSide) {
					animal.ageUp(growthBoost);
				}
				// Client: green glow particles rising from the baby
				if (level.isClientSide && level.getGameTime() % 4 == 0) {
					double ox = (level.random.nextDouble() - 0.5) * animal.getBbWidth();
					double oz = (level.random.nextDouble() - 0.5) * animal.getBbWidth();
					level.addParticle(
							HemoParticleData.hitGlow(ParticleColor.GREEN),
							ax + ox, ay, az + oz,
							0, 0.02, 0);
				}
			} else {
				// Server: trigger breeding on eligible adults
				if (!level.isClientSide && level.getGameTime() % breedInterval == 0) {
					if (animal.getAge() == 0 && !animal.isInLove()) {
						animal.setInLove(null);
					}
				}
				// Client: red glow particles on adults
				if (level.isClientSide && level.getGameTime() % 3 == 0) {
					double ox = (level.random.nextDouble() - 0.5) * animal.getBbWidth();
					double oz = (level.random.nextDouble() - 0.5) * animal.getBbWidth();
					level.addParticle(
							HemoParticleData.hitGlow(ParticleColor.RED),
							ax + ox, ay, az + oz,
							0, 0.03, 0);
				}
			}
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}

