package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** What the Iron Pillar, Spike and Wall shed as they erupt, while they stand, and as they go. */
final class FerricConstructEffects {
	private static final ParticleColor IRON = new ParticleColor(120, 118, 124);
	private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
	private static final ParticleColor CLOT = new ParticleColor(70, 6, 14);

	private FerricConstructEffects() {
	}

	static void erupt(Level level, RandomSource random, double x, double y, double z) {
		level.addParticle(HemoParticleData.darkGlow(CLOT), x, y, z, 0, 0.01, 0);
		for (int i = 0; i < 4; i++) {
			level.addParticle(HemoParticleData.emberSpark(IRON, 0.06F, 1F, 16), x, y, z,
					(random.nextDouble() - 0.5) * 0.08, random.nextDouble() * 0.05, (random.nextDouble() - 0.5) * 0.08);
		}
	}

	static void seep(Level level, double x, double y, double z) {
		level.addParticle(HemoParticleData.bloodCell(BLOOD), x, y, z, 0, 0, 0);
	}

	static void flake(Level level, double x, double y, double z) {
		level.addParticle(HemoParticleData.emberSpark(IRON, 0.031F, 0.8F, 20), x, y, z, 0, 0.01, 0);
	}

	static void expire(Level level, RandomSource random, double x, double y, double z) {
		puff(level, random, x, y, z);
	}

	static void dissolve(Level level, RandomSource random, double x, double y, double z) {
		puff(level, random, x, y, z);
		for (int i = 0; i < 6; i++) {
			level.addParticle(HemoParticleData.bloodCell(BLOOD), x, y, z,
					(random.nextDouble() - 0.5) * 0.06, random.nextDouble() * 0.03, (random.nextDouble() - 0.5) * 0.06);
		}
	}

	private static void puff(Level level, RandomSource random, double x, double y, double z) {
		for (int i = 0; i < 3; i++) {
			level.addParticle(HemoParticleData.darkGlow(CLOT), x, y, z,
					(random.nextDouble() - 0.5) * 0.02, 0.01, (random.nextDouble() - 0.5) * 0.02);
		}
	}
}
