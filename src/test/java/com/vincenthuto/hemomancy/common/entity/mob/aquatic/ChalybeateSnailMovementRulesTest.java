package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class ChalybeateSnailMovementRulesTest {
	private ChalybeateSnailMovementRulesTest() {
	}

	public static void main(String[] args) {
		assertTrue("extended underwater snails can climb vent blocks",
				ChalybeateSnailMovementRules.canUseVentClimb(true, false, true));
		assertFalse("retracted snails cannot climb vent blocks",
				ChalybeateSnailMovementRules.canUseVentClimb(true, true, true));
		assertFalse("snails do not climb when away from vent blocks",
				ChalybeateSnailMovementRules.canUseVentClimb(true, false, false));
		assertFalse("snails do not climb out of water",
				ChalybeateSnailMovementRules.canUseVentClimb(false, false, true));
		assertDouble("climb nudge raises weak downward motion",
				ChalybeateSnailMovementRules.CLIMB_UPWARD_SPEED,
				ChalybeateSnailMovementRules.climbAdjustedY(-0.04D, true));
		assertDouble("climb nudge preserves stronger upward motion",
				0.12D,
				ChalybeateSnailMovementRules.climbAdjustedY(0.12D, true));
		assertDouble("non-climb motion is unchanged",
				-0.04D,
				ChalybeateSnailMovementRules.climbAdjustedY(-0.04D, false));
		assertTrue("crawl starts more often than the first pass",
				ChalybeateSnailMovementRules.CRAWL_START_CHANCE < 70);
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001D) {
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
