package com.vincenthuto.hemomancy.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class ModelAbhorentThought<T extends Entity> extends EntityModel<T> {
	private final ModelPart whole;
	private final ModelPart hips;

	public ModelAbhorentThought(ModelPart root) {
		this.whole = root.getChild("whole");
		this.hips = root.getChild("hips");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createLayers() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 16.0F, 8.0F));

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition abdomen = body.addOrReplaceChild("abdomen",
				CubeListBuilder.create().texOffs(59, 106)
						.addBox(-3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-2.0F, -5.0F, -3.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-2.0F, -5.0F, 2.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-2.0F, -9.0F, 2.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-4.0F, -10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-4.0F, -10.0F, -4.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -20.0F, -1.0F));

		PartDefinition torso = abdomen.addOrReplaceChild("torso",
				CubeListBuilder.create().texOffs(44, 104)
						.addBox(-6.0F, -2.0F, -5.0F, 13.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(44, 104)
						.addBox(-3.0F, -1.0F, -4.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-7.0F, -6.0F, -6.0F, 15.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-6.0F, -6.0F, 2.0F, 13.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(1, 111)
						.addBox(-9.0F, -11.0F, 3.0F, 19.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(68, 106)
						.addBox(-4.0F, -11.0F, 4.0F, 9.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(1, 111)
						.addBox(-8.0F, -12.0F, -8.0F, 17.0F, 6.0F, 11.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -10.0F, 1.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition neck = torso.addOrReplaceChild("neck",
				CubeListBuilder.create().texOffs(21, 112)
						.addBox(6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-4.0F, -5.0F, 3.0F, 9.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-2.0F, -5.0F, 4.0F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(21, 112)
						.addBox(-5.0F, -6.0F, -5.0F, 11.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -12.0F, -2.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(),
				PartPose.offset(0.0F, -4.0F, -1.0F));

		PartDefinition brain = head.addOrReplaceChild("brain", CubeListBuilder.create(),
				PartPose.offset(0.0F, 15.3781F, -2.4547F));

		PartDefinition arm = brain.addOrReplaceChild("arm",
				CubeListBuilder.create().texOffs(44, 20).addBox(-1.3061F, -1.777F, -1.0551F, 3.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.5F, -24.0F, -2.5F, 0.0F, 0.0F, -0.6109F));

		PartDefinition arm2 = arm.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(0, 7)
				.addBox(7.724F, -10.7451F, -2.0551F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 55)
				.addBox(7.224F, -15.7451F, -2.5551F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(60, 41)
				.addBox(6.724F, -15.4951F, -1.5551F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-8.0301F, 9.9681F, 2.0F));

		PartDefinition arm3 = arm2.addOrReplaceChild("arm3", CubeListBuilder.create().texOffs(90, 0)
				.addBox(-0.819F, -4.1542F, -0.8774F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(30, 55)
				.addBox(-0.319F, -0.1542F, -0.3774F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(50, 94)
				.addBox(-1.319F, -3.9042F, 0.1226F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(8.043F, -15.5909F, -1.6777F));

		PartDefinition arm4 = arm3.addOrReplaceChild("arm4", CubeListBuilder.create().texOffs(65, 88)
				.addBox(-0.7587F, -3.789F, -0.8366F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(55, 25)
				.addBox(-0.2587F, 0.211F, -0.3366F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.0603F, -4.3653F, -0.0409F));

		PartDefinition arm5 = arm4.addOrReplaceChild("arm5",
				CubeListBuilder.create().texOffs(93, 71)
						.addBox(-2.0F, -4.8F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(24, 97)
						.addBox(-1.0F, -5.8F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(16, 93)
						.addBox(-1.5F, -4.05F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(56, 21)
						.addBox(-0.5F, -0.05F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.2413F, -3.739F, 0.1634F));

		PartDefinition arm6 = brain.addOrReplaceChild("arm6",
				CubeListBuilder.create().texOffs(35, 23).addBox(-1.6817F, -2.0044F, -1.3425F, 3.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, -24.0F, 5.5F, -0.6981F, 0.0F, 0.5672F));

		PartDefinition arm7 = arm6.addOrReplaceChild("arm7", CubeListBuilder.create().texOffs(0, 6)
				.addBox(-0.2039F, 0.7361F, -0.9643F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(54, 0)
				.addBox(-0.7039F, -4.2639F, -1.4643F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(60, 36)
				.addBox(-1.2039F, -4.0139F, -0.4643F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.4778F, -1.7405F, 0.6218F));

		PartDefinition arm8 = arm7.addOrReplaceChild("arm8", CubeListBuilder.create().texOffs(57, 88)
				.addBox(-1.1789F, -4.2889F, -1.0393F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(54, 16)
				.addBox(-0.6789F, -0.2889F, -0.5393F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(44, 94)
				.addBox(-1.6789F, -4.0389F, -0.0393F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.475F, -3.975F, -0.425F));

		PartDefinition arm9 = arm8.addOrReplaceChild("arm9", CubeListBuilder.create().texOffs(49, 88)
				.addBox(-1.2039F, -4.2639F, -0.9643F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(53, 20)
				.addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(73, 92)
				.addBox(-1.7039F, -4.0139F, 0.0357F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.025F, -4.025F, -0.075F));

		PartDefinition arm10 = arm9.addOrReplaceChild("arm10", CubeListBuilder.create().texOffs(8, 93)
				.addBox(-2.2039F, -5.0139F, 0.0357F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(24, 97)
				.addBox(-1.2039F, -6.0139F, 0.0357F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 93)
				.addBox(-1.7039F, -4.2639F, -0.4643F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(51, 26)
				.addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arm21 = brain.addOrReplaceChild("arm21",
				CubeListBuilder.create().texOffs(0, 6).addBox(-1.3309F, -2.0216F, -1.3925F, 3.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, -25.0F, -2.5F, -0.7854F, 0.0F, -0.5236F));

		PartDefinition arm22 = arm21.addOrReplaceChild("arm22", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-0.8327F, 0.6119F, -0.3336F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 20)
				.addBox(-1.3327F, -4.3881F, -0.8336F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(12, 12)
				.addBox(-1.8327F, -4.1381F, 0.1664F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5018F, -1.6335F, -0.0588F));

		PartDefinition arm23 = arm22.addOrReplaceChild("arm23", CubeListBuilder.create().texOffs(48, 62)
				.addBox(-0.8327F, -4.1381F, -0.9586F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(30, 26)
				.addBox(-0.3327F, -0.1381F, -0.4586F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(66, 54)
				.addBox(-1.3327F, -3.8881F, 0.0414F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, -4.25F, 0.125F));

		PartDefinition arm24 = arm23.addOrReplaceChild("arm24", CubeListBuilder.create().texOffs(24, 55)
				.addBox(-0.8309F, -3.9306F, -0.8799F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(26, 26)
				.addBox(-0.3309F, 0.0694F, -0.3799F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.0018F, -4.2075F, -0.0788F));

		PartDefinition arm25 = arm24.addOrReplaceChild("arm25", CubeListBuilder.create().texOffs(90, 66)
				.addBox(-1.8309F, -5.1806F, 0.1201F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(24, 97)
				.addBox(-0.8309F, -6.1806F, 0.1201F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(90, 45)
				.addBox(-1.3309F, -4.4306F, -0.3799F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(12, 10)
				.addBox(-0.3309F, -0.4306F, -0.3799F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -3.5F, 0.0F));

		PartDefinition arm11 = brain.addOrReplaceChild("arm11",
				CubeListBuilder.create().texOffs(26, 20).addBox(-1.8056F, -0.8487F, -1.8441F, 3.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.5F, -24.0F, -9.5F, 1.1345F, 0.0F, -0.48F));

		PartDefinition arm12 = arm11.addOrReplaceChild("arm12", CubeListBuilder.create().texOffs(0, 2)
				.addBox(-0.8794F, 0.2157F, -0.6307F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(34, 46)
				.addBox(-1.3794F, -4.7843F, -1.1307F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(12, 5)
				.addBox(-1.8794F, -4.5343F, -0.1307F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0738F, -0.0644F, -0.2133F));

		PartDefinition arm13 = arm12.addOrReplaceChild("arm13", CubeListBuilder.create().texOffs(20, 87)
				.addBox(-0.8544F, -4.0593F, -0.9557F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(47, 26)
				.addBox(-0.3544F, -0.0593F, -0.4557F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(73, 88)
				.addBox(-1.3544F, -3.8093F, 0.0443F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.525F, -4.725F, -0.175F));

		PartDefinition arm14 = arm13.addOrReplaceChild("arm14", CubeListBuilder.create().texOffs(12, 87)
				.addBox(-1.1294F, -3.7843F, -0.8807F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(4, 44)
				.addBox(-0.6294F, 0.2157F, -0.3807F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.275F, -4.275F, -0.075F));

		PartDefinition arm15 = arm14.addOrReplaceChild("arm15", CubeListBuilder.create().texOffs(36, 92)
				.addBox(-1.8794F, -5.0343F, -0.1307F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(24, 97)
				.addBox(-0.8794F, -6.0343F, -0.1307F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(91, 50)
				.addBox(-1.3794F, -4.2843F, -0.6307F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 44)
				.addBox(-0.3794F, -0.2843F, -0.6307F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.25F, -3.5F, 0.25F));

		PartDefinition arm16 = brain.addOrReplaceChild("arm16",
				CubeListBuilder.create().texOffs(0, 12).addBox(-1.6817F, -1.6382F, -0.9899F, 3.0F, 3.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -23.0F, -2.5F, 0.3054F, 0.0F, 0.5672F));

		PartDefinition arm17 = arm16.addOrReplaceChild("arm17", CubeListBuilder.create().texOffs(0, 1)
				.addBox(-0.7039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 37)
				.addBox(-1.2039F, -5.0006F, -0.8468F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(12, 0)
				.addBox(-1.7039F, -4.7506F, 0.1532F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0222F, -0.6376F, 0.3569F));

		PartDefinition arm18 = arm17.addOrReplaceChild("arm18", CubeListBuilder.create().texOffs(41, 86)
				.addBox(-0.6789F, -4.0256F, -0.9218F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(41, 20)
				.addBox(-0.1789F, -0.0256F, -0.4218F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(72, 54)
				.addBox(-1.1789F, -3.7756F, 0.0782F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.525F, -4.975F, 0.075F));

		PartDefinition arm19 = arm18.addOrReplaceChild("arm19", CubeListBuilder.create().texOffs(0, 71)
				.addBox(-0.7039F, -4.0006F, -0.8468F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(38, 21)
				.addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.025F, -4.025F, -0.075F));

		PartDefinition arm20 = arm19.addOrReplaceChild("arm20", CubeListBuilder.create().texOffs(91, 31)
				.addBox(-1.7039F, -4.7506F, 0.1532F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(24, 97)
				.addBox(-0.7039F, -5.7506F, 0.1532F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(28, 91)
				.addBox(-1.2039F, -4.0006F, -0.3468F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(35, 20)
				.addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition bone2 = brain.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F,
				-15.3381F, -10.5551F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.25F, -0.25F));

		PartDefinition bone3 = brain.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 71).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, -6.0F, -2.15F));

		PartDefinition bone4 = brain.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(34, 46).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.9F, -8.0F, -3.4F));

		PartDefinition bone5 = brain.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(80, 80).addBox(-3.0F,
				-18.3381F, -4.5551F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.15F, -8.0F, 4.4F));

		PartDefinition bone6 = brain.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 38).addBox(-4.5F,
				-18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.4F, -6.0F, 5.35F));

		PartDefinition bone7 = brain.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(24, 63).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, -6.0F, 4.6F));

		PartDefinition bone8 = brain.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(26, 29).addBox(-4.5F,
				-18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.6F, -6.5F, -1.75F));

		PartDefinition bone9 = brain.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(58, 38).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.1F, -5.25F, -4.15F));

		PartDefinition bone10 = brain.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(58, 58).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-6.1F, -5.75F, 2.55F));

		PartDefinition bone11 = brain.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(0, 55).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.9F, -5.0F, -4.35F));

		PartDefinition bone12 = brain.addOrReplaceChild("bone12",
				CubeListBuilder.create().texOffs(0, 20)
						.addBox(-13.5F, -12.8381F, -12.5551F, 9.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(0, 20).addBox(-7.5F, -12.8381F, -9.5551F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(0, 20).addBox(-3.5F, -10.8381F, -11.5551F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(0, 20).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(5.4F, -5.0F, 3.35F));

		PartDefinition bone13 = brain.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(54, 0).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.1F, -4.35F, -6.15F));

		PartDefinition bone16 = brain.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(48, 74).addBox(-4.0F,
				-17.3381F, -5.5551F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.85F, -4.5F, -5.4F));

		PartDefinition bone15 = brain.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(52, 20).addBox(-4.0F,
				-18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.9F, -5.5F, 6.6F));

		PartDefinition eye = brain.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 87).addBox(-1.5F, -1.5F,
				-1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -26.3381F, -3.0551F));

		PartDefinition eye2 = brain.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(85, 25).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, -24.3381F, 2.9449F));

		PartDefinition eye3 = brain.addOrReplaceChild("eye3", CubeListBuilder.create().texOffs(29, 85).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.75F, -25.3381F, 2.9449F));

		PartDefinition eye7 = brain.addOrReplaceChild("eye7", CubeListBuilder.create().texOffs(76, 22).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-4.75F, -19.3381F, 8.9449F));

		PartDefinition eye12 = brain.addOrReplaceChild("eye12", CubeListBuilder.create().texOffs(72, 74).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.25F, -22.5881F, 7.9449F));

		PartDefinition eye8 = brain.addOrReplaceChild("eye8", CubeListBuilder.create().texOffs(82, 39).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.75F, -17.3381F, -12.0551F));

		PartDefinition eye9 = brain.addOrReplaceChild("eye9", CubeListBuilder.create().texOffs(82, 33).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -17.3381F, -8.0551F));

		PartDefinition eye13 = brain.addOrReplaceChild("eye13", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -19.3381F, 3.9449F));

		PartDefinition eye10 = brain.addOrReplaceChild("eye10", CubeListBuilder.create().texOffs(32, 79).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-9.75F, -19.3381F, -7.0551F));

		PartDefinition eye11 = brain.addOrReplaceChild("eye11", CubeListBuilder.create().texOffs(78, 0).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-9.75F, -17.3381F, 6.9449F));

		PartDefinition eye4 = brain.addOrReplaceChild("eye4", CubeListBuilder.create().texOffs(84, 74).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.25F, -24.3381F, 2.9449F));

		PartDefinition eye5 = brain.addOrReplaceChild("eye5", CubeListBuilder.create().texOffs(82, 60).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(7.25F, -24.3381F, -6.0551F));

		PartDefinition eye6 = brain.addOrReplaceChild("eye6", CubeListBuilder.create().texOffs(82, 54).addBox(-1.5F,
				-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.75F, -24.3381F, -9.0551F));

		PartDefinition rightArm = torso.addOrReplaceChild("rightArm",
				CubeListBuilder.create().texOffs(67, 118)
						.addBox(-5.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(67, 118)
						.addBox(-11.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-8.0F, -8.75F, -2.5F, 0.0F, -0.5236F, -1.0908F));

		PartDefinition rightBicep = rightArm.addOrReplaceChild("rightBicep",
				CubeListBuilder.create().texOffs(55, 108)
						.addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(1, 111)
						.addBox(-3.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-11.0F, 0.1F, 0.0F, 0.0F, 0.0F, -0.48F));

		PartDefinition rightFore = rightBicep.addOrReplaceChild("rightFore",
				CubeListBuilder.create().texOffs(1, 111)
						.addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(59, 102)
						.addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.0F, -0.1F, 0.0F, 0.0F, 0.7418F, 0.0F));

		PartDefinition rightWrist = rightFore.addOrReplaceChild("rightWrist",
				CubeListBuilder.create().texOffs(1, 111)
						.addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(55, 108)
						.addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.0F, 0.1F, 0.0F, 0.0F, 0.2618F, -0.3054F));

		PartDefinition rightHand = rightWrist.addOrReplaceChild("rightHand",
				CubeListBuilder.create().texOffs(63, 113).addBox(-6.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.0F, 0.25F, 0.5F, 0.0F, -0.6981F, 0.0F));

		PartDefinition leftArm = torso.addOrReplaceChild("leftArm",
				CubeListBuilder.create().texOffs(67, 118)
						.addBox(0.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(58, 112)
						.addBox(5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(9.0F, -8.75F, -2.5F, 0.0F, 0.5236F, 1.0908F));

		PartDefinition leftBicep = leftArm.addOrReplaceChild("leftBicep",
				CubeListBuilder.create().texOffs(68, 120)
						.addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(1, 111)
						.addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(11.0F, 0.1F, 0.0F, 0.0F, 0.0F, 0.48F));

		PartDefinition leftFore = leftBicep.addOrReplaceChild("leftFore",
				CubeListBuilder.create().texOffs(1, 111)
						.addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(67, 118)
						.addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.0F, -0.1F, 0.0F, 0.0F, -0.7418F, 0.0F));

		PartDefinition leftWrist = leftFore.addOrReplaceChild("leftWrist",
				CubeListBuilder.create().texOffs(1, 111)
						.addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(60, 109)
						.addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.0F, 0.1F, 0.0F, 0.0F, -0.2618F, 0.3054F));

		PartDefinition leftHand = leftWrist
				.addOrReplaceChild("leftHand",
						CubeListBuilder.create().texOffs(75, 106).addBox(0.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(6.0F, 0.25F, 0.5F, 0.0F, 0.6981F, 0.0F));

		PartDefinition hips = partdefinition.addOrReplaceChild("hips",
				CubeListBuilder.create().texOffs(59, 106)
						.addBox(-3.5F, -6.5F, -3.0F, 7.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 106)
						.addBox(-2.5F, -5.5F, -2.6F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 2.5F, 7.0F));

		PartDefinition leftLeg = hips.addOrReplaceChild("leftLeg",
				CubeListBuilder.create().texOffs(48, 115)
						.addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(48, 115)
						.addBox(-2.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(48, 115)
						.addBox(-2.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(4.5F, 1.5F, 0.0F));

		PartDefinition leftLeg2 = leftLeg.addOrReplaceChild("leftLeg2",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

		PartDefinition leftLeg3 = leftLeg2.addOrReplaceChild("leftLeg3",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 7.0F, 0.0F, 0.9599F, 0.0F, 0.0F));

		PartDefinition leftLeg4 = leftLeg3.addOrReplaceChild("leftLeg4",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

		PartDefinition rightLeg = hips.addOrReplaceChild("rightLeg",
				CubeListBuilder.create().texOffs(48, 115)
						.addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(48, 115)
						.addBox(-4.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(48, 115)
						.addBox(-4.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.5F, 1.5F, 0.0F));

		PartDefinition rightLeg2 = rightLeg.addOrReplaceChild("rightLeg2",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.0F, -3.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

		PartDefinition rightLeg3 = rightLeg2.addOrReplaceChild("rightLeg3",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 7.0F, 0.0F, 0.9599F, 0.0F, 0.0F));

		PartDefinition rightLeg4 = rightLeg3.addOrReplaceChild("rightLeg4",
				CubeListBuilder.create().texOffs(62, 106)
						.addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(62, 106)
						.addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
		hips.render(poseStack, buffer, packedLight, packedOverlay);
	}
}