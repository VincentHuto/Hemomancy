package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** How the Heartwood tree's core turns blood and bleeding mobs into glow, and when it has become Ravenous. */
class HeartwoodCoreRulesTest {
	@Test
	void theStageRisesWithTheFeedAndOnlyAFullCoreIsRavenous() {
		assertEquals(0, HeartwoodCoreRules.stage(0));
		assertEquals(1, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL * 0.25));
		assertEquals(2, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL * 0.5));
		assertEquals(3, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL * 0.75));
		assertEquals(3, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL - 0.5));
		assertEquals(HeartwoodCoreRules.RAVENOUS_STAGE, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL));
		assertFalse(HeartwoodCoreRules.isRavenous(HeartwoodCoreRules.FULL - 0.5));
		assertTrue(HeartwoodCoreRules.isRavenous(HeartwoodCoreRules.FULL));
	}

	@Test
	void theFeedStaysInRange() {
		assertEquals(HeartwoodCoreRules.FULL, HeartwoodCoreRules.fed(HeartwoodCoreRules.FULL - 1, 500));
		assertEquals(0, HeartwoodCoreRules.fed(0, -5));
		assertEquals(HeartwoodCoreRules.RAVENOUS_STAGE, HeartwoodCoreRules.stage(HeartwoodCoreRules.FULL * 3));
	}

	@Test
	void theCoreGlowsBrighterAsItFeeds() {
		for (int stage = 1; stage <= HeartwoodCoreRules.RAVENOUS_STAGE; stage++) {
			assertTrue(HeartwoodCoreRules.light(stage) > HeartwoodCoreRules.light(stage - 1), "stage " + stage);
		}
		assertEquals(15, HeartwoodCoreRules.light(HeartwoodCoreRules.RAVENOUS_STAGE));
	}

	@Test
	void aProjectionTakesOnlyWhatTheCoreHasRoomFor() {
		assertEquals(40, HeartwoodCoreRules.accepted(0, 40, 500));
		assertEquals(10, HeartwoodCoreRules.accepted(HeartwoodCoreRules.FULL - 10, 40, 500));
		assertEquals(25, HeartwoodCoreRules.accepted(0, 40, 25), "the caster's own blood is a limit");
		assertEquals(0, HeartwoodCoreRules.accepted(HeartwoodCoreRules.FULL, 40, 500));
	}

	@Test
	void aPlayerFeedsItAsMuchAsThePulseDrainsAndAMobFeedsMore() {
		// Blood Loss I drains a player 0.5 mL a tick; a pulse is PULSE_TICKS long.
		assertEquals(0.5 * HeartwoodCoreRules.PULSE_TICKS, HeartwoodCoreRules.PLAYER_FEED);
		assertTrue(HeartwoodCoreRules.MOB_FEED > HeartwoodCoreRules.PLAYER_FEED);
	}

	@Test
	void aboutNineCowsFillIt() {
		// A cow's ten health lasts about twenty pulses of Blood Loss I at its one damage per second.
		double cowPulses = 20;
		long cows = Math.round(Math.ceil(HeartwoodCoreRules.FULL / (cowPulses * HeartwoodCoreRules.MOB_FEED)));
		assertEquals(9, cows);
	}
}
