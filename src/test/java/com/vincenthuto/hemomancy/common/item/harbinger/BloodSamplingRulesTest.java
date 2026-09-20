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
}
