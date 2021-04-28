package com.huto.hemomancy.model.entity.armor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelLivingAxe extends Model {
	private final ModelRenderer whole;
	private final ModelRenderer grip;
	private final ModelRenderer grip2;
	private final ModelRenderer grip3;
	private final ModelRenderer pommel;
	private final ModelRenderer swell;
	private final ModelRenderer blade;
	private final List<ModelRenderer> parts;

	public ModelLivingAxe() {
		super(RenderType::getEntityTranslucent);

		textureWidth = 64;
		textureHeight = 64;

		whole = new ModelRenderer(this);
		whole.setRotationPoint(0.0029F, 16.4441F, -4.9853F);

		grip = new ModelRenderer(this);
		grip.setRotationPoint(1.4971F, -19.4441F, -2.5147F);
		whole.addChild(grip);
		grip.setTextureOffset(4, 27).addBox(-2.5F, -8.0F, 6.5F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		grip.setTextureOffset(15, 6).addBox(-2.75F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(15, 0).addBox(-2.75F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(0, 9).addBox(-2.75F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(0, 6).addBox(-1.25F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(0, 28).addBox(-2.0F, -3.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(27, 0).addBox(-2.0F, -7.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(28, 15).addBox(-2.0F, -5.1F, 6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(0, 3).addBox(-1.25F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(12, 12).addBox(-1.25F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(18, 27).addBox(-2.0F, -3.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(27, 21).addBox(-2.0F, -5.1F, 7.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip.setTextureOffset(12, 17).addBox(-2.0F, -7.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		grip2 = new ModelRenderer(this);
		grip2.setRotationPoint(1.4971F, 2.5559F, -2.5147F);
		whole.addChild(grip2);
		grip2.setTextureOffset(4, 27).addBox(-2.5F, -8.0F, 6.5F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		grip2.setTextureOffset(39, 0).addBox(-2.0F, -38.0F, 7.0F, 1.0F, 40.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(15, 6).addBox(-2.75F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(15, 0).addBox(-2.75F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(0, 9).addBox(-2.75F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(0, 6).addBox(-1.25F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(0, 28).addBox(-2.0F, -3.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(27, 0).addBox(-2.0F, -7.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(28, 15).addBox(-2.0F, -5.1F, 6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(0, 3).addBox(-1.25F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(12, 12).addBox(-1.25F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(18, 27).addBox(-2.0F, -3.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(27, 21).addBox(-2.0F, -5.1F, 7.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip2.setTextureOffset(12, 17).addBox(-2.0F, -7.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		grip3 = new ModelRenderer(this);
		grip3.setRotationPoint(1.4971F, -8.4441F, -2.5147F);
		whole.addChild(grip3);
		grip3.setTextureOffset(4, 27).addBox(-2.5F, -8.0F, 6.5F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		grip3.setTextureOffset(15, 6).addBox(-2.75F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(15, 0).addBox(-2.75F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(0, 9).addBox(-2.75F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(0, 6).addBox(-1.25F, -4.0F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(0, 28).addBox(-2.0F, -3.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(27, 0).addBox(-2.0F, -7.6F, 6.25F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(28, 15).addBox(-2.0F, -5.1F, 6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(0, 3).addBox(-1.25F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(12, 12).addBox(-1.25F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(18, 27).addBox(-2.0F, -3.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(27, 21).addBox(-2.0F, -5.1F, 7.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		grip3.setTextureOffset(12, 17).addBox(-2.0F, -7.6F, 7.75F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		pommel = new ModelRenderer(this);
		pommel.setRotationPoint(1.4971F, 6.5559F, -2.5147F);
		whole.addChild(pommel);
		pommel.setTextureOffset(34, 22).addBox(-3.0F, -1.6F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pommel.setTextureOffset(33, 22).addBox(-2.0F, -1.6F, 8.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pommel.setTextureOffset(26, 11).addBox(-2.5F, -2.0F, 6.5F, 2.0F, 2.0F, 2.0F, 0.0F, false);
		pommel.setTextureOffset(30, 21).addBox(-2.0F, -1.5F, 7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		pommel.setTextureOffset(34, 22).addBox(-1.0F, -1.6F, 7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		pommel.setTextureOffset(33, 22).addBox(-2.0F, -1.6F, 6.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		swell = new ModelRenderer(this);
		swell.setRotationPoint(1.4971F, -20.4441F, -2.5147F);
		whole.addChild(swell);
		swell.setTextureOffset(24, 11).addBox(-3.0F, -9.1F, 5.7F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(24, 17).addBox(-3.0F, -9.0F, 6.0F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(24, 17).addBox(-2.5F, -8.0F, 6.0F, 2.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(27, 6).addBox(-3.25F, -9.1F, 6.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(21, 24).addBox(-3.0F, -9.1F, 6.3F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(24, 28).addBox(-0.75F, -9.1F, 6.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(0, 6).addBox(-3.5F, -10.0F, 5.0F, 4.0F, 1.0F, 5.0F, 0.0F, false);
		swell.setTextureOffset(12, 27).addBox(-0.7F, -11.0F, 5.5F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		swell.setTextureOffset(27, 1).addBox(-3.2F, -11.0F, 5.5F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		swell.setTextureOffset(0, 0).addBox(-3.5F, -12.0F, 5.0F, 4.0F, 1.0F, 5.0F, 0.0F, false);
		swell.setTextureOffset(12, 17).addBox(-3.0F, -12.5F, 5.5F, 3.0F, 1.0F, 4.0F, 0.0F, false);
		swell.setTextureOffset(18, 28).addBox(-2.0F, -12.5F, 3.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		swell.setTextureOffset(29, 29).addBox(-2.0F, -13.5F, 4.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(29, 29).addBox(-2.0F, -13.5F, 8.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(29, 29).addBox(-3.0F, -14.5F, 6.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(29, 29).addBox(-1.0F, -14.0F, 6.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		swell.setTextureOffset(0, 0).addBox(-2.0F, -14.5F, 3.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		swell.setTextureOffset(0, 0).addBox(-2.0F, -12.5F, 11.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		swell.setTextureOffset(7, 4).addBox(-2.9F, -13.0F, 6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		swell.setTextureOffset(7, 4).addBox(-2.9F, -13.0F, 8.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		swell.setTextureOffset(7, 4).addBox(-1.1F, -13.0F, 6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		swell.setTextureOffset(7, 4).addBox(-1.1F, -13.0F, 8.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		blade = new ModelRenderer(this);
		blade.setRotationPoint(1.4971F, 13.5559F, -9.5147F);
		whole.addChild(blade);
		blade.setTextureOffset(0, 9).addBox(-1.5F, -45.5F, 6.25F, 0.001f, 10.0F, 3.0F, 0.0F, false);
		blade.setTextureOffset(0, 19).addBox(-1.5F, -45.5F, 9.25F, 0.001f, 6.0F, 3.0F, 0.0F, false);
		blade.setTextureOffset(0, 9).addBox(-1.5F, -45.5F, 12.25F, 0.001f, 6.0F, 2.0F, 0.0F, false);
		blade.setTextureOffset(4, 12).addBox(-1.5F, -39.5F, 9.25F, 0.001f, 3.0F, 2.0F, 0.0F, false);
		blade.setTextureOffset(0, 31).addBox(-1.5F, -44.5F, 5.25F, 0.001f, 8.0F, 1.0F, 0.0F, false);
		blade.setTextureOffset(12, 21).addBox(-1.5F, -48.0F, 9.0F, 0.001f, 2.0F, 1.0F, 0.0F, false);
		blade.setTextureOffset(12, 21).addBox(-1.5F, -35.0F, 9.0F, 0.001f, 2.0F, 1.0F, 0.0F, false);
		blade.setTextureOffset(12, 21).addBox(-1.5F, -36.5F, 9.5F, 0.001f, 2.0F, 1.0F, 0.0F, false);
		blade.setTextureOffset(0, 0).addBox(-1.5F, -46.5F, 7.0F, 0.001f, 1.0F, 2.0F, 0.0F, false);
		blade.setTextureOffset(0, 0).addBox(-1.5F, -35.5F, 7.0F, 0.001f, 1.0F, 2.0F, 0.0F, false);
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