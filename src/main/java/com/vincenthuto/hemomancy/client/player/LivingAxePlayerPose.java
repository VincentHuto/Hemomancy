package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

public final class LivingAxePlayerPose {
	private LivingAxePlayerPose() { }

	public static AxePose swing(float progress, boolean rightHanded) {
		float swing = Mth.clamp(progress, 0.0F, 1.0F);
		float raised = smooth(swing / 0.38F);
		float drop = smooth((swing - 0.42F) / 0.22F);
		float recovery = 1.0F - smooth((swing - 0.70F) / 0.30F);
		float weight = smooth(swing / 0.16F) * recovery;
		float armX = (-2.65F * raised + 3.05F * drop) * recovery;
		float side = rightHanded ? -1.0F : 1.0F;
		return new AxePose(armX, side * 0.18F * weight, side * 0.22F * weight,
				side * 0.28F * weight, weight);
	}

	/** Replaces vanilla's diagonal sweep before the item model is transformed. */
	public static void firstPerson(PoseStack poses, float progress, float equip, boolean rightHanded) {
		AxePose pose = swing(progress, rightHanded);
		float side = rightHanded ? 1.0F : -1.0F;
		poses.translate(side * (0.56F - 0.18F * pose.weight()), -0.52F - equip * 0.6F,
				-0.72F - 0.14F * pose.weight());
		poses.mulPose(Axis.XP.rotation(pose.armXRot() * 0.72F));
		poses.mulPose(Axis.YP.rotation(pose.armYRot()));
		poses.mulPose(Axis.ZP.rotation(pose.armZRot() * 0.5F));
	}

	private static float smooth(float value) {
		float t = Mth.clamp(value, 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	public record AxePose(float armXRot, float armYRot, float armZRot, float bodyYRot, float weight) { }
}
