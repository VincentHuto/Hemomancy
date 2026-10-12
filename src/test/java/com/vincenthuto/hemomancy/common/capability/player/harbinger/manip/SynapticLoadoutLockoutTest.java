package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynapticLoadoutLockoutTest {
	@Test
	void lockoutCountsDownAndExpires() {
		UUID player = UUID.randomUUID();
		SynapticLoadoutLockout.start(player, 1_000L);
		assertEquals(200L, SynapticLoadoutLockout.remaining(player, 1_000L));
		assertEquals(50L, SynapticLoadoutLockout.remaining(player, 1_150L));
		assertEquals(0L, SynapticLoadoutLockout.remaining(player, 1_200L));
	}

	@Test
	void aStampFromAnotherWorldsClockIsDiscarded() {
		UUID player = UUID.randomUUID();
		SynapticLoadoutLockout.start(player, 1_000_000L);
		assertEquals(0L, SynapticLoadoutLockout.remaining(player, 5_000L), "a backwards clock must not lock presets");
		assertEquals(0L, SynapticLoadoutLockout.remaining(player, 1_000_000L), "the stale stamp must be dropped");
	}

	@Test
	void logoutClearsOnlyThatPlayer() {
		UUID leaving = UUID.randomUUID();
		UUID staying = UUID.randomUUID();
		SynapticLoadoutLockout.start(leaving, 0L);
		SynapticLoadoutLockout.start(staying, 0L);
		SynapticLoadoutLockout.clear(leaving);
		assertEquals(0L, SynapticLoadoutLockout.remaining(leaving, 10L));
		assertEquals(190L, SynapticLoadoutLockout.remaining(staying, 10L));
	}
}
