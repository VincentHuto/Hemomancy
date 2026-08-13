package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

public final class RegularPolygonGeometryTest {
	@Test
	void decagonUsesFacetedBoundsForDrawingAndHitTesting() {
		assertTrue("centre is inside", RegularPolygonGeometry.isInside(0.0D, 0.0D, 0, 0, 10, 10));
		assertTrue("top vertex is inside", RegularPolygonGeometry.isInside(0.0D, -10.0D, 0, 0, 10, 10));
		assertFalse("bounding-box corner is outside",
				RegularPolygonGeometry.isInside(9.0D, -9.0D, 0, 0, 10, 10));
		int[] middle = RegularPolygonGeometry.horizontalSpan(0, 10, 10);
		int[] shoulder = RegularPolygonGeometry.horizontalSpan(-8, 10, 10);
		assertTrue("middle is wider than shoulder", middle[1] - middle[0] > shoulder[1] - shoulder[0]);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}
}
