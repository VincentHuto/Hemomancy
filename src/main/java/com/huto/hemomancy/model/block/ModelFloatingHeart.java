package com.huto.hemomancy.model.block;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class ModelFloatingHeart extends Model {
	private final ModelPart aorta;
	private final ModelPart bb_main;
	private final List<ModelPart> parts;

	public ModelFloatingHeart() {
		super(RenderType::getEntitySolid);

		textureWidth = 64;
		textureHeight = 64;

		aorta = new ModelRenderer(this);
		aorta.setRotationPoint(0.5F, 10.5F, -0.55F);
		setRotationAngle(aorta, 0.0F, 0.4363F, 0.0F);
		aorta.setTextureOffset(11, 27).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 5.0F, 0.0F, false);
		aorta.setTextureOffset(0, 26).addBox(-0.5F, -0.5F, 2.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		aorta.setTextureOffset(14, 3).addBox(-0.5F, -0.5F, -3.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		aorta.setTextureOffset(24, 12).addBox(-2.25F, -2.5F, 0.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		aorta.setTextureOffset(20, 20).addBox(-0.75F, -2.5F, 1.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		aorta.setTextureOffset(0, 14).addBox(-1.75F, -2.5F, -2.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		aorta.setTextureOffset(0, 0).addBox(-4.75F, -2.5F, -2.0F, 1.0F, 5.0F, 1.0F, 0.0F, false);

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(0, 26).addBox(-3.0F, -5.0F, -3.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 14).addBox(-2.0F, -11.0F, -3.3F, 4.0F, 6.0F, 6.0F, 0.0F, false);
		bb_main.setTextureOffset(24, 12).addBox(-2.5F, -3.0F, -3.0F, 3.0F, 1.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(20, 0).addBox(-4.0F, -12.0F, -2.8F, 4.0F, 7.0F, 5.0F, 0.0F, false);
		bb_main.setTextureOffset(25, 28).addBox(-5.0F, -11.0F, -2.3F, 1.0F, 6.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 32).addBox(0.25F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(31, 24).addBox(0.75F, -11.5F, -2.3F, 2.0F, 2.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(29, 17).addBox(-4.5F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(20, 20).addBox(-1.75F, -13.5F, -3.05F, 2.0F, 3.0F, 5.0F, 0.0F, false);
		bb_main.setTextureOffset(12, 34).addBox(-3.5F, -15.0F, -1.55F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(14, 0).addBox(-3.0F, -5.0F, 1.0F, 3.0F, 2.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(15, 12).addBox(0.25F, -10.75F, -3.05F, 2.0F, 3.0F, 5.0F, 0.0F, false);

		this.parts = ImmutableList.of(this.aorta, this.bb_main);

	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		this.renderAll(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	public void renderAll(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		this.parts.forEach((p_228248_8_) -> {
			p_228248_8_.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		});
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

}