package com.vincenthuto.hemomancy.common.rite.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnstainedRitePreflightTest {
	@Test
	void baptismRejectsBeforeCommitUntilInfectionIsSuppressed() {
		var blocked = UnstainedRitePreflight.check(
				"cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, false, false, false, false, false));

		assertFalse(blocked.success());
		assertEquals(UnstainedRitePreflight.Failure.INFECTION_NOT_SUPPRESSED, blocked.failure());

		var retry = UnstainedRitePreflight.check(
				"cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, true, false, false, false, false));

		assertTrue(retry.success());
	}

	@Test
	void clarityAscensionRequiresPurityAndCopperPreparationBeforeCommit() {
		var notPurified = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				new UnstainedRitePreflight.State(true, true, true, false, false, false));
		var notPrepared = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				new UnstainedRitePreflight.State(true, true, true, true, false, false));
		var retry = UnstainedRitePreflight.check(
				"cardinal_rite/clarity_ascension",
				new UnstainedRitePreflight.State(true, true, true, true, true, false));

		assertEquals(UnstainedRitePreflight.Failure.NOT_PURIFIED, notPurified.failure());
		assertEquals(UnstainedRitePreflight.Failure.CLARITY_NOT_PREPARED, notPrepared.failure());
		assertTrue(retry.success());
	}

	@Test
	void ordinaryAndAlreadyValidUnstainedRitesRemainAccepted() {
		var state = new UnstainedRitePreflight.State(true, true, true, true, true, false);

		assertTrue(UnstainedRitePreflight.check("cardinal_rite/silver_veil", state).success());
		assertTrue(UnstainedRitePreflight.check("cardinal_rite/sanguine_initiation", state).success());
	}
}
