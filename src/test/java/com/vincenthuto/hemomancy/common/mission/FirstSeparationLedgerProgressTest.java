package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationLedgerProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstSeparationLedgerProgressTest {
	@Test
	void reportsTheWholeAssignmentThroughInitiatePromotion() {
		var fresh = new FirstSeparationLedgerProgress(false, false, false, false, false, false, false, false, false);
		assertEquals(0, fresh.completedSteps());
		assertEquals(8, fresh.totalSteps());
		assertFalse(fresh.complete());

		var promoted = new FirstSeparationLedgerProgress(true, true, true, true, true, true, true, false, true);
		assertEquals(8, promoted.completedSteps());
		assertTrue(promoted.complete());
	}

	@Test
	void injectionPendingDoesNotPretendThePromotionIsComplete() {
		var waitingForSleep = new FirstSeparationLedgerProgress(true, true, true, true, true, true, true, true, false);
		assertEquals(7, waitingForSleep.completedSteps());
		assertTrue(waitingForSleep.concentratedBloodPending());
		assertFalse(waitingForSleep.complete());
	}

	@Test
	void promotionStateDoesNotHideMissingDistillationProof() {
		var missingDistillation = new FirstSeparationLedgerProgress(
				true, true, true, true, true, true, false, false, true);
		assertEquals(7, missingDistillation.completedSteps());
		assertFalse(missingDistillation.complete());
	}
}
