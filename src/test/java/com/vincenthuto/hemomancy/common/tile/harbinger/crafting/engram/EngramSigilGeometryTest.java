package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;
import static org.junit.jupiter.api.Assertions.*;

class EngramSigilGeometryTest {
	@Test
	void theStarHasTwentyFiveNodesAndFiftySixEdges() {
		assertEquals(25, NODES);
		assertEquals(56, edges().length);
		for (int[] edge : edges()) assertEquals(edgeIndex(edge[0], edge[1]), edgeIndex(edge[1], edge[0]));
	}

	@Test
	void eachRingHasItsDegree() {
		for (int n = 0; n < 8; n++) {
			assertTrue(isTip(n));
			assertEquals(4, neighbours(n).length, "tip " + n);
			assertTrue(isNotch(n + 8));
			assertEquals(4, neighbours(n + 8).length, "notch " + n);
			assertTrue(isInner(n + 16));
			assertEquals(5, neighbours(n + 16).length, "inner " + n);
		}
		assertEquals(8, neighbours(VESSEL).length);
	}

	@Test
	void ringsSitAtTheOctagramsCrossingRadii() {
		for (int k = 0; k < 8; k++) {
			assertEquals(1.0, radius(k), 1e-6);
			assertEquals(0.5412, radius(8 + k), 1e-3);
			assertEquals(0.4142, radius(16 + k), 1e-3);
			// The inner octagon lies on the tips' bearings, the notches between them.
			assertEquals(angle(k), angle(16 + k), 1e-6);
			assertEquals(angle(k) + Math.PI / 8, angle(8 + k), 1e-6);
		}
		assertArrayEquals(new double[]{0, 0}, unit(VESSEL), 1e-9);
	}

	@Test
	void tipZeroPointsOutOfTheLoomsFrontAndTheTipsRunClockwise() {
		assertArrayEquals(new double[]{0, -1}, unit(0), 1e-9);
		assertArrayEquals(new double[]{1, 0}, unit(2), 1e-9);
		assertArrayEquals(new double[]{0, 1}, unit(4), 1e-9);
	}

	@Test
	void everyStarLineCarriesSixNodesAndTheSpokesReachTheVessel() {
		assertEquals(8, starLines().length);
		for (int[] line : starLines()) {
			assertEquals(6, line.length);
			assertTrue(isTip(line[0]) && isTip(line[5]));
			for (int i = 0; i < 5; i++) assertTrue(adjacent(line[i], line[i + 1]));
		}
		for (int face = 0; face < 8; face++) {
			assertEquals(face, face(spokeInner(face)));
			assertTrue(adjacent(spokeInner(face), VESSEL));
		}
		assertTrue(adjacent(0, 1) && adjacent(7, 0), "the outer ring joins neighbouring tips");
		assertFalse(adjacent(0, VESSEL));
	}

	@Test
	void collinearRunsFillOnlyAlongOneStarLine() {
		int[] line = starLines()[0];
		assertArrayEquals(new int[]{line[1], line[2]}, collinearRun(line[0], line[3]));
		assertArrayEquals(new int[0], collinearRun(line[0], line[1]));
		assertArrayEquals(new int[]{line[3], line[2], line[1]}, collinearRun(line[4], line[0]));
		assertNull(collinearRun(0, 4), "tips 0 and 4 share no line");
		assertNull(collinearRun(0, VESSEL));
	}

	@Test
	void drawnEdgesNeverCrossExceptAtTheirNodes() {
		int[][] edges = edges();
		for (int i = 0; i < edges.length; i++)
			for (int j = i + 1; j < edges.length; j++) {
				int[] a = edges[i], b = edges[j];
				if (a[0] == b[0] || a[0] == b[1] || a[1] == b[0] || a[1] == b[1]) continue;
				if (a[0] < 8 && a[1] < 8 || b[0] < 8 && b[1] < 8) continue;   // ring arcs are curves outside the star
				assertFalse(cross(unit(a[0]), unit(a[1]), unit(b[0]), unit(b[1])), "edges " + i + " and " + j + " cross");
			}
	}

	private static double radius(int n) {
		double[] p = unit(n);
		return Math.hypot(p[0], p[1]);
	}

	/** Bearing clockwise from the front. */
	private static double angle(int n) {
		double[] p = unit(n);
		double a = Math.atan2(p[0], -p[1]);
		return a < -1e-9 ? a + Math.PI * 2 : a;
	}

	private static boolean cross(double[] p1, double[] p2, double[] p3, double[] p4) {
		double d1 = side(p3, p4, p1), d2 = side(p3, p4, p2), d3 = side(p1, p2, p3), d4 = side(p1, p2, p4);
		return (d1 > 1e-9 && d2 < -1e-9 || d1 < -1e-9 && d2 > 1e-9) && (d3 > 1e-9 && d4 < -1e-9 || d3 < -1e-9 && d4 > 1e-9);
	}

	private static double side(double[] a, double[] b, double[] p) {
		return (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
	}
}
