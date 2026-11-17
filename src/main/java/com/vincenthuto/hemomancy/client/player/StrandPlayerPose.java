package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.data.StrandClientState;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The third-person arm while a strand is out (core-loop Track 12): the holding arm points along the strand, not the
 * bow pose's level aim. A lash's arm follows the chain a node out from the hand, so it whips through the stroke a
 * moment ahead of the tip; a plant's points down to the ground it sinks into; a jet, spray or brand points at its
 * contact. The free arm draws back to balance the stroke, or, with a twin from the off hand (Second Strand), points
 * along the twin the same way.
 */
final class StrandPlayerPose {
	/** The lash node the arm follows: near the hand, so the arm leads and the tip trails. */
	private static final int LEAD_NODE = 2;

	private StrandPlayerPose() {
	}

	static boolean apply(Player player, HumanoidModel<?> model, float partial) {
		StrandClientState.Strand strand = StrandClientState.of(player.getId());
		if (strand == null || strand.cue == null || strand.released >= 0 || !player.isUsingItem()) return false;
		Vec3 target = target(player, strand, partial);
		if (target == null) return false;
		boolean left = strand.cue.leftArm();
		float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
		ModelPart arm = left ? model.leftArm : model.rightArm;
		ModelPart free = left ? model.rightArm : model.leftArm;
		if (!point(player, arm, left, bodyYaw, target, partial)) return false;
		Vec3 twin = strand.cue.twin() ? twinTarget(player, strand, target, partial) : null;
		if (twin == null || !point(player, free, !left, bodyYaw, twin, partial)) {
			// The free arm swings back against the stroke.
			free.xRot = 0.35F;
			free.yRot = 0.0F;
			free.zRot = left ? -0.3F : 0.3F;
		}
		if (model instanceof net.minecraft.client.model.PlayerModel<?> playerModel) {
			playerModel.rightSleeve.copyFrom(model.rightArm);
			playerModel.leftSleeve.copyFrom(model.leftArm);
		}
		return true;
	}

	/** Points {@code arm} from its shoulder at {@code target}; false when the target sits on the shoulder. */
	private static boolean point(Player player, ModelPart arm, boolean left, float bodyYaw, Vec3 target, float partial) {
		double side = left ? 0.33D : -0.33D;
		double yawRad = Math.toRadians(bodyYaw);
		// The shoulder, in world space: a little to the arm's side of the body and below the eye.
		Vec3 shoulder = player.getPosition(partial).add(0, player.getBbHeight() * 0.76D, 0)
				.add(Math.cos(yawRad) * side, 0, Math.sin(yawRad) * side);
		Vec3 dir = target.subtract(shoulder);
		if (dir.lengthSqr() < 1.0E-4D) return false;
		float aimYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
		float aimPitch = (float) Math.toDegrees(-Math.atan2(dir.y, dir.horizontalDistance()));
		arm.xRot = -Mth.HALF_PI + Mth.clamp(aimPitch, -80.0F, 85.0F) * Mth.DEG_TO_RAD;
		arm.yRot = Mth.wrapDegrees(aimYaw - bodyYaw) * Mth.DEG_TO_RAD;
		arm.zRot = 0.0F;
		return true;
	}

	/** What the off hand's arm follows with a twin: the twin lash's lead node, or the same contact as the lead. */
	private static Vec3 twinTarget(Player player, StrandClientState.Strand strand, Vec3 lead, float partial) {
		if (strand.form() == StrandForm.LASH) {
			LashRules.State twin = strand.twinLash;
			return twin.seeded() ? twin.previous(LEAD_NODE).lerp(twin.node(LEAD_NODE), partial) : null;
		}
		return lead;
	}

	private static Vec3 target(Player player, StrandClientState.Strand strand, float partial) {
		if (strand.form() == StrandForm.LASH) {
			LashRules.State lash = strand.lash;
			return lash.seeded() ? lash.previous(LEAD_NODE).lerp(lash.node(LEAD_NODE), partial) : null;
		}
		if (strand.form() == StrandForm.PLANT) return strand.point;
		if (strand.form() == StrandForm.SPRAY || strand.end == null)
			return player.getEyePosition(partial).add(player.getViewVector(partial).scale(3.0D));
		return strand.endPrevious.lerp(strand.end, partial);
	}
}
