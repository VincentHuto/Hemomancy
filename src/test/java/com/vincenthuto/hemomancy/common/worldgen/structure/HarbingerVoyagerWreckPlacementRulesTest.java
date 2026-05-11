package com.vincenthuto.hemomancy.common.worldgen.structure;

final class HarbingerVoyagerWreckPlacementRulesTest {
	public static void main(String[] args) {
		rejectsDryShallowOrLiquidAnchors();
		acceptsStableSubmergedOceanFloors();
		rejectsRoughOceanFloors();
	}

	private static void rejectsDryShallowOrLiquidAnchors() {
		assertFalse(HarbingerVoyagerWreckPlacementRules.canPlaceWreck(false, true, false,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH,
				HarbingerVoyagerWreckPlacementRules.MAX_FLOOR_VARIATION),
				"voyager wrecks should not generate outside submerged columns");
		assertFalse(HarbingerVoyagerWreckPlacementRules.canPlaceWreck(true, true, false,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH - 1,
				HarbingerVoyagerWreckPlacementRules.MAX_FLOOR_VARIATION),
				"voyager wrecks should require enough water depth for salvage exploration");
		assertFalse(HarbingerVoyagerWreckPlacementRules.canPlaceWreck(true, true, true,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH,
				HarbingerVoyagerWreckPlacementRules.MAX_FLOOR_VARIATION),
				"voyager wrecks should not anchor on liquid floor blocks");
	}

	private static void acceptsStableSubmergedOceanFloors() {
		assertTrue(HarbingerVoyagerWreckPlacementRules.canPlaceWreck(true, true, false,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH,
				HarbingerVoyagerWreckPlacementRules.MAX_FLOOR_VARIATION),
				"stable underwater floors are valid wreck anchors");
	}

	private static void rejectsRoughOceanFloors() {
		assertFalse(HarbingerVoyagerWreckPlacementRules.canPlaceWreck(true, true, false,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH,
				HarbingerVoyagerWreckPlacementRules.MAX_FLOOR_VARIATION + 1),
				"voyager wrecks should not float across sharp floor variation");
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
