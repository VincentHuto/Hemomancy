package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngramAimTest {
	private static final double RADIUS = 5.0, SNAP = 0.6;

	@Test
	void aimingAtANodeTakesIt() {
		for (int node = 0; node < EngramSigilGeometry.VESSEL; node++) {
			double[] at = EngramSigilGeometry.unit(node);
			assertEquals(node, EngramAim.nearestNode(at[0] * RADIUS + 0.2, at[1] * RADIUS - 0.2, RADIUS, SNAP), "node " + node);
		}
		assertEquals(EngramSigilGeometry.VESSEL, EngramAim.nearestNode(0.3, -0.4, RADIUS, SNAP), "the loom itself is the Vessel");
	}

	@Test
	void aimingBetweenNodesTakesNothing() {
		double[] a = EngramSigilGeometry.unit(0), b = EngramSigilGeometry.unit(1);
		// The outer ring between two tips is far from every node.
		double x = (a[0] + b[0]) / 2 * RADIUS, z = (a[1] + b[1]) / 2 * RADIUS;
		assertEquals(-1, EngramAim.nearestNode(x, z, RADIUS, SNAP));
		assertEquals(-1, EngramAim.nearestNode(40, 40, RADIUS, SNAP));
	}

	@Test
	void theEyeRayMeetsTheSigilsPlaneOnlyAheadAndWithinReach() {
		double[] hit = EngramAim.planeHit(0, 2, 0, 0, -1, 1, 0, 6);
		assertNotNull(hit);
		assertEquals(0, hit[0], 1e-9);
		assertEquals(2, hit[1], 1e-9);
		assertNull(EngramAim.planeHit(0, 2, 0, 0, 1, 1, 0, 6), "looking up never meets the floor");
		assertNull(EngramAim.planeHit(0, 2, 0, 0, -0.1, 1, 0, 6), "too far along the ray");
	}

	@Test
	void sigilAndWorldOffsetsRoundTripForEveryFacing() {
		for (int turns = 0; turns < 4; turns++) {
			double[] world = EngramAim.toWorld(1.5, -2.0, turns);
			double[] back = EngramAim.toSigil(world[0], world[1], turns);
			assertArrayEquals(new double[]{1.5, -2.0}, back, 1e-9);
		}
		assertArrayEquals(new double[]{1, 0}, EngramAim.toWorld(0, -1, 1), 1e-9, "an east-facing loom's front is east");
		assertArrayEquals(new double[]{0, 1}, EngramAim.toWorld(0, -1, 2), 1e-9);
		assertArrayEquals(new double[]{-1, 0}, EngramAim.toWorld(0, -1, 3), 1e-9);
	}

	@Test
	void theStaffsPointSettlesOnTheCentreOfTheFacePixelItTouches() {
		// A top face: the sixteenth the point fell in, a thirty-second proud of the face.
		assertArrayEquals(new double[]{10.28125, 64.03125, -3.78125}, EngramAim.onPixel(10.27, 64.0, -3.81, 0, 1, 0), 1e-9);
		// A side face keeps its own plane and snaps across it.
		assertArrayEquals(new double[]{5.03125, 70.96875, 2.53125}, EngramAim.onPixel(5.0, 70.99, 2.5, 1, 0, 0), 1e-9);
		assertArrayEquals(new double[]{0.53125, 62.96875, 0.03125}, EngramAim.onPixel(0.5, 63.0, 0.01, 0, -1, 0), 1e-9);
		// Two touches within one sixteenth share a pixel; across its edge they part.
		assertArrayEquals(EngramAim.onPixel(3.01, 64, 3.01, 0, 1, 0), EngramAim.onPixel(3.06, 64, 3.06, 0, 1, 0), 1e-9);
		assertNotEquals(EngramAim.onPixel(3.06, 64, 3.0, 0, 1, 0)[0], EngramAim.onPixel(3.07, 64, 3.0, 0, 1, 0)[0]);
	}
}
