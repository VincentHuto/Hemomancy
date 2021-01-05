package com.huto.hemomancy.model.entity;
// Made with Blockbench 3.6.6

import com.huto.hemomancy.entity.EntityLeech;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

public class ModelLeech extends EntityModel<EntityLeech> {
	private final ModelRenderer Head;
	private final ModelRenderer LeftEye;
	private final ModelRenderer RightEye;
	private final ModelRenderer Body;
	private final ModelRenderer Tail;

	public ModelLeech() {
		textureWidth = 16;
		textureHeight = 16;

		Head = new ModelRenderer(this);
		Head.setRotationPoint(-0.5F, 24.0F, -2.0F);
		Head.setTextureOffset(5, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		Head.setTextureOffset(0, 6).addBox(-0.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		Head.setTextureOffset(0, 8).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
		Head.setTextureOffset(5, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.0F, 2.0F, 0.0F, false);

		LeftEye = new ModelRenderer(this);
		LeftEye.setRotationPoint(0.5F, 0.0F, 2.0F);
		Head.addChild(LeftEye);
		

		RightEye = new ModelRenderer(this);
		RightEye.setRotationPoint(0.0F, 0.0F, 3.0F);
		Head.addChild(RightEye);
		

		Body = new ModelRenderer(this);
		Body.setRotationPoint(-0.5F, 24.0F, 0.0F);
		Body.setTextureOffset(0, 4).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Body.setTextureOffset(0, 4).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.0F, 3.0F, 0.0F, false);

		Tail = new ModelRenderer(this);
		Tail.setRotationPoint(-0.5F, 23.5F, 1.0F);
		Tail.setTextureOffset(0, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Tail.setTextureOffset(0, 0).addBox(-1.5F, 0.5F, 0.0F, 3.0F, 0.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityLeech entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.Head.rotateAngleX = headPitch * ((float) Math.PI / 180F)* .5F;
		this.Head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F)* .5F;
		this.Tail.rotateAngleY = MathHelper.sin(limbSwing * 7.6662F) * .5F * limbSwingAmount;
		this.Body.rotateAngleY = MathHelper.cos(limbSwing * 7.6662F + (float) Math.PI) * .5F * limbSwingAmount;
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		Tail.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}