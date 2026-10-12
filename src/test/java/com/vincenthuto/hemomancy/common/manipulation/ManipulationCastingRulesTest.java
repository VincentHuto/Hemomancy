package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManipulationCastingRulesTest {
	@Test
	void chargeFractionClampsUntrustedTicks() {
		assertEquals(0.0F, ManipulationCastingRules.chargeFraction(-1, 40));
		assertEquals(0.5F, ManipulationCastingRules.chargeFraction(20, 40));
		assertEquals(1.0F, ManipulationCastingRules.chargeFraction(80, 40));
		assertEquals(0.0F, ManipulationCastingRules.chargeFraction(Float.NaN, 40));
	}

	@Test
	void chargedCostScalesTheFinalModifiedCost() {
		assertEquals(150.0D, ManipulationCastingRules.chargedCost(300.0D, 20, 40).getAsDouble());
		assertEquals(300.0D, ManipulationCastingRules.chargedCost(300.0D, 40, 40).getAsDouble());
	}

	@Test
	void chargedCostRefusesBelowTheQuarterFloor() {
		assertEquals(0.25F, ManipulationCastingRules.MIN_CHARGE_FRACTION);
		assertTrue(ManipulationCastingRules.chargedCost(400.0D, 4, 40).isEmpty(), "0.1 charge must be refused");
		assertTrue(ManipulationCastingRules.chargedCost(400.0D, 0, 40).isEmpty(), "no charge must be refused");
		assertEquals(100.0D, ManipulationCastingRules.chargedCost(400.0D, 10, 40).getAsDouble(), 1.0E-9,
				"0.25 charge pays 25% of the cost");
	}

	@Test
	void rewardsScaleOnlyForChargedCasts() {
		assertEquals(0.3F, ManipulationCastingRules.rewardScale(EnumManipulationType.CHARGED, 18, 60), 1.0E-6F);
		assertEquals(1.0F, ManipulationCastingRules.rewardScale(EnumManipulationType.QUICK, 0, 0));
		assertEquals(1.0F, ManipulationCastingRules.rewardScale(EnumManipulationType.CONTINUOUS, 0, 0));
		assertEquals(1.0F, ManipulationCastingRules.rewardScale(EnumManipulationType.PASSIVE, 0, 0));
		assertFalse(ManipulationCastingRules.earnsMasteryUse(0.3F));
		assertTrue(ManipulationCastingRules.earnsMasteryUse(0.5F));
		assertEquals(1, ManipulationCastingRules.scaledSkillPoints(4, 0.3F));
		assertEquals(4, ManipulationCastingRules.scaledSkillPoints(4, 1.0F));
	}

	@Test
	void reportedChargeIsClampedToTheObservedHold() {
		assertEquals(0.0F, ManipulationCastingRules.clampReportedCharge(60, -1, 100), "no observed key-down");
		assertEquals(2.0F, ManipulationCastingRules.clampReportedCharge(60, 100, 100), "elapsed 0 keeps the 2-tick floor");
		assertEquals(12.0F, ManipulationCastingRules.clampReportedCharge(60, 90, 100), "elapsed 10 + max(2, ceil 1.5)");
		assertEquals(88.0F, ManipulationCastingRules.clampReportedCharge(120, 24, 100), "elapsed 76 + ceil(11.4)");
		assertEquals(80.0F, ManipulationCastingRules.clampReportedCharge(80, 24, 100),
				"a 19 TPS server still credits an honest 80-tick hold");
		assertEquals(30.0F, ManipulationCastingRules.clampReportedCharge(30, 0, 100), "honest clients keep their charge");
		assertEquals(0.0F, ManipulationCastingRules.clampReportedCharge(Float.NaN, 0, 100));
	}

	@Test
	void staleKeyDownIsTreatedAsNoKeyDown() {
		// A 30-tick spell keeps its key-down for 2 × 30 + 20 = 80 ticks.
		assertFalse(ManipulationCastingRules.chargeStartStale(20, 100, 30), "an 80-tick hold is still live");
		assertTrue(ManipulationCastingRules.chargeStartStale(19, 100, 30), "an 81-tick-old key-down is stale");
		assertFalse(ManipulationCastingRules.chargeStartStale(-1, 100, 30), "no key-down is not a stale one");
		assertEquals(30.0F, ManipulationCastingRules.clampReportedCharge(30, 20, 100, 30),
				"holding through the end of a short cooldown keeps the full charge");
		assertEquals(0.0F, ManipulationCastingRules.clampReportedCharge(30, 0, 1000, 30),
				"a key-down left over from a refused cast credits nothing");
		assertEquals(12.0F, ManipulationCastingRules.clampReportedCharge(60, 90, 100, 30),
				"a live key-down still clamps to the observed hold");
	}
}
