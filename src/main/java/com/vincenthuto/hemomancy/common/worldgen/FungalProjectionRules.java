package com.vincenthuto.hemomancy.common.worldgen;

public final class FungalProjectionRules {
	public static final int FIRST_VISIT_TICKS = 20 * 60 * 2;

	private FungalProjectionRules() {}

	public static boolean shouldStartProjection(boolean revelationWitnessed, int degree, boolean spineGranted) {
		return !revelationWitnessed && degree >= 7 && spineGranted;
	}

	public static boolean shouldForceReturn(int remainingTicks) {
		return remainingTicks <= 0;
	}

	public static boolean shouldSync(int remainingTicks) {
		return remainingTicks <= 0 || remainingTicks % 20 == 0;
	}
}
