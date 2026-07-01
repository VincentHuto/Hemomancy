package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.tile.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpecimenJarItemRenderer extends BlockEntityWithoutLevelRenderer {
	private final SpecimenJarRenderer specimenRenderer = new SpecimenJarRenderer(null);

	public SpecimenJarItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.pushPose();
		applyDisplayTransform(displayContext, poseStack);
		minecraft.getBlockRenderer().renderSingleBlock(BlockInit.specimen_jar.get().defaultBlockState(),
				poseStack, buffer, combinedLight, OverlayTexture.NO_OVERLAY);

		CompoundTag specimen = SpecimenJarData.getSpecimen(stack);
		Level level = minecraft.level;
		if (level != null && !specimen.isEmpty()) {
			specimenRenderer.renderSpecimen(level, specimen, minecraft.getFrameTimeNs(), poseStack, buffer, combinedLight);
		}
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}

	private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		poseStack.translate(0.5D, 0.5D, 0.5D);
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
			poseStack.scale(0.68F, 0.68F, 0.68F);
		} else if (displayContext == ItemDisplayContext.GROUND) {
			poseStack.translate(0.0D, 3.0D / 16.0D, 0.0D);
			poseStack.scale(0.4F, 0.4F, 0.4F);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.mulPose(Axis.YP.rotationDegrees(-180.0F));
			poseStack.translate(0.0D, -1.0D / 16.0D, 0.0D);
			poseStack.scale(0.5F, 0.5F, 0.5F);
		} else if (displayContext.firstPerson()) {
			poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
			poseStack.translate(0.0D, 1.0D / 16.0D, 0.0D);
			poseStack.scale(0.38F, 0.38F, 0.38F);
		} else {
			poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
			poseStack.translate(0.0D, 2.0D / 16.0D, 0.0D);
			poseStack.scale(0.38F, 0.38F, 0.38F);
		}
		poseStack.translate(-0.5D, -0.5D, -0.5D);
	}
}
