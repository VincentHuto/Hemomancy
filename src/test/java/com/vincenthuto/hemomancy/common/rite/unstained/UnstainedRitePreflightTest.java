package com.vincenthuto.hemomancy.common.rite.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class UnstainedRitePreflightTest {
	@Test
	void baptismRejectsBeforeCommitUntilInfectionIsSuppressed() {
		var blocked = UnstainedRitePreflight.check(
				"cardinal_rite/lethean_baptism",
				state(true, false, false, false, false, false, false, false, false, false));

		assertFalse(blocked.success());
		assertEquals(UnstainedRitePreflight.Failure.INFECTION_NOT_SUPPRESSED, blocked.failure());

		var retry = UnstainedRitePreflight.check(
				"cardinal_rite/lethean_baptism",
				state(true, true, false, false, false, false, false, false, false, false));

		assertTrue(retry.success());
	}

	@Test
	void clarityAscensionRequiresPurityAndCopperPreparationBeforeCommit() {
		var notPurified = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				state(true, true, true, false, false, false, false, false, false, false));
		var notPrepared = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				state(true, true, true, true, true, false, false, false, true, false));
		var retry = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				state(true, true, true, true, true, false, true, false, true, false));

		assertEquals(UnstainedRitePreflight.Failure.NOT_READY_TO_PLEDGE, notPurified.failure());
		assertEquals(UnstainedRitePreflight.Failure.CLARITY_NOT_PREPARED, notPrepared.failure());
		assertTrue(retry.success());
	}

	@Test
	void ordinaryAndAlreadyValidUnstainedRitesRemainAccepted() {
		var state = state(true, true, true, true, true, false, true, false, true, false);

		assertTrue(UnstainedRitePreflight.check("cardinal_rite/silver_veil", state).success());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/sanguine_initiation", state).success());
	}

	@Test
	void healthyVowsAndCleanBloodCanPrepareTheSamePledge() {
		var healthy = state(true, false, false, false, false, true, true, false, true, false);

		assertTrue(UnstainedRitePreflight.check("cardinal_rite/clarity_ascension", healthy).success());
	}

	@Test
	void closedVeinIsCureAtFullPurityAndRepeatableOnlyAfterPledge() {
		var tooSoon = state(true, true, true, false, false, false, false, false, false, false);
		var cureReady = state(true, true, true, true, false, false, false, false, false, false);
		var alreadyCleansed = state(true, false, true, true, true, false, false, false, true, false);
		var pledged = state(true, false, true, true, true, false, false, true, true, false);

		assertEquals(UnstainedRitePreflight.Failure.CURE_NOT_READY,
				UnstainedRitePreflight.check("cardinal_rite/closed_vein", tooSoon).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/closed_vein", cureReady).success());
		assertEquals(UnstainedRitePreflight.Failure.CLOSED_VEIN_MEMBER_ONLY,
				UnstainedRitePreflight.check("cardinal_rite/closed_vein", alreadyCleansed).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/closed_vein", pledged).success());
	}

	@Test
	void severedCovenantCannotConsumeMediaUntilFounderUnlockIsValid() {
		var blocked = state(false, false, false, false, false, false, false, false, false, false);
		var eligible = state(false, false, false, false, false, false, false, false, false, true);

		assertEquals(UnstainedRitePreflight.Failure.SEVERANCE_NOT_READY,
				UnstainedRitePreflight.check("cardinal_rite/severed_covenant", blocked).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/severed_covenant", eligible).success());
	}

	@Test
	void severedCovenantAlsoNeedsTheFoundersOwnLivingCovenant() {
		var noCovenant = new UnstainedRitePreflight.State(false, false, false, false, false, false, false, false,
				false, true, false, 0.0f, false);

		assertEquals(UnstainedRitePreflight.Failure.NO_FOUNDERS_COVENANT,
				UnstainedRitePreflight.check("cardinal_rite/severed_covenant", noCovenant).failure());
	}

	@Test
	void silverVeilRefusesBeforeCommitBelowTaintedPurity() {
		assertEquals(UnstainedRitePreflight.Failure.VEIL_PURITY_TOO_LOW,
				UnstainedRitePreflight.check("cardinal_rite/silver_veil", withPurity(24.9f, false, false)).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/silver_veil", withPurity(25.0f, false, false)).success());
	}

	@Test
	void letheanTideNeedsPurityAndALiveBloodMoonBeforeCommit() {
		assertEquals(UnstainedRitePreflight.Failure.TIDE_PURITY_TOO_LOW,
				UnstainedRitePreflight.check("cardinal_rite/lethean_tide", withPurity(49.0f, true, true)).failure());
		assertEquals(UnstainedRitePreflight.Failure.NO_BLOOD_MOON,
				UnstainedRitePreflight.check("cardinal_rite/lethean_tide", withPurity(60.0f, true, false)).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/lethean_tide", withPurity(60.0f, true, true)).success());
	}

	@Test
	void silverDawnRequiresClarityBeforeCommit() {
		var noClarity = state(true, false, true, true, true, false, false, false, true, false);
		var clarity = state(true, false, true, true, true, false, false, true, true, false);

		assertEquals(UnstainedRitePreflight.Failure.CLARITY_REQUIRED,
				UnstainedRitePreflight.check("cardinal_rite/silver_dawn", noClarity).failure());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/silver_dawn", clarity).success());
	}

	private static UnstainedRitePreflight.State withPurity(float purity, boolean begunPurification,
			boolean bloodMoonActive) {
		return new UnstainedRitePreflight.State(true, true, begunPurification, false, false, false, false, false,
				false, false, false, purity, bloodMoonActive);
	}

	private static UnstainedRitePreflight.State state(boolean maySeekCure, boolean infectionSuppressed,
			boolean begunPurification, boolean purified, boolean baselineRestored, boolean novitiateVowsComplete,
			boolean clarityPrepared, boolean clarityUnlocked, boolean cleanBlood, boolean severedCovenantEligible) {
		return new UnstainedRitePreflight.State(maySeekCure, infectionSuppressed, begunPurification, purified,
				baselineRestored, novitiateVowsComplete, clarityPrepared, clarityUnlocked, cleanBlood,
				severedCovenantEligible, severedCovenantEligible, purified ? 100.0f : 0.0f, false);
	}
}
