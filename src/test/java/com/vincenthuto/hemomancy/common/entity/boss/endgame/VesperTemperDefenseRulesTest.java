package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.manipulation.TemperAffinityRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperTemperDefenseRulesTest {
	@Test
	void matchingTemperIsResisted() {
		assertEquals(0.5F, VesperTemperDefenseRules.damageMultiplier(
				EnumBloodTemper.ANIMUS, EnumBloodTemper.ANIMUS, 1.75F), 0.001F);
	}

	@Test
	void opposingTemperUsesTheAttackersAffinityBonus() {
		assertEquals(1.75F, VesperTemperDefenseRules.damageMultiplier(
				EnumBloodTemper.ANIMUS, EnumBloodTemper.MORTEM, 1.75F), 0.001F);
	}

	@Test
	void adjacentTempersRemainNeutral() {
		assertEquals(1.0F, VesperTemperDefenseRules.damageMultiplier(
				EnumBloodTemper.ANIMUS, EnumBloodTemper.LUX, 1.75F), 0.001F);
	}

	@Test
	void everyOppositionPairWorksInBothDirections() {
		assertOpposed(EnumBloodTemper.ANIMUS, EnumBloodTemper.MORTEM);
		assertOpposed(EnumBloodTemper.DUCTILIS, EnumBloodTemper.FERRIC);
		assertOpposed(EnumBloodTemper.LUX, EnumBloodTemper.TENEBRIS);
		assertOpposed(EnumBloodTemper.FLAMMEUS, EnumBloodTemper.CONGEATIO);
	}

	@Test
	void mixedManipulationsComposePrimaryAndSecondaryMatchups() {
		float matchingPrimary = VesperTemperDefenseRules.damageMultiplier(
				EnumBloodTemper.ANIMUS, EnumBloodTemper.ANIMUS, 1.75F);
		float opposingSecondary = VesperTemperDefenseRules.damageMultiplier(
				EnumBloodTemper.ANIMUS, EnumBloodTemper.MORTEM, 1.75F);

		assertEquals(0.8125F,
				TemperAffinityRules.composeDamageMultiplier(matchingPrimary, opposingSecondary), 0.001F);
	}

	private static void assertOpposed(EnumBloodTemper first, EnumBloodTemper second) {
		assertEquals(2.0F, VesperTemperDefenseRules.damageMultiplier(first, second, 2.0F), 0.001F);
		assertEquals(2.0F, VesperTemperDefenseRules.damageMultiplier(second, first, 2.0F), 0.001F);
	}
}
