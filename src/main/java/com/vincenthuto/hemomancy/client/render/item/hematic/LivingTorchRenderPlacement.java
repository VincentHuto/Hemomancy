package com.vincenthuto.hemomancy.client.render.item.hematic;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** Shared model placement and flame-tip extraction for rendering and breath effects. */
public final class LivingTorchRenderPlacement {
	public static final float BASE_MODEL_SCALE = 0.68F;
	public static final float GUI_MODEL_SCALE = 0.78F;
	public static final float TIP_LOCAL_Y = 27.5F / 16.0F;
	public static final double THIRD_PERSON_TORCH_LIFT = 0.14D;
	public static final double THIRD_PERSON_TORCH_OUTWARD_OFFSET = 0.18D;

	private LivingTorchRenderPlacement() { }

	public static void applyCustomModelTransform(PoseStack poseStack, ItemDisplayContext displayContext) {
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(BASE_MODEL_SCALE, BASE_MODEL_SCALE, BASE_MODEL_SCALE);
		poseStack.translate(-0.14D, 0.08D, 0.18D);
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-32.0F));
			poseStack.translate(0.1D, 0.75D, 0.5D);
			poseStack.scale(BASE_MODEL_SCALE * 0.8F, BASE_MODEL_SCALE * 0.8F, BASE_MODEL_SCALE * 0.8F);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			poseStack.translate(1.0D, 0.5D, -0.22D);
		} else if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
				double side = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0D : 1.0D;
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			poseStack.translate(side * THIRD_PERSON_TORCH_OUTWARD_OFFSET, THIRD_PERSON_TORCH_LIFT, 0.34D);
		}
	}

	public static Vec3 tipFromCurrentPose(PoseStack poseStack, Vec3 cameraPosition) {
		Vector3f renderPosition = poseStack.last().pose()
				.transformPosition(0.0F, TIP_LOCAL_Y, 0.0F, new Vector3f());
		return cameraPosition.add(renderPosition.x(), renderPosition.y(), renderPosition.z());
	}
}
