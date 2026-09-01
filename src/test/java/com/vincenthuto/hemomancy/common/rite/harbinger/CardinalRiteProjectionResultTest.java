package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteProjectionResultTest {
	@Test
	void recognizedRiteAnchorBlocksFallbackEvenWhenItAcceptsNoBlood() {
		CardinalRiteProjectionResult result = CardinalRiteProjectionResult.handled(0.0D);

		assertTrue(result.handled());
		assertFalse(result.allowsOrdinaryProjection());
		assertEquals(0.0D, result.bloodSpent(), 0.0001D);
	}

	@Test
	void missedRiteTargetAllowsOrdinaryProjection() {
		CardinalRiteProjectionResult result = CardinalRiteProjectionResult.unhandled();

		assertFalse(result.handled());
		assertTrue(result.allowsOrdinaryProjection());
	}
}
