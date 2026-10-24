package com.vincenthuto.hemomancy.common.worldgen.structure.voyager;

public final class ActiveHarbingerVoyagerVesselPlacementRules {
	public static final int MIN_WATER_DEPTH = 5;
	public static final int MAX_SURFACE_VARIATION = 1;

	private ActiveHarbingerVoyagerVesselPlacementRules() {
	}

	public static boolean canPlaceVessel(boolean waterColumn, boolean stableSurface, int waterDepth,
			int surfaceVariation) {
		return waterColumn
				&& stableSurface
				&& waterDepth >= MIN_WATER_DEPTH
				&& surfaceVariation <= MAX_SURFACE_VARIATION;
	}
}
