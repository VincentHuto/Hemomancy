package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingSickleModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LivingSickleItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_sickle.png");
	private final LivingSickleModel model;

	public LivingSickleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
		super(dispatcher, models);
		this.model = new LivingSickleModel(models.bakeLayer(LivingSickleModel.LAYER_LOCATION));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
			MultiBufferSource buffers, int light, int overlay) {
		poseStack.pushPose();
		poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
		poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
		poseStack.scale(0.7F, 0.7F, 0.7F);
		poseStack.translate(-0.3D, -0.15D, 0.0D);
		if (context == ItemDisplayContext.GUI) poseStack.translate(-0.2D, 0.2D, 0.0D);
		VertexConsumer base = buffers.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, base, light, OverlayTexture.NO_OVERLAY, -1);
		if (!LivingStaffMorphRenderer.isMorphBuffer(buffers)) {
			VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
			model.renderToBuffer(poseStack, glint, light, OverlayTexture.NO_OVERLAY, -1);
		}
		poseStack.popPose();
	}

	public static void renderModel(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
			MultiBufferSource buffers, int light, int seed) {
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light,
				OverlayTexture.NO_OVERLAY, poseStack, buffers, Minecraft.getInstance().level, seed);
	}
}
