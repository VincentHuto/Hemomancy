package com.vincenthuto.hemomancy.common.summon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class RingmasterConductorRulesTest {
	@Test
	void activeRingmasterOffsetsOnlyItsOwnSlot() {
		assertEquals(1, RingmasterConductorRules.activeCap(1, false));
		assertEquals(2, RingmasterConductorRules.activeCap(1, true));
	}

	@Test
	void relayRequiresSameOwnerSessionAndThirtyTwoBlockRange() {
		UUID owner = UUID.randomUUID();
		UUID session = UUID.randomUUID();
		assertTrue(RingmasterConductorRules.canRelay(owner, owner, session, session, 32.0D * 32.0D));
		assertFalse(RingmasterConductorRules.canRelay(owner, UUID.randomUUID(), session, session, 1.0D));
		assertFalse(RingmasterConductorRules.canRelay(owner, owner, session, UUID.randomUUID(), 1.0D));
		assertFalse(RingmasterConductorRules.canRelay(owner, owner, session, session, 32.1D * 32.1D));
	}
}
