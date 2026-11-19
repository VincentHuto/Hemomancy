package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Only a fit, settled owner may absorb him at the kneel (2026-11-18); the kill stays open to everyone who wins. */
class RingmasterAbsorptionRulesTest {
	private static final int SETTLED = FinaleRules.KNEEL_SETTLE_TICKS, FULL = 1000, ACTS = 0b1_1101;

	@Test
	void ineligiblePlayersAreRefused() {
		assertTrue(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, true, true, FULL, ACTS, false));
		assertFalse(RingmasterAbsorptionRules.canAbsorb(false, SETTLED, true, true, FULL, ACTS, false), "not kneeling");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED - 1, true, true, FULL, ACTS, false), "tick 29");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, false, true, FULL, ACTS, false), "not the owner");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, true, false, FULL, ACTS, false), "dormant blood");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, true, true, FULL - 1, ACTS, false), "999 acclimation");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, true, true, FULL, ACTS & ~0b100, false), "a missing act");
		assertFalse(RingmasterAbsorptionRules.canAbsorb(true, SETTLED, true, true, FULL, ACTS, true), "Unstained");
	}

	@Test
	void progressFillsToTheRequiredAndStops() {
		assertEquals(100, RingmasterAbsorptionRules.ABSORPTION_REQUIRED);
		assertEquals(40.0F, RingmasterAbsorptionRules.advance(39.0F, 1.0F), 1.0E-6F);
		assertEquals(100.0F, RingmasterAbsorptionRules.advance(99.5F, 2.0F), 1.0E-6F);
		assertTrue(RingmasterAbsorptionRules.complete(100.0F));
		assertFalse(RingmasterAbsorptionRules.complete(99.9F));
	}
}
