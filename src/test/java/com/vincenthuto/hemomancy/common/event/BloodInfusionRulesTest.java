package com.vincenthuto.hemomancy.common.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BloodInfusionRulesTest {
	@Test
	void acceptsOnlyPositiveFiniteBloodCosts() {
		assertTrue(BloodInfusionRules.isValidCost(50.0D));
		assertFalse(BloodInfusionRules.isValidCost(0.0D));
		assertFalse(BloodInfusionRules.isValidCost(-1.0D));
		assertFalse(BloodInfusionRules.isValidCost(Double.NaN));
		assertFalse(BloodInfusionRules.isValidCost(Double.POSITIVE_INFINITY));
	}

	@Test
	void sourceMustStillMatchAtCollapse() {
		assertTrue(BloodInfusionRules.canComplete(true, false));
		assertFalse(BloodInfusionRules.canComplete(false, false));
		assertFalse(BloodInfusionRules.canComplete(true, true));
	}
}
