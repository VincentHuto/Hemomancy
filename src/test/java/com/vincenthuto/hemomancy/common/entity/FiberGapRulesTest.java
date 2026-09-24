package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FiberGapRules;
import org.junit.jupiter.api.Test;

class FiberGapRulesTest {
	/** order: west, east, down, up, north, south */
	private static boolean[] neighbours(boolean west, boolean east, boolean down, boolean up,
			boolean north, boolean south) {
		return new boolean[] { west, east, down, up, north, south };
	}

	@Test
	void airFlankedOnTheXAxisIsAGap() {
		boolean[] n = neighbours(true, true, false, false, false, false);
		assertTrue(FiberGapRules.isGap(n), "cable continues on both sides, so this hole is a severed run");
		assertEquals(0, FiberGapRules.gapAxis(n), "the replacement fiber must run along X");
	}

	@Test
	void airFlankedOnTheZAxisIsAGap() {
		boolean[] n = neighbours(false, false, false, false, true, true);
		assertTrue(FiberGapRules.isGap(n));
		assertEquals(2, FiberGapRules.gapAxis(n));
	}

	@Test
	void aDeadEndIsNotAGap() {
		boolean[] n = neighbours(true, false, false, false, false, false);
		assertFalse(FiberGapRules.isGap(n),
				"cable on one side only means open void beyond; mending here would extrude fiber into nothing");
		assertEquals(-1, FiberGapRules.gapAxis(n));
	}

	@Test
	void emptySpaceIsNotAGap() {
		assertFalse(FiberGapRules.isGap(neighbours(false, false, false, false, false, false)));
	}

	@Test
	void aCornerIsNotAGap() {
		assertFalse(FiberGapRules.isGap(neighbours(true, false, false, false, true, false)),
				"two different axes is a corner, not a severed straight run");
	}
}
