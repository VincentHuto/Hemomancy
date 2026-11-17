package com.vincenthuto.hemomancy.common.tile;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CL-8: owner and bloodline draw; others only when the server allows; an unowned machine is open. */
class BloodDrawRightsTest {
	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID OTHER = UUID.randomUUID();

	@Test
	void ownerAndKinMayDraw() {
		assertTrue(BloodDrawRights.permits(OWNER, OWNER, false, false));
		assertTrue(BloodDrawRights.permits(OWNER, OTHER, true, false));
	}

	@Test
	void strangersNeedTheServerFlag() {
		assertFalse(BloodDrawRights.permits(OWNER, OTHER, false, false));
		assertTrue(BloodDrawRights.permits(OWNER, OTHER, false, true));
		assertFalse(BloodDrawRights.permits(OWNER, null, false, false));
	}

	@Test
	void anUnownedMachineIsOpen() {
		assertTrue(BloodDrawRights.permits(null, OTHER, false, false));
		assertTrue(BloodDrawRights.permits(null, null, false, false));
	}

	@Test
	void readoutStatesFollowTheBlood() {
		assertEquals(ReservoirReadout.State.LIVING, ReservoirReadout.state(true, 0, 500, 0));
		assertEquals(ReservoirReadout.State.EMPTY, ReservoirReadout.state(false, 0, 0, 1));
		assertEquals(ReservoirReadout.State.FRESH, ReservoirReadout.state(false, 400, 0, 0.8));
		assertEquals(ReservoirReadout.State.TURNING, ReservoirReadout.state(false, 400, 0, 0.2));
		assertEquals(ReservoirReadout.State.CLOTTING, ReservoirReadout.state(false, 400, 30, 0));
		assertEquals(ReservoirReadout.State.CLOTTING, ReservoirReadout.state(false, 0, 30, 0));
	}
}
