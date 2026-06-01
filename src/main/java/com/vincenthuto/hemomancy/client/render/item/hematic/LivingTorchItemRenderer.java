package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingTorchModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LivingTorchItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_torch.png");
	private static final float BASE_MODEL_SCALE = 0.68F;
	private static final float GUI_MODEL_SCALE = 0.78F;
	private static final float THIRD_PERSON_UPRIGHT_ROLL_DEGREES = 180.0F;
	private static final double THIRD_PERSON_TORCH_LIFT =0.62D;
	private static final double THIRD_PERSON_TORCH_OUTWARD_OFFSET = -0.6D;
	private LivingTorchModel model;

	public LivingTorchItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new LivingTorchModel(modelSet.bakeLayer(LivingTorchModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (!(stack.getItem() instanceof LivingTorchItem)) {
			return;
		}
		ensureModel();
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
		poseStack.scale(BASE_MODEL_SCALE, BASE_MODEL_SCALE, BASE_MODEL_SCALE);
		poseStack.translate(-0.14, 0.08, 0.18);
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-32.0f));
			poseStack.translate(0.1, 0.75, 0.5);
			poseStack.scale(BASE_MODEL_SCALE*.8f, BASE_MODEL_SCALE*.8f, BASE_MODEL_SCALE*.8f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
			poseStack.translate(1, 0.5, -0.22);
		} else if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			double side = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0D : 1.0D;
			poseStack.mulPose(Axis.ZP.rotationDegrees(THIRD_PERSON_UPRIGHT_ROLL_DEGREES));
			poseStack.translate(-side * THIRD_PERSON_TORCH_OUTWARD_OFFSET, THIRD_PERSON_TORCH_LIFT, 0.55);
		}
		VertexConsumer base = buffer.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, -1);
		VertexConsumer glint = buffer.getBuffer(RenderTypeInit.getCrimsonGlint());
		model.renderToBuffer(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
	}

	private void ensureModel() {
		if (model == null) {
			model = new LivingTorchModel(Minecraft.getInstance().getEntityModels()
					.bakeLayer(LivingTorchModel.LAYER_LOCATION));
		}
	}
}
