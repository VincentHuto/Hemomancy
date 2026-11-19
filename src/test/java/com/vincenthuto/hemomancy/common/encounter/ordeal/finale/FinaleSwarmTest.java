package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** His bats' stolen blood charges him only when it reaches him (2026-11-18). */
class FinaleSwarmTest {
	@Test
	void onlyBloodThatReachesHimCharges() {
		assertEquals(FinaleRules.CHARGE_PER_SIP, FinaleSwarm.chargeFrom(0, true));
		assertEquals(20, FinaleSwarm.chargeFrom(0, true));
		assertEquals(0, FinaleSwarm.chargeFrom(0, false), "a bat killed on the way back brings nothing");
		assertEquals(40, FinaleSwarm.chargeFrom(40, false));
		assertEquals(FinaleRules.CHARGE_MAX, FinaleSwarm.chargeFrom(95, true), "capped at 100");
		assertEquals(100, FinaleSwarm.chargeFrom(100, true));
	}

	@Test
	void aSipIsTwentyFiveMillilitres() {
		assertEquals(25, FinaleSwarm.SIP_ML);
	}
}
