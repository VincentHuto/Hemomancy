package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.VeinwingVultureModel;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VeinwingVultureRenderer extends MobRenderer<VeinwingVultureEntity, VeinwingVultureModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/veinwing_vulture.png");

	public VeinwingVultureRenderer(EntityRendererProvider.Context context) {
		super(context, new VeinwingVultureModel(context.bakeLayer(VeinwingVultureModel.LAYER_LOCATION)), 0.25F);
	}

	@Override
	public ResourceLocation getTextureLocation(VeinwingVultureEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(VeinwingVultureEntity entity, float entityYaw, float partialTicks,
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
