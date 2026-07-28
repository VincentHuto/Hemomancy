package com.vincenthuto.hemomancy.client.particle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RitePillarParticleGeometryTest {
	@Test
	void verticalParticleHeightIsIndependentFromItsNarrowWidth() {
		var quad = RitePillarParticleGeometry.quad(0.12F, 2.0F, 0.0F, 1.0F);

		assertEquals(4, quad.size());
		assertEquals(-2.0F, quad.get(0).y(), 0.0001F);
		assertEquals(2.0F, quad.get(1).y(), 0.0001F);
		for (var vertex : quad) {
			assertEquals(0.12F, Math.hypot(vertex.x(), vertex.z()), 0.0001F);
		}
	}

	@Test
	void verticalParticleTurnsAroundTheWorldYAxisToFaceTheCamera() {
		var lookingSouth = RitePillarParticleGeometry.quad(0.1F, 1.0F, 0.0F, 1.0F);
		var lookingEast = RitePillarParticleGeometry.quad(0.1F, 1.0F, 1.0F, 0.0F);

		assertTrue(Math.abs(lookingSouth.get(0).x()) > 0.09F);
		assertEquals(0.0F, lookingSouth.get(0).z(), 0.0001F);
		assertTrue(Math.abs(lookingEast.get(0).z()) > 0.09F);
		assertEquals(0.0F, lookingEast.get(0).x(), 0.0001F);
	}

	@Test
	void ribbonBendsItsCenterlineWithoutBreakingVerticalContinuity() {
		var ribbon = RitePillarParticleGeometry.ribbon(
				0.1F, 2.0F, 0.0F, 1.0F, 0.0F, 4, 0.2F);

		assertEquals(16, ribbon.size());
		assertEquals(-2.0F, ribbon.get(0).y(), 0.0001F);
		assertEquals(2.0F, ribbon.get(13).y(), 0.0001F);
		assertEquals(ribbon.get(1).x(), ribbon.get(4).x(), 0.0001F,
				"adjacent ribbon sections must share their centerline boundary");
		assertTrue(Math.abs(midpointX(ribbon, 4)) > 0.15F,
				"an interior ribbon boundary should visibly bow away from the straight quad");
	}

	@Test
	void changingAnimationPhaseMovesTheRibbon() {
		var firstFrame = RitePillarParticleGeometry.ribbon(
				0.1F, 1.0F, 0.0F, 1.0F, 0.0F, 4, 0.2F);
		var laterFrame = RitePillarParticleGeometry.ribbon(
				0.1F, 1.0F, 0.0F, 1.0F, (float) (Math.PI * 0.5D), 4, 0.2F);

		assertTrue(Math.abs(midpointX(firstFrame, 0) - midpointX(laterFrame, 0)) > 0.15F);
	}

	private static float midpointX(java.util.List<RitePillarParticleGeometry.Vertex> vertices, int start) {
		return (vertices.get(start).x() + vertices.get(start + 3).x()) * 0.5F;
	}
}
