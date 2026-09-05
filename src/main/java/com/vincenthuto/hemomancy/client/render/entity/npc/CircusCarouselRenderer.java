package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusCarouselModel;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class CircusCarouselRenderer extends EntityRenderer<CircusCarouselEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/harbinger/circus/carousel.png");
	private static final ResourceLocation GLOW = Hemomancy.rloc("textures/entity/npc/harbinger/circus/carousel_glow.png");
	private final CircusCarouselModel model;

	public CircusCarouselRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new CircusCarouselModel(context.bakeLayer(CircusCarouselModel.LAYER_LOCATION));
		shadowRadius = 3.5F;
	}

	@Override
	public void render(CircusCarouselEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 7.0D, 0.0D);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		model.prepare(entity, partialTick);
		model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
				OverlayTexture.NO_OVERLAY, -1);
		int glowColor = entity.isActive() ? -1 : 0x66FFFFFF;
		model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.eyes(GLOW)), 0xF000F0,
				OverlayTexture.NO_OVERLAY, glowColor);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
	}

	@Override
	public boolean shouldRender(CircusCarouselEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		return frustum.isVisible(entity.getCarouselRenderBounds());
	}

	@Override
	public ResourceLocation getTextureLocation(CircusCarouselEntity entity) {
		return TEXTURE;
	}
}
