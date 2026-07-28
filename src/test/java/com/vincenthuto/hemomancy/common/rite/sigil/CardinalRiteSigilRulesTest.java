package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class CardinalRiteSigilRulesTest {
	@Test
	void flatRiteNodesSitAboveTheGroundBlock() {
		int surfaceY = CardinalRiteSigilRules.surfaceAirY(10, y -> y == 10);

		assertEquals(11, surfaceY, "flat surface");
	}

	@Test
	void raisedStructureNodesSitAboveTheHighestNearbyBlock() {
		int surfaceY = CardinalRiteSigilRules.surfaceAirY(10,
				y -> y == 9 || y == 10 || y == 11 || y == 12);

		assertEquals(13, surfaceY, "raised surface");
	}

	@Test
	void emptyColumnsUseTheExpectedGroundPlane() {
		int surfaceY = CardinalRiteSigilRules.surfaceAirY(10, y -> false);

		assertEquals(11, surfaceY, "empty-column fallback");
	}

	@Test
	void overlappingHitRadiiSelectTheClosestSigilNodeInsteadOfTheFirstNode() {
		List<BlockPos> nodeAirPositions = List.of(
				new BlockPos(-5, 65, -1),
				new BlockPos(-4, 65, -1),
				new BlockPos(-3, 65, -1));

		int touched = CardinalRiteSigilRules.closestNodeIndex(
				nodeAirPositions, new BlockPos(-3, 64, -1), 1.4D);

		assertEquals(2, touched, "third Bastion stroke");
	}

	@Test
	void projectionMayPassOverCompletedNodesWithoutCreatingAFalseStroke() {
		assertEquals(CardinalRiteSigilRules.StrokeDisposition.COMPLETED,
				CardinalRiteSigilRules.strokeDisposition(1, 2), "completed node");
		assertEquals(CardinalRiteSigilRules.StrokeDisposition.EXPECTED,
				CardinalRiteSigilRules.strokeDisposition(2, 2), "expected node");
		assertEquals(CardinalRiteSigilRules.StrokeDisposition.FALSE,
				CardinalRiteSigilRules.strokeDisposition(3, 2), "skipped future node");
	}

	@Test
	void completedNodesAndFullyFormedSigilsAreNotActionableAgain() {
		assertFalse(CardinalRiteSigilRules.isActionableNode(0, 1, 4), "completed node");
		assertFalse(CardinalRiteSigilRules.isActionableNode(0, 4, 4), "completed sigil");
		assertTrue(CardinalRiteSigilRules.isActionableNode(1, 1, 4), "current node");
		assertEquals(50, CardinalRiteSigilRules.nodeCompletionStorageMl(200, 4),
				"reservoir fills while it is formed");
	}

	@Test
	void falseStrokePenaltyIsLightAndBloodCostIsCapped() {
		assertEquals(1, CardinalRiteSigilRules.FALSE_STROKE_INSTABILITY,
				"false-stroke instability");
		assertEquals(5, CardinalRiteSigilRules.falseStrokeBloodRequest(50.0D),
				"high-rate projection");
		assertEquals(3, CardinalRiteSigilRules.falseStrokeBloodRequest(3.9D),
				"low-rate projection");
	}

	@Test
	void holdingProjectionOnOneFalseNodeDoesNotPunishEveryTick() {
		assertTrue(CardinalRiteSigilRules.falseStrokePenaltyReady(100, 80),
				"cooldown elapsed");
		assertFalse(CardinalRiteSigilRules.falseStrokePenaltyReady(101, 100),
				"next projection tick");
		assertTrue(CardinalRiteSigilRules.falseStrokePenaltyReady(120, 100),
				"one second later");
		assertTrue(CardinalRiteSigilRules.falseStrokePenaltyReady(2, 100),
				"phase timer reset");
	}

	@Test
	void correctNodeFeedbackGrowsWithItsStoredBlood() {
		float empty = CardinalRiteSigilRules.formingNodeRadius(0);
		float beginning = CardinalRiteSigilRules.formingNodeRadius(1);
		float halfway = CardinalRiteSigilRules.formingNodeRadius(25);
		float complete = CardinalRiteSigilRules.formingNodeRadius(50);

		assertFloatEquals(0.0F, empty, "empty node");
		assertTrue(beginning > empty, "first offered blood becomes visible");
		assertTrue(halfway > beginning, "half-filled node is larger");
		assertTrue(complete > halfway, "completed node is largest");
		assertFloatEquals(0.0925F, halfway, "half-filled node follows a smooth organic growth curve");
		assertFloatEquals(0.16F, complete, "forming feedback meets the completed node radius");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertFloatEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean actual, String label) {
		if (!actual) throw new AssertionError(label + " should be actionable");
	}

	private static void assertFalse(boolean actual, String label) {
		if (actual) throw new AssertionError(label + " should be inert");
	}
}
