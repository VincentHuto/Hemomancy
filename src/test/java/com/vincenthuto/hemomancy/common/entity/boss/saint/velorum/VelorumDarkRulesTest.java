package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDarkRules.Source;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules.Inputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumDarkRulesTest {
	/** A tick's inputs from a short flag string: s sneak, r sprint, i item, p presence, h shelter, d dark. */
	private static Inputs in(double travel, int slow, int rime, String flags) {
		return new Inputs(travel, flags.contains("s"), flags.contains("r"), flags.contains("i"), slow, rime,
				flags.contains("p"), flags.contains("h"), false, false, false, false, false, false, flags.contains("d"));
	}

	@Test
	void eachSourcesPoolGrowsAndLivesByPhase() {
		assertEquals(3.5D, VelorumDarkRules.cap(Source.LONG_NIGHT, 1));
		assertEquals(4.5D, VelorumDarkRules.cap(Source.LONG_NIGHT, 2));
		assertEquals(4.5D, VelorumDarkRules.cap(Source.LONG_NIGHT, 3));
		assertEquals(300, VelorumDarkRules.life(Source.LONG_NIGHT, 1));
		assertEquals(400, VelorumDarkRules.life(Source.LONG_NIGHT, 2));
		assertEquals(500, VelorumDarkRules.life(Source.LONG_NIGHT, 3));
		assertEquals(2.5D, VelorumDarkRules.cap(Source.STEP, 2));
		assertEquals(200, VelorumDarkRules.life(Source.STEP, 2));
		assertEquals(1.5D, VelorumDarkRules.cap(Source.CUT, 2));
		assertEquals(160, VelorumDarkRules.life(Source.CUT, 2));
		assertEquals(240, VelorumDarkRules.life(Source.CUT, 3));
		assertEquals(30, VelorumDarkRules.growTicks(3.5D));
		assertEquals(30, VelorumDarkRules.growTicks(4.5D));
		assertEquals(8, VelorumDarkRules.growTicks(2.5D));
		assertEquals(6, VelorumDarkRules.growTicks(1.5D));
		assertEquals(0.0D, VelorumDarkRules.ownRadius(3.5D, 0));
		double half = VelorumDarkRules.ownRadius(3.5D, 15);
		assertTrue(half > 1.75D && half < 3.5D, "an ease-out: past half way at half time");
		assertEquals(3.5D, VelorumDarkRules.ownRadius(3.5D, 30), 1.0E-9D);
		assertEquals(3.5D, VelorumDarkRules.ownRadius(3.5D, 300), 1.0E-9D);
	}

	@Test
	void theGutterFloodsEveryPoolTowardSevenAndEasesBack() {
		// while it gutters: +0.25 a tick
		assertEquals(5.0D, VelorumDarkRules.poolRadius(2.5D, 100, 10, -1), 1.0E-9D);
		assertEquals(7.0D, VelorumDarkRules.poolRadius(2.5D, 100, 40, -1), 1.0E-9D, "capped at 7");
		// after it closes the pool eases back to its own curve over 40 ticks
		assertEquals(7.0D, VelorumDarkRules.poolRadius(2.5D, 100, 40, 0), 1.0E-9D);
		double back = VelorumDarkRules.poolRadius(2.5D, 100, 40, 20);
		assertTrue(back > 2.5D && back < 7.0D);
		assertEquals(2.5D, VelorumDarkRules.poolRadius(2.5D, 100, 40, 40), 1.0E-9D);
		assertEquals(2.5D, VelorumDarkRules.poolRadius(2.5D, 100, 0, -1), 1.0E-9D, "no flood, its own radius");
	}

	@Test
	void theLanternsLightAgainstTheDarkByState() {
		assertEquals(6.0F, VelorumDarkRules.lanternRadius(1, 0, -1, -1, 0, -1), 1.0E-6F);
		assertEquals(5.0F, VelorumDarkRules.lanternRadius(2, 0, -1, -1, 0, -1), 1.0E-6F);
		assertEquals(4.0F, VelorumDarkRules.lanternRadius(3, 0, -1, -1, 0, -1), 1.0E-6F);
		// the Long Night's wind-up shrinks it to 3 by the commit
		assertEquals(3.0F, VelorumDarkRules.lanternRadius(1, 1.0F, -1, -1, 0, -1), 1.0E-6F);
		assertTrue(VelorumDarkRules.lanternRadius(1, 0.5F, -1, -1, 0, -1) < 6.0F);
		// the Gutter: 1 within its first 10 ticks, the dark floods to his feet
		assertEquals(1.0F, VelorumDarkRules.lanternRadius(1, 0, 10, -1, 0, -1), 1.0E-6F);
		assertEquals(1.0F, VelorumDarkRules.lanternRadius(3, 0, 30, -1, 0, -1), 1.0E-6F);
		// back to the phase value over 20 ticks after it
		assertEquals(1.0F, VelorumDarkRules.lanternRadius(1, 0, -1, 0, 0, -1), 1.0E-6F);
		float mid = VelorumDarkRules.lanternRadius(1, 0, -1, 10, 0, -1);
		assertTrue(mid > 1.0F && mid < 6.0F);
		assertEquals(6.0F, VelorumDarkRules.lanternRadius(1, 0, -1, 20, 0, -1), 1.0E-6F);
		// the Eclipse puts it out; relit, it flares to 8 for 20 ticks and eases back
		assertEquals(0.0F, VelorumDarkRules.lanternRadius(3, 0, -1, -1, 1.0F, -1), 1.0E-6F);
		assertEquals(8.0F, VelorumDarkRules.lanternRadius(3, 0, -1, -1, 0, 0), 1.0E-6F);
		assertEquals(8.0F, VelorumDarkRules.lanternRadius(3, 0, -1, -1, 0, 20), 1.0E-6F);
		assertEquals(4.0F, VelorumDarkRules.lanternRadius(3, 0, -1, -1, 0, 30), 1.0E-6F);
		assertTrue(VelorumDarkRules.lit(5.9D, 6.0D) && !VelorumDarkRules.lit(6.0D, 6.0D));
	}

	@Test
	void theDarkHoldsOnlyOutsideTheLightAwayFromWarmthAndWithoutSearing() {
		assertTrue(VelorumDarkRules.inDark(true, false, false, false, false));
		assertFalse(VelorumDarkRules.inDark(false, false, false, false, false), "out of every pool");
		assertFalse(VelorumDarkRules.inDark(true, true, false, false, false), "inside his light");
		assertFalse(VelorumDarkRules.inDark(true, false, true, false, false), "an ally's warmth");
		assertFalse(VelorumDarkRules.inDark(true, false, false, true, false), "their own heat");
		// the Eclipse: everyone, but for an ally's warmth
		assertTrue(VelorumDarkRules.inDark(false, true, false, true, true));
		assertFalse(VelorumDarkRules.inDark(false, false, true, false, true));
		assertEquals(1.5D, VelorumDarkRules.WARMTH);
	}

	@Test
	void theDarksTermsComeAfterThePresenceAndBeforeTheShelter() {
		assertEquals(0.98F, VelorumStillnessRules.drift(in(0, 0, 0, "d")), 1.0E-5F);
		assertEquals(1.47F, VelorumStillnessRules.drift(in(0, 0, 0, "pd")), 1.0E-5F);
		assertEquals(0.735F, VelorumStillnessRules.drift(in(0, 0, 0, "pdh")), 1.0E-5F);
		assertEquals(1.4F, VelorumStillnessRules.drift(in(0, 0, 0, "id")), 1.0E-5F, "item use is multiplied too");
		// the Slowness and Rime terms are not multiplied
		assertEquals(0.98F + 0.2F, VelorumStillnessRules.drift(in(0, 1, 2, "d")), 1.0E-5F);
		// the drain: walking x0.8 in his presence, x0.9 in the dark
		assertEquals(-0.396F, VelorumStillnessRules.drift(in(0.2, 0, 0, "pd")), 1.0E-5F);
	}

	@Test
	void theDarkFillsInAHundredAndTwoTicksAndSixtyEightInHisPresence() {
		// the design rounds 102.04 and 68.03 down; the meter arrives on the tick after
		assertEquals(103, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "d")));
		assertEquals(69, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "pd")));
		assertEquals(143, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "")));
	}

	@Test
	void walkingAlwaysGetsOutEvenInTheWorstStack() {
		// his presence, the dark, Stilling's Slowness I and the two Rime levels he never goes past
		assertTrue(VelorumStillnessRules.drift(in(0.2, 1, VelorumMoves.RIME_CAP, "pd")) <= -0.08F);
		assertEquals(-0.096F, VelorumStillnessRules.drift(in(0.2, 1, VelorumMoves.RIME_CAP, "pd")), 1.0E-5F);
		assertTrue(VelorumStillnessRules.drift(in(0.3, 1, VelorumMoves.RIME_CAP, "pdr")) <= -0.3F);
		assertEquals(2, VelorumMoves.RIME_CAP);
	}

	@Test
	void poolsStayOnTheBossFloorAndItsLeashRing() {
		// the leash box of a 15-wide floor from 0 to 15 is -1 to 16
		assertArrayEquals(new double[]{-0.5D, 15.5D}, VelorumDarkRules.onLeash(-4.0D, 20.0D, -1.0D, -1.0D, 16.0D, 16.0D), 1.0E-9D);
		assertArrayEquals(new double[]{3.0D, 4.0D}, VelorumDarkRules.onLeash(3.0D, 4.0D, -1.0D, -1.0D, 16.0D, 16.0D), 1.0E-9D);
		assertTrue(VelorumDarkRules.leashed(15.9D, 0.0D, -1.0D, -1.0D, 16.0D, 16.0D));
		assertFalse(VelorumDarkRules.leashed(0.0D, -2.0D, -1.0D, -1.0D, 16.0D, 16.0D), "never into the aisles");
	}

	@Test
	void coverageSamplesTheFloorsCellsAndTheCapFadesPast45Percent() {
		// a disc of radius 3 at a 10 by 10 floor's centre covers 32 of its 100 cell centres
		double coverage = VelorumDarkRules.coverage(0, 0, 10, 10, (x, z) -> Math.hypot(x - 5, z - 5) <= 3.0D);
		assertEquals(0.32D, coverage, 1.0E-9D);
		assertEquals(1.0D, VelorumDarkRules.coverage(0, 0, 10, 10, (x, z) -> true));
		assertEquals(0.0D, VelorumDarkRules.coverage(0, 0, 10, 10, (x, z) -> false));
		assertFalse(VelorumDarkRules.overCap(0.45D));
		assertTrue(VelorumDarkRules.overCap(0.46D));
		assertTrue(VelorumEclipseRules.gate(3, true, 400, 600, VelorumEclipseRules.COVERAGE_GATE, 0.5F),
				"the Eclipse's gate reads the same sampling");
	}
}
