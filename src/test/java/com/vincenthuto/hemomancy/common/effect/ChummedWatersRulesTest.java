package com.vincenthuto.hemomancy.common.effect;

public final class ChummedWatersRulesTest {
	private ChummedWatersRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("base chum duration", 2400, ChummedWatersRules.DURATION_TICKS);
		assertEquals("base lure acceleration", 3, ChummedWatersRules.extraLureTicksPerTick(0));
		assertEquals("amplified lure acceleration", 5, ChummedWatersRules.extraLureTicksPerTick(1));
		assertEquals("lure acceleration clamps negative amplifiers", 3, ChummedWatersRules.extraLureTicksPerTick(-4));
		assertEquals("lure countdown never drops below fish approach threshold", 20,
				ChummedWatersRules.adjustedLureTime(21, 0));
		assertEquals("lure countdown accelerates while preserving vanilla approach behavior", 57,
				ChummedWatersRules.adjustedLureTime(60, 0));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
