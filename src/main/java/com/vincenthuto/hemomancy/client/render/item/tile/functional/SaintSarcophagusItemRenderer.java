package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.HarbingerSaintSarcophagus;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SaintSarcophagusItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_saint_sarcophagus.png");

	private HarbingerSaintSarcophagus model;

	public SaintSarcophagusItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new HarbingerSaintSarcophagus(
					modelSet.bakeLayer(HarbingerSaintSarcophagus.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new HarbingerSaintSarcophagus(
					modelSet.bakeLayer(HarbingerSaintSarcophagus.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
			poseStack.mulPose(new Quaternion(Vector3.YP, 90, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.ZP, 30, true).toMoj());
			poseStack.translate(-0.5, -0.2, 0);
		}

		poseStack.pushPose();

		if (isGui) {
			poseStack.translate(0.5, 0.85, 0.5);
			poseStack.scale(0.25f, 0.25f, 0.25f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.scale(0.2f, 0.2f, 0.2f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		} else {
			poseStack.translate(0.5, 0.7, 0.5);
			poseStack.scale(0.2f, 0.2f, 0.2f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}
