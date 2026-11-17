package com.vincenthuto.hemomancy.common.world.chum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 7: chum's pure rules. The radius grows with the blood from 3 to 12; a pool fades by half each
 * minute and four times faster in moving water; the lure is pulled harder in a stronger pool but never below a fish's
 * approach; a catch is likelier in a strong pool and far likelier in home waters; only a strong pool in deep water
 * draws a creature.
 */
class ChumRulesTest {
	@Test
	void theRadiusGrowsWithTheBloodFromThreeToTwelve() {
		assertEquals(0.0D, ChumRules.radius(0.0D));
		assertEquals(3.45D, ChumRules.radius(1.0D), 1.0E-6D);
		assertEquals(7.5D, ChumRules.radius(100.0D), 1.0E-6D);
		assertEquals(12.0D, ChumRules.radius(400.0D), 1.0E-6D);
		assertEquals(12.0D, ChumRules.radius(800.0D), "capped");
		assertTrue(ChumRules.radius(50.0D) < ChumRules.radius(60.0D));
	}

	@Test
	void aPoolHoldsAtMostItsCapAndStrengthIsAShareOfFull() {
		assertEquals(ChumRules.MAX_ML, ChumRules.pour(790.0D, 40.0D));
		assertEquals(0.5D, ChumRules.strength(200.0D));
		assertEquals(1.0D, ChumRules.strength(800.0D));
	}

	@Test
	void aPoolFadesByHalfEveryHalfLifeAndFasterInMovingWater() {
		assertEquals(200.0D, ChumRules.fade(400.0D, ChumRules.HALF_LIFE_TICKS, false), 1.0E-6D);
		assertEquals(25.0D, ChumRules.fade(400.0D, ChumRules.HALF_LIFE_TICKS, true), 1.0E-6D);
		assertEquals(0.0D, ChumRules.fade(400.0D, ChumRules.HALF_LIFE_TICKS * 9, false), "gone below 1 mL");
		assertTrue(ChumRules.fade(ChumRules.ML_PER_TICK, 1L, false) > 0.0D, "one tick's pour lasts to the next tick");
		assertEquals(400.0D, ChumRules.fade(400.0D, 0L, true));
		double minutes = Math.log(ChumRules.FULL_ML / ChumRules.GONE_ML) / Math.log(2.0D);
		assertTrue(minutes * ChumRules.HALF_LIFE_TICKS / 1_200.0D < 7.0D, "a full pool is gone in a few minutes of still water");
	}

	@Test
	void theLureIsPulledByStrengthButAFishStillApproaches() {
		assertEquals(1, ChumRules.extraLurePerTick(0.0D));
		assertEquals(5, ChumRules.extraLurePerTick(1.0D));
		assertEquals(295, ChumRules.adjustedLureTime(300, 1.0D));
		assertEquals(20, ChumRules.adjustedLureTime(22, 1.0D), "never below the approach");
		assertEquals(15, ChumRules.adjustedLureTime(15, 1.0D));
		assertEquals(300, ChumRules.adjustedLureTime(300, 0.0D), "outside a pool nothing changes");
	}

	@Test
	void aCatchIsLikelierInAStrongPoolAndInHomeWaters() {
		assertEquals(0.0D, ChumRules.catchChance(0.0D, true));
		assertEquals(0.04D + 0.21D * 0.5D, ChumRules.catchChance(0.5D, false), 1.0E-6D);
		assertEquals(0.25D, ChumRules.catchChance(1.0D, false), 1.0E-6D);
		assertEquals(0.75D, ChumRules.catchChance(1.0D, true), 1.0E-6D);
		assertTrue(ChumRules.catchChance(0.2D, true) > ChumRules.catchChance(0.2D, false) * 2.9D);
	}

	@Test
	void onlyAStrongPoolInDeepWaterDraws() {
		assertTrue(ChumRules.mayDraw(0.5D, 3));
		assertFalse(ChumRules.mayDraw(0.49D, 6));
		assertFalse(ChumRules.mayDraw(1.0D, 2));
	}

	@Test
	void pouringMovesTheCentreByTheBlood() {
		assertEquals(5.0D, ChumRules.blend(0.0D, 0.0D, 5.0D, 2.0D), "a new pool starts where it is poured");
		assertEquals(1.0D, ChumRules.blend(0.0D, 90.0D, 10.0D, 10.0D), 1.0E-6D);
	}
}
