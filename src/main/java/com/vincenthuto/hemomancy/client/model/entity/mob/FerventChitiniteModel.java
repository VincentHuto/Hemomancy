package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.FerventChitiniteEntity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class FerventChitiniteModel extends HierarchicalModel<FerventChitiniteEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("ferventchitinitemodel"), "main");

	public static final ModelLayerLocation CRYSTAL_LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("ferventchitinitemodel"), "crystal");

	private final ModelPart whole;

	public FerventChitiniteModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	public static LayerDefinition createCrystalLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition plate4 = whole.addOrReplaceChild("plate4", CubeListBuilder.create().texOffs(21, 12).addBox(-2.5F, 0.849F, -0.3726F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 5).addBox(-4.0F, -0.3232F, -1.0217F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 27).addBox(-2.0F, 0.7951F, -0.1097F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 30).addBox(3.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 30).addBox(-4.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.7637F, 2.3841F, -0.2182F, 0.0F, 0.0F));

		PartDefinition crystalBack = plate4.addOrReplaceChild("crystalBack", CubeListBuilder.create().texOffs(30, 47).addBox(-3.5F, -3.0683F, -1.112F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 48).addBox(-2.5F, -3.9683F, -0.612F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.6451F, 1.0903F));

		PartDefinition plate3 = whole.addOrReplaceChild("plate3", CubeListBuilder.create().texOffs(0, 15).addBox(-3.0F, -1.6226F, -1.9775F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 35).addBox(3.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(30, 31).addBox(-4.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -2.2445F, -2.0571F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 27).addBox(-4.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(3.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5774F, -0.0225F, -0.0436F, 0.0F, 0.0F));

		PartDefinition crystalMiddle = plate3.addOrReplaceChild("crystalMiddle", CubeListBuilder.create().texOffs(9, 44).addBox(-3.0F, -4.3F, -1.5F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(10, 52).addBox(-2.0F, -5.0F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition plate2 = whole.addOrReplaceChild("plate2", CubeListBuilder.create().texOffs(0, 10).addBox(-4.0F, -0.5197F, -3.1395F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 18).addBox(-3.0F, 0.2803F, -3.1395F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(3.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(6, 29).addBox(-4.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.7759F, -2.2109F, 0.1745F, 0.0F, 0.0F));

		PartDefinition crystalFront = plate2.addOrReplaceChild("crystalFront", CubeListBuilder.create().texOffs(29, 54).addBox(-3.0F, -2.1F, 1.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 55).addBox(-2.0F, -3.0F, 1.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.4803F, -3.1395F));

		PartDefinition plate5 = whole.addOrReplaceChild("plate5", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, -0.1149F, -0.0043F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(20, 1).addBox(-3.0F, -0.3367F, -0.8838F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(10, 24).addBox(-1.0F, 0.256F, -0.0315F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 22).addBox(2.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 17).addBox(-3.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.1786F, 4.2788F, -0.3054F, 0.0F, 0.0F));

		PartDefinition lLeg4 = whole.addOrReplaceChild("lLeg4", CubeListBuilder.create().texOffs(16, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 6).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.125F, -2.184F, 3.115F, 0.2182F, 0.0F, 0.0F));

		PartDefinition lLeg5 = whole.addOrReplaceChild("lLeg5", CubeListBuilder.create().texOffs(2, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.625F, -2.5568F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg = whole.addOrReplaceChild("rLeg", CubeListBuilder.create().texOffs(0, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 16).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.875F, -2.2329F, 3.1042F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg4 = whole.addOrReplaceChild("rLeg4", CubeListBuilder.create().texOffs(2, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 5).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.375F, -2.5568F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg3 = whole.addOrReplaceChild("rLeg3", CubeListBuilder.create().texOffs(0, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.625F, -2.2F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg3 = whole.addOrReplaceChild("lLeg3", CubeListBuilder.create().texOffs(0, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 11).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.875F, -2.2F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg = whole.addOrReplaceChild("lLeg", CubeListBuilder.create().texOffs(18, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.625F, -2.4957F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg2 = whole.addOrReplaceChild("rLeg2", CubeListBuilder.create().texOffs(2, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.875F, -2.151F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg5 = whole.addOrReplaceChild("rLeg5", CubeListBuilder.create().texOffs(0, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.375F, -2.4957F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition lLeg2 = whole.addOrReplaceChild("lLeg2", CubeListBuilder.create().texOffs(2, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.125F, -2.151F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		PartDefinition plate = whole.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(20, 6).addBox(-2.5F, -0.7866F, -2.7514F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(6, 34).addBox(-2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 33).addBox(2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.549F, -4.7451F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head = plate.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 24).addBox(-2.5F, -0.2667F, -1.8333F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.2301F, -0.668F));

		PartDefinition leftEye = head.addOrReplaceChild("leftEye", CubeListBuilder.create().texOffs(10, 26).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 0.0333F, -1.0833F));

		PartDefinition rightEye = head.addOrReplaceChild("rightEye", CubeListBuilder.create().texOffs(11, 22).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.75F, 0.0333F, -1.0833F));

		PartDefinition shell = whole.addOrReplaceChild("shell", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition plate4 = whole.addOrReplaceChild("plate4",
				CubeListBuilder.create().texOffs(21, 12)
						.addBox(-2.5F, 0.849F, -0.3726F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 5)
						.addBox(-4.0F, -0.3232F, -1.0217F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(24, 27)
						.addBox(-2.0F, 0.7951F, -0.1097F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 30)
						.addBox(3.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 30)
						.addBox(-4.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -3.7637F, 2.3841F, -0.2182F, 0.0F, 0.0F));


		PartDefinition plate3 = whole.addOrReplaceChild("plate3",
				CubeListBuilder.create().texOffs(0, 15)
						.addBox(-3.0F, -1.6226F, -1.9775F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 35)
						.addBox(3.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(30, 31)
						.addBox(-4.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.0F, -2.2445F, -2.0571F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(18, 27)
						.addBox(-4.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 26)
						.addBox(3.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -2.5774F, -0.0225F, -0.0436F, 0.0F, 0.0F));

		PartDefinition plate2 = whole.addOrReplaceChild("plate2",
				CubeListBuilder.create().texOffs(0, 10)
						.addBox(-4.0F, -0.5197F, -3.1395F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(16, 18)
						.addBox(-3.0F, 0.2803F, -3.1395F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 32)
						.addBox(3.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(6, 29)
						.addBox(-4.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -3.7759F, -2.2109F, 0.1745F, 0.0F, 0.0F));



		PartDefinition plate5 = whole.addOrReplaceChild("plate5",
				CubeListBuilder.create().texOffs(0, 22)
						.addBox(-2.0F, -0.1149F, -0.0043F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(20, 1)
						.addBox(-3.0F, -0.3367F, -0.8838F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(10, 24)
						.addBox(-1.0F, 0.256F, -0.0315F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 22)
						.addBox(2.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 17)
						.addBox(-3.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -3.1786F, 4.2788F, -0.3054F, 0.0F, 0.0F));

		PartDefinition lLeg4 = whole.addOrReplaceChild("lLeg4",
				CubeListBuilder.create().texOffs(16, 14)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 6)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.125F, -2.184F, 3.115F, 0.2182F, 0.0F, 0.0F));

		PartDefinition lLeg5 = whole.addOrReplaceChild("lLeg5",
				CubeListBuilder.create().texOffs(2, 12)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 0)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.625F, -2.5568F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg = whole.addOrReplaceChild("rLeg",
				CubeListBuilder.create().texOffs(0, 12)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 16)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.875F, -2.2329F, 3.1042F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg4 = whole.addOrReplaceChild("rLeg4",
				CubeListBuilder.create().texOffs(2, 2)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 5)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.375F, -2.5568F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg3 = whole.addOrReplaceChild("rLeg3",
				CubeListBuilder.create().texOffs(0, 7)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 10)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.625F, -2.2F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg3 = whole.addOrReplaceChild("lLeg3",
				CubeListBuilder.create().texOffs(0, 17)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 11)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.875F, -2.2F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg = whole.addOrReplaceChild("lLeg",
				CubeListBuilder.create().texOffs(18, 14)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(6, 26)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.625F, -2.4957F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg2 = whole.addOrReplaceChild("rLeg2",
				CubeListBuilder.create().texOffs(2, 7)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 15)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.875F, -2.151F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg5 = whole.addOrReplaceChild("rLeg5",
				CubeListBuilder.create().texOffs(0, 2)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.375F, -2.4957F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition lLeg2 = whole.addOrReplaceChild("lLeg2",
				CubeListBuilder.create().texOffs(2, 17)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 26)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.125F, -2.151F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		PartDefinition plate = whole.addOrReplaceChild("plate",
				CubeListBuilder.create().texOffs(20, 6)
						.addBox(-2.5F, -0.7866F, -2.7514F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(6, 34)
						.addBox(-2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(18, 33)
						.addBox(2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, -2.549F, -4.7451F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head = plate.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 24).addBox(-2.5F,
				-0.2667F, -1.8333F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 0.2301F, -0.668F));

		PartDefinition leftEye = head.addOrReplaceChild("leftEye", CubeListBuilder.create().texOffs(10, 26)
				.addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.75F, 0.0333F, -1.0833F));

		PartDefinition rightEye = head.addOrReplaceChild("rightEye", CubeListBuilder.create().texOffs(11, 22)
				.addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.75F, 0.0333F, -1.0833F));

		PartDefinition shell = whole.addOrReplaceChild("shell", CubeListBuilder.create(),
				PartPose.offset(0.0F, -3.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(FerventChitiniteEntity p_102618_, float p_102619_, float p_102620_, float p_102621_,
			float p_102622_, float p_102623_) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		// TODO Auto-generated method stub
		return this.whole;
	}

}