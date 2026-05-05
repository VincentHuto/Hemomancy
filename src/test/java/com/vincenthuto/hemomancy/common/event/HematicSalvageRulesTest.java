package com.vincenthuto.hemomancy.common.event;

public final class HematicSalvageRulesTest {
	private HematicSalvageRulesTest() {
	}

	public static void main(String[] args) {
		assertDouble("no damaged armor uses base chance", 0.12,
				HematicSalvageRules.calculateChance(0, 0.12, 0.04, 0.25));
		assertDouble("damaged armor adds per-piece chance", 0.20,
				HematicSalvageRules.calculateChance(2, 0.12, 0.04, 0.25));
		assertDouble("chance is capped", 0.25,
				HematicSalvageRules.calculateChance(4, 0.12, 0.04, 0.25));

		assertTrue("minimum damage qualifies",
				HematicSalvageRules.isEligible(4.0f, 4.0, 2, 2, 100L, -1L, 1, 1200));
		assertFalse("low damage does not qualify",
				HematicSalvageRules.isEligible(3.5f, 4.0, 2, 2, 100L, -1L, 1, 1200));
		assertFalse("high degree does not qualify",
				HematicSalvageRules.isEligible(4.0f, 4.0, 3, 2, 100L, -1L, 1, 1200));
		assertFalse("cooldown blocks repeat drops",
				HematicSalvageRules.isEligible(4.0f, 4.0, 2, 2, 1000L, 0L, 1, 1200));
		assertTrue("cooldown allows later drops",
				HematicSalvageRules.isEligible(4.0f, 4.0, 2, 2, 1300L, 0L, 1, 1200));
		assertFalse("damaged armor is required",
				HematicSalvageRules.isEligible(4.0f, 4.0, 2, 2, 1300L, 0L, 0, 1200));
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
