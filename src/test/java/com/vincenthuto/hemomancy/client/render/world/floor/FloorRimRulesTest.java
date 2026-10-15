package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The membrane fits the floor the player laid; braziers beyond it are wired in by roots. */
final class FloorRimRulesTest {
	private static final int HALF = 3;

	@Test
	void theTrenchRunsInsideTheLaidFloor() {
		float[][] rim = FloorRimRules.rimLoop(HALF, 8 * GashLoop.GASH_STEPS);
		assertArrayEquals(rim[0], rim[rim.length - 1], 1.0e-6F, "the loop closes");
		float edge = HALF + 0.5F;
		float nearest = Float.MAX_VALUE;
		for (float[] point : rim) {
			float reach = Math.max(Math.abs(point[0] - 0.5F), Math.abs(point[1] - 0.5F));
			assertTrue(reach <= edge - FloorRimRules.EDGE_INSET + 1.0e-4F, "inside the floor's edge: " + reach);
			nearest = Math.min(nearest, reach);
		}
		assertTrue(nearest > HALF - 1.0F, "a frame round the floor, not a small ring at its middle");
		float[] corner = rim[GashLoop.GASH_STEPS];
		assertTrue(Math.hypot(corner[0] - 0.5F, corner[1] - 0.5F) < Math.sqrt(2) * (edge - FloorRimRules.EDGE_INSET) - 0.1D,
				"its corners are rounded");
	}

	@Test
	void eachRootLeavesTheRimTowardItsBrazier() {
		float[] start = FloorRimRules.rimPoint(HALF, 4.5F, 0.5F);
		assertEquals(0.5F + HALF + 0.5F - FloorRimRules.EDGE_INSET, start[0], 1.0e-4F, "on the rim, facing the brazier");
		assertEquals(0.5F, start[1], 1.0e-4F);
		float[] diagonal = FloorRimRules.rimPoint(HALF, 5.0F, 5.0F);
		assertTrue(diagonal[0] > 0.5F && diagonal[1] > 0.5F, "toward a corner brazier");
	}

	@Test
	void aRootArchesAcrossTheGapAndDivesIntoTheMound() {
		float[] start = {3.15F, 1.0F, 0.5F};
		float[] end = {5.25F, 1.1F, 0.5F};
		float[][] path = FloorRimRules.rootPath(start, end, 20);
		assertArrayEquals(start, path[0], 1.0e-5F);
		assertArrayEquals(end, path[path.length - 1], 1.0e-5F);
		float highest = Float.NEGATIVE_INFINITY;
		for (float[] point : path) highest = Math.max(highest, point[1]);
		assertTrue(highest > Math.max(start[1], end[1]) + 0.15F, "it arches up over the gap rather than sagging into it");
		for (int i = 1; i < path.length; i++) {
			assertTrue(path[i][0] >= path[i - 1][0] - 1.0e-4F, "it runs steadily out toward the brazier");
		}
	}
}
