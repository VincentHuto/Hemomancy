package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SanguineOmenOverlayCleanupTest {
	@Test
	void clearImmediatelyRemovesActiveTint() {
		SanguineOmenOverlayState state = new SanguineOmenOverlayState();
		state.start(8, 0.52F, 17);
		state.clear();

		assertFalse(state.isActive());
		assertEquals(0.0F, state.alpha(0.0F), 0.0001F);
	}
}
