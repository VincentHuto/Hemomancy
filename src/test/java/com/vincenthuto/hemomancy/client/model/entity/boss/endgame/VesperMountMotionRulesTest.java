package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperMountMotionRulesTest {
	@Test
	void tripodLegsAlternate() {
		for (float cycle = 0.0F; cycle < 3.0F; cycle += 0.13F) {
			// Left-front, right-middle and left-rear step together; the other three are half a cycle off.
			float groupA = VesperMountMotionRules.tripodPhase(VesperMountMotionRules.leg(0, true), cycle);
			assertEquals(groupA, VesperMountMotionRules.tripodPhase(VesperMountMotionRules.leg(1, false), cycle), 1.0E-5F);
			assertEquals(groupA, VesperMountMotionRules.tripodPhase(VesperMountMotionRules.leg(2, true), cycle), 1.0E-5F);
			float groupB = VesperMountMotionRules.tripodPhase(VesperMountMotionRules.leg(0, false), cycle);
			float offset = (groupB - groupA + 1.0F) % 1.0F;
			assertEquals(0.5F, offset, 1.0E-4F);
		}
	}

	@Test
	void footLiftsOnlyInSwing() {
		assertTrue(VesperMountMotionRules.footLift(0.25F) > 0.9F);
		assertEquals(0.0F, VesperMountMotionRules.footLift(0.5F), 1.0E-5F);
		assertEquals(0.0F, VesperMountMotionRules.footLift(0.8F), 1.0E-5F);
		assertEquals(0.0F, VesperMountMotionRules.footLift(0.0F), 1.0E-5F);
	}

	@Test
	void overshootSettlesToOne() {
		assertEquals(0.0F, VesperMountMotionRules.overshoot(0.0F), 1.0E-5F);
		assertEquals(1.0F, VesperMountMotionRules.overshoot(1.0F), 1.0E-3F);
		float max = 0.0F;
		for (float t = 0.0F; t <= 1.0F; t += 0.01F) max = Math.max(max, VesperMountMotionRules.overshoot(t));
		assertTrue(max > 1.0F, "a planted joint should overshoot before settling");
	}

	@Test
	void wingSpreadFollowsFlightMode() {
		assertEquals(0.0F, VesperMountMotionRules.wingSpread(
				com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules.FlightMode.GROUNDED, 0.0F), 1.0E-5F);
		assertEquals(1.0F, VesperMountMotionRules.wingSpread(
				com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules.FlightMode.CIRCLING, 5.0F), 1.0E-5F);
		float growing = VesperMountMotionRules.wingSpread(
				com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules.FlightMode.WING_GROWTH, 30.0F);
		assertTrue(growing > 0.0F && growing <= 1.0F);
	}

	@Test
	void stanceDragsTheFootBackAtAConstantRate() {
		assertEquals(-1.0F, VesperMountMotionRules.stepSweep(0.0F), 1.0E-5F);
		assertEquals(1.0F, VesperMountMotionRules.stepSweep(0.5F), 1.0E-5F);
		// Planted feet move back evenly, so they hold against the ground instead of skating.
		float a = VesperMountMotionRules.stepSweep(0.6F) - VesperMountMotionRules.stepSweep(0.65F);
		float b = VesperMountMotionRules.stepSweep(0.85F) - VesperMountMotionRules.stepSweep(0.9F);
		assertEquals(a, b, 1.0E-5F);
		// Continuous across the swing/stance seams.
		assertEquals(VesperMountMotionRules.stepSweep(0.4999F), VesperMountMotionRules.stepSweep(0.5001F), 2.0E-3F);
		assertEquals(VesperMountMotionRules.stepSweep(0.9999F), VesperMountMotionRules.stepSweep(1.0001F), 2.0E-3F);
	}

	@Test
	void footOffsetMovesTheFootWhereAsked() {
		// Re-apply the linearised joint deltas to the leg's lever arms and check the foot lands on target.
		float[] down = VesperMountMotionRules.footOffset(8.0F, 0.0F);
		assertEquals(8.0F, 34.2F * down[0] + 18.3F * down[1], 1.0E-3F);
		assertEquals(0.0F, -30.4F * down[0] - 52.2F * down[1], 1.0E-3F);
		assertEquals(0.0F, down[0] + down[1] + down[2], 1.0E-6F, "the hooked foot stays upright");
		float[] out = VesperMountMotionRules.footOffset(0.0F, 5.0F);
		assertEquals(0.0F, 34.2F * out[0] + 18.3F * out[1], 1.0E-3F);
		assertEquals(5.0F, -30.4F * out[0] - 52.2F * out[1], 1.0E-3F);
		assertTrue(down[0] > 0.0F, "reaching down drops the femur, the same way a lowered lift does");
	}

	@Test
	void restFeetMirrorAndSplayFrontToBack() {
		float[] frontRight = VesperMountMotionRules.restFoot(0, false);
		float[] frontLeft = VesperMountMotionRules.restFoot(0, true);
		float[] rearRight = VesperMountMotionRules.restFoot(2, false);
		assertEquals(frontRight[0], -frontLeft[0], 1.0E-5F);
		assertEquals(frontRight[1], frontLeft[1], 1.0E-5F);
		assertTrue(frontRight[1] < -40.0F && rearRight[1] > 0.0F, "front feet reach forward, rear feet back");
	}

	@Test
	void planeFitRecoversASlope() {
		float[] xs = new float[6];
		float[] zs = new float[6];
		float[] hs = new float[6];
		for (int i = 0; i < 6; i++) {
			float[] foot = VesperMountMotionRules.restFoot(i / 2, i % 2 == 1);
			xs[i] = foot[0] / 16.0F;
			zs[i] = foot[1] / 16.0F;
			hs[i] = 0.25F + 0.1F * xs[i] - 0.3F * zs[i];
		}
		float[] plane = VesperMountMotionRules.fitPlane(xs, zs, hs);
		assertEquals(0.25F, plane[0], 1.0E-4F);
		assertEquals(0.1F, plane[1], 1.0E-4F);
		assertEquals(-0.3F, plane[2], 1.0E-4F);
	}

	@Test
	void followIsFrameRateIndependent() {
		float once = VesperMountMotionRules.follow(2.0F, 1.5F);
		float half = VesperMountMotionRules.follow(1.0F, 1.5F);
		float twice = 1.0F - (1.0F - half) * (1.0F - half);
		assertEquals(once, twice, 1.0E-5F);
		assertEquals(0.0F, VesperMountMotionRules.follow(0.0F, 1.5F), 1.0E-6F);
	}
}
