package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cruor Javelin drawn and thrown: its stages, what each is worth, and what Absorption takes back. */
class CruorJavelinThrowRulesTest {
	@Test
	void theDrawClimbsThroughThreeStagesAndStops() {
		assertEquals(1, CruorJavelinThrowRules.stage(0));
		assertEquals(1, CruorJavelinThrowRules.stage(14));
		assertEquals(2, CruorJavelinThrowRules.stage(15));
		assertEquals(3, CruorJavelinThrowRules.stage(30));
		assertEquals(3, CruorJavelinThrowRules.stage(900));
	}

	@Test
	void aDrawLetGoTooSoonThrowsNothing() {
		assertFalse(CruorJavelinThrowRules.throwable(4));
		assertTrue(CruorJavelinThrowRules.throwable(5));
	}

	@Test
	void eachStageFliesFasterAndHitsHarder() {
		for (int stage = 2; stage <= 3; stage++) {
			assertTrue(CruorJavelinThrowRules.speed(stage) > CruorJavelinThrowRules.speed(stage - 1));
			assertTrue(CruorJavelinThrowRules.damage(stage) > CruorJavelinThrowRules.damage(stage - 1));
			assertTrue(CruorJavelinThrowRules.length(stage) > CruorJavelinThrowRules.length(stage - 1));
		}
		assertEquals(4.0F, CruorJavelinThrowRules.damage(1));
		assertEquals(10.0F, CruorJavelinThrowRules.damage(3));
		assertEquals(10.0F, CruorJavelinThrowRules.damage(7), "a stage past the last is the last");
	}

	@Test
	void aWetBodyTakesOneMoreRimeLevel() {
		assertEquals(1, CruorJavelinThrowRules.rimeLevels(1, false));
		assertEquals(3, CruorJavelinThrowRules.rimeLevels(3, false));
		assertEquals(4, CruorJavelinThrowRules.rimeLevels(3, true));
	}

	@Test
	void absorptionTakesBackHalfWhatWasDrawnIn() {
		assertEquals(50.0D, CruorJavelinThrowRules.refund(100.0D), 1.0E-9D);
		assertEquals(0.0D, CruorJavelinThrowRules.refund(-5.0D), 1.0E-9D);
	}
}
