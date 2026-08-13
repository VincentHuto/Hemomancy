package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ChamberVisitRulesTest {
	@Test
	void dreamChanceEscalatesThenSettlesAfterFirstVisit() {
		assertEquals(0.35D, ChamberVisitRules.dreamChance(0, false));
		assertEquals(0.65D, ChamberVisitRules.dreamChance(1, false));
		assertEquals(1.00D, ChamberVisitRules.dreamChance(2, false));
		assertEquals(0.25D, ChamberVisitRules.dreamChance(0, true));
	}

	@Test
	void durationsFollowDegreeAndAttunement() {
		assertEquals(1_200, ChamberVisitRules.durationTicks(1, ChamberVisitMode.DREAM, false));
		assertEquals(2_400, ChamberVisitRules.durationTicks(2, ChamberVisitMode.DREAM, false));
		assertEquals(6_000, ChamberVisitRules.durationTicks(3, ChamberVisitMode.TIMED_CHAIR, false));
		assertEquals(12_000, ChamberVisitRules.durationTicks(4, ChamberVisitMode.TIMED_CHAIR, false));
		assertEquals(24_000, ChamberVisitRules.durationTicks(5, ChamberVisitMode.TIMED_CHAIR, false));
		assertEquals(24_000, ChamberVisitRules.durationTicks(6, ChamberVisitMode.TIMED_CHAIR, false));
		assertEquals(0, ChamberVisitRules.durationTicks(6, ChamberVisitMode.ATTUNED, true));
	}

	@Test
	void permissionsDistinguishDreamTimedAndAttunedVisits() {
		assertTrue(ChamberVisitRules.canUseArbor(ChamberVisitMode.DREAM));
		assertFalse(ChamberVisitRules.canBuild(ChamberVisitMode.DREAM));
		assertFalse(ChamberVisitRules.canMoveItems(ChamberVisitMode.DREAM));
		assertTrue(ChamberVisitRules.isProtected(ChamberVisitMode.DREAM));

		assertTrue(ChamberVisitRules.canBuild(ChamberVisitMode.TIMED_CHAIR));
		assertTrue(ChamberVisitRules.canMoveItems(ChamberVisitMode.TIMED_CHAIR));
		assertTrue(ChamberVisitRules.isProtected(ChamberVisitMode.TIMED_CHAIR));

		assertTrue(ChamberVisitRules.canBuild(ChamberVisitMode.ATTUNED));
		assertFalse(ChamberVisitRules.isProtected(ChamberVisitMode.ATTUNED));
	}

	@Test
	void roomRadiusGrowsAtEveryDegreeAndClamps() {
		assertEquals(3, ChamberVisitRules.radiusForDegree(0));
		assertEquals(3, ChamberVisitRules.radiusForDegree(1));
		assertEquals(4, ChamberVisitRules.radiusForDegree(2));
		assertEquals(5, ChamberVisitRules.radiusForDegree(3));
		assertEquals(8, ChamberVisitRules.radiusForDegree(6));
		assertEquals(10, ChamberVisitRules.radiusForDegree(8));
		assertEquals(10, ChamberVisitRules.radiusForDegree(20));
	}

	@Test
	void dreamEligibilityEndsWhenChairProgressionBegins() {
		assertTrue(ChamberVisitRules.ordinaryBedDreamEligible(1, false, false));
		assertTrue(ChamberVisitRules.ordinaryBedDreamEligible(2, false, false));
		assertFalse(ChamberVisitRules.ordinaryBedDreamEligible(3, false, false));
		assertFalse(ChamberVisitRules.ordinaryBedDreamEligible(2, true, false));
		assertFalse(ChamberVisitRules.ordinaryBedDreamEligible(2, false, true));
	}
}
