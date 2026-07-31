package com.vincenthuto.hemomancy.client.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class CardinalRiteFirstPersonPlantingPoseTest {

	@Test
	void shouldersStayOutsideWhileHandsCrossPastTheCenterStaff() {
		float rightShoulder = CardinalRiteFirstPersonPlantingPose.shoulderOffsetX(true);
		float leftShoulder = CardinalRiteFirstPersonPlantingPose.shoulderOffsetX(false);
		float rightWindup = CardinalRiteFirstPersonPlantingPose.armRollDegrees(true, 0.0F);
		float leftWindup = CardinalRiteFirstPersonPlantingPose.armRollDegrees(false, 0.0F);
		float rightImpact = CardinalRiteFirstPersonPlantingPose.armRollDegrees(true, 1.0F);
		float leftImpact = CardinalRiteFirstPersonPlantingPose.armRollDegrees(false, 1.0F);

		assertEquals(0.48F, rightShoulder, 0.0001F);
		assertEquals(-0.48F, leftShoulder, 0.0001F);
		assertTrue(rightWindup >= 65.0F,
				"the right hand must travel past the center instead of merely leaning inward");
		assertTrue(leftWindup <= -65.0F,
				"the left hand must travel past the center instead of merely leaning inward");
		assertTrue(rightImpact >= 75.0F);
		assertTrue(leftImpact <= -75.0F);
		assertEquals(rightWindup, -leftWindup, 0.0001F);
		assertEquals(rightImpact, -leftImpact, 0.0001F);
	}

	@Test
	void modelBodyOffsetsAreCancelledBeforeRenderingEachArm() {
		assertEquals(5.0F / 16.0F,
				CardinalRiteFirstPersonPlantingPose.modelPivotCorrectionX(true), 0.0001F);
		assertEquals(-5.0F / 16.0F,
				CardinalRiteFirstPersonPlantingPose.modelPivotCorrectionX(false), 0.0001F);
		assertEquals(-2.0F / 16.0F,
				CardinalRiteFirstPersonPlantingPose.modelPivotCorrectionY(), 0.0001F);
	}

	@Test
	void staffRaisesForWindupThenSlamsDownDuringStrike() {
		float resting = CardinalRiteFirstPersonPlantingPose.verticalOffset(0.0F, 0.0F, 0.0F);
		float woundUp = CardinalRiteFirstPersonPlantingPose.verticalOffset(1.0F, 0.0F, 0.0F);
		float impact = CardinalRiteFirstPersonPlantingPose.verticalOffset(1.0F, 1.0F, 0.0F);
		float recovery = CardinalRiteFirstPersonPlantingPose.verticalOffset(1.0F, 1.0F, 1.0F);

		assertEquals(-0.18F, resting, 0.0001F);
		assertEquals(0.16F, woundUp, 0.0001F);
		assertEquals(-0.56F, impact, 0.0001F);
		assertEquals(-0.48F, recovery, 0.0001F);
		assertTrue(woundUp > resting, "windup must lift the staff");
		assertTrue(impact < resting, "impact must drive the staff below its resting position");
	}
}
