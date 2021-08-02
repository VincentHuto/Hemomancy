package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityAbhorentThought;
import com.hutoslib.client.model.AnimatedEntityModel;
import com.hutoslib.client.model.AnimatedModelRenderer;
import com.hutoslib.client.model.ModelAnimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.Mth;

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
		texWidth = 128;
		texHeight = 128;

		whole = new AnimatedModelRenderer(this);
		whole.setPos(0.0F, 16.0F, 8.0F);

		body = new AnimatedModelRenderer(this);
		body.setPos(0.0F, 0.0F, 0.0F);
		whole.addChild(body);

		abdomen = new AnimatedModelRenderer(this);
		abdomen.setPos(0.0F, -20.0F, -1.0F);
		body.addChild(abdomen);
		abdomen.texOffs(59, 106).addBox(-3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-2.0F, -5.0F, -3.0F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-2.0F, -5.0F, 2.0F, 5.0F, 3.0F, 2.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-2.0F, -9.0F, 2.0F, 5.0F, 3.0F, 2.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-4.0F, -10.0F, -3.0F, 9.0F, 5.0F, 6.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(5.0F, -10.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(3.0F, -5.0F, -3.0F, 1.0F, 3.0F, 6.0F, 0.0F, false);
		abdomen.texOffs(59, 106).addBox(-4.0F, -10.0F, -4.0F, 9.0F, 4.0F, 1.0F, 0.0F, false);

		torso = new AnimatedModelRenderer(this);
		torso.setPos(0.0F, -10.0F, 1.0F);
		abdomen.addChild(torso);
		setRotationAngle(torso, 0.2618F, 0.0F, 0.0F);
		torso.texOffs(44, 104).addBox(-6.0F, -2.0F, -5.0F, 13.0F, 2.0F, 6.0F, 0.0F, false);
		torso.texOffs(44, 104).addBox(-3.0F, -1.0F, -4.0F, 7.0F, 3.0F, 4.0F, 0.0F, false);
		torso.texOffs(21, 112).addBox(-7.0F, -6.0F, -6.0F, 15.0F, 5.0F, 8.0F, 0.0F, false);
		torso.texOffs(21, 112).addBox(-6.0F, -6.0F, 2.0F, 13.0F, 4.0F, 2.0F, 0.0F, false);
		torso.texOffs(1, 111).addBox(-9.0F, -11.0F, 3.0F, 19.0F, 5.0F, 1.0F, 0.0F, false);
		torso.texOffs(68, 106).addBox(-4.0F, -11.0F, 4.0F, 9.0F, 6.0F, 1.0F, 0.0F, false);
		torso.texOffs(1, 111).addBox(-8.0F, -12.0F, -8.0F, 17.0F, 6.0F, 11.0F, 0.0F, false);

		neck = new AnimatedModelRenderer(this);
		neck.setPos(0.0F, -12.0F, -2.0F);
		torso.addChild(neck);
		setRotationAngle(neck, 0.2618F, 0.0F, 0.0F);
		neck.texOffs(21, 112).addBox(6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.texOffs(21, 112).addBox(-4.0F, -5.0F, 3.0F, 9.0F, 7.0F, 1.0F, 0.0F, false);
		neck.texOffs(21, 112).addBox(-2.0F, -5.0F, 4.0F, 5.0F, 7.0F, 1.0F, 0.0F, false);
		neck.texOffs(21, 112).addBox(-6.0F, -5.0F, -5.0F, 1.0F, 7.0F, 7.0F, 0.0F, false);
		neck.texOffs(21, 112).addBox(-5.0F, -6.0F, -5.0F, 11.0F, 8.0F, 8.0F, 0.0F, false);

		head = new AnimatedModelRenderer(this);
		head.setPos(0.0F, -4.0F, -1.0F);
		neck.addChild(head);

		bone = new AnimatedModelRenderer(this);
		bone.setPos(0.0F, 15.3781F, -2.4547F);
		head.addChild(bone);

		arm = new AnimatedModelRenderer(this);
		arm.setPos(-7.5F, -24.0F, -2.5F);
		bone.addChild(arm);
		setRotationAngle(arm, 0.0F, 0.0F, -0.6109F);
		arm.texOffs(44, 20).addBox(-1.3061F, -1.777F, -1.0551F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm2 = new AnimatedModelRenderer(this);
		arm2.setPos(-8.0301F, 9.9681F, 2.0F);
		arm.addChild(arm2);
		arm2.texOffs(0, 7).addBox(7.724F, -10.7451F, -2.0551F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm2.texOffs(0, 55).addBox(7.224F, -15.7451F, -2.5551F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm2.texOffs(60, 41).addBox(6.724F, -15.4951F, -1.5551F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm3 = new AnimatedModelRenderer(this);
		arm3.setPos(8.043F, -15.5909F, -1.6777F);
		arm2.addChild(arm3);
		arm3.texOffs(90, 0).addBox(-0.819F, -4.1542F, -0.8774F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm3.texOffs(30, 55).addBox(-0.319F, -0.1542F, -0.3774F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm3.texOffs(50, 94).addBox(-1.319F, -3.9042F, 0.1226F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm4 = new AnimatedModelRenderer(this);
		arm4.setPos(-0.0603F, -4.3653F, -0.0409F);
		arm3.addChild(arm4);
		arm4.texOffs(65, 88).addBox(-0.7587F, -3.789F, -0.8366F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm4.texOffs(55, 25).addBox(-0.2587F, 0.211F, -0.3366F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm5 = new AnimatedModelRenderer(this);
		arm5.setPos(0.2413F, -3.739F, 0.1634F);
		arm4.addChild(arm5);
		arm5.texOffs(93, 71).addBox(-2.0F, -4.8F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm5.texOffs(24, 97).addBox(-1.0F, -5.8F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm5.texOffs(16, 93).addBox(-1.5F, -4.05F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm5.texOffs(56, 21).addBox(-0.5F, -0.05F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm6 = new AnimatedModelRenderer(this);
		arm6.setPos(-0.5F, -24.0F, 5.5F);
		bone.addChild(arm6);
		setRotationAngle(arm6, -0.6981F, 0.0F, 0.5672F);
		arm6.texOffs(35, 23).addBox(-1.6817F, -2.0044F, -1.3425F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm7 = new AnimatedModelRenderer(this);
		arm7.setPos(-0.4778F, -1.7405F, 0.6218F);
		arm6.addChild(arm7);
		arm7.texOffs(0, 6).addBox(-0.2039F, 0.7361F, -0.9643F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm7.texOffs(54, 0).addBox(-0.7039F, -4.2639F, -1.4643F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm7.texOffs(60, 36).addBox(-1.2039F, -4.0139F, -0.4643F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm8 = new AnimatedModelRenderer(this);
		arm8.setPos(0.475F, -3.975F, -0.425F);
		arm7.addChild(arm8);
		arm8.texOffs(57, 88).addBox(-1.1789F, -4.2889F, -1.0393F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm8.texOffs(54, 16).addBox(-0.6789F, -0.2889F, -0.5393F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm8.texOffs(44, 94).addBox(-1.6789F, -4.0389F, -0.0393F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm9 = new AnimatedModelRenderer(this);
		arm9.setPos(0.025F, -4.025F, -0.075F);
		arm8.addChild(arm9);
		arm9.texOffs(49, 88).addBox(-1.2039F, -4.2639F, -0.9643F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm9.texOffs(53, 20).addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm9.texOffs(73, 92).addBox(-1.7039F, -4.0139F, 0.0357F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm10 = new AnimatedModelRenderer(this);
		arm10.setPos(0.0F, -4.0F, 0.0F);
		arm9.addChild(arm10);
		arm10.texOffs(8, 93).addBox(-2.2039F, -5.0139F, 0.0357F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm10.texOffs(24, 97).addBox(-1.2039F, -6.0139F, 0.0357F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm10.texOffs(0, 93).addBox(-1.7039F, -4.2639F, -0.4643F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm10.texOffs(51, 26).addBox(-0.7039F, -0.2639F, -0.4643F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm21 = new AnimatedModelRenderer(this);
		arm21.setPos(-0.5F, -25.0F, -2.5F);
		bone.addChild(arm21);
		setRotationAngle(arm21, -0.7854F, 0.0F, -0.5236F);
		arm21.texOffs(0, 6).addBox(-1.3309F, -2.0216F, -1.3925F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm22 = new AnimatedModelRenderer(this);
		arm22.setPos(0.5018F, -1.6335F, -0.0588F);
		arm21.addChild(arm22);
		arm22.texOffs(0, 0).addBox(-0.8327F, 0.6119F, -0.3336F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm22.texOffs(0, 20).addBox(-1.3327F, -4.3881F, -0.8336F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm22.texOffs(12, 12).addBox(-1.8327F, -4.1381F, 0.1664F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm23 = new AnimatedModelRenderer(this);
		arm23.setPos(-0.5F, -4.25F, 0.125F);
		arm22.addChild(arm23);
		arm23.texOffs(48, 62).addBox(-0.8327F, -4.1381F, -0.9586F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm23.texOffs(30, 26).addBox(-0.3327F, -0.1381F, -0.4586F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm23.texOffs(66, 54).addBox(-1.3327F, -3.8881F, 0.0414F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm24 = new AnimatedModelRenderer(this);
		arm24.setPos(-0.0018F, -4.2075F, -0.0788F);
		arm23.addChild(arm24);
		arm24.texOffs(24, 55).addBox(-0.8309F, -3.9306F, -0.8799F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm24.texOffs(26, 26).addBox(-0.3309F, 0.0694F, -0.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm25 = new AnimatedModelRenderer(this);
		arm25.setPos(0.0F, -3.5F, 0.0F);
		arm24.addChild(arm25);
		arm25.texOffs(90, 66).addBox(-1.8309F, -5.1806F, 0.1201F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm25.texOffs(24, 97).addBox(-0.8309F, -6.1806F, 0.1201F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm25.texOffs(90, 45).addBox(-1.3309F, -4.4306F, -0.3799F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm25.texOffs(12, 10).addBox(-0.3309F, -0.4306F, -0.3799F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm11 = new AnimatedModelRenderer(this);
		arm11.setPos(3.5F, -24.0F, -9.5F);
		bone.addChild(arm11);
		setRotationAngle(arm11, 1.1345F, 0.0F, -0.48F);
		arm11.texOffs(26, 20).addBox(-1.8056F, -0.8487F, -1.8441F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm12 = new AnimatedModelRenderer(this);
		arm12.setPos(0.0738F, -0.0644F, -0.2133F);
		arm11.addChild(arm12);
		arm12.texOffs(0, 2).addBox(-0.8794F, 0.2157F, -0.6307F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm12.texOffs(34, 46).addBox(-1.3794F, -4.7843F, -1.1307F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm12.texOffs(12, 5).addBox(-1.8794F, -4.5343F, -0.1307F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm13 = new AnimatedModelRenderer(this);
		arm13.setPos(-0.525F, -4.725F, -0.175F);
		arm12.addChild(arm13);
		arm13.texOffs(20, 87).addBox(-0.8544F, -4.0593F, -0.9557F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm13.texOffs(47, 26).addBox(-0.3544F, -0.0593F, -0.4557F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm13.texOffs(73, 88).addBox(-1.3544F, -3.8093F, 0.0443F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm14 = new AnimatedModelRenderer(this);
		arm14.setPos(0.275F, -4.275F, -0.075F);
		arm13.addChild(arm14);
		arm14.texOffs(12, 87).addBox(-1.1294F, -3.7843F, -0.8807F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm14.texOffs(4, 44).addBox(-0.6294F, 0.2157F, -0.3807F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm15 = new AnimatedModelRenderer(this);
		arm15.setPos(-0.25F, -3.5F, 0.25F);
		arm14.addChild(arm15);
		arm15.texOffs(36, 92).addBox(-1.8794F, -5.0343F, -0.1307F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm15.texOffs(24, 97).addBox(-0.8794F, -6.0343F, -0.1307F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm15.texOffs(91, 50).addBox(-1.3794F, -4.2843F, -0.6307F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm15.texOffs(0, 44).addBox(-0.3794F, -0.2843F, -0.6307F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm16 = new AnimatedModelRenderer(this);
		arm16.setPos(6.5F, -23.0F, -2.5F);
		bone.addChild(arm16);
		setRotationAngle(arm16, 0.3054F, 0.0F, 0.5672F);
		arm16.texOffs(0, 12).addBox(-1.6817F, -1.6382F, -0.9899F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm17 = new AnimatedModelRenderer(this);
		arm17.setPos(0.0222F, -0.6376F, 0.3569F);
		arm16.addChild(arm17);
		arm17.texOffs(0, 1).addBox(-0.7039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm17.texOffs(0, 37).addBox(-1.2039F, -5.0006F, -0.8468F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm17.texOffs(12, 0).addBox(-1.7039F, -4.7506F, 0.1532F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm18 = new AnimatedModelRenderer(this);
		arm18.setPos(-0.525F, -4.975F, 0.075F);
		arm17.addChild(arm18);
		arm18.texOffs(41, 86).addBox(-0.6789F, -4.0256F, -0.9218F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm18.texOffs(41, 20).addBox(-0.1789F, -0.0256F, -0.4218F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm18.texOffs(72, 54).addBox(-1.1789F, -3.7756F, 0.0782F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm19 = new AnimatedModelRenderer(this);
		arm19.setPos(0.025F, -4.025F, -0.075F);
		arm18.addChild(arm19);
		arm19.texOffs(0, 71).addBox(-0.7039F, -4.0006F, -0.8468F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm19.texOffs(38, 21).addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm20 = new AnimatedModelRenderer(this);
		arm20.setPos(0.0F, -4.0F, 0.0F);
		arm19.addChild(arm20);
		arm20.texOffs(91, 31).addBox(-1.7039F, -4.7506F, 0.1532F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm20.texOffs(24, 97).addBox(-0.7039F, -5.7506F, 0.1532F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm20.texOffs(28, 91).addBox(-1.2039F, -4.0006F, -0.3468F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm20.texOffs(35, 20).addBox(-0.2039F, -0.0006F, -0.3468F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bone2 = new AnimatedModelRenderer(this);
		bone2.setPos(0.0F, -1.25F, -0.25F);
		bone.addChild(bone2);
		bone2.texOffs(0, 0).addBox(-9.0F, -15.3381F, -10.5551F, 18.0F, 2.0F, 18.0F, 0.0F, false);

		bone3 = new AnimatedModelRenderer(this);
		bone3.setPos(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.texOffs(0, 71).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new AnimatedModelRenderer(this);
		bone4.setPos(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.texOffs(34, 46).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new AnimatedModelRenderer(this);
		bone5.setPos(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.texOffs(80, 80).addBox(-3.0F, -18.3381F, -4.5551F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new AnimatedModelRenderer(this);
		bone6.setPos(3.4F, -6.0F, 5.35F);
		bone.addChild(bone6);
		bone6.texOffs(0, 38).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new AnimatedModelRenderer(this);
		bone7.setPos(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.texOffs(24, 63).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new AnimatedModelRenderer(this);
		bone8.setPos(-2.6F, -6.5F, -1.75F);
		bone.addChild(bone8);
		bone8.texOffs(26, 29).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new AnimatedModelRenderer(this);
		bone9.setPos(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.texOffs(58, 38).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone10 = new AnimatedModelRenderer(this);
		bone10.setPos(-6.1F, -5.75F, 2.55F);
		bone.addChild(bone10);
		bone10.texOffs(58, 58).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new AnimatedModelRenderer(this);
		bone11.setPos(3.9F, -5.0F, -4.35F);
		bone.addChild(bone11);
		bone11.texOffs(0, 55).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new AnimatedModelRenderer(this);
		bone12.setPos(5.4F, -5.0F, 3.35F);
		bone.addChild(bone12);
		bone12.texOffs(0, 20).addBox(-13.5F, -12.8381F, -12.5551F, 9.0F, 6.0F, 8.0F, 0.0F, false);
		bone12.texOffs(0, 20).addBox(-7.5F, -12.8381F, -9.5551F, 7.0F, 8.0F, 8.0F, 0.0F, false);
		bone12.texOffs(0, 20).addBox(-3.5F, -10.8381F, -11.5551F, 4.0F, 3.0F, 8.0F, 0.0F, false);
		bone12.texOffs(0, 20).addBox(-4.5F, -18.8381F, -5.5551F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new AnimatedModelRenderer(this);
		bone13.setPos(-0.1F, -4.35F, -6.15F);
		bone.addChild(bone13);
		bone13.texOffs(54, 0).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new AnimatedModelRenderer(this);
		bone16.setPos(-5.85F, -4.5F, -5.4F);
		bone.addChild(bone16);
		bone16.texOffs(48, 74).addBox(-4.0F, -17.3381F, -5.5551F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new AnimatedModelRenderer(this);
		bone15.setPos(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.texOffs(52, 20).addBox(-4.0F, -18.3381F, -5.5551F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new AnimatedModelRenderer(this);
		eye.setPos(1.0F, -26.3381F, -3.0551F);
		bone.addChild(eye);
		eye.texOffs(0, 87).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new AnimatedModelRenderer(this);
		eye2.setPos(-7.5F, -24.3381F, 2.9449F);
		bone.addChild(eye2);
		eye2.texOffs(85, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new AnimatedModelRenderer(this);
		eye3.setPos(-0.75F, -25.3381F, 2.9449F);
		bone.addChild(eye3);
		eye3.texOffs(29, 85).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new AnimatedModelRenderer(this);
		eye7.setPos(-4.75F, -19.3381F, 8.9449F);
		bone.addChild(eye7);
		eye7.texOffs(76, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new AnimatedModelRenderer(this);
		eye12.setPos(5.25F, -22.5881F, 7.9449F);
		bone.addChild(eye12);
		eye12.texOffs(72, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new AnimatedModelRenderer(this);
		eye8.setPos(-0.75F, -17.3381F, -12.0551F);
		bone.addChild(eye8);
		eye8.texOffs(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new AnimatedModelRenderer(this);
		eye9.setPos(8.0F, -17.3381F, -8.0551F);
		bone.addChild(eye9);
		eye9.texOffs(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new AnimatedModelRenderer(this);
		eye13.setPos(10.0F, -19.3381F, 3.9449F);
		bone.addChild(eye13);
		eye13.texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new AnimatedModelRenderer(this);
		eye10.setPos(-9.75F, -19.3381F, -7.0551F);
		bone.addChild(eye10);
		eye10.texOffs(32, 79).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new AnimatedModelRenderer(this);
		eye11.setPos(-9.75F, -17.3381F, 6.9449F);
		bone.addChild(eye11);
		eye11.texOffs(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new AnimatedModelRenderer(this);
		eye4.setPos(7.25F, -24.3381F, 2.9449F);
		bone.addChild(eye4);
		eye4.texOffs(84, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new AnimatedModelRenderer(this);
		eye5.setPos(7.25F, -24.3381F, -6.0551F);
		bone.addChild(eye5);
		eye5.texOffs(82, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new AnimatedModelRenderer(this);
		eye6.setPos(-3.75F, -24.3381F, -9.0551F);
		bone.addChild(eye6);
		eye6.texOffs(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightArm = new AnimatedModelRenderer(this);
		rightArm.setPos(-8.0F, -8.75F, -2.5F);
		torso.addChild(rightArm);
		setRotationAngle(rightArm, 0.0F, -0.5236F, -1.0908F);
		rightArm.texOffs(67, 118).addBox(-5.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		rightArm.texOffs(67, 118).addBox(-11.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightBicep = new AnimatedModelRenderer(this);
		rightBicep.setPos(-11.0F, 0.1F, 0.0F);
		rightArm.addChild(rightBicep);
		setRotationAngle(rightBicep, 0.0F, 0.0F, -0.48F);
		rightBicep.texOffs(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		rightBicep.texOffs(1, 111).addBox(-3.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		rightFore = new AnimatedModelRenderer(this);
		rightFore.setPos(-6.0F, -0.1F, 0.0F);
		rightBicep.addChild(rightFore);
		setRotationAngle(rightFore, 0.0F, 0.7418F, 0.0F);
		rightFore.texOffs(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightFore.texOffs(59, 102).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightWrist = new AnimatedModelRenderer(this);
		rightWrist.setPos(-6.0F, 0.1F, 0.0F);
		rightFore.addChild(rightWrist);
		setRotationAngle(rightWrist, 0.0F, 0.2618F, -0.3054F);
		rightWrist.texOffs(1, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		rightWrist.texOffs(55, 108).addBox(-6.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		rightHand = new AnimatedModelRenderer(this);
		rightHand.setPos(-6.0F, 0.25F, 0.5F);
		rightWrist.addChild(rightHand);
		setRotationAngle(rightHand, 0.0F, -0.6981F, 0.0F);
		rightHand.texOffs(63, 113).addBox(-6.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		leftArm = new AnimatedModelRenderer(this);
		leftArm.setPos(9.0F, -8.75F, -2.5F);
		torso.addChild(leftArm);
		setRotationAngle(leftArm, 0.0F, 0.5236F, 1.0908F);
		leftArm.texOffs(67, 118).addBox(0.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
		leftArm.texOffs(58, 112).addBox(5.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftBicep = new AnimatedModelRenderer(this);
		leftBicep.setPos(11.0F, 0.1F, 0.0F);
		leftArm.addChild(leftBicep);
		setRotationAngle(leftBicep, 0.0F, 0.0F, 0.48F);
		leftBicep.texOffs(68, 120).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
		leftBicep.texOffs(1, 111).addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 0.0F, false);

		leftFore = new AnimatedModelRenderer(this);
		leftFore.setPos(6.0F, -0.1F, 0.0F);
		leftBicep.addChild(leftFore);
		setRotationAngle(leftFore, 0.0F, -0.7418F, 0.0F);
		leftFore.texOffs(1, 111).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		leftFore.texOffs(67, 118).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftWrist = new AnimatedModelRenderer(this);
		leftWrist.setPos(6.0F, 0.1F, 0.0F);
		leftFore.addChild(leftWrist);
		setRotationAngle(leftWrist, 0.0F, -0.2618F, 0.3054F);
		leftWrist.texOffs(1, 111).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		leftWrist.texOffs(60, 109).addBox(0.0F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);

		leftHand = new AnimatedModelRenderer(this);
		leftHand.setPos(6.0F, 0.25F, 0.5F);
		leftWrist.addChild(leftHand);
		setRotationAngle(leftHand, 0.0F, 0.6981F, 0.0F);
		leftHand.texOffs(75, 106).addBox(0.0F, -1.25F, -1.5F, 6.0F, 2.0F, 2.0F, 0.0F, false);

		hips = new AnimatedModelRenderer(this);
		hips.setPos(0.5F, 2.5F, 7.0F);
		hips.texOffs(59, 106).addBox(-3.5F, -6.5F, -3.0F, 7.0F, 3.0F, 6.0F, 0.0F, false);
		hips.texOffs(59, 106).addBox(-2.5F, -5.5F, -2.6F, 5.0F, 3.0F, 6.0F, 0.0F, false);

		leftLeg = new AnimatedModelRenderer(this);
		leftLeg.setPos(4.5F, 1.5F, 0.0F);
		hips.addChild(leftLeg);
		leftLeg.texOffs(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.texOffs(48, 115).addBox(-2.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg.texOffs(48, 115).addBox(-2.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		leftLeg2 = new AnimatedModelRenderer(this);
		leftLeg2.setPos(0.0F, -3.0F, 0.0F);
		leftLeg.addChild(leftLeg2);
		setRotationAngle(leftLeg2, -0.5672F, 0.0F, 0.0F);
		leftLeg2.texOffs(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg2.texOffs(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg3 = new AnimatedModelRenderer(this);
		leftLeg3.setPos(2.0F, 7.0F, 0.0F);
		leftLeg2.addChild(leftLeg3);
		setRotationAngle(leftLeg3, 0.9599F, 0.0F, 0.0F);
		leftLeg3.texOffs(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		leftLeg3.texOffs(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg3.texOffs(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		leftLeg4 = new AnimatedModelRenderer(this);
		leftLeg4.setPos(0.0F, 11.0F, 0.0F);
		leftLeg3.addChild(leftLeg4);
		setRotationAngle(leftLeg4, -0.3927F, 0.0F, 0.0F);
		leftLeg4.texOffs(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		leftLeg4.texOffs(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		leftLeg4.texOffs(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		leftLeg4.texOffs(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		leftLeg4.texOffs(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);

		rightLeg = new AnimatedModelRenderer(this);
		rightLeg.setPos(-1.5F, 1.5F, 0.0F);
		hips.addChild(rightLeg);
		rightLeg.texOffs(48, 115).addBox(-3.0F, -6.0F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.texOffs(48, 115).addBox(-4.0F, -5.0F, -1.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg.texOffs(48, 115).addBox(-4.0F, -7.9F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		rightLeg2 = new AnimatedModelRenderer(this);
		rightLeg2.setPos(-4.0F, -3.0F, 0.0F);
		rightLeg.addChild(rightLeg2);
		setRotationAngle(rightLeg2, -0.5672F, 0.0F, 0.0F);
		rightLeg2.texOffs(62, 106).addBox(-1.0F, -0.3132F, -0.9254F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg2.texOffs(62, 106).addBox(0.0F, 0.6868F, 1.0746F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg3 = new AnimatedModelRenderer(this);
		rightLeg3.setPos(2.0F, 7.0F, 0.0F);
		rightLeg2.addChild(rightLeg3);
		setRotationAngle(rightLeg3, 0.9599F, 0.0F, 0.0F);
		rightLeg3.texOffs(62, 106).addBox(-3.0F, 1.8478F, -2.7654F, 3.0F, 9.0F, 3.0F, 0.0F, false);
		rightLeg3.texOffs(62, 106).addBox(-2.0F, 0.8478F, -1.7654F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		rightLeg3.texOffs(62, 106).addBox(-2.0F, 2.8478F, -4.7654F, 1.0F, 9.0F, 3.0F, 0.0F, false);

		rightLeg4 = new AnimatedModelRenderer(this);
		rightLeg4.setPos(0.0F, 11.0F, 0.0F);
		rightLeg3.addChild(rightLeg4);
		setRotationAngle(rightLeg4, -0.3927F, 0.0F, 0.0F);
		rightLeg4.texOffs(62, 106).addBox(-2.5F, 5.0F, -7.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);
		rightLeg4.texOffs(62, 106).addBox(-3.0F, 4.0F, -4.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		rightLeg4.texOffs(62, 106).addBox(-2.0F, 6.0F, -10.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		rightLeg4.texOffs(62, 106).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		rightLeg4.texOffs(62, 106).addBox(-2.0F, -1.0F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, false);

		animator = ModelAnimator.create();
		headArray = new AnimatedModelRenderer[] { head };
		setDefaultPose();

	}

	@Override
	public void prepareMobModel(EntityAbhorentThought entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.tickCount + partialTick;
		resetToDefaultPose();
		animator.update(entity, partialTick);
		// Body
		this.torso.xRot = (float) ((float) (Math.sin((frame) * 0.13f) * 0.0325) + Math.toRadians(15));

		this.rightLeg2.xRot = Mth.cos(limbSwing * 0.9662F) * 2.4F * limbSwingAmount / 3;
		this.rightLeg3.xRot = Math.abs(Mth.cos(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
		this.rightLeg4.xRot = Mth.cos(limbSwing * 0.9662F) * 2.4F * limbSwingAmount / 3;

		this.leftLeg2.xRot = Mth.cos(limbSwing * 0.9662F + (float) Math.PI) * 2.4F * limbSwingAmount / 3;
		this.leftLeg3.xRot = Math.abs(Mth.sin(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
		this.leftLeg4.xRot = Mth.cos(limbSwing * 0.9662F + (float) Math.PI) * 2.4F * limbSwingAmount / 3;

		// Arms
		this.leftArm.xRot = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.leftBicep.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.leftFore.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.leftWrist.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.leftHand.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.rightArm.xRot = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.rightBicep.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.rightFore.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.rightWrist.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.rightHand.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		// Tentacles
		this.arm.xRot = (float) ((float) Math.sin((frame) * 0.3f) * 0.05f - Math.toRadians(25));
		this.arm2.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm3.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm4.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm5.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm6.xRot = (float) ((float) Math.cos((frame) * 0.3f) * 0.05f - Math.toRadians(40));
		this.arm7.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm8.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm9.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm10.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f;

		this.arm11.xRot = (float) ((float) Math.cos((frame) * 0.3f) * 0.05f + Math.toRadians(65));
		this.arm12.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm13.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm14.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm15.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm16.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f;
		this.arm17.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm18.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm19.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm20.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm21.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f;
		this.arm22.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm23.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm24.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm25.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f;

		// Eyes
		this.eye.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye2.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye2.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye3.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye3.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye4.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye4.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye5.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye5.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye6.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye6.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye7.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye7.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye8.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye8.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye9.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye9.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye10.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye10.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye11.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye11.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye12.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye12.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye13.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye13.yRot = (float) (Math.cos((frame)) * 0.0325);
	}

	@Override
	public void setupAnim(EntityAbhorentThought entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
		hips.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(AnimatedModelRenderer AnimatedModelRenderer, float x, float y, float z) {
		AnimatedModelRenderer.xRot = x;
		AnimatedModelRenderer.yRot = y;
		AnimatedModelRenderer.zRot = z;
	}
}