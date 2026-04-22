package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenBloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenCurvedHornModel;
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

	public static final ResourceLocation TEXTURE_WHITE_OPEN = Hemomancy.rloc("textures/entity/blood_gourd/white_open.png");
	public static final ResourceLocation TEXTURE_RED_OPEN = Hemomancy.rloc("textures/entity/blood_gourd/red_open.png");
	public static final ResourceLocation TEXTURE_BLACK_OPEN = Hemomancy.rloc("textures/entity/blood_gourd/black_open.png");
	public static final ResourceLocation TEXTURE_CURVED_OPEN = Hemomancy.rloc("textures/entity/blood_gourd/curved_horn_open.png");

	private BloodGourdModel<?> gourdModel;
	private CurvedHornModel<?> curvedHornModel;
	private OpenBloodGourdModel<?> openGourdModel;
	private OpenCurvedHornModel<?> openCurvedHornModel;

	public BloodGourdItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.gourdModel = new BloodGourdModel<>(modelSet.bakeLayer(BloodGourdModel.blood_gourd));
			this.curvedHornModel = new CurvedHornModel<>(modelSet.bakeLayer(CurvedHornModel.curved_horn));
			this.openGourdModel = new OpenBloodGourdModel<>(modelSet.bakeLayer(OpenBloodGourdModel.open_blood_gourd));
			this.openCurvedHornModel = new OpenCurvedHornModel<>(modelSet.bakeLayer(OpenCurvedHornModel.open_curved_horn));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.gourdModel == null || this.curvedHornModel == null || this.openGourdModel == null || this.openCurvedHornModel == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.gourdModel = new BloodGourdModel<>(modelSet.bakeLayer(BloodGourdModel.blood_gourd));
			this.curvedHornModel = new CurvedHornModel<>(modelSet.bakeLayer(CurvedHornModel.curved_horn));
			this.openGourdModel = new OpenBloodGourdModel<>(modelSet.bakeLayer(OpenBloodGourdModel.open_blood_gourd));
			this.openCurvedHornModel = new OpenCurvedHornModel<>(modelSet.bakeLayer(OpenCurvedHornModel.open_curved_horn));
		}

		if (stack.getItem() instanceof BloodGourdItem) {
			// Check if the gourd is open
			boolean isOpen = stack.hasTag() && stack.getTag().getBoolean(BloodGourdItem.TAG_STATE);

			// Determine texture based on which gourd item it is and its open/closed state
			ResourceLocation texture;
			boolean isCurvedHorn = stack.is(ItemInit.curved_horn.get());

			if (isCurvedHorn) {
				texture = isOpen ? TEXTURE_CURVED_OPEN : TEXTURE_CURVED;
			} else if (stack.is(ItemInit.blood_gourd_white.get())) {
				texture = isOpen ? TEXTURE_WHITE_OPEN : TEXTURE_WHITE;
			} else if (stack.is(ItemInit.blood_gourd_red.get())) {
				texture = isOpen ? TEXTURE_RED_OPEN : TEXTURE_RED;
			} else if (stack.is(ItemInit.blood_gourd_black.get())) {
				texture = isOpen ? TEXTURE_BLACK_OPEN : TEXTURE_BLACK;
			} else {
				texture = isOpen ? TEXTURE_WHITE_OPEN : TEXTURE_WHITE;
			}

			poseStack.pushPose();
			poseStack.scale(2f, 2f, 2f);
			poseStack.translate(0.3, -.25, 0.3);
			poseStack.mulPose(new Quaternion(Vector3.YP, 90, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
			poseStack.translate(0.0, -0.5, 0.0);

			// Undo the body offset that the layer model has built-in, and center it
			poseStack.translate(-5.75 / 16.0, -12.5722 / 16.0, -0.25 / 16.0);

			VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.text(texture));
				if (isOpen) {
					openCurvedHornModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
							OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					curvedHornModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
							OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			} else {
				if (isOpen) {
					openGourdModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
							OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					gourdModel.renderToBuffer(poseStack, vertexConsumer, combinedLight,
							OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			}

			poseStack.popPose();
		}
	}
}

