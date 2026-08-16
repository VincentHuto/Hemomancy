package com.vincenthuto.hemomancy.common.item.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BloodSamplingRulesTest {
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
