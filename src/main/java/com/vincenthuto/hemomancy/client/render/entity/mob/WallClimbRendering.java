package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.WallClimbPose;
import net.minecraft.util.Mth;

/**
 * Turns a climbing mob onto its wall: the body swings to face the wall,
 * tips up so the head leads and the belly or foot lies on the surface, and
 * slides forward so it touches the wall instead of hovering off it.
 */
public final class WallClimbRendering {
	private WallClimbRendering() {
	}

	/** Body yaw for this frame, swung toward the wall as the climb blends in. */
	public static float bodyYaw(WallClimbPose pose, float bodyYaw, float partialTick) {
		float climb = pose.amount(partialTick);
		if (climb <= 0.0F) {
			return bodyYaw;
		}
		return bodyYaw + Mth.wrapDegrees(pose.wallYaw() - bodyYaw) * climb;
	}

	/**
	 * Called after the renderer's yaw turn, so -Z is the way the body faces.
	 *
	 * @param wallOffset distance from the entity centre to the wall, usually half its width
	 * @param lift       height that centres the upright body on the hitbox, usually half its length
	 */
	public static void tilt(WallClimbPose pose, PoseStack poseStack, float partialTick, float wallOffset,
			float lift) {
		float climb = pose.amount(partialTick);
		if (climb <= 0.0F) {
			return;
		}
		poseStack.translate(0.0F, lift * climb, -wallOffset * climb);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * climb));
	}
}
