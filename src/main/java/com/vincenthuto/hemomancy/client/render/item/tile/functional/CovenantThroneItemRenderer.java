package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.tile.functional.CovenantThroneModel;
import com.vincenthuto.hemomancy.client.render.tile.functional.CovenantThroneRenderer;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CovenantThroneItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE =
			CovenantThroneRenderer.TEXTURE;

	private CovenantThroneModel model;

	private static int packColor(float red, float green, float blue, float alpha) {
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = Mth.clamp((int) (red * 255.0F), 0, 255);
		int g = Mth.clamp((int) (green * 255.0F), 0, 255);
		int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public CovenantThroneItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new CovenantThroneModel(
					modelSet.bakeLayer(CovenantThroneModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new CovenantThroneModel(
					modelSet.bakeLayer(CovenantThroneModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
			poseStack.mulPose(new Quaternion(Vector3.YP, 90, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.ZP, 30, true).toMoj());
			poseStack.translate(-0.5, -0.15, 0);
		}

		poseStack.pushPose();

		if (isGui) {
			poseStack.translate(0.5, 0.7, 0.5);
			poseStack.scale(0.25f, 0.25f, 0.25f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.scale(0.2f, 0.2f, 0.2f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		} else {
			poseStack.translate(0.5, 0.6, 0.5);
			poseStack.scale(0.2f, 0.2f, 0.2f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight,
				OverlayTexture.NO_OVERLAY, packColor(0.05F, 0.02F, 0.02F, 1F));

		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}


