package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LayerViewNavigationTest {
	@Test
	void cyclingLayersUsesTheSameAllTopBottomSequenceEverywhere() {
		assertEquals(3, LayerViewNavigation.cycle(-1, 3, 1));
		assertEquals(-1, LayerViewNavigation.cycle(3, 3, 1));
		assertEquals(0, LayerViewNavigation.cycle(-1, 3, -1));
		assertEquals(-1, LayerViewNavigation.cycle(0, 3, -1));
	}

	@Test
	void singleLayerPreviewsRemainOnAllLayers() {
		assertEquals(-1, LayerViewNavigation.cycle(-1, 0, 1));
		assertEquals(-1, LayerViewNavigation.cycle(0, 0, -1));
	}
}
