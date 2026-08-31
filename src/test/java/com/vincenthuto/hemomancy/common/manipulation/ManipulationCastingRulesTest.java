package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipulationCastingRulesTest {
	@Test
	void chargeFractionClampsUntrustedTicks() {
		assertEquals(0.0F, ManipulationCastingRules.chargeFraction(-1, 40));
		assertEquals(0.5F, ManipulationCastingRules.chargeFraction(20, 40));
		assertEquals(1.0F, ManipulationCastingRules.chargeFraction(80, 40));
	}

	@Test
	void chargedCostScalesTheFinalModifiedCost() {
		assertEquals(150.0D, ManipulationCastingRules.chargedCost(300.0D, 20, 40));
		assertEquals(300.0D, ManipulationCastingRules.chargedCost(300.0D, 40, 40));
	}
}
