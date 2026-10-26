package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantBroodRulesTest {
	@Test
	void sheNursesOnlyAnUngrownFunglingUnderTheCap() {
		assertFalse(MycophantBroodRules.mayNurse(3, 2));
		assertTrue(MycophantBroodRules.mayNurse(1, 0));
		assertTrue(MycophantBroodRules.mayNurse(2, 1));
		assertFalse(MycophantBroodRules.mayNurse(0, 0));
		// every living fungling is already grown
		assertFalse(MycophantBroodRules.mayNurse(1, 1));
	}

	@Test
	void tumorsOnlyInOwnedFights() {
		assertFalse(MycophantBroodRules.mayPlantTumor(false, 0));
		assertFalse(MycophantBroodRules.mayPlantTumor(true, 6));
		assertTrue(MycophantBroodRules.mayPlantTumor(true, 5));
		assertTrue(MycophantBroodRules.mayPlantTumor(true, 0));
	}

	@Test
	void aTumorHatchesAfterTwentySeconds() {
		assertFalse(MycophantBroodRules.hatchDue(100, 499));
		assertTrue(MycophantBroodRules.hatchDue(100, 500));
		assertTrue(MycophantBroodRules.hatchDue(100, 900));
	}

	@Test
	void aGrownDeathMakesHerGrieveLonger() {
		assertEquals(50, MycophantBroodRules.griefTicks(true));
		assertEquals(30, MycophantBroodRules.griefTicks(false));
		assertEquals(MycophantControllerRules.griefTicks(), MycophantBroodRules.griefTicks(false));
	}

	@Test
	void constants() {
		assertEquals(14.0, MycophantBroodRules.NURSE_RANGE);
		assertEquals(80, MycophantBroodRules.NURSE_TICKS);
		assertEquals(8.0F, MycophantBroodRules.NURSE_BREAK_DAMAGE);
		assertEquals(2, MycophantBroodRules.GROWN_CAP);
		assertEquals(1.8F, MycophantBroodRules.GROWN_SCALE);
		assertEquals(30.0, MycophantBroodRules.GROWN_HEALTH);
		assertEquals(8.0, MycophantBroodRules.GROWN_BITE);
		assertEquals(6, MycophantBroodRules.TUMOR_CAP);
		assertEquals(400, MycophantBroodRules.TUMOR_HATCH_TICKS);
		assertEquals(4, MycophantBroodRules.BROOD_CAP);
	}
}
