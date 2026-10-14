package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RingOpeningMathTest {
	@Test
	void depthRunsFromTheFirstLaterRingToTheLast() {
		assertEquals(1.0F, RingOpeningMath.depth(1, 2), 1e-6F, "a two-ring rite's only later ring is also its last");
		assertEquals(0.0F, RingOpeningMath.depth(1, 3), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.depth(2, 3), 1e-6F);
		assertEquals(0.0F, RingOpeningMath.depth(1, 8), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.depth(7, 8), 1e-6F);
		assertTrue(RingOpeningMath.depth(4, 8) > RingOpeningMath.depth(3, 8));
	}

	@Test
	void everyFactorGrowsWithDepth() {
		assertTrue(RingOpeningMath.burstCells(1.0F) > RingOpeningMath.burstCells(0.0F));
		assertTrue(RingOpeningMath.tearTicks(1.0F) > RingOpeningMath.tearTicks(0.0F));
		assertTrue(RingOpeningMath.shockScale(1.0F) > RingOpeningMath.shockScale(0.0F));
		assertTrue(RingOpeningMath.shockReach(1.0F) > RingOpeningMath.shockReach(0.0F));
		assertTrue(RingOpeningMath.stingVolume(1.0F) > RingOpeningMath.stingVolume(0.0F));
		assertTrue(RingOpeningMath.stingPitch(1.0F) < RingOpeningMath.stingPitch(0.0F), "deeper rings sound lower");
		assertTrue(RingOpeningMath.fogSurge(1.0F) > RingOpeningMath.fogSurge(0.0F));
		assertEquals(1.0F, RingOpeningMath.fogSurge(1.0F), 1e-6F, "the last ring surges the world fully");
	}

	@Test
	void aSoreTearsFromItsScarToFullSize() {
		assertEquals(0.0F, RingOpeningMath.tear(0.0F, 0.5F), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.tear(RingOpeningMath.tearTicks(0.5F), 0.5F), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.tear(-1.0F, 0.5F), 1e-6F, "no beat remembered: fully open");
		assertTrue(RingOpeningMath.SCAR_SCALE > 0.0F && RingOpeningMath.SCAR_SCALE < 0.5F);
	}
}
