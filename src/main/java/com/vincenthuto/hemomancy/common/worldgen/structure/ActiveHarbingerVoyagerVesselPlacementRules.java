package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerVesselPlacementRules {
	static final int MIN_WATER_DEPTH = 5;
	static final int MAX_SURFACE_VARIATION = 1;

	private ActiveHarbingerVoyagerVesselPlacementRules() {
	}

	static boolean canPlaceVessel(boolean waterColumn, boolean stableSurface, int waterDepth,
			int surfaceVariation) {
		return waterColumn
				&& stableSurface
				&& waterDepth >= MIN_WATER_DEPTH
				&& surfaceVariation <= MAX_SURFACE_VARIATION;
	}
}
