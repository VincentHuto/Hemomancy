package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.SynapseHoundEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SynapseHoundModel extends EntityModel<SynapseHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("synapse_hound"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;
	private final ModelPart tail;

	public SynapseHoundModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.frontLeftLeg = root.getChild("frontLeftLeg");
		this.frontRightLeg = root.getChild("frontRightLeg");
		this.backLeftLeg = root.getChild("backLeftLeg");
		this.backRightLeg = root.getChild("backRightLeg");
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Wolf/hyena-like body
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-2.5F, -3.0F, -5.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(20, 16).addBox(-1.5F, -1.0F, -8.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 26).addBox(-2.5F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(6, 26).addBox(0.5F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, -5.0F));

		partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create()
				.texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 17.0F, -3.0F));

		partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create()
				.texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 17.0F, -3.0F));

		partdefinition.addOrReplaceChild("backLeftLeg", CubeListBuilder.create()
				.texOffs(32, 9).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 17.0F, 4.0F));

		partdefinition.addOrReplaceChild("backRightLeg", CubeListBuilder.create()
				.texOffs(40, 9).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 17.0F, 4.0F));

		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(32, 18).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 12.0F, 5.0F, -0.5236F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(SynapseHoundEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.frontLeftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.frontRightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.backLeftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.backRightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.tail.yRot = (float) Math.sin(ageInTicks * 0.1F) * 0.3F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		frontLeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		frontRightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		backLeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		backRightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		tail.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
