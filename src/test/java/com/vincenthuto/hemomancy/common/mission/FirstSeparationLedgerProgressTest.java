package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationLedgerProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstSeparationLedgerProgressTest {
	@Test
	void reportsTheWholeAssignmentThroughInitiatePromotion() {
		var fresh = new FirstSeparationLedgerProgress(false, false, false, false, false, false, false, false);
		assertEquals(0, fresh.completedSteps());
		assertEquals(7, fresh.totalSteps());
		assertFalse(fresh.complete());

		var promoted = new FirstSeparationLedgerProgress(true, true, true, true, true, true, false, true);
		assertEquals(7, promoted.completedSteps());
		assertTrue(promoted.complete());
	}

	@Test
	void injectionPendingDoesNotPretendThePromotionIsComplete() {
		var waitingForSleep = new FirstSeparationLedgerProgress(true, true, true, true, true, true, true, false);
		assertEquals(6, waitingForSleep.completedSteps());
		assertTrue(waitingForSleep.concentratedBloodPending());
		assertFalse(waitingForSleep.complete());
	}
}
