package com.vincenthuto.hemomancy.common.effect;

import java.util.List;

import com.vincenthuto.hemomancy.client.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A beneficial effect that causes nearby animals to enter love mode (breeding)
 * and accelerates the growth of baby animals. The radius and speed scale with
 * the amplifier level. Affected animals emit green (baby growth) and red
 * (breeding) glow particles.
 */
public class SanguineFertilityEffect extends MobEffect {

	public SanguineFertilityEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return;
		Level level = entity.level();

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
							HitGlowParticleFactory.createData(ParticleColor.GREEN),
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
							HitGlowParticleFactory.createData(ParticleColor.RED),
							ax + ox, ay, az + oz,
							0, 0.03, 0);
				}
			}
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.sanguine_fertility");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
