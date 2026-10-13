package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanZoomStateFocusTest {

	@Test
	void focusPutsTheHubAtTheViewportCentre() {
		PanZoomState view = new PanZoomState();
		view.focusOn(480, 480, 170, 928, 508);

		assertEquals(464, view.sx(0, 480), 1);
		assertEquals(254, view.sy(0, 480), 1);
	}

	@Test
	void fitZoomNeverDropsBelowTheIconThresholdOnSmallViewports() {
		assertEquals(0.5F, PanZoomState.fitZoom(270, 448, 238), 1.0E-4F);

		PanZoomState view = new PanZoomState();
		view.focusOn(480, 480, 270, 448, 238);
		assertEquals(0.5F, view.zoom, 1.0E-4F);
		assertEquals(224, view.sx(0, 480), 1);
		assertEquals(119, view.sy(0, 480), 1);
	}

	@Test
	void layerSwitchCentringKeepsItsZoomClearOfTheDeepExitThreshold() {
		PanZoomState view = new PanZoomState();
		view.centreHub(480, 480, 1.0F, 448, 238);

		assertEquals(1.0F, view.zoom, 1.0E-4F);
		assertEquals(224, view.sx(0, 480), 1);
		assertEquals(119, view.sy(0, 480), 1);
	}

	@Test
	void fitZoomNeverMagnifiesBeyondNaturalSize() {
		assertEquals(1.0F, PanZoomState.fitZoom(40, 928, 508), 1.0E-4F);
	}

	@Test
	void fitZoomShrinksToShowTheRequestedRadius() {
		// 508 / (2 * (170 + 24)) = 1.309 -> clamped to 1.0; 508 / (2 * (270 + 24)) = 0.864
		assertEquals(0.864F, PanZoomState.fitZoom(270, 928, 508), 1.0E-3F);
	}
}
