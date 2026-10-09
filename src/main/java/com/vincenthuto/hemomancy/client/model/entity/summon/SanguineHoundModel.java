package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_sanguine_hound.py; regenerate the model, atlas and BBModel together.
public class SanguineHoundModel extends EntityModel<SanguineHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "sanguine_hound"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart hindLeftLeg;
	private final ModelPart hindRightLeg;
	private final ModelPart tail;
	private int color = -1;

	public SanguineHoundModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.frontLeftLeg = this.root.getChild("front_left_leg");
		this.frontRightLeg = this.root.getChild("front_right_leg");
		this.hindLeftLeg = this.root.getChild("hind_left_leg");
		this.hindRightLeg = this.root.getChild("hind_right_leg");
		this.tail = this.root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -5.0F, 4.0F, 5.0F, 5.0F).texOffs(19, 0).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 3.0F, 4.0F).texOffs(34, 0).addBox(-2.0F, -2.5F, 3.5F, 4.0F, 4.0F, 4.0F).texOffs(51, 0).addBox(-0.5F, -4.0F, -5.0F, 1.0F, 1.0F, 12.0F).texOffs(78, 0).addBox(-2.5F, -3.5F, -3.5F, 5.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 13.0F, 0.0F));
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(91, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -2.0F, -4.5F, 0.8726646259971648F, 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(100, 0).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 3.0F).texOffs(113, 0).addBox(-1.0F, -0.5F, -5.0F, 2.0F, 2.0F, 3.0F).texOffs(0, 14).addBox(-1.5F, -1.0F, -4.0F, 3.0F, 3.0F, 1.0F).texOffs(9, 14).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 9.0F, -7.5F));
        PartDefinition snout_tip = head.addOrReplaceChild("snout_tip", CubeListBuilder.create().texOffs(16, 14).addBox(-0.5F, -1.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.5F, -5.0F, 0.4886921905584123F, 0.0F, 0.0F));
        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(25, 14).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(30, 14).addBox(-0.5F, -6.0F, -1.0F, 1.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(-1.0F, -1.5F, 0.0F, -0.17453292519943295F, 0.0F, -0.13962634015954636F));
        PartDefinition front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(37, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 14.0F, -4.0F));
        PartDefinition front_right_shin = front_right_leg.addOrReplaceChild("front_right_shin", CubeListBuilder.create().texOffs(46, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F).texOffs(55, 14).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(60, 14).addBox(-0.5F, 5.0F, -1.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create().texOffs(67, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 14.0F, 5.0F));
        PartDefinition hind_right_shin = hind_right_leg.addOrReplaceChild("hind_right_shin", CubeListBuilder.create().texOffs(76, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F).texOffs(85, 14).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(90, 14).addBox(-0.5F, 5.0F, -1.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(97, 14).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(102, 14).addBox(-0.5F, -6.0F, -1.0F, 1.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(1.0F, -1.5F, 0.0F, -0.17453292519943295F, 0.0F, 0.13962634015954636F));
        PartDefinition front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(109, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 14.0F, -4.0F));
        PartDefinition front_left_shin = front_left_leg.addOrReplaceChild("front_left_shin", CubeListBuilder.create().texOffs(118, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F).texOffs(0, 22).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(5, 22).addBox(-0.5F, 5.0F, -1.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 14.0F, 5.0F));
        PartDefinition hind_left_shin = hind_left_leg.addOrReplaceChild("hind_left_shin", CubeListBuilder.create().texOffs(21, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F).texOffs(30, 22).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(35, 22).addBox(-0.5F, 5.0F, -1.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 22).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 11.0F, 7.5F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(57, 22).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 1.0F).texOffs(66, 22).addBox(-1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 2.0F).texOffs(73, 22).addBox(0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 6.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(SanguineHoundEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD;
		this.frontLeftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
		this.frontRightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.15F * limbSwingAmount;
		this.hindLeftLeg.xRot = this.frontRightLeg.xRot;
		this.hindRightLeg.xRot = this.frontLeftLeg.xRot;
		this.tail.yRot = Mth.sin(ageInTicks * 0.12F) * 0.24F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, color == -1 ? packedColor : color);
	}

	public void setColor(int color) {
		this.color = color;
	}
}
