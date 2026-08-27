package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.ScarletMummerModel;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ScarletMummerRenderer extends MobRenderer<ScarletMummerEntity, ScarletMummerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/scarlet_mummer.png");

	public ScarletMummerRenderer(EntityRendererProvider.Context context) {
		super(context, new ScarletMummerModel(context.bakeLayer(ScarletMummerModel.LAYER_LOCATION)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(ScarletMummerEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(ScarletMummerEntity entity, float entityYaw, float partialTicks,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) return;
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
