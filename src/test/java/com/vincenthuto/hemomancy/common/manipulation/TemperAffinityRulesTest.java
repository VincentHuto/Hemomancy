package com.vincenthuto.hemomancy.common.manipulation;

public final class TemperAffinityRulesTest {
	private TemperAffinityRulesTest() {
	}

	public static void main(String[] args) {
		pureManipulationUsesFullPrimaryAffinity();
		weakerSecondaryNeverDilutesThePrimary();
		strongerSecondaryAddsOnlyItsQuarterWeight();
		secondaryTemperGainIsHalfPrimaryGain();
	}

	private static void pureManipulationUsesFullPrimaryAffinity() {
		assertEquals("pure affinity keeps full primary multiplier", 1.75f,
				TemperAffinityRules.composeDamageMultiplier(1.75f, null));
	}

	private static void weakerSecondaryNeverDilutesThePrimary() {
		assertEquals("a neutral secondary must not dilute a strong primary", 2.0f,
				TemperAffinityRules.composeDamageMultiplier(2.0f, 1.0f));
		assertEquals("a neutral secondary must not dilute a 1.5 primary", 1.5f,
				TemperAffinityRules.composeDamageMultiplier(1.5f, 1.0f));
	}

	private static void strongerSecondaryAddsOnlyItsQuarterWeight() {
		assertEquals("secondary-only matchup gets only the quarter-weighted benefit", 1.25f,
				TemperAffinityRules.composeDamageMultiplier(1.0f, 2.0f));
	}

	private static void secondaryTemperGainIsHalfPrimaryGain() {
		assertEquals("secondary temper gain is half the configured manipulation gain", 3.0f,
				TemperAffinityRules.secondaryTemperGain(6.0f));
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001f) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}
}
