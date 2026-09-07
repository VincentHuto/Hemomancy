package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MnemonistPuppetModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.RingmasterPatternModel;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MnemonistPuppetRenderer extends MobRenderer<MnemonistPuppetEntity, HumanoidModel<MnemonistPuppetEntity>> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/mnemonist_puppet.png");
	private static final ResourceLocation RINGMASTER_TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/ringmaster_pattern.png");
	private static final ResourceLocation RINGMASTER_GLOW =
			Hemomancy.rloc("textures/entity/puppeteer_summon/ringmaster_pattern_glow.png");
	private final HumanoidModel<MnemonistPuppetEntity> mnemonist;
	private final HumanoidModel<MnemonistPuppetEntity> ringmaster;

	public MnemonistPuppetRenderer(EntityRendererProvider.Context context) {
		super(context, new MnemonistPuppetModel(context.bakeLayer(MnemonistPuppetModel.LAYER_LOCATION)), 0.45F);
		mnemonist = model;
		ringmaster = new RingmasterPatternModel(context.bakeLayer(RingmasterPatternModel.LAYER_LOCATION));
		addLayer(new RingmasterPatternLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(MnemonistPuppetEntity entity) {
		return isRingmasterPattern(entity) ? RINGMASTER_TEXTURE : TEXTURE;
	}

	@Override
	public void render(MnemonistPuppetEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		try {
			PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
			boolean pattern = isRingmasterPattern(entity);
			model = pattern ? ringmaster : mnemonist;
			if (pattern) poseStack.scale(1.12F, 1.12F, 1.12F);
			super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		} finally {
			model = mnemonist;
			poseStack.popPose();
		}
	}

	private static boolean isRingmasterPattern(MnemonistPuppetEntity entity) {
		return PuppeteerSummonDefinitions.RINGMASTER_PATTERN.equals(entity.hemomancy$getSummonName());
	}

	private static final class RingmasterPatternLayer extends RenderLayer<MnemonistPuppetEntity, HumanoidModel<MnemonistPuppetEntity>> {
		private RingmasterPatternLayer(RenderLayerParent<MnemonistPuppetEntity, HumanoidModel<MnemonistPuppetEntity>> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
				MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount, float partialTick,
				float ageInTicks, float netHeadYaw, float headPitch) {
			if (!isRingmasterPattern(entity)) return;
			VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(RINGMASTER_GLOW));
			getParentModel().renderToBuffer(poseStack, glow, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, 0xCCFFFFFF);
		}
	}
}
