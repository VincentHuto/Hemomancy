package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core-loop round two P6: the brood's cost, its pace, its pauses, what the young inherits and how a press is judged. */
class BroodRulesTest {
	@Test
	void aYoungCostsThirtyMillilitresAHealthPointAndNeverUnderTheFloor() {
		assertEquals(300.0D, BroodRules.broodCost(10.0F), 1.0E-9D, "a cow");
		assertEquals(600.0D, BroodRules.broodCost(20.0F), 1.0E-9D, "a blaze");
		assertEquals(150.0D, BroodRules.broodCost(2.0F), 1.0E-9D, "a small creature pays the floor");
		assertEquals(150.0D, BroodRules.broodCost(-4.0F), 1.0E-9D, "nonsense health pays the floor");
	}

	@Test
	void theCostIsPaidInOneHundredTwentyFedSeconds() {
		double cost = BroodRules.broodCost(10.0F);
		assertEquals(2.5D, BroodRules.perSecond(cost), 1.0E-9D);
		double paid = 0.0D;
		int seconds = 0;
		while (!BroodRules.atTerm(paid, cost)) {
			paid += BroodRules.share(paid, cost);
			seconds++;
		}
		assertEquals(BroodRules.BROOD_SECONDS, seconds);
		assertEquals(cost, paid, 1.0E-6D, "the shares add up to exactly the cost");
		assertEquals(0.0D, BroodRules.share(paid, cost), 1.0E-9D, "nothing more is taken at term");
		assertEquals(120, BroodRules.paidSeconds(paid, cost));
	}

	@Test
	void bothParentsMustBeAboveTheMark() {
		assertTrue(BroodRules.gestates(75.0D, 90.0D));
		assertFalse(BroodRules.gestates(74.9D, 100.0D), "the bearer under the mark pauses it");
		assertFalse(BroodRules.gestates(100.0D, 60.0D), "the mate under the mark pauses it");
	}

	@Test
	void parentsPayVigorEveryTenthPaidSecond() {
		assertFalse(BroodRules.drainsVigor(0));
		assertFalse(BroodRules.drainsVigor(9));
		assertTrue(BroodRules.drainsVigor(10));
		assertTrue(BroodRules.drainsVigor(120));
	}

	@Test
	void progressIsBounded() {
		assertEquals(0.0D, BroodRules.progress(-5.0D, 300.0D), 1.0E-9D);
		assertEquals(0.5D, BroodRules.progress(150.0D, 300.0D), 1.0E-9D);
		assertEquals(1.0D, BroodRules.progress(900.0D, 300.0D), 1.0E-9D);
	}

	@Test
	void theYoungInheritsASharedSchoolAlwaysAndALoneOneHalfTheTime() {
		assertEquals(EnumBloodTemper.LUX, BroodRules.inherits(EnumBloodTemper.LUX, EnumBloodTemper.LUX, 0.99D));
		assertEquals(EnumBloodTemper.LUX, BroodRules.inherits(EnumBloodTemper.LUX, null, 0.2D));
		assertNull(BroodRules.inherits(EnumBloodTemper.LUX, null, 0.7D));
		assertEquals(EnumBloodTemper.MORTEM, BroodRules.inherits(null, EnumBloodTemper.MORTEM, 0.49D));
		assertNull(BroodRules.inherits(null, EnumBloodTemper.MORTEM, 0.5D));
		assertEquals(EnumBloodTemper.LUX, BroodRules.inherits(EnumBloodTemper.LUX, EnumBloodTemper.FERRIC, 0.1D));
		assertEquals(EnumBloodTemper.FERRIC, BroodRules.inherits(EnumBloodTemper.LUX, EnumBloodTemper.FERRIC, 0.9D));
		assertNull(BroodRules.inherits(null, null, 0.0D));
	}

	@Test
	void stateFollowsPairingPaymentFeedingAndRest() {
		assertEquals(BroodRules.State.NONE, BroodRules.state(false, 0.0D, 300.0D, true, 100L, Long.MIN_VALUE));
		assertEquals(BroodRules.State.RESTING, BroodRules.state(false, 0.0D, 300.0D, true, 100L, 6100L));
		assertEquals(BroodRules.State.NONE, BroodRules.state(false, 0.0D, 300.0D, true, 6100L, 6100L), "rest ends");
		assertEquals(BroodRules.State.GESTATING, BroodRules.state(true, 10.0D, 300.0D, true, 100L, Long.MIN_VALUE));
		assertEquals(BroodRules.State.PAUSED, BroodRules.state(true, 10.0D, 300.0D, false, 100L, Long.MIN_VALUE));
		assertEquals(BroodRules.State.AT_TERM, BroodRules.state(true, 300.0D, 300.0D, false, 100L, Long.MIN_VALUE),
				"at term it waits whatever its Vigor");
	}

	@Test
	void aPressIsJudgedOnBothSides() {
		assertEquals(BroodRules.Refusal.PRESERVED, BroodRules.ownSide(false, true, true, false, false, false));
		assertEquals(BroodRules.Refusal.NOT_YOURS, BroodRules.ownSide(true, false, true, false, false, false));
		assertEquals(BroodRules.Refusal.HUSK, BroodRules.ownSide(true, true, false, false, false, false));
		assertEquals(BroodRules.Refusal.UNDEAD, BroodRules.ownSide(true, true, true, true, false, false));
		assertEquals(BroodRules.Refusal.CANNOT_BREED, BroodRules.ownSide(true, true, true, false, true, false));
		assertEquals(BroodRules.Refusal.RESTING, BroodRules.ownSide(true, true, true, false, false, true));
		assertEquals(BroodRules.Refusal.NONE, BroodRules.ownSide(true, true, true, false, false, false));
		assertEquals(BroodRules.Refusal.OTHER_KEEPER, BroodRules.mateSide(false, true, false, false));
		assertEquals(BroodRules.Refusal.MATE_HUSK, BroodRules.mateSide(true, false, false, false));
		assertEquals(BroodRules.Refusal.MATE_PAIRED, BroodRules.mateSide(true, true, true, false));
		assertEquals(BroodRules.Refusal.MATE_RESTING, BroodRules.mateSide(true, true, false, true));
		assertEquals(BroodRules.Refusal.NONE, BroodRules.mateSide(true, true, false, false));
		assertEquals(BroodRules.Refusal.NONE, BroodRules.Refusal.byOrdinal(-1));
		assertEquals(BroodRules.Refusal.NO_MATE, BroodRules.Refusal.byOrdinal(BroodRules.Refusal.NO_MATE.ordinal()));
	}

	@Test
	void everyShownValueFitsAShort() {
		assertTrue(BroodRules.MAX_SHOWN_COST <= Short.MAX_VALUE);
		assertTrue(VatVigorRules.MAX * 100.0D <= Short.MAX_VALUE, "Vigor in hundredths");
	}
}
