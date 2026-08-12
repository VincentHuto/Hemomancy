package com.vincenthuto.hemomancy.common.event.worldevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ChamberBoundaryRulesTest {
	@Test
	void placementAndClampShareTheAuthoritativeRadius() {
		assertTrue(ChamberBoundaryRules.insidePlatform(10, 10, 0, 0, 10));
		assertFalse(ChamberBoundaryRules.insidePlatform(11, 0, 0, 0, 10));
		assertEquals(10.5, ChamberBoundaryRules.clampCoordinate(30.0, 0, 10));
		assertEquals(-9.5, ChamberBoundaryRules.clampCoordinate(-30.0, 0, 10));
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
