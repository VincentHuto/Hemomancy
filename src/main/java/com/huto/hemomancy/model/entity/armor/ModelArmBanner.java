package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class ModelArmBanner extends Model {
	private final ModelPart plate;
	private final ModelPart handle;

	public ModelArmBanner() {
		super(RenderType::getEntitySolid);
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.plate = new ModelRenderer(this, 0, 0);
		this.plate.addBox(-6.0F, -11.0F, -2.0F, 12.0F, 22.0F, 1.0F, 0.0F);
		this.handle = new ModelRenderer(this, 26, 0);
		this.handle.addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F, 0.0F);
	}

	public ModelRenderer getPlate() {
		return this.plate;
	}

	public ModelRenderer getHandle() {
		return this.handle;
	}

	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		this.plate.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		this.handle.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
