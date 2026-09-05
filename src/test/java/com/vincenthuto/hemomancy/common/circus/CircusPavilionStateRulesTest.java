package com.vincenthuto.hemomancy.common.circus;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircusPavilionStateRulesTest {
	@Test
	void onlyOnePlayerCanOwnAnActiveFinale() {
		UUID owner = UUID.randomUUID();
		assertTrue(CircusPavilionStateRules.canBegin(null, CircusPavilionStateRules.Outcome.NEUTRAL));
		assertTrue(CircusPavilionStateRules.canAct(owner, owner));
		assertFalse(CircusPavilionStateRules.canAct(owner, UUID.randomUUID()));
	}

	@Test
	void completedSiteCannotBeClaimedAgain() {
		assertFalse(CircusPavilionStateRules.canBegin(null, CircusPavilionStateRules.Outcome.SUCCESSION));
		assertFalse(CircusPavilionStateRules.canBegin(null, CircusPavilionStateRules.Outcome.RUIN));
	}

	@Test
	void failedAttemptReturnsToNeutralWithoutChangingOutcome() {
		assertEquals(CircusPavilionStateRules.Phase.IDLE,
				CircusPavilionStateRules.resetPhase(CircusPavilionStateRules.Outcome.NEUTRAL));
		assertEquals(CircusPavilionStateRules.Outcome.NEUTRAL,
				CircusPavilionStateRules.resetOutcome(CircusPavilionStateRules.Outcome.NEUTRAL));
	}
}
