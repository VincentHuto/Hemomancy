package com.vincenthuto.hemomancy.client.render.item.unstained;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.unstained.AbsolutionDaggerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AbsolutionDaggerItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation DAGGER_TEXTURE =
			Hemomancy.rloc("textures/block/pale_silver_block.png");
	private static final float GUI_MODEL_SCALE = 0.62F;
	private static final double GUI_MODEL_TRANSLATE_X = 0.2D;
	private static final double GUI_MODEL_TRANSLATE_Y = -0.2D;

	private AbsolutionDaggerModel model;

	public AbsolutionDaggerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new AbsolutionDaggerModel(modelSet.bakeLayer(AbsolutionDaggerModel.LAYER_LOCATION));
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
		VertexConsumer vertexConsumer = bufferSource.getBuffer(model().renderType(DAGGER_TEXTURE));
		model().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (gui) {
			Lighting.setupFor3DItems();
		}
	}

	private AbsolutionDaggerModel model() {
		if (this.model == null) {
			this.model = new AbsolutionDaggerModel(
					Minecraft.getInstance().getEntityModels().bakeLayer(AbsolutionDaggerModel.LAYER_LOCATION));
		}
		return this.model;
	}

	private void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

		switch (displayContext) {
			case GUI -> {
				poseStack.translate(GUI_MODEL_TRANSLATE_X, GUI_MODEL_TRANSLATE_Y, 0.0D);
				poseStack.mulPose(Axis.ZP.rotationDegrees(42.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(40.0F));
				poseStack.scale(GUI_MODEL_SCALE, GUI_MODEL_SCALE, GUI_MODEL_SCALE);
			}
			case GROUND -> {
				poseStack.translate(0.0F, -0.08F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(74.0F));
				poseStack.scale(0.38F, 0.38F, 0.38F);
			}
			case FIXED -> {
				poseStack.translate(0.0F, -0.14F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
				poseStack.scale(0.5F, 0.5F, 0.5F);
			}
			case FIRST_PERSON_LEFT_HAND -> applyFirstPersonTransform(poseStack, 1.0F);
			case FIRST_PERSON_RIGHT_HAND -> applyFirstPersonTransform(poseStack, -1.0F);
			case THIRD_PERSON_LEFT_HAND -> applyThirdPersonTransform(poseStack, 1.0F);
			case THIRD_PERSON_RIGHT_HAND -> applyThirdPersonTransform(poseStack, -1.0F);
			default -> {
				poseStack.translate(0.0F, -0.12F, 0.0F);
				poseStack.scale(0.52F, 0.52F, 0.52F);
			}
		}
	}

	private void applyFirstPersonTransform(PoseStack poseStack, float side) {
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
		poseStack.translate(side * 0.34F, -0.24F, -0.6F);
		poseStack.scale(0.48F, 0.48F, 0.48F);
	}

	private void applyThirdPersonTransform(PoseStack poseStack, float side) {
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
		if(side == -1){
			poseStack.translate(side * 0.5F, -0.3F, -0.5F);
		}else{
			poseStack.translate(side * 0.5F, -0.3F, .5F);
		}
		poseStack.scale(0.54F, 0.54F, 0.54F);
	}
}
