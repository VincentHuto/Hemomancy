package com.vincenthuto.hemomancy.common.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodLossFormationChanceTest {
	@Test
	void aBleedingMobDropsAFormationTwoTimesInTenThousandTicks() {
		assertEquals(0.0002D, BloodLossEffect.MOB_FORMATION_CHANCE, 0D, "LP-8: 0.02% a tick");
	}
}
