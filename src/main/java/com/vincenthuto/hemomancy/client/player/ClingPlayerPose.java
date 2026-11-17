package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * The third-person body of a player rooted by Venous Cling (core-loop Track 9). On a wall both hands reach up into
 * the face and the legs brace against it; under a ceiling both arms hold straight up and the legs hang. A crawl moves
 * the limbs hand over hand with the walk cycle. The model is never turned upside down.
 */
final class ClingPlayerPose {
	private ClingPlayerPose() {
	}

	static boolean apply(Player player, HumanoidModel<?> model, float partial) {
		ClingState cling = HemoCapabilityAccess.getCling(player);
		if (!cling.isClinging()) return false;
		float cycle = player.walkAnimation.position(partial) * 0.9F;
		float stride = Math.min(1.0F, player.walkAnimation.speed(partial) * 2.0F);
		float swing = Mth.sin(cycle) * 0.35F * stride;
		if (cling.surface() == ClingRules.Surface.CEILING) {
			model.rightArm.xRot = -Mth.PI + 0.12F + swing;
			model.leftArm.xRot = -Mth.PI + 0.12F - swing;
			model.rightArm.yRot = 0.0F;
			model.leftArm.yRot = 0.0F;
			model.rightArm.zRot = 0.12F;
			model.leftArm.zRot = -0.12F;
			model.rightLeg.xRot = 0.08F + swing * 0.6F;
			model.leftLeg.xRot = 0.08F - swing * 0.6F;
			model.rightLeg.zRot = 0.05F;
			model.leftLeg.zRot = -0.05F;
		} else {
			// The wall's direction from the body, as a yaw relative to it: the hands reach that way.
			float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
			float wallYaw = cling.face().getOpposite().toYRot();
			float toward = Mth.clamp(Mth.wrapDegrees(wallYaw - bodyYaw), -70.0F, 70.0F) * Mth.DEG_TO_RAD;
			model.rightArm.xRot = -Mth.HALF_PI - 0.75F + swing;
			model.leftArm.xRot = -Mth.HALF_PI - 0.75F - swing;
			model.rightArm.yRot = toward - 0.12F;
			model.leftArm.yRot = toward + 0.12F;
			model.rightArm.zRot = 0.0F;
			model.leftArm.zRot = 0.0F;
			model.rightLeg.xRot = -0.45F - swing;
			model.leftLeg.xRot = -0.15F + swing;
			model.rightLeg.yRot = toward * 0.5F;
			model.leftLeg.yRot = toward * 0.5F;
		}
		if (model instanceof PlayerModel<?> playerModel) {
			playerModel.rightSleeve.copyFrom(model.rightArm);
			playerModel.leftSleeve.copyFrom(model.leftArm);
			playerModel.rightPants.copyFrom(model.rightLeg);
			playerModel.leftPants.copyFrom(model.leftLeg);
		}
		return true;
	}
}
