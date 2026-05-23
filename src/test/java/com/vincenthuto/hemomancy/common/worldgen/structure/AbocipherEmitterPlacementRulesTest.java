package com.vincenthuto.hemomancy.common.worldgen.structure;

final class AbocipherEmitterPlacementRulesTest {
	public static void main(String[] args) {
		spreadsHarbingerOutpostEmittersAcrossThreeFloors();
		clampsHarbingerOutpostEmittersInsideShortBoxes();
	}

	private static void spreadsHarbingerOutpostEmittersAcrossThreeFloors() {
		int minY = 64;
		int maxY = 83;

		assertEquals(3, AbocipherEmitterPlacementRules.HARBINGER_OUTPOST_FLOOR_COUNT,
				"harbinger outposts should have lower, middle, and upper ambience bands");
		assertEquals(67, AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 0),
				"lower ambience should start above the first floor");
		assertEquals(73, AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 1),
				"middle ambience should move off the first floor");
		assertEquals(79, AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 2),
				"upper ambience should reach the top floor");
	}

	private static void clampsHarbingerOutpostEmittersInsideShortBoxes() {
		int minY = 10;
		int maxY = 12;

		for (int floorIndex = 0; floorIndex < AbocipherEmitterPlacementRules.HARBINGER_OUTPOST_FLOOR_COUNT;
				floorIndex++) {
			int y = AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, floorIndex);
			assertTrue(y >= minY && y <= maxY, "emitter Y should remain inside short structure boxes");
		}
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
