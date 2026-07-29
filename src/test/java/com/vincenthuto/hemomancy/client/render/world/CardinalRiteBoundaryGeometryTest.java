package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class CardinalRiteBoundaryGeometryTest {
	@Test
	void interactiveExteriorBeginsAtOutermostCompletedRing() {
		assertEquals(0.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 0, false),
				"no boundary before first completed ring");
		assertEquals(3.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 1, false),
				"first interactive ring");
		assertEquals(6.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 4, false),
				"fourth interactive ring");
		assertEquals(9.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 7, false),
				"degree seven outer ring");
		assertEquals(3.0F,
				CardinalRiteBoundaryGeometry.interactiveRingRadius(0),
				"first ring clears the three-by-three altar");
	}

	@Test
	void legacyExteriorMatchesRenderedTierBoundary() {
		assertEquals(2.5F,
				CardinalRiteBoundaryGeometry.exteriorRadius(3, 1, true),
				"minor legacy boundary");
		assertEquals(11.5F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 4, true),
				"grand legacy boundary");
	}

	@Test
	void faneExteriorBeginsAtDegreeThree() {
		assertFalse(CardinalRiteBoundaryGeometry.shouldRenderExterior(1), "degree one");
		assertFalse(CardinalRiteBoundaryGeometry.shouldRenderExterior(2), "degree two");
		assertTrue(CardinalRiteBoundaryGeometry.shouldRenderExterior(3), "degree three");
		assertTrue(CardinalRiteBoundaryGeometry.shouldRenderExterior(7), "degree seven");
	}

	@Test
	void boundaryPlaneRemainsOnTheLowRitePlane() {
		assertEquals(64.065F, CardinalRiteBoundaryGeometry.boundaryPlaneY(64), "low rite plane");
	}

	@Test
	void footprintEnclosesDisplacedSigilNodesBeyondTheOuterRing() {
		assertEquals(11.75F, CardinalRiteBoundaryGeometry.footprintRadius(
				List.of(new BlockPos(7, 0, 0)),
				List.of(new BlockPos(11, 0, 0))), "sigil footprint plus marker clearance");
	}

	@Test
	void thornsOnlyRootInsideVisibleBoundaryArcs() {
		var quarterArc = List.of(new CardinalRiteBoundaryProgress.Segment(
				0, -Math.PI / 2.0D, Math.PI / 2.0D));

		assertTrue(CardinalRiteBoundaryGeometry.hasVisibleBeamAt(
				quarterArc, -Math.PI / 4.0D), "thorn over completed quarter");
		assertFalse(CardinalRiteBoundaryGeometry.hasVisibleBeamAt(
				quarterArc, Math.PI / 2.0D), "thorn over missing boundary");
		assertFalse(CardinalRiteBoundaryGeometry.hasVisibleBeamAt(
				List.of(), 0.0D), "thorn with no boundary");
	}

	@Test
	void damagedBoundaryBecomesThinAndDryInsteadOfMerelyTransparent() {
		assertEquals(0.09F, CardinalRiteBoundaryGeometry.integrityWidth(0.09F, 1.0F),
				"healthy wet artery");
		assertTrue(CardinalRiteBoundaryGeometry.integrityWidth(0.09F, 0.2F) < 0.04F,
				"damaged arc narrows");
		assertTrue(CardinalRiteBoundaryGeometry.integrityBrightness(0.2F) < 0.35F,
				"damaged arc darkens");
	}

	@Test
	void arterialHighlightTravelsAndRemainsBounded() {
		double firstPosition = CardinalRiteBoundaryGeometry.arterialHighlightPosition(10.0F, 2);
		double laterPosition = CardinalRiteBoundaryGeometry.arterialHighlightPosition(20.0F, 2);
		float first = CardinalRiteBoundaryGeometry.arterialHighlight(firstPosition, 10.0F, 2);
		float later = CardinalRiteBoundaryGeometry.arterialHighlight(laterPosition, 20.0F, 2);

		assertTrue(first >= 0.0F && first <= 1.0F, "bounded first highlight");
		assertTrue(later >= 0.0F && later <= 1.0F, "bounded later highlight");
		assertTrue(first > 0.99F && later > 0.99F, "computed positions locate the pulse peak");
		assertTrue(Math.abs(firstPosition - laterPosition) > 0.0001D,
				"highlight travels around the ring");
	}

	private static void assertEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean actual, String label) {
		if (!actual) throw new AssertionError(label + " should render the exterior");
	}

	private static void assertFalse(boolean actual, String label) {
		if (actual) throw new AssertionError(label + " should not render the exterior");
	}
}
