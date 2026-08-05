package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BrazierItemAbsorptionRiteTest {
	@Test
	void itemAbsorptionCompletesAfterSixtyContinuousTicks() {
		assertFalse(BrazierItemAbsorptionRite.isComplete(59));
		assertTrue(BrazierItemAbsorptionRite.isComplete(60));
	}

	@Test
	void itemParticlesPulseEveryTenTicksThroughoutTheLoop() {
		assertFalse(BrazierItemAbsorptionRite.shouldEmitPulse(0));
		assertFalse(BrazierItemAbsorptionRite.shouldEmitPulse(9));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitPulse(10));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitPulse(50));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitPulse(60));
		assertFalse(BrazierItemAbsorptionRite.shouldEmitPulse(61));
	}

	@Test
	void offeringItemParticlesStreamEveryTickThroughoutTheLoop() {
		assertFalse(BrazierItemAbsorptionRite.shouldEmitItemStream(0));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitItemStream(1));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitItemStream(37));
		assertTrue(BrazierItemAbsorptionRite.shouldEmitItemStream(60));
		assertFalse(BrazierItemAbsorptionRite.shouldEmitItemStream(61));
	}
}
