package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.tile.functional.SpecimenJarModel;
import com.vincenthuto.hemomancy.client.render.tile.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SpecimenJarItemRenderer extends BlockEntityWithoutLevelRenderer {
	private SpecimenJarModel model;
	private SpecimenJarRenderer specimenRenderer;

	public SpecimenJarItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new SpecimenJarModel(modelSet.bakeLayer(SpecimenJarModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new SpecimenJarModel(modelSet.bakeLayer(SpecimenJarModel.LAYER_LOCATION));
		}
		if (this.specimenRenderer == null) {
			this.specimenRenderer = new SpecimenJarRenderer();
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		if (isGui) {
			poseStack.translate(0.5D, 0.0D, 0.5D);
			poseStack.scale(0.62F, 0.62F, 0.62F);
			poseStack.mulPose(new Quaternion(Vector3.YP, 35, true).toMoj());
			poseStack.translate(-0.5D, -1.5D, -0.5D);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5D, 0.0, 0.5D);
			poseStack.scale(0.55F, 0.55F, 0.55F);
			poseStack.translate(-0.5D, -1.5D, -0.5D);
		} else {
			poseStack.translate(0.5D, 0.5D, 0.5D);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
			poseStack.translate(-0.5D, -1.5D, -0.5D);
		}

		SpecimenJarRenderer.renderJarFrame(model, poseStack, buffer, combinedLight, combinedOverlay);
		if (SpecimenJarData.hasSpecimen(stack)) {
			poseStack.pushPose();
			poseStack.translate(0.1D, 1.25D, 0.125D);
			poseStack.scale(0.8F, 0.8F, 0.8F);
			specimenRenderer.renderSpecimen(Minecraft.getInstance().level, SpecimenJarData.getSpecimen(stack),
					0.0F, poseStack, buffer, combinedLight);
			poseStack.popPose();

		}
		SpecimenJarRenderer.renderJarGlass(model, poseStack, buffer, combinedLight, combinedOverlay);
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}

}
