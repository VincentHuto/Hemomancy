package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

final class AvatarHeldItemTransform {
	private AvatarHeldItemTransform() {
	}

	static void apply(PoseStack poseStack, HumanoidArm arm) {
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate((arm == HumanoidArm.LEFT ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
	}

	static ItemDisplayContext displayContext(HumanoidArm arm) {
		return arm == HumanoidArm.LEFT
				? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				: ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
	}
}
