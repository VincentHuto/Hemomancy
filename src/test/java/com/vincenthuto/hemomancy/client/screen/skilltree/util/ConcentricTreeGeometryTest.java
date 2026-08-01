package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ConcentricTreeGeometryTest {
	@Test
	void exposesTheDegreeRingsUsedByTheSkillsCompass() {
		assertArrayEquals(new int[] {72, 120, 170, 220, 270, 320, 370, 420, 470},
				ConcentricTreeGeometry.degreeRingRadii());
		assertEquals(480, ConcentricTreeGeometry.CENTER_X);
		assertEquals(480, ConcentricTreeGeometry.CENTER_Y);
	}

	@Test
	void pointsArePlacedOnTheRequestedRing() {
		ConcentricTreeGeometry.Point point = ConcentricTreeGeometry.pointOnDegreeRing(2, 0.0);
		assertEquals(650, point.x());
		assertEquals(480, point.y());
	}
}
