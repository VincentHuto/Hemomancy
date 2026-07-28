package com.vincenthuto.hemomancy.common.rite.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CardinalRiteInteractionMarkerTest {
	@Test
	void interactiveMarkersLayerLargeGlowOverBloodCells() {
		assertEquals(List.of(
				CardinalRiteInteractionMarker.Layer.BLOOD_CELL,
				CardinalRiteInteractionMarker.Layer.GLOW),
				CardinalRiteInteractionMarker.layers());
	}
}
