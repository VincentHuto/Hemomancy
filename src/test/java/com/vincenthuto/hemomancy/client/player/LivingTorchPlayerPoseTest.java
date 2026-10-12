package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingTorchPlayerPoseTest {
	@Test
	void breathMovesTorchToMouthWhileLeavingCenterClear() {
		LivingTorchPlayerPose.BreathPose start = LivingTorchPlayerPose.breath(0.0F, true);
		LivingTorchPlayerPose.BreathPose ready = LivingTorchPlayerPose.breath(1.0F, true);
		assertTrue(ready.armXRot() < start.armXRot());
		assertTrue(Math.abs(ready.firstPersonX()) >= 0.42F,
				"torch remains beside the screen center");
		assertTrue(ready.firstPersonY() > 0.35F,
				"positive render-space Y raises the torch toward the mouth");
		assertTrue(ready.armXRot() > -1.65F && ready.armXRot() < -1.20F,
				"third-person arm raises the torch to mouth height without holding it overhead");
		assertTrue(ready.armXRot() < 0.0F,
				"negative X projects the torch away from the body");
		assertTrue(ready.armZRot() > 0.40F,
				"positive Z rolls the right-hand crown diagonally toward the mouth");
		assertTrue(ready.bodyLean() > 0.0F);
		assertEquals(-ready.armYRot(), LivingTorchPlayerPose.breath(1.0F, false).armYRot(), 0.0001F);
		assertEquals(-ready.armZRot(), LivingTorchPlayerPose.breath(1.0F, false).armZRot(), 0.0001F);
	}
}
