package com.vincenthuto.hemomancy.client.render.item.unstained;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.unstained.UnstainedWarhammerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class UnstainedWarhammerItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation WARHAMMER_TEXTURE =
			Hemomancy.rloc("textures/block/pale_silver_bell_body.png");
	private static final float GUI_MODEL_SCALE = 0.46F;
	private static final double GUI_MODEL_TRANSLATE_X = 0.42D;
	private static final double GUI_MODEL_TRANSLATE_Y = -0.44D;

	private UnstainedWarhammerModel model;

	public UnstainedWarhammerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new UnstainedWarhammerModel(modelSet.bakeLayer(UnstainedWarhammerModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		boolean gui = displayContext == ItemDisplayContext.GUI;
		if (gui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		applyDisplayTransform(displayContext, poseStack);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(model().renderType(WARHAMMER_TEXTURE));
		model().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (gui) {
			Lighting.setupFor3DItems();
		}
	}

	private UnstainedWarhammerModel model() {
		if (this.model == null) {
			this.model = new UnstainedWarhammerModel(
					Minecraft.getInstance().getEntityModels().bakeLayer(UnstainedWarhammerModel.LAYER_LOCATION));
		}
		return this.model;
	}

	private void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

		switch (displayContext) {
			case GUI -> {
				poseStack.translate(GUI_MODEL_TRANSLATE_X, GUI_MODEL_TRANSLATE_Y, 0.0D);
				poseStack.mulPose(Axis.ZP.rotationDegrees(38.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(36.0F));
				poseStack.scale(GUI_MODEL_SCALE, GUI_MODEL_SCALE, GUI_MODEL_SCALE);
			}
			case GROUND -> {
				poseStack.translate(0.0F, -0.14F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
				poseStack.scale(0.3F, 0.3F, 0.3F);
			}
			case FIXED -> {
				poseStack.translate(0.0F, -0.22F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
				poseStack.scale(0.4F, 0.4F, 0.4F);
			}
			case FIRST_PERSON_LEFT_HAND -> applyFirstPersonTransform(poseStack, 1.0F);
			case FIRST_PERSON_RIGHT_HAND -> applyFirstPersonTransform(poseStack, -1.0F);
			case THIRD_PERSON_LEFT_HAND -> applyThirdPersonTransform(poseStack, 1.0F);
			case THIRD_PERSON_RIGHT_HAND -> applyThirdPersonTransform(poseStack, -1.0F);
			default -> {
				poseStack.translate(0.0F, -0.2F, 0.0F);
				poseStack.scale(0.42F, 0.42F, 0.42F);
			}
		}
	}

	private void applyFirstPersonTransform(PoseStack poseStack, float side) {
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
		poseStack.translate(side * 0.4F, -0.7F, -0.42F);
		poseStack.scale(0.43F, 0.43F, 0.43F);
	}

	private void applyThirdPersonTransform(PoseStack poseStack, float side) {
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
		if(side == -1){
			poseStack.translate(side * 0.48F, -0.5F, -0.5F);
		}else{
			poseStack.mulPose(Axis.YP.rotationDegrees(side * 180.0F));
			poseStack.translate(side * -0.45F, -0.5F, -0.5F);
		}
		poseStack.scale(0.75F, 0.75F, 0.75F);
	}
}
