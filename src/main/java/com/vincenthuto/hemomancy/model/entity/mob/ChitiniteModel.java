package com.vincenthuto.hemomancy.model.entity.mob;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ChitiniteModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelchitinite"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition shell = partdefinition.addOrReplaceChild("shell", CubeListBuilder.create(),
				PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition plate4 = partdefinition.addOrReplaceChild("plate4",
				CubeListBuilder.create().texOffs(21, 12)
						.addBox(-2.5F, 0.849F, -0.3726F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 5)
						.addBox(-4.0F, -0.3232F, -1.0217F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(24, 27)
						.addBox(-2.0F, 0.7951F, -0.1097F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 30)
						.addBox(3.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 30)
						.addBox(-4.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 20.2363F, 2.3841F, -0.2182F, 0.0F, 0.0F));

		PartDefinition plate3 = partdefinition.addOrReplaceChild("plate3",
				CubeListBuilder.create().texOffs(0, 15)
						.addBox(-3.0F, -1.6226F, -1.9775F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 35)
						.addBox(3.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(30, 31)
						.addBox(-4.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.0F, -2.2445F, -2.0571F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(18, 27)
						.addBox(-4.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 26)
						.addBox(3.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 21.4226F, -0.0225F, -0.0436F, 0.0F, 0.0F));

		PartDefinition plate2 = partdefinition.addOrReplaceChild("plate2",
				CubeListBuilder.create().texOffs(0, 10)
						.addBox(-4.0F, -0.5197F, -3.1395F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(16, 18)
						.addBox(-3.0F, 0.2803F, -3.1395F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 32)
						.addBox(3.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(6, 29)
						.addBox(-4.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 20.2241F, -2.2109F, 0.1745F, 0.0F, 0.0F));

		PartDefinition plate5 = partdefinition.addOrReplaceChild("plate5",
				CubeListBuilder.create().texOffs(0, 22)
						.addBox(-2.0F, -0.1149F, -0.0043F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(20, 1)
						.addBox(-3.0F, -0.3367F, -0.8838F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(10, 24)
						.addBox(-1.0F, 0.256F, -0.0315F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 22)
						.addBox(2.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 17)
						.addBox(-3.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 20.8214F, 4.2788F, -0.3054F, 0.0F, 0.0F));

		PartDefinition lLeg4 = partdefinition.addOrReplaceChild("lLeg4",
				CubeListBuilder.create().texOffs(16, 14)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 6)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.125F, 21.816F, 3.115F, 0.2182F, 0.0F, 0.0F));

		PartDefinition lLeg5 = partdefinition.addOrReplaceChild("lLeg5",
				CubeListBuilder.create().texOffs(2, 12)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 0)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.625F, 21.4432F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg = partdefinition.addOrReplaceChild("rLeg",
				CubeListBuilder.create().texOffs(0, 12)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 16)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.875F, 21.7671F, 3.1042F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg4 = partdefinition.addOrReplaceChild("rLeg4",
				CubeListBuilder.create().texOffs(2, 2)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 5)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.375F, 21.4432F, 4.8661F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg3 = partdefinition.addOrReplaceChild("rLeg3",
				CubeListBuilder.create().texOffs(0, 7)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 10)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.625F, 21.8F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg3 = partdefinition.addOrReplaceChild("lLeg3",
				CubeListBuilder.create().texOffs(0, 17)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 11)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.875F, 21.8F, 0.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition plate = partdefinition.addOrReplaceChild("plate",
				CubeListBuilder.create().texOffs(20, 6)
						.addBox(-2.5F, -0.7866F, -2.7514F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(6, 34)
						.addBox(-2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(18, 33)
						.addBox(2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 21.451F, -4.7451F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head = plate.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 24).addBox(-2.5F,
				-0.2667F, -1.8333F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 0.2301F, -0.668F));

		PartDefinition leftEye = head.addOrReplaceChild("leftEye", CubeListBuilder.create().texOffs(10, 26)
				.addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.75F, 0.0333F, -1.0833F));

		PartDefinition rightEye = head.addOrReplaceChild("rightEye", CubeListBuilder.create().texOffs(11, 22)
				.addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.75F, 0.0333F, -1.0833F));

		PartDefinition lLeg = partdefinition.addOrReplaceChild("lLeg",
				CubeListBuilder.create().texOffs(18, 14)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(6, 26)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.625F, 21.5043F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg2 = partdefinition.addOrReplaceChild("rLeg2",
				CubeListBuilder.create().texOffs(2, 7)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 15)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.875F, 21.849F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg5 = partdefinition.addOrReplaceChild("rLeg5",
				CubeListBuilder.create().texOffs(0, 2)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.375F, 21.5043F, -3.8503F, -0.1745F, 0.0F, 0.0F));

		PartDefinition lLeg2 = partdefinition.addOrReplaceChild("lLeg2",
				CubeListBuilder.create().texOffs(2, 17)
						.addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 26)
						.addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.125F, 21.849F, -1.9111F, -0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart shell;
	private final ModelPart plate4;
	private final ModelPart plate3;
	private final ModelPart plate2;
	private final ModelPart plate5;
	private final ModelPart lLeg4;
	private final ModelPart lLeg5;
	private final ModelPart rLeg;
	private final ModelPart rLeg4;
	private final ModelPart rLeg3;
	private final ModelPart lLeg3;
	private final ModelPart plate;
	private final ModelPart lLeg;
	private final ModelPart rLeg2;
	private final ModelPart rLeg5;

	private final ModelPart lLeg2;

	public ChitiniteModel(ModelPart root) {
		this.shell = root.getChild("shell");
		this.plate4 = root.getChild("plate4");
		this.plate3 = root.getChild("plate3");
		this.plate2 = root.getChild("plate2");
		this.plate5 = root.getChild("plate5");
		this.lLeg4 = root.getChild("lLeg4");
		this.lLeg5 = root.getChild("lLeg5");
		this.rLeg = root.getChild("rLeg");
		this.rLeg4 = root.getChild("rLeg4");
		this.rLeg3 = root.getChild("rLeg3");
		this.lLeg3 = root.getChild("lLeg3");
		this.plate = root.getChild("plate");
		this.lLeg = root.getChild("lLeg");
		this.rLeg2 = root.getChild("rLeg2");
		this.rLeg5 = root.getChild("rLeg5");
		this.lLeg2 = root.getChild("lLeg2");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		shell.render(poseStack, buffer, packedLight, packedOverlay);
		plate4.render(poseStack, buffer, packedLight, packedOverlay);
		plate3.render(poseStack, buffer, packedLight, packedOverlay);
		plate2.render(poseStack, buffer, packedLight, packedOverlay);
		plate5.render(poseStack, buffer, packedLight, packedOverlay);
		lLeg4.render(poseStack, buffer, packedLight, packedOverlay);
		lLeg5.render(poseStack, buffer, packedLight, packedOverlay);
		rLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rLeg4.render(poseStack, buffer, packedLight, packedOverlay);
		rLeg3.render(poseStack, buffer, packedLight, packedOverlay);
		lLeg3.render(poseStack, buffer, packedLight, packedOverlay);
		plate.render(poseStack, buffer, packedLight, packedOverlay);
		lLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rLeg2.render(poseStack, buffer, packedLight, packedOverlay);
		rLeg5.render(poseStack, buffer, packedLight, packedOverlay);
		lLeg2.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

		this.lLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.lLeg2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.lLeg3.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.lLeg4.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.lLeg5.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

		this.rLeg5.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rLeg2.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.rLeg3.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.rLeg4.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

	}
}