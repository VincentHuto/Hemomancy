package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AbyssalSiphonEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class AbyssalSiphonModel extends EntityModel<AbyssalSiphonEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("abyssal_siphon"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftLegs;
	private final ModelPart rightLegs;
	private final ModelPart proboscis;

	public AbyssalSiphonModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.leftLegs = root.getChild("leftLegs");
		this.rightLegs = root.getChild("rightLegs");
		this.proboscis = root.getChild("proboscis");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Spidery, low-profile body
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -2.0F, -4.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, 0.0F));

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, -4.0F));

		// Blood-draining proboscis
		partdefinition.addOrReplaceChild("proboscis", CubeListBuilder.create()
				.texOffs(20, 14).addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, -8.0F));

		// Spider-like legs
		partdefinition.addOrReplaceChild("leftLegs", CubeListBuilder.create()
				.texOffs(0, 22).addBox(0.0F, 0.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(0.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 26).addBox(0.0F, 0.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(5.0F, 21.0F, 0.0F));

		partdefinition.addOrReplaceChild("rightLegs", CubeListBuilder.create()
				.texOffs(14, 22).addBox(-6.0F, 0.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(14, 24).addBox(-6.0F, 0.0F, -1.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(14, 26).addBox(-6.0F, 0.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 21.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(AbyssalSiphonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.3F * limbSwingAmount;
		this.leftLegs.zRot = legSwing;
		this.rightLegs.zRot = -legSwing;
		this.proboscis.xRot = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		leftLegs.render(poseStack, buffer, packedLight, packedOverlay);
		rightLegs.render(poseStack, buffer, packedLight, packedOverlay);
		proboscis.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
