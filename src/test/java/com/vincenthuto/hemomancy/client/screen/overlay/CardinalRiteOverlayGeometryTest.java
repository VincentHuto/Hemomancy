package com.vincenthuto.hemomancy.client.screen.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	@Test
	void renderedHudIsCompactAtSmallGuiWidths() {
		assertEquals(0.72F, CardinalRiteOverlayGeometry.HUD_SCALE);
		assertEquals(166, CardinalRiteOverlayGeometry.renderedHudWidth());
		assertTrue(CardinalRiteOverlayGeometry.renderedHudWidth() <= 256 * 2 / 3,
				"the rite readout should not dominate a 256-wide GUI");
		assertEquals(356, CardinalRiteOverlayGeometry.virtualScreenWidth(256));
	}
}
