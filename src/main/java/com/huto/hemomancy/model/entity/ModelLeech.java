package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.EntityLeech;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelLeech extends EntityModel<EntityLeech> {
	private final ModelRenderer Head;
	private final ModelRenderer headTop;
	private final ModelRenderer Body;
	private final ModelRenderer bodyTop;
	private final ModelRenderer Tail;
	private final ModelRenderer tailTop;

	public ModelLeech() {
		textureWidth = 16;
		textureHeight = 16;

		Head = new ModelRenderer(this);
		Head.setRotationPoint(-0.5F, 24.0F, -2.0F);
		Head.setTextureOffset(5, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		Head.setTextureOffset(5, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.01F, 2.0F, 0.0F, false);

		headTop = new ModelRenderer(this);
		headTop.setRotationPoint(-0.5F, 24.0F, -2.0F);
		headTop.setTextureOffset(11, 11).addBox(-0.5F, -2.0F, -1.0F, 1.0F,  0.5F, 1.0F, 0.0F, false);

		Body = new ModelRenderer(this);
		Body.setRotationPoint(-0.5F, 24.0F, 0.0F);
		Body.setTextureOffset(0, 4).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Body.setTextureOffset(0, 4).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.01F, 3.0F, 0.0F, false);

		bodyTop = new ModelRenderer(this);
		bodyTop.setRotationPoint(-0.5F, 24.0F, 0.0F);
		bodyTop.setTextureOffset(8, 11).addBox(-0.5F, -2.0F, -2.0F, 1.0F,  0.5F, 3.0F, 0.0F, false);

		Tail = new ModelRenderer(this);
		Tail.setRotationPoint(-0.5F, 23.5F, 1.0F);
		Tail.setTextureOffset(0, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Tail.setTextureOffset(0, 0).addBox(-1.5F, 0.5F, 0.0F, 3.0F, 0.01F, 3.0F, 0.0F, false);

		tailTop = new ModelRenderer(this);
		tailTop.setRotationPoint(-0.5F, 23.5F, 1.0F);
		tailTop.setTextureOffset(10, 13).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 0.5F, 2.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityLeech entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.Head.rotateAngleX = headPitch * ((float) Math.PI / 180F) * .5F;
		this.Head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F) * .5F;
		this.headTop.rotateAngleX = headPitch * ((float) Math.PI / 180F) * .5F;
		this.headTop.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F) * .5F;
		this.Tail.rotateAngleY = MathHelper.sin(limbSwing * 7.6662F) * .5F * limbSwingAmount;
		this.tailTop.rotateAngleY = MathHelper.sin(limbSwing * 7.6662F) * .5F * limbSwingAmount;
		this.Body.rotateAngleY = MathHelper.cos(limbSwing * 7.6662F + (float) Math.PI) * .5F * limbSwingAmount;
		this.bodyTop.rotateAngleY = MathHelper.cos(limbSwing * 7.6662F + (float) Math.PI) * .5F * limbSwingAmount;
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		Tail.render(matrixStack, buffer, packedLight, packedOverlay);
		headTop.render(matrixStack, buffer, packedLight, packedOverlay);
		bodyTop.render(matrixStack, buffer, packedLight, packedOverlay);
		tailTop.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}