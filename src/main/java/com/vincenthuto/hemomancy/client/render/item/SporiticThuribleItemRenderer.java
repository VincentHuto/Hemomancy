package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.SporiticThuribleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SporiticThuribleItemRenderer extends BlockEntityWithoutLevelRenderer {
	private SporiticThuribleModel<?> model;

	public SporiticThuribleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new SporiticThuribleModel<>(modelSet.bakeLayer(SporiticThuribleModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
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
			float side = leftHand ? -0.5f : 0.5f;
			poseStack.translate(side * 0.08, 1, 0.16);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(side * -10.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(side * 3.0f));
			poseStack.scale(0.64f, 0.64f, 0.64f);
			if (player != null) {
				HumanoidArm arm = leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
				SporiticThuribleRenderHelper.renderHeld(model, player, arm, stack, poseStack, buffer,
						combinedLight, combinedOverlay, true);
			} else {
				SporiticThuribleRenderHelper.renderStatic(model, stack, poseStack, buffer, combinedLight, combinedOverlay);
			}
		} else if (displayContext == ItemDisplayContext.GUI) {
			poseStack.translate(0.5, 1.0, 0.5);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(25.0f));
			poseStack.scale(0.56f, 0.56f, 0.56f);
			SporiticThuribleRenderHelper.renderStatic(model, stack, poseStack, buffer, combinedLight, combinedOverlay);
		} else {
			poseStack.translate(0.5, 0.8, 0.5);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
			poseStack.scale(0.62f, 0.62f, 0.62f);
			SporiticThuribleRenderHelper.renderStatic(model, stack, poseStack, buffer, combinedLight, combinedOverlay);
		}
		poseStack.popPose();
	}

	private void ensureModel() {
		if (this.model == null) {
			this.model = new SporiticThuribleModel<>(Minecraft.getInstance().getEntityModels()
					.bakeLayer(SporiticThuribleModel.LAYER_LOCATION));
		}
	}
}
