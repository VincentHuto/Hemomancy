package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CrimsonDoeModel extends EntityModel<CrimsonDoeEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("crimson_doe"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart antlerLeft;
	private final ModelPart antlerRight;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;
	private final ModelPart tail;

	public CrimsonDoeModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.antlerLeft = root.getChild("antlerLeft");
		this.antlerRight = root.getChild("antlerRight");
		this.frontLeftLeg = root.getChild("frontLeftLeg");
		this.frontRightLeg = root.getChild("frontRightLeg");
		this.backLeftLeg = root.getChild("backLeftLeg");
		this.backRightLeg = root.getChild("backRightLeg");
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Graceful deer body - slender and elegant
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 11.0F, 0.0F));

		// Elegant head with long neck
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(18, 18).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 27).addBox(-1.5F, -1.0F, 1.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 6.0F, -6.0F));

		// Crystalline blood-red antlers - branching elegantly
		partdefinition.addOrReplaceChild("antlerLeft", CubeListBuilder.create()
				.texOffs(36, 0).addBox(0.0F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(0.0F, -8.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 0).addBox(0.0F, -5.0F, -2.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 3.0F, -6.0F, -0.1745F, 0.0F, -0.2618F));

		partdefinition.addOrReplaceChild("antlerRight", CubeListBuilder.create()
				.texOffs(36, 7).addBox(-1.0F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(40, 7).addBox(-1.0F, -8.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 7).addBox(-1.0F, -5.0F, -2.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, 3.0F, -6.0F, -0.1745F, 0.0F, 0.2618F));

		// Slender legs
		partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create()
				.texOffs(24, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 14.0F, -4.0F));

		partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 14.0F, -4.0F));

		partdefinition.addOrReplaceChild("backLeftLeg", CubeListBuilder.create()
				.texOffs(24, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 14.0F, 4.0F));

		partdefinition.addOrReplaceChild("backRightLeg", CubeListBuilder.create()
				.texOffs(30, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 14.0F, 4.0F));

		// Small fluffy tail
		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(36, 14).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 9.0F, 6.0F, -0.5236F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(CrimsonDoeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.antlerLeft.yRot = this.head.yRot;
		this.antlerLeft.xRot = this.head.xRot - 0.1745F;
		this.antlerRight.yRot = this.head.yRot;
		this.antlerRight.xRot = this.head.xRot - 0.1745F;
		this.frontLeftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.frontRightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.backLeftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.backRightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.tail.yRot = (float) Math.sin(ageInTicks * 0.1F) * 0.15F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		antlerLeft.render(poseStack, buffer, packedLight, packedOverlay);
		antlerRight.render(poseStack, buffer, packedLight, packedOverlay);
		frontLeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		frontRightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		backLeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		backRightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		tail.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
