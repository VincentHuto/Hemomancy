package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CardinalRiteOverlayGeometryTest {
	@Test
	void instabilityBarEndsAtTheSameRightEdgeAsTheRitualHud() {
		assertEquals(230, CardinalRiteOverlayGeometry.HUD_WIDTH);
		assertEquals(82, CardinalRiteOverlayGeometry.INSTABILITY_BAR_OFFSET);
		assertEquals(148, CardinalRiteOverlayGeometry.instabilityBarWidth());
		assertEquals(CardinalRiteOverlayGeometry.HUD_WIDTH,
				CardinalRiteOverlayGeometry.INSTABILITY_BAR_OFFSET
						+ CardinalRiteOverlayGeometry.instabilityBarWidth());
	}
}
