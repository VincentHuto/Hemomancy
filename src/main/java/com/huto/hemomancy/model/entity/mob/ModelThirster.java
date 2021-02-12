package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityThirster;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelThirster extends EntityModel<EntityThirster> {
	private final ModelRenderer Head;
	private final ModelRenderer head2;
	private final ModelRenderer lowerJaw;
	private final ModelRenderer neck;
	private final ModelRenderer Body;
	private final ModelRenderer RightArm;
	private final ModelRenderer LeftArm;
	private final ModelRenderer RightLeg;
	private final ModelRenderer LeftLeg;

	public ModelThirster() {
		textureWidth = 64;
		textureHeight = 64;

		Head = new ModelRenderer(this);
		Head.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotationAngle(Head, 0.3491F, 0.0F, 0.0F);

		head2 = new ModelRenderer(this);
		head2.setRotationPoint(0.0F, -0.7104F, 1.2249F);
		Head.addChild(head2);
		head2.setTextureOffset(0, 0).addBox(-4.0F, -6.5111F, -7.4805F, 8.0F, 7.0F, 8.0F, 0.0F, false);

		lowerJaw = new ModelRenderer(this);
		lowerJaw.setRotationPoint(0.0F, 2.5312F, -0.9696F);
		head2.addChild(lowerJaw);
		setRotationAngle(lowerJaw, -0.0873F, 0.0F, 0.0F);
		lowerJaw.setTextureOffset(16, 22).addBox(-3.0F, -2.0261F, -6.8191F, 6.0F, 2.0F, 8.0F, 0.0F, false);

		neck = new ModelRenderer(this);
		neck.setRotationPoint(0.0F, 26.0F, -2.0F);
		Head.addChild(neck);
		neck.setTextureOffset(39, 28).addBox(-2.0F, -27.9136F, -0.8389F, 4.0F, 4.0F, 6.0F, 0.0F, false);

		Body = new ModelRenderer(this);
		Body.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.setTextureOffset(0, 15).addBox(-4.0F, -1.0F, 2.0F, 8.0F, 11.0F, 4.0F, 0.0F, false);
		Body.setTextureOffset(0, 32).addBox(-4.0F, 1.0F, -1.0F, 8.0F, 11.0F, 3.0F, 0.0F, false);
		Body.setTextureOffset(0, 46).addBox(-4.0F, 10.0F, 2.0F, 8.0F, 3.0F, 3.0F, 0.0F, false);

		RightArm = new ModelRenderer(this);
		RightArm.setRotationPoint(-5.0F, 2.0F, 2.0F);
		RightArm.setTextureOffset(44, 12).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		LeftArm = new ModelRenderer(this);
		LeftArm.setRotationPoint(5.0F, 2.0F, 2.0F);
		LeftArm.setTextureOffset(38, 38).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		RightLeg = new ModelRenderer(this);
		RightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		RightLeg.setTextureOffset(32, 0).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		LeftLeg = new ModelRenderer(this);
		LeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		LeftLeg.setTextureOffset(22, 32).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityThirster entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		RightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		RightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}