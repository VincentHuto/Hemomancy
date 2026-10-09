package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.CinderBellowsModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.CinderBellowsGlowLayer;
import com.vincenthuto.hemomancy.client.render.world.CinderBreathFlames;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CinderBellowsRenderer extends MobRenderer<CinderBellowsEntity, CinderBellowsModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/cinder_bellows.png");

	public CinderBellowsRenderer(EntityRendererProvider.Context context) {
		super(context, new CinderBellowsModel(context.bakeLayer(CinderBellowsModel.LAYER_LOCATION)), 0.35F);
		addLayer(new CinderBellowsGlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(CinderBellowsEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(CinderBellowsEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		CinderBreathFlames.emit(entity, partialTicks);
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
