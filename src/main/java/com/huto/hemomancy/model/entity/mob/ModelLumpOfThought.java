package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityLumpOfThought;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

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
		textureWidth = 128;
		textureHeight = 128;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0F, 24.0F, 0.0F);

		arm = new ModelRenderer(this);
		arm.setRotationPoint(-7.5F, -10.0F, -0.5F);
		bone.addChild(arm);
		setRotationAngle(arm, 0.0F, 0.0F, -0.6109F);
		arm.setTextureOffset(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm2 = new ModelRenderer(this);
		arm2.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm.addChild(arm2);
		arm2.setTextureOffset(0, 7).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm2.setTextureOffset(0, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm2.setTextureOffset(60, 41).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm3 = new ModelRenderer(this);
		arm3.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm2.addChild(arm3);
		arm3.setTextureOffset(90, 0).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm3.setTextureOffset(30, 55).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm3.setTextureOffset(50, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm4 = new ModelRenderer(this);
		arm4.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm3.addChild(arm4);
		arm4.setTextureOffset(65, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm4.setTextureOffset(55, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm5 = new ModelRenderer(this);
		arm5.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm4.addChild(arm5);
		arm5.setTextureOffset(93, 71).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm5.setTextureOffset(16, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm5.setTextureOffset(56, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm6 = new ModelRenderer(this);
		arm6.setRotationPoint(-0.5F, -10.0F, 7.5F);
		bone.addChild(arm6);
		setRotationAngle(arm6, -0.6981F, 0.0F, 0.5672F);
		arm6.setTextureOffset(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm7 = new ModelRenderer(this);
		arm7.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm6.addChild(arm7);
		arm7.setTextureOffset(0, 6).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm7.setTextureOffset(54, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm7.setTextureOffset(60, 36).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm8 = new ModelRenderer(this);
		arm8.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm7.addChild(arm8);
		arm8.setTextureOffset(57, 88).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm8.setTextureOffset(54, 16).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm8.setTextureOffset(44, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm9 = new ModelRenderer(this);
		arm9.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm8.addChild(arm9);
		arm9.setTextureOffset(49, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm9.setTextureOffset(53, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm9.setTextureOffset(73, 92).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm10 = new ModelRenderer(this);
		arm10.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm9.addChild(arm10);
		arm10.setTextureOffset(8, 93).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm10.setTextureOffset(0, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm10.setTextureOffset(51, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm21 = new ModelRenderer(this);
		arm21.setRotationPoint(-0.5F, -11.0F, -0.5F);
		bone.addChild(arm21);
		setRotationAngle(arm21, -0.7854F, 0.0F, -0.5236F);
		arm21.setTextureOffset(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm22 = new ModelRenderer(this);
		arm22.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm21.addChild(arm22);
		arm22.setTextureOffset(0, 0).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm22.setTextureOffset(0, 20).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm22.setTextureOffset(12, 12).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm23 = new ModelRenderer(this);
		arm23.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm22.addChild(arm23);
		arm23.setTextureOffset(48, 62).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm23.setTextureOffset(30, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm23.setTextureOffset(66, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm24 = new ModelRenderer(this);
		arm24.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm23.addChild(arm24);
		arm24.setTextureOffset(24, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm24.setTextureOffset(26, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm25 = new ModelRenderer(this);
		arm25.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm24.addChild(arm25);
		arm25.setTextureOffset(90, 66).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm25.setTextureOffset(90, 45).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm25.setTextureOffset(12, 10).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm11 = new ModelRenderer(this);
		arm11.setRotationPoint(3.5F, -9.0F, -7.5F);
		bone.addChild(arm11);
		setRotationAngle(arm11, 1.1345F, 0.0F, -0.48F);
		arm11.setTextureOffset(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm12 = new ModelRenderer(this);
		arm12.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm11.addChild(arm12);
		arm12.setTextureOffset(0, 2).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm12.setTextureOffset(34, 46).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm12.setTextureOffset(12, 5).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm13 = new ModelRenderer(this);
		arm13.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm12.addChild(arm13);
		arm13.setTextureOffset(20, 87).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm13.setTextureOffset(47, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm13.setTextureOffset(73, 88).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm14 = new ModelRenderer(this);
		arm14.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm13.addChild(arm14);
		arm14.setTextureOffset(12, 87).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm14.setTextureOffset(4, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm15 = new ModelRenderer(this);
		arm15.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm14.addChild(arm15);
		arm15.setTextureOffset(36, 92).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm15.setTextureOffset(91, 50).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm15.setTextureOffset(0, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm16 = new ModelRenderer(this);
		arm16.setRotationPoint(6.5F, -9.0F, -0.5F);
		bone.addChild(arm16);
		setRotationAngle(arm16, 0.3054F, 0.0F, 0.5672F);
		arm16.setTextureOffset(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		arm17 = new ModelRenderer(this);
		arm17.setRotationPoint(0.0F, -1.5F, 0.0F);
		arm16.addChild(arm17);
		arm17.setTextureOffset(0, 1).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.0F, false);
		arm17.setTextureOffset(0, 37).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		arm17.setTextureOffset(12, 0).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, 0.0F, false);

		arm18 = new ModelRenderer(this);
		arm18.setRotationPoint(-0.025F, -3.975F, 0.575F);
		arm17.addChild(arm18);
		arm18.setTextureOffset(41, 86).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm18.setTextureOffset(41, 20).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		arm18.setTextureOffset(72, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, 0.0F, false);

		arm19 = new ModelRenderer(this);
		arm19.setRotationPoint(0.025F, -4.025F, -0.575F);
		arm18.addChild(arm19);
		arm19.setTextureOffset(0, 71).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		arm19.setTextureOffset(38, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		arm20 = new ModelRenderer(this);
		arm20.setRotationPoint(0.0F, -4.0F, 0.0F);
		arm19.addChild(arm20);
		arm20.setTextureOffset(91, 31).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);
		arm20.setTextureOffset(28, 91).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, 0.0F, false);
		arm20.setTextureOffset(35, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(0.0F, -1.25F, -0.25F);
		bone.addChild(bone2);
		bone2.setTextureOffset(0, 0).addBox(-9.0F, -1.0F, -9.0F, 18.0F, 2.0F, 18.0F, 0.0F, false);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.setTextureOffset(0, 71).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.setTextureOffset(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.setTextureOffset(80, 80).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(3.4F, -6.0F, 5.35F);
		bone.addChild(bone6);
		bone6.setTextureOffset(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.setTextureOffset(24, 63).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(-2.6F, -6.5F, -1.75F);
		bone.addChild(bone8);
		bone8.setTextureOffset(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.setTextureOffset(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(-6.1F, -5.75F, 2.55F);
		bone.addChild(bone10);
		bone10.setTextureOffset(58, 58).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(3.9F, -5.0F, -4.35F);
		bone.addChild(bone11);
		bone11.setTextureOffset(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(5.4F, -5.0F, 3.35F);
		bone.addChild(bone12);
		bone12.setTextureOffset(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new ModelRenderer(this);
		bone13.setRotationPoint(-0.1F, -4.35F, -6.15F);
		bone.addChild(bone13);
		bone13.setTextureOffset(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new ModelRenderer(this);
		bone16.setRotationPoint(-5.85F, -4.5F, -5.4F);
		bone.addChild(bone16);
		bone16.setTextureOffset(48, 74).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new ModelRenderer(this);
		bone15.setRotationPoint(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.setTextureOffset(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new ModelRenderer(this);
		eye.setRotationPoint(-3.0F, -10.0F, -5.5F);
		bone.addChild(eye);
		eye.setTextureOffset(0, 87).addBox(2.5F, -3.5F, 2.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new ModelRenderer(this);
		eye2.setRotationPoint(-7.5F, -10.0F, 4.5F);
		bone.addChild(eye2);
		eye2.setTextureOffset(85, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new ModelRenderer(this);
		eye3.setRotationPoint(-0.75F, -11.0F, 4.5F);
		bone.addChild(eye3);
		eye3.setTextureOffset(29, 85).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new ModelRenderer(this);
		eye7.setRotationPoint(2.25F, -7.0F, 9.5F);
		bone.addChild(eye7);
		eye7.setTextureOffset(76, 22).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new ModelRenderer(this);
		eye12.setRotationPoint(12.25F, -10.25F, 8.5F);
		bone.addChild(eye12);
		eye12.setTextureOffset(72, 74).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new ModelRenderer(this);
		eye8.setRotationPoint(-0.75F, -3.0F, -10.5F);
		bone.addChild(eye8);
		eye8.setTextureOffset(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new ModelRenderer(this);
		eye9.setRotationPoint(8.0F, -3.0F, -6.5F);
		bone.addChild(eye9);
		eye9.setTextureOffset(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new ModelRenderer(this);
		eye13.setRotationPoint(10.0F, -5.0F, 5.5F);
		bone.addChild(eye13);
		eye13.setTextureOffset(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new ModelRenderer(this);
		eye10.setRotationPoint(-9.75F, -5.0F, -5.5F);
		bone.addChild(eye10);
		eye10.setTextureOffset(32, 79).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new ModelRenderer(this);
		eye11.setRotationPoint(-9.75F, -3.0F, 8.5F);
		bone.addChild(eye11);
		eye11.setTextureOffset(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new ModelRenderer(this);
		eye4.setRotationPoint(7.25F, -10.0F, 4.5F);
		bone.addChild(eye4);
		eye4.setTextureOffset(84, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new ModelRenderer(this);
		eye5.setRotationPoint(7.25F, -10.0F, -4.5F);
		bone.addChild(eye5);
		eye5.setTextureOffset(82, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new ModelRenderer(this);
		eye6.setRotationPoint(-3.75F, -10.0F, -7.5F);
		bone.addChild(eye6);
		eye6.setTextureOffset(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setLivingAnimations(EntityLumpOfThought entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.ticksExisted + partialTick;

		// Tentacles
		this.arm.rotateAngleX = (float) Math.sin((frame) * 0.3f) * 0.05f;
		this.arm2.rotateAngleX = (float) Math.sin((frame) * 0.5f) * 0.1f;
		this.arm3.rotateAngleX = (float) Math.cos((frame) * 0.6f) * 0.15f;
		this.arm4.rotateAngleX = (float) Math.cos((frame) * 0.7f) * 0.25f;
		this.arm5.rotateAngleX = (float) Math.cos((frame) * 0.8f) * 0.35f;

		this.arm6.rotateAngleX = (float) Math.cos((frame) * 0.3f) * 0.05f;
		this.arm7.rotateAngleX = (float) Math.cos((frame) * 0.5f) * 0.1f;
		this.arm8.rotateAngleX = (float) Math.sin((frame) * 0.6f) * 0.15f;
		this.arm9.rotateAngleX = (float) Math.sin((frame) * 0.7f) * 0.25f;
		this.arm10.rotateAngleX = (float) Math.sin((frame) * 0.8f) * 0.35f;

		this.arm11.rotateAngleX = (float) Math.cos((frame) * 0.3f) * 0.05f;
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
	public void setRotationAngles(EntityLumpOfThought entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}