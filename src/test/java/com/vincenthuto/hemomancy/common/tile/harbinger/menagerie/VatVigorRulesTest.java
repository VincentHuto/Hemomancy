package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core-loop Track 5: a kept creature's Vigor, what feeding costs and what a draw gives. */
class VatVigorRulesTest {
	@Test
	void vigorFallsAPointAMinuteAndStopsAtZero() {
		assertEquals(99.0D, VatVigorRules.settle(100.0D, 0L, 1200L, Long.MIN_VALUE), 1.0E-9D);
		assertEquals(50.0D, VatVigorRules.settle(100.0D, 0L, 60_000L, Long.MIN_VALUE), 1.0E-9D);
		assertEquals(0.0D, VatVigorRules.settle(100.0D, 0L, 10_000_000L, Long.MIN_VALUE), 1.0E-9D);
	}

	@Test
	void settlingInManyStepsMatchesOneStep() {
		double stepped = 100.0D;
		long at = 0L;
		for (long now = 37L; now <= 50_000L; now += 37L) {
			stepped = VatVigorRules.settle(stepped, at, now, Long.MIN_VALUE);
			at = now;
		}
		assertEquals(VatVigorRules.settle(100.0D, 0L, at, Long.MIN_VALUE), stepped, 1.0E-6D);
	}

	@Test
	void tendedTimeDoesNotCount() {
		assertEquals(100.0D, VatVigorRules.settle(100.0D, 0L, 6000L, 6000L), 1.0E-9D, "tended the whole time");
		assertEquals(97.5D, VatVigorRules.settle(100.0D, 0L, 6000L, 3000L), 1.0E-9D, "tended for the first half");
		assertEquals(95.0D, VatVigorRules.settle(100.0D, 1000L, 7000L, 500L), 1.0E-9D, "tending ended before");
	}

	@Test
	void aSyringeDrawCostsLessVigorThanAVial() {
		assertEquals(5.0D, VatVigorRules.SAMPLE_COST, 1.0E-9D);
		assertEquals(3.0D, VatVigorRules.SYRINGE_SAMPLE_COST, 1.0E-9D);
		assertTrue(VatVigorRules.SYRINGE_SAMPLE_COST < VatVigorRules.SAMPLE_COST, "the syringe is the gentler draw");
		assertEquals("thriving", VatVigorRules.word(80.0D));
		assertEquals("wan", VatVigorRules.word(30.0D));
		assertEquals("failing", VatVigorRules.word(10.0D));
	}

	@Test
	void aBigBodyCostsMoreToFeed() {
		assertEquals(10.0D, VatVigorRules.mlPerVigor(20.0F), 1.0E-9D, "a blaze");
		assertEquals(5.0D, VatVigorRules.mlPerVigor(10.0F), 1.0E-9D, "a cow");
		assertEquals(2.0D, VatVigorRules.mlPerVigor(1.0F), 1.0E-9D, "never under two");
	}

	@Test
	void aDrawGivesMoreAtHigherVigorAndIsCutByAcclimation() {
		assertEquals(6.0D, VatVigorRules.temperPerDraw(100.0D, 1.0D), 1.0E-9D);
		assertEquals(1.5D, VatVigorRules.temperPerDraw(0.0D, 1.0D), 1.0E-9D);
		assertEquals(3.0D, VatVigorRules.temperPerDraw(100.0D, 0.5D), 1.0E-9D);
		assertEquals(2.0D, VatVigorRules.bloodPerDraw(100.0D, 1.0D), 1.0E-9D);
		assertEquals(0.0D, VatVigorRules.temperPerDraw(100.0D, 0.0D), 1.0E-9D, "fully acclimated gives nothing");
	}

	@Test
	void aVatTurnsBloodIntoTemperAtALoss() {
		// A blaze at full Vigor: each draw costs 2 Vigor, 20 mL of the keeper's blood, and gives at most 6 Temper and 2 mL.
		double fed = VatVigorRules.DRAW_STEP * VatVigorRules.mlPerVigor(20.0F);
		double given = VatVigorRules.temperPerDraw(100.0D, 1.0D) + VatVigorRules.bloodPerDraw(100.0D, 1.0D);
		assertTrue(given < fed, "a draw gives " + given + " for " + fed + " mL fed");
	}

	@Test
	void drawsStopAtTheFloorAndHostilesRattleAndBreakOut() {
		assertTrue(VatVigorRules.drawable(10.5D));
		assertFalse(VatVigorRules.drawable(10.0D));
		assertTrue(VatVigorRules.rattles(20.0D, true));
		assertFalse(VatVigorRules.rattles(20.0D, false), "a passive creature does not rattle");
		assertFalse(VatVigorRules.rattles(30.0D, true));
		assertTrue(VatVigorRules.mayBreakOut(5.0D, true, 20.0F));
		assertFalse(VatVigorRules.mayBreakOut(5.0D, true, 10.0F), "a weak creature cannot force the glass");
		assertFalse(VatVigorRules.mayBreakOut(15.0D, true, 20.0F));
	}

	@Test
	void researchBandsSplitVigorInThree() {
		assertEquals(0, VatVigorRules.band(10.0D));
		assertEquals(1, VatVigorRules.band(50.0D));
		assertEquals(2, VatVigorRules.band(90.0D));
	}
}
