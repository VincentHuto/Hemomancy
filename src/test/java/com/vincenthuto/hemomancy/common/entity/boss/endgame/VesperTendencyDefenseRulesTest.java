package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperTendencyDefenseRulesTest {
	@Test
	void matchingTendencyIsResisted() {
		assertEquals(0.5F, VesperTendencyDefenseRules.damageMultiplier(
				EnumBloodTendency.ANIMUS, EnumBloodTendency.ANIMUS, 1.75F), 0.001F);
	}

	@Test
	void opposingTendencyUsesTheAttackersAffinityBonus() {
		assertEquals(1.75F, VesperTendencyDefenseRules.damageMultiplier(
				EnumBloodTendency.ANIMUS, EnumBloodTendency.MORTEM, 1.75F), 0.001F);
	}

	@Test
	void adjacentTendenciesRemainNeutral() {
		assertEquals(1.0F, VesperTendencyDefenseRules.damageMultiplier(
				EnumBloodTendency.ANIMUS, EnumBloodTendency.LUX, 1.75F), 0.001F);
	}

	@Test
	void everyOppositionPairWorksInBothDirections() {
		assertOpposed(EnumBloodTendency.ANIMUS, EnumBloodTendency.MORTEM);
		assertOpposed(EnumBloodTendency.DUCTILIS, EnumBloodTendency.FERRIC);
		assertOpposed(EnumBloodTendency.LUX, EnumBloodTendency.TENEBRIS);
		assertOpposed(EnumBloodTendency.FLAMMEUS, EnumBloodTendency.CONGEATIO);
	}

	@Test
	void mixedManipulationsComposePrimaryAndSecondaryMatchups() {
		float matchingPrimary = VesperTendencyDefenseRules.damageMultiplier(
				EnumBloodTendency.ANIMUS, EnumBloodTendency.ANIMUS, 1.75F);
		float opposingSecondary = VesperTendencyDefenseRules.damageMultiplier(
				EnumBloodTendency.ANIMUS, EnumBloodTendency.MORTEM, 1.75F);

		assertEquals(0.8125F,
				TendencyAffinityRules.composeDamageMultiplier(matchingPrimary, opposingSecondary), 0.001F);
	}

	private static void assertOpposed(EnumBloodTendency first, EnumBloodTendency second) {
		assertEquals(2.0F, VesperTendencyDefenseRules.damageMultiplier(first, second, 2.0F), 0.001F);
		assertEquals(2.0F, VesperTendencyDefenseRules.damageMultiplier(second, first, 2.0F), 0.001F);
	}
}
