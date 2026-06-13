package com.vincenthuto.hemomancy.common.effect;

public final class MnemonicCandleRulesTest {
	private MnemonicCandleRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(80, MnemonicCandleRules.AURA_INTERVAL_TICKS, "aura interval");
		assertEquals(120, MnemonicCandleRules.AURA_DURATION_TICKS, "aura duration");
		assertEquals(0.90D, MnemonicCandleRules.manipulationCooldownMultiplier(true), "cooldown aura");
		assertEquals(1.0D, MnemonicCandleRules.manipulationCooldownMultiplier(false), "no aura cooldown");
		assertEquals(1.5D, MnemonicCandleRules.bonusBloodRegenPerTick(true), "regen aura");
		assertEquals(0.0D, MnemonicCandleRules.bonusBloodRegenPerTick(false), "no aura regen");
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}
}
