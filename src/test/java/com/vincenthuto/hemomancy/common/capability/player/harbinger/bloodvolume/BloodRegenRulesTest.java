package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Core-loop Track 10 (CL-18): the body sets blood regeneration. A starving body makes none, a hungry one half, a plain
 * one the base, a well-fed one half as much again; a night's sleep adds the base on top while food lasts. A freshly
 * spawned body (a full bar, 5 saturation) is plain, so a fixed GameTest player regenerates at the base rate.
 */
class BloodRegenRulesTest {
	@Test
	void theFoodBarSetsTheRate() {
		assertEquals(0.0D, BloodRegenRules.multiplier(0, 0.0F, false));
		assertEquals(0.5D, BloodRegenRules.multiplier(5, 0.0F, false));
		assertEquals(1.0D, BloodRegenRules.multiplier(6, 0.0F, false));
		assertEquals(1.0D, BloodRegenRules.multiplier(20, 5.0F, false), "a fresh body is plain");
		assertEquals(1.0D, BloodRegenRules.multiplier(19, 15.0F, false), "a bar short of full is plain");
		assertEquals(1.5D, BloodRegenRules.multiplier(20, 12.8F, false), "after a steak");
	}

	/**
	 * Track 11's gate table: the Apotheos hunger pin (food held at {@code HUNGER_PIN}, saturation 0) must survive the
	 * body rule, so an Apotheos regenerates at the plain base rate rather than starving or feasting.
	 */
	@Test
	void theApotheosHungerPinRegeneratesAtTheBaseRate() {
		int pin = com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.ApotheosBodyRules.HUNGER_PIN;
		assertEquals(BloodRegenRules.Body.PLAIN, BloodRegenRules.body(pin, 0.0F));
		assertEquals(1.0D, BloodRegenRules.multiplier(pin, 0.0F, false));
	}

	@Test
	void restAddsOnTopButNeverFeedsAStarvingBody() {
		assertEquals(2.5D, BloodRegenRules.multiplier(20, 12.8F, true));
		assertEquals(2.0D, BloodRegenRules.multiplier(20, 5.0F, true));
		assertEquals(1.5D, BloodRegenRules.multiplier(3, 0.0F, true));
		assertEquals(0.0D, BloodRegenRules.multiplier(0, 0.0F, true));
	}

	@Test
	void theBodyIsNamedForTeaching() {
		assertEquals(BloodRegenRules.Body.STARVING, BloodRegenRules.body(0, 0.0F));
		assertEquals(BloodRegenRules.Body.HUNGRY, BloodRegenRules.body(5, 5.0F));
		assertEquals(BloodRegenRules.Body.PLAIN, BloodRegenRules.body(20, 9.9F));
		assertEquals(BloodRegenRules.Body.WELL_FED, BloodRegenRules.body(20, 10.0F));
	}

	@Test
	void aMealIsItsNutritionTimesItsWorth() {
		assertEquals(48.0D, BloodRegenRules.mealBlood(8, 6.0D), "a steak");
		assertEquals(0.0D, BloodRegenRules.mealBlood(0, 6.0D));
		assertEquals(0.0D, BloodRegenRules.mealBlood(8, 0.0D));
		assertEquals(0.0D, BloodRegenRules.mealBlood(-2, 6.0D));
	}
}
