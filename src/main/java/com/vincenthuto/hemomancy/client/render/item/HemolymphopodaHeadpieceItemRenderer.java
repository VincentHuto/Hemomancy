package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;

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

public class HemolymphopodaHeadpieceItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/hemolymphopoda/model_hemolymphopoda.png");

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

		switch (context) {
		case GUI -> {
			poseStack.translate(0.5D, 1.25D, 0.5D);
			poseStack.scale(0.15F, -0.15F, -0.15F);
		}
		case GROUND -> {
			poseStack.translate(0.5D, 0.35D, 0.5D);
			poseStack.scale(0.08F, -0.08F, -0.08F);
		}
		case FIXED -> {
			poseStack.translate(0.5D, 0.5D, 0.5D);
			poseStack.scale(0.1F, -0.1F, -0.1F);
		}
		default -> {
			poseStack.translate(0.5D, 0.9D, 0.5D);
			poseStack.scale(0.11F, -0.11F, -0.11F);
		}
		}

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

		poseStack.popPose();
	}
}
