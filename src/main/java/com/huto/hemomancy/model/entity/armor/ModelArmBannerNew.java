package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public class ModelArmBannerNew extends Model {
	public final ModelPart leftShoulder;

	public ModelArmBannerNew() {
		super(RenderType::getEntitySolid);
		textureWidth = 32;
		textureHeight = 32;

		leftShoulder = new ModelRenderer(this);
		leftShoulder.setRotationPoint(5.25F, 1.75F, 0.0F);
		leftShoulder.setTextureOffset(0, 0).addBox(-1.0F, -4.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, false);
		leftShoulder.setTextureOffset(0, 7).addBox(3.0F, -2.0F, -2.5F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		leftShoulder.setTextureOffset(0, 15).addBox(4.0F, -3.75F, -2.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		leftShoulder.setTextureOffset(7, 7).addBox(-1.0F, -5.0F, -1.5F, 4.0F, 1.0F, 3.0F, 0.0F, false);
		leftShoulder.setTextureOffset(15, 0).addBox(-1.0F, -2.5F, 2.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		leftShoulder.setTextureOffset(12, 11).addBox(-1.0F, -2.5F, -3.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		leftShoulder.setTextureOffset(14, 16).addBox(-1.0F, -3.5F, -3.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
		leftShoulder.setTextureOffset(6, 16).addBox(-1.0F, -3.5F, 2.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
	}


	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		leftShoulder.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}