package com.vincenthuto.hemomancy.common.manipulation;

public final class TendencyAffinityRulesTest {
	private TendencyAffinityRulesTest() {
	}

	public static void main(String[] args) {
		pureManipulationUsesFullPrimaryAffinity();
		mixedManipulationSplitsPrimaryAndSecondaryAffinity();
		secondaryTendencyGainIsHalfPrimaryGain();
	}

	private static void pureManipulationUsesFullPrimaryAffinity() {
		assertEquals("pure affinity keeps full primary multiplier", 1.75f,
				TendencyAffinityRules.composeDamageMultiplier(1.75f, null));
	}

	private static void mixedManipulationSplitsPrimaryAndSecondaryAffinity() {
		assertEquals("mixed affinity is 75 percent primary and 25 percent secondary", 1.375f,
				TendencyAffinityRules.composeDamageMultiplier(1.5f, 1.0f));
		assertEquals("secondary-only matchup gets only the quarter-weighted benefit", 1.25f,
				TendencyAffinityRules.composeDamageMultiplier(1.0f, 2.0f));
	}

	private static void secondaryTendencyGainIsHalfPrimaryGain() {
		assertEquals("secondary tendency gain is half the configured manipulation gain", 3.0f,
				TendencyAffinityRules.secondaryTendencyGain(6.0f));
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001f) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}
}
