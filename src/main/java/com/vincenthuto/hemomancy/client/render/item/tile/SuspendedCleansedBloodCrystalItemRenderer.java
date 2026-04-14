package com.vincenthuto.hemomancy.client.render.item.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedCleansedBloodCrystalModel;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SuspendedCleansedBloodCrystalItemRenderer extends BlockEntityWithoutLevelRenderer {

	// Using the same texture as the blood crystal for now - replace with final texture later
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_suspended_cleansed_blood_crystal.png");

	private SuspendedCleansedBloodCrystalModel model;

	public SuspendedCleansedBloodCrystalItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new SuspendedCleansedBloodCrystalModel(
					modelSet.bakeLayer(SuspendedCleansedBloodCrystalModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new SuspendedCleansedBloodCrystalModel(
					modelSet.bakeLayer(SuspendedCleansedBloodCrystalModel.LAYER_LOCATION));
		}

		poseStack.pushPose();
		poseStack.translate(0.5, 1, 0.5);
		poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		poseStack.translate(0.0, -0.5, 0.0);

		model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
	}
}
