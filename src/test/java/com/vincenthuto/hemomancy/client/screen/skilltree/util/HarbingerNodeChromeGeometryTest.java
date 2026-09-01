package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerNodeChromeGeometryTest {
	@Test
	void flatAndDiagonalHexagonEdgesHaveEqualLength() {
		assertTrue(NodeShapeRenderer.isInside(EnumNodeShape.HEXAGON, 18, 0, 0, 0, 18));
		assertTrue(NodeShapeRenderer.isInside(EnumNodeShape.HEXAGON, 9, -16, 0, 0, 18));
		assertFalse(NodeShapeRenderer.isInside(EnumNodeShape.HEXAGON, 10, -16, 0, 0, 18));
		assertFalse(NodeShapeRenderer.isInside(EnumNodeShape.HEXAGON, 0, -17, 0, 0, 18));

		double flatEdgeLength = 18.0D;
		double diagonalEdgeLength = Math.hypot(18 - 9, 16);
		assertTrue(Math.abs(flatEdgeLength - diagonalEdgeLength) < 0.5D);
	}

	@Test
	void regularOctagonClipsItsCornersConsistently() {
		assertArrayEquals(new int[] { -18, 18 }, RegularPolygonGeometry.horizontalSpan(0, 18, 8));
		assertArrayEquals(new int[] { -12, 12 }, RegularPolygonGeometry.horizontalSpan(-13, 18, 8));
		assertTrue(NodeShapeRenderer.isInside(EnumNodeShape.OCTAGON, 12, -13, 0, 0, 18));
		assertFalse(NodeShapeRenderer.isInside(EnumNodeShape.OCTAGON, 13, -13, 0, 0, 18));
	}
}
