package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

/**
 * The shared home-condition bonus (Brined Court spec, "Home condition"): unless a power says otherwise, a strain at
 * home gets +50% to durations and rates and +25% to radii.
 */
public final class MorphlingHomeBonus {
	public static final double DURATION_AND_RATE = 1.5D;
	public static final double RADIUS = 1.25D;

	private MorphlingHomeBonus() {
	}

	public static int duration(int ticks, boolean home) {
		return home ? (int) Math.round(ticks * DURATION_AND_RATE) : ticks;
	}

	public static double rate(double amount, boolean home) {
		return home ? amount * DURATION_AND_RATE : amount;
	}

	public static double radius(double blocks, boolean home) {
		return home ? blocks * RADIUS : blocks;
	}
}
