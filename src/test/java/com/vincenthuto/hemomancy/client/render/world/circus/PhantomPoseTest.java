package com.vincenthuto.hemomancy.client.render.world.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhantomPoseTest {
	@Test
	void seatedVariesBySeed() {
		var a = PhantomPose.seated(1L, 100.0F, 0.0F, 0.0F);
		var b = PhantomPose.seated(2L, 100.0F, 0.0F, 0.0F);
		assertNotEquals(a, b);
		assertTrue(a.legX() < -1.0F, "seated legs reach forward");
	}

	@Test
	void applauseRaisesArms() {
		var still = PhantomPose.seated(5L, 40.0F, 0.0F, 0.0F);
		var clapping = PhantomPose.seated(5L, 40.0F, 0.0F, 1.0F);
		assertTrue(clapping.rightArmX() < still.rightArmX() - 0.5F);
		assertTrue(clapping.leftArmX() < still.leftArmX() - 0.5F);
	}

	@Test
	void trapezeSwingIsBounded() {
		for (int t = 0; t < 400; t++) {
			double angle = PhantomPose.trapezeSwing(t, 77L);
			assertTrue(Math.abs(angle) <= Math.toRadians(40.0D) + 1.0E-9D, "angle " + angle);
		}
	}

	@Test
	void swingIsFrozenTimeStable() {
		assertEquals(PhantomPose.swinging(1.25F), PhantomPose.swinging(1.25F));
		assertEquals(PhantomPose.trapezeSwing(33.0F, 9L), PhantomPose.trapezeSwing(33.0F, 9L));
		assertEquals(PhantomPose.ringAct(1, 50.0F), PhantomPose.ringAct(1, 50.0F));
	}

	@Test
	void headTurnIsClamped() {
		var turned = PhantomPose.seated(3L, 0.0F, (float) Math.PI, 0.0F);
		assertTrue(Math.abs(turned.headYaw()) <= Math.toRadians(60.0D) + 1.0E-6D);
	}
}
