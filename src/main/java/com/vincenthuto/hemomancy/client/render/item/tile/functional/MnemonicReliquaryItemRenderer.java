package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MnemonicReliquaryModel;
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

public class MnemonicReliquaryItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_mnemonic_reliquary.png");

	private MnemonicReliquaryModel model;

	public MnemonicReliquaryItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new MnemonicReliquaryModel(
					modelSet.bakeLayer(MnemonicReliquaryModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new MnemonicReliquaryModel(
					modelSet.bakeLayer(MnemonicReliquaryModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
			poseStack.mulPose(new Quaternion(Vector3.YN, 90, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.ZN, 30, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());

			poseStack.translate(-1, -1.75, -0.125);
			poseStack.scale(1.25f,1.25f,1.25f);
		}else{
			poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
			poseStack.translate(0, -1.5, -1);

		}

		poseStack.pushPose();
		poseStack.translate(0.5, 1.2, 0.5);
		poseStack.scale(0.5f, 0.5f, 0.5f);
		poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());

		// Reset lid to closed and brain to default position for item rendering
		model.getLid().zRot = 0;
		model.getBrain().y = 4.0F;

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight,
				OverlayTexture.NO_OVERLAY, -1);

		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}

