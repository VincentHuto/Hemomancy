package com.vincenthuto.hemomancy.client.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodBindingTendrilClientStateTest {
	@Test
	void delayedEffectSyncDoesNotRetractBeforeTheBodyCoilForms() {
		var entry = new BloodBindingTendrilClientState.Entry(1, 2, 7L, 0L, 120L);

		assertFalse(entry.updateRetraction(16L, true, false));
		assertFalse(entry.expired(16L));
	}

	@Test
	void observedEffectRemovalStartsTheFullReverseRetraction() {
		var entry = new BloodBindingTendrilClientState.Entry(1, 2, 7L, 0L, 120L);

		assertFalse(entry.updateRetraction(20L, true, true));
		assertTrue(entry.updateRetraction(21L, true, false));
		assertFalse(entry.expired(36L));
		assertTrue(entry.expired(37L));
	}
}
