package com.huto.hemomancy.model.entity.armor;

import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDarkArrowHorizontal extends EntityModel<EntityBloodShot> {
	private final ModelRenderer bone;

	public ModelDarkArrowHorizontal() {
		textureWidth = 16;
		textureHeight = 16;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(7.75F, 21.0F, -9.0F);
		bone.setTextureOffset(0, 13).addBox(-9.0F, -5.0F, 0.0F, 1.0F, 2.0F, 14.0F, 0.0F, false);
		bone.setTextureOffset(0, 1).addBox(-9.0F, -5.0F, -2.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
		bone.setTextureOffset(0, 1).addBox(-9.0F, -6.0F, -1.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		bone.setTextureOffset(0, 1).addBox(-9.0F, -2.0F, 0.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		bone.setTextureOffset(0, 1).addBox(-9.0F, -7.0F, 0.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		bone.setTextureOffset(0, 1).addBox(-9.0F, -3.0F, -1.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		bone.setTextureOffset(0, 4).addBox(-9.0F, -3.0F, 15.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.setTextureOffset(0, 4).addBox(-9.0F, -6.0F, 15.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.setTextureOffset(0, 4).addBox(-9.0F, -7.0F, 17.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.setTextureOffset(0, 4).addBox(-9.0F, -2.0F, 17.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.setTextureOffset(0, 4).addBox(-9.0F, -5.0F, 14.0F, 1.0F, 2.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityBloodShot entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
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