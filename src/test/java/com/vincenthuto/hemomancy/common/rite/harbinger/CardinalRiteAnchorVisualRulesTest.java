package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

public final class CardinalRiteAnchorVisualRulesTest {
	@Test
	void boundaryAnchorBeginsGrowingAsSoonAsItReceivesBlood() {
		assertEquals(CardinalRiteAnchorVisualRules.Visual.INTERACTION_MARKER,
				CardinalRiteAnchorVisualRules.boundaryVisual(0, 50), "empty boundary anchor");
		assertEquals(CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB,
				CardinalRiteAnchorVisualRules.boundaryVisual(1, 50), "forming boundary anchor");
		assertEquals(CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB,
				CardinalRiteAnchorVisualRules.boundaryVisual(50, 50), "filled boundary anchor");
		assertFloatEquals(0.0F,
				CardinalRiteAnchorVisualRules.formingBoundaryRadius(0, 50), "empty boundary radius");
		assertFloatEquals(0.11F,
				CardinalRiteAnchorVisualRules.formingBoundaryRadius(25, 50), "half-full boundary radius");
		assertFloatEquals(0.19F,
				CardinalRiteAnchorVisualRules.formingBoundaryRadius(50, 50), "full boundary radius");
	}

	@Test
	void eachCompletedSigilNodeBecomesABlobWhileFutureNodesRemainMarkers() {
		assertEquals(CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB,
				CardinalRiteAnchorVisualRules.sigilVisual(1, 2), "completed sigil node");
		assertEquals(CardinalRiteAnchorVisualRules.Visual.INTERACTION_MARKER,
				CardinalRiteAnchorVisualRules.sigilVisual(2, 2), "next sigil node");
	}

	@Test
	void authoredSigilColorIsPreservedAndBoundaryUsesBloodRed() {
		assertEquals(0xFF3746, CardinalRiteAnchorVisualRules.BOUNDARY_COLOR, "boundary color");
		assertEquals(0x36D9FF, CardinalRiteAnchorVisualRules.sigilColor(0xAA36D9FF), "sigil color");
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertFloatEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
