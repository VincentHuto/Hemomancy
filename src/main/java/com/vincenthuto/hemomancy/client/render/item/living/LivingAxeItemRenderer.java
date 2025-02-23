package com.vincenthuto.hemomancy.client.render.item.living;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingAxeModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingAxeItem;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LivingAxeItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation living_blade = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_axe_hand.png");

	public final LivingAxeModel unleashed;

	public LivingAxeItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		unleashed = new LivingAxeModel(p_172551_.bakeLayer(LivingAxeModel.living_axe));
	}

	public LivingAxeModel getModel() {
		return unleashed;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof LivingAxeItem) {
			Model model;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());

			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(living_blade));
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.6, 0.25, 0.25);
			if (p_239207_2_ == ItemDisplayContext.GUI) {
				ms.translate(-0, 0.15, 0);
			}
			model = unleashed;
			if (model == unleashed) {
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
				model.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				model.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.endBatch();
			ms.popPose();
		}
	}
}