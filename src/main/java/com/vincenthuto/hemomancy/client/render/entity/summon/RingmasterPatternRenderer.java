package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.RingmasterPatternModel;
import com.vincenthuto.hemomancy.common.entity.summon.RingmasterPatternEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RingmasterPatternRenderer extends MobRenderer<RingmasterPatternEntity, RingmasterPatternModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/ringmaster_pattern.png");
	private static final ResourceLocation GLOW =
			Hemomancy.rloc("textures/entity/puppeteer_summon/ringmaster_pattern_glow.png");

	public RingmasterPatternRenderer(EntityRendererProvider.Context context) {
		super(context, new RingmasterPatternModel(context.bakeLayer(RingmasterPatternModel.LAYER_LOCATION)), 0.45F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(RingmasterPatternEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(RingmasterPatternEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		try {
			PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
			// The empty tailcoat stands a little taller than the doll whose entity type it once shared.
			poseStack.scale(1.12F, 1.12F, 1.12F);
			super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		} finally {
			poseStack.popPose();
		}
	}

	private static final class GlowLayer extends RenderLayer<RingmasterPatternEntity, RingmasterPatternModel> {
		private GlowLayer(RenderLayerParent<RingmasterPatternEntity, RingmasterPatternModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
				RingmasterPatternEntity entity, float limbSwing, float limbSwingAmount, float partialTick,
				float ageInTicks, float netHeadYaw, float headPitch) {
			VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(GLOW));
			getParentModel().renderToBuffer(poseStack, glow, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, 0xCCFFFFFF);
		}
	}
}
