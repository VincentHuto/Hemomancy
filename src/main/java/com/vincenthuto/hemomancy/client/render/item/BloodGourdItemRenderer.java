package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
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

public class BloodGourdItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE_WHITE = Hemomancy.rloc("textures/entity/blood_gourd/white.png");
	public static final ResourceLocation TEXTURE_RED = Hemomancy.rloc("textures/entity/blood_gourd/red.png");
	public static final ResourceLocation TEXTURE_BLACK = Hemomancy.rloc("textures/entity/blood_gourd/black.png");
	public static final ResourceLocation TEXTURE_CURVED = Hemomancy.rloc("textures/entity/blood_gourd/curved_horn.png");

	private BloodGourdModel<?> gourdModel;
	private CurvedHornModel<?> curvedHornModel;

	public BloodGourdItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.gourdModel = new BloodGourdModel<>(modelSet.bakeLayer(BloodGourdModel.blood_gourd));
			this.curvedHornModel = new CurvedHornModel<>(modelSet.bakeLayer(CurvedHornModel.curved_horn));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.gourdModel == null || this.curvedHornModel == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.gourdModel = new BloodGourdModel<>(modelSet.bakeLayer(BloodGourdModel.blood_gourd));
			this.curvedHornModel = new CurvedHornModel<>(modelSet.bakeLayer(CurvedHornModel.curved_horn));
		}

		if (stack.getItem() instanceof BloodGourdItem) {
			// Determine texture based on which gourd item it is
			ResourceLocation texture;
			boolean isCurvedHorn = stack.is(ItemInit.curved_horn.get());

			if (isCurvedHorn) {
				texture = TEXTURE_CURVED;
			} else if (stack.is(ItemInit.blood_gourd_white.get())) {
				texture = TEXTURE_WHITE;
			} else if (stack.is(ItemInit.blood_gourd_red.get())) {
				texture = TEXTURE_RED;
			} else if (stack.is(ItemInit.blood_gourd_black.get())) {
				texture = TEXTURE_BLACK;
			} else {
				texture = TEXTURE_WHITE;
			}

			poseStack.pushPose();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
			poseStack.translate(0.0, -0.5, 0.0);

			// Undo the body offset that the layer model has built-in, and center it
			poseStack.translate(-5.75 / 16.0, -12.5722 / 16.0, -0.25 / 16.0);

			MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());
			VertexConsumer vertexConsumer = immediateBuffer.getBuffer(RenderType.text(texture));

			if (isCurvedHorn) {
				curvedHornModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
						OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				gourdModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
						OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			immediateBuffer.endBatch();
			poseStack.popPose();
		}
	}
}

