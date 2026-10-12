package com.vincenthuto.hemomancy.client.player;

import net.minecraft.util.Mth;

/** Frame-rate-independent breath pose shared by first- and third-person torch rendering; the jab is a Living Arsenal stroke. */
public final class LivingTorchPlayerPose {
	private LivingTorchPlayerPose() { }

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

	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	public record BreathPose(float armXRot, float armYRot, float armZRot, float bodyLean,
			float firstPersonX, float firstPersonY, float firstPersonZ, float progress) { }
}
