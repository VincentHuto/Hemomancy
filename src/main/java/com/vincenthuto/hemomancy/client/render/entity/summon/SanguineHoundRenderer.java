package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.SanguineHoundModel;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SanguineHoundRenderer extends MobRenderer<SanguineHoundEntity, SanguineHoundModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/puppeteer_summon/sanguine_hound.png");
	private static final ResourceLocation CUR_TEXTURE = Hemomancy.rloc(
			"textures/entity/puppeteer_summon/sanguine_hound_cur.png");

	public SanguineHoundRenderer(EntityRendererProvider.Context context) {
		super(context, new SanguineHoundModel(context.bakeLayer(SanguineHoundModel.LAYER_LOCATION)), 0.55F);
	}

	@Override
	public ResourceLocation getTextureLocation(SanguineHoundEntity entity) {
		return entity.isBloodCur() ? CUR_TEXTURE : TEXTURE;
	}

	@Override
	protected float getBob(SanguineHoundEntity entity, float partialTick) {
		return entity.getTailAngle();
	}

	@Override
	public void render(SanguineHoundEntity entity, float entityYaw, float partialTicks,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) return;
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
