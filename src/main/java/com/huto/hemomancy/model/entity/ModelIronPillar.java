package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.blood.iron.EntityIronPillar;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelIronPillar extends EntityModel<EntityIronPillar> {
	private final ModelPart pillar;

	public ModelIronPillar() {
		textureWidth = 128;
		textureHeight = 128;

		pillar = new ModelRenderer(this);
		pillar.setRotationPoint(0.0F, 24.0F, 0.0F);
		pillar.setTextureOffset(0, 20).addBox(-5.0F, -14.0F, -5.0F, 10.0F, 1.0F, 10.0F, 0.0F, false);
		pillar.setTextureOffset(30, 30).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, 0.0F, false);
		pillar.setTextureOffset(0, 0).addBox(-4.0F, -13.0F, -4.0F, 8.0F, 12.0F, 8.0F, 0.0F, false);
		pillar.setTextureOffset(24, 4).addBox(3.0F, -2.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(24, 2).addBox(4.0F, -2.0F, -4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(24, 0).addBox(4.0F, -13.0F, -4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(4, 24).addBox(3.0F, -13.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 24).addBox(4.0F, -2.0F, 3.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(4, 22).addBox(4.0F, -13.0F, 3.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 22).addBox(3.0F, -2.0F, 4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(4, 20).addBox(3.0F, -13.0F, 4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 20).addBox(-4.0F, -2.0F, 4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 6).addBox(-4.0F, -13.0F, 4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(3, 5).addBox(-5.0F, -2.0F, 3.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 4).addBox(-5.0F, -2.0F, -4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(3, 1).addBox(-4.0F, -13.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(3, 3).addBox(-4.0F, -2.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 2).addBox(-5.0F, -13.0F, -4.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pillar.setTextureOffset(0, 0).addBox(-5.0F, -13.0F, 3.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityIronPillar entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
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