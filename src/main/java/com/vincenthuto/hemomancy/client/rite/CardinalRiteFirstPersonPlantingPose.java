package com.vincenthuto.hemomancy.client.rite;

/** Handed first-person arm angles for the Living Staff planting animation. */
public final class CardinalRiteFirstPersonPlantingPose {
	private static final float SHOULDER_OFFSET_X = 0.48F;
	private static final float MODEL_ARM_PIVOT_X = 5.0F / 16.0F;
	private static final float MODEL_ARM_PIVOT_Y = 2.0F / 16.0F;

	private CardinalRiteFirstPersonPlantingPose() {
	}

	public static float shoulderOffsetX(boolean right) {
		return (right ? 1.0F : -1.0F) * SHOULDER_OFFSET_X;
	}

	public static float armRollDegrees(boolean right, float strikeProgress) {
		float side = right ? 1.0F : -1.0F;
		float strike = Math.max(0.0F, Math.min(1.0F, strikeProgress));
		return side * (68.0F + strike * 8.0F);
	}

	public static float modelPivotCorrectionX(boolean right) {
		return (right ? 1.0F : -1.0F) * MODEL_ARM_PIVOT_X;
	}

	public static float modelPivotCorrectionY() {
		return -MODEL_ARM_PIVOT_Y;
	}

	public static float verticalOffset(float windupProgress, float strikeProgress,
			float recoveryProgress) {
		float windup = Math.max(0.0F, Math.min(1.0F, windupProgress));
		float strike = Math.max(0.0F, Math.min(1.0F, strikeProgress));
		float recovery = Math.max(0.0F, Math.min(1.0F, recoveryProgress));
		return -0.18F + windup * 0.34F - strike * 0.72F + recovery * 0.08F;
	}
}
