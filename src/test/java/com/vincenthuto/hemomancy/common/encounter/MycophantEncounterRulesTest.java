package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MycophantEncounterRulesTest {
	@Test
	void onlyLivingSurvivalApotheosAccumulateFirstHuntExposureInTheGardens() {
		assertTrue(MycophantEncounterRules.shouldAccumulate(
				8, EnumArchonPath.APOTHEOS, false, true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldAccumulate(
				7, EnumArchonPath.APOTHEOS_PENDING, false, true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldAccumulate(
				8, EnumArchonPath.APOTHEOS, true, true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldAccumulate(
				8, EnumArchonPath.APOTHEOS, false, false, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldAccumulate(
				8, EnumArchonPath.APOTHEOS, false, true, true, true, false, false));
	}

	@Test
	void fifteenMinuteHuntUsesTheFourApprovedThresholds() {
		int duration = 18_000;
		assertEquals(MycophantEncounterRules.HuntStage.QUIET,
				MycophantEncounterRules.huntStage(3_599, duration));
		assertEquals(MycophantEncounterRules.HuntStage.WHISPERS,
				MycophantEncounterRules.huntStage(3_600, duration));
		assertEquals(MycophantEncounterRules.HuntStage.VIGNETTE,
				MycophantEncounterRules.huntStage(8_400, duration));
		assertEquals(MycophantEncounterRules.HuntStage.HALLUCINATION,
				MycophantEncounterRules.huntStage(13_200, duration));
		assertEquals(MycophantEncounterRules.HuntStage.CLAIM,
				MycophantEncounterRules.huntStage(18_000, duration));
	}

	@Test
	void cueCadenceTightensAsTheClaimApproaches() {
		assertEquals(Integer.MAX_VALUE,
				MycophantEncounterRules.averageCueIntervalSeconds(MycophantEncounterRules.HuntStage.QUIET));
		assertEquals(90,
				MycophantEncounterRules.averageCueIntervalSeconds(MycophantEncounterRules.HuntStage.WHISPERS));
		assertEquals(60,
				MycophantEncounterRules.averageCueIntervalSeconds(MycophantEncounterRules.HuntStage.VIGNETTE));
		assertEquals(35,
				MycophantEncounterRules.averageCueIntervalSeconds(MycophantEncounterRules.HuntStage.HALLUCINATION));
	}

	@Test
	void lureRequiresACompletedFirstVictoryAndNoCooldownOrActiveFight() {
		assertTrue(MycophantEncounterRules.canUseLure(8, EnumArchonPath.APOTHEOS,
				true, 0, false, true, true));
		assertFalse(MycophantEncounterRules.canUseLure(8, EnumArchonPath.APOTHEOS,
				false, 0, false, true, true));
		assertFalse(MycophantEncounterRules.canUseLure(8, EnumArchonPath.APOTHEOS,
				true, 1, false, true, true));
		assertFalse(MycophantEncounterRules.canUseLure(8, EnumArchonPath.APOTHEOS,
				true, 0, true, true, true));
	}

	@Test
	void aLostRematchReArmsOnceTheCooldownHasRun() {
		assertTrue(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, false, 0,
				true, true, false, false, false), "no lost rematch is owed");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 1,
				true, true, false, false, false), "the cooldown has not run");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, false, true, 0,
				true, true, false, false, false), "an unbeaten player is on the first hunt");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.SILENTIUM, true, true, 0,
				true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldRearmRematch(7, EnumArchonPath.APOTHEOS, true, true, 0,
				true, true, false, false, false));
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				false, true, false, false, false), "outside the Gardens");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				true, false, false, false, false), "dead");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				true, true, true, false, false), "creative");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				true, true, false, true, false), "spectator");
		assertFalse(MycophantEncounterRules.shouldRearmRematch(8, EnumArchonPath.APOTHEOS, true, true, 0,
				true, true, false, false, true), "already fighting");
	}
}
