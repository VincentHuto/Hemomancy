package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

public final class CircusPavilionPlacementRules {
	public static final int MAX_SURFACE_VARIATION = 5;

	private CircusPavilionPlacementRules() {
	}

	public static boolean canPlacePavilion(boolean dryGround, boolean stableGround, boolean clearFootprint,
			int surfaceVariation) {
		return dryGround
				&& stableGround
				&& clearFootprint
				&& surfaceVariation <= MAX_SURFACE_VARIATION;
	}

	public static int surfaceVariation(int... heights) {
		int minimum = heights[0];
		int maximum = heights[0];
		for (int height : heights) {
			minimum = Math.min(minimum, height);
			maximum = Math.max(maximum, height);
		}
		return maximum - minimum;
	}
}
