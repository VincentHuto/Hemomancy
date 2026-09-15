package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingAxeRotRulesTest {
	@Test
	void poolsMergeWhenTheirEdgesOverlapEvenIfNeitherContainsTheOtherCenter() {
		assertTrue(LivingAxeRotRules.overlaps(0, 0, 4.5, 0));
		assertFalse(LivingAxeRotRules.overlaps(0, 0, 5.01, 0));
	}

	@Test
	void poolPulsesImmediatelyThenOncePerSecondForSixSeconds() {
		assertTrue(LivingAxeRotRules.shouldPulse(40L, 40L));
		assertFalse(LivingAxeRotRules.shouldPulse(40L, 59L));
		assertTrue(LivingAxeRotRules.shouldPulse(40L, 60L));
		assertFalse(LivingAxeRotRules.isActive(40L, 160L));
	}

	@Test
	void poolUsesHorizontalTwoAndAHalfBlockRadius() {
		assertTrue(LivingAxeRotRules.contains(0.0D, 0.0D, 1.5D, 2.0D));
		assertFalse(LivingAxeRotRules.contains(0.0D, 0.0D, 2.0D, 2.0D));
	}

	@Test
	void sameOwnerCriticalRefreshesRatherThanShortensThePool() {
		assertEquals(170L, LivingAxeRotRules.mergedExpiry(50L, 130L));
		assertEquals(200L, LivingAxeRotRules.mergedExpiry(80L, 200L));
	}
}
