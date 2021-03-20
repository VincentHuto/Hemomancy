package com.huto.hemomancy.model.entity.mob;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAbhorentThought extends EntityModel<Entity> {
	private final ModelRenderer whole;
	private final ModelRenderer body;
	private final ModelRenderer abdomen;
	private final ModelRenderer torso;
	private final ModelRenderer neck;
	private final ModelRenderer head;
	private final ModelRenderer bone;
	private final ModelRenderer arm;
	private final ModelRenderer arm2;
	private final ModelRenderer arm3;
	private final ModelRenderer arm4;
	private final ModelRenderer arm5;
	private final ModelRenderer arm6;
	private final ModelRenderer arm7;
	private final ModelRenderer arm8;
	private final ModelRenderer arm9;
	private final ModelRenderer arm10;
	private final ModelRenderer arm21;
	private final ModelRenderer arm22;
	private final ModelRenderer arm23;
	private final ModelRenderer arm24;
	private final ModelRenderer arm25;
	private final ModelRenderer arm11;
	private final ModelRenderer arm12;
	private final ModelRenderer arm13;
	private final ModelRenderer arm14;
	private final ModelRenderer arm15;
	private final ModelRenderer arm16;
	private final ModelRenderer arm17;
	private final ModelRenderer arm18;
	private final ModelRenderer arm19;
	private final ModelRenderer arm20;
	private final ModelRenderer bone2;
	private final ModelRenderer bone3;
	private final ModelRenderer bone4;
	private final ModelRenderer bone5;
	private final ModelRenderer bone6;
	private final ModelRenderer bone7;
	private final ModelRenderer bone8;
	private final ModelRenderer bone9;
	private final ModelRenderer bone10;
	private final ModelRenderer bone11;
	private final ModelRenderer bone12;
	private final ModelRenderer bone13;
	private final ModelRenderer bone16;
	private final ModelRenderer bone15;
	private final ModelRenderer eye;
	private final ModelRenderer eye2;
	private final ModelRenderer eye3;
	private final ModelRenderer eye7;
	private final ModelRenderer eye12;
	private final ModelRenderer eye8;
	private final ModelRenderer eye9;
	private final ModelRenderer eye13;
	private final ModelRenderer eye10;
	private final ModelRenderer eye11;
	private final ModelRenderer eye4;
	private final ModelRenderer eye5;
	private final ModelRenderer eye6;
	private final ModelRenderer rightArm;
	private final ModelRenderer rightBicep;
	private final ModelRenderer rightFore;
	private final ModelRenderer rightWrist;
	private final ModelRenderer rightHand;
	private final ModelRenderer leftArm;
	private final ModelRenderer leftBicep;
	private final ModelRenderer leftFore;
	private final ModelRenderer leftWrist;
	private final ModelRenderer leftHand;
	private final ModelRenderer hips;
	private final ModelRenderer leftLeg;
	private final ModelRenderer leftLeg2;
	private final ModelRenderer leftLeg3;
	private final ModelRenderer leftLeg4;
	private final ModelRenderer rightLeg;
	private final ModelRenderer rightLeg2;
	private final ModelRenderer rightLeg3;
	private final ModelRenderer rightLeg4;

	public ModelAbhorentThought() {
		textureWidth = 128;
		textureHeight = 128;

		whole = new ModelRenderer(this);
		whole.setRotationPoint(0.0F, 16.0F, 8.0F);

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		whole.addChild(body);

		abdomen = new ModelRenderer(this);
		abdomen.setRotationPoint(0.0F, -20.0F, -1.0F);
		body.addChild(abdomen);
		abdomen.setTextureOffset(59, 106).addBox(-3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-2.0F, -5.0F, -3.0F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-2.0F, -5.0F, 2.0F, 5.0F, 3.0F, 2.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-2.0F, -9.0F, 2.0F, 5.0F, 3.0F, 2.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-4.0F, -10.0F, -3.0F, 9.0F, 5.0F, 6.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.setTextureOffset(59, 106).addBox(-4.0F, -10.0F, -4.0F, 9.0F, 4.0F, 1.0F, 0.0F, false);

		torso = new ModelRenderer(this);
		torso.setRotationPoint(0.0F, -10.0F, 1.0F);
		abdomen.addChild(torso);
		setRotationAngle(torso, 0.2618F, 0.0F, 0.0F);
		torso.setTextureOffset(44, 104).addBox(-6.0F, -2.0F, -5.0F, 13.0F, 2.0F, 6.0F, 0.0F, false);
		torso.setTextureOffset(44, 104).addBox(-3.0F, -1.0F, -4.0F, 7.0F, 3.0F, 4.0F, 0.0F, false);
		torso.setTextureOffset(21, 112).addBox(-7.0F, -6.0F, -6.0F, 15.0F, 5.0F, 8.0F, 0.0F, false);
		torso.setTextureOffset(21, 112).addBox(-6.0F, -6.0F, 2.0F, 13.0F, 4.0F, 2.0F, 0.0F, false);
		torso.setTextureOffset(1, 111).addBox(-9.0F, -11.0F, 3.0F, 19.0F, 5.0F, 1.0F, 0.0F, false);
		torso.setTextureOffset(68, 106).addBox(-4.0F, -11.0F, 4.0F, 9.0F, 6.0F, 1.0F, 0.0F, false);
		torso.setTextureOffset(1, 111).addBox(-8.0F, -12.0F, -8.0F, 17.0F, 6.0F, 11.0F, 0.0F, false);

		neck = new ModelRenderer(this);
		neck.setRotationPoint(0.0F, -12.0F, -2.0F);
		torso.addChild(neck);
		setRotationAngle(neck, 0.2618F, 0.0F, 0.0F);
		neck.setTextureOffset(21, 112).addBox(6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-4.0F, -5.0F, 3.0F, 9.0F, 7.0F, 1.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-2.0F, -5.0F, 4.0F, 5.0F, 7.0F, 1.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-5.0F, -6.0F, -5.0F, 11.0F, 8.0F, 8.0F, 0.0F, false);

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -4.0F, -1.0F);
		neck.addChild(head);

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0F, 15.3781F, -2.4547F);
		head.addChild(bone);

		arm = new ModelRenderer(this);
		arm.setRotationPoint(-7.5F, -10.0F, -0.5F);
		bone.addChild(arm);
		setRotationAngle(arm, 0.0F, 0.0F, -0.6109F);
		arm.setTextureOffset(44, 20).addBox(6.724F, -13.2451F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm2 = new ModelRenderer(this);
		arm2.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm.addChild(arm2);
		arm2.setTextureOffset(0, 7).addBox(7.724F, -10.7451F, -2.0551F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm2.setTextureOffset(0, 55).addBox(7.224F, -15.7451F, -2.5551F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm2.setTextureOffset(60, 41).addBox(6.724F, -15.4951F, -1.5551F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm3 = new ModelRenderer(this);
		arm3.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm2.addChild(arm3);
		arm3.setTextureOffset(90, 0).addBox(7.249F, -15.7701F, -3.1301F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm3.setTextureOffset(30, 55).addBox(7.749F, -11.7701F, -2.6301F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm3.setTextureOffset(50, 94).addBox(6.749F, -15.5201F, -2.1301F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm4 = new ModelRenderer(this);
		arm4.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm3.addChild(arm4);
		arm4.setTextureOffset(65, 88).addBox(7.224F, -15.7451F, -2.5551F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm4.setTextureOffset(55, 25).addBox(7.724F, -11.7451F, -2.0551F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm5 = new ModelRenderer(this);
		arm5.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm4.addChild(arm5);
		arm5.setTextureOffset(93, 71).addBox(6.224F, -16.4951F, -1.5551F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(24, 97).addBox(7.224F, -17.4951F, -1.5551F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(16, 93).addBox(6.724F, -15.7451F, -2.0551F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm5.setTextureOffset(56, 21).addBox(7.724F, -11.7451F, -2.0551F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm6 = new ModelRenderer(this);
		arm6.setRotationPoint(-0.5F, -10.0F, 7.5F);
		bone.addChild(arm6);
		setRotationAngle(arm6, -0.6981F, 0.0F, 0.5672F);
		arm6.setTextureOffset(35, 23).addBox(-9.2039F, -9.7639F, -10.4643F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm7 = new ModelRenderer(this);
		arm7.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm6.addChild(arm7);
		arm7.setTextureOffset(0, 6).addBox(-8.2039F, -7.2639F, -9.4643F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm7.setTextureOffset(54, 0).addBox(-8.7039F, -12.2639F, -9.9643F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm7.setTextureOffset(60, 36).addBox(-9.2039F, -12.0139F, -8.9643F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm8 = new ModelRenderer(this);
		arm8.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm7.addChild(arm8);
		arm8.setTextureOffset(57, 88).addBox(-8.6789F, -12.2889F, -10.5393F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm8.setTextureOffset(54, 16).addBox(-8.1789F, -8.2889F, -10.0393F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm8.setTextureOffset(44, 94).addBox(-9.1789F, -12.0389F, -9.5393F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm9 = new ModelRenderer(this);
		arm9.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm8.addChild(arm9);
		arm9.setTextureOffset(49, 88).addBox(-8.7039F, -12.2639F, -9.9643F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm9.setTextureOffset(53, 20).addBox(-8.2039F, -8.2639F, -9.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm9.setTextureOffset(73, 92).addBox(-9.2039F, -12.0139F, -8.9643F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm10 = new ModelRenderer(this);
		arm10.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm9.addChild(arm10);
		arm10.setTextureOffset(8, 93).addBox(-9.7039F, -13.0139F, -8.9643F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(24, 97).addBox(-8.7039F, -14.0139F, -8.9643F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(0, 93).addBox(-9.2039F, -12.2639F, -9.4643F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm10.setTextureOffset(51, 26).addBox(-8.2039F, -8.2639F, -9.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm21 = new ModelRenderer(this);
		arm21.setRotationPoint(-0.5F, -11.0F, -0.5F);
		bone.addChild(arm21);
		setRotationAngle(arm21, -0.7854F, 0.0F, -0.5236F);
		arm21.setTextureOffset(0, 6).addBox(5.6691F, -9.1806F, -11.3799F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm22 = new ModelRenderer(this);
		arm22.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm21.addChild(arm22);
		arm22.setTextureOffset(0, 0).addBox(6.6691F, -6.6806F, -10.3799F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm22.setTextureOffset(0, 20).addBox(6.1691F, -11.6806F, -10.8799F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm22.setTextureOffset(12, 12).addBox(5.6691F, -11.4306F, -9.8799F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm23 = new ModelRenderer(this);
		arm23.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm22.addChild(arm23);
		arm23.setTextureOffset(48, 62).addBox(6.1941F, -11.7056F, -11.4549F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm23.setTextureOffset(30, 26).addBox(6.6941F, -7.7056F, -10.9549F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm23.setTextureOffset(66, 54).addBox(5.6941F, -11.4556F, -10.4549F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm24 = new ModelRenderer(this);
		arm24.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm23.addChild(arm24);
		arm24.setTextureOffset(24, 55).addBox(6.1691F, -11.6806F, -10.8799F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm24.setTextureOffset(26, 26).addBox(6.6691F, -7.6806F, -10.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm25 = new ModelRenderer(this);
		arm25.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm24.addChild(arm25);
		arm25.setTextureOffset(90, 66).addBox(5.1691F, -12.4306F, -9.8799F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(24, 97).addBox(6.1691F, -13.4306F, -9.8799F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(90, 45).addBox(5.6691F, -11.6806F, -10.3799F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm25.setTextureOffset(12, 10).addBox(6.6691F, -7.6806F, -10.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm11 = new ModelRenderer(this);
		arm11.setRotationPoint(3.5F, -9.0F, -7.5F);
		bone.addChild(arm11);
		setRotationAngle(arm11, 1.1345F, 0.0F, -0.48F);
		arm11.setTextureOffset(26, 20).addBox(5.1206F, -8.2843F, 9.3693F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm12 = new ModelRenderer(this);
		arm12.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm11.addChild(arm12);
		arm12.setTextureOffset(0, 2).addBox(6.1206F, -5.7843F, 10.3693F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm12.setTextureOffset(34, 46).addBox(5.6206F, -10.7843F, 9.8693F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm12.setTextureOffset(12, 5).addBox(5.1206F, -10.5343F, 10.8693F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm13 = new ModelRenderer(this);
		arm13.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm12.addChild(arm13);
		arm13.setTextureOffset(20, 87).addBox(5.6456F, -10.8093F, 9.2943F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm13.setTextureOffset(47, 26).addBox(6.1456F, -6.8093F, 9.7943F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm13.setTextureOffset(73, 88).addBox(5.1456F, -10.5593F, 10.2943F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm14 = new ModelRenderer(this);
		arm14.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm13.addChild(arm14);
		arm14.setTextureOffset(12, 87).addBox(5.6206F, -10.7843F, 9.8693F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm14.setTextureOffset(4, 44).addBox(6.1206F, -6.7843F, 10.3693F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm15 = new ModelRenderer(this);
		arm15.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm14.addChild(arm15);
		arm15.setTextureOffset(36, 92).addBox(4.6206F, -11.5343F, 10.8693F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(24, 97).addBox(5.6206F, -12.5343F, 10.8693F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(91, 50).addBox(5.1206F, -10.7843F, 10.3693F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm15.setTextureOffset(0, 44).addBox(6.1206F, -6.7843F, 10.3693F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm16 = new ModelRenderer(this);
		arm16.setRotationPoint(6.5F, -9.0F, -0.5F);
		bone.addChild(arm16);
		setRotationAngle(arm16, 0.3054F, 0.0F, 0.5672F);
		arm16.setTextureOffset(0, 12).addBox(-9.2039F, -13.5006F, 0.6532F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm17 = new ModelRenderer(this);
		arm17.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm16.addChild(arm17);
		arm17.setTextureOffset(0, 1).addBox(-8.2039F, -11.0006F, 1.6532F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm17.setTextureOffset(0, 37).addBox(-8.7039F, -16.0006F, 1.1532F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm17.setTextureOffset(12, 0).addBox(-9.2039F, -15.7506F, 2.1532F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm18 = new ModelRenderer(this);
		arm18.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm17.addChild(arm18);
		arm18.setTextureOffset(41, 86).addBox(-8.6789F, -16.0256F, 0.5782F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm18.setTextureOffset(41, 20).addBox(-8.1789F, -12.0256F, 1.0782F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm18.setTextureOffset(72, 54).addBox(-9.1789F, -15.7756F, 1.5782F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm19 = new ModelRenderer(this);
		arm19.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm18.addChild(arm19);
		arm19.setTextureOffset(0, 71).addBox(-8.7039F, -16.0006F, 1.1532F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm19.setTextureOffset(38, 21).addBox(-8.2039F, -12.0006F, 1.6532F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm20 = new ModelRenderer(this);
		arm20.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm19.addChild(arm20);
		arm20.setTextureOffset(91, 31).addBox(-9.7039F, -16.7506F, 2.1532F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(24, 97).addBox(-8.7039F, -17.7506F, 2.1532F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(28, 91).addBox(-9.2039F, -16.0006F, 1.6532F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm20.setTextureOffset(35, 20).addBox(-8.2039F, -12.0006F, 1.6532F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(0.0F, -1.25F, -0.25F);
		bone.addChild(bone2);
		bone2.setTextureOffset(0, 0).addBox(-9.0F, -15.3381F, -10.5551F, 18.0F, 2.0F, 18.0F, 0.0F, false);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.setTextureOffset(0, 71).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.setTextureOffset(34, 46).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.setTextureOffset(80, 80).addBox(-3.0F, -18.3381F, -4.5551F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(3.4F, -6.0F, 5.35F);
		bone.addChild(bone6);
		bone6.setTextureOffset(0, 38).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.setTextureOffset(24, 63).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(-2.6F, -6.5F, -1.75F);
		bone.addChild(bone8);
		bone8.setTextureOffset(26, 29).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.setTextureOffset(58, 38).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(-6.1F, -5.75F, 2.55F);
		bone.addChild(bone10);
		bone10.setTextureOffset(58, 58).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(3.9F, -5.0F, -4.35F);
		bone.addChild(bone11);
		bone11.setTextureOffset(0, 55).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(5.4F, -5.0F, 3.35F);
		bone.addChild(bone12);
		bone12.setTextureOffset(0, 20).addBox(-13.5F, -12.8381F, -12.5551F, 9.0F, 6.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-7.5F, -12.8381F, -9.5551F, 7.0F, 8.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-3.5F, -10.8381F, -11.5551F, 4.0F, 3.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new ModelRenderer(this);
		bone13.setRotationPoint(-0.1F, -4.35F, -6.15F);
		bone.addChild(bone13);
		bone13.setTextureOffset(54, 0).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new ModelRenderer(this);
		bone16.setRotationPoint(-5.85F, -4.5F, -5.4F);
		bone.addChild(bone16);
		bone16.setTextureOffset(48, 74).addBox(-4.0F, -17.3381F, -5.5551F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new ModelRenderer(this);
		bone15.setRotationPoint(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.setTextureOffset(52, 20).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new ModelRenderer(this);
		eye.setRotationPoint(-3.0F, -10.0F, -5.5F);
		bone.addChild(eye);
		eye.setTextureOffset(0, 87).addBox(2.5F, -17.8381F, 0.9449F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new ModelRenderer(this);
		eye2.setRotationPoint(-7.5F, -10.0F, 4.5F);
		bone.addChild(eye2);
		eye2.setTextureOffset(85, 25).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new ModelRenderer(this);
		eye3.setRotationPoint(-0.75F, -11.0F, 4.5F);
		bone.addChild(eye3);
		eye3.setTextureOffset(29, 85).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new ModelRenderer(this);
		eye7.setRotationPoint(2.25F, -7.0F, 9.5F);
		bone.addChild(eye7);
		eye7.setTextureOffset(76, 22).addBox(-8.5F, -13.8381F, -2.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new ModelRenderer(this);
		eye12.setRotationPoint(12.25F, -10.25F, 8.5F);
		bone.addChild(eye12);
		eye12.setTextureOffset(72, 74).addBox(-8.5F, -13.8381F, -2.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new ModelRenderer(this);
		eye8.setRotationPoint(-0.75F, -3.0F, -10.5F);
		bone.addChild(eye8);
		eye8.setTextureOffset(82, 39).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new ModelRenderer(this);
		eye9.setRotationPoint(8.0F, -3.0F, -6.5F);
		bone.addChild(eye9);
		eye9.setTextureOffset(82, 33).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new ModelRenderer(this);
		eye13.setRotationPoint(10.0F, -5.0F, 5.5F);
		bone.addChild(eye13);
		eye13.setTextureOffset(0, 0).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new ModelRenderer(this);
		eye10.setRotationPoint(-9.75F, -5.0F, -5.5F);
		bone.addChild(eye10);
		eye10.setTextureOffset(32, 79).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new ModelRenderer(this);
		eye11.setRotationPoint(-9.75F, -3.0F, 8.5F);
		bone.addChild(eye11);
		eye11.setTextureOffset(78, 0).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new ModelRenderer(this);
		eye4.setRotationPoint(7.25F, -10.0F, 4.5F);
		bone.addChild(eye4);
		eye4.setTextureOffset(84, 74).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new ModelRenderer(this);
		eye5.setRotationPoint(7.25F, -10.0F, -4.5F);
		bone.addChild(eye5);
		eye5.setTextureOffset(82, 60).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new ModelRenderer(this);
		eye6.setRotationPoint(-3.75F, -10.0F, -7.5F);
		bone.addChild(eye6);
		eye6.setTextureOffset(82, 54).addBox(-1.5F, -15.8381F, -3.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightArm = new ModelRenderer(this);
		rightArm.setRotationPoint(-8.0F, -8.75F, -2.5F);
		torso.addChild(rightArm);
		setRotationAngle(rightArm, 0.0F, -0.5236F, -1.0908F);
		rightArm.setTextureOffset(67, 118).addBox(-5.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		rightArm.setTextureOffset(67, 118).addBox(-11.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightBicep = new ModelRenderer(this);
		rightBicep.setRotationPoint(-11.0F, 0.1F, 0.0F);
		rightArm.addChild(rightBicep);
		setRotationAngle(rightBicep, 0.0F, 0.0F, -0.48F);
		rightBicep.setTextureOffset(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		rightBicep.setTextureOffset(1, 111).addBox(-3.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		rightFore = new ModelRenderer(this);
		rightFore.setRotationPoint(-6.0F, -0.1F, 0.0F);
		rightBicep.addChild(rightFore);
		setRotationAngle(rightFore, 0.0F, 0.7418F, 0.0F);
		rightFore.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightFore.setTextureOffset(59, 102).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightWrist = new ModelRenderer(this);
		rightWrist.setRotationPoint(-6.0F, 0.1F, 0.0F);
		rightFore.addChild(rightWrist);
		setRotationAngle(rightWrist, 0.0F, 0.2618F, -0.3054F);
		rightWrist.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightWrist.setTextureOffset(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightHand = new ModelRenderer(this);
		rightHand.setRotationPoint(-6.0F, 0.25F, 0.5F);
		rightWrist.addChild(rightHand);
		setRotationAngle(rightHand, 0.0F, -0.6981F, 0.0F);
		rightHand.setTextureOffset(63, 113).addBox(-6.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		leftArm = new ModelRenderer(this);
		leftArm.setRotationPoint(9.0F, -8.75F, -2.5F);
		torso.addChild(leftArm);
		setRotationAngle(leftArm, 0.0F, 0.5236F, 1.0908F);
		leftArm.setTextureOffset(67, 118).addBox(0.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		leftArm.setTextureOffset(58, 112).addBox(5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftBicep = new ModelRenderer(this);
		leftBicep.setRotationPoint(11.0F, 0.1F, 0.0F);
		leftArm.addChild(leftBicep);
		setRotationAngle(leftBicep, 0.0F, 0.0F, 0.48F);
		leftBicep.setTextureOffset(68, 120).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		leftBicep.setTextureOffset(1, 111).addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		leftFore = new ModelRenderer(this);
		leftFore.setRotationPoint(6.0F, -0.1F, 0.0F);
		leftBicep.addChild(leftFore);
		setRotationAngle(leftFore, 0.0F, -0.7418F, 0.0F);
		leftFore.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		leftFore.setTextureOffset(67, 118).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftWrist = new ModelRenderer(this);
		leftWrist.setRotationPoint(6.0F, 0.1F, 0.0F);
		leftFore.addChild(leftWrist);
		setRotationAngle(leftWrist, 0.0F, -0.2618F, 0.3054F);
		leftWrist.setTextureOffset(1, 111).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		leftWrist.setTextureOffset(60, 109).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftHand = new ModelRenderer(this);
		leftHand.setRotationPoint(6.0F, 0.25F, 0.5F);
		leftWrist.addChild(leftHand);
		setRotationAngle(leftHand, 0.0F, 0.6981F, 0.0F);
		leftHand.setTextureOffset(75, 106).addBox(0.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		hips = new ModelRenderer(this);
		hips.setRotationPoint(0.5F, 2.5F, 7.0F);
		hips.setTextureOffset(59, 106).addBox(-3.5F, -6.5F, -3.0F, 7.0F, 3.0F, 6.0F, 0.0F, false);
		hips.setTextureOffset(59, 106).addBox(-2.5F, -5.5F, -2.6F, 5.0F, 3.0F, 6.0F, 0.0F, false);

		leftLeg = new ModelRenderer(this);
		leftLeg.setRotationPoint(4.5F, 1.5F, 0.0F);
		hips.addChild(leftLeg);
		leftLeg.setTextureOffset(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.setTextureOffset(48, 115).addBox(-2.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.setTextureOffset(48, 115).addBox(-2.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		leftLeg2 = new ModelRenderer(this);
		leftLeg2.setRotationPoint(0.0F, -3.0F, 0.0F);
		leftLeg.addChild(leftLeg2);
		setRotationAngle(leftLeg2, -0.5672F, 0.0F, 0.0F);
		leftLeg2.setTextureOffset(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg2.setTextureOffset(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg3 = new ModelRenderer(this);
		leftLeg3.setRotationPoint(2.0F, 7.0F, 0.0F);
		leftLeg2.addChild(leftLeg3);
		setRotationAngle(leftLeg3, 0.9599F, 0.0F, 0.0F);
		leftLeg3.setTextureOffset(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg3.setTextureOffset(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg3.setTextureOffset(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg4 = new ModelRenderer(this);
		leftLeg4.setRotationPoint(0.0F, 11.0F, 0.0F);
		leftLeg3.addChild(leftLeg4);
		setRotationAngle(leftLeg4, -0.3927F, 0.0F, 0.0F);
		leftLeg4.setTextureOffset(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);

		rightLeg = new ModelRenderer(this);
		rightLeg.setRotationPoint(-1.5F, 1.5F, 0.0F);
		hips.addChild(rightLeg);
		rightLeg.setTextureOffset(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.setTextureOffset(48, 115).addBox(-4.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.setTextureOffset(48, 115).addBox(-4.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightLeg2 = new ModelRenderer(this);
		rightLeg2.setRotationPoint(-4.0F, -3.0F, 0.0F);
		rightLeg.addChild(rightLeg2);
		setRotationAngle(rightLeg2, -0.5672F, 0.0F, 0.0F);
		rightLeg2.setTextureOffset(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg2.setTextureOffset(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg3 = new ModelRenderer(this);
		rightLeg3.setRotationPoint(2.0F, 7.0F, 0.0F);
		rightLeg2.addChild(rightLeg3);
		setRotationAngle(rightLeg3, 0.9599F, 0.0F, 0.0F);
		rightLeg3.setTextureOffset(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg3.setTextureOffset(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		rightLeg3.setTextureOffset(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg4 = new ModelRenderer(this);
		rightLeg4.setRotationPoint(0.0F, 11.0F, 0.0F);
		rightLeg3.addChild(rightLeg4);
		setRotationAngle(rightLeg4, -0.3927F, 0.0F, 0.0F);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
		hips.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}