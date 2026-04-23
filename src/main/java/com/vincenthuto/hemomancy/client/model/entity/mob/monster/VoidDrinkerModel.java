package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.VoidDrinkerEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class VoidDrinkerModel extends EntityModel<VoidDrinkerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("void_drinker"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tentacle1;
	private final ModelPart tentacle2;
	private final ModelPart tentacle3;
	private final ModelPart tentacle4;

	public VoidDrinkerModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tentacle1 = root.getChild("tentacle1");
		this.tentacle2 = root.getChild("tentacle2");
		this.tentacle3 = root.getChild("tentacle3");
		this.tentacle4 = root.getChild("tentacle4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Floating jellyfish/squid-like body
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 12.0F, 0.0F));

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 8.0F, 0.0F));

		// Hanging tentacles
		partdefinition.addOrReplaceChild("tentacle1", CubeListBuilder.create()
				.texOffs(24, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 16.0F, -2.0F));

		partdefinition.addOrReplaceChild("tentacle2", CubeListBuilder.create()
				.texOffs(28, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 16.0F, -2.0F));

		partdefinition.addOrReplaceChild("tentacle3", CubeListBuilder.create()
				.texOffs(32, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 16.0F, 2.0F));

		partdefinition.addOrReplaceChild("tentacle4", CubeListBuilder.create()
				.texOffs(36, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 16.0F, 2.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(VoidDrinkerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float sway = (float) Math.sin(ageInTicks * 0.1F) * 0.15F;
		this.tentacle1.xRot = sway;
		this.tentacle2.xRot = -sway;
		this.tentacle3.zRot = sway;
		this.tentacle4.zRot = -sway;
		this.body.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle1.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle2.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle3.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle4.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
