package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.DessicantEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class DessicantModel extends EntityModel<DessicantEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("dessicant"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart leftLegs;
	private final ModelPart rightLegs;

	public DessicantModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.leftLegs = root.getChild("leftLegs");
		this.rightLegs = root.getChild("rightLegs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Scorpion-like body
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, 0.0F));

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 11).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(14, 11).addBox(-3.0F, -1.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(14, 15).addBox(1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, -4.0F));

		// Tail curving upward
		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 6).addBox(-0.5F, -4.0F, 3.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(26, 6).addBox(-0.5F, -6.0F, 2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, 4.0F));

		// Legs
		partdefinition.addOrReplaceChild("leftLegs", CubeListBuilder.create()
				.texOffs(0, 17).addBox(0.0F, 0.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 19).addBox(0.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(0.0F, 0.0F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(0.0F, 0.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 22.0F, 0.0F));

		partdefinition.addOrReplaceChild("rightLegs", CubeListBuilder.create()
				.texOffs(10, 17).addBox(-4.0F, 0.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 19).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 21).addBox(-4.0F, 0.0F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 23).addBox(-4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 22.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(DessicantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
		this.leftLegs.zRot = legSwing;
		this.rightLegs.zRot = -legSwing;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		tail.render(poseStack, buffer, packedLight, packedOverlay);
		leftLegs.render(poseStack, buffer, packedLight, packedOverlay);
		rightLegs.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
