package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;
import com.vincenthuto.hemomancy.client.render.armor.ArmorItemDisplayTransformHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HemolymphopodaHeadpieceItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/hemolymphopoda/model_hemolymphopoda.png");
	private static final float DOME_CENTER_Y = -23.0F / 16.0F;
	private static final float DOME_CENTER_Z = 5.5F / 16.0F;
	private static final float GUI_HEADPIECE_SCALE = 0.64F;
	private static final float HELD_HEADPIECE_SCALE = 0.44F;

	private HemolymphopodaModel<?> model;

	public HemolymphopodaHeadpieceItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new HemolymphopodaModel<>(modelSet.bakeLayer(HemolymphopodaModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer,
			int light, int overlay) {
		if (this.model == null) {
			this.model = new HemolymphopodaModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(HemolymphopodaModel.LAYER_LOCATION));
		}

		poseStack.pushPose();
		applyHeadpieceItemTransform(context, poseStack);
		poseStack.translate(0.0F, DOME_CENTER_Y, DOME_CENTER_Z);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);

		poseStack.popPose();
	}

	private static void applyHeadpieceItemTransform(ItemDisplayContext context, PoseStack poseStack) {
		ArmorItemDisplayTransformHelper.apply(context, EquipmentSlot.HEAD, poseStack);
		if (context == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.YP.rotationDegrees(-20.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-15.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
			poseStack.scale(GUI_HEADPIECE_SCALE, GUI_HEADPIECE_SCALE, GUI_HEADPIECE_SCALE);
			poseStack.translate(0.0D, -0.18D, -0.1D);
		} else {
			poseStack.mulPose(Axis.YP.rotationDegrees(-12.0F));
			poseStack.scale(HELD_HEADPIECE_SCALE, HELD_HEADPIECE_SCALE, HELD_HEADPIECE_SCALE);
			poseStack.translate(-0.5D, 0.40D, 0.14D);
		}
	}
}
