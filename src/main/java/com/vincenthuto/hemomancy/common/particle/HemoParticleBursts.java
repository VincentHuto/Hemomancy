package com.vincenthuto.hemomancy.common.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;

/**
 * Shaped server bursts, where a random box spread would read as a cloud. Each particle is sent with a count of 0, so
 * its offset is its direction and {@code speed} its speed.
 */
public final class HemoParticleBursts {
	private static final double GOLDEN_ANGLE = 2.399963D;

	private HemoParticleBursts() {
	}

	/**
	 * Flung outward from one point along evenly spread directions. {@code lift} scales the vertical part: below 1
	 * flattens the burst toward a ring, 0 lays it flat.
	 */
	public static void outward(ServerLevel level, ParticleOptions options, double x, double y, double z, int count,
			double speed, double lift) {
		for (int i = 0; i < count; i++) {
			double up = 1.0D - 2.0D * (i + 0.5D) / count;
			double flat = Math.sqrt(1.0D - up * up);
			double angle = i * GOLDEN_ANGLE;
			level.sendParticles(options, x, y, z, 0, Math.cos(angle) * flat, up * lift, Math.sin(angle) * flat, speed);
		}
	}

	/** Sprayed up a column, such as a trunk, from its foot to {@code height} above it, each one rising. */
	public static void upward(ServerLevel level, ParticleOptions options, double x, double y, double z, int count,
			double height, double spread, double speed) {
		for (int i = 0; i < count; i++) {
			double angle = i * GOLDEN_ANGLE;
			double at = height * (i + level.random.nextDouble()) / count;
			level.sendParticles(options, x + Math.cos(angle) * spread, y + at, z + Math.sin(angle) * spread, 0,
					Math.cos(angle) * 0.25D, 1.0D, Math.sin(angle) * 0.25D, speed);
		}
	}
}
