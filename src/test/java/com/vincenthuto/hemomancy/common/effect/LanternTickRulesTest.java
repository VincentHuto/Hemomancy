package com.vincenthuto.hemomancy.common.effect;

public final class LanternTickRulesTest {
	private LanternTickRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(10, LanternTickRules.HELMET_LIGHT_LEVEL, "helmet light level");
		assertEquals(160, LanternTickRules.LATCH_DRAIN_INTERVAL_TICKS, "latch drain interval");
		assertEquals(15.0D, LanternTickRules.LURE_PLAYER_DISTANCE, "lure player distance");
		assertEquals(3.0D, LanternTickRules.LATCH_DISTANCE, "latch distance");
		assertEquals(1.4D, LanternTickRules.LATCH_LEAP_STRENGTH, "latch leap strength");
		assertEquals(8.0D, LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE, "blood drain per bite");
		assertEquals(1.0F, LanternTickRules.FALLBACK_DAMAGE_PER_BITE, "fallback bite damage");
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
