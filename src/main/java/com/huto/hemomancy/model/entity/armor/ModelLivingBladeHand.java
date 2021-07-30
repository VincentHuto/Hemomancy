package com.huto.hemomancy.model.entity.armor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public class ModelLivingBladeHand extends Model {
	private final ModelPart whole;
	private final List<ModelPart> parts;

	public ModelLivingBladeHand() {
		super(RenderType::getEntityTranslucent);
		textureWidth = 64;
		textureHeight = 64;

		whole = new ModelRenderer(this);
		whole.setRotationPoint(0.0029F, 16.4441F, 0.0147F);
		whole.setTextureOffset(30, 21).addBox(-1.0029F, 5.5559F, -1.0147F, 2.0F, 2.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(0, 28).addBox(-0.5029F, 2.9559F, -1.2647F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(18, 27).addBox(-0.5029F, 2.9559F, 0.2353F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(27, 0).addBox(-0.5029F, -1.0441F, -1.2647F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 17).addBox(-0.5029F, -1.0441F, 0.2353F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(28, 15).addBox(-0.5029F, 1.4559F, -1.2647F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(27, 21).addBox(-0.5029F, 1.4559F, 0.2353F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(21, 22).addBox(-1.0029F, 4.9559F, -2.0147F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 32).addBox(0.9971F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(31, 26).addBox(-2.0029F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(10, 27).addBox(-1.0029F, 4.9559F, 0.9853F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(4, 27).addBox(-1.0029F, -1.4441F, -1.0147F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-1.2529F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 6).addBox(0.2471F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(15, 0).addBox(-1.2529F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 12).addBox(0.2471F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 9).addBox(-1.2529F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 3).addBox(0.2471F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 22).addBox(-1.5029F, 4.5559F, -1.5147F, 3.0F, 2.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(24, 17).addBox(-1.5029F, -2.4441F, -1.5147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(24, 11).addBox(-1.5029F, -2.5441F, -1.8147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(21, 24).addBox(-1.5029F, -2.5441F, -1.2147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(24, 28).addBox(0.7471F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(27, 6).addBox(-1.7529F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(12, 17).addBox(-2.0029F, -5.9441F, -2.0147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(18, 28).addBox(-0.5029F, -6.9441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(29, 29).addBox(-0.5029F, -7.9441F, -1.0147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(-0.5029F, -8.9441F, -2.0147F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(6, 9).addBox(-0.0029F, -22.9441F, -1.0147F, 0.001F, 15.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(0, 9).addBox(-0.0029F, -38.9441F, -1.5147F, 0.001F, 16.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(-0.0029F, -39.9441F, -0.5147F, 0.001F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(-0.0029F, -39.9441F, -0.5147F, 0.001F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(12, 21).addBox(-0.0029F, -40.9441F, 1.4853F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-2.0029F, -4.4441F, -2.0147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(15, 0).addBox(-2.0029F, -4.4441F, -1.7147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(12, 12).addBox(-2.0029F, -4.4441F, -2.3147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(27, 1).addBox(-2.2029F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(12, 27).addBox(1.2971F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 6).addBox(-2.5029F, -3.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(-2.5029F, -5.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, 0.0F, false);
		this.parts = ImmutableList.of(whole);

	}

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
}