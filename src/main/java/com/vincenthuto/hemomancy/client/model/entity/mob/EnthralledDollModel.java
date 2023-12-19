package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class EnthralledDollModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelenthralleddoll"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(12, 10)
						.addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 6)
						.addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		PartDefinition headTentacles = head.addOrReplaceChild("headTentacles",
				CubeListBuilder.create().texOffs(24, 18)
						.addBox(1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.2F)).texOffs(24, 7)
						.addBox(-3.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition headTentacles2 = headTentacles.addOrReplaceChild("headTentacles2",
				CubeListBuilder.create().texOffs(24, 2)
						.addBox(1.0F, 0.6933F, -2.9706F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)).texOffs(0, 23)
						.addBox(-3.0F, 0.6933F, -2.9706F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition headTentacles3 = headTentacles.addOrReplaceChild("headTentacles3",
				CubeListBuilder.create().texOffs(17, 5)
						.addBox(0.95F, 0.9802F, -4.5947F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)).texOffs(15, 0)
						.addBox(-2.95F, 0.9802F, -4.5947F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(0.0F, 4.0F, 6.0F, -1.2217F, 0.0F, 0.0F));

		PartDefinition jingles = headTentacles.addOrReplaceChild("jingles",
				CubeListBuilder.create().texOffs(0, 28)
						.addBox(0.95F, 0.9802F, -3.5947F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F)).texOffs(14, 27)
						.addBox(-2.95F, 0.9802F, -3.5947F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F)),
				PartPose.offsetAndRotation(0.0F, 5.0F, 6.25F, -1.2217F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(12, 18)
						.addBox(-2.0F, 0.0F, -1.0F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-2.5F, 0.0F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 14)
						.addBox(-2.0F, 0.0F, -1.0F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		PartDefinition leftArm = partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(24, 23)
				.addBox(0.0F, -1.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 16.5F, 0.0F));

		PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(8, 26)
				.addBox(-1.0F, -1.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 16.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leftArm;

	private final ModelPart rightArm;

	public EnthralledDollModel(ModelPart root) {
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.leftArm = root.getChild("leftArm");
		this.rightArm = root.getChild("rightArm");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}
}