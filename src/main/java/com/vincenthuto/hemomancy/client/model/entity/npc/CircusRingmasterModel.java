package com.vincenthuto.hemomancy.client.model.entity.npc;

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

	@Override
	public void setupAnim(CircusRingmasterEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
		float breath = Mth.sin(ageInTicks * 0.045F) * 0.025F;
		body.xRot = 0.18F + breath;
		head.xRot = -0.1F - breath;
		head.yRot = 0.0F;
		rightArm.xRot = -0.55F;
		rightArm.zRot = 0.12F;
		leftArm.xRot = -0.2F;
		leftArm.zRot = -0.18F;
		rightLeg.xRot = leftLeg.xRot = -1.18F;
		rightLeg.yRot = 0.12F;
		leftLeg.yRot = -0.12F;
		rightCalf.xRot = leftCalf.xRot = 1.38F;
		float pulse = 1.0F + Mth.sin(ageInTicks * 0.11F) * 0.04F;
		staffTopper.xScale = staffTopper.yScale = staffTopper.zScale = pulse;
	}
}
