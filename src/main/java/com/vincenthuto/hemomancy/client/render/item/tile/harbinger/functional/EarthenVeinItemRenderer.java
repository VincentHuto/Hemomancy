package com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.EarthenVeinModel;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.EarthenVeinRenderer.EarthenVeinAnimContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EarthenVeinItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/earthen_vein/model_earthen_vein.png");
	private static final float GUI_MODEL_PITCH_DEGREES = 198.0F;
	private static final float GUI_MODEL_YAW_DEGREES = -42.0F;
	private static final float GUI_MODEL_ROLL_DEGREES = -8.0F;

	private EarthenVeinModel model;
	private final EarthenVeinAnimContext animCtx = new EarthenVeinAnimContext(new AnimationState());

	public EarthenVeinItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new EarthenVeinModel(
					modelSet.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		}
		animCtx.state().start(0);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new EarthenVeinModel(
					modelSet.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		}

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		if (isGui) {
			poseStack.translate(0.5D, 1.0D, 0.5D);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(GUI_MODEL_PITCH_DEGREES));
			poseStack.mulPose(Axis.YP.rotationDegrees(GUI_MODEL_YAW_DEGREES));
			poseStack.mulPose(Axis.ZP.rotationDegrees(GUI_MODEL_ROLL_DEGREES));
		} else {
			poseStack.translate(0.5D, 1.0D, 0.5D);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
		}

		// Drive the wiggle animation using the client level's game time
		if (Minecraft.getInstance().level != null) {
			float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
			model.setupAnimation(Minecraft.getInstance().level, partialTicks, animCtx);
		}

		// Hide stent and nametag for item rendering
		model.getRoot().getChild("stent").visible = false;

		model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight,
				OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}


