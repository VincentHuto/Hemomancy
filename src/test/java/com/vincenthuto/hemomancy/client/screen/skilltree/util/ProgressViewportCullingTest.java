package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressViewportCullingTest {

	@Test
	void includesNodesWhoseBoundsTouchAnyViewportEdge() {
		assertTrue(ProgressViewportCulling.intersects(90, 150, 10, 100, 100, 200, 200));
		assertTrue(ProgressViewportCulling.intersects(210, 150, 10, 100, 100, 200, 200));
		assertTrue(ProgressViewportCulling.intersects(150, 90, 10, 100, 100, 200, 200));
		assertTrue(ProgressViewportCulling.intersects(150, 210, 10, 100, 100, 200, 200));
	}

	@Test
	void rejectsNodesCompletelyOutsideAfterPanOrZoomTransforms() {
		assertFalse(ProgressViewportCulling.intersects(89, 150, 10, 100, 100, 200, 200));
		assertFalse(ProgressViewportCulling.intersects(211, 150, 10, 100, 100, 200, 200));
		assertFalse(ProgressViewportCulling.intersects(150, 89, 10, 100, 100, 200, 200));
		assertFalse(ProgressViewportCulling.intersects(150, 211, 10, 100, 100, 200, 200));
	}

	@Test
	void supportsTheOffsetRitesAndCraftingInspectorViewport() {
		int left = 42;
		int top = 71;
		int right = 614;
		int bottom = 397;

		assertTrue(ProgressViewportCulling.intersects(50, 80, 13, left, top, right, bottom));
		assertFalse(ProgressViewportCulling.intersects(20, 80, 13, left, top, right, bottom));
	}

	@Test
	void transformedNodesFollowPanAndZoomBeforeIntersectionTesting() {
		PanZoomState view = new PanZoomState();
		view.panX = -160.0D;
		view.panY = 30.0D;
		view.zoom = 2.0F;
		int screenX = view.sx(20, 100);
		int screenY = view.sy(40, 50);

		assertTrue(ProgressViewportCulling.intersects(screenX, screenY, view.halfNode(26),
				40, 100, 260, 240));
		view.panX = -400.0D;
		screenX = view.sx(20, 100);

		assertFalse(ProgressViewportCulling.intersects(screenX, screenY, view.halfNode(26),
				40, 100, 260, 240));
	}
}
