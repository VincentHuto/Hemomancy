package com.vincenthuto.hemomancy.common.worldgen.terrablender;

final class ErythrocoralReefRegionTest {
	public static void main(String[] args) {
		mapsOnlyDeepWarmOceanSlotToReef();
		defersEveryShallowOceanSlot();
		defersEveryNonOceanTerrainTable();
	}

	private static void mapsOnlyDeepWarmOceanSlotToReef() {
		String[][] oceans = ErythrocoralReefRegion.oceanBiomePaths();
		assertEquals(2, oceans.length, "ocean table should have deep and shallow rows");
		assertEquals(5, oceans[0].length, "deep ocean row should follow TerraBlender's five temperature slots");

		for (int temperatureIndex = 0; temperatureIndex < oceans[0].length; temperatureIndex++) {
			String expected = temperatureIndex == 3
					? ErythrocoralReefRegion.ERYTHROCORAL_REEF_PATH
					: ErythrocoralReefRegion.DEFERRED_PLACEHOLDER_PATH;
			assertEquals(expected, oceans[0][temperatureIndex],
					"only TerraBlender's deep warm/lukewarm ocean slot should become erythrocoral reef");
		}
	}

	private static void defersEveryShallowOceanSlot() {
		String[][] oceans = ErythrocoralReefRegion.oceanBiomePaths();
		assertEquals(5, oceans[1].length, "shallow ocean row should follow TerraBlender's five temperature slots");

		for (String biome : oceans[1]) {
			assertEquals(ErythrocoralReefRegion.DEFERRED_PLACEHOLDER_PATH, biome,
					"ordinary shallow oceans should be left to vanilla or other TerraBlender regions");
		}
	}

	private static void defersEveryNonOceanTerrainTable() {
		assertAllDeferred(ErythrocoralReefRegion.middleBiomePaths(), "middle biomes");
		assertAllDeferred(ErythrocoralReefRegion.middleBiomeVariantPaths(), "middle biome variants");
		assertAllDeferred(ErythrocoralReefRegion.plateauBiomePaths(), "plateau biomes");
		assertAllDeferred(ErythrocoralReefRegion.plateauBiomeVariantPaths(), "plateau biome variants");
		assertAllDeferred(ErythrocoralReefRegion.shatteredBiomePaths(), "shattered biomes");
		assertAllDeferred(ErythrocoralReefRegion.beachBiomePaths(), "beach biomes");
		assertAllDeferred(ErythrocoralReefRegion.peakBiomePaths(), "peak biomes");
		assertAllDeferred(ErythrocoralReefRegion.peakBiomeVariantPaths(), "peak biome variants");
		assertAllDeferred(ErythrocoralReefRegion.slopeBiomePaths(), "slope biomes");
		assertAllDeferred(ErythrocoralReefRegion.slopeBiomeVariantPaths(), "slope biome variants");
	}

	private static void assertAllDeferred(String[][] table, String tableName) {
		assertEquals(5, table.length, tableName + " should have five temperature rows");
		for (String[] row : table) {
			assertEquals(5, row.length, tableName + " should have five humidity columns");
			for (String biome : row) {
				assertEquals(ErythrocoralReefRegion.DEFERRED_PLACEHOLDER_PATH, biome,
						tableName + " should never place reef on land");
			}
		}
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertEquals(String expected, String actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
		}
	}
}
