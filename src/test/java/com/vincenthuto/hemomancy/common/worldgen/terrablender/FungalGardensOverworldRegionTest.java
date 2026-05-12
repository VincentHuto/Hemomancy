package com.vincenthuto.hemomancy.common.worldgen.terrablender;

final class FungalGardensOverworldRegionTest {
	public static void main(String[] args) {
		keepsFungalGardensLandReplacementSeparateFromReefRegion();
	}

	private static void keepsFungalGardensLandReplacementSeparateFromReefRegion() {
		assertEquals("fungal_gardens_overworld", FungalGardensOverworldRegion.REGION_PATH,
				"named fungal gardens region should not be confused with reef generation");
		assertContains(FungalGardensOverworldRegion.replacementTargetPaths(), "minecraft:jungle",
				"optional fungal gardens land replacement may still target jungle");
		assertContains(FungalGardensOverworldRegion.replacementTargetPaths(), "minecraft:mangrove_swamp",
				"optional fungal gardens land replacement may still target mangrove swamp");
	}

	private static void assertContains(String[] values, String expected, String message) {
		for (String value : values) {
			if (expected.equals(value)) {
				return;
			}
		}
		throw new AssertionError(message);
	}

	private static void assertEquals(String expected, String actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
		}
	}
}
