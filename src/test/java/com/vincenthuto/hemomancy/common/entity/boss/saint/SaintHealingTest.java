package com.vincenthuto.hemomancy.common.entity.boss.saint;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The presence heal multipliers: the lowest live entry wins, entries expire, and the session clears them. */
final class SaintHealingTest {
	private static final UUID PLAYER = new UUID(7, 7);

	@AfterEach
	void clear() {
		SaintHealing.clear();
	}

	@Test
	void theLowestLiveEntryWins() {
		assertEquals(1.0F, SaintHealing.multiplier(PLAYER, 0L), "none held");
		SaintHealing.set(PLAYER, 0.6F, 40, 0L);
		SaintHealing.set(PLAYER, 0.8F, 100, 0L);
		assertEquals(0.6F, SaintHealing.multiplier(PLAYER, 10L));
		assertEquals(0.8F, SaintHealing.multiplier(PLAYER, 40L), "the lower entry ran out");
		assertEquals(1.0F, SaintHealing.multiplier(PLAYER, 100L));
	}

	@Test
	void logoutAndTheSessionEndEntries() {
		SaintHealing.set(PLAYER, 0.5F, 40, 0L);
		SaintHealing.end(PLAYER);
		assertEquals(1.0F, SaintHealing.multiplier(PLAYER, 1L));
		SaintHealing.set(PLAYER, 0.5F, 40, 0L);
		SaintHealing.clear();
		assertEquals(1.0F, SaintHealing.multiplier(PLAYER, 1L));
		SaintHealing.set(PLAYER, 0.5F, 0, 0L);
		assertEquals(1.0F, SaintHealing.multiplier(PLAYER, 0L), "a zero-tick entry holds nothing");
		assertTrue(SaintHealing.multiplier(new UUID(1, 1), 0L) == 1.0F);
	}
}
