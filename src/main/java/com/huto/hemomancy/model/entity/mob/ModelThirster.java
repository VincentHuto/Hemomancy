package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityThirster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelThirster extends EntityModel<EntityThirster> {
	private final ModelPart Head;
	private final ModelPart head2;
	private final ModelPart lowerJaw;
	private final ModelPart neck;
	private final ModelPart Body;
	private final ModelPart RightArm;
	private final ModelPart LeftArm;
	private final ModelPart RightLeg;
	private final ModelPart LeftLeg;

	public ModelThirster() {
		texWidth = 64;
		texHeight = 64;

		Head = new ModelPart(this);
		Head.setPos(0.0F, 0.0F, 0.0F);
		setRotationAngle(Head, 0.3491F, 0.0F, 0.0F);

		head2 = new ModelPart(this);
		head2.setPos(0.0F, -0.7104F, 1.2249F);
		Head.addChild(head2);
		head2.texOffs(0, 0).addBox(-4.0F, -6.5111F, -7.4805F, 8.0F, 7.0F, 8.0F, 0.0F, false);

		lowerJaw = new ModelPart(this);
		lowerJaw.setPos(0.0F, 2.5312F, -0.9696F);
		head2.addChild(lowerJaw);
		setRotationAngle(lowerJaw, -0.0873F, 0.0F, 0.0F);
		lowerJaw.texOffs(16, 22).addBox(-3.0F, -2.0261F, -6.8191F, 6.0F, 2.0F, 8.0F, 0.0F, false);

		neck = new ModelPart(this);
		neck.setPos(0.0F, 26.0F, -2.0F);
		Head.addChild(neck);
		neck.texOffs(39, 28).addBox(-2.0F, -27.9136F, -0.8389F, 4.0F, 4.0F, 6.0F, 0.0F, false);

		Body = new ModelPart(this);
		Body.setPos(0.0F, 0.0F, 0.0F);
		Body.texOffs(0, 15).addBox(-4.0F, -1.0F, 2.0F, 8.0F, 11.0F, 4.0F, 0.0F, false);
		Body.texOffs(0, 32).addBox(-4.0F, 1.0F, -1.0F, 8.0F, 11.0F, 3.0F, 0.0F, false);
		Body.texOffs(0, 46).addBox(-4.0F, 10.0F, 2.0F, 8.0F, 3.0F, 3.0F, 0.0F, false);

		RightArm = new ModelPart(this);
		RightArm.setPos(-5.0F, 2.0F, 2.0F);
		RightArm.texOffs(44, 12).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		LeftArm = new ModelPart(this);
		LeftArm.setPos(5.0F, 2.0F, 2.0F);
		LeftArm.texOffs(38, 38).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		RightLeg = new ModelPart(this);
		RightLeg.setPos(-1.9F, 12.0F, 0.0F);
		RightLeg.texOffs(32, 0).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		LeftLeg = new ModelPart(this);
		LeftLeg.setPos(1.9F, 12.0F, 0.0F);
		LeftLeg.texOffs(22, 32).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityThirster entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		RightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		RightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}