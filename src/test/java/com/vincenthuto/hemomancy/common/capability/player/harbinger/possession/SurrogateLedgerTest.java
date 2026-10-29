package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurrogateLedgerTest {
	private final UUID body = UUID.randomUUID();
	private final UUID owner = UUID.randomUUID();

	@Test
	void aHeldSurrogateIsNeverOfferedToASecondCaster() {
		SurrogateLedger ledger = new SurrogateLedger();
		ledger.hold(body, owner);
		assertTrue(ledger.isHeld(body));
		assertEquals(SurrogateLedger.Verdict.HELD, ledger.verdict(body));
	}

	@Test
	void aReturnSettlesTheSurrogateEvenWhenItIsNotLoaded() {
		SurrogateLedger ledger = new SurrogateLedger();
		ledger.hold(body, owner);
		ledger.settle(body, true);
		assertEquals(SurrogateLedger.Verdict.KILL, ledger.verdict(body), "a broken visit kills it when it next loads");
		assertFalse(ledger.isHeld(body));
		ledger.forget(body);
		assertNull(ledger.verdict(body));
	}

	@Test
	void aCleanReturnReleases() {
		SurrogateLedger ledger = new SurrogateLedger();
		ledger.hold(body, owner);
		ledger.settle(body, false);
		assertEquals(SurrogateLedger.Verdict.RELEASE, ledger.verdict(body));
		assertEquals(owner, ledger.owner(body));
	}
}
