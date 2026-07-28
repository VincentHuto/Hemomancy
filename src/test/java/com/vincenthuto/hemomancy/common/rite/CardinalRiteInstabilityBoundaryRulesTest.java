package com.vincenthuto.hemomancy.common.rite;

import java.util.List;

import org.junit.jupiter.api.Test;

public final class CardinalRiteInstabilityBoundaryRulesTest {
	@Test
	void eachAnchorOwnsAnEqualInstabilityBandForItsTier() {
		assertEquals(0, CardinalRiteInstabilityBoundaryRules.brokenAnchorCount(24, 4), "four-anchor warning");
		assertEquals(1, CardinalRiteInstabilityBoundaryRules.brokenAnchorCount(25, 4), "25 percent");
		assertEquals(1, CardinalRiteInstabilityBoundaryRules.brokenAnchorCount(13, 8), "12.5 percent");
		assertEquals(1, CardinalRiteInstabilityBoundaryRules.brokenAnchorCount(7, 16), "6.25 percent");
	}

	@Test
	void theNextBoundarySectionFlickersTowardItsBreakThreshold() {
		assertNear(0.50D,
				CardinalRiteInstabilityBoundaryRules.flickerProgress(12.5D, 4), "half of first band");
		assertNear(0.96D,
				CardinalRiteInstabilityBoundaryRules.flickerProgress(12.0D, 8), "near eight-anchor break");
		assertNear(0.0D,
				CardinalRiteInstabilityBoundaryRules.flickerProgress(25.0D, 4), "exact break threshold");
	}

	@Test
	void oneFullRepairOfferingRemovesOneTierScaledDamageBand() {
		assertEquals(25, CardinalRiteInstabilityBoundaryRules.repairInstabilityAmount(4), "four anchors");
		assertEquals(13, CardinalRiteInstabilityBoundaryRules.repairInstabilityAmount(8), "eight anchors");
		assertEquals(7, CardinalRiteInstabilityBoundaryRules.repairInstabilityAmount(16), "sixteen anchors");
	}

	@Test
	void damageConsumesTheOutermostRingBeforeMovingInward() {
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = List.of(
				anchor(0, 0), anchor(0, 1), anchor(0, 2), anchor(0, 3),
				anchor(1, 0), anchor(1, 1), anchor(1, 2), anchor(1, 3),
				anchor(2, 0), anchor(2, 1), anchor(2, 2), anchor(2, 3));

		List<Integer> expected = List.of(8, 9, 10, 11, 4, 5, 6, 7, 0, 1, 2, 3);
		List<Integer> actual = CardinalRiteInstabilityBoundaryRules.damagePriority(anchors);
		if (!expected.equals(actual)) {
			throw new AssertionError("outer-to-inner priority: expected " + expected + " but got " + actual);
		}
	}

	private static CardinalRiteCeremonyDefinition.Anchor anchor(int ring, int order) {
		return new CardinalRiteCeremonyDefinition.Anchor(0, 1, 0, ring, order);
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertNear(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
