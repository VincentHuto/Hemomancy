package com.vincenthuto.hemomancy.common.capability.player.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class UnstainedAccessRulesTest {
	@Test
	void derivesEveryPersistedProgressPhase() {
		UnstainedProgress progress = new UnstainedProgress();
		assertEquals(UnstainedPhase.OUTSIDER, UnstainedAccessRules.phase(progress));

		progress.setAcceptedObservances(1 << 9);
		assertEquals(UnstainedPhase.NOVITIATE, UnstainedAccessRules.phase(progress));

		progress.setBegunPurification(true);
		progress.setPurity(40f);
		assertEquals(UnstainedPhase.PURIFYING, UnstainedAccessRules.phase(progress));

		progress.setPurity(100f);
		assertEquals(UnstainedPhase.CURE_READY, UnstainedAccessRules.phase(progress));

		progress.setBaselineRestored(true);
		assertEquals(UnstainedPhase.CLEANSED_UNPLEDGED, UnstainedAccessRules.phase(progress));

		progress.setClarityUnlocked(true);
		assertEquals(UnstainedPhase.PLEDGED, UnstainedAccessRules.phase(progress));
	}

	@Test
	void baptismStartsLowerForEachDegreeAndSeveredFoundersStartAtZero() {
		assertEquals(50f, UnstainedAccessRules.baptismStartingPurity(0, false));
		assertEquals(40f, UnstainedAccessRules.baptismStartingPurity(1, false));
		assertEquals(30f, UnstainedAccessRules.baptismStartingPurity(2, false));
		assertEquals(20f, UnstainedAccessRules.baptismStartingPurity(3, false));
		assertEquals(10f, UnstainedAccessRules.baptismStartingPurity(4, false));
		assertEquals(0f, UnstainedAccessRules.baptismStartingPurity(5, false));
		assertEquals(0f, UnstainedAccessRules.baptismStartingPurity(0, true));
	}

	@Test
	void allFiveNewVowsAreRequiredForHealthyPledgeEligibility() {
		int fourVows = UnstainedAccessRules.NOVITIATE_MASK & ~(1 << 13);
		assertFalse(UnstainedAccessRules.hasCompletedNovitiateVows(fourVows));
		assertTrue(UnstainedAccessRules.hasCompletedNovitiateVows(UnstainedAccessRules.NOVITIATE_MASK));
	}

	@Test
	void bloodProgressStopsOnlyAfterPurificationBeginsOrBaselineIsRestored() {
		assertTrue(UnstainedAccessRules.mayGainBloodProgress(UnstainedPhase.OUTSIDER));
		assertTrue(UnstainedAccessRules.mayGainBloodProgress(UnstainedPhase.NOVITIATE));
		assertFalse(UnstainedAccessRules.mayGainBloodProgress(UnstainedPhase.PURIFYING));
		assertFalse(UnstainedAccessRules.mayUseBloodPowers(UnstainedPhase.CLEANSED_UNPLEDGED));
		assertFalse(UnstainedAccessRules.mayUseBloodPowers(UnstainedPhase.PLEDGED));
	}

	@Test
	void novitiateAccessUnlocksOnlyTheAuthoredPublicTools() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setClaimedObservances(1 << 9);
		assertTrue(UnstainedAccessRules.bypassesUnstainedLevelGate(
				"blood_structure/pallid_retort", progress));
		assertFalse(UnstainedAccessRules.bypassesUnstainedLevelGate(
				"cardinal_rite/still_waters", progress));

		progress.setClaimedObservances(progress.getClaimedObservances() | (1 << 12));
		assertTrue(UnstainedAccessRules.bypassesUnstainedLevelGate(
				"cardinal_rite/still_waters", progress));
		assertFalse(UnstainedAccessRules.bypassesUnstainedLevelGate(
				"cardinal_rite/silver_veil", progress));
	}

	@Test
	void treatmentBlocksHarbingerRitesButNotKnownBloodPowers() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setBegunPurification(true);
		progress.setPurity(40f);

		assertTrue(UnstainedAccessRules.blocksHarbingerProgress(progress));
		assertFalse(UnstainedAccessRules.blocksKnownBloodPowerUse(progress));
		progress.setBaselineRestored(true);
		assertTrue(UnstainedAccessRules.blocksKnownBloodPowerUse(progress));
	}
}
