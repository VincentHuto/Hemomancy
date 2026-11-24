package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

/**
 * Walks like a heron: each foot lifts high with its toes curled, comes forward and is set down flat while the head thrusts
 * in time. Fleeing, it levels out with its neck stretched and wings half open. Standing, it now and then bends its neck to
 * peer sideways at the ground. The mesh is baked from the Blockbench project.
 */
public final class VenousStriderModel extends HierarchicalModel<VenousStriderEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("venous_strider_model"), "main");
	private static final float STRIDE_RATE = 0.85F;
	/** Its stroll is slow enough to give a small limb swing amount, so the stride is scaled up to full height. */
	private static final float STRIDE_GAIN = 2.2F;
	private static final float PEER_PERIOD = 170.0F;
	private static final float PEER_TICKS = 50.0F;

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart neckBase;
	private final ModelPart neckFold;
	private final ModelPart neckUpper;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart crest;
	private final ModelPart tail;
	private final Wing leftWing;
	private final Wing rightWing;
	private final Leg leftLeg;
	private final Leg rightLeg;

	private record Wing(ModelPart shoulder, ModelPart primaries, float side) {
		static Wing of(ModelPart body, String prefix) {
			ModelPart shoulder = body.getChild(prefix + "_wing");
			return new Wing(shoulder, shoulder.getChild(prefix + "_primaries"), Math.signum(shoulder.x));
		}

		void open(float amount) {
			shoulder.zRot -= amount * side;
			shoulder.yRot += amount * 0.35F * side;
			primaries.yRot += amount * 0.3F * side;
		}
	}

	private record Leg(ModelPart tibia, ModelPart hock, ModelPart foot, float tibiaLength, float tarsusLength,
			float restTibiaAngle, float restTarsusAngle, float restFootY, float restFootZ) {
		static Leg of(ModelPart root, String prefix) {
			ModelPart tibia = root.getChild(prefix + "_leg");
			ModelPart hock = tibia.getChild(prefix + "_hock");
			ModelPart foot = hock.getChild(prefix + "_foot");
			return new Leg(tibia, hock, foot,
					(float) Math.hypot(hock.y, hock.z), (float) Math.hypot(foot.y, foot.z),
					(float) Math.atan2(hock.z, hock.y), (float) Math.atan2(foot.z, foot.y),
					hock.y + foot.y, hock.z + foot.z);
		}

		void stride(float phase, float reach, float amount) {
			float lift = Math.max(0.0F, Mth.sin(phase)) * amount;
			float targetY = restFootY - 3.25F * lift;
			float targetZ = restFootZ + Mth.cos(phase) * reach * amount;
			float distance = (float) Math.hypot(targetY, targetZ);
			float distanceSquared = distance * distance;
			float tibiaSquared = tibiaLength * tibiaLength;
			float tarsusSquared = tarsusLength * tarsusLength;
			// Solve in the side plane with the hock bending backward throughout the step.
			float tibiaAngle = (float) Math.atan2(targetZ, targetY)
					+ angle((tibiaSquared + distanceSquared - tarsusSquared) / (2.0F * tibiaLength * distance));
			float jointAngle = -angle((distanceSquared - tibiaSquared - tarsusSquared) / (2.0F * tibiaLength * tarsusLength));
			tibia.xRot = tibiaAngle - restTibiaAngle;
			hock.xRot = jointAngle + restTibiaAngle - restTarsusAngle;
			foot.xRot = lift * 0.6F - tibia.xRot - hock.xRot;
		}

		private static float angle(float cosine) {
			return (float) Math.acos(Mth.clamp(cosine, -1.0F, 1.0F));
		}
	}

	public VenousStriderModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		body = root.getChild("body");
		neckBase = body.getChild("neck_base");
		neckFold = neckBase.getChild("neck_fold");
		neckUpper = neckFold.getChild("neck_upper");
		head = neckUpper.getChild("head");
		jaw = head.getChild("lower_bill");
		crest = head.getChild("crest");
		tail = body.getChild("tail");
		leftWing = Wing.of(body, "left");
		rightWing = Wing.of(body, "right");
		leftLeg = Leg.of(root, "left");
		rightLeg = Leg.of(root, "right");
	}

	public static LayerDefinition createBodyLayer() {
		return VenousStriderMesh.createBodyLayer();
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(VenousStriderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float amount = Math.min(limbSwingAmount * STRIDE_GAIN, 1.0F);
		float run = Mth.clamp((limbSwingAmount - 0.42F) / 0.38F, 0.0F, 1.0F);
		float age = ageInTicks + entity.getId() * 23;
		walk(limbSwing * STRIDE_RATE, amount, run);
		stand(age, 1.0F - amount);

		crest.xRot += Mth.sin(age * 0.07F) * 0.05F - run * 0.2F;
		neckUpper.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.3F;
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.7F;
		head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
	}

	private void walk(float phase, float amount, float run) {
		float reach = 1.9F + run * 1.7F;
		leftLeg.stride(phase, reach, amount);
		rightLeg.stride(phase + Mth.PI, reach, amount);
		float thrust = Mth.sin(phase * 2.0F) * amount;
		neckBase.xRot += thrust * 0.07F + run * 0.32F;
		neckFold.xRot += thrust * 0.09F + run * 0.40F;
		neckUpper.xRot -= thrust * 0.03F + run * 0.16F;
		head.xRot -= thrust * 0.13F + run * 0.70F;
		body.y -= Math.abs(Mth.cos(phase)) * 0.4F * amount;
		body.xRot += run * 0.14F;
		tail.xRot -= run * 0.14F;
		leftWing.open(run * 0.48F);
		rightWing.open(run * 0.48F);
	}

	private void stand(float age, float still) {
		float peer = peer(age) * still;
		neckBase.xRot += peer * 0.22F + Mth.sin(age * 0.04F) * 0.012F * still;
		neckFold.xRot += peer * 0.26F;
		neckUpper.xRot -= peer * 0.14F;
		head.xRot += peer * 0.18F;
		head.zRot += peer * 0.45F * (((int) (age / PEER_PERIOD)) % 2 == 0 ? 1.0F : -1.0F);
		jaw.xRot += Math.max(0.0F, Mth.sin(age * 0.03F) - 0.9F) * 3.0F * still;
	}

	/** 0 to 1 and back over the first PEER_TICKS of every PEER_PERIOD. */
	private static float peer(float age) {
		return Mth.sin(Math.min(age % PEER_PERIOD, PEER_TICKS) / PEER_TICKS * Mth.PI);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
