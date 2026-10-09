package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MnemonistPuppetModel;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MnemonistPuppetRenderer extends MobRenderer<MnemonistPuppetEntity, MnemonistPuppetModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/mnemonist_puppet.png");

	public MnemonistPuppetRenderer(EntityRendererProvider.Context context) {
		super(context, new MnemonistPuppetModel(context.bakeLayer(MnemonistPuppetModel.LAYER_LOCATION)), 0.45F);
		addLayer(new MnemonistThreadLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(MnemonistPuppetEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(MnemonistPuppetEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// A legacy Ringmaster save converts on its first server tick; skip the frame before it does.
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity) || !entity.replaysMemories()) {
			return;
		}
		poseStack.pushPose();
		try {
			PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
			super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		} finally {
			poseStack.popPose();
		}
	}
}
