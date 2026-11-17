package com.vincenthuto.hemomancy.common.tile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Living blood (core-loop Track 3, CL-5): five minutes fresh, then a tenth of what is left each minute, gone in an hour. */
class CoagulationRulesTest {
	@Test
	void bloodStaysWholeThroughTheFreshWindow() {
		assertEquals(1.0D, CoagulationRules.freshFraction(0L));
		assertEquals(1.0D, CoagulationRules.freshFraction(CoagulationRules.FRESH_WINDOW_TICKS));
		assertEquals(6_000L, CoagulationRules.FRESH_WINDOW_TICKS);
	}

	@Test
	void aTenthOfWhatIsLeftClotsEachMinutePastTheWindow() {
		long window = CoagulationRules.FRESH_WINDOW_TICKS;
		assertEquals(0.9D, CoagulationRules.freshFraction(window + 1_200L), 1e-9);
		assertEquals(0.81D, CoagulationRules.freshFraction(window + 2_400L), 1e-9);
		// Thirty minutes away leaves a full tank mostly clot.
		assertTrue(CoagulationRules.freshFraction(36_000L) < 0.1D);
	}

	@Test
	void anHourAfterFeedingNothingIsFresh() {
		assertEquals(0.0D, CoagulationRules.freshFraction(CoagulationRules.FULL_CLOT_TICKS));
		assertEquals(0.0D, CoagulationRules.freshFraction(1_000_000L));
		assertEquals(5_000.0D, CoagulationRules.clotBetween(5_000.0D, 0L, 72_000L), 1e-9);
	}

	@Test
	void settlingInStepsClotsExactlyAsMuchAsSettlingOnce() {
		double once = CoagulationRules.clotBetween(1_000.0D, 0L, 30_000L);
		double fresh = 1_000.0D;
		double clot = 0.0D;
		for (long t = 0L; t < 30_000L; t += 777L) {
			double moved = CoagulationRules.clotBetween(fresh, t, Math.min(30_000L, t + 777L));
			fresh -= moved;
			clot += moved;
		}
		assertEquals(once, clot, 1e-6);
	}

	@Test
	void freshnessReadsAsColourNotACountdown() {
		assertEquals(1.0D, CoagulationRules.freshness(0L));
		assertEquals(0.5D, CoagulationRules.freshness(3_000L), 1e-9);
		assertEquals(0.0D, CoagulationRules.freshness(6_000L));
		assertEquals(0.0D, CoagulationRules.freshness(60_000L));
	}

	@Test
	void neglectedClotYieldsFarWorseThanTheCondenser() {
		assertEquals(0, CoagulationRules.neglectFormations(399.0D));
		assertEquals(1, CoagulationRules.neglectFormations(400.0D));
		assertEquals(6, CoagulationRules.neglectFormations(2_500.0D));
		for (int stage = 0; stage <= 2; stage++)
			assertTrue(CoagulationRules.condenserMlPerFormation(stage) < CoagulationRules.NEGLECT_ML_PER_FORMATION);
	}

	@Test
	void theCondenserYieldTableLivesHere() {
		assertEquals(150.0D, CoagulationRules.condenserMlPerFormation(0));
		assertEquals(125.0D, CoagulationRules.condenserMlPerFormation(1));
		assertEquals(100.0D, CoagulationRules.condenserMlPerFormation(2));
		assertEquals(0.25D, CoagulationRules.collapseChance(0, 0), 1e-9);
		assertEquals(0.0D, CoagulationRules.collapseChance(1, 0));
		assertEquals(0.25D, CoagulationRules.bonusChance(2));
	}

	@Test
	void aBottledStampIsRoundedDownToTheMinute() {
		assertEquals(0L, CoagulationRules.bottledStamp(1_199L));
		assertEquals(1_200L, CoagulationRules.bottledStamp(1_200L));
		assertEquals(24_000L, CoagulationRules.bottledStamp(25_150L));
	}

	@Test
	void distillingAVesselGivesByItsFreshShare() {
		assertEquals(2, CoagulationRules.freshCount(2, 1.0D));
		assertEquals(1, CoagulationRules.freshCount(2, 0.5D));
		assertEquals(0, CoagulationRules.freshCount(2, 0.2D));
		assertEquals(0, CoagulationRules.freshCount(2, 0.0D));
	}

	@Test
	void halfOfAPourWakesClotAtTwoForOne() {
		// The owner's example: 5,000 fresh, 1,000 clot, 2,000 room. Pour 1,000.
		CoagulationRules.Pour pour = CoagulationRules.pour(1_000.0D, 2_000.0D, 1_000.0D, 0.5D, 2.0D);
		assertEquals(500.0D, pour.spent(), 1e-9);
		assertEquals(1_000.0D, pour.woken(), 1e-9);
		assertEquals(500.0D, pour.fresh(), 1e-9);
		assertEquals(1_000.0D, pour.taken(), 1e-9);
	}

	@Test
	void aTankWithNoRoomSpendsTheWholePourOnItsClot() {
		CoagulationRules.Pour pour = CoagulationRules.pour(100.0D, 0.0D, 1_000.0D, 0.5D, 2.0D);
		assertEquals(100.0D, pour.spent(), 1e-9);
		assertEquals(200.0D, pour.woken(), 1e-9);
		assertEquals(0.0D, pour.fresh(), 1e-9);
	}

	@Test
	void onceTheClotIsGoneTheRestOfThePourIsFresh() {
		CoagulationRules.Pour pour = CoagulationRules.pour(1_000.0D, 5_000.0D, 100.0D, 0.5D, 2.0D);
		assertEquals(50.0D, pour.spent(), 1e-9);
		assertEquals(100.0D, pour.woken(), 1e-9);
		assertEquals(950.0D, pour.fresh(), 1e-9);
		CoagulationRules.Pour clean = CoagulationRules.pour(300.0D, 5_000.0D, 0.0D, 0.5D, 2.0D);
		assertEquals(0.0D, clean.spent());
		assertEquals(300.0D, clean.fresh(), 1e-9);
	}

	@Test
	void aPourTakesOnlyWhatTheTankCanUse() {
		// 10 mL of room and 40 mL of clot (20 mL to wake it): a 500 mL pour takes 30.
		CoagulationRules.Pour pour = CoagulationRules.pour(500.0D, 10.0D, 40.0D, 0.5D, 2.0D);
		assertEquals(30.0D, pour.taken(), 1e-9);
		assertEquals(40.0D, pour.woken(), 1e-9);
		// Offering exactly what it takes is taken in full.
		assertEquals(30.0D, CoagulationRules.pour(30.0D, 10.0D, 40.0D, 0.5D, 2.0D).taken(), 1e-9);
	}

	@Test
	void theSutureWakesFourForOne() {
		CoagulationRules.Pour pour = CoagulationRules.pour(60.0D, 0.0D, 60.0D, 0.5D, 4.0D);
		assertEquals(15.0D, pour.taken(), 1e-9);
		assertEquals(60.0D, pour.woken(), 1e-9);
	}

	@Test
	void theReviveValuesDefaultToTheOwnersAnswers() {
		assertEquals(0.5D, CoagulationRules.reviveShare());
		assertEquals(2.0D, CoagulationRules.reviveYield());
		assertEquals(4.0D, CoagulationRules.suturedReviveYield());
	}

	@Test
	void agesBlendByVolume() {
		assertEquals(6_000L, CoagulationRules.blendedAge(8_000.0D, 6_000L, 1.0D, 0L), 1L);
		assertEquals(3_000L, CoagulationRules.blendedAge(500.0D, 6_000L, 500.0D, 0L));
		assertEquals(0L, CoagulationRules.blendedAge(0.0D, 9_999L, 100.0D, 0L));
		assertEquals(0L, CoagulationRules.blendedAge(0.0D, 0L, 0.0D, 0L));
	}
}
