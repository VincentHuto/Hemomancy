package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperMountModel;
import com.vincenthuto.hemomancy.client.render.geometry.SanguineConduitCoreGeometry;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWoundRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The open wound on the mount: four scute plates split and hinge back from the socket, exposing a pulsing blood
 * core that throbs harder and churns as Blood Absorption drains it, flashing on every hit or surge.
 */
public final class VesperWoundLayer extends RenderLayer<VesperTheCrownedRefusalEntity, VesperMountModel> {
	private static final ResourceLocation MOUNT = Hemomancy.rloc("textures/entity/boss/endgame/vesper_mount.png");

	public VesperWoundLayer(RenderLayerParent<VesperTheCrownedRefusalEntity, VesperMountModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		int anchor = entity.getActiveAnchor();
		if (anchor < 0 || entity.isInvisible()) return;
		float drained = entity.getWoundDrained();
		float progress = Mth.clamp(drained / VesperWoundRules.DRAIN_REQUIRED, 0.0F, 1.0F);
		float flash = VesperCombatRules.anchorFlashStrength(entity.getAnchorFlashTicks());
		float breath = 0.5F + 0.5F * Mth.sin(ageInTicks * (0.12F + progress * 0.2F));

		poseStack.pushPose();
		getParentModel().translateToSocket(poseStack, switch (VesperWoundRules.Site.fromIndex(anchor)) {
			case CARAPACE -> VesperMountModel.Socket.WOUND_CARAPACE;
			case NECK -> VesperMountModel.Socket.WOUND_NECK;
			case TAIL -> VesperMountModel.Socket.WOUND_TAIL;
		});
		// The plates peel further open as he weakens, and twitch shut a little on each breath.
		float open = (55.0F + 30.0F * progress - 8.0F * breath) * Mth.DEG_TO_RAD;
		for (int i = 0; i < 4; i++) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(90.0F * i + 45.0F));
			// Lift the split scutes clear of the surrounding carapace so they stand out against it.
			poseStack.translate(0.0F, -0.12F, -0.32F);
			// The mount's own painted wound scute: plate outside, raw veined tissue on the underside.
			getParentModel().renderWoundScute(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(MOUNT)),
					packedLight, OverlayTexture.NO_OVERLAY, -open);
			poseStack.popPose();
		}
		float radius = 0.5F + 0.15F * progress;
		SanguineConduitCoreGeometry.Style style = new SanguineConduitCoreGeometry.Style(
				radius, 0.07F, 1.0F, 0.06F + 0.06F * progress,
				0.08F + 0.16F * progress, 0.35F + 0.9F * progress, flash,
				new SanguineConduitCoreGeometry.Color(0.86F, 0.02F, 0.03F, 0.95F),
				new SanguineConduitCoreGeometry.Color(0.02F, 0.0F, 0.0F, 0.45F));
		float phase = (entity.getId() & 1023) * (Mth.TWO_PI / 1024.0F);
		SanguineConduitCoreGeometry.render(poseStack, buffer, ageInTicks, phase, style);
		poseStack.popPose();
	}
}
