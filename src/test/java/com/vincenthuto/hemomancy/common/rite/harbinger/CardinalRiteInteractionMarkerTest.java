package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardinalRiteInteractionMarkerTest {
	@Test
	void interactiveMarkersLayerLargeGlowOverBloodCells() {
		assertEquals(List.of(
				CardinalRiteInteractionMarker.Layer.BLOOD_CELL,
				CardinalRiteInteractionMarker.Layer.GLOW),
				CardinalRiteInteractionMarker.layers());
	}
}
