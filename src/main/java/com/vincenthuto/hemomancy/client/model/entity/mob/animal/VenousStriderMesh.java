package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Baked from VenousStriderModel.bbmodel by tools/model_export/venous_strider.py. */
public final class VenousStriderMesh {
	private VenousStriderMesh() {
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, -17F, 0F));
		body.addOrReplaceChild("body_cube_1", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3F, -3F, -3.75F, 6F, 6F, 7F), PartPose.offsetAndRotation(0F, -0.5F, -1F, 0.139626F, 0F, 0F));
		body.addOrReplaceChild("body_cube_2", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-2.5F, -2.25F, -1.25F, 5F, 5F, 5F), PartPose.offsetAndRotation(0F, -0.5F, 2F, 0.226893F, 0F, 0F));
		body.addOrReplaceChild("body_cube_3", CubeListBuilder.create()
				.texOffs(48, 0).addBox(-1.5F, -0.75F, -0.75F, 3F, 5F, 2F), PartPose.offsetAndRotation(0F, -2F, -4.75F, 0.349066F, 0F, 0F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0F, 0F, 4.75F));
		tail.addOrReplaceChild("tail_cube_1", CubeListBuilder.create()
				.texOffs(59, 0).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 4F), PartPose.offsetAndRotation(1.5F, 0F, 0F, -0.226893F, 0.20944F, 0F));
		tail.addOrReplaceChild("tail_cube_2", CubeListBuilder.create()
				.texOffs(70, 0).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 5F), PartPose.offsetAndRotation(0F, -0.25F, 0.25F, -0.226893F, 0F, 0F));
		tail.addOrReplaceChild("tail_cube_3", CubeListBuilder.create()
				.texOffs(59, 0).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 4F), PartPose.offsetAndRotation(-1.5F, 0F, 0F, -0.226893F, -0.20944F, 0F));
		PartDefinition neck_base = body.addOrReplaceChild("neck_base", CubeListBuilder.create(), PartPose.offset(0F, -2F, -4.25F));
		neck_base.addOrReplaceChild("neck_base_cube_1", CubeListBuilder.create()
				.texOffs(83, 0).addBox(-1.5F, -4F, -1F, 3F, 5F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.593412F, 0F, 0F));
		PartDefinition neck_fold = neck_base.addOrReplaceChild("neck_fold", CubeListBuilder.create(), PartPose.offset(0F, -3.25F, -2.25F));
		neck_fold.addOrReplaceChild("neck_fold_cube_1", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-1F, -4F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.593412F, 0F, 0F));
		PartDefinition neck_upper = neck_fold.addOrReplaceChild("neck_upper", CubeListBuilder.create(), PartPose.offset(0F, -3.25F, 2.25F));
		neck_upper.addOrReplaceChild("neck_upper_cube_1", CubeListBuilder.create()
				.texOffs(103, 0).addBox(-1F, -4F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(-0.25F, 0F, 0F, 0.383972F, 0F, 0F));
		PartDefinition head = neck_upper.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(112, 0).addBox(-1.5F, -2.25F, -3F, 3F, 3F, 5F), PartPose.offset(0F, -3.75F, -1.5F));
		PartDefinition crest = head.addOrReplaceChild("crest", CubeListBuilder.create(), PartPose.offset(0F, -1.5F, 1F));
		crest.addOrReplaceChild("crest_cube_1", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-0.5F, -0.75F, -0.25F, 1F, 1F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.279253F, 0F, 0F));
		crest.addOrReplaceChild("crest_cube_2", CubeListBuilder.create()
				.texOffs(13, 14).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 4F), PartPose.offsetAndRotation(0F, 0.25F, 0.25F, -0.453786F, -0.244346F, 0F));
		PartDefinition bill = head.addOrReplaceChild("bill", CubeListBuilder.create(), PartPose.offset(0F, -0.25F, -2.75F));
		bill.addOrReplaceChild("bill_cube_1", CubeListBuilder.create()
				.texOffs(24, 14).addBox(-1F, -1.5F, -2.5F, 2F, 2F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.087266F, 0F, 0F));
		bill.addOrReplaceChild("bill_cube_2", CubeListBuilder.create()
				.texOffs(35, 14).addBox(-1F, -1F, -5.25F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.087266F, 0F, 0F));
		bill.addOrReplaceChild("bill_cube_3", CubeListBuilder.create()
				.texOffs(46, 14).addBox(-0.5F, -1F, -7F, 1F, 1F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.087266F, 0F, 0F));
		PartDefinition lower_bill = head.addOrReplaceChild("lower_bill", CubeListBuilder.create(), PartPose.offset(0F, 0.25F, -2.75F));
		lower_bill.addOrReplaceChild("lower_bill_cube_1", CubeListBuilder.create()
				.texOffs(53, 14).addBox(-0.5F, -0.75F, -4.25F, 1F, 1F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.122173F, 0F, 0F));
		PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(-2.5F, -2F, -4F));
		left_wing.addOrReplaceChild("left_wing_cube_1", CubeListBuilder.create()
				.texOffs(66, 14).addBox(-1.25F, -1.75F, -1.25F, 2F, 4F, 7F), PartPose.offsetAndRotation(0F, 1F, 0.5F, 0.122173F, 0.087266F, 0F));
		left_wing.addOrReplaceChild("left_wing_cube_2", CubeListBuilder.create()
				.texOffs(85, 14).addBox(-1F, -1F, -1F, 2F, 2F, 7F), PartPose.offsetAndRotation(0F, -0.75F, 0.75F, -0.139626F, -0.15708F, 0F));
		left_wing.addOrReplaceChild("left_wing_cube_3", CubeListBuilder.create()
				.texOffs(104, 14).addBox(-0.75F, -1.5F, -0.5F, 2F, 3F, 6F), PartPose.offsetAndRotation(-0.25F, 1.25F, 3.75F, -0.191986F, -0.139626F, 0F));
		PartDefinition left_primaries = left_wing.addOrReplaceChild("left_primaries", CubeListBuilder.create(), PartPose.offset(-0.25F, 1.25F, 5.75F));
		left_primaries.addOrReplaceChild("left_primaries_cube_1", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F), PartPose.offsetAndRotation(0F, -0.5F, -1F, -0.174533F, 0.139626F, 0F));
		left_primaries.addOrReplaceChild("left_primaries_cube_2", CubeListBuilder.create()
				.texOffs(19, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F), PartPose.offsetAndRotation(-0.25F, 0.25F, -1.75F, -0.244346F, 0.139626F, 0F));
		left_primaries.addOrReplaceChild("left_primaries_cube_3", CubeListBuilder.create()
				.texOffs(38, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F), PartPose.offsetAndRotation(-0.25F, 1F, -2.5F, -0.296706F, 0.139626F, 0F));
		left_primaries.addOrReplaceChild("left_primaries_cube_4", CubeListBuilder.create()
				.texOffs(55, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F), PartPose.offsetAndRotation(-0.25F, 1.75F, -3.25F, -0.366519F, 0.139626F, 0F));
		PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(2.5F, -2F, -4F));
		right_wing.addOrReplaceChild("right_wing_cube_1", CubeListBuilder.create()
				.texOffs(66, 14).addBox(-0.75F, -1.75F, -1.25F, 2F, 4F, 7F), PartPose.offsetAndRotation(0F, 1F, 0.5F, 0.122173F, -0.087266F, 0F));
		right_wing.addOrReplaceChild("right_wing_cube_2", CubeListBuilder.create()
				.texOffs(85, 14).addBox(-1F, -1F, -1F, 2F, 2F, 7F), PartPose.offsetAndRotation(0F, -0.75F, 0.75F, -0.139626F, 0.15708F, 0F));
		right_wing.addOrReplaceChild("right_wing_cube_3", CubeListBuilder.create()
				.texOffs(104, 14).addBox(-1.25F, -1.5F, -0.5F, 2F, 3F, 6F), PartPose.offsetAndRotation(0.25F, 1.25F, 3.75F, -0.191986F, 0.139626F, 0F));
		PartDefinition right_primaries = right_wing.addOrReplaceChild("right_primaries", CubeListBuilder.create(), PartPose.offset(0.25F, 1.25F, 5.75F));
		right_primaries.addOrReplaceChild("right_primaries_cube_1", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F), PartPose.offsetAndRotation(0F, -0.5F, -1F, -0.174533F, -0.139626F, 0F));
		right_primaries.addOrReplaceChild("right_primaries_cube_2", CubeListBuilder.create()
				.texOffs(19, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F), PartPose.offsetAndRotation(0.25F, 0.25F, -1.75F, -0.244346F, -0.139626F, 0F));
		right_primaries.addOrReplaceChild("right_primaries_cube_3", CubeListBuilder.create()
				.texOffs(38, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F), PartPose.offsetAndRotation(0.25F, 1F, -2.5F, -0.296706F, -0.139626F, 0F));
		right_primaries.addOrReplaceChild("right_primaries_cube_4", CubeListBuilder.create()
				.texOffs(55, 26).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F), PartPose.offsetAndRotation(0.25F, 1.75F, -3.25F, -0.366519F, -0.139626F, 0F));
		PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.5F, -15.5F, 0.75F));
		left_leg.addOrReplaceChild("left_leg_cube_1", CubeListBuilder.create()
				.texOffs(70, 26).addBox(-1F, -1.25F, -1F, 2F, 4F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.20944F, 0F, 0F));
		left_leg.addOrReplaceChild("left_leg_cube_2", CubeListBuilder.create()
				.texOffs(79, 26).addBox(-0.5F, -8F, -0.5F, 1F, 8F, 1F), PartPose.offsetAndRotation(0F, 8.75F, 2F, 0.261799F, 0F, 0F));
		PartDefinition left_hock = left_leg.addOrReplaceChild("left_hock", CubeListBuilder.create()
				.texOffs(84, 26).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F), PartPose.offset(0F, 8.75F, 2F));
		left_hock.addOrReplaceChild("left_hock_cube_1", CubeListBuilder.create()
				.texOffs(89, 26).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F), PartPose.offsetAndRotation(0F, 6.25F, -2.25F, -0.349066F, 0F, 0F));
		PartDefinition left_foot = left_hock.addOrReplaceChild("left_foot", CubeListBuilder.create()
				.texOffs(94, 26).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F), PartPose.offset(0F, 6.25F, -2.25F));
		left_foot.addOrReplaceChild("left_foot_cube_1", CubeListBuilder.create()
				.texOffs(105, 26).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 3F, new CubeDeformation(-0.125F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.575959F, 0F));
		left_foot.addOrReplaceChild("left_foot_cube_2", CubeListBuilder.create()
				.texOffs(105, 26).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 3F, new CubeDeformation(-0.125F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.575959F, 0F));
		left_foot.addOrReplaceChild("left_foot_cube_3", CubeListBuilder.create()
				.texOffs(114, 26).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -3.141593F, 0F));
		PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.5F, -15.5F, -0.25F));
		right_leg.addOrReplaceChild("right_leg_cube_1", CubeListBuilder.create()
				.texOffs(70, 26).addBox(-1F, -1.25F, -1F, 2F, 4F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.20944F, 0F, 0F));
		right_leg.addOrReplaceChild("right_leg_cube_2", CubeListBuilder.create()
				.texOffs(79, 26).addBox(-0.5F, -8F, -0.5F, 1F, 8F, 1F), PartPose.offsetAndRotation(0F, 8.75F, 2F, 0.261799F, 0F, 0F));
		PartDefinition right_hock = right_leg.addOrReplaceChild("right_hock", CubeListBuilder.create()
				.texOffs(84, 26).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F), PartPose.offset(0F, 8.75F, 2F));
		right_hock.addOrReplaceChild("right_hock_cube_1", CubeListBuilder.create()
				.texOffs(89, 26).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F), PartPose.offsetAndRotation(0F, 6.25F, -2.25F, -0.349066F, 0F, 0F));
		PartDefinition right_foot = right_hock.addOrReplaceChild("right_foot", CubeListBuilder.create()
				.texOffs(94, 26).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F), PartPose.offset(0F, 6.25F, -2.25F));
		right_foot.addOrReplaceChild("right_foot_cube_1", CubeListBuilder.create()
				.texOffs(105, 26).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 3F, new CubeDeformation(-0.125F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.575959F, 0F));
		right_foot.addOrReplaceChild("right_foot_cube_2", CubeListBuilder.create()
				.texOffs(105, 26).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 3F, new CubeDeformation(-0.125F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.575959F, 0F));
		right_foot.addOrReplaceChild("right_foot_cube_3", CubeListBuilder.create()
				.texOffs(114, 26).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -3.141593F, 0F));
		return LayerDefinition.create(mesh, 128, 64);
	}
}
