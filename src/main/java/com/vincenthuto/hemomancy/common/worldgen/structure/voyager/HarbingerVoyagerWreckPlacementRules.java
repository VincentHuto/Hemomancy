package com.vincenthuto.hemomancy.common.worldgen.structure.voyager;

public final class HarbingerVoyagerWreckPlacementRules {
	public static final int MIN_WATER_DEPTH = 7;
	public static final int MAX_FLOOR_VARIATION = 5;

	private HarbingerVoyagerWreckPlacementRules() {
	}

	public static boolean canPlaceWreck(boolean submerged, boolean stableFloor, boolean liquidFloor,
			int waterDepth, int floorVariation) {
		return submerged
				&& stableFloor
				&& !liquidFloor
				&& waterDepth >= MIN_WATER_DEPTH
				&& floorVariation <= MAX_FLOOR_VARIATION;
	}
}
