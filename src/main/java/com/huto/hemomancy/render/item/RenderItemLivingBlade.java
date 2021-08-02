package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingBlade;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHand;
import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHandTame;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingBlade extends BlockEntityWithoutLevelRenderer {

	public final ModelLivingBladeHand unleashed = new ModelLivingBladeHand();
	public final ModelLivingBladeHandTame tame = new ModelLivingBladeHandTame();

	public static ResourceLocation living_blade = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_blade_hand.png");

	public RenderItemLivingBlade(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingBlade) {
			Model model;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
			ms.mulPose(new Quaternion(Vector3f.YP, 180, true));

			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());
			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(unleashed.renderType(living_blade));
			ms.scale(0.5f, 0.5f, 0.5f);
			ms.translate(-1, -1.8, 1);
			model = stack.getTag().getBoolean("state") ? unleashed : tame;
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

	public ModelLivingBladeHand getModel() {
		return unleashed;
	}
}