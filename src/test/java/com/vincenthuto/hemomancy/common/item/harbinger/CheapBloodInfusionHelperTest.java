package com.vincenthuto.hemomancy.common.item.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheapBloodInfusionHelperTest {
	@Test
	void eachInfusionRaisesTheLevelUpToTheFourthDose() {
		assertEquals(0, CheapBloodInfusionRules.nextAmplifier(-1));
		assertEquals(1, CheapBloodInfusionRules.nextAmplifier(0));
		assertEquals(3, CheapBloodInfusionRules.nextAmplifier(3));
		assertEquals(3, CheapBloodInfusionRules.nextAmplifier(9));
	}

	@Test
	void drunkennessRaisesCastCostAFifteenthALevel() {
		assertEquals(1.0D, CheapBloodInfusionRules.getManipulationCostMultiplier(-1), 1e-9);
		assertEquals(1.15D, CheapBloodInfusionRules.getManipulationCostMultiplier(0), 1e-9);
		assertEquals(1.60D, CheapBloodInfusionRules.getManipulationCostMultiplier(3), 1e-9);
		assertEquals(1.60D, CheapBloodInfusionRules.getManipulationCostMultiplier(8), 1e-9);
	}

	@Test
	void onlyTheTopLevelSlowsCooldowns() {
		assertEquals(1.0D, CheapBloodInfusionRules.getManipulationCooldownMultiplier(2), 1e-9);
		assertEquals(1.25D, CheapBloodInfusionRules.getManipulationCooldownMultiplier(3), 1e-9);
		assertEquals(1.25D, CheapBloodInfusionRules.getManipulationCooldownMultiplier(8), 1e-9);
	}

	@Test
	void repeatedDrinksGiveLessAndLess() {
		assertEquals(1.00D, CheapBloodInfusionRules.yieldShare(-1), 1e-9);
		assertEquals(0.80D, CheapBloodInfusionRules.yieldShare(0), 1e-9);
		assertEquals(0.60D, CheapBloodInfusionRules.yieldShare(1), 1e-9);
		assertEquals(0.40D, CheapBloodInfusionRules.yieldShare(2), 1e-9);
		assertEquals(0.25D, CheapBloodInfusionRules.yieldShare(3), 1e-9);
		assertEquals(0.25D, CheapBloodInfusionRules.yieldShare(7), 1e-9);
		assertEquals(3 * 60 * 20, CheapBloodInfusionRules.DURATION_TICKS, "three minutes without a drink resets the count");
	}
}
