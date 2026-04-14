package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.VisceralMirrorModel;
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

public class VisceralMirrorItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_visceral_mirror.png");

	private VisceralMirrorModel model;

	public VisceralMirrorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new VisceralMirrorModel(
					modelSet.bakeLayer(VisceralMirrorModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new VisceralMirrorModel(
					modelSet.bakeLayer(VisceralMirrorModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();

		if (isGui) {
			// ── GUI / inventory slot ──
			// Centre the model in the 16×16 slot, scale to fit
			poseStack.translate(0.5, 0.75, 0.5);
			poseStack.scale(0.45f, 0.45f, 0.45f);
			// Flip Y-down → Y-up (Blockbench convention)
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			// Isometric-ish viewing angle
			poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.XP, 0, true).toMoj());
		} else if (displayContext == ItemDisplayContext.FIXED) {
			// ── Item frame ──
			poseStack.translate(0.5, 0.15, 0.5);
			poseStack.scale(0.4f, 0.4f, 0.4f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		} else {
			// ── Hand / ground / third-person ──
			poseStack.translate(0.5, 0.6, 0.5);
			poseStack.scale(0.35f, 0.35f, 0.35f);
			poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}
