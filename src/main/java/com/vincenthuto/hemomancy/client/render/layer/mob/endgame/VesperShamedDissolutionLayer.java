package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEveningStarPresentationRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** Vesper-owned monolith shell, independent of the low-health red-line layer. */
public final class VesperShamedDissolutionLayer
		extends RenderLayer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
	private static final int SHELL_LIGHT = 0x00F000F0;

	public VesperShamedDissolutionLayer(
			RenderLayerParent<VesperTheEveningStarEntity, VesperTheEveningStarModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible() || !entity.isAwaitingAbsorption()) return;
		float progress = VesperEveningStarPresentationRules.absorptionDissolve(
				entity.getDefeatAbsorptionProgress());
		float alpha = Mth.lerp(progress, 0.42F, 0.12F)
				* (1.0F - 0.55F * VesperEveningStarPresentationRules.finalCollapseProgress(
						entity.getDefeatAbsorptionProgress()));
		int color = (Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24) | 0x00FFFFFF;
		float seed = Math.floorMod(entity.getUUID().hashCode(), 10007) / 10007.0F;
		VertexConsumer consumer = buffer.getBuffer(HemoRenderTypes.monolithicDislocationShell(
				SilentArchonArmorRenderHelper.timeSeconds(), seed));
		poseStack.pushPose();
		poseStack.scale(1.035F, 1.035F, 1.035F);
		getParentModel().renderToBuffer(poseStack, consumer, SHELL_LIGHT, OverlayTexture.NO_OVERLAY, color);
		poseStack.popPose();
	}
}
