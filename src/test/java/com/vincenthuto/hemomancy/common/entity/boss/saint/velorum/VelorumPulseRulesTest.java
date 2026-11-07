package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumPulseRules.Beat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumPulseRulesTest {
	@Test
	void itBeatsAtThirtySixtyAndNinetyEachToldTwelveTicksBefore() {
		assertEquals(0, VelorumPulseRules.beatAt(30));
		assertEquals(1, VelorumPulseRules.beatAt(60));
		assertEquals(2, VelorumPulseRules.beatAt(90));
		assertEquals(-1, VelorumPulseRules.beatAt(45));
		assertEquals(0, VelorumPulseRules.tellAt(18));
		assertEquals(2, VelorumPulseRules.tellAt(78));
		assertTrue(VelorumPulseRules.telling(18) && VelorumPulseRules.telling(29));
		assertFalse(VelorumPulseRules.telling(30) || VelorumPulseRules.telling(17));
		assertEquals(Beat.TELL, VelorumPulseRules.beat(48, 0.0D, 0.0F, false));
		assertEquals(Beat.NONE, VelorumPulseRules.beat(40, 0.0D, 0.0F, false));
		assertFalse(VelorumPulseRules.spent(89));
		assertTrue(VelorumPulseRules.spent(90));
	}

	@Test
	void walkingUnderSeventyShedsTheFrostAndStandingBursts() {
		assertEquals(Beat.PASS, VelorumPulseRules.beat(30, 0.2D, 50.0F, false));
		assertEquals(Beat.PASS, VelorumPulseRules.beat(60, 0.13D, 69.9F, false));
		assertEquals(Beat.BURST, VelorumPulseRules.beat(30, 0.0D, 10.0F, false), "standing still");
		assertEquals(Beat.BURST, VelorumPulseRules.beat(30, 0.065D, 10.0F, false), "a sneak is not walking pace");
		assertEquals(Beat.BURST, VelorumPulseRules.beat(90, 0.25D, 70.0F, false), "at 70 the frost holds whatever the pace");
		// inside an opening every beat passes
		assertEquals(Beat.PASS, VelorumPulseRules.beat(30, 0.0D, 95.0F, true));
	}

	@Test
	void theBurstsNumbersAndItsDisc() {
		assertEquals(5.0F, VelorumPulseRules.BURST_DAMAGE);
		assertEquals(15.0F, VelorumPulseRules.BURST_STILLNESS);
		assertEquals(20, VelorumPulseRules.BURST_SLOW);
		assertEquals(1.5D, VelorumPulseRules.SPLASH);
		assertEquals(5.0F, VelorumPulseRules.SPLASH_DAMAGE);
		assertEquals(10.0F, VelorumPulseRules.SPLASH_STILLNESS);
	}

	@Test
	void thePaceIsTheMeanOfTheLastTenTicks() {
		double[] travel = {0.2, 0.2, 0.2, 0.2, 0.2, 0.0, 0.0, 0.0, 0.0, 0.0};
		assertEquals(0.1D, VelorumPulseRules.averagePace(travel, 10), 1.0E-9D, "stopped half way: not walking");
		assertEquals(0.2D, VelorumPulseRules.averagePace(travel, 5), 1.0E-9D, "only what has been recorded");
		assertEquals(0.0D, VelorumPulseRules.averagePace(travel, 0));
	}

	@Test
	void searingAWarmingOrAReleaseEndsIt() {
		assertTrue(VelorumPulseRules.ends(true, 0, false));
		assertTrue(VelorumPulseRules.ends(false, VelorumPulseRules.WARM_TICKS, false));
		assertFalse(VelorumPulseRules.ends(false, VelorumPulseRules.WARM_TICKS - 1, false));
		assertTrue(VelorumPulseRules.ends(false, 0, true));
	}

	@Test
	void twoMarksOnlyInPhaseThreeWithTwoChallengersAndAColdTargetDrawsIt() {
		assertEquals(1, VelorumPulseRules.marks(1, 4));
		assertEquals(1, VelorumPulseRules.marks(3, 1));
		assertEquals(2, VelorumPulseRules.marks(3, 2));
		assertEquals(12, VelorumPulseRules.coldPressure(0.4F));
		assertEquals(0, VelorumPulseRules.coldPressure(0.39F));
	}
}
