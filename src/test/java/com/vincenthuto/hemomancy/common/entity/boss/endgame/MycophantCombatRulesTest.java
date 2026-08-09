package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MycophantCombatRulesTest {
	@Test
	void phaseTwoBeginsAtHalfHealthAndUsesTheFasterCadences() {
		assertEquals(1, MycophantCombatRules.phase(361.0F, 720.0F));
		assertEquals(2, MycophantCombatRules.phase(360.0F, 720.0F));
		assertEquals(100, MycophantCombatRules.sweepCadenceTicks(1));
		assertEquals(70, MycophantCombatRules.sweepCadenceTicks(2));
		assertEquals(280, MycophantCombatRules.cocoonCadenceTicks(1));
		assertEquals(180, MycophantCombatRules.cocoonCadenceTicks(2));
		assertEquals(360, MycophantCombatRules.surgeCadenceTicks(1));
		assertEquals(240, MycophantCombatRules.surgeCadenceTicks(2));
	}

	@Test
	void phaseTwoHasFourCocoonNodesAndBothEscapeKindsRelievePressure() {
		assertEquals(3, MycophantCombatRules.cocoonNodeCount(1));
		assertEquals(4, MycophantCombatRules.cocoonNodeCount(2));
		assertEquals(55.0F, MycophantCombatRules.pressureAfterEscape(65.0F, false), 0.001F);
		assertEquals(45.0F, MycophantCombatRules.pressureAfterEscape(65.0F, true), 0.001F);
		assertEquals(0.0F, MycophantCombatRules.pressureAfterEscape(8.0F, true), 0.001F);
	}

	@Test
	void pressureRisesByPhaseAndSurgesAndFailuresClampAtOneHundred() {
		assertEquals(10.25F, MycophantCombatRules.pressureAfterSecond(10.0F, 1), 0.001F);
		assertEquals(50.5F, MycophantCombatRules.pressureAfterSecond(10.0F, 2), 0.001F);
		assertEquals(98.0F, MycophantCombatRules.pressureAfterSurge(88.0F), 0.001F);
		assertEquals(100.0F, MycophantCombatRules.pressureAfterFailedCocoon(92.0F), 0.001F);
	}

	@Test
	void pressureThresholdsMapToTheApprovedHazards() {
		assertEquals(MycophantCombatRules.NectarHazard.SHALLOW,
				MycophantCombatRules.nectarHazard(29.99F));
		assertEquals(MycophantCombatRules.NectarHazard.SLOW,
				MycophantCombatRules.nectarHazard(30.0F));
		assertEquals(MycophantCombatRules.NectarHazard.DEEP,
				MycophantCombatRules.nectarHazard(60.0F));
		assertEquals(MycophantCombatRules.NectarHazard.ENGULFING,
				MycophantCombatRules.nectarHazard(85.0F));
	}
}
