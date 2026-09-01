package com.vincenthuto.hemomancy.common.entity.summon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SanguineHoundRulesTest {
	@Test
	void parentRupturesOnlyAfterCrossingItsInjuryThreshold() {
		assertFalse(SanguineHoundRules.shouldRupture(false, false, 12.1F, 30.0F));
		assertTrue(SanguineHoundRules.shouldRupture(false, false, 12.0F, 30.0F));
		assertFalse(SanguineHoundRules.shouldRupture(true, false, 1.0F, 5.0F));
		assertFalse(SanguineHoundRules.shouldRupture(false, true, 1.0F, 30.0F));
	}

	@Test
	void ruptureCreatesThreeToFiveShortLivedCursAndNeverRefundsBlood() {
		assertEquals(3, SanguineHoundRules.curCount(0));
		assertEquals(4, SanguineHoundRules.curCount(1));
		assertEquals(5, SanguineHoundRules.curCount(2));
		assertEquals(3, SanguineHoundRules.curCount(3));
		assertEquals(220, SanguineHoundRules.CUR_LIFETIME_TICKS);
		assertEquals(0, SanguineHoundRules.dissolutionBloodRefund(850));
	}
}
