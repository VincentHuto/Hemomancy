package com.vincenthuto.hemomancy.common.event;

public final class SanguineFormationProjectionRulesTest {
	private SanguineFormationProjectionRulesTest() {
	}

	public static void main(String[] args) {
		genericProjectionUsesHigherBloodCost();
		attunedProjectionUsesLowerBloodCostAndFasterFeed();
		genericCollapseChanceFallsWithSkillLevels();
		attunedProjectionNeverCollapses();
		feedAmountCapsToRemainingCostAndAvailableBlood();
	}

	private static void genericProjectionUsesHigherBloodCost() {
		assertEquals("generic projection blood cost", 150.0,
				SanguineFormationProjectionRules.bloodCost(false));
	}

	private static void attunedProjectionUsesLowerBloodCostAndFasterFeed() {
		assertEquals("attuned projection blood cost", 100.0,
				SanguineFormationProjectionRules.bloodCost(true));
		assertEquals("attuned projection feed multiplier", 2.0,
				SanguineFormationProjectionRules.feedMultiplier(true));
		assertEquals("generic projection feed multiplier", 1.0,
				SanguineFormationProjectionRules.feedMultiplier(false));
	}

	private static void genericCollapseChanceFallsWithSkillLevels() {
		assertEquals("generic projection base collapse chance", 0.25,
				SanguineFormationProjectionRules.collapseChance(false, 0));
		assertEquals("generic projection level three collapse chance", 0.10,
				SanguineFormationProjectionRules.collapseChance(false, 3));
		assertEquals("generic projection level five removes collapse chance", 0.0,
				SanguineFormationProjectionRules.collapseChance(false, 5));
		assertEquals("generic projection levels above cap stay safe", 0.0,
				SanguineFormationProjectionRules.collapseChance(false, 9));
	}

	private static void attunedProjectionNeverCollapses() {
		assertEquals("attuned projection ignores collapse chance", 0.0,
				SanguineFormationProjectionRules.collapseChance(true, 0));
		assertEquals("attuned projection remains safe with skill levels", 0.0,
				SanguineFormationProjectionRules.collapseChance(true, 5));
	}

	private static void feedAmountCapsToRemainingCostAndAvailableBlood() {
		assertEquals("fresh generic projection drains at projection rate", 5.0,
				SanguineFormationProjectionRules.feedAmount(0.0, false, 1000.0, 5.0));
		assertEquals("attuned projection doubles the projection rate", 10.0,
				SanguineFormationProjectionRules.feedAmount(0.0, true, 1000.0, 5.0));
		assertEquals("projection only drains remaining cost", 3.0,
				SanguineFormationProjectionRules.feedAmount(147.0, false, 1000.0, 5.0));
		assertEquals("projection only drains available blood", 2.0,
				SanguineFormationProjectionRules.feedAmount(0.0, false, 2.0, 5.0));
		assertEquals("complete projection stops draining", 0.0,
				SanguineFormationProjectionRules.feedAmount(150.0, false, 1000.0, 5.0));
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
