package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class CircusRingmasterModel extends HumanoidModel<CircusRingmasterEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_ringmaster"), "main");
	private final ModelPart leftCalf;
	private final ModelPart rightCalf;
	private final ModelPart staffTopper;

	public CircusRingmasterModel(ModelPart root) {
		super(root);
		leftCalf = leftLeg.getChild("left_calf");
		rightCalf = rightLeg.getChild("right_calf");
		staffTopper = rightArm.getChild("living_staff").getChild("staff_topper");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		PartDefinition topHat = head.addOrReplaceChild("top_hat", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-6.0F, -1.0F, -5.0F, 12.0F, 1.0F, 10.0F)
				.texOffs(48, 18).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 5.0F, 8.0F)
				.texOffs(80, 18).addBox(-4.5F, -3.5F, -4.5F, 9.0F, 2.0F, 9.0F),
				PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.035F, 0.0F, -0.035F));
		topHat.addOrReplaceChild("hat_brooch", CubeListBuilder.create()
				.texOffs(116, 18).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F),
				PartPose.offset(0.0F, -2.6F, -4.45F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(24, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F,
						new CubeDeformation(0.12F)), PartPose.ZERO);
		body.addOrReplaceChild("left_lapel", CubeListBuilder.create()
				.texOffs(48, 32).addBox(0.0F, 0.0F, -0.7F, 2.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(0.15F, 0.7F, -2.1F, 0.0F, 0.0F, -0.18F));
		body.addOrReplaceChild("right_lapel", CubeListBuilder.create()
				.texOffs(48, 32).mirror().addBox(-2.0F, 0.0F, -0.7F, 2.0F, 7.0F, 1.0F).mirror(false),
				PartPose.offsetAndRotation(-0.15F, 0.7F, -2.1F, 0.0F, 0.0F, 0.18F));
		body.addOrReplaceChild("waistcoat", CubeListBuilder.create()
				.texOffs(56, 32).addBox(-4.5F, -1.0F, -2.5F, 9.0F, 2.0F, 5.0F),
				PartPose.offset(0.0F, 9.0F, 0.0F));
		body.addOrReplaceChild("coat_clasp", CubeListBuilder.create()
				.texOffs(84, 32).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
				PartPose.offset(0.0F, 8.8F, -2.7F));
		body.addOrReplaceChild("coat_left", CubeListBuilder.create()
				.texOffs(0, 50).addBox(0.0F, 0.0F, 1.5F, 4.0F, 8.0F, 1.0F),
				PartPose.offsetAndRotation(0.15F, 10.5F, 0.0F, 0.08F, 0.0F, 0.04F));
		body.addOrReplaceChild("coat_right", CubeListBuilder.create()
				.texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 1.5F, 4.0F, 8.0F, 1.0F).mirror(false),
				PartPose.offsetAndRotation(-0.15F, 10.5F, 0.0F, 0.08F, 0.0F, -0.04F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 64).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("right_pauldron_upper", CubeListBuilder.create()
				.texOffs(16, 64).addBox(-3.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 0.0F, 0.0F, 0.0F, 0.12F));
		rightArm.addOrReplaceChild("right_cuff", CubeListBuilder.create()
				.texOffs(90, 64).addBox(-3.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F),
				PartPose.offset(0.0F, 8.7F, 0.0F));
		PartDefinition staff = rightArm.addOrReplaceChild("living_staff", CubeListBuilder.create()
				.texOffs(0, 96).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 30.0F, 2.0F)
				.texOffs(8, 96).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 7.0F, 3.0F)
				.texOffs(20, 96).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 1.0F, 3.0F)
				.texOffs(20, 96).addBox(-1.5F, 5.0F, -1.5F, 3.0F, 1.0F, 3.0F),
				PartPose.offsetAndRotation(-3.0F, 5.0F, -3.0F, -0.12F, 0.0F, 0.08F));
		PartDefinition staffTopper = staff.addOrReplaceChild("staff_topper", CubeListBuilder.create()
				.texOffs(84, 96).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F),
				PartPose.offset(0.0F, -12.0F, 0.0F));
		staffTopper.addOrReplaceChild("ring_top", CubeListBuilder.create()
				.texOffs(40, 96).addBox(-4.0F, -0.5F, -1.0F, 8.0F, 1.0F, 2.0F),
				PartPose.offset(0.0F, -7.5F, 0.0F));
		staffTopper.addOrReplaceChild("ring_bottom", CubeListBuilder.create()
				.texOffs(40, 96).addBox(-4.0F, -0.5F, -1.0F, 8.0F, 1.0F, 2.0F),
				PartPose.offset(0.0F, -0.5F, 0.0F));
		staffTopper.addOrReplaceChild("ring_left", CubeListBuilder.create()
				.texOffs(60, 96).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
				PartPose.offset(-3.5F, -4.0F, 0.0F));
		staffTopper.addOrReplaceChild("ring_right", CubeListBuilder.create()
				.texOffs(60, 96).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
				PartPose.offset(3.5F, -4.0F, 0.0F));
		PartDefinition core = staffTopper.addOrReplaceChild("blood_core", CubeListBuilder.create()
				.texOffs(68, 96).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, -4.0F, -0.15F, 0.0F, 0.0F, 0.7854F));
		core.addOrReplaceChild("blood_core_drop", CubeListBuilder.create()
				.texOffs(78, 96).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
				PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(0, 64).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror(false),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("left_pauldron_upper", CubeListBuilder.create()
				.texOffs(16, 64).mirror().addBox(-1.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 0.0F, 0.0F, 0.0F, -0.12F));
		leftArm.addOrReplaceChild("left_cuff", CubeListBuilder.create()
				.texOffs(90, 64).mirror().addBox(-1.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F).mirror(false),
				PartPose.offset(0.0F, 8.7F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 82).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
		rightLeg.addOrReplaceChild("right_knee", CubeListBuilder.create()
				.texOffs(88, 82).addBox(-2.0F, -1.0F, -2.5F, 4.0F, 2.0F, 1.0F),
				PartPose.offset(0.0F, 5.0F, -0.3F));
		PartDefinition rightCalf = rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(48, 82).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 6.0F, 5.0F,
						new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
		rightCalf.addOrReplaceChild("right_toe", CubeListBuilder.create()
				.texOffs(66, 82).addBox(-2.0F, -1.0F, -3.5F, 4.0F, 2.0F, 5.0F),
				PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(0, 82).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));
		leftLeg.addOrReplaceChild("left_knee", CubeListBuilder.create()
				.texOffs(88, 82).mirror().addBox(-2.0F, -1.0F, -2.5F, 4.0F, 2.0F, 1.0F).mirror(false),
				PartPose.offset(0.0F, 5.0F, -0.3F));
		PartDefinition leftCalf = leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(48, 82).mirror().addBox(-2.0F, 0.0F, -2.5F, 4.0F, 6.0F, 5.0F,
						new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 6.0F, 0.0F));
		leftCalf.addOrReplaceChild("left_toe", CubeListBuilder.create()
				.texOffs(66, 82).mirror().addBox(-2.0F, -1.0F, -3.5F, 4.0F, 2.0F, 5.0F).mirror(false),
				PartPose.offset(0.0F, 5.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	/** The throw's whole beat: the wind-up the server waits before the knife leaves, then the follow-through. */
	private static final float THROW_TICKS = CircusRingmasterEntity.KNIFE_THROW.total();
	/** Where in the beat the knife leaves: the commit tick of the server's throw. */
	private static final float THROW_RELEASE = CircusRingmasterEntity.KNIFE_THROW.commitTick() / THROW_TICKS;
	private static final float STRIKE_TICKS = CircusRingmasterEntity.STAFF_STRIKE.total();
	private static final float STRIKE_COMMIT = CircusRingmasterEntity.STAFF_STRIKE.commitTick();

	/**
	 * Perched in the rafters he crouches on the beam, staff planted, only the head and the throwing arm moving. On
	 * the carousel he stands and presents, staff raised, the free arm thrown wide. Descended, he walks and strikes
	 * like any man, the calves bending under the vanilla stride and the vanilla swing on the staff arm.
	 */
	@Override
	public void setupAnim(CircusRingmasterEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		CircusPavilionStateRules.Phase phase = entity.phase();
		boolean onFoot = phase == CircusPavilionStateRules.Phase.DESCENT
				|| phase == CircusPavilionStateRules.Phase.COMPLETE;
		super.setupAnim(entity, onFoot ? limbSwing : 0.0F, onFoot ? limbSwingAmount : 0.0F, ageInTicks, netHeadYaw,
				headPitch);
		leftCalf.resetPose();
		rightCalf.resetPose();
		float breath = Mth.sin(ageInTicks * 0.045F) * 0.025F;
		if (onFoot) {
			stride(breath);
		} else if (phase == CircusPavilionStateRules.Phase.CAROUSEL) {
			present(ageInTicks, breath);
		} else {
			perch(breath);
		}
		entity.throwState.updateTime(ageInTicks, 1.0F);
		if (entity.throwState.isStarted()) {
			float ticks = entity.throwState.getAccumulatedTime() / 50.0F;
			if (ticks > THROW_TICKS) entity.throwState.stop();
			else throwBeat(ticks / THROW_TICKS);
		}
		entity.strikeState.updateTime(ageInTicks, 1.0F);
		if (entity.strikeState.isStarted()) {
			float ticks = entity.strikeState.getAccumulatedTime() / 50.0F;
			if (ticks > STRIKE_TICKS) entity.strikeState.stop();
			else staffStrike(ticks);
		}
		float pulse = 1.0F + Mth.sin(ageInTicks * 0.11F) * (phase == CircusPavilionStateRules.Phase.CAROUSEL ? 0.09F : 0.04F);
		staffTopper.xScale = staffTopper.yScale = staffTopper.zScale = pulse;
	}

	/** Crouched on the beam: the body leans out over the ring, the staff arm plants, the legs fold under. */
	private void perch(float breath) {
		body.xRot = 0.18F + breath;
		head.xRot += -0.1F - breath;
		rightArm.xRot = -0.55F;
		rightArm.zRot = 0.12F;
		leftArm.xRot = -0.2F;
		leftArm.zRot = -0.18F;
		rightLeg.xRot = leftLeg.xRot = -1.18F;
		rightLeg.yRot = 0.12F;
		leftLeg.yRot = -0.12F;
		rightCalf.xRot = leftCalf.xRot = 1.38F;
	}

	/** Standing on the carousel's hub, presenting: staff raised high, the free arm thrown out to the crowd. */
	private void present(float ageInTicks, float breath) {
		float sway = Mth.sin(ageInTicks * 0.07F);
		body.xRot = -0.08F + breath;
		body.zRot = 0.03F * sway;
		head.xRot += -0.15F;
		rightArm.xRot = -2.6F + 0.05F * sway;
		rightArm.zRot = 0.25F;
		leftArm.xRot = -0.35F;
		leftArm.zRot = -1.15F + 0.06F * sway;
		leftArm.yRot = 0.2F;
		rightLeg.xRot = -0.05F;
		leftLeg.xRot = 0.05F;
		rightLeg.yRot = 0.1F;
		leftLeg.yRot = -0.1F;
	}

	/** Walking and striking: the vanilla stride and swing stand, the calves bend behind the knee, the staff plants. */
	private void stride(float breath) {
		body.xRot += 0.06F + breath;
		rightCalf.xRot = Math.max(0.0F, -rightLeg.xRot) * 0.9F;
		leftCalf.xRot = Math.max(0.0F, -leftLeg.xRot) * 0.9F;
		rightArm.xRot -= 0.35F;
		rightArm.zRot += 0.1F;
	}

	/**
	 * The free arm draws back behind the shoulder through the server's wind-up, holds, then whips out and down on the
	 * commit tick, when the knife leaves; the follow-through settles it.
	 */
	private void throwBeat(float t) {
		float whipStart = THROW_RELEASE - 0.06F;
		float whipEnd = THROW_RELEASE + 0.05F;
		float wind = HemoEasing.smoothstep(0.0F, THROW_RELEASE * 0.55F, t) * (1.0F - HemoEasing.smoothstep(whipStart, whipEnd, t));
		float whip = HemoEasing.smoothstep(whipStart, whipEnd, t) * (1.0F - HemoEasing.smoothstep(0.85F, 1.0F, t));
		leftArm.xRot = Mth.lerp(wind, leftArm.xRot, -2.9F);
		leftArm.zRot = Mth.lerp(wind, leftArm.zRot, -0.4F);
		leftArm.xRot = Mth.lerp(whip, leftArm.xRot, -0.9F);
		leftArm.yRot = Mth.lerp(whip, leftArm.yRot, 0.45F);
		body.yRot = Mth.lerp(whip, body.yRot, -0.25F);
		head.yRot = Mth.lerp(whip, head.yRot, 0.2F);
	}

	/** The staff comes up over the head through the wind-up, the body rearing back, then cracks down on the commit tick. */
	private void staffStrike(float ticks) {
		float raise = HemoEasing.smoothstep(0.0F, STRIKE_COMMIT, ticks)
				* (1.0F - HemoEasing.smoothstep(STRIKE_COMMIT - 0.5F, STRIKE_COMMIT + 1.0F, ticks));
		float smash = HemoEasing.smoothstep(STRIKE_COMMIT - 0.5F, STRIKE_COMMIT + 1.0F, ticks)
				* (1.0F - HemoEasing.smoothstep(STRIKE_COMMIT + 2.0F, STRIKE_TICKS, ticks));
		rightArm.xRot = Mth.lerp(raise, rightArm.xRot, -3.0F);
		rightArm.zRot = Mth.lerp(raise, rightArm.zRot, -0.15F);
		body.xRot = Mth.lerp(raise, body.xRot, -0.2F);
		head.xRot = Mth.lerp(raise, head.xRot, -0.25F);
		rightArm.xRot = Mth.lerp(smash, rightArm.xRot, -0.75F);
		body.xRot = Mth.lerp(smash, body.xRot, 0.25F);
		leftArm.xRot = Mth.lerp(raise, leftArm.xRot, -0.9F);
	}
}
