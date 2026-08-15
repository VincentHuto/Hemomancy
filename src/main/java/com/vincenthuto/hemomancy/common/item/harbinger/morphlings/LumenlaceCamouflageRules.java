package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class LumenlaceCamouflageRules {
	public static final int STILLNESS_REQUIRED_TICKS = 40;
	public static final double MAX_STILL_DELTA_SQR = 0.0036D;

	private LumenlaceCamouflageRules() {
	}

	public static boolean isEligible(int maturity, boolean inWater, int lightLevel) {
		return maturity >= 3 && (inWater || lightLevel <= 7);
	}

	public static boolean isStillEnough(double deltaMovementSqr) {
		return deltaMovementSqr <= MAX_STILL_DELTA_SQR;
	}

	public static boolean shouldCamouflage(long now, long stillSince) {
		return stillSince > 0L && now - stillSince >= STILLNESS_REQUIRED_TICKS;
	}
}
