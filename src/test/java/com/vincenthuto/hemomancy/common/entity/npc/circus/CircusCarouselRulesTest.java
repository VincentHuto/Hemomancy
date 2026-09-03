package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircusCarouselRulesTest {
	@Test
	void carouselAcceleratesAndSpacesThreeBobbingHorses() {
		assertEquals(0.5F, CircusCarouselRules.targetSpeed(false));
		assertEquals(3.0F, CircusCarouselRules.targetSpeed(true));
		assertEquals(0.6F, CircusCarouselRules.nextSpeed(0.5F, true), 0.0001F);
		assertEquals(2.9F, CircusCarouselRules.nextSpeed(3.0F, false), 0.0001F);

		var first = CircusCarouselRules.horsePose(0.0F, 0);
		var second = CircusCarouselRules.horsePose(0.0F, 1);
		var third = CircusCarouselRules.horsePose(0.0F, 2);
		assertEquals(CircusCarouselRules.HORSE_RADIUS, Math.hypot(first.x(), first.z()), 0.0001D);
		assertEquals(120.0F, second.angleDegrees() - first.angleDegrees(), 0.0001F);
		assertEquals(120.0F, third.angleDegrees() - second.angleDegrees(), 0.0001F);
		assertNotEquals(first.bob(), second.bob());
	}

	@Test
	void onlyAlertTroupesActivateTheHazard() {
		assertFalse(CircusCarouselRules.shouldActivate(0));
		assertTrue(CircusCarouselRules.shouldActivate(1));
		assertFalse(CircusCarouselRules.canStrike(19));
		assertTrue(CircusCarouselRules.canStrike(20));
	}
}
