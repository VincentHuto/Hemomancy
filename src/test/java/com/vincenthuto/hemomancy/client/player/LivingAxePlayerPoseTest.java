package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingAxePlayerPoseTest {
	@Test
	void firstPersonHandReturnsToItsRestTransformAndMirrorsItsReach() {
		var start = new com.mojang.blaze3d.vertex.PoseStack();
		var end = new com.mojang.blaze3d.vertex.PoseStack();
		LivingAxePlayerPose.firstPerson(start, 0, 0, true);
		LivingAxePlayerPose.firstPerson(end, 1, 0, true);
		assertTrue(start.last().pose().equals(end.last().pose(), .00001F));
		var left = new com.mojang.blaze3d.vertex.PoseStack();
		var right = new com.mojang.blaze3d.vertex.PoseStack();
		LivingAxePlayerPose.firstPerson(left, .4F, 0, false);
		LivingAxePlayerPose.firstPerson(right, .4F, 0, true);
		assertEquals(-left.last().pose().m30(), right.last().pose().m30(), .00001F);
		assertEquals(left.last().pose().m31(), right.last().pose().m31(), .00001F);
		assertEquals(left.last().pose().m32(), right.last().pose().m32(), .00001F);
	}

	@Test
	void overheadSwingBlendsBackToTheIdlePoseWithoutSnapping() {
		var start = LivingAxePlayerPose.swing(0.0F, true);
		var end = LivingAxePlayerPose.swing(1.0F, true);
		assertEquals(0.0F, start.armXRot(), 0.0001F);
		assertEquals(start.armXRot(), end.armXRot(), 0.0001F);
		assertEquals(0.0F, end.weight(), 0.0001F);
		assertTrue(LivingAxePlayerPose.swing(0.99F, true).weight() < 0.03F);
	}

	@Test
	void swingRaisesTheAxeThenCommitsDownward() {
		var start = LivingAxePlayerPose.swing(0.0F, true);
		var raised = LivingAxePlayerPose.swing(0.35F, true);
		var impact = LivingAxePlayerPose.swing(0.72F, true);

		assertEquals(0.0F, start.weight(), 0.0001F);
		assertTrue(raised.armXRot() < -2.0F, "windup should lift the axe above the shoulder");
		assertTrue(impact.armXRot() > raised.armXRot(), "impact should drive the axe downward");
		assertTrue(raised.bodyYRot() < 0.0F, "right-handed windup should twist the torso right");
	}

	@Test
	void leftHandMirrorsTorsoAndArmRoll() {
		var right = LivingAxePlayerPose.swing(0.5F, true);
		var left = LivingAxePlayerPose.swing(0.5F, false);

		assertEquals(-right.bodyYRot(), left.bodyYRot(), 0.0001F);
		assertEquals(-right.armZRot(), left.armZRot(), 0.0001F);
	}
}
