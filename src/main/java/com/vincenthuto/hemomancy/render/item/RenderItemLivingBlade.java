package com.vincenthuto.hemomancy.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingBlade;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeHandTame;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeUnleashed;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingBlade extends BlockEntityWithoutLevelRenderer {

	public final ModelLivingBladeUnleashed unleashed;
	public final ModelLivingBladeHandTame tame;

	public static ResourceLocation living_blade = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_blade_hand.png");

	public RenderItemLivingBlade(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		unleashed = new ModelLivingBladeUnleashed(p_172551_.bakeLayer(ModelLivingBladeUnleashed.living_blade_unleashed));
		tame = new ModelLivingBladeHandTame(p_172551_.bakeLayer(ModelLivingBladeHandTame.living_blade_tame));
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

			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(living_blade));
			ms.scale(0.5f, 0.5f, 0.5f);
			ms.translate(-1, -1, 1);
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

	public ModelLivingBladeUnleashed getModel() {
		return unleashed;
	}
}