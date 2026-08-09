package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Brief full-body crimson ignition before the Evening Star begins combat. */
public final class VesperAwakeningGlowLayer
		extends RenderLayer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/boss/endgame/vesper_evening_star.png");

	public VesperAwakeningGlowLayer(
			RenderLayerParent<VesperTheEveningStarEntity, VesperTheEveningStarModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!entity.isAwakening() || entity.isInvisible()) return;
		float glow = VesperPhaseTransitionRules.awakeningGlow(entity.getAwakeningFrame(partialTick));
		if (glow <= 0.0F) return;
		float pulse = 0.82F + Mth.sin(ageInTicks * 0.55F) * 0.18F;
		int alpha = Mth.clamp(Math.round(190.0F * glow * pulse), 0, 255);
		int color = (alpha << 24) | 0x00FF0808;
		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, color);
	}
}
