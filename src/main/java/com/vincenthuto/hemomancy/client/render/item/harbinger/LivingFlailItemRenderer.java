package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LivingFlailItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final float GUI_MODEL_SCALE = 0.76F;
	private static final float HELD_MODEL_SCALE = 0.72F;
	private LivingFlailModel<?> model;

	public LivingFlailItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new LivingFlailModel<>(modelSet.bakeLayer(LivingFlailModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ensureModel();

		if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			return;
		}

		poseStack.pushPose();
		if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
			LocalPlayer player = Minecraft.getInstance().player;
			boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			float side = leftHand ? 1.0f : 3f;
			poseStack.translate(side * 0.18, 0.52, .2);
			poseStack.mulPose(Axis.XP.rotationDegrees(-110));
			poseStack.mulPose(Axis.YP.rotationDegrees( 180.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(side * 1.0f));
			poseStack.scale(HELD_MODEL_SCALE, HELD_MODEL_SCALE, HELD_MODEL_SCALE);
			if (player != null) {
				HumanoidArm arm = leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
				LivingFlailRenderHelper.renderHeld(model, player, arm, stack, poseStack, buffer,
						packedLight, packedOverlay, true);
			} else {

				LivingFlailRenderHelper.renderStatic(model, stack, poseStack, buffer, packedLight, packedOverlay);
			}
		} else if (displayContext == ItemDisplayContext.GUI) {
			poseStack.translate(0.5, 0.45, 0.5);
			poseStack.mulPose(Axis.XP.rotationDegrees(0.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(28.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-24.0f));
			poseStack.scale(GUI_MODEL_SCALE*.75f, GUI_MODEL_SCALE*.75f, GUI_MODEL_SCALE*.75f);
			LivingFlailRenderHelper.renderStatic(model, stack, poseStack, buffer, packedLight, packedOverlay);
		} else {
			poseStack.translate(0.5, 0.78, 0.5);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
			poseStack.scale(0.7f, 0.7f, 0.7f);
			LivingFlailRenderHelper.renderStatic(model, stack, poseStack, buffer, packedLight, packedOverlay);
		}
		poseStack.popPose();
	}

	private void ensureModel() {
		if (model == null) {
			model = new LivingFlailModel<>(Minecraft.getInstance().getEntityModels()
					.bakeLayer(LivingFlailModel.LAYER_LOCATION));
		}
	}
}
