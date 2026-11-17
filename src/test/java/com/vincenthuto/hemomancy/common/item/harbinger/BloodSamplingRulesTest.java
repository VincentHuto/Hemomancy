package com.vincenthuto.hemomancy.common.item.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodSamplingRulesTest {
	@Test
	void onlyUsingTheLivingSyringeUnlocksRestrictedTargets() {
		assertEquals(BloodSamplingResult.REQUIRES_LIVING_SYRINGE,
				BloodSamplingRules.evaluate(false, true, true, false, true, true, false));
		assertEquals(BloodSamplingResult.SUCCESS,
				BloodSamplingRules.evaluate(false, true, true, false, true, true, true));
		assertEquals(BloodSamplingResult.SUCCESS,
				BloodSamplingRules.evaluate(false, true, true, false, true, false, false));
		assertEquals(BloodSamplingResult.INSUFFICIENT_CONDITION,
				BloodSamplingRules.evaluate(false, true, false, false, true, true, true));
		assertEquals(BloodSamplingResult.INSUFFICIENT_CONDITION,
				BloodSamplingRules.evaluate(false, true, true, true, true, true, true));
		assertEquals(BloodSamplingResult.ALREADY_FILLED,
				BloodSamplingRules.evaluate(true, true, true, false, true, true, false));
	}
	@Test
	void distinguishesSuccessFromEveryPlayerCorrectableFailure() {
		assertEquals(BloodSamplingResult.ALREADY_FILLED,
				BloodSamplingRules.evaluate(true, true, true, false, true));
		assertEquals(BloodSamplingResult.INVALID_TARGET,
				BloodSamplingRules.evaluate(false, false, true, false, true));
		assertEquals(BloodSamplingResult.INSUFFICIENT_CONDITION,
				BloodSamplingRules.evaluate(false, true, false, false, true));
		assertEquals(BloodSamplingResult.INSUFFICIENT_CONDITION,
				BloodSamplingRules.evaluate(false, true, true, true, true));
		assertEquals(BloodSamplingResult.FAILED,
				BloodSamplingRules.evaluate(false, true, true, false, false));
		assertEquals(BloodSamplingResult.SUCCESS,
				BloodSamplingRules.evaluate(false, true, true, false, true));
	}

	@Test
	void aDrawCostsTheCreatureAndARecentlyDrawnOneRefuses() {
		// Core-loop Track 5: a vial takes 2 health, a syringe 1, and the creature recovers for 30 seconds.
		assertEquals(2.0F, BloodSamplingRules.healthCost(false));
		assertEquals(1.0F, BloodSamplingRules.healthCost(true));
		assertEquals(600, BloodSamplingRules.COOLDOWN_TICKS);
		assertEquals(true, BloodSamplingRules.recentlyDrawn(1_000L, 999L));
		assertEquals(false, BloodSamplingRules.recentlyDrawn(1_000L, 1_000L));
		assertEquals(BloodSamplingResult.RECENTLY_DRAWN,
				BloodSamplingRules.evaluate(false, true, true, false, true, false, false, true));
		assertEquals(BloodSamplingResult.SUCCESS,
				BloodSamplingRules.evaluate(false, true, true, false, true, false, false, false));
		// Every other refusal still comes first: an empty vial check never reports the cooldown over them.
		assertEquals(BloodSamplingResult.REQUIRES_LIVING_SYRINGE,
				BloodSamplingRules.evaluate(false, true, true, false, true, true, false, true));
	}
}
