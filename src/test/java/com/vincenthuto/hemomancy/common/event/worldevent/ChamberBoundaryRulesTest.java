package com.vincenthuto.hemomancy.common.event.worldevent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ChamberBoundaryRulesTest {
	@Test
	void placementAndClampShareTheAuthoritativeRadius() {
		assertTrue(ChamberBoundaryRules.insidePlatform(10, 0, 0, 0, 10));
		assertFalse(ChamberBoundaryRules.insidePlatform(11, 0, 0, 0, 10));
		assertFalse(ChamberBoundaryRules.insidePlatform(10, 10, 0, 0, 10), "the old square's corner is gone");
		assertEquals(11.0, ChamberBoundaryRules.clamp(30.0, 0.5, 0, 0, 10).dz() + 10.5, 1e-6);
		assertEquals(0.5 + 10.5, ChamberBoundaryRules.clamp(30.0, 0.5, 0, 0, 10).dx(), 1e-6);
		assertEquals(0.5 - 10.5, ChamberBoundaryRules.clamp(0.5, -30.0, 0, 0, 10).dz(), 1e-6);
	}

	@Test
	void aBodyInsideIsHandedBackExactlyAsItStood() {
		// Any rounding here reads as a change, and the player is teleported, and stopped, every tick.
		for (double x : new double[] { 0.1, 0.3, 0.49, -0.2, -0.45, 1.7, 2.13 }) {
			for (double z : new double[] { 0.1, 0.37, -0.2, 3.3 }) {
				var held = ChamberBoundaryRules.clamp(x, z, 0, 0, 5);
				assertEquals(x, held.dx(), 0.0D);
				assertEquals(z, held.dz(), 0.0D);
				var farCell = ChamberBoundaryRules.clamp(x, 512.0D + z, 0, 512, 5);
				assertEquals(x, farCell.dx(), 0.0D);
				assertEquals(512.0D + z, farCell.dz(), 0.0D);
			}
		}
	}

	@Test
	void orbAndOrdinaryRescuePlanesAreDistinct() {
		assertFalse(ChamberBoundaryRules.belowOrbPlane(-1.0, 2));
		assertTrue(ChamberBoundaryRules.belowOrbPlane(-1.01, 2));
		assertFalse(ChamberBoundaryRules.belowRescuePlane(-6.0, 2));
		assertTrue(ChamberBoundaryRules.belowRescuePlane(-6.01, 2));
	}

	@Test
	void allocatedCellAllowsAPlatformEdgeThrowButRejectsAnotherCell() {
		assertTrue(ChamberBoundaryRules.insideAllocatedCell(12, 12, 0, 0, 256));
		assertFalse(ChamberBoundaryRules.insideAllocatedCell(0, 140, 0, 0, 256));
	}
}
