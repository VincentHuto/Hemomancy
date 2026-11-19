package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The finale's gates since the route moved to the kneel (2026-11-18). */
class CircusRouteRulesTest {
	@Test
	void finaleOpensAfterTheAttentionActForAnOpenGate() {
		assertTrue(CircusRouteRules.canBeginFinale(0b1, CircusRouteRules.PactGate.OPEN));
		assertFalse(CircusRouteRules.canBeginFinale(0b0, CircusRouteRules.PactGate.OPEN));
		assertFalse(CircusRouteRules.canBeginFinale(0b1, CircusRouteRules.PactGate.NEEDS_DEGREE));
	}

	@Test
	void absorptionNeedsFullAcclimationTheActsAndNotUnstained() {
		assertTrue(CircusRouteRules.mayAbsorb(1000, 0b1_1101, false));
		assertFalse(CircusRouteRules.mayAbsorb(999, 0b1_1101, false));
		assertFalse(CircusRouteRules.mayAbsorb(1000, 0b0_1101, false));
		assertFalse(CircusRouteRules.mayAbsorb(1000, 0b1_1101, true));
	}

	/** D-30, NQ-15-5: Degree 4 and any one school report open the finale for everyone but the Unstained. */
	@Test
	void theFinaleNeedsTheFourthDegreeAndAnyOneReport() {
		assertEquals(CircusRouteRules.PactGate.OPEN, CircusRouteRules.pactGate(true, true, false, false));
		assertEquals(CircusRouteRules.PactGate.NEEDS_DEGREE, CircusRouteRules.pactGate(false, true, true, false));
		assertEquals(CircusRouteRules.PactGate.NEEDS_REPORT, CircusRouteRules.pactGate(true, false, false, false));
		assertEquals(CircusRouteRules.PactGate.NEEDS_DEGREE_AND_REPORT, CircusRouteRules.pactGate(false, false, false, false));
		assertEquals(4, CircusRouteRules.PACT_DEGREE);
	}

	/** D-31, NQ-15-6 B: the Unstained fight on The Missing Understudy report and no Degree. */
	@Test
	void theUnstainedMayFightWithTheUnderstudyReport() {
		assertEquals(CircusRouteRules.PactGate.OPEN, CircusRouteRules.pactGate(false, false, true, true));
		assertEquals(CircusRouteRules.PactGate.NEEDS_UNDERSTUDY, CircusRouteRules.pactGate(true, true, false, true));
	}
}
