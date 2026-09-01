package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public final class TerrestrialSpeculumRules {
	public static final int REQUIRED_DEGREE = 2;
	public static final int BLOOD_COST = 1_000;
	public static final double MAX_ORIGIN_DISTANCE_SQR = 64.0D;

	private TerrestrialSpeculumRules() {
	}

	public static boolean canManifest(int degree, boolean bloodActive, int claimedVeins, boolean replaceable) {
		return degree >= REQUIRED_DEGREE && bloodActive && claimedVeins > 0 && replaceable;
	}

	public static boolean canTravel(boolean hasSpeculum, boolean ownsOrigin, double originDistanceSqr,
			boolean ownsDestination, boolean destinationStented, double bloodMl) {
		return hasSpeculum && ownsOrigin && originDistanceSqr <= MAX_ORIGIN_DISTANCE_SQR
				&& ownsDestination && destinationStented && bloodMl >= BLOOD_COST;
	}
}
