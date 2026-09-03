package com.vincenthuto.hemomancy.common.worldgen.structure;

final class CircusPavilionPlacementRulesTest {
	public static void main(String[] args) {
		rejectsWetOrObstructedFootprints();
		rejectsGroundTooRoughForThePavilion();
		acceptsDryClearLevelGround();
		measuresTheFullSampledSurfaceRange();
	}

	private static void rejectsWetOrObstructedFootprints() {
		assertFalse(CircusPavilionPlacementRules.canPlacePavilion(false, true, true, 0),
				"the pavilion should not generate over wet ground");
		assertFalse(CircusPavilionPlacementRules.canPlacePavilion(true, false, true, 0),
				"the pavilion should require a stable foundation");
		assertFalse(CircusPavilionPlacementRules.canPlacePavilion(true, true, false, 0),
				"the pavilion should not cut through an occupied footprint");
	}

	private static void rejectsGroundTooRoughForThePavilion() {
		assertFalse(CircusPavilionPlacementRules.canPlacePavilion(true, true, true,
				CircusPavilionPlacementRules.MAX_SURFACE_VARIATION + 1),
				"the pavilion should not bridge sharp terrain changes");
	}

	private static void acceptsDryClearLevelGround() {
		assertTrue(CircusPavilionPlacementRules.canPlacePavilion(true, true, true,
				CircusPavilionPlacementRules.MAX_SURFACE_VARIATION),
				"dry clear ground within the terrain limit should accept the pavilion");
	}

	private static void measuresTheFullSampledSurfaceRange() {
		assertEquals(5, CircusPavilionPlacementRules.surfaceVariation(64, 69, 66, 68, 65),
				"surface variation should span the lowest and highest footprint samples");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + " expected " + expected + " but was " + actual);
		}
	}
}
