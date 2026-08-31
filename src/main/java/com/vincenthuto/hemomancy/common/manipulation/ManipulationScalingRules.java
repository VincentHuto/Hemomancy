package com.vincenthuto.hemomancy.common.manipulation;

public final class ManipulationScalingRules {
	private ManipulationScalingRules() {
	}

	public static double scaled(double minimum, double maximum, float chargeTicks, int requiredTicks) {
		return minimum + (maximum - minimum)
				* ManipulationCastingRules.chargeFraction(chargeTicks, requiredTicks);
	}

	public static int scaledInt(int minimum, int maximum, float chargeTicks, int requiredTicks) {
		return (int) Math.round(scaled(minimum, maximum, chargeTicks, requiredTicks));
	}

	public static int scaledCount(int minimum, int maximum, float chargeTicks, int requiredTicks) {
		return Math.max(minimum, (int) Math.ceil(maximum
				* ManipulationCastingRules.chargeFraction(chargeTicks, requiredTicks)));
	}
}
