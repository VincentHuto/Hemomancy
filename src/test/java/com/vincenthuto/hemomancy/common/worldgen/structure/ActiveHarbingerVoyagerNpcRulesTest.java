package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerNpcRulesTest {
	public static void main(String[] args) {
		voyagerAlwaysSpawns();
		votaryUsesOneInFiveRoll();
	}

	private static void voyagerAlwaysSpawns() {
		assertTrue(ActiveHarbingerVoyagerNpcRules.shouldSpawnVoyager(),
				"active voyager vessels should always spawn a Harbinger Voyager");
	}

	private static void votaryUsesOneInFiveRoll() {
		assertTrue(ActiveHarbingerVoyagerNpcRules.shouldSpawnVotaryWayfarer(0),
				"roll 0 should spawn the Votary Wayfarer companion");
		for (int roll = 1; roll < ActiveHarbingerVoyagerNpcRules.VOTARY_WAYFARER_CHANCE; roll++) {
			assertFalse(ActiveHarbingerVoyagerNpcRules.shouldSpawnVotaryWayfarer(roll),
					"non-zero companion rolls should not spawn the Votary Wayfarer");
		}
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
