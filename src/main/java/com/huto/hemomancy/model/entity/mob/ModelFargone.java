package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityFargone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelFargone extends EntityModel<EntityFargone> {
	private final ModelPart Body;
	private final ModelPart Head;
	private final ModelPart head2;
	private final ModelPart lowerJaw;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart neck;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart RightArm;
	private final ModelPart rightFore;
	private final ModelPart RightLeg;
	private final ModelPart LeftLeg;
	private final ModelPart LeftArm;
	private final ModelPart leftFore;

	public ModelFargone() {
		texWidth = 64;
		texHeight = 64;

		Body = new ModelPart(this);
		Body.setPos(0.0F, 5.5627F, 0.9718F);
		setRotationAngle(Body, 0.2618F, 0.0F, 0.0F);
		Body.texOffs(22, 39).addBox(-3.5F, -6.3992F, 0.5124F, 7.0F, 11.0F, 2.0F, 0.0F, false);
		Body.texOffs(40, 39).addBox(-3.5F, -4.3381F, -2.7903F, 7.0F, 7.0F, 4.0F, 0.0F, false);
		Body.texOffs(24, 0).addBox(-2.5F, 2.6619F, -1.7903F, 5.0F, 4.0F, 3.0F, 0.0F, false);

		Head = new ModelPart(this);
		Head.setPos(0.0F, -5.597F, -1.7562F);
		Body.addChild(Head);
		setRotationAngle(Head, 0.3491F, 0.0F, 0.0F);

		head2 = new ModelPart(this);
		head2.setPos(0.0F, -0.7104F, 1.2249F);
		Head.addChild(head2);
		setRotationAngle(head2, -0.2618F, 0.0F, 0.0F);
		head2.texOffs(0, 0).addBox(-4.0F, -6.169F, -6.5408F, 8.0F, 7.0F, 8.0F, -1.0F, false);
		head2.texOffs(34, 21).addBox(0.0F, -6.7928F, -7.0782F, 5.0F, 5.0F, 6.0F, -1.0F, false);
		head2.texOffs(0, 33).addBox(-5.0F, -6.7928F, -7.0782F, 5.0F, 5.0F, 6.0F, -1.0F, false);

		lowerJaw = new ModelPart(this);
		lowerJaw.setPos(0.0F, 2.5312F, -0.9696F);
		head2.addChild(lowerJaw);
		setRotationAngle(lowerJaw, -0.0873F, 0.0F, 0.0F);
		lowerJaw.texOffs(29, 8).addBox(-3.0F, -3.6991F, -5.3355F, 6.0F, 3.0F, 7.0F, -1.0F, false);
		lowerJaw.texOffs(14, 15).addBox(-1.0F, -3.0613F, -13.6828F, 2.0F, 1.0F, 11.0F, -1.0F, false);
		lowerJaw.texOffs(0, 15).addBox(-0.5F, -2.5771F, -15.0622F, 1.0F, 1.0F, 12.0F, -1.0F, false);
		lowerJaw.texOffs(17, 27).addBox(-1.5F, -4.6068F, -11.2687F, 3.0F, 3.0F, 9.0F, -1.0F, false);

		bone = new ModelPart(this);
		bone.setPos(-1.2981F, -4.7485F, -6.1487F);
		lowerJaw.addChild(bone);
		setRotationAngle(bone, 0.0F, 0.0F, -0.6981F);
		bone.texOffs(29, 18).addBox(-1.5138F, -0.7585F, -0.6527F, 1.0F, 4.0F, 3.0F, -1.0F, false);

		bone2 = new ModelPart(this);
		bone2.setPos(1.7019F, -4.7485F, -6.1487F);
		lowerJaw.addChild(bone2);
		setRotationAngle(bone2, 0.0F, 0.0F, 0.6981F);
		bone2.texOffs(0, 0).addBox(0.0542F, -0.3728F, -0.6527F, 1.0F, 4.0F, 3.0F, -1.0F, false);

		neck = new ModelPart(this);
		neck.setPos(0.0F, 26.0F, -2.0F);
		Head.addChild(neck);
		neck.texOffs(41, 32).addBox(-1.5F, -27.5795F, 1.3225F, 3.0F, 4.0F, 4.0F, 0.0F, false);

		rightWing = new ModelPart(this);
		rightWing.setPos(-3.0F, -5.4353F, 2.0799F);
		Body.addChild(rightWing);
		setRotationAngle(rightWing, 0.0F, 0.48F, 0.0F);
		rightWing.texOffs(14, 19).addBox(-0.6006F, -1.1582F, -0.2803F, 0.0F, 1.0F, 3.0F, 0.0F, false);
		rightWing.texOffs(8, 40).addBox(-0.6006F, -0.1582F, 0.2197F, 0.0F, 15.0F, 4.0F, 0.0F, false);
		rightWing.texOffs(0, 20).addBox(-0.6006F, 14.8418F, 0.7197F, 0.0F, 1.0F, 3.0F, 0.0F, false);

		leftWing = new ModelPart(this);
		leftWing.setPos(3.3F, -5.2412F, 2.8043F);
		Body.addChild(leftWing);
		setRotationAngle(leftWing, 0.0F, -0.48F, 0.0F);
		leftWing.texOffs(14, 18).addBox(0.0F, -1.3523F, -0.7844F, 0.0F, 1.0F, 3.0F, 0.0F, false);
		leftWing.texOffs(0, 40).addBox(0.0F, -0.3523F, -0.2844F, 0.0F, 15.0F, 4.0F, 0.0F, false);
		leftWing.texOffs(0, 4).addBox(0.0F, 14.6477F, 0.2156F, 0.0F, 1.0F, 3.0F, 0.0F, false);

		RightArm = new ModelPart(this);
		RightArm.setPos(-4.5F, 2.0F, 0.0F);
		RightArm.texOffs(24, 52).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		RightArm.texOffs(14, 15).addBox(-1.3F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, 0.0F, false);

		rightFore = new ModelPart(this);
		rightFore.setPos(0.0F, 3.8333F, 0.6667F);
		RightArm.addChild(rightFore);
		rightFore.texOffs(14, 23).addBox(-0.5F, 5.1667F, 0.3333F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		rightFore.texOffs(5, 23).addBox(-0.5F, 5.1667F, -1.4167F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		rightFore.texOffs(24, 52).addBox(-1.0F, 0.1667F, -0.9167F, 2.0F, 5.0F, 2.0F, 0.0F, false);

		RightLeg = new ModelPart(this);
		RightLeg.setPos(-1.9F, 12.0F, 0.0F);
		RightLeg.texOffs(48, 50).addBox(-2.1F, 3.4F, 0.3F, 3.0F, 5.0F, 3.0F, 0.0F, false);
		RightLeg.texOffs(38, 50).addBox(-2.0F, 0.0F, 0.8F, 3.0F, 12.0F, 2.0F, 0.0F, false);

		LeftLeg = new ModelPart(this);
		LeftLeg.setPos(1.9F, 12.0F, 0.0F);
		LeftLeg.texOffs(48, 0).addBox(-1.0F, 0.0F, 0.8F, 3.0F, 12.0F, 2.0F, 0.0F, false);
		LeftLeg.texOffs(0, 15).addBox(-0.9F, 3.4F, 0.3F, 3.0F, 5.0F, 3.0F, 0.0F, false);

		LeftArm = new ModelPart(this);
		LeftArm.setPos(4.5F, 2.0F, 0.0F);
		LeftArm.texOffs(16, 50).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		LeftArm.texOffs(16, 28).addBox(-0.7F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, 0.0F, false);

		leftFore = new ModelPart(this);
		leftFore.setPos(0.0F, 4.8333F, 0.6667F);
		LeftArm.addChild(leftFore);
		leftFore.texOffs(16, 50).addBox(-1.0F, -0.8333F, -0.9167F, 2.0F, 5.0F, 2.0F, 0.0F, false);
		leftFore.texOffs(21, 15).addBox(-0.5F, 4.1667F, 0.3333F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		leftFore.texOffs(20, 21).addBox(-0.5F, 4.1667F, -1.4167F, 1.0F, 2.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityFargone entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		RightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		RightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}