package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.PuppeteersSpindleModel;
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

public class PuppeteersSpindleItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/block/polished_venous_stone_bricks.png");
	private static final float GUI_MODEL_PITCH_DEGREES = 198.0F;
	private static final float GUI_MODEL_YAW_DEGREES = -42.0F;
	private static final float GUI_MODEL_ROLL_DEGREES = -8.0F;
	private static final double GUI_MODEL_TRANSLATE_Y = 0.26D;

	private PuppeteersSpindleModel model;

	public PuppeteersSpindleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new PuppeteersSpindleModel(modelSet.bakeLayer(PuppeteersSpindleModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new PuppeteersSpindleModel(modelSet.bakeLayer(PuppeteersSpindleModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		if (isGui) {
			poseStack.translate(0.5D, GUI_MODEL_TRANSLATE_Y, 0.5D);
			poseStack.scale(0.45F, 0.45F, 0.45F);
			poseStack.mulPose(Axis.XP.rotationDegrees(GUI_MODEL_PITCH_DEGREES));
			poseStack.mulPose(Axis.YP.rotationDegrees(GUI_MODEL_YAW_DEGREES));
			poseStack.mulPose(Axis.ZP.rotationDegrees(GUI_MODEL_ROLL_DEGREES));
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5, 0.55, 0.5);
			poseStack.scale(0.35F, 0.35F, 0.35F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		} else {
			poseStack.translate(0.5, 0.48, 0.5);
			poseStack.scale(0.35F, 0.35F, 0.35F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		}

		this.model.setupAnim(0.0F);
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}
