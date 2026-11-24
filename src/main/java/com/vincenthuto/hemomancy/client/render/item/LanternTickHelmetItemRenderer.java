package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.LanternTickModel;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemDisplayTransformHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LanternTickHelmetItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final float MODEL_CENTER_Y = -20.0F / 16.0F;
	private static final float GUI_HELMET_SCALE = 0.56F;
	private static final float HELD_HELMET_SCALE = 0.40F;

	private LanternTickModel model;

	public LanternTickHelmetItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new LanternTickModel(modelSet.bakeLayer(LanternTickModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer,
			int light, int overlay) {
		if (this.model == null) {
			this.model = new LanternTickModel(
					Minecraft.getInstance().getEntityModels().bakeLayer(LanternTickModel.LAYER_LOCATION));
		}

		poseStack.pushPose();
		applyHelmetItemTransform(context, poseStack);
		poseStack.translate(0.0D, MODEL_CENTER_Y, 0.08D);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LanternTickModel.TEXTURE));
		this.model.setupHelmetPose(0.0F);
		this.model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		this.model.renderLantern(poseStack, buffer);
		poseStack.popPose();
	}

	private static void applyHelmetItemTransform(ItemDisplayContext context, PoseStack poseStack) {
		ArmorItemDisplayTransformHelper.apply(context, EquipmentSlot.HEAD, poseStack);
		if (context == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.YP.rotationDegrees(-20.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-15.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
			poseStack.scale(GUI_HELMET_SCALE, GUI_HELMET_SCALE, GUI_HELMET_SCALE);
			poseStack.translate(0.0D, -0.18D, -0.1D);
		} else {
			poseStack.mulPose(Axis.YP.rotationDegrees(-10.0F));
			poseStack.scale(HELD_HELMET_SCALE, HELD_HELMET_SCALE, HELD_HELMET_SCALE);
			poseStack.translate(-0.5D, 0.40D, 0.14D);
		}
	}
}
