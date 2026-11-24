package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public final class MnemonistPuppetModel extends HierarchicalModel<MnemonistPuppetEntity> {
	private final ModelPart root = MnemonistPuppetMesh.createRoot();
	private final ModelPart pelvis = root.getChild("pelvis");
	private final ModelPart torso = pelvis.getChild("rib_cage");
	private final ModelPart head = torso.getChild("head");
	private final ModelPart jaw = head.getChild("jaw");
	private final ModelPart spool = torso.getChild("memory_spool");
	private final ModelPart dorsalLead = torso.getChild("dorsal_lead");
	private final Side right = side("right");
	private final Side left = side("left");

	@Override
	public ModelPart root() {
		return root;
	}

	public void translateToSpool(PoseStack stack) {
		root.translateAndRotate(stack);
		pelvis.translateAndRotate(stack);
		torso.translateAndRotate(stack);
		stack.translate(spool.x / 16.0F, spool.y / 16.0F, spool.z / 16.0F);
	}

	@Override
	public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0, 1);
		float encore = Mth.clamp(entity.encoreAnimationAge(partialTick) / MnemonistPuppetEntity.ENCORE_POSE_TICKS, 0, 1);
		float hurt = entity.hurtTime > 0 ? (entity.hurtTime - partialTick) / Math.max(1, entity.hurtDuration) : 0;
		float slack = entity.deathTime > 0 ? Mth.clamp((entity.deathTime + partialTick) / 20, 0, 1) : 0;
		HumanoidArm strikingArm = entity.getMainArm();
		if (entity.swingingArm == InteractionHand.OFF_HAND) strikingArm = strikingArm.getOpposite();
		pose(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, strikingArm == HumanoidArm.LEFT,
				attackTime, encore, hurt, slack);
	}

	void pose(float stride, float amount, float age, float yaw, float pitch, boolean leftStrike,
			  float attack, float encore, float hurt, float slack) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float gait = Mth.clamp(amount, 0, 1);
		float phase = stride * 0.88F;
		float life = 1 - slack;
		float strike = Mth.sin(attack * Mth.PI) * life;
		float replay = Mth.sin(encore * Mth.PI) * life;
		float twitch = Mth.sin(age * 2.1F) * hurt * life;

		pelvis.y += 1.8F * slack - Mth.abs(Mth.sin(phase)) * gait * 0.8F;
		torso.xRot += gait * 0.12F + strike * 0.30F - replay * 0.26F + slack * 0.52F;
		torso.yRot += Mth.sin(phase) * gait * 0.12F + (leftStrike ? -1 : 1) * strike * 0.23F;
		head.yRot += Mth.clamp(yaw, -65, 65) * Mth.DEG_TO_RAD * life;
		head.xRot += Mth.clamp(pitch, -45, 45) * Mth.DEG_TO_RAD * life - replay * 0.30F + slack * 0.36F;
		head.zRot += 0.13F + Mth.sin(age * 0.15F) * 0.09F * life + twitch * 0.13F;
		jaw.xRot += strike * 0.30F + replay * 0.73F + slack * 0.22F;
		spool.zRot += age * 0.035F + strike * 0.22F + Mth.TWO_PI * encore;
		dorsalLead.xRot += Mth.sin(age * 0.2F) * 0.08F + replay * 0.24F + slack * 0.85F;

		animateSide(right, phase, gait, age, -1, leftStrike ? 0 : strike, replay, twitch, slack);
		animateSide(left, phase + Mth.PI, gait, age, 1, leftStrike ? strike : 0, replay, twitch, slack);
	}

	private static void animateSide(Side side, float phase, float gait, float age, int sign,
									float strike, float replay, float twitch, float slack) {
		float step = Mth.cos(phase);
		float lift = Math.max(0, Mth.sin(phase));
		side.hip().xRot += step * gait * 0.78F - slack * 0.3F;
		side.knee().xRot += lift * gait * 0.87F + slack * 0.62F;
		side.ankle().xRot -= (step * 0.40F + lift * 0.48F) * gait;
		float reach = Mth.cos(phase - 0.6F) * gait * 0.42F - strike * 1.74F - replay * 0.38F;
		side.shoulder().xRot += reach;
		side.shoulder().zRot += sign * (0.05F + replay * 0.36F - slack * 0.17F);
		side.elbow().xRot -= 0.10F + Math.max(0, step) * gait * 0.45F + strike * 0.28F + replay * 0.55F;
		side.hand().xRot += Mth.sin(phase - 0.9F) * gait * 0.23F + twitch * 0.22F;
		for (int finger = 0; finger < 3; finger++) {
			side.fingers()[finger].xRot += 0.11F + strike * 0.5F - replay * 0.33F
					+ Mth.sin(age * 0.15F + finger) * 0.09F;
		}
		// The lead rises from the eyelet even as the arm swings below it.
		side.lead().xRot -= reach * 0.90F - Mth.sin(age * 0.2F + sign) * 0.07F - slack * 0.8F;
		side.lead().zRot -= side.shoulder().zRot * 0.75F;
	}

	private Side side(String name) {
		ModelPart shoulder = torso.getChild(name + "_shoulder");
		ModelPart elbow = shoulder.getChild(name + "_elbow");
		ModelPart hand = elbow.getChild(name + "_hand");
		ModelPart hip = pelvis.getChild(name + "_hip");
		ModelPart knee = hip.getChild(name + "_knee");
		return new Side(shoulder, elbow, hand, shoulder.getChild(name + "_lead"), hip, knee,
				knee.getChild(name + "_ankle"), new ModelPart[] {
						hand.getChild(name + "_finger_1"), hand.getChild(name + "_finger_2"), hand.getChild(name + "_finger_3")
				});
	}

	private record Side(ModelPart shoulder, ModelPart elbow, ModelPart hand, ModelPart lead,
						ModelPart hip, ModelPart knee, ModelPart ankle, ModelPart[] fingers) {}
}
