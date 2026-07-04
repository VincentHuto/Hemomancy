package com.vincenthuto.hemomancy.common.event;

public final class CirculationIncomeRulesTest {
	private CirculationIncomeRulesTest() {
	}

	public static void main(String[] args) {
		singleSourceUnderBandwidthPassesThroughUnchanged();
		stackedSourcesShareOneWindow();
		staleWindowResetsAndGrantsAgain();
		bandwidthScalesWithDegreeAndCapacity();
		degenerateInputsAreSafe();
	}

	private static void singleSourceUnderBandwidthPassesThroughUnchanged() {
		double bandwidth = CirculationIncomeRules.bandwidthPerWindow(
				CirculationIncomeRules.DEFAULT_BASE_BANDWIDTH,
				CirculationIncomeRules.DEFAULT_BANDWIDTH_PER_DEGREE, 1,
				CirculationIncomeRules.DEFAULT_BANDWIDTH_PER_CAPACITY_POINT, 0.0D);
		CirculationIncomeRules.Grant grant = CirculationIncomeRules.grant(2.0D, 100L, 20, 100L, 0.0D, bandwidth);
		assertDouble("hematic iron regen alone is never clipped", 2.0D, grant.granted());
		assertDouble("window usage records the grant", 2.0D, grant.usedInWindow());
	}

	private static void stackedSourcesShareOneWindow() {
		double bandwidth = 6.0D;
		CirculationIncomeRules.Grant first = CirculationIncomeRules.grant(4.0D, 100L, 20, 100L, 0.0D, bandwidth);
		CirculationIncomeRules.Grant second = CirculationIncomeRules.grant(4.0D, 105L, 20, first.windowStart(),
				first.usedInWindow(), bandwidth);
		CirculationIncomeRules.Grant third = CirculationIncomeRules.grant(4.0D, 110L, 20, second.windowStart(),
				second.usedInWindow(), bandwidth);
		assertDouble("first source granted fully", 4.0D, first.granted());
		assertDouble("second source clipped to remaining bandwidth", 2.0D, second.granted());
		assertDouble("third source finds the pipe empty", 0.0D, third.granted());
	}

	private static void staleWindowResetsAndGrantsAgain() {
		CirculationIncomeRules.Grant exhausted = CirculationIncomeRules.grant(6.0D, 100L, 20, 100L, 0.0D, 6.0D);
		assertDouble("window exhausted", 6.0D, exhausted.usedInWindow());
		CirculationIncomeRules.Grant nextWindow = CirculationIncomeRules.grant(3.0D, 125L, 20,
				exhausted.windowStart(), exhausted.usedInWindow(), 6.0D);
		assertDouble("new window grants again", 3.0D, nextWindow.granted());
		assertEquals("window start rolls forward", 125L, nextWindow.windowStart());
		CirculationIncomeRules.Grant clockRewound = CirculationIncomeRules.grant(3.0D, 50L, 20,
				exhausted.windowStart(), exhausted.usedInWindow(), 6.0D);
		assertDouble("a rewound clock resets instead of blocking forever", 3.0D, clockRewound.granted());
	}

	private static void bandwidthScalesWithDegreeAndCapacity() {
		double base = CirculationIncomeRules.bandwidthPerWindow(6.0D, 1.5D, 0, 0.002D, 0.0D);
		double archon = CirculationIncomeRules.bandwidthPerWindow(6.0D, 1.5D, 7, 0.002D, 2500.0D);
		assertDouble("degree zero baseline", 6.0D, base);
		assertDouble("archon with capacity five", 21.5D, archon);
		assertTrue("bandwidth is monotonic in degree", archon > base);
	}

	private static void degenerateInputsAreSafe() {
		CirculationIncomeRules.Grant negative = CirculationIncomeRules.grant(-5.0D, 100L, 20, 100L, 0.0D, 6.0D);
		assertDouble("negative requests grant nothing", 0.0D, negative.granted());
		CirculationIncomeRules.Grant zeroWindow = CirculationIncomeRules.grant(2.0D, 100L, 0, 100L, 0.0D, 6.0D);
		assertDouble("zero window ticks falls back to default instead of dividing the world", 2.0D,
				zeroWindow.granted());
		double negativeBandwidth = CirculationIncomeRules.bandwidthPerWindow(-10.0D, 0.0D, 0, 0.0D, 0.0D);
		assertDouble("bandwidth never negative", 0.0D, negativeBandwidth);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label + ": expected true");
		}
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
