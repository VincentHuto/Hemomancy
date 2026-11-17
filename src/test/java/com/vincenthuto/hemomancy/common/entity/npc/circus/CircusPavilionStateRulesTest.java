package com.vincenthuto.hemomancy.common.entity.npc.circus;

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
	void aRefusedClaimSaysWhetherTheSiteIsHeldOrSettled() {
		assertEquals("hemomancy.circus.finale.busy",
				CircusPavilionStateRules.refusalKey(CircusPavilionStateRules.Outcome.NEUTRAL));
		assertEquals("hemomancy.circus.finale.settled",
				CircusPavilionStateRules.refusalKey(CircusPavilionStateRules.Outcome.SUCCESSION));
		assertEquals("hemomancy.circus.finale.settled",
				CircusPavilionStateRules.refusalKey(CircusPavilionStateRules.Outcome.RUIN));
	}

	@Test
	void failedAttemptReturnsToNeutralWithoutChangingOutcome() {
		assertEquals(CircusPavilionStateRules.Phase.IDLE,
				CircusPavilionStateRules.resetPhase(CircusPavilionStateRules.Outcome.NEUTRAL));
	}

	/** D-34: a bystander is told once per act, however many troupe members they strike, and again in a new act. */
	@Test
	void aBystanderIsToldOncePerAct() {
		java.util.Map<UUID, UUID> told = new java.util.HashMap<>();
		UUID bystander = UUID.randomUUID();
		UUID holder = UUID.randomUUID();
		assertTrue(CircusPavilionStateRules.firstRefusal(told, bystander, holder));
		assertFalse(CircusPavilionStateRules.firstRefusal(told, bystander, holder), "a second troupe member stays quiet");
		assertTrue(CircusPavilionStateRules.firstRefusal(told, bystander, UUID.randomUUID()), "another holder's act tells again");
		assertTrue(CircusPavilionStateRules.firstRefusal(told, UUID.randomUUID(), holder), "each bystander is told");
	}
}
