package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelIronSpike extends EntityModel<EntityIronSpike> {
	private final ModelRenderer pillar;

	public ModelIronSpike() {
		textureWidth = 64;
		textureHeight = 64;

		pillar = new ModelRenderer(this);
		pillar.setRotationPoint(0.0F, 24.0F, 0.0F);
		pillar.setTextureOffset(18, 18).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
		pillar.setTextureOffset(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 4.0F, 8.0F, 0.0F, false);
		pillar.setTextureOffset(0, 21).addBox(-2.0F, -11.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
		pillar.setTextureOffset(0, 0).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		pillar.setTextureOffset(24, 0).addBox(-0.5F, -23.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(28, 28).addBox(-0.5F, -10.0F, -3.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(16, 25).addBox(-0.5F, -13.0F, -2.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(22, 12).addBox(-0.5F, -13.0F, 1.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 28).addBox(-0.5F, -6.0F, -4.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(12, 28).addBox(-0.5F, -10.0F, 2.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(24, 25).addBox(-0.5F, -6.0F, 3.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(8, 28).addBox(2.5F, -10.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(18, 12).addBox(1.5F, -13.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 12).addBox(-2.5F, -13.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(26, 12).addBox(3.5F, -6.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(4, 28).addBox(-3.5F, -10.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(20, 25).addBox(-4.5F, -6.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 12).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 3.0F, 6.0F, 0.0F, false);
		pillar.setTextureOffset(24, 31).addBox(3.0F, -4.5F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(20, 31).addBox(4.0F, -4.5F, -4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(16, 31).addBox(4.0F, -4.5F, 3.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(30, 12).addBox(3.0F, -4.5F, 4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(28, 4).addBox(-4.0F, -4.5F, 4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(28, 0).addBox(-5.0F, -4.5F, 3.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(12, 21).addBox(-5.0F, -4.5F, -4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 21).addBox(-4.0F, -4.5F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityIronSpike entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		pillar.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}