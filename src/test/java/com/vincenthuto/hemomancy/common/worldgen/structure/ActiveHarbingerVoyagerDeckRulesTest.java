package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerDeckRulesTest {
	public static void main(String[] args) {
		acceptsDryAirAboveSturdyDeckInsideBounds();
		rejectsWaterOrMissingDeckFloor();
	}

	private static void acceptsDryAirAboveSturdyDeckInsideBounds() {
		assertTrue(ActiveHarbingerVoyagerDeckRules.canSpawnOnDeck(true, true, true, true),
				"crew should be allowed on dry air over a sturdy deck inside the vessel bounds");
	}

	private static void rejectsWaterOrMissingDeckFloor() {
		assertFalse(ActiveHarbingerVoyagerDeckRules.canSpawnOnDeck(true, true, false, true),
				"crew should not spawn in water on active vessels");
		assertFalse(ActiveHarbingerVoyagerDeckRules.canSpawnOnDeck(true, true, true, false),
				"crew should not spawn without a sturdy deck floor");
		assertFalse(ActiveHarbingerVoyagerDeckRules.canSpawnOnDeck(false, true, true, true),
				"crew should not spawn outside the vessel bounding box");
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
