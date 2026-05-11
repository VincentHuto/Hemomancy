package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class BloodLanternJellyMovementRules {
	public static final double MAX_HORIZONTAL_DRIFT = 0.018D;
	public static final double MAX_VERTICAL_DRIFT = 0.012D;
	public static final double DAMPING = 0.86D;

	private BloodLanternJellyMovementRules() {
	}

	public static boolean canDrift(boolean inWater, boolean alive) {
		return inWater && alive;
	}

	public static double verticalImpulse(boolean waterAbove, boolean waterBelow, double pulse) {
		if (!waterAbove) {
			return -MAX_VERTICAL_DRIFT * 1.8D;
		}
		if (!waterBelow) {
			return MAX_VERTICAL_DRIFT * 1.6D;
		}
		return clamp(pulse, -1.0D, 1.0D) * MAX_VERTICAL_DRIFT;
	}

	public static double horizontalDrift(double pulse) {
		return clamp(pulse, -1.0D, 1.0D) * MAX_HORIZONTAL_DRIFT;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
