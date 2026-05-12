package com.vincenthuto.hemomancy.common.worldgen.terrablender;

final class ErythrocoralReefClimateRulesTest {
	public static void main(String[] args) {
		keepsReefsInDeepOceanClimate();
		rejectsShallowCoastAndInlandClimate();
		usesNarrowWarmHumidDeepWaterNiche();
		rejectsBroadRidgeMakingClimateSlices();
	}

	private static void keepsReefsInDeepOceanClimate() {
		assertTrue(ErythrocoralReefClimateRules.allowsContinentalnessName("DEEP_OCEAN"),
				"erythrocoral reefs should target deep ocean climate");
	}

	private static void rejectsShallowCoastAndInlandClimate() {
		assertFalse(ErythrocoralReefClimateRules.allowsContinentalnessName("OCEAN"),
				"ordinary ocean continentalness can generate broad shallow land shelves");
		assertFalse(ErythrocoralReefClimateRules.allowsContinentalnessName("COAST"),
				"coast climate can paint reef water onto grassland edges");
		assertFalse(ErythrocoralReefClimateRules.allowsContinentalnessName("NEAR_INLAND"),
				"near-inland climate should never become erythrocoral reef");
	}

	private static void usesNarrowWarmHumidDeepWaterNiche() {
		assertTrue(ErythrocoralReefClimateRules.allowsTemperatureName("WARM"),
				"reefs should use lukewarm/warm deep-ocean climate");
		assertTrue(ErythrocoralReefClimateRules.allowsHumidityName("HUMID"),
				"reefs should use one humid climate bucket to stay rare");
		assertTrue(ErythrocoralReefClimateRules.allowsErosionName("EROSION_6"),
				"reefs should prefer the smoothest deep-ocean erosion bucket");
		assertTrue(ErythrocoralReefClimateRules.allowsWeirdnessName("VALLEY"),
				"reefs should prefer valley weirdness to avoid raised ocean ridges");
	}

	private static void rejectsBroadRidgeMakingClimateSlices() {
		assertFalse(ErythrocoralReefClimateRules.allowsTemperatureName("HOT"),
				"hot ocean climate makes reefs too common and overlaps warm-ocean edges");
		assertFalse(ErythrocoralReefClimateRules.allowsHumidityName("WET"),
				"wet plus humid doubles reef coverage");
		assertFalse(ErythrocoralReefClimateRules.allowsErosionName("EROSION_4"),
				"lower erosion buckets can produce rough raised shelves");
		assertFalse(ErythrocoralReefClimateRules.allowsErosionName("EROSION_5"),
				"multiple erosion buckets make reef patches too large");
		assertFalse(ErythrocoralReefClimateRules.allowsWeirdnessName("MID_SLICE_NORMAL_ASCENDING"),
				"mid ascending weirdness can make ocean ridges breach the surface");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		if (condition) {
			throw new AssertionError(message);
		}
	}
}
