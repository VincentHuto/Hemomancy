package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.IchorianSigilPreviewCycle;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilBodyAnimation;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilFacing;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPose;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPoseCalculator;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

public final class IchorianSigilPreviewRenderer {
	private IchorianSigilPreviewRenderer() {
	}

	public static boolean render(GuiGraphics gfx, IchorianSigilDefinition sigil,
			int areaX, int areaY, int areaW, int areaH, float rotationDegrees,
			long elapsedTicks, float partialTick) {
		if (sigil == null || sigil.nodes().isEmpty() || sigil.awakenedForm().isEmpty()) return false;

		IchorianSigilPreviewCycle.Sample cycle =
				IchorianSigilPreviewCycle.sample(elapsedTicks, partialTick);
		float animationAge = cycle.animationAgeTicks();
		AwakenedIchorianSigilPose sigilPose = AwakenedIchorianSigilPoseCalculator.calculate(
				sigil, cycle.morphAgeTicks(), animationAge, 0.0F);
		sigilPose = new AwakenedIchorianSigilPose(
				sigilPose.landmarks(),
				sigilPose.primaryVessels().stream()
						.map(vessel -> new AwakenedIchorianSigilPose.Vessel(
								vessel.from(), vessel.to(), vessel.thickness(), 1.0F))
						.toList(),
				sigilPose.secondaryVessels(), sigilPose.membranes(),
				sigilPose.detachment(), sigilPose.migration(), sigilPose.quickening(), sigilPose.scale());
		AwakenedIchorianSigilBodyAnimation.BodyPose body =
				AwakenedIchorianSigilBodyAnimation.pose(sigil.id(), animationAge, 0.0F);
		float extent = Math.max(0.8F, AwakenedIchorianSigilPoseCalculator.tierScale(sigil.tier()));
		float scale = Math.min(areaW, areaH) / (extent * 2.35F);

		PoseStack stack = gfx.pose();
		stack.pushPose();
		stack.translate(areaX + areaW / 2.0F, areaY + areaH / 2.0F, 300.0F);
		stack.scale(scale, -scale, scale);
		stack.mulPose(Axis.XP.rotationDegrees(18.0F));
		stack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
		stack.mulPose(Axis.YP.rotationDegrees(
				AwakenedIchorianSigilFacing.authoredForwardCorrection(
						sigil.awakenedForm().orElseThrow().forward().x,
						sigil.awakenedForm().orElseThrow().forward().z)));
		stack.translate(body.offsetX(), body.offsetY(), body.offsetZ());
		stack.mulPose(Axis.YP.rotationDegrees(body.yawDegrees()));
		stack.mulPose(Axis.XP.rotationDegrees(body.pitchDegrees()));
		stack.mulPose(Axis.ZP.rotationDegrees(body.rollDegrees()));
		stack.scale(body.scaleX(), body.scaleY(), body.scaleZ());

		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		try {
			AwakenedIchorianSigilGeometryRenderer.render(
					sigilPose, stack, buffers, animationAge, sigil.color(), sigil.id().hashCode());
			buffers.endBatch();
		} finally {
			stack.popPose();
		}
		return true;
	}
}
