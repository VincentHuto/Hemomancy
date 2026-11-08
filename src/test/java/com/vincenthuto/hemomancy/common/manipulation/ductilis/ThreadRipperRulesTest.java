package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ThreadRipperRulesTest {
	@Test
	void healthyEnemyPuppetIsDisruptedAndWeakPuppetUnravels() {
		assertEquals(ThreadRipperRules.Outcome.DISRUPT,
				ThreadRipperRules.outcome(true, false, 0.31F));
		assertEquals(ThreadRipperRules.Outcome.UNRAVEL,
				ThreadRipperRules.outcome(true, false, 0.30F));
	}

	@Test
	void alliedOrProtectedBodiesAreImmune() {
		assertEquals(ThreadRipperRules.Outcome.NONE,
				ThreadRipperRules.outcome(true, true, 0.1F));
		assertEquals(ThreadRipperRules.Outcome.NONE,
				ThreadRipperRules.outcome(false, false, 0.1F));
	}

	@Test
	void foreignSummonsUnravelAtAnyHealthUnlessProtected() {
		assertEquals(ThreadRipperRules.Outcome.UNRAVEL,
				ThreadRipperRules.outcome(false, true, false, 1.0F));
		assertEquals(ThreadRipperRules.Outcome.NONE,
				ThreadRipperRules.outcome(false, true, true, 1.0F));
		// A puppet keeps its own health-gated disruption.
		assertEquals(ThreadRipperRules.Outcome.DISRUPT,
				ThreadRipperRules.outcome(true, true, false, 1.0F));
	}
}
