package com.vincenthuto.hemomancy.client.render.item.tile.crafting;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.tile.crafting.HematicArmatureModel;
import com.vincenthuto.hemomancy.client.render.tile.crafting.HematicArmatureRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HematicArmatureItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final ResourceLocation TEXTURE = HematicArmatureRenderer.TEXTURE;
	private static final float GUI_MODEL_SCALE = 0.13F;
	private static final float GUI_MODEL_PITCH_DEGREES = 198.0F;
	private static final float GUI_MODEL_YAW_DEGREES = -42.0F;
	private static final float GUI_MODEL_ROLL_DEGREES = -8.0F;
	private static final float WORLD_MODEL_SCALE = 0.105F;

	private HematicArmatureModel model;

	public HematicArmatureItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new HematicArmatureModel(
					modelSet.bakeLayer(HematicArmatureModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new HematicArmatureModel(
					modelSet.bakeLayer(HematicArmatureModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		if (isGui) {
			poseStack.translate(0.5D, 0.48D, 0.5D);
			poseStack.scale(GUI_MODEL_SCALE, GUI_MODEL_SCALE, GUI_MODEL_SCALE);
			poseStack.mulPose(Axis.XP.rotationDegrees(GUI_MODEL_PITCH_DEGREES));
			poseStack.mulPose(Axis.YP.rotationDegrees(GUI_MODEL_YAW_DEGREES));
			poseStack.mulPose(Axis.ZP.rotationDegrees(GUI_MODEL_ROLL_DEGREES));
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5D, 0.62D, 0.5D);
			poseStack.scale(WORLD_MODEL_SCALE, WORLD_MODEL_SCALE, WORLD_MODEL_SCALE);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		} else {
			poseStack.translate(0.5D, 0.68D, 0.5D);
			poseStack.scale(WORLD_MODEL_SCALE, WORLD_MODEL_SCALE, WORLD_MODEL_SCALE);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}
