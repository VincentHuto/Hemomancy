package com.vincenthuto.hemomancy.common.effect;

public final class MnemonicPotionRulesTest {
	private MnemonicPotionRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("whispers duration", 1200, MnemonicPotionRules.WHISPERS_DURATION_TICKS);
		assertEquals("screams duration", 1200, MnemonicPotionRules.SCREAMS_DURATION_TICKS);
		assertDouble("whispers cooldown multiplier", 0.75D,
				MnemonicPotionRules.manipulationCooldownMultiplier(true));
		assertDouble("no whispers cooldown multiplier", 1.0D,
				MnemonicPotionRules.manipulationCooldownMultiplier(false));
		assertDouble("screams cost multiplier", 1.5D,
				MnemonicPotionRules.manipulationCostMultiplier(true));
		assertDouble("no screams cost multiplier", 1.0D,
				MnemonicPotionRules.manipulationCostMultiplier(false));
		assertTrue("drinking whispers while whispers is active should punish",
				MnemonicPotionRules.shouldApplyScreamsOnWhispersDrink(true, true));
		assertFalse("first whispers drink should not punish",
				MnemonicPotionRules.shouldApplyScreamsOnWhispersDrink(true, false));
		assertFalse("other potions should not punish",
				MnemonicPotionRules.shouldApplyScreamsOnWhispersDrink(false, true));
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
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
