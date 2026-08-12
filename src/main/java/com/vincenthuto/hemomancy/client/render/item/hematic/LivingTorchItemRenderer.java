package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
		LivingTorchRenderPlacement.applyCustomModelTransform(poseStack, displayContext);
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
