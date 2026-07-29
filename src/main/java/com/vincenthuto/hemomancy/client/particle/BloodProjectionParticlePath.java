package com.vincenthuto.hemomancy.client.particle;

import net.minecraft.world.phys.Vec3;

public final class BloodProjectionParticlePath {
	private static final double MINIMUM_ARC_HEIGHT = 0.2D;
	private static final double ARC_HEIGHT_PER_BLOCK = 0.14D;
	private static final double MAXIMUM_ARC_HEIGHT = 1.1D;
	private static final double MAXIMUM_LATERAL_DEVIATION = 0.22D;
	private static final double MAXIMUM_VERTICAL_VARIATION = 0.12D;

	private BloodProjectionParticlePath() {
	}

	public static Vec3 arcDeviation(Vec3 source, Vec3 target, double lateralNoise, double verticalNoise) {
		Vec3 path = target.subtract(source);
		double distance = path.length();
		if (distance < 1.0E-8D) {
			return Vec3.ZERO;
		}

		Vec3 sideways = path.cross(new Vec3(0.0D, 1.0D, 0.0D));
		if (sideways.lengthSqr() < 1.0E-8D) {
			sideways = path.cross(new Vec3(1.0D, 0.0D, 0.0D));
		}

		double arcHeight = Math.min(MAXIMUM_ARC_HEIGHT,
				MINIMUM_ARC_HEIGHT + distance * ARC_HEIGHT_PER_BLOCK);
		double clampedLateralNoise = Math.max(-1.0D, Math.min(1.0D, lateralNoise));
		double clampedVerticalNoise = Math.max(-1.0D, Math.min(1.0D, verticalNoise));
		return sideways.normalize().scale(clampedLateralNoise * MAXIMUM_LATERAL_DEVIATION)
				.add(0.0D, arcHeight + clampedVerticalNoise * MAXIMUM_VERTICAL_VARIATION, 0.0D);
	}

	public static Vec3 position(Vec3 source, Vec3 target, Vec3 deviation, double progress) {
		double clampedProgress = Math.max(0.0D, Math.min(1.0D, progress));
		double deviationEnvelope = Math.sin(Math.PI * clampedProgress);
		return source.lerp(target, clampedProgress).add(deviation.scale(deviationEnvelope));
	}

	public static double progress(int age, int lifetime) {
		if (lifetime <= 1) {
			return 1.0D;
		}
		return Math.max(0.0D, Math.min(1.0D, age / (double) (lifetime - 1)));
	}
}
