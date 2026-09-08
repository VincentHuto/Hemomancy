package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingSickleModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.mojang.math.Axis;
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
	private static final double THIRD_PERSON_MODEL_LIFT = 0.52D;
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
		applyHandGrip(poseStack, context);
		if (context == ItemDisplayContext.GUI) {
			poseStack.translate(0.0D, 0.18D, -0.1D);
			poseStack.mulPose(Axis.ZP.rotationDegrees(-24.0F));
		}
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		float scale = context == ItemDisplayContext.GUI ? 0.75F : 0.62F;
		boolean thirdPersonHand = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
		poseStack.scale(scale, scale, scale);
		poseStack.translate(context == ItemDisplayContext.GUI ? 0.8D : -0.45D,
				thirdPersonHand ? THIRD_PERSON_MODEL_LIFT : -0.1D, 0.0D);
		VertexConsumer base = buffers.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, base, light, OverlayTexture.NO_OVERLAY, -1);
		if (!LivingStaffMorphRenderer.isMorphBuffer(buffers)) {
			VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
			model.renderToBuffer(poseStack, glint, light, OverlayTexture.NO_OVERLAY, -1);
		}
		poseStack.popPose();
	}

	private static void applyHandGrip(PoseStack poseStack, ItemDisplayContext context) {
		if (context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
				&& context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				&& context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				&& context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) return;
		poseStack.translate(0.5D, 1.1D, 0.2D);
		poseStack.mulPose(Axis.XP.rotationDegrees(0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
		poseStack.mulPose(Axis.ZP.rotationDegrees( 0F));
	}

	public static void renderModel(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
			MultiBufferSource buffers, int light, int seed) {
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light,
				OverlayTexture.NO_OVERLAY, poseStack, buffers, Minecraft.getInstance().level, seed);
	}
}
