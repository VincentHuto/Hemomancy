package com.vincenthuto.hemomancy.client.render.world.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonHazeTest {
	@Test
	void theBoxHoldsTheFigureItsShellAndItsTrail() {
		double[] box = DaemonHaze.box(1.0, 0.0, 0.0);
		assertEquals(1.2, box[0], 1e-9, "half-width: 0.30 of the 4-block height, room for soot and a sideways trail");
		assertEquals(2.64, box[1], 1e-9, "half-height: 0.66 of the height, room for wisps trailing below a rising daemon");
		double[] tiny = DaemonHaze.box(0.1, 0.0, 0.0);
		assertTrue(tiny[0] > 0.0 && tiny[1] > 0.0, "a merging daemon keeps a real box");
	}

	@Test
	void aDaemonLyingAlongItsPathGetsABoxLongEnoughToLieIn() {
		double[] prone = DaemonHaze.box(1.0, Math.PI / 2.0, 0.0);
		assertEquals(2.64, prone[0], 1e-9, "lying along its path it is as long across as it was tall");
		assertEquals(1.2, prone[1], 1e-9, "and only as tall as it was wide");
		double[] tilting = DaemonHaze.box(1.0, Math.PI / 4.0, 0.0);
		assertTrue(tilting[0] > DaemonHaze.box(1.0, Math.PI / 8.0, 0.0)[0] && DaemonHaze.box(1.0, Math.PI / 8.0, 0.0)[0] > 1.2,
				"the box grows across as it tips over");
		double[] diving = DaemonHaze.box(1.0, Math.PI, 0.0);
		assertEquals(2.64, diving[1], 1e-9, "head first straight down it is tall again");
	}

	@Test
	void theBoxReachesTheEndOfAStreamingTail() {
		double[] still = DaemonHaze.box(1.0, Math.PI / 2.0, 0.0);
		double[] streaming = DaemonHaze.box(1.0, Math.PI / 2.0, 1.0);
		// Streaming, the teardrop below the chest (0.60 down to its tip at 0.03) stretches TAIL_STRETCH longer.
		double tipFromCentre = 0.5 - (0.60 - 0.57 * (1.0 + DaemonHaze.TAIL_STRETCH));
		assertTrue(streaming[0] >= (tipFromCentre + 0.12) * 4.0, "the tail's wisps and soot stay inside the box");
		assertTrue(streaming[1] > still[1], "its coil swings wider than the body");
	}

	@Test
	void theTiltTurnsTheCrownFromUprightToWhereItPoints() {
		float[] upright = DaemonHaze.tilt(0.0, 1.0, 0.0, 0.0);
		assertEquals(0.0F, upright[3], 1e-6F);
		double yaw = Math.toRadians(30.0);
		double[][] crowns = {{-0.8, 0.6, 0.0}, {0.0, 0.0, 1.0}, {0.3, -0.9, Math.sqrt(1.0 - 0.09 - 0.81)}};
		for (double[] crown : crowns) {
			float[] tilt = DaemonHaze.tilt(crown[0], crown[1], crown[2], yaw);
			assertEquals(0.0F, tilt[1], 1e-6F, "it tips about a level axis");
			assertEquals(1.0, Math.hypot(tilt[0], tilt[2]), 1e-6);
			// Rodrigues: upright turned about the axis by the angle lands on the crown.
			double c = Math.cos(tilt[3]);
			double s = Math.sin(tilt[3]);
			double[] turned = {-tilt[2] * s, c, tilt[0] * s};
			for (int axis = 0; axis < 3; axis++) assertEquals(crown[axis], turned[axis], 1e-5, "axis " + axis);
		}
	}

	@Test
	void headFirstStraightDownItTipsOverItsFront() {
		double yaw = Math.toRadians(90.0); // facing -x
		float[] tilt = DaemonHaze.tilt(0.0, -1.0, 0.0, yaw);
		assertEquals(Math.PI, tilt[3], 1e-6);
		// Turned a little way about that axis, upright leans toward the facing, not sideways.
		double s = Math.sin(0.3);
		assertTrue(-tilt[2] * s < -0.25, "toward -x");
	}

	@Test
	void aCameraAtTheBoxFaceCountsAsInside() {
		assertTrue(DaemonHaze.cameraInside(0.0, 0.0, 0.0, 1.0, 2.0));
		assertTrue(DaemonHaze.cameraInside(1.1, 0.0, 0.0, 1.0, 2.0), "within the margin past a face: still inside");
		assertFalse(DaemonHaze.cameraInside(1.2, 0.0, 0.0, 1.0, 2.0));
		assertFalse(DaemonHaze.cameraInside(0.0, 2.3, 0.0, 1.0, 2.0));
		assertEquals(0.15, DaemonHaze.INSIDE_MARGIN, 1e-9, "the shader uses the same margin");
	}

	@Test
	void theStaffHazeHangsAroundTheCrookNotUpAtTheDaemon() {
		double[] centre = DaemonHaze.staffHazeCentre(10, 64, -3);
		// The planted staff's head, through its model's fixed display transform: centred on the block, ~2.84 up.
		assertEquals(10.47, centre[0], 1e-9, "over the staff's head as drawn, not the model's origin");
		assertEquals(64 + 2.84 + DaemonHaze.STAFF_HAZE_LIFT, centre[1], 1e-9);
		assertEquals(-3 + 0.51, centre[2], 1e-9);
		assertTrue(DaemonHaze.STAFF_HAZE_HALF_WIDTH <= 0.6, "about as wide as the staff's head, not a cloud");
		assertTrue(DaemonHaze.STAFF_HAZE_HALF_HEIGHT <= 0.9, "it stays around the staff instead of climbing to the daemon");
	}

	@Test
	void raysStartAtTheTrueEyeIncludingViewBobbing() {
		org.joml.Matrix4f projection = new org.joml.Matrix4f().perspective((float) Math.toRadians(70), 1.0F, 0.05F, 1000.0F);
		org.joml.Matrix4f identity = new org.joml.Matrix4f();
		org.joml.Matrix4f pose = new org.joml.Matrix4f().translation(-5.0F, 0.0F, -10.0F);

		org.joml.Vector3f eye = DaemonHaze.eyeInBox(projection, identity, pose);
		assertEquals(5.0F, eye.x, 1e-3F);
		assertEquals(0.0F, eye.y, 1e-3F);
		assertEquals(10.0F, eye.z, 1e-3F);

		// Minecraft folds view bobbing into the projection; the eye moves with it, so the volume no longer bounces.
		org.joml.Matrix4f bobbed = new org.joml.Matrix4f(projection).mul(new org.joml.Matrix4f().translation(0.0F, 0.05F, 0.0F));
		assertEquals(-0.05F, DaemonHaze.eyeInBox(bobbed, identity, pose).y, 1e-3F);
	}

	@Test
	void theSmokeTrailsOppositeTheMotionAndIsCapped() {
		float[] trail = DaemonHaze.trail(0.2, -0.1, 0.0);
		assertTrue(trail[0] < 0.0F, "moving +x, the smoke trails toward -x");
		assertTrue(trail[1] > 0.0F, "sinking, the smoke trails above");
		assertEquals(0.0F, trail[2], 1e-6F);
		assertEquals(-0.6F, DaemonHaze.trail(9.0, 0, 0)[0], 1e-6F, "capped");
	}

	@Test
	void eyesOpenOnlyAsEmergenceEnds() {
		assertEquals(0.0F, DaemonHaze.eyesOpen(0.0F), 1e-6F);
		assertEquals(0.0F, DaemonHaze.eyesOpen(0.8F), 1e-6F);
		assertEquals(1.0F, DaemonHaze.eyesOpen(1.0F), 1e-6F);
	}

	@Test
	void theSwallowFlashRisesThenDiesAway() {
		assertEquals(0.0F, DaemonHaze.swallow(-1.0F), 1e-6F);
		float peak = DaemonHaze.swallow(3.0F);
		assertTrue(peak > 0.7F, "bright soon after arrival: " + peak);
		assertTrue(DaemonHaze.swallow(12.0F) < peak);
		assertEquals(0.0F, DaemonHaze.swallow(DaemonHaze.SWALLOW_TICKS), 1e-6F);
	}

	@Test
	void cancellationThinsTheHaze() {
		assertEquals(1.0F, DaemonHaze.strength(1.0, 0.0F), 1e-6F);
		assertEquals(0.25F, DaemonHaze.strength(1.0, 1.0F), 1e-6F);
		assertTrue(DaemonHaze.strength(0.55, 0.0F) < 1.0F, "loose before the seal");
	}

	@Test
	void anOfferingSinksToTheChestAndVanishes() {
		double[] start = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, 0.0F);
		assertEquals(1.0, start[0], 1e-9);
		assertTrue(start[3] > 0.0);
		double[] end = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, DaemonHaze.SWALLOW_TICKS);
		assertEquals(0.0, end[0], 1e-9);
		assertEquals(CardinalRiteVisualRules.bodyY(DaemonHaze.CHEST, 1.0), end[1], 1e-9);
		assertEquals(0.0, end[3], 1e-9, "dissolved");
		assertTrue(DaemonHaze.sink(0, 0, 0, 0.1, 5.0F)[3] > 0.0, "a merging daemon still swallows a visible item");
	}
}
