package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteRingPipsTest {
	@Test
	void sleepingRingsDrawAsHollowCircles() {
		assertEquals("◆◇○", CardinalRiteOverlay.pips(1, 2, 3));
		assertEquals("◆◆", CardinalRiteOverlay.pips(2, 2, 2));
		assertEquals("◇○○○", CardinalRiteOverlay.pips(0, 1, 4));
	}
}
