package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;

public class MarrowSpitterRenderer extends SkeletonRenderer<MarrowSpitterEntity> {
	public MarrowSpitterRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(MarrowSpitterEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
