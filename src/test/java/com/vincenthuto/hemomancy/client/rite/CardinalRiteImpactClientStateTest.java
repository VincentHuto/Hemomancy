package com.vincenthuto.hemomancy.client.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardinalRiteImpactClientStateTest {
	@Test
	void impactShakeIsSharpAndClearsAfterEightTicks() {
		CardinalRiteImpactClientState state = new CardinalRiteImpactClientState();
		state.start(8, 17);

		assertTrue(state.isActive());
		assertTrue(Math.abs(state.pitchShake(0.0F)) > 0.1F);
		for (int tick = 0; tick < 8; tick++) state.tick();
		assertFalse(state.isActive());
		assertEquals(0.0F, state.pitchShake(0.5F), 0.0001F);
	}

	@Test
	void clearImmediatelyRemovesAnyImpactShake() {
		CardinalRiteImpactClientState state = new CardinalRiteImpactClientState();
		state.start(8, 31);
		state.clear();

		assertFalse(state.isActive());
		assertEquals(0.0F, state.rollShake(0.0F), 0.0001F);
	}
}
