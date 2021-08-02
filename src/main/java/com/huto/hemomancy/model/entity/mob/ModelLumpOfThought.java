package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityLumpOfThought;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelLumpOfThought extends EntityModel<EntityLumpOfThought> {
	private final ModelPart bone;
	private final ModelPart arm;
	private final ModelPart arm2;
	private final ModelPart arm3;
	private final ModelPart arm4;
	private final ModelPart arm5;
	private final ModelPart arm6;
	private final ModelPart arm7;
	private final ModelPart arm8;
	private final ModelPart arm9;
	private final ModelPart arm10;
	private final ModelPart arm21;
	private final ModelPart arm22;
	private final ModelPart arm23;
	private final ModelPart arm24;
	private final ModelPart arm25;
	private final ModelPart arm11;
	private final ModelPart arm12;
	private final ModelPart arm13;
	private final ModelPart arm14;
	private final ModelPart arm15;
	private final ModelPart arm16;
	private final ModelPart arm17;
	private final ModelPart arm18;
	private final ModelPart arm19;
	private final ModelPart arm20;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart bone9;
	private final ModelPart bone10;
	private final ModelPart bone11;
	private final ModelPart bone12;
	private final ModelPart bone13;
	private final ModelPart bone16;
	private final ModelPart bone15;
	private final ModelPart eye;
	private final ModelPart eye2;
	private final ModelPart eye3;
	private final ModelPart eye7;
	private final ModelPart eye12;
	private final ModelPart eye8;
	private final ModelPart eye9;
	private final ModelPart eye13;
	private final ModelPart eye10;
	private final ModelPart eye11;
	private final ModelPart eye4;
	private final ModelPart eye5;
	private final ModelPart eye6;

	public ModelLumpOfThought() {
		texWidth = 128;
		texHeight = 128;

		bone = new ModelPart(this);
		bone.setPos(0.0F, 24.0F, 0.0F);

		arm = new ModelPart(this);
		arm.setPos(-7.5F, -10.0F, -0.5F);
		bone.addChild(arm);
		setRotationAngle(arm, 0.0F, 0.0F, -0.6109F);
		arm.texOffs(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm2 = new ModelPart(this);
		arm2.setPos(0.0F, -1.5F, 0.0F);
		arm.addChild(arm2);
		arm2.texOffs(0, 7).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm2.texOffs(0, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm2.texOffs(60, 41).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm3 = new ModelPart(this);
		arm3.setPos(-0.025F, -3.975F, 0.575F);
		arm2.addChild(arm3);
		arm3.texOffs(90, 0).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm3.texOffs(30, 55).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm3.texOffs(50, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm4 = new ModelPart(this);
		arm4.setPos(0.025F, -4.025F, -0.575F);
		arm3.addChild(arm4);
		arm4.texOffs(65, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm4.texOffs(55, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm5 = new ModelPart(this);
		arm5.setPos(0.0F, -4.0F, 0.0F);
		arm4.addChild(arm5);
		arm5.texOffs(93, 71).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm5.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm5.texOffs(16, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm5.texOffs(56, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm6 = new ModelPart(this);
		arm6.setPos(-0.5F, -10.0F, 7.5F);
		bone.addChild(arm6);
		setRotationAngle(arm6, -0.6981F, 0.0F, 0.5672F);
		arm6.texOffs(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm7 = new ModelPart(this);
		arm7.setPos(0.0F, -1.5F, 0.0F);
		arm6.addChild(arm7);
		arm7.texOffs(0, 6).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm7.texOffs(54, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm7.texOffs(60, 36).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm8 = new ModelPart(this);
		arm8.setPos(-0.025F, -3.975F, 0.575F);
		arm7.addChild(arm8);
		arm8.texOffs(57, 88).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm8.texOffs(54, 16).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm8.texOffs(44, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm9 = new ModelPart(this);
		arm9.setPos(0.025F, -4.025F, -0.575F);
		arm8.addChild(arm9);
		arm9.texOffs(49, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm9.texOffs(53, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm9.texOffs(73, 92).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm10 = new ModelPart(this);
		arm10.setPos(0.0F, -4.0F, 0.0F);
		arm9.addChild(arm10);
		arm10.texOffs(8, 93).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm10.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm10.texOffs(0, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm10.texOffs(51, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm21 = new ModelPart(this);
		arm21.setPos(-0.5F, -11.0F, -0.5F);
		bone.addChild(arm21);
		setRotationAngle(arm21, -0.7854F, 0.0F, -0.5236F);
		arm21.texOffs(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm22 = new ModelPart(this);
		arm22.setPos(0.0F, -1.5F, 0.0F);
		arm21.addChild(arm22);
		arm22.texOffs(0, 0).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm22.texOffs(0, 20).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm22.texOffs(12, 12).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm23 = new ModelPart(this);
		arm23.setPos(-0.025F, -3.975F, 0.575F);
		arm22.addChild(arm23);
		arm23.texOffs(48, 62).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm23.texOffs(30, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm23.texOffs(66, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm24 = new ModelPart(this);
		arm24.setPos(0.025F, -4.025F, -0.575F);
		arm23.addChild(arm24);
		arm24.texOffs(24, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm24.texOffs(26, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm25 = new ModelPart(this);
		arm25.setPos(0.0F, -4.0F, 0.0F);
		arm24.addChild(arm25);
		arm25.texOffs(90, 66).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm25.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm25.texOffs(90, 45).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm25.texOffs(12, 10).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm11 = new ModelPart(this);
		arm11.setPos(3.5F, -9.0F, -7.5F);
		bone.addChild(arm11);
		setRotationAngle(arm11, 1.1345F, 0.0F, -0.48F);
		arm11.texOffs(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm12 = new ModelPart(this);
		arm12.setPos(0.0F, -1.5F, 0.0F);
		arm11.addChild(arm12);
		arm12.texOffs(0, 2).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm12.texOffs(34, 46).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm12.texOffs(12, 5).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm13 = new ModelPart(this);
		arm13.setPos(-0.025F, -3.975F, 0.575F);
		arm12.addChild(arm13);
		arm13.texOffs(20, 87).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm13.texOffs(47, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm13.texOffs(73, 88).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm14 = new ModelPart(this);
		arm14.setPos(0.025F, -4.025F, -0.575F);
		arm13.addChild(arm14);
		arm14.texOffs(12, 87).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm14.texOffs(4, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm15 = new ModelPart(this);
		arm15.setPos(0.0F, -4.0F, 0.0F);
		arm14.addChild(arm15);
		arm15.texOffs(36, 92).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm15.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm15.texOffs(91, 50).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm15.texOffs(0, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm16 = new ModelPart(this);
		arm16.setPos(6.5F, -9.0F, -0.5F);
		bone.addChild(arm16);
		setRotationAngle(arm16, 0.3054F, 0.0F, 0.5672F);
		arm16.texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm17 = new ModelPart(this);
		arm17.setPos(0.0F, -1.5F, 0.0F);
		arm16.addChild(arm17);
		arm17.texOffs(0, 1).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm17.texOffs(0, 37).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm17.texOffs(12, 0).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm18 = new ModelPart(this);
		arm18.setPos(-0.025F, -3.975F, 0.575F);
		arm17.addChild(arm18);
		arm18.texOffs(41, 86).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm18.texOffs(41, 20).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm18.texOffs(72, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm19 = new ModelPart(this);
		arm19.setPos(0.025F, -4.025F, -0.575F);
		arm18.addChild(arm19);
		arm19.texOffs(0, 71).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm19.texOffs(38, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm20 = new ModelPart(this);
		arm20.setPos(0.0F, -4.0F, 0.0F);
		arm19.addChild(arm20);
		arm20.texOffs(91, 31).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm20.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm20.texOffs(28, 91).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm20.texOffs(35, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bone2 = new ModelPart(this);
		bone2.setPos(0.0F, -1.25F, -0.25F);
		bone.addChild(bone2);
		bone2.texOffs(0, 0).addBox(-9.0F, -1.0F, -9.0F, 18.0F, 2.0F, 18.0F, 0.0F, false);

		bone3 = new ModelPart(this);
		bone3.setPos(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.texOffs(0, 71).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new ModelPart(this);
		bone4.setPos(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.texOffs(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new ModelPart(this);
		bone5.setPos(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.texOffs(80, 80).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new ModelPart(this);
		bone6.setPos(3.4F, -6.0F, 5.35F);
		bone.addChild(bone6);
		bone6.texOffs(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new ModelPart(this);
		bone7.setPos(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.texOffs(24, 63).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new ModelPart(this);
		bone8.setPos(-2.6F, -6.5F, -1.75F);
		bone.addChild(bone8);
		bone8.texOffs(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new ModelPart(this);
		bone9.setPos(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.texOffs(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone10 = new ModelPart(this);
		bone10.setPos(-6.1F, -5.75F, 2.55F);
		bone.addChild(bone10);
		bone10.texOffs(58, 58).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new ModelPart(this);
		bone11.setPos(3.9F, -5.0F, -4.35F);
		bone.addChild(bone11);
		bone11.texOffs(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new ModelPart(this);
		bone12.setPos(5.4F, -5.0F, 3.35F);
		bone.addChild(bone12);
		bone12.texOffs(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new ModelPart(this);
		bone13.setPos(-0.1F, -4.35F, -6.15F);
		bone.addChild(bone13);
		bone13.texOffs(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new ModelPart(this);
		bone16.setPos(-5.85F, -4.5F, -5.4F);
		bone.addChild(bone16);
		bone16.texOffs(48, 74).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new ModelPart(this);
		bone15.setPos(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.texOffs(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new ModelPart(this);
		eye.setPos(-3.0F, -10.0F, -5.5F);
		bone.addChild(eye);
		eye.texOffs(0, 87).addBox(2.5F, -3.5F, 2.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new ModelPart(this);
		eye2.setPos(-7.5F, -10.0F, 4.5F);
		bone.addChild(eye2);
		eye2.texOffs(85, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new ModelPart(this);
		eye3.setPos(-0.75F, -11.0F, 4.5F);
		bone.addChild(eye3);
		eye3.texOffs(29, 85).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new ModelPart(this);
		eye7.setPos(2.25F, -7.0F, 9.5F);
		bone.addChild(eye7);
		eye7.texOffs(76, 22).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new ModelPart(this);
		eye12.setPos(12.25F, -10.25F, 8.5F);
		bone.addChild(eye12);
		eye12.texOffs(72, 74).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new ModelPart(this);
		eye8.setPos(-0.75F, -3.0F, -10.5F);
		bone.addChild(eye8);
		eye8.texOffs(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new ModelPart(this);
		eye9.setPos(8.0F, -3.0F, -6.5F);
		bone.addChild(eye9);
		eye9.texOffs(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new ModelPart(this);
		eye13.setPos(10.0F, -5.0F, 5.5F);
		bone.addChild(eye13);
		eye13.texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new ModelPart(this);
		eye10.setPos(-9.75F, -5.0F, -5.5F);
		bone.addChild(eye10);
		eye10.texOffs(32, 79).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new ModelPart(this);
		eye11.setPos(-9.75F, -3.0F, 8.5F);
		bone.addChild(eye11);
		eye11.texOffs(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new ModelPart(this);
		eye4.setPos(7.25F, -10.0F, 4.5F);
		bone.addChild(eye4);
		eye4.texOffs(84, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new ModelPart(this);
		eye5.setPos(7.25F, -10.0F, -4.5F);
		bone.addChild(eye5);
		eye5.texOffs(82, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new ModelPart(this);
		eye6.setPos(-3.75F, -10.0F, -7.5F);
		bone.addChild(eye6);
		eye6.texOffs(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void prepareMobModel(EntityLumpOfThought entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.tickCount + partialTick;

		// Tentacles
		this.arm.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f;
		this.arm2.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm3.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm4.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm5.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm6.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f;
		this.arm7.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm8.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm9.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm10.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f;

		this.arm11.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f;
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
	public void setupAnim(EntityLumpOfThought entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}