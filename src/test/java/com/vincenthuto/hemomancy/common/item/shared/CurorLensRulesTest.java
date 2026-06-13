package com.vincenthuto.hemomancy.common.item.shared;

public final class CurorLensRulesTest {
	private CurorLensRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(16.0D, CurorLensRules.SCAN_RANGE, "scan range");
		assertEquals(72000, CurorLensRules.USE_DURATION_TICKS, "use duration");
		assertEquals(2, CurorLensRules.MAX_SCAR_NAMES, "max scar names");
		assertTrue(CurorLensRules.SCREEN_TINT_ALPHA <= 24, "screen tint remains see-through");
		assertEquals("FERRIC", CurorLensRules.dominantTendencyName(new float[] {
				0.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F }), "dominant tendency");
		assertEquals("unresolved", CurorLensRules.dominantTendencyName(new float[] { 0.0F, 0.0F, 0.0F }), "empty tendency");
		assertEquals("12/20", CurorLensRules.healthLine(12.2F, 20.0F), "health line");
		assertEquals("450/1000 mL", CurorLensRules.bloodLine(450.0D, 1000.0D), "blood line");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(String expected, String actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertTrue(boolean condition, String label) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}
}
