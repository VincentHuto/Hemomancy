package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerVesselPlacementRulesTest {
	public static void main(String[] args) {
		rejectsDryOrShallowColumns();
		acceptsValidOceanSurfaceColumns();
		rejectsRoughOceanSurface();
	}

	private static void rejectsDryOrShallowColumns() {
		assertFalse(ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(false, true,
				ActiveHarbingerVoyagerVesselPlacementRules.MIN_WATER_DEPTH,
				ActiveHarbingerVoyagerVesselPlacementRules.MAX_SURFACE_VARIATION),
				"active voyager vessels should require ocean water columns");
		assertFalse(ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(true, true,
				ActiveHarbingerVoyagerVesselPlacementRules.MIN_WATER_DEPTH - 1,
				ActiveHarbingerVoyagerVesselPlacementRules.MAX_SURFACE_VARIATION),
				"active voyager vessels should not generate in shallow water");
	}

	private static void acceptsValidOceanSurfaceColumns() {
		assertTrue(ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(true, true,
				ActiveHarbingerVoyagerVesselPlacementRules.MIN_WATER_DEPTH,
				ActiveHarbingerVoyagerVesselPlacementRules.MAX_SURFACE_VARIATION),
				"valid ocean-surface columns should support active voyager vessels");
	}

	private static void rejectsRoughOceanSurface() {
		assertFalse(ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(true, true,
				ActiveHarbingerVoyagerVesselPlacementRules.MIN_WATER_DEPTH,
				ActiveHarbingerVoyagerVesselPlacementRules.MAX_SURFACE_VARIATION + 1),
				"active voyager vessels should not straddle sharp surface variation");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}
}
