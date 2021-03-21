package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityAbhorentThought;
import com.huto.hemomancy.model.animation.AnimatedEntityModel;
import com.huto.hemomancy.model.animation.AnimatedModelRenderer;
import com.huto.hemomancy.model.animation.ModelAnimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.math.MathHelper;

public class ModelAbhorentThought extends AnimatedEntityModel<EntityAbhorentThought> {
	private final AnimatedModelRenderer whole;
	private final AnimatedModelRenderer body;
	private final AnimatedModelRenderer abdomen;
	private final AnimatedModelRenderer torso;
	private final AnimatedModelRenderer neck;
	private final AnimatedModelRenderer head;
	private final AnimatedModelRenderer bone;
	private final AnimatedModelRenderer arm;
	private final AnimatedModelRenderer arm2;
	private final AnimatedModelRenderer arm3;
	private final AnimatedModelRenderer arm4;
	private final AnimatedModelRenderer arm5;
	private final AnimatedModelRenderer arm6;
	private final AnimatedModelRenderer arm7;
	private final AnimatedModelRenderer arm8;
	private final AnimatedModelRenderer arm9;
	private final AnimatedModelRenderer arm10;
	private final AnimatedModelRenderer arm21;
	private final AnimatedModelRenderer arm22;
	private final AnimatedModelRenderer arm23;
	private final AnimatedModelRenderer arm24;
	private final AnimatedModelRenderer arm25;
	private final AnimatedModelRenderer arm11;
	private final AnimatedModelRenderer arm12;
	private final AnimatedModelRenderer arm13;
	private final AnimatedModelRenderer arm14;
	private final AnimatedModelRenderer arm15;
	private final AnimatedModelRenderer arm16;
	private final AnimatedModelRenderer arm17;
	private final AnimatedModelRenderer arm18;
	private final AnimatedModelRenderer arm19;
	private final AnimatedModelRenderer arm20;
	private final AnimatedModelRenderer bone2;
	private final AnimatedModelRenderer bone3;
	private final AnimatedModelRenderer bone4;
	private final AnimatedModelRenderer bone5;
	private final AnimatedModelRenderer bone6;
	private final AnimatedModelRenderer bone7;
	private final AnimatedModelRenderer bone8;
	private final AnimatedModelRenderer bone9;
	private final AnimatedModelRenderer bone10;
	private final AnimatedModelRenderer bone11;
	private final AnimatedModelRenderer bone12;
	private final AnimatedModelRenderer bone13;
	private final AnimatedModelRenderer bone16;
	private final AnimatedModelRenderer bone15;
	private final AnimatedModelRenderer eye;
	private final AnimatedModelRenderer eye2;
	private final AnimatedModelRenderer eye3;
	private final AnimatedModelRenderer eye7;
	private final AnimatedModelRenderer eye12;
	private final AnimatedModelRenderer eye8;
	private final AnimatedModelRenderer eye9;
	private final AnimatedModelRenderer eye13;
	private final AnimatedModelRenderer eye10;
	private final AnimatedModelRenderer eye11;
	private final AnimatedModelRenderer eye4;
	private final AnimatedModelRenderer eye5;
	private final AnimatedModelRenderer eye6;
	private final AnimatedModelRenderer rightArm;
	private final AnimatedModelRenderer rightBicep;
	private final AnimatedModelRenderer rightFore;
	private final AnimatedModelRenderer rightWrist;
	private final AnimatedModelRenderer rightHand;
	private final AnimatedModelRenderer leftArm;
	private final AnimatedModelRenderer leftBicep;
	private final AnimatedModelRenderer leftFore;
	private final AnimatedModelRenderer leftWrist;
	private final AnimatedModelRenderer leftHand;
	private final AnimatedModelRenderer hips;
	private final AnimatedModelRenderer leftLeg;
	private final AnimatedModelRenderer leftLeg2;
	private final AnimatedModelRenderer leftLeg3;
	private final AnimatedModelRenderer leftLeg4;
	private final AnimatedModelRenderer rightLeg;
	private final AnimatedModelRenderer rightLeg2;
	private final AnimatedModelRenderer rightLeg3;
	private final AnimatedModelRenderer rightLeg4;
	public ModelAnimator animator;
	public final AnimatedModelRenderer[] headArray;

	public ModelAbhorentThought() {
		textureWidth = 128;
		textureHeight = 128;

		whole = new AnimatedModelRenderer(this);
		whole.setRotationPoint(0.0F, 16.0F, 8.0F);

		body = new AnimatedModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		whole.addChild(body);

		abdomen = new AnimatedModelRenderer(this);
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

		torso = new AnimatedModelRenderer(this);
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

		neck = new AnimatedModelRenderer(this);
		neck.setRotationPoint(0.0F, -12.0F, -2.0F);
		torso.addChild(neck);
		setRotationAngle(neck, 0.2618F, 0.0F, 0.0F);
		neck.setTextureOffset(21, 112).addBox(6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-4.0F, -5.0F, 3.0F, 9.0F, 7.0F, 1.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-2.0F, -5.0F, 4.0F, 5.0F, 7.0F, 1.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.setTextureOffset(21, 112).addBox(-5.0F, -6.0F, -5.0F, 11.0F, 8.0F, 8.0F, 0.0F, false);

		head = new AnimatedModelRenderer(this);
		head.setRotationPoint(0.0F, -4.0F, -1.0F);
		neck.addChild(head);

		bone = new AnimatedModelRenderer(this);
		bone.setRotationPoint(0.0F, 15.3781F, -2.4547F);
		head.addChild(bone);

		arm = new AnimatedModelRenderer(this);
		arm.setRotationPoint(-7.5F, -24.0F, -2.5F);
		bone.addChild(arm);
		setRotationAngle(arm, 0.0F, 0.0F, -0.6109F);
		arm.setTextureOffset(44, 20).addBox(-1.3061F, -1.777F, -1.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm2 = new AnimatedModelRenderer(this);
		arm2.setRotationPoint(-8.0301F, 9.9681F, 2.0F);
		arm.addChild(arm2);
		arm2.setTextureOffset(0, 7).addBox(7.724F, -10.7451F, -2.0551F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm2.setTextureOffset(0, 55).addBox(7.224F, -15.7451F, -2.5551F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm2.setTextureOffset(60, 41).addBox(6.724F, -15.4951F, -1.5551F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm3 = new AnimatedModelRenderer(this);
		arm3.setRotationPoint(8.043F, -15.5909F, -1.6777F);
		arm2.addChild(arm3);
		arm3.setTextureOffset(90, 0).addBox(-0.819F, -4.1542F, -0.8774F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm3.setTextureOffset(30, 55).addBox(-0.319F, -0.1542F, -0.3774F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm3.setTextureOffset(50, 94).addBox(-1.319F, -3.9042F, 0.1226F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm4 = new AnimatedModelRenderer(this);
		arm4.setRotationPoint(-0.0603F, -4.3653F, -0.0409F);
		arm3.addChild(arm4);
		arm4.setTextureOffset(65, 88).addBox(-0.7587F, -3.789F, -0.8366F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm4.setTextureOffset(55, 25).addBox(-0.2587F, 0.211F, -0.3366F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm5 = new AnimatedModelRenderer(this);
		arm5.setRotationPoint(0.2413F, -3.739F, 0.1634F);
		arm4.addChild(arm5);
		arm5.setTextureOffset(93, 71).addBox(-2.0F, -4.8F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(24, 97).addBox(-1.0F, -5.8F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(16, 93).addBox(-1.5F, -4.05F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm5.setTextureOffset(56, 21).addBox(-0.5F, -0.05F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm6 = new AnimatedModelRenderer(this);
		arm6.setRotationPoint(-0.5F, -24.0F, 5.5F);
		bone.addChild(arm6);
		setRotationAngle(arm6, -0.6981F, 0.0F, 0.5672F);
		arm6.setTextureOffset(35, 23).addBox(-1.6817F, -2.0044F, -1.3425F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm7 = new AnimatedModelRenderer(this);
		arm7.setRotationPoint(-0.4778F, -1.7405F, 0.6218F);
		arm6.addChild(arm7);
		arm7.setTextureOffset(0, 6).addBox(-0.2039F, 0.7361F, -0.9643F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm7.setTextureOffset(54, 0).addBox(-0.7039F, -4.2639F, -1.4643F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm7.setTextureOffset(60, 36).addBox(-1.2039F, -4.0139F, -0.4643F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm8 = new AnimatedModelRenderer(this);
		arm8.setRotationPoint(0.475F, -3.975F, -0.425F);
		arm7.addChild(arm8);
		arm8.setTextureOffset(57, 88).addBox(-1.1789F, -4.2889F, -1.0393F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm8.setTextureOffset(54, 16).addBox(-0.6789F, -0.2889F, -0.5393F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm8.setTextureOffset(44, 94).addBox(-1.6789F, -4.0389F, -0.0393F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm9 = new AnimatedModelRenderer(this);
		arm9.setRotationPoint(0.025F, -4.025F, -0.075F);
		arm8.addChild(arm9);
		arm9.setTextureOffset(49, 88).addBox(-1.2039F, -4.2639F, -0.9643F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm9.setTextureOffset(53, 20).addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm9.setTextureOffset(73, 92).addBox(-1.7039F, -4.0139F, 0.0357F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm10 = new AnimatedModelRenderer(this);
		arm10.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm9.addChild(arm10);
		arm10.setTextureOffset(8, 93).addBox(-2.2039F, -5.0139F, 0.0357F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(24, 97).addBox(-1.2039F, -6.0139F, 0.0357F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(0, 93).addBox(-1.7039F, -4.2639F, -0.4643F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm10.setTextureOffset(51, 26).addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm21 = new AnimatedModelRenderer(this);
		arm21.setRotationPoint(-0.5F, -25.0F, -2.5F);
		bone.addChild(arm21);
		setRotationAngle(arm21, -0.7854F, 0.0F, -0.5236F);
		arm21.setTextureOffset(0, 6).addBox(-1.3309F, -2.0216F, -1.3925F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm22 = new AnimatedModelRenderer(this);
		arm22.setRotationPoint(0.5018F, -1.6335F, -0.0588F);
		arm21.addChild(arm22);
		arm22.setTextureOffset(0, 0).addBox(-0.8327F, 0.6119F, -0.3336F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm22.setTextureOffset(0, 20).addBox(-1.3327F, -4.3881F, -0.8336F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm22.setTextureOffset(12, 12).addBox(-1.8327F, -4.1381F, 0.1664F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm23 = new AnimatedModelRenderer(this);
		arm23.setRotationPoint(-0.5F, -4.25F, 0.125F);
		arm22.addChild(arm23);
		arm23.setTextureOffset(48, 62).addBox(-0.8327F, -4.1381F, -0.9586F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm23.setTextureOffset(30, 26).addBox(-0.3327F, -0.1381F, -0.4586F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm23.setTextureOffset(66, 54).addBox(-1.3327F, -3.8881F, 0.0414F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm24 = new AnimatedModelRenderer(this);
		arm24.setRotationPoint(-0.0018F, -4.2075F, -0.0788F);
		arm23.addChild(arm24);
		arm24.setTextureOffset(24, 55).addBox(-0.8309F, -3.9306F, -0.8799F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm24.setTextureOffset(26, 26).addBox(-0.3309F, 0.0694F, -0.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm25 = new AnimatedModelRenderer(this);
		arm25.setRotationPoint(0.0F, -3.5F, 0.0F);
		arm24.addChild(arm25);
		arm25.setTextureOffset(90, 66).addBox(-1.8309F, -5.1806F, 0.1201F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(24, 97).addBox(-0.8309F, -6.1806F, 0.1201F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(90, 45).addBox(-1.3309F, -4.4306F, -0.3799F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm25.setTextureOffset(12, 10).addBox(-0.3309F, -0.4306F, -0.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm11 = new AnimatedModelRenderer(this);
		arm11.setRotationPoint(3.5F, -24.0F, -9.5F);
		bone.addChild(arm11);
		setRotationAngle(arm11, 1.1345F, 0.0F, -0.48F);
		arm11.setTextureOffset(26, 20).addBox(-1.8056F, -0.8487F, -1.8441F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm12 = new AnimatedModelRenderer(this);
		arm12.setRotationPoint(0.0738F, -0.0644F, -0.2133F);
		arm11.addChild(arm12);
		arm12.setTextureOffset(0, 2).addBox(-0.8794F, 0.2157F, -0.6307F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm12.setTextureOffset(34, 46).addBox(-1.3794F, -4.7843F, -1.1307F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm12.setTextureOffset(12, 5).addBox(-1.8794F, -4.5343F, -0.1307F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm13 = new AnimatedModelRenderer(this);
		arm13.setRotationPoint(-0.525F, -4.725F, -0.175F);
		arm12.addChild(arm13);
		arm13.setTextureOffset(20, 87).addBox(-0.8544F, -4.0593F, -0.9557F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm13.setTextureOffset(47, 26).addBox(-0.3544F, -0.0593F, -0.4557F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm13.setTextureOffset(73, 88).addBox(-1.3544F, -3.8093F, 0.0443F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm14 = new AnimatedModelRenderer(this);
		arm14.setRotationPoint(0.275F, -4.275F, -0.075F);
		arm13.addChild(arm14);
		arm14.setTextureOffset(12, 87).addBox(-1.1294F, -3.7843F, -0.8807F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm14.setTextureOffset(4, 44).addBox(-0.6294F, 0.2157F, -0.3807F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm15 = new AnimatedModelRenderer(this);
		arm15.setRotationPoint(-0.25F, -3.5F, 0.25F);
		arm14.addChild(arm15);
		arm15.setTextureOffset(36, 92).addBox(-1.8794F, -5.0343F, -0.1307F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(24, 97).addBox(-0.8794F, -6.0343F, -0.1307F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(91, 50).addBox(-1.3794F, -4.2843F, -0.6307F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm15.setTextureOffset(0, 44).addBox(-0.3794F, -0.2843F, -0.6307F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm16 = new AnimatedModelRenderer(this);
		arm16.setRotationPoint(6.5F, -23.0F, -2.5F);
		bone.addChild(arm16);
		setRotationAngle(arm16, 0.3054F, 0.0F, 0.5672F);
		arm16.setTextureOffset(0, 12).addBox(-1.6817F, -1.6382F, -0.9899F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm17 = new AnimatedModelRenderer(this);
		arm17.setRotationPoint(0.0222F, -0.6376F, 0.3569F);
		arm16.addChild(arm17);
		arm17.setTextureOffset(0, 1).addBox(-0.7039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm17.setTextureOffset(0, 37).addBox(-1.2039F, -5.0006F, -0.8468F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm17.setTextureOffset(12, 0).addBox(-1.7039F, -4.7506F, 0.1532F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm18 = new AnimatedModelRenderer(this);
		arm18.setRotationPoint(-0.525F, -4.975F, 0.075F);
		arm17.addChild(arm18);
		arm18.setTextureOffset(41, 86).addBox(-0.6789F, -4.0256F, -0.9218F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm18.setTextureOffset(41, 20).addBox(-0.1789F, -0.0256F, -0.4218F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm18.setTextureOffset(72, 54).addBox(-1.1789F, -3.7756F, 0.0782F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm19 = new AnimatedModelRenderer(this);
		arm19.setRotationPoint(0.025F, -4.025F, -0.075F);
		arm18.addChild(arm19);
		arm19.setTextureOffset(0, 71).addBox(-0.7039F, -4.0006F, -0.8468F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm19.setTextureOffset(38, 21).addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm20 = new AnimatedModelRenderer(this);
		arm20.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm19.addChild(arm20);
		arm20.setTextureOffset(91, 31).addBox(-1.7039F, -4.7506F, 0.1532F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(24, 97).addBox(-0.7039F, -5.7506F, 0.1532F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(28, 91).addBox(-1.2039F, -4.0006F, -0.3468F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm20.setTextureOffset(35, 20).addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bone2 = new AnimatedModelRenderer(this);
		bone2.setRotationPoint(0.0F, -1.25F, -0.25F);
		bone.addChild(bone2);
		bone2.setTextureOffset(0, 0).addBox(-9.0F, -15.3381F, -10.5551F, 18.0F, 2.0F, 18.0F, 0.0F, false);

		bone3 = new AnimatedModelRenderer(this);
		bone3.setRotationPoint(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.setTextureOffset(0, 71).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new AnimatedModelRenderer(this);
		bone4.setRotationPoint(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.setTextureOffset(34, 46).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new AnimatedModelRenderer(this);
		bone5.setRotationPoint(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.setTextureOffset(80, 80).addBox(-3.0F, -18.3381F, -4.5551F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new AnimatedModelRenderer(this);
		bone6.setRotationPoint(3.4F, -6.0F, 5.35F);
		bone.addChild(bone6);
		bone6.setTextureOffset(0, 38).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new AnimatedModelRenderer(this);
		bone7.setRotationPoint(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.setTextureOffset(24, 63).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new AnimatedModelRenderer(this);
		bone8.setRotationPoint(-2.6F, -6.5F, -1.75F);
		bone.addChild(bone8);
		bone8.setTextureOffset(26, 29).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new AnimatedModelRenderer(this);
		bone9.setRotationPoint(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.setTextureOffset(58, 38).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone10 = new AnimatedModelRenderer(this);
		bone10.setRotationPoint(-6.1F, -5.75F, 2.55F);
		bone.addChild(bone10);
		bone10.setTextureOffset(58, 58).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new AnimatedModelRenderer(this);
		bone11.setRotationPoint(3.9F, -5.0F, -4.35F);
		bone.addChild(bone11);
		bone11.setTextureOffset(0, 55).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new AnimatedModelRenderer(this);
		bone12.setRotationPoint(5.4F, -5.0F, 3.35F);
		bone.addChild(bone12);
		bone12.setTextureOffset(0, 20).addBox(-13.5F, -12.8381F, -12.5551F, 9.0F, 6.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-7.5F, -12.8381F, -9.5551F, 7.0F, 8.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-3.5F, -10.8381F, -11.5551F, 4.0F, 3.0F, 8.0F, 0.0F, false);
		bone12.setTextureOffset(0, 20).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new AnimatedModelRenderer(this);
		bone13.setRotationPoint(-0.1F, -4.35F, -6.15F);
		bone.addChild(bone13);
		bone13.setTextureOffset(54, 0).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new AnimatedModelRenderer(this);
		bone16.setRotationPoint(-5.85F, -4.5F, -5.4F);
		bone.addChild(bone16);
		bone16.setTextureOffset(48, 74).addBox(-4.0F, -17.3381F, -5.5551F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new AnimatedModelRenderer(this);
		bone15.setRotationPoint(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.setTextureOffset(52, 20).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new AnimatedModelRenderer(this);
		eye.setRotationPoint(1.0F, -26.3381F, -3.0551F);
		bone.addChild(eye);
		eye.setTextureOffset(0, 87).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new AnimatedModelRenderer(this);
		eye2.setRotationPoint(-7.5F, -24.3381F, 2.9449F);
		bone.addChild(eye2);
		eye2.setTextureOffset(85, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new AnimatedModelRenderer(this);
		eye3.setRotationPoint(-0.75F, -25.3381F, 2.9449F);
		bone.addChild(eye3);
		eye3.setTextureOffset(29, 85).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new AnimatedModelRenderer(this);
		eye7.setRotationPoint(-4.75F, -19.3381F, 8.9449F);
		bone.addChild(eye7);
		eye7.setTextureOffset(76, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new AnimatedModelRenderer(this);
		eye12.setRotationPoint(5.25F, -22.5881F, 7.9449F);
		bone.addChild(eye12);
		eye12.setTextureOffset(72, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new AnimatedModelRenderer(this);
		eye8.setRotationPoint(-0.75F, -17.3381F, -12.0551F);
		bone.addChild(eye8);
		eye8.setTextureOffset(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new AnimatedModelRenderer(this);
		eye9.setRotationPoint(8.0F, -17.3381F, -8.0551F);
		bone.addChild(eye9);
		eye9.setTextureOffset(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new AnimatedModelRenderer(this);
		eye13.setRotationPoint(10.0F, -19.3381F, 3.9449F);
		bone.addChild(eye13);
		eye13.setTextureOffset(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new AnimatedModelRenderer(this);
		eye10.setRotationPoint(-9.75F, -19.3381F, -7.0551F);
		bone.addChild(eye10);
		eye10.setTextureOffset(32, 79).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new AnimatedModelRenderer(this);
		eye11.setRotationPoint(-9.75F, -17.3381F, 6.9449F);
		bone.addChild(eye11);
		eye11.setTextureOffset(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new AnimatedModelRenderer(this);
		eye4.setRotationPoint(7.25F, -24.3381F, 2.9449F);
		bone.addChild(eye4);
		eye4.setTextureOffset(84, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new AnimatedModelRenderer(this);
		eye5.setRotationPoint(7.25F, -24.3381F, -6.0551F);
		bone.addChild(eye5);
		eye5.setTextureOffset(82, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new AnimatedModelRenderer(this);
		eye6.setRotationPoint(-3.75F, -24.3381F, -9.0551F);
		bone.addChild(eye6);
		eye6.setTextureOffset(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightArm = new AnimatedModelRenderer(this);
		rightArm.setRotationPoint(-8.0F, -8.75F, -2.5F);
		torso.addChild(rightArm);
		setRotationAngle(rightArm, 0.0F, -0.5236F, -1.0908F);
		rightArm.setTextureOffset(67, 118).addBox(-5.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		rightArm.setTextureOffset(67, 118).addBox(-11.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightBicep = new AnimatedModelRenderer(this);
		rightBicep.setRotationPoint(-11.0F, 0.1F, 0.0F);
		rightArm.addChild(rightBicep);
		setRotationAngle(rightBicep, 0.0F, 0.0F, -0.48F);
		rightBicep.setTextureOffset(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		rightBicep.setTextureOffset(1, 111).addBox(-3.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		rightFore = new AnimatedModelRenderer(this);
		rightFore.setRotationPoint(-6.0F, -0.1F, 0.0F);
		rightBicep.addChild(rightFore);
		setRotationAngle(rightFore, 0.0F, 0.7418F, 0.0F);
		rightFore.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightFore.setTextureOffset(59, 102).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightWrist = new AnimatedModelRenderer(this);
		rightWrist.setRotationPoint(-6.0F, 0.1F, 0.0F);
		rightFore.addChild(rightWrist);
		setRotationAngle(rightWrist, 0.0F, 0.2618F, -0.3054F);
		rightWrist.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightWrist.setTextureOffset(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightHand = new AnimatedModelRenderer(this);
		rightHand.setRotationPoint(-6.0F, 0.25F, 0.5F);
		rightWrist.addChild(rightHand);
		setRotationAngle(rightHand, 0.0F, -0.6981F, 0.0F);
		rightHand.setTextureOffset(63, 113).addBox(-6.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		leftArm = new AnimatedModelRenderer(this);
		leftArm.setRotationPoint(9.0F, -8.75F, -2.5F);
		torso.addChild(leftArm);
		setRotationAngle(leftArm, 0.0F, 0.5236F, 1.0908F);
		leftArm.setTextureOffset(67, 118).addBox(0.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		leftArm.setTextureOffset(58, 112).addBox(5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftBicep = new AnimatedModelRenderer(this);
		leftBicep.setRotationPoint(11.0F, 0.1F, 0.0F);
		leftArm.addChild(leftBicep);
		setRotationAngle(leftBicep, 0.0F, 0.0F, 0.48F);
		leftBicep.setTextureOffset(68, 120).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		leftBicep.setTextureOffset(1, 111).addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		leftFore = new AnimatedModelRenderer(this);
		leftFore.setRotationPoint(6.0F, -0.1F, 0.0F);
		leftBicep.addChild(leftFore);
		setRotationAngle(leftFore, 0.0F, -0.7418F, 0.0F);
		leftFore.setTextureOffset(1, 111).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		leftFore.setTextureOffset(67, 118).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftWrist = new AnimatedModelRenderer(this);
		leftWrist.setRotationPoint(6.0F, 0.1F, 0.0F);
		leftFore.addChild(leftWrist);
		setRotationAngle(leftWrist, 0.0F, -0.2618F, 0.3054F);
		leftWrist.setTextureOffset(1, 111).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		leftWrist.setTextureOffset(60, 109).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftHand = new AnimatedModelRenderer(this);
		leftHand.setRotationPoint(6.0F, 0.25F, 0.5F);
		leftWrist.addChild(leftHand);
		setRotationAngle(leftHand, 0.0F, 0.6981F, 0.0F);
		leftHand.setTextureOffset(75, 106).addBox(0.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		hips = new AnimatedModelRenderer(this);
		hips.setRotationPoint(0.5F, 2.5F, 7.0F);
		hips.setTextureOffset(59, 106).addBox(-3.5F, -6.5F, -3.0F, 7.0F, 3.0F, 6.0F, 0.0F, false);
		hips.setTextureOffset(59, 106).addBox(-2.5F, -5.5F, -2.6F, 5.0F, 3.0F, 6.0F, 0.0F, false);

		leftLeg = new AnimatedModelRenderer(this);
		leftLeg.setRotationPoint(4.5F, 1.5F, 0.0F);
		hips.addChild(leftLeg);
		leftLeg.setTextureOffset(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.setTextureOffset(48, 115).addBox(-2.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.setTextureOffset(48, 115).addBox(-2.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		leftLeg2 = new AnimatedModelRenderer(this);
		leftLeg2.setRotationPoint(0.0F, -3.0F, 0.0F);
		leftLeg.addChild(leftLeg2);
		setRotationAngle(leftLeg2, -0.5672F, 0.0F, 0.0F);
		leftLeg2.setTextureOffset(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg2.setTextureOffset(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg3 = new AnimatedModelRenderer(this);
		leftLeg3.setRotationPoint(2.0F, 7.0F, 0.0F);
		leftLeg2.addChild(leftLeg3);
		setRotationAngle(leftLeg3, 0.9599F, 0.0F, 0.0F);
		leftLeg3.setTextureOffset(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg3.setTextureOffset(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg3.setTextureOffset(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg4 = new AnimatedModelRenderer(this);
		leftLeg4.setRotationPoint(0.0F, 11.0F, 0.0F);
		leftLeg3.addChild(leftLeg4);
		setRotationAngle(leftLeg4, -0.3927F, 0.0F, 0.0F);
		leftLeg4.setTextureOffset(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		leftLeg4.setTextureOffset(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);

		rightLeg = new AnimatedModelRenderer(this);
		rightLeg.setRotationPoint(-1.5F, 1.5F, 0.0F);
		hips.addChild(rightLeg);
		rightLeg.setTextureOffset(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.setTextureOffset(48, 115).addBox(-4.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.setTextureOffset(48, 115).addBox(-4.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightLeg2 = new AnimatedModelRenderer(this);
		rightLeg2.setRotationPoint(-4.0F, -3.0F, 0.0F);
		rightLeg.addChild(rightLeg2);
		setRotationAngle(rightLeg2, -0.5672F, 0.0F, 0.0F);
		rightLeg2.setTextureOffset(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg2.setTextureOffset(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg3 = new AnimatedModelRenderer(this);
		rightLeg3.setRotationPoint(2.0F, 7.0F, 0.0F);
		rightLeg2.addChild(rightLeg3);
		setRotationAngle(rightLeg3, 0.9599F, 0.0F, 0.0F);
		rightLeg3.setTextureOffset(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg3.setTextureOffset(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		rightLeg3.setTextureOffset(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg4 = new AnimatedModelRenderer(this);
		rightLeg4.setRotationPoint(0.0F, 11.0F, 0.0F);
		rightLeg3.addChild(rightLeg4);
		setRotationAngle(rightLeg4, -0.3927F, 0.0F, 0.0F);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		rightLeg4.setTextureOffset(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);

		animator = ModelAnimator.create();
		headArray = new AnimatedModelRenderer[] { head };
		setDefaultPose();

	}

	@Override
	public void setLivingAnimations(EntityAbhorentThought entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.ticksExisted + partialTick;
		resetToDefaultPose();
		animator.update(entity, partialTick);
		// Body
		this.torso.rotateAngleX = (float) ((float) (Math.sin((frame) * 0.13f) * 0.0325) + Math.toRadians(15));

		this.rightLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.9662F) * 2.4F * limbSwingAmount / 3;
		this.rightLeg3.rotateAngleX = Math
				.abs(MathHelper.cos(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
		this.rightLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.9662F) * 2.4F * limbSwingAmount / 3;

		this.leftLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.9662F + (float) Math.PI) * 2.4F * limbSwingAmount / 3;
		this.leftLeg3.rotateAngleX = Math
				.abs(MathHelper.sin(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
		this.leftLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.9662F + (float) Math.PI) * 2.4F * limbSwingAmount / 3;

		// Arms
		this.leftArm.rotateAngleX = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.leftBicep.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.leftFore.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.leftWrist.rotateAngleX = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.leftHand.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.rightArm.rotateAngleX = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.rightBicep.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.rightFore.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.rightWrist.rotateAngleX = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.rightHand.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		// Tentacles
		this.arm.rotateAngleX = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.arm2.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm3.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm4.rotateAngleX = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm5.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm6.rotateAngleX = (float) ((float) Math.cos((frame) * 0.3f) * 0.05f - Math.toRadians(40));
		this.arm7.rotateAngleX = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm8.rotateAngleX = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm9.rotateAngleX = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm10.rotateAngleX = (float) Math.sin((frame) * 0.8f) * 0.35f;

		this.arm11.rotateAngleX = (float) ((float) Math.cos((frame) * 0.3f) * 0.05f + Math.toRadians(65));
		this.arm12.rotateAngleX = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm13.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm14.rotateAngleX = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm15.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm16.rotateAngleX = (float) Math.cos((frame) * 0.3f) * 0.05f;
		this.arm17.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm18.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm19.rotateAngleX = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm20.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm21.rotateAngleX = (float) Math.sin((frame) * 0.3f) * 0.05f;
		this.arm22.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm23.rotateAngleX = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm24.rotateAngleX = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm25.rotateAngleX = (float) Math.sin((frame) * 0.8f) * 0.35f;

		// Eyes
		this.eye.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye2.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye2.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye3.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye3.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye4.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye4.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye5.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye5.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye6.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye6.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye7.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye7.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye8.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye8.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye9.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye9.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye10.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye10.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye11.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye11.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye12.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye12.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye13.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye13.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
	}

	@Override
	public void setRotationAngles(EntityAbhorentThought entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
		hips.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(AnimatedModelRenderer AnimatedModelRenderer, float x, float y, float z) {
		AnimatedModelRenderer.rotateAngleX = x;
		AnimatedModelRenderer.rotateAngleY = y;
		AnimatedModelRenderer.rotateAngleZ = z;
	}
}