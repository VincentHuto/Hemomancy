package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperFightOuterRingDetailTest {
	@Test
	void toggleSelectsLowPolyOrSubdividedHorizonGeometry() {
		assertEquals(1, VesperFightOuterRingDetail.fromLowPolyToggle(true).subdivisions());
		assertEquals(4, VesperFightOuterRingDetail.fromLowPolyToggle(false).subdivisions());
	}

	@Test
	void fadingFloorGeometryStillWritesDepthSoRearFacesCannotShowThrough() {
		assertEquals(true, VesperFightOuterRingDetail.LOW_POLY.writesDepth());
		assertEquals(true, VesperFightOuterRingDetail.HIGH_RES.writesDepth());
	}
}
