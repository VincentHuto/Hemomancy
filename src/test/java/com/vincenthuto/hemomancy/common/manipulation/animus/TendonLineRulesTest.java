package com.vincenthuto.hemomancy.common.manipulation.animus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TendonLineRulesTest {
	private static final double BARE_ANCHOR_REACH = 5.5D * TendonLineRules.REACH_SCALE;

	@Test
	void theLineReachesThreeTimesTheVerbAndSnapsAThirdFurther() {
		assertEquals(16.5D, BARE_ANCHOR_REACH, 1.0E-9D);
		assertTrue(TendonLineRules.canAnchor(16.49D, BARE_ANCHOR_REACH));
		assertFalse(TendonLineRules.canAnchor(16.51D, BARE_ANCHOR_REACH));
		assertEquals(22.0D, TendonLineRules.detachRange(BARE_ANCHOR_REACH), 1.0E-9D);
		assertFalse(TendonLineRules.shouldDetach(21.99D, BARE_ANCHOR_REACH));
		assertTrue(TendonLineRules.shouldDetach(22.01D, BARE_ANCHOR_REACH));
	}

	@Test
	void theHaulArrivesWithinACloseDistance() {
		assertTrue(TendonLineRules.hasArrived(1.34D));
		assertFalse(TendonLineRules.hasArrived(1.36D));
	}

	@Test
	void thePullHasAFixedSpeedTowardTheAnchor() {
		TendonLineRules.Motion up = TendonLineRules.pullMotion(0.0D, 0.0D, 0.0D, 0.0D, 10.0D, 0.0D);
		assertEquals(0.0D, up.x(), 1.0E-9D);
		assertEquals(TendonLineRules.PULL_SPEED, up.y(), 1.0E-9D);
		assertEquals(0.0D, up.z(), 1.0E-9D);
		TendonLineRules.Motion still = TendonLineRules.pullMotion(1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
		assertEquals(0.0D, still.x() + still.y() + still.z(), 1.0E-9D);
	}

	@Test
	void aThrowAndAHaulPriceAsDesigned() {
		assertEquals(10.0D, TendonLineRules.THROW_BLOOD * 20.0D / TendonLineRules.THROW_CADENCE, 1.0E-9D);
		assertEquals(12.0D, TendonLineRules.HAUL_BLOOD_PER_TICK * 20.0D / TendonLineRules.HAUL_CADENCE, 1.0E-9D);
	}
}
