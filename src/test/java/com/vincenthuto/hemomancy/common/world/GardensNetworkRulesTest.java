package com.vincenthuto.hemomancy.common.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardensNetworkRulesTest {
	@Test
	void onlyAudibleStepsOnTheNetworkCount() {
		assertEquals(0.5, GardensNetworkRules.footfall(false, true, 0.25), 1e-9);
		assertEquals(0.0, GardensNetworkRules.footfall(true, true, 0.25), "sneaking is silent");
		assertEquals(0.0, GardensNetworkRules.footfall(false, false, 0.25), "off the network");
		assertEquals(0.0, GardensNetworkRules.footfall(false, true, 0.0001), "standing still");
		assertEquals(0.0, GardensNetworkRules.footfall(false, true, 2.25), "a teleport or pearl is not a footstep");
	}

	@Test
	void theNetworkAlarmsAfterEightBlocksAndNotTooOften() {
		assertTrue(GardensNetworkRules.shouldAlarm(8.0, 100));
		assertFalse(GardensNetworkRules.shouldAlarm(7.9, 1000));
		assertFalse(GardensNetworkRules.shouldAlarm(20.0, 99));
	}

	@Test
	void autonomyAndBloodRaiseSuspicionButTheHivesOwnBloodDoesNot() {
		assertEquals(2f, GardensNetworkRules.castSuspicion(false));
		assertEquals(1f, GardensNetworkRules.castSuspicion(true));
		assertEquals(0.25f, GardensNetworkRules.bloodSuspicionPerSecond(false, 800, 1000));
		assertEquals(0f, GardensNetworkRules.bloodSuspicionPerSecond(false, 700, 1000));
		assertEquals(0f, GardensNetworkRules.bloodSuspicionPerSecond(true, 1000, 1000));
		assertEquals(1f, GardensNetworkRules.DECAY_PER_10S);
	}

	@Test
	void antibodiesAnswerInProportionAndRest() {
		assertEquals(0, GardensNetworkRules.antibodiesFor(11.9f));
		assertEquals(2, GardensNetworkRules.antibodiesFor(12f));
		assertEquals(3, GardensNetworkRules.antibodiesFor(18f));
		assertEquals(4, GardensNetworkRules.antibodiesFor(60f));
		assertTrue(GardensNetworkRules.canSummon(600));
		assertFalse(GardensNetworkRules.canSummon(599));
	}

	@Test
	void theApotheosSensesWhatStandsOnTheNetworkNearby() {
		assertTrue(GardensNetworkRules.senses(true, true, true, 31 * 31));
		assertFalse(GardensNetworkRules.senses(false, true, true, 4));
		assertFalse(GardensNetworkRules.senses(true, false, true, 4));
		assertFalse(GardensNetworkRules.senses(true, true, false, 4));
		assertFalse(GardensNetworkRules.senses(true, true, true, 33 * 33));
	}

	@Test
	void puffballsReactOnlyInTheGardens() {
		assertEquals(GardensNetworkRules.PuffballOutcome.NONE, GardensNetworkRules.puffball(false, false));
		assertEquals(GardensNetworkRules.PuffballOutcome.NONE, GardensNetworkRules.puffball(true, false));
		assertEquals(GardensNetworkRules.PuffballOutcome.ABSORB, GardensNetworkRules.puffball(true, true));
		assertEquals(GardensNetworkRules.PuffballOutcome.BURST, GardensNetworkRules.puffball(false, true));
	}
}
