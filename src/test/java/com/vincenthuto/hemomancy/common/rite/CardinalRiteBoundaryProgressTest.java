package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

public final class CardinalRiteBoundaryProgressTest {
	private static final List<CardinalRiteCeremonyDefinition.Anchor> CARDINAL_RING = List.of(
			new CardinalRiteCeremonyDefinition.Anchor(0, 1, -2, 0, 0),
			new CardinalRiteCeremonyDefinition.Anchor(2, 1, 0, 0, 1),
			new CardinalRiteCeremonyDefinition.Anchor(0, 1, 2, 0, 2),
			new CardinalRiteCeremonyDefinition.Anchor(-2, 1, 0, 0, 3));

	@Test
	void adjacentCompletedAnchorsCreateOneQuarterArc() {
		var segments = CardinalRiteBoundaryProgress.completedSegments(
				CARDINAL_RING, new int[] {50, 50, 0, 0});

		assertEquals(1, segments.size(), "adjacent segment count");
		assertEquals(0, segments.get(0).ring(), "ring");
		assertEquals(0, segments.get(0).startAnchorIndex(), "damage owner");
		assertNear(-Math.PI / 2.0D, segments.get(0).startAngle(), "start angle");
		assertNear(Math.PI / 2.0D, segments.get(0).sweepAngle(), "quarter sweep");
	}

	@Test
	void separatedCompletedAnchorsDoNotCreateAnArc() {
		var segments = CardinalRiteBoundaryProgress.completedSegments(
				CARDINAL_RING, new int[] {50, 0, 50, 0});

		assertEquals(0, segments.size(), "separated segment count");
	}

	@Test
	void allFourAnchorsCloseFourQuarterArcs() {
		var segments = CardinalRiteBoundaryProgress.completedSegments(
				CARDINAL_RING, new int[] {50, 50, 50, 50});

		assertEquals(4, segments.size(), "closed ring segment count");
		for (var segment : segments) {
			assertNear(Math.PI / 2.0D, segment.sweepAngle(), "closed quarter sweep");
		}
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
