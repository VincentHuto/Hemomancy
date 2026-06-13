package com.vincenthuto.hemomancy.common.item.shared;

public final class TendonLineRulesTest {
	private TendonLineRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(18.0D, TendonLineRules.ANCHOR_RANGE, "anchor range");
		assertEquals(24.0D, TendonLineRules.DETACH_RANGE, "detach range");
		assertEquals(1.35D, TendonLineRules.CLOSE_DISTANCE, "close distance");
		assertEquals(0.34D, TendonLineRules.PULL_SPEED, "pull speed");
		assertEquals(10, TendonLineRules.USE_COOLDOWN_TICKS, "cooldown");
		assertTrue(TendonLineRules.canAnchor(17.99D), "inside anchor range");
		assertFalse(TendonLineRules.canAnchor(18.01D), "outside anchor range");
		assertTrue(TendonLineRules.shouldDetach(24.01D), "past detach range");
		assertTrue(TendonLineRules.hasArrived(1.34D), "close enough");

		TendonLineRules.Motion motion = TendonLineRules.pullMotion(0.0D, 0.0D, 0.0D, 0.0D, 10.0D, 0.0D);
		assertEquals(0.0D, motion.x(), "vertical pull x");
		assertEquals(TendonLineRules.PULL_SPEED, motion.y(), "vertical pull y");
		assertEquals(0.0D, motion.z(), "vertical pull z");
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

	private static void assertTrue(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(boolean value, String label) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
