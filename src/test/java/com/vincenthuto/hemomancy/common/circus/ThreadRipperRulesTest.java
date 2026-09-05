package com.vincenthuto.hemomancy.common.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ThreadRipperRulesTest {
	@Test
	void liberationRiderIsSeveredRegardlessOfHealth() {
		assertEquals(ThreadRipperRules.Outcome.SEVER_CAPTIVE,
				ThreadRipperRules.outcome(true, true, false, 1.0F));
	}

	@Test
	void healthyEnemyPuppetIsDisruptedAndWeakPuppetUnravels() {
		assertEquals(ThreadRipperRules.Outcome.DISRUPT,
				ThreadRipperRules.outcome(false, true, false, 0.31F));
		assertEquals(ThreadRipperRules.Outcome.UNRAVEL,
				ThreadRipperRules.outcome(false, true, false, 0.30F));
	}

	@Test
	void alliedOrProtectedBodiesAreImmune() {
		assertEquals(ThreadRipperRules.Outcome.NONE,
				ThreadRipperRules.outcome(false, true, true, 0.1F));
		assertEquals(ThreadRipperRules.Outcome.NONE,
				ThreadRipperRules.outcome(false, false, false, 0.1F));
	}
}
