package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingAxeLodestoneRulesTest {
	@Test
	void fieldsMergeWhenTheirEdgesOverlapEvenIfNeitherContainsTheOtherCenter() {
		assertTrue(LivingAxeLodestoneRules.overlaps(0, 0, 4.5, 0));
		assertFalse(LivingAxeLodestoneRules.overlaps(0, 0, 5.01, 0));
	}

	@Test
	void fieldPulsesImmediatelyThenOncePerSecondForSixSeconds() {
		assertTrue(LivingAxeLodestoneRules.shouldPulse(40L, 40L));
		assertFalse(LivingAxeLodestoneRules.shouldPulse(40L, 59L));
		assertTrue(LivingAxeLodestoneRules.shouldPulse(40L, 60L));
		assertFalse(LivingAxeLodestoneRules.isActive(40L, 160L));
	}

	@Test
	void fieldUsesHorizontalTwoAndAHalfBlockRadius() {
		assertTrue(LivingAxeLodestoneRules.contains(0.0D, 0.0D, 1.5D, 2.0D));
		assertFalse(LivingAxeLodestoneRules.contains(0.0D, 0.0D, 2.0D, 2.0D));
	}

	@Test
	void sameOwnerCriticalRefreshesRatherThanShortensTheField() {
		assertEquals(170L, LivingAxeLodestoneRules.mergedExpiry(50L, 130L));
		assertEquals(200L, LivingAxeLodestoneRules.mergedExpiry(80L, 200L));
	}

	@Test
	void pulseDragsTargetsTowardTheCentreWithoutOvershooting() {
		double[] far = LivingAxeLodestoneRules.pull(0, 0, 2.0D, 0, 0);
		assertTrue(far[0] < 0 && far[1] == 0, "The pull must point at the centre");
		assertEquals(LivingAxeLodestoneRules.PULL_STRENGTH, -far[0], 1.0E-9);
		double[] near = LivingAxeLodestoneRules.pull(0, 0, 0.2D, 0, 0);
		assertTrue(-near[0] <= 0.2D, "A target near the centre must not be flung past it");
	}

	@Test
	void knockbackResistanceHoldsTargetsInPlace() {
		double[] half = LivingAxeLodestoneRules.pull(0, 0, 0, 2.0D, 0.5D);
		assertEquals(-LivingAxeLodestoneRules.PULL_STRENGTH * 0.5D, half[1], 1.0E-9);
		double[] anchored = LivingAxeLodestoneRules.pull(0, 0, 0, 2.0D, 1.0D);
		assertEquals(0.0D, anchored[0]);
		assertEquals(0.0D, anchored[1]);
	}
}
