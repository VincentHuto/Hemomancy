package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class IchorianSigilOrganicGeometryTest {

	@Test
	void livingStrokeKeepsAuthoredNodesFixedWhileItsBodyWrithes() {
		var start = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 0, 6, 0.1F);
		var middle = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 3, 6, 0.1F);
		var end = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 6, 6, 0.1F);

		assertPoint(start, -1.0D, 0.0D, 0.0D);
		assertPoint(end, 1.0D, 0.0D, 0.0D);
		assertNotEquals(0.0D, middle.z(), 0.00001D, "the vessel body should not remain ruler-straight");
		assertNotEquals(0.0D, middle.y(), 0.00001D, "the vessel should subtly lift and contract");
	}

	@Test
	void deformationStaysSmallEnoughToPreserveTheReadableSigil() {
		for (int step = 0; step <= 8; step++) {
			var point = IchorianSigilOrganicGeometry.sample(
					0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3.0D,
					125.0F, 287L, step, 8, 0.12F);
			double authoredX = 0.0D;
			double authoredY = 0.0D;
			double authoredZ = 3.0D * step / 8.0D;
			assertTrue(Math.sqrt(
					(point.x() - authoredX) * (point.x() - authoredX)
							+ (point.y() - authoredY) * (point.y() - authoredY)
							+ (point.z() - authoredZ) * (point.z() - authoredZ)) <= 0.13D);
			assertTrue(point.halfWidth() > 0.0F);
		}
	}

	@Test
	void nodesBeatAtDifferentPhasesWithoutExtremeScaling() {
		float first = IchorianSigilOrganicGeometry.nodePulse(60.0F, 41L, 0);
		float second = IchorianSigilOrganicGeometry.nodePulse(60.0F, 41L, 1);

		assertNotEquals(first, second);
		assertTrue(first >= 0.88F && first <= 1.12F);
		assertTrue(second >= 0.88F && second <= 1.12F);
	}

	@Test
	void writhingRibbonUsesOneSharedCrossSectionAtEachJoint() {
		List<IchorianSigilOrganicGeometry.Sample> samples = List.of(
				new IchorianSigilOrganicGeometry.Sample(0.0D, 0.0D, 0.0D, 0.1F),
				new IchorianSigilOrganicGeometry.Sample(1.0D, 0.0D, 0.25D, 0.12F),
				new IchorianSigilOrganicGeometry.Sample(2.0D, 0.0D, 0.0D, 0.1F));

		var joints = IchorianSigilOrganicGeometry.ribbonJoints(samples);
		var segments = IchorianSigilOrganicGeometry.ribbonSegments(samples);

		assertEquals(samples.size(), joints.size());
		assertEquals(2, segments.size());
		assertSame(segments.get(0).end(), segments.get(1).start(),
				"neighboring quads must reuse the exact same joint object");
		assertEquals(0.25D, joints.get(1).centerZ(), 0.000001D);
	}

	@Test
	void tubeFramesRemainFiniteAndSharedThroughAThreeDimensionalBend() {
		List<IchorianSigilOrganicGeometry.Sample> samples = List.of(
				new IchorianSigilOrganicGeometry.Sample(0.0D, 0.0D, 0.0D, 0.08F),
				new IchorianSigilOrganicGeometry.Sample(0.0D, 0.7D, 0.1D, 0.09F),
				new IchorianSigilOrganicGeometry.Sample(0.4D, 1.1D, 0.2D, 0.07F));

		var frames = IchorianSigilOrganicGeometry.tubeFrames(samples);

		assertEquals(samples.size(), frames.size());
		assertTrue(Double.isFinite(frames.get(1).sideX()));
		assertTrue(Double.isFinite(frames.get(1).verticalY()));
		assertEquals(samples.get(1).x(), frames.get(1).centerX(), 0.0D);
		assertEquals(samples.get(1).halfWidth(), frames.get(1).radius(), 0.0F);
	}

	@Test
	void vesselTaperAndHeartbeatStayBoundedAndContinuousAtLandmarks() {
		float beat = IchorianSigilOrganicGeometry.heartbeat(40.0F);
		float start = IchorianSigilOrganicGeometry.vesselWidth(0.1F, 0.0F, beat, 0.4F);
		float middle = IchorianSigilOrganicGeometry.vesselWidth(0.1F, 0.5F, beat, 0.4F);
		float end = IchorianSigilOrganicGeometry.vesselWidth(0.1F, 1.0F, beat, 0.4F);

		assertEquals(start, end, 0.000001F);
		assertTrue(start >= 0.04F && start <= 0.08F);
		assertTrue(middle >= start && middle <= 0.13F);
	}

	@Test
	void travellingBolusMovesWithoutChangingItsStableSeedPhase() {
		float first = IchorianSigilOrganicGeometry.bolusPosition(10.0F, 91L);
		float later = IchorianSigilOrganicGeometry.bolusPosition(15.0F, 91L);

		assertNotEquals(first, later);
		assertEquals(first, IchorianSigilOrganicGeometry.bolusPosition(10.0F, 91L), 0.0F);
		assertTrue(IchorianSigilOrganicGeometry.bolusIntensity(first, first) > 0.9F);
		assertTrue(IchorianSigilOrganicGeometry.bolusIntensity(1.0F - first, first) >= 0.0F);
		assertTrue(IchorianSigilOrganicGeometry.bolusIntensity(1.0F - first, first) <= 1.0F);
	}

	private static void assertPoint(IchorianSigilOrganicGeometry.Sample point,
			double x, double y, double z) {
		assertEquals(x, point.x(), 0.000001D);
		assertEquals(y, point.y(), 0.000001D);
		assertEquals(z, point.z(), 0.000001D);
	}
}
