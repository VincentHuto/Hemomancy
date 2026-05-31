package com.vincenthuto.hemomancy.common.item.harbinger;

public final class QliphothPomeRulesTest {
	private QliphothPomeRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("base empowerment duration", 3600L, QliphothPomeRules.empowermentDurationTicks(0));
		assertEquals("gestation extends empowerment duration", 4680L, QliphothPomeRules.empowermentDurationTicks(3));
		assertDouble("gestation deepens empowerment discount", 0.66, QliphothPomeRules.empowermentCostMultiplier(3));
	}

	private static void assertEquals(String label, long expected, long actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
