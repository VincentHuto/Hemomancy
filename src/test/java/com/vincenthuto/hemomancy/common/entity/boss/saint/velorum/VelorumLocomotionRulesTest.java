package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumLocomotionRulesTest {
	@Test
	void aGlideThatStopsInTheEndlessHour() {
		assertEquals(0.10D, VelorumLocomotionRules.glide(1, 1.0D), 1.0E-9D);
		assertEquals(0.12D, VelorumLocomotionRules.glide(2, 1.0D), 1.0E-9D);
		assertEquals(0.0D, VelorumLocomotionRules.glide(3, 1.15D), 1.0E-9D);
		assertEquals(0.115D, VelorumLocomotionRules.glide(1, 1.15D), 1.0E-9D);
	}

	@Test
	void turningIsAllHeHasInPhaseThreeAndAGripHalvesIt() {
		assertEquals(5.0F, VelorumLocomotionRules.turn(1, 1.0D, false), 1.0E-6F);
		assertEquals(6.0F, VelorumLocomotionRules.turn(2, 1.0D, false), 1.0E-6F);
		assertEquals(8.0F, VelorumLocomotionRules.turn(3, 1.0D, false), 1.0E-6F);
		assertEquals(4.0F, VelorumLocomotionRules.turn(3, 1.0D, true), 1.0E-6F);
	}

	@Test
	void phaseTwoClosesByTheShadowStepAndThePhaseThreeCardStepsToTheRing() {
		assertTrue(VelorumLocomotionRules.closesByGlide(1));
		assertFalse(VelorumLocomotionRules.closesByGlide(2));
		assertFalse(VelorumLocomotionRules.closesByGlide(3));
		assertFalse(VelorumLocomotionRules.underInCard(13));
		assertTrue(VelorumLocomotionRules.underInCard(VelorumLocomotionRules.CARD_MOVE));
		assertFalse(VelorumLocomotionRules.underInCard(VelorumLocomotionRules.CARD_SURFACED + 1));
		assertTrue(VelorumLocomotionRules.CARD_SURFACED < 50, "he is up before the card ends");
	}

	@Test
	void theStepRisesFourToSixFromTheTargetBesideOrBehindThemInsideTheLeash() {
		// the target at the origin, Velorum 8 to the west, the leash box -8..8
		double tx = 0, tz = 0, sx = -8, sz = 0;
		assertTrue(VelorumLocomotionRules.stepCell(5, 0, tx, tz, sx, sz, -8, -8, 8, 8, 1.0D, true), "behind them");
		assertTrue(VelorumLocomotionRules.stepCell(0, 5, tx, tz, sx, sz, -8, -8, 8, 8, 1.0D, true), "beside them");
		assertFalse(VelorumLocomotionRules.stepCell(-5, 0, tx, tz, sx, sz, -8, -8, 8, 8, 1.0D, true), "never between");
		assertFalse(VelorumLocomotionRules.stepCell(3, 0, tx, tz, sx, sz, -8, -8, 8, 8, 1.0D, true), "too close");
		assertFalse(VelorumLocomotionRules.stepCell(7, 0, tx, tz, sx, sz, -8, -8, 8, 8, 1.0D, true), "too far");
		assertFalse(VelorumLocomotionRules.stepCell(5, 0, tx, tz, sx, sz, -8, -8, 4, 8, 1.0D, true), "outside the leash");
		assertFalse(VelorumLocomotionRules.stepCell(5, 0, tx, tz, sx, sz, -8, -8, 8, 8, 2.0D, true), "away from a figure");
		assertTrue(VelorumLocomotionRules.stepCell(5, 0, tx, tz, sx, sz, -8, -8, 8, 8, 2.0D, false), "the fallback");
	}

	@Test
	void decisionDelaysAndTheHold() {
		assertEquals(24, VelorumLocomotionRules.decisionDelay(1, false));
		assertEquals(18, VelorumLocomotionRules.decisionDelay(2, false));
		assertEquals(12, VelorumLocomotionRules.decisionDelay(3, false));
		assertEquals(4, VelorumLocomotionRules.decisionDelay(3, true));
		assertEquals(0, VelorumLocomotionRules.approach(8.0D, 200, 0.0D), "he holds MID");
		assertEquals(0, VelorumLocomotionRules.approach(14.0D, 30, 0.0D), "not until FAR for 60 ticks");
		assertEquals(1, VelorumLocomotionRules.approach(14.0D, 60, 0.0D));
		assertEquals(0, VelorumLocomotionRules.approach(14.0D, 60, 0.3D), "never a chase after a sprint");
	}
}
