package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.GoreboundHulkModel;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoreboundHulkRenderer extends MobRenderer<GoreboundHulkEntity, GoreboundHulkModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/gorebound_hulk.png");

	public GoreboundHulkRenderer(EntityRendererProvider.Context context) {
		super(context, new GoreboundHulkModel(context.bakeLayer(GoreboundHulkModel.LAYER_LOCATION)), 0.65F);
	}

	@Override
	public ResourceLocation getTextureLocation(GoreboundHulkEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(GoreboundHulkEntity entity, float entityYaw, float partialTicks,
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
