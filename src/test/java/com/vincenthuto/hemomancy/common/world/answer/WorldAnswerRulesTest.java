package com.vincenthuto.hemomancy.common.world.answer;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.OssuaryClockReadout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 10: the world's answer, the pure numbers. The count is per 64-block region, halves every two days
 * (faster under a ward), and bands still to seething. The Blood Moon follows the phase (none at the new moon, most at
 * the full) and rises with pressure but never past its cap; Wills and scavengers read the same pressure; stained sod
 * comes only in a heavy region and goes back as it quiets.
 */
class WorldAnswerRulesTest {
	@Test
	void regionsAreSixtyFourBlocksSquare() {
		assertEquals(WorldAnswerRules.regionKey(0, 0), WorldAnswerRules.regionKey(63, 63));
		assertNotEquals(WorldAnswerRules.regionKey(0, 0), WorldAnswerRules.regionKey(64, 0));
		assertNotEquals(WorldAnswerRules.regionKey(0, 0), WorldAnswerRules.regionKey(0, 64));
		assertNotEquals(WorldAnswerRules.regionKey(-1, 0), WorldAnswerRules.regionKey(0, 0));
		assertEquals(WorldAnswerRules.regionKey(-64, -1), WorldAnswerRules.regionKey(-1, -64));
	}

	/**
	 * Round two P9's "Brought On": a moon the spilled blood raised is one whose roll fell under the raised chance but not
	 * under the phase's own. No pressure never brings one on, and the new moon has none to bring.
	 */
	@Test
	void aMoonIsBroughtOnOnlyByTheRollThePressureAdded() {
		double base = WorldAnswerRules.moonChance(0, 0.0D);
		double raised = WorldAnswerRules.moonChance(0, 1.0D);
		assertTrue(raised > base, "pressure did not raise the full moon's chance");
		double between = (base + raised) / 2.0D;
		assertTrue(WorldAnswerRules.broughtOn(0, 1.0D, between), "a roll between the phase's and the raised chance was not brought on");
		assertFalse(WorldAnswerRules.broughtOn(0, 1.0D, base / 2.0D), "a moon the phase alone raised was credited to the blood");
		assertFalse(WorldAnswerRules.broughtOn(0, 1.0D, raised), "a roll at the raised chance raised no moon");
		assertFalse(WorldAnswerRules.broughtOn(0, 0.0D, between), "no pressure brought a moon on");
		for (double roll = 0.0D; roll < 1.0D; roll += 0.01D) {
			assertFalse(WorldAnswerRules.broughtOn(4, 1.0D, roll), "the new moon brought a Blood Moon on at roll " + roll);
		}
	}

	@Test
	void theCountHalvesEveryTwoDaysAndFasterUnderAWard() {
		assertEquals(1000.0D, WorldAnswerRules.decay(1000.0D, 0L, 0));
		assertEquals(500.0D, WorldAnswerRules.decay(1000.0D, WorldAnswerRules.HALF_LIFE_TICKS, 0), 1.0E-9D);
		assertEquals(250.0D, WorldAnswerRules.decay(1000.0D, WorldAnswerRules.HALF_LIFE_TICKS, 1), 1.0E-9D);
		assertEquals(0.0D, WorldAnswerRules.decay(1.5D, WorldAnswerRules.HALF_LIFE_TICKS, 0), "gone below a millilitre");
		double split = WorldAnswerRules.decay(WorldAnswerRules.decay(800.0D, 1_000L, 0), 2_000L, 0);
		assertEquals(WorldAnswerRules.decay(800.0D, 3_000L, 0), split, 1.0E-9D, "settling is path-independent");
	}

	@Test
	void aWardHalvesWhatIsCountedUpToThree() {
		assertEquals(40.0D, WorldAnswerRules.counted(40.0D, 0));
		assertEquals(20.0D, WorldAnswerRules.counted(40.0D, 1));
		assertEquals(5.0D, WorldAnswerRules.counted(40.0D, 3));
		assertEquals(5.0D, WorldAnswerRules.counted(40.0D, 9), "no more than three count");
		assertEquals(0.0D, WorldAnswerRules.add(10.0D, -40.0D), "cleaning never goes below nothing");
		assertEquals(WorldAnswerRules.MAX_ML, WorldAnswerRules.add(WorldAnswerRules.MAX_ML, 500.0D));
	}

	@Test
	void bandsRunStillToSeething() {
		assertEquals(WorldAnswerRules.STILL, WorldAnswerRules.band(0.0D));
		assertEquals(WorldAnswerRules.STILL, WorldAnswerRules.band(199.0D));
		assertEquals(WorldAnswerRules.STIRRING, WorldAnswerRules.band(200.0D));
		assertEquals(WorldAnswerRules.UNEASY, WorldAnswerRules.band(600.0D));
		assertEquals(WorldAnswerRules.HEAVY, WorldAnswerRules.band(1_500.0D));
		assertEquals(WorldAnswerRules.SEETHING, WorldAnswerRules.band(3_000.0D));
		for (int band = 0; band <= WorldAnswerRules.SEETHING; band++) {
			assertEquals(band, WorldAnswerRules.band(WorldAnswerRules.bandFloor(band)));
		}
		assertEquals(1.0D, WorldAnswerRules.pressure(9_000.0D));
	}

	@Test
	void theBloodMoonFollowsThePhaseAndRisesWithPressure() {
		assertEquals(0.25D, WorldAnswerRules.moonChance(0, 0.0D), 1.0E-9D, "a quiet full moon");
		assertEquals(0.0D, WorldAnswerRules.moonChance(4, 1.0D), "never at the new moon, however busy");
		for (int phase = 1; phase < 8; phase++) {
			assertTrue(WorldAnswerRules.moonChance(phase, 0.0D) < WorldAnswerRules.moonChance(0, 0.0D));
			assertEquals(WorldAnswerRules.moonBase(phase), WorldAnswerRules.moonBase(8 - phase), "waxing mirrors waning");
		}
		assertTrue(WorldAnswerRules.moonChance(1, 0.5D) > WorldAnswerRules.moonChance(1, 0.0D));
		assertEquals(WorldAnswerRules.MOON_CAP, WorldAnswerRules.moonChance(0, 1.0D), 1.0E-9D);
		double quiet = 0.0D;
		for (int phase = 0; phase < 8; phase++) quiet += WorldAnswerRules.moonChance(phase, 0.0D);
		assertTrue(quiet / 8.0D < 1.0D / 7.0D, "a quiet world sees fewer Blood Moons than the old one-in-seven");
		assertEquals(0, WorldAnswerRules.nightsToFullMoon(0));
		assertEquals(7, WorldAnswerRules.nightsToFullMoon(1));
		assertEquals(4, WorldAnswerRules.nightsToFullMoon(4));
		assertEquals(1, WorldAnswerRules.nightsToFullMoon(7));
	}

	@Test
	void theClockShowsTheOmen() {
		assertEquals(2, OssuaryClockReadout.omen(true, 0.0D));
		assertEquals(1, OssuaryClockReadout.omen(false, WorldAnswerRules.OMEN_CHANCE));
		assertEquals(0, OssuaryClockReadout.omen(false, 0.04D));
		assertEquals("none", OssuaryClockReadout.omenWord(0.0D));
		assertEquals("strong", OssuaryClockReadout.omenWord(0.9D));
	}

	@Test
	void willsAndScavengersReadThePressure() {
		assertEquals(1.0D, WorldAnswerRules.willMultiplier(0.0D));
		assertEquals(3.0D, WorldAnswerRules.willMultiplier(1.0D));
		assertEquals(0.0D, WorldAnswerRules.scavengerChance(2, WorldAnswerRules.SEETHING), "a stain or two draws nothing");
		assertTrue(WorldAnswerRules.scavengerChance(3, WorldAnswerRules.HEAVY) > WorldAnswerRules.scavengerChance(3, WorldAnswerRules.STILL));
		assertFalse(WorldAnswerRules.poolDrawsHunter(0.5D, true, WorldAnswerRules.SEETHING), "a weak pool draws no hunter");
		assertFalse(WorldAnswerRules.poolDrawsHunter(1.0D, false, WorldAnswerRules.STIRRING), "by day in a quiet region");
		assertTrue(WorldAnswerRules.poolDrawsHunter(1.0D, true, WorldAnswerRules.STILL));
		assertTrue(WorldAnswerRules.poolDrawsHunter(1.0D, false, WorldAnswerRules.UNEASY));
	}

	/** Round two, P7: the water decides the hunter, home waters before a swamp; a swamp's pond may be shallower. */
	@Test
	void theWaterDecidesWhichHunterAPoolDraws() {
		assertEquals(WorldAnswerRules.HunterWaters.OPEN, WorldAnswerRules.hunterWaters(false, false));
		assertEquals(WorldAnswerRules.HunterWaters.HOME, WorldAnswerRules.hunterWaters(true, false));
		assertEquals(WorldAnswerRules.HunterWaters.HOME, WorldAnswerRules.hunterWaters(true, true), "the Court's waters win");
		assertEquals(WorldAnswerRules.HunterWaters.SWAMP, WorldAnswerRules.hunterWaters(false, true));
		assertEquals(2, WorldAnswerRules.hunterDepth(WorldAnswerRules.HunterWaters.OPEN));
		assertEquals(2, WorldAnswerRules.hunterDepth(WorldAnswerRules.HunterWaters.HOME));
		assertEquals(1, WorldAnswerRules.hunterDepth(WorldAnswerRules.HunterWaters.SWAMP));
		assertEquals(1, WorldAnswerRules.HUNTER_CAP, "one hunter at a time, whatever its kind");
	}

	@Test
	void killsLeaveBloodBySize() {
		assertEquals(WorldAnswerRules.KILL_SPILL, WorldAnswerRules.killSpill(10.0F), "at least one share");
		assertEquals(WorldAnswerRules.KILL_SPILL * 5.0D, WorldAnswerRules.killSpill(100.0F), 1.0E-9D);
	}

	/** CB-26, NQ-02-7: kills lift a region to the uneasy floor and no further. */
	@Test
	void killsStopCountingAtTheUneasyFloor() {
		assertEquals(600.0D, WorldAnswerRules.KILL_SHARE_CAP_ML, 1.0E-9D);
		assertEquals(WorldAnswerRules.UNEASY, WorldAnswerRules.band(WorldAnswerRules.KILL_SHARE_CAP_ML), "the cap sits on the uneasy floor");
		assertEquals(6.0D, WorldAnswerRules.killCounted(0.0D, 6.0D), 1.0E-9D, "below the cap a kill counts in full");
		assertEquals(2.0D, WorldAnswerRules.killCounted(598.0D, 6.0D), 1.0E-9D, "a kill counts only up to the cap");
		assertEquals(0.0D, WorldAnswerRules.killCounted(600.0D, 6.0D), 1.0E-9D, "a kill at the cap counts nothing");
		assertEquals(0.0D, WorldAnswerRules.killCounted(2_000.0D, 30.0D), 1.0E-9D, "a kill in a heavy region counts nothing");
	}

	@Test
	void sodGoesBackOnlyOnceTheRegionQuiets() {
		assertTrue(WorldAnswerRules.sodRecovers(WorldAnswerRules.STILL, 0.0F));
		assertTrue(WorldAnswerRules.sodRecovers(WorldAnswerRules.STIRRING, 0.1F));
		assertFalse(WorldAnswerRules.sodRecovers(WorldAnswerRules.UNEASY, 0.0F));
		assertFalse(WorldAnswerRules.sodRecovers(WorldAnswerRules.STILL, 0.9F), "slowly");
	}
}
