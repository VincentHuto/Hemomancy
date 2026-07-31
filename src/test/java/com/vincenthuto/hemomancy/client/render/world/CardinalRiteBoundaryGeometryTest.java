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
		assertEquals(2.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 1, false),
				"first interactive ring");
		assertEquals(5.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 4, false),
				"fourth interactive ring");
		assertEquals(8.0F,
				CardinalRiteBoundaryGeometry.exteriorRadius(9, 7, false),
				"degree seven outer ring");
		assertEquals(2.0F,
				CardinalRiteBoundaryGeometry.interactiveRingRadius(0),
				"first ring is inset from the former three-block boundary");
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
	void boundaryPlaneClearsTheFloorTopByOneTenthOfABlock() {
		assertEquals(65.1F, CardinalRiteBoundaryGeometry.boundaryPlaneY(64),
				"raised rite plane");
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

	@Test
	void growingConnectionsAdvanceEquallyFromBothSocketEdges() {
		var segment = new CardinalRiteBoundaryProgress.Segment(
				0, 0.0D, Math.PI / 2.0D, 3, 0.6F);
		double clearance = 2.0D * Math.asin(0.34D / 6.0D);
		double availableSweep = Math.PI / 2.0D - clearance * 2.0D;

		var invisible = CardinalRiteBoundaryGeometry.animatedSocketArcs(
				segment, 0.0F, 3.0F, 0.34F);
		var halfway = CardinalRiteBoundaryGeometry.animatedSocketArcs(
				segment, 0.5F, 3.0F, 0.34F);
		var complete = CardinalRiteBoundaryGeometry.animatedSocketArcs(
				segment, 1.0F, 3.0F, 0.34F);

		assertEquals(0, invisible.size(), "zero-growth arc count");
		assertEquals(2, halfway.size(), "half-growth arc count");
		assertNear(clearance, halfway.get(0).startAngle(), "first endpoint inset");
		assertNear(availableSweep * 0.25D, halfway.get(0).sweepAngle(), "first endpoint growth");
		assertNear(Math.PI / 2.0D - clearance - availableSweep * 0.25D,
				halfway.get(1).startAngle(), "second endpoint growth");
		assertNear(0.6D, halfway.get(1).integrity(), "integrity retained");
		assertEquals(1, complete.size(), "complete arc count");
		assertNear(clearance, complete.get(0).startAngle(), "complete start inset");
		assertNear(availableSweep, complete.get(0).sweepAngle(), "complete clipped sweep");
	}

	@Test
	void socketClippingPreservesWrappedAuthoredAngles() {
		var wrapped = new CardinalRiteBoundaryProgress.Segment(
				1, 6.1D, 0.6D, 7, 1.0F);
		double clearance = 2.0D * Math.asin(0.34D / 8.0D);

		var arcs = CardinalRiteBoundaryGeometry.animatedSocketArcs(
				wrapped, 1.0F, 4.0F, 0.34F);

		assertEquals(1, arcs.size(), "wrapped arc count");
		assertNear(6.1D + clearance, arcs.get(0).startAngle(), "wrapped authored start");
		assertNear(0.6D - clearance * 2.0D, arcs.get(0).sweepAngle(), "wrapped clipped sweep");
	}

	@Test
	void floorStainAppearsOnlyAfterTheConnectionIsComplete() {
		var segment = new CardinalRiteBoundaryProgress.Segment(
				0, 0.0D, Math.PI / 2.0D, 3, 0.6F);
		double clearance = 2.0D * Math.asin(0.34D / 6.0D);

		var growing = CardinalRiteBoundaryGeometry.completedSocketArc(
				segment, 0.99F, 3.0F, 0.34F);
		var complete = CardinalRiteBoundaryGeometry.completedSocketArc(
				segment, 1.0F, 3.0F, 0.34F);

		assertTrue(growing.isEmpty(), "growing connection has no floor stain");
		assertTrue(complete.isPresent(), "completed connection has a floor stain");
		assertNear(clearance, complete.orElseThrow().startAngle(),
				"completed stain begins outside the socket");
		assertNear(Math.PI / 2.0D - clearance * 2.0D,
				complete.orElseThrow().sweepAngle(),
				"completed stain ends outside the other socket");
	}

	@Test
	void completedBoundaryStainRetainsAHighContrastBlackBand() {
		assertTrue(CardinalRiteBoundaryGeometry.boundaryStainWidth() >= 0.75F,
				"black boundary stain remains substantially wider than the glowing core");
		assertTrue(CardinalRiteBoundaryGeometry.boundaryStainAlpha(0) >= 0.22F,
				"innermost black boundary stain remains visibly dark");
		assertTrue(CardinalRiteBoundaryGeometry.boundaryStainAlpha(3) >= 0.15F,
				"outer black boundary stain retains contrast");
	}

	@Test
	void socketStainExtendsPastTheSocketWithAnOpaqueBlackEdge() {
		float intensity = CardinalRiteBoundaryGeometry.socketIntensity(0.0F);
		float boundaryHalfWidth = CardinalRiteBoundaryGeometry.boundaryStainWidth() * 0.5F;
		float minimumJunctionCoverage = (float) Math.hypot(0.34F, boundaryHalfWidth);

		assertTrue(CardinalRiteBoundaryGeometry.socketStainOuterRadius(0.34F)
						> minimumJunctionCoverage,
				"black socket stain overlaps the full boundary backing at its junction");
		assertTrue(CardinalRiteBoundaryGeometry.socketStainInnerAlpha(intensity) >= 0.20F,
				"empty socket center retains a dark black stain");
		assertTrue(CardinalRiteBoundaryGeometry.socketStainEdgeAlpha(intensity) >= 0.07F,
				"black socket outline remains visible at its outer edge");
	}

	@Test
	void socketStainFeathersToTransparencyAtItsOuterEdge() {
		float socketRadius = 0.34F;
		float intensity = CardinalRiteBoundaryGeometry.socketIntensity(0.0F);
		float featherStart =
				CardinalRiteBoundaryGeometry.socketStainFeatherStartRadius(socketRadius);
		float outerRadius =
				CardinalRiteBoundaryGeometry.socketStainOuterRadius(socketRadius);

		assertTrue(featherStart > socketRadius,
				"dark stain body fully backs the red socket");
		assertTrue(featherStart < outerRadius,
				"socket stain reserves an outer feather band");
		assertEquals(CardinalRiteBoundaryGeometry.socketStainEdgeAlpha(intensity),
				CardinalRiteBoundaryGeometry.socketStainFeatherAlpha(
						intensity, socketRadius, featherStart),
				"feather begins at the authored dark edge alpha");
		float halfwayAlpha = CardinalRiteBoundaryGeometry.socketStainFeatherAlpha(
				intensity, socketRadius, (featherStart + outerRadius) * 0.5F);
		assertTrue(halfwayAlpha > 0.0F
						&& halfwayAlpha
						< CardinalRiteBoundaryGeometry.socketStainEdgeAlpha(intensity),
				"socket stain fades smoothly through the feather");
		assertEquals(0.0F,
				CardinalRiteBoundaryGeometry.socketStainFeatherAlpha(
						intensity, socketRadius, outerRadius),
				"socket stain reaches full transparency at its outer edge");
	}

	@Test
	void boundaryStainStopsAtTheAnchorStainInsteadOfOverlappingIt() {
		double redClearance = CardinalRiteBoundaryGeometry.socketClearanceAngle(
				3.0F, 3.0F, 0.34F);
		double stainClearance = CardinalRiteBoundaryGeometry.socketStainClearanceAngle(
				3.0F, 3.0F, 0.34F);

		assertTrue(stainClearance > redClearance,
				"black boundary backing is clipped at the wider anchor stain");
		assertNear(
				CardinalRiteBoundaryGeometry.socketClearanceAngle(
						3.0F, 3.0F,
						CardinalRiteBoundaryGeometry.socketStainOuterRadius(0.34F)),
				stainClearance, "stain layers meet at the same radius");
	}

	@Test
	void boundaryStainIsMaskedOnlyWhereTheAnchorStainOverlapsIt() {
		float radius = CardinalRiteBoundaryGeometry.socketStainOuterRadius(0.34F);
		float feather = 0.04F;

		assertEquals(0.0F,
				CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
						3.0F, 0.0F, 3.0F, 0.0F, radius, feather),
				"boundary stain is removed inside the anchor stain");
		assertEquals(0.0F,
				CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
						3.0F + radius, 0.0F, 3.0F, 0.0F, radius, feather),
				"boundary stain stops at the anchor stain edge");
		float feathered = CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
				3.0F + radius + feather * 0.5F, 0.0F,
				3.0F, 0.0F, radius, feather);
		assertTrue(feathered > 0.0F && feathered < 1.0F,
				"boundary stain receives a narrow antialiased edge outside the socket");
		assertEquals(1.0F,
				CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
						3.0F + radius + feather, 0.0F,
						3.0F, 0.0F, radius, feather),
				"boundary stain remains unchanged outside the anchor stain");
	}

	@Test
	void anchorStainUsesADistinctDepthLayerAboveTheBoundaryStain() {
		float boundaryOffset = CardinalRiteBoundaryGeometry.boundaryStainSurfaceOffset();
		float anchorOffset = CardinalRiteBoundaryGeometry.socketStainSurfaceOffset();

		assertTrue(boundaryOffset > 0.0F,
				"boundary stain remains above the shared rite plane");
		assertTrue(anchorOffset > boundaryOffset,
				"anchor fan cannot z-fight with the boundary backing");
		assertTrue(anchorOffset < CardinalRiteBoundaryGeometry.surfaceSafeOffset(-0.012F),
				"both stain layers remain beneath the lowest animated red boundary");
	}

	@Test
	void stainOpacityUsesASmoothFadeInsteadOfAppearingAtFullStrength() {
		assertEquals(0.0F, CardinalRiteBoundaryGeometry.stainOpacity(0.0F),
				"stain is transparent on its first frame");
		float halfway = CardinalRiteBoundaryGeometry.stainOpacity(0.5F);
		assertTrue(halfway > 0.0F && halfway < 1.0F,
				"stain is partially visible during its fade");
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.stainOpacity(1.0F),
				"stain reaches authored opacity");
	}

	@Test
	void boundaryUndulationRetainsClearanceAboveTheFloor() {
		assertEquals(0.013F, CardinalRiteBoundaryGeometry.surfaceSafeOffset(-0.012F),
				"lowest ring wave remains above the floor");
		assertEquals(0.025F, CardinalRiteBoundaryGeometry.surfaceSafeOffset(0.0F),
				"flat boundary receives surface clearance");
		assertEquals(0.037F, CardinalRiteBoundaryGeometry.surfaceSafeOffset(0.012F),
				"positive wave keeps its full motion");
	}

	@Test
	void unifiedSocketThroatUnderlapsTheVisibleBand() {
		assertEquals(0.3125F,
				CardinalRiteBoundaryGeometry.socketThroatRadius(0.34F, 0.055F),
				"boundary reaches beneath the socket band so angled joins cannot crack");
	}

	@Test
	void diagonalSocketUsesItsActualCircleIntersection() {
		float ringRadius = 3.0F;
		float socketRadius = 0.34F;
		var junction = CardinalRiteBoundaryGeometry.socketJunction(
				ringRadius, 2.0F, 2.0F, socketRadius, 1);

		assertNear(ringRadius, Math.hypot(junction.x(), junction.z()),
				"junction remains on the boundary circle");
		assertNear(socketRadius,
				Math.hypot(junction.x() - 2.0D, junction.z() - 2.0D),
				"junction also lies on the offset socket circle");
		assertNear(junction.socketAngle(),
				Math.atan2(junction.z() - 2.0D, junction.x() - 2.0D),
				"socket gate faces the real junction instead of an assumed tangent");
	}

	@Test
	void offsetSocketBodyOpensAroundItsTwoRealJunctions() {
		var first = CardinalRiteBoundaryGeometry.socketJunction(
				3.0F, 2.0F, 2.0F, 0.34F, -1);
		var second = CardinalRiteBoundaryGeometry.socketJunction(
				3.0F, 2.0F, 2.0F, 0.34F, 1);
		var body = CardinalRiteBoundaryGeometry.socketBodyArcsBetweenGates(
				first.socketAngle(), second.socketAngle(), 0.12D);

		assertEquals(2, body.size(), "two complementary socket body arcs");
		assertNear(CardinalRiteBoundaryGeometry.normalizeRadians(
						first.socketAngle() + 0.12D),
				CardinalRiteBoundaryGeometry.normalizeRadians(
						body.getFirst().startAngle()),
				"first body begins after the real first gate");
		assertTrue(body.stream().allMatch(arc -> arc.sweepAngle() > 0.0D),
				"offset gates do not create crossed body arcs");
	}

	@Test
	void connectionSupportsDifferentClearanceAtEachEndpoint() {
		var segment = new CardinalRiteBoundaryProgress.Segment(
				0, 0.0D, Math.PI / 2.0D, 3, 0.6F);

		var complete = CardinalRiteBoundaryGeometry.animatedSocketArcs(
				segment, 1.0F, 0.12D, 0.08D);

		assertEquals(1, complete.size(), "complete asymmetric arc count");
		assertNear(0.12D, complete.getFirst().startAngle(),
				"start uses its own socket clearance");
		assertNear(Math.PI / 2.0D - 0.20D, complete.getFirst().sweepAngle(),
				"end uses its own socket clearance");
	}

	@Test
	void socketBodyLeavesTwoExactGatesForBoundaryNecks() {
		double halfGate = 0.35D;
		var arcs = CardinalRiteBoundaryGeometry.socketBodyArcs(0.0D, halfGate);

		assertEquals(2, arcs.size(), "two socket body arcs");
		assertNear(halfGate, arcs.get(0).startAngle(), "first body start");
		assertNear(Math.PI - halfGate * 2.0D, arcs.get(0).sweepAngle(),
				"first body sweep");
		assertNear(Math.PI + halfGate, arcs.get(1).startAngle(), "second body start");
		assertNear(Math.PI - halfGate * 2.0D, arcs.get(1).sweepAngle(),
				"second body sweep");
	}

	@Test
	void socketOverlayStaysContinuousAcrossBoundaryConnections() {
		var arcs = CardinalRiteBoundaryGeometry.socketOverlayArcs();

		assertEquals(1, arcs.size(), "continuous socket arc count");
		assertNear(0.0D, arcs.getFirst().startAngle(), "continuous socket start");
		assertNear(Math.PI * 2.0D, arcs.getFirst().sweepAngle(),
				"continuous socket covers both connection seams");
	}

	@Test
	void boundaryTessellationPreservesExactJunctionEndpoints() {
		var pieces = CardinalRiteBoundaryGeometry.tessellateArc(
				0.13D, 0.90D, 96);

		assertNear(0.13D, pieces.getFirst().startAngle(), "exact arc start");
		var last = pieces.getLast();
		assertNear(1.03D, last.startAngle() + last.sweepAngle(), "exact arc end");
		assertTrue(pieces.stream().allMatch(piece ->
				piece.sweepAngle() <= Math.PI * 2.0D / 96.0D + 0.0001D),
				"pieces retain the original ring resolution");
	}

	@Test
	void boundaryUndulationFadesOutAtSocketThroats() {
		var segment = new CardinalRiteBoundaryProgress.Segment(
				0, 0.0D, Math.PI / 2.0D, 3, 1.0F);
		double clearance = 2.0D * Math.asin(0.34D / 6.0D);
		double start = clearance;
		double end = Math.PI / 2.0D - clearance;
		double fadeAngle = 0.20D;

		assertNear(0.0D, CardinalRiteBoundaryGeometry.socketEndpointWaveScale(
				start, List.of(segment), 3.0F, 0.34F, fadeAngle),
				"first socket throat is pinned");
		assertNear(0.0D, CardinalRiteBoundaryGeometry.socketEndpointWaveScale(
				end, List.of(segment), 3.0F, 0.34F, fadeAngle),
				"second socket throat is pinned");
		assertNear(0.5D, CardinalRiteBoundaryGeometry.socketEndpointWaveScale(
				start + fadeAngle * 0.5D, List.of(segment),
				3.0F, 0.34F, fadeAngle),
				"undulation eases smoothly away from the throat");
		assertNear(1.0D, CardinalRiteBoundaryGeometry.socketEndpointWaveScale(
				start + fadeAngle, List.of(segment),
				3.0F, 0.34F, fadeAngle),
				"the rest of the boundary keeps its full undulation");
	}

	@Test
	void boundaryWaveCanUseAuthoredOffsetSocketEndpoints() {
		assertNear(0.0D, CardinalRiteBoundaryGeometry.endpointWaveScale(
				1.2D, List.of(1.2D, 2.4D), 0.20D),
				"offset socket endpoint is pinned");
		assertNear(0.5D, CardinalRiteBoundaryGeometry.endpointWaveScale(
				1.3D, List.of(1.2D, 2.4D), 0.20D),
				"wave eases away from the actual intersection");
		assertNear(1.0D, CardinalRiteBoundaryGeometry.endpointWaveScale(
				1.4D, List.of(1.2D, 2.4D), 0.20D),
				"full wave resumes outside the junction");
	}

	@Test
	void boundaryRadiusTracksShiftedAnchorCenters() {
		assertNear(Math.sqrt(8.0D),
				CardinalRiteBoundaryGeometry.anchorAlignedRingRadius(
						3.0F, List.of(
								new BlockPos(2, 0, 2),
								new BlockPos(-2, 0, 2),
								new BlockPos(-2, 0, -2),
								new BlockPos(2, 0, -2))),
				"diagonal boundary passes through all authored socket centers");
		assertNear(3.0D, CardinalRiteBoundaryGeometry.anchorAlignedRingRadius(
				3.0F, List.of()), "ring without authored sockets keeps its nominal radius");
	}

	@Test
	void socketUndulationFadesOutAtNeckGates() {
		double tangent = 0.7D;
		double halfGate = 0.38D;
		double gate = tangent + halfGate;

		assertNear(0.0D, CardinalRiteBoundaryGeometry.socketGateWaveScale(
				gate, tangent, halfGate, 0.16D), "socket gate is pinned");
		assertNear(0.5D, CardinalRiteBoundaryGeometry.socketGateWaveScale(
				gate + 0.08D, tangent, halfGate, 0.16D),
				"socket wave eases away from its gate");
		assertNear(1.0D, CardinalRiteBoundaryGeometry.socketGateWaveScale(
				gate + 0.16D, tangent, halfGate, 0.16D),
				"socket body retains its full wave");
	}

	@Test
	void offsetSocketUndulationPinsBothNonOppositeGates() {
		double firstGate = -0.28D;
		double secondGate = 1.86D;

		assertNear(0.0D, CardinalRiteBoundaryGeometry.socketGateWaveScale(
				firstGate + 0.38D, firstGate, secondGate,
				0.38D, 0.16D), "first offset gate edge is pinned");
		assertNear(0.0D, CardinalRiteBoundaryGeometry.socketGateWaveScale(
				secondGate - 0.38D, firstGate, secondGate,
				0.38D, 0.16D), "second offset gate edge is pinned");
	}

	@Test
	void interiorTeethBeginAtTheInnerBoundaryEdge() {
		assertEquals(2.955F, CardinalRiteBoundaryGeometry.veinRootRadius(
				3.0F, 0.09F, 0.0F),
				"flat tooth root clears the boundary core");
		assertEquals(2.985F, CardinalRiteBoundaryGeometry.veinRootRadius(
				3.0F, 0.09F, 0.03F),
				"undulation moves the inset tooth root");
	}

	@Test
	void interiorTeethStayAboveTheirAnimatedBoundaryRoot() {
		assertEquals(0.019F, CardinalRiteBoundaryGeometry.veinSurfaceOffset(0.013F),
				"tooth above lowest boundary wave");
		assertEquals(0.031F, CardinalRiteBoundaryGeometry.veinSurfaceOffset(0.025F),
				"tooth above flat boundary");
		assertEquals(0.043F, CardinalRiteBoundaryGeometry.veinSurfaceOffset(0.037F),
				"tooth above highest boundary wave");
	}

	@Test
	void boundaryAnchorGlowStaysInsideItsSocket() {
		assertEquals(0.19F, CardinalRiteBoundaryGeometry.landmarkRenderRadius(
				0.19F, true, false), "boundary core radius");
		assertEquals(0.19F, CardinalRiteBoundaryGeometry.landmarkRenderRadius(
				0.19F, true, true), "boundary glow does not create a separate shell");
		assertEquals(0.28F, CardinalRiteBoundaryGeometry.landmarkRenderRadius(
				0.19F, false, true), "sigil landmarks retain their independent glow");
	}

	@Test
	void anchorSocketsBrightenAsTheSanguineBlobFillsTheirGap() {
		assertEquals(0.35F, CardinalRiteBoundaryGeometry.socketIntensity(0.0F),
				"empty socket intensity");
		assertEquals(0.675F, CardinalRiteBoundaryGeometry.socketIntensity(0.095F),
				"half-filled socket intensity");
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.socketIntensity(0.19F),
				"full socket intensity");
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.socketIntensity(0.30F),
				"socket intensity clamp");
	}

	@Test
	void sealAndBolusTimingCreatesDistinctCompletionBeats() {
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.sealPulseAlpha(0.0F),
				"seal begins bright");
		assertEquals(0.25F, CardinalRiteBoundaryGeometry.sealPulseAlpha(5.0F),
				"seal fades quadratically");
		assertEquals(0.0F, CardinalRiteBoundaryGeometry.sealPulseAlpha(10.0F),
				"seal expires");
		assertEquals(0.5F, CardinalRiteBoundaryGeometry.sealTravel(4.0F),
				"seal fronts travel toward anchors");
		assertEquals(0.0F, CardinalRiteBoundaryGeometry.bolusProgress(3.0F),
				"bolus begins after seal");
		assertEquals(0.5F, CardinalRiteBoundaryGeometry.bolusProgress(10.0F),
				"bolus crosses half the connection");
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.bolusProgress(17.0F),
				"bolus reaches both anchors");
		assertEquals(0.0F, CardinalRiteBoundaryGeometry.bolusAlpha(3.0F),
				"bolus fades in from midpoint");
		assertEquals(1.0F, CardinalRiteBoundaryGeometry.bolusAlpha(10.0F),
				"bolus peaks halfway");
		assertEquals(0.0F, CardinalRiteBoundaryGeometry.bolusAlpha(17.0F),
				"bolus fades at anchors");
	}

	@Test
	void unstableSocketsTwitchWhileHealthySocketsRemainCentered() {
		var healthy = CardinalRiteBoundaryGeometry.socketDistortion(
				1.2D, 30.0F, 2, 1.0F);
		var damaged = CardinalRiteBoundaryGeometry.socketDistortion(
				1.2D, 30.0F, 2, 0.0F);
		var later = CardinalRiteBoundaryGeometry.socketDistortion(
				1.2D, 31.0F, 2, 0.0F);

		assertNear(0.0D, healthy.offsetX(), "healthy socket x offset");
		assertNear(0.0D, healthy.offsetZ(), "healthy socket z offset");
		assertNear(1.0D, healthy.radialScale(), "healthy socket scale");
		assertTrue(Math.abs(damaged.offsetX()) <= 0.061D, "damaged x offset bounded");
		assertTrue(Math.abs(damaged.offsetZ()) <= 0.061D, "damaged z offset bounded");
		assertTrue(damaged.radialScale() >= 0.84F && damaged.radialScale() <= 1.16F,
				"damaged radial deformation bounded");
		assertTrue(Math.abs(damaged.offsetX() - later.offsetX()) > 0.0001D
				|| Math.abs(damaged.offsetZ() - later.offsetZ()) > 0.0001D,
				"damaged socket twitch changes over time");
	}

	private static void assertEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertNear(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.0001D) {
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
