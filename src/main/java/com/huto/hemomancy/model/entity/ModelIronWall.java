package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.EntityIronWall;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelIronWall extends EntityModel<EntityIronWall> {
	private final ModelRenderer whole;

	public ModelIronWall() {
		textureWidth = 128;
		textureHeight = 128;

		whole = new ModelRenderer(this);
		whole.setRotationPoint(0.0F, 24.0F, 0.0F);
		whole.setTextureOffset(0, 48).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(28, 72).addBox(-8.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.setTextureOffset(54, 63).addBox(-8.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.setTextureOffset(0, 16).addBox(-8.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(48, 32).addBox(8.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(74, 43).addBox(10.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(74, 11).addBox(10.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(48, 48).addBox(8.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(48, 16).addBox(-15.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(56, 77).addBox(-14.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(74, 27).addBox(-14.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(48, 0).addBox(-15.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(24, 8).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(24, 40).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(0, 32).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(0, 66).addBox(0.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.setTextureOffset(14, 80).addBox(1.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(70, 0).addBox(1.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 80).addBox(1.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(38, 0).addBox(1.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(78, 55).addBox(-7.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(24, 0).addBox(-7.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(77, 62).addBox(-7.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(72, 77).addBox(-7.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(26, 58).addBox(0.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(0.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.setTextureOffset(24, 24).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityIronWall entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}