package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FirstBloodcraftLedgerProgressTest {
	@Test
	void absorptionAccumulatesAndProofsAreIndependentOfOrder() {
		var early = new FirstBloodcraftLedgerProgress(499, true, true, true, false);
		assertFalse(early.readyForVicar());
		assertEquals(3, early.completedProofs());
		var complete = new FirstBloodcraftLedgerProgress(500, true, true, true, false);
		assertTrue(complete.readyForVicar());
		assertEquals(4, complete.completedProofs());
		assertFalse(new FirstBloodcraftLedgerProgress(900, true, false, true, false).readyForVicar());
		assertFalse(new FirstBloodcraftLedgerProgress(900, true, true, false, false).readyForVicar());
	}
}
