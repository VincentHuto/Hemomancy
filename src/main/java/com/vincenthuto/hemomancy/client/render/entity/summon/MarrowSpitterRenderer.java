package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MarrowSpitterModel;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MarrowSpitterRenderer extends MobRenderer<MarrowSpitterEntity, MarrowSpitterModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/marrow_spitter.png");

	public MarrowSpitterRenderer(EntityRendererProvider.Context context) {
		super(context, new MarrowSpitterModel(context.bakeLayer(MarrowSpitterModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(MarrowSpitterEntity entity) {
		return TEXTURE;
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
