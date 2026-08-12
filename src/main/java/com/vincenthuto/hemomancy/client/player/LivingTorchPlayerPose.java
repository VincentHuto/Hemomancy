package com.vincenthuto.hemomancy.client.player;

import net.minecraft.util.Mth;

/** Frame-rate-independent pose math shared by first- and third-person torch rendering. */
public final class LivingTorchPlayerPose {
	private LivingTorchPlayerPose() { }

	public static ArmPose jabArm(float swingProgress, boolean rightArm) {
		float swing = Mth.clamp(swingProgress, 0.0F, 1.0F);
		float draw = segment(swing, 0.0F, 0.22F);
		float extend = segment(swing, 0.22F, 0.55F);
		float compress = segment(swing, 0.55F, 0.70F);
		float recovery = segment(swing, 0.70F, 1.0F);
		float weight = swing >= 1.0F ? 0.0F : Math.max(draw, extend * (1.0F - recovery));
		float side = rightArm ? 1.0F : -1.0F;
		float extension = extend * (1.0F - recovery);
		return new ArmPose(
				(-0.72F - draw * 0.22F - extension * 1.02F + compress * 0.14F) * weight,
				side * (-0.12F + extension * 0.18F) * weight,
				side * (0.08F - extension * 0.12F) * weight,
				side * (0.22F - extension * 0.14F),
				0.03F + draw * 0.08F,
				-0.22F - extension * 0.72F + recovery * 0.72F,
				weight);
	}

	public static BreathPose breath(float windupProgress, boolean rightArm) {
		float progress = smooth(Mth.clamp(windupProgress, 0.0F, 1.0F));
		float side = rightArm ? 1.0F : -1.0F;
		return new BreathPose(
				-0.55F - progress * 0.82F,
				side * (-0.08F - progress * 0.28F),
				side * (-0.03F + progress * 0.50F),
				0.13F * progress,
				side * (0.55F - progress * 0.10F),
				0.42F + progress * 0.12F,
				-0.72F,
				progress);
	}

	private static float segment(float value, float start, float end) {
		return smooth(Mth.clamp((value - start) / (end - start), 0.0F, 1.0F));
	}

	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	public record ArmPose(float xRot, float yRot, float zRot, float xOffset,
			float yOffset, float zOffset, float weight) { }

	public record BreathPose(float armXRot, float armYRot, float armZRot, float bodyLean,
			float firstPersonX, float firstPersonY, float firstPersonZ, float progress) { }
}
