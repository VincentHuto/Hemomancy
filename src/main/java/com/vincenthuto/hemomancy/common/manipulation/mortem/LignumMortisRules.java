package com.vincenthuto.hemomancy.common.manipulation.mortem;

public final class LignumMortisRules {
	public static final int MAX_BLOCKS = 128;
	public static final int MAX_RADIUS = 24;
	public static final double BASE_BLOCKS_PER_SECOND = 2.0D;
	public static final double MAX_BLOCKS_PER_SECOND = 8.0D;

	private LignumMortisRules() {
	}

	public static int maxRadius(int mastery, double mortemAlignment, double animusAlignment,
			double reachMultiplier) {
		int clampedMastery = Math.max(0, Math.min(4, mastery));
		double mortem = Math.max(0.0D, Math.min(100.0D, mortemAlignment));
		double animus = Math.max(0.0D, Math.min(100.0D, animusAlignment));
		double base = 6.0D + clampedMastery * 2.0D + Math.floor((mortem + animus * 0.5D) / 20.0D);
		return Math.min(MAX_RADIUS, Math.max(1, (int) Math.round(base * Math.max(0.0D, reachMultiplier))));
	}

	public static double blocksPerSecond(double farthestDistance) {
		return Math.min(MAX_BLOCKS_PER_SECOND,
				BASE_BLOCKS_PER_SECOND + Math.max(0.0D, farthestDistance) * 0.25D);
	}

	public static int overlayBand(double distance, double maxRadius) {
		if (maxRadius <= 0.0D) return 3;
		return Math.min(3, (int) Math.floor(Math.max(0.0D, distance) / maxRadius * 4.0D));
	}
}
