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
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
				.texOffs(32, 0).addBox(-4.0F, -7.5F, -4.2F, 8.0F, 7.5F, 8.2F, new CubeDeformation(0.35F))
				.texOffs(0, 18).addBox(-3.2F, -5.2F, -4.8F, 6.4F, 2.0F, 0.8F), PartPose.ZERO);
		PartDefinition topHat = head.addOrReplaceChild("top_hat", CubeListBuilder.create()
				.texOffs(0, 22).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.5F, 14.0F)
				.texOffs(0, 38).addBox(-4.7F, -10.0F, -4.7F, 9.4F, 9.0F, 9.4F)
				.texOffs(40, 38).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 2.0F, 10.0F),
				PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.08F, 0.0F, -0.06F));
		topHat.addOrReplaceChild("hat_brooch", CubeListBuilder.create()
				.texOffs(72, 0).addBox(-1.5F, -1.5F, -0.7F, 3.0F, 3.0F, 1.0F)
				.texOffs(80, 0).addBox(-0.5F, -2.5F, -0.8F, 1.0F, 5.0F, 1.0F),
				PartPose.offset(0.0F, -3.0F, -4.7F));
	root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 57).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
				.texOffs(48, 54).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.5F, 4.0F, new CubeDeformation(0.45F))
				.texOffs(0, 74).addBox(-5.0F, 0.0F, -2.7F, 10.0F, 3.5F, 5.4F, new CubeDeformation(0.2F))
				.texOffs(32, 74).addBox(-4.8F, 8.5F, -2.6F, 9.6F, 2.0F, 5.2F), PartPose.ZERO);
		body.addOrReplaceChild("coat_left", CubeListBuilder.create().texOffs(0, 84)
				.addBox(-0.4F, 0.0F, -2.3F, 4.4F, 8.0F, 4.6F), PartPose.offset(0.2F, 10.5F, 0.0F));
		body.addOrReplaceChild("coat_right", CubeListBuilder.create().texOffs(20, 84)
				.addBox(-4.0F, 0.0F, -2.3F, 4.4F, 8.0F, 4.6F), PartPose.offset(-0.2F, 10.5F, 0.0F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(40, 57).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(72, 10).addBox(-3.5F, -2.7F, -2.7F, 5.0F, 5.0F, 5.4F, new CubeDeformation(0.35F))
				.texOffs(72, 24).addBox(-4.6F, -2.8F, -3.8F, 7.0F, 1.0F, 7.6F), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition staff = rightArm.addOrReplaceChild("living_staff", CubeListBuilder.create()
				.texOffs(96, 0).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 38.0F, 2.0F)
				.texOffs(104, 0).addBox(-1.5F, 20.0F, -1.5F, 3.0F, 8.0F, 3.0F),
				PartPose.offsetAndRotation(-1.0F, 5.0F, -3.0F, -0.18F, 0.0F, 0.12F));
		PartDefinition topper = staff.addOrReplaceChild("staff_topper", CubeListBuilder.create()
				.texOffs(96, 44).addBox(-3.0F, -5.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-0.25F))
				.texOffs(96, 58).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(0.0F, -9.0F, 0.0F));
		topper.addOrReplaceChild("topper_spines", CubeListBuilder.create()
				.texOffs(112, 58).addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F)
				.texOffs(112, 62).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 10.0F, 1.0F), PartPose.ZERO);

		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 57).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(72, 10).mirror().addBox(-1.5F, -2.7F, -2.7F, 5.0F, 5.0F, 5.4F, new CubeDeformation(0.35F))
				.texOffs(72, 24).mirror().addBox(-2.4F, -2.8F, -3.8F, 7.0F, 1.0F, 7.6F), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(40, 84).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(56, 84).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(40, 96).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(56, 96).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.3F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));
		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(40, 84).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(56, 84).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(40, 96).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(56, 96).mirror().addBox(-2.0F, 0.0F, -3.0F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.3F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));
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
