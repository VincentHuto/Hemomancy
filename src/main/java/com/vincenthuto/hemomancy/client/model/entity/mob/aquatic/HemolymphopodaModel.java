package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class HemolymphopodaModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelhemolymphopoda"), "main");

	private final ModelPart Dome;
	private final ModelPart Abdomen;
	private final ModelPart Tail;
	private final ModelPart LeftLegs;
	private final ModelPart LLeg5;
	private final ModelPart LLeg4;
	private final ModelPart LLeg3;
	private final ModelPart LLeg2;
	private final ModelPart LLeg1;
	private final ModelPart Rlegs;
	private final ModelPart RLeg5;
	private final ModelPart RLeg4;
	private final ModelPart RLeg3;
	private final ModelPart RLeg2;
	private final ModelPart RLeg1;

	public HemolymphopodaModel(ModelPart root) {
		this.Dome = root.getChild("Dome");
		this.Abdomen = this.Dome.getChild("Abdomen");
		this.Tail = this.Abdomen.getChild("Tail");
		this.LeftLegs = root.getChild("LeftLegs");
		this.LLeg5 = this.LeftLegs.getChild("LLeg5");
		this.LLeg4 = this.LeftLegs.getChild("LLeg4");
		this.LLeg3 = this.LeftLegs.getChild("LLeg3");
		this.LLeg2 = this.LeftLegs.getChild("LLeg2");
		this.LLeg1 = this.LeftLegs.getChild("LLeg1");
		this.Rlegs = root.getChild("Rlegs");
		this.RLeg5 = this.Rlegs.getChild("RLeg5");
		this.RLeg4 = this.Rlegs.getChild("RLeg4");
		this.RLeg3 = this.Rlegs.getChild("RLeg3");
		this.RLeg2 = this.Rlegs.getChild("RLeg2");
		this.RLeg1 = this.Rlegs.getChild("RLeg1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Dome = partdefinition.addOrReplaceChild("Dome", CubeListBuilder.create().texOffs(0, 27).addBox(-6.0F, -3.0F, -4.5F, 12.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(44, 27).addBox(0.0F, -4.0F, -4.5F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(50, 13).addBox(-4.0F, -4.0F, -4.5F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 52).addBox(4.0F, -4.0F, -4.5F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(-7.0F, -2.0F, -5.5F, 14.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, 1.0F, -6.5F, 16.0F, 0.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, -5.5F));

		PartDefinition Abdomen = Dome.addOrReplaceChild("Abdomen", CubeListBuilder.create().texOffs(0, 44).addBox(-6.0F, -3.0F, 1.0F, 12.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 38).addBox(-7.0F, 0.0F, 1.0F, 14.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 52).addBox(4.0F, -4.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(30, 52).addBox(0.0F, -4.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(40, 54).addBox(-4.0F, -4.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 4.0F));

		PartDefinition Tail = Abdomen.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(40, 38).addBox(0.0F, -1.0F, 7.0F, 0.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -1.0F));

		PartDefinition LeftLegs = partdefinition.addOrReplaceChild("LeftLegs", CubeListBuilder.create(), PartPose.offset(2.5F, 23.0F, -8.0F));

		PartDefinition LLeg5 = LeftLegs.addOrReplaceChild("LLeg5", CubeListBuilder.create().texOffs(34, 47).addBox(0.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		PartDefinition LLeg4 = LeftLegs.addOrReplaceChild("LLeg4", CubeListBuilder.create().texOffs(34, 46).addBox(0.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition LLeg3 = LeftLegs.addOrReplaceChild("LLeg3", CubeListBuilder.create().texOffs(34, 45).addBox(0.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

		PartDefinition LLeg2 = LeftLegs.addOrReplaceChild("LLeg2", CubeListBuilder.create().texOffs(34, 44).addBox(0.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition LLeg1 = LeftLegs.addOrReplaceChild("LLeg1", CubeListBuilder.create().texOffs(34, 50).addBox(0.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Rlegs = partdefinition.addOrReplaceChild("Rlegs", CubeListBuilder.create(), PartPose.offset(-2.5F, 23.0F, -8.0F));

		PartDefinition RLeg5 = Rlegs.addOrReplaceChild("RLeg5", CubeListBuilder.create().texOffs(34, 51).addBox(-3.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		PartDefinition RLeg4 = Rlegs.addOrReplaceChild("RLeg4", CubeListBuilder.create().texOffs(50, 54).addBox(-3.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition RLeg3 = Rlegs.addOrReplaceChild("RLeg3", CubeListBuilder.create().texOffs(50, 55).addBox(-3.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

		PartDefinition RLeg2 = Rlegs.addOrReplaceChild("RLeg2", CubeListBuilder.create().texOffs(56, 24).addBox(-3.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition RLeg1 = Rlegs.addOrReplaceChild("RLeg1", CubeListBuilder.create().texOffs(56, 25).addBox(-3.5F, 0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
		Dome.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		LeftLegs.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		Rlegs.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		// Leg walking animation
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
	}
}
