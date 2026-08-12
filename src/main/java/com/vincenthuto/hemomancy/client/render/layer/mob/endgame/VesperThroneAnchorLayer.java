package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.client.render.geometry.SanguineConduitCoreGeometry;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;

/** Renders the exposed throne target at the interpolated multipart hitbox center. */
public final class VesperThroneAnchorLayer
		extends RenderLayer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
	private static final float CORE_RADIUS = 0.225F;

	public VesperThroneAnchorLayer(
			RenderLayerParent<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.getActiveAnchor() < 0 || entity.isInvisible()) return;
		double entityX = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double entityZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		VesperCombatRules.AnchorCenter center = entity.getInterpolatedAnchorCenter(partialTick);
		float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		float damage = entity.getActiveAnchorDamage();
		float flash = VesperCombatRules.anchorFlashStrength(entity.getAnchorFlashTicks());
		SanguineConduitCoreGeometry.Style style = new SanguineConduitCoreGeometry.Style(
				CORE_RADIUS, 0.055F, 1.0F, 0.055F,
				VesperCombatRules.anchorPulseSpeed(damage),
				VesperCombatRules.anchorSurfaceAgitation(damage), flash,
				new SanguineConduitCoreGeometry.Color(0.86F, 0.015F, 0.025F, 0.94F),
				new SanguineConduitCoreGeometry.Color(0.012F, 0.0F, 0.0F, 0.42F));

		poseStack.pushPose();
		// Cancel LivingEntityRenderer's model-space transform, then apply the interpolated world offset.
		poseStack.translate(0.0D, 1.501D, 0.0D);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));
		poseStack.translate(center.x() - entityX, center.y() - entityY, center.z() - entityZ);
		float phase = (entity.getId() & 1023) * (Mth.TWO_PI / 1024.0F);
		SanguineConduitCoreGeometry.render(poseStack, buffer, entity.tickCount + partialTick, phase, style);
		poseStack.popPose();
	}
}
