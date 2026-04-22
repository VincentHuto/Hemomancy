package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeHandTameModel;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeUnleashedModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingBladeItem;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class LivingBladeItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation living_blade = Hemomancy.rloc("textures/entity/model_living_blade_hand.png");
	
	public final LivingBladeUnleashedModel unleashed;

	public final LivingBladeHandTameModel tame;

	public LivingBladeItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		unleashed = new LivingBladeUnleashedModel(p_172551_.bakeLayer(LivingBladeUnleashedModel.living_blade_unleashed));
		tame = new LivingBladeHandTameModel(p_172551_.bakeLayer(LivingBladeHandTameModel.living_blade_tame));
	}

	public LivingBladeUnleashedModel getModel() {
		return unleashed;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof LivingBladeItem) {
			Model model;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			VertexConsumer ivertexbuilder = buffers.getBuffer(RenderType.text(living_blade));
			ms.scale(0.5f, 0.5f, 0.5f);
			ms.translate(-1, -1, 1);
			model = (stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("state")) ? unleashed : tame;
			if (model == unleashed) {
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
				model.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				model.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			ms.popPose();
		}
	}
}