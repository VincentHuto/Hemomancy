package com.huto.hemomancy.model.entity.mob;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFungling extends EntityModel<Entity> {
	private final ModelRenderer tent1;
	private final ModelRenderer bone2;
	private final ModelRenderer bone3;
	private final ModelRenderer bone4;
	private final ModelRenderer tent2;
	private final ModelRenderer bone5;
	private final ModelRenderer bone6;
	private final ModelRenderer bone7;
	private final ModelRenderer tent3;
	private final ModelRenderer bone8;
	private final ModelRenderer bone9;
	private final ModelRenderer bone10;
	private final ModelRenderer tent4;
	private final ModelRenderer bone11;
	private final ModelRenderer bone12;
	private final ModelRenderer bone13;
	private final ModelRenderer bb_main;

	public ModelFungling() {
		textureWidth = 64;
		textureHeight = 64;

		tent1 = new ModelRenderer(this);
		tent1.setRotationPoint(0.75F, 23.25F, 0.0F);
		tent1.setTextureOffset(29, 21).addBox(0.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(1.75F, -0.25F, -0.75F);
		tent1.addChild(bone2);
		bone2.setTextureOffset(28, 29).addBox(0.0F, -0.5F, -0.75F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(2.0F, 0.25F, -0.5F);
		bone2.addChild(bone3);
		bone3.setTextureOffset(28, 18).addBox(0.0F, -0.25F, -0.75F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(2.0F, 0.5F, 0.0F);
		bone3.addChild(bone4);
		bone4.setTextureOffset(7, 30).addBox(0.0F, -0.25F, -0.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent2 = new ModelRenderer(this);
		tent2.setRotationPoint(-0.75F, 23.25F, 2.5F);
		tent2.setTextureOffset(24, 29).addBox(-1.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(-1.75F, -0.25F, 0.5F);
		tent2.addChild(bone5);
		bone5.setTextureOffset(24, 6).addBox(-2.0F, -0.5F, -0.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(-2.0F, 0.25F, -0.5F);
		bone5.addChild(bone6);
		bone6.setTextureOffset(6, 23).addBox(-2.0F, -0.25F, -0.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-2.0F, 0.5F, 1.0F);
		bone6.addChild(bone7);
		bone7.setTextureOffset(22, 29).addBox(-1.0F, -0.25F, -0.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent3 = new ModelRenderer(this);
		tent3.setRotationPoint(1.75F, 23.25F, 1.5F);
		tent3.setTextureOffset(22, 21).addBox(-2.5F, -1.25F, 1.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(0.0F, -0.25F, -0.5F);
		tent3.addChild(bone8);
		bone8.setTextureOffset(18, 29).addBox(-1.0F, -0.5F, 2.75F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(-2.0F, 0.25F, 0.5F);
		bone8.addChild(bone9);
		bone9.setTextureOffset(0, 29).addBox(1.5F, -0.25F, 4.25F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(-2.0F, 0.5F, -1.0F);
		bone9.addChild(bone10);
		bone10.setTextureOffset(4, 29).addBox(4.5F, -0.25F, 7.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent4 = new ModelRenderer(this);
		tent4.setRotationPoint(-2.25F, 23.25F, 0.5F);
		tent4.setTextureOffset(0, 6).addBox(0.5F, -1.25F, -2.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(0.0F, -0.25F, -0.5F);
		tent4.addChild(bone11);
		bone11.setTextureOffset(28, 15).addBox(0.0F, -0.5F, -3.75F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(-2.0F, 0.25F, 0.5F);
		bone11.addChild(bone12);
		bone12.setTextureOffset(14, 28).addBox(1.5F, -0.25F, -6.25F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		bone13 = new ModelRenderer(this);
		bone13.setRotationPoint(-2.0F, 0.5F, -1.0F);
		bone12.addChild(bone13);
		bone13.setTextureOffset(0, 17).addBox(2.5F, -0.25F, -6.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(8, 25).addBox(-1.5F, -5.0F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 0).addBox(-4.0F, -17.25F, -3.0F, 8.0F, 7.0F, 8.0F, 0.0F, false);
		bb_main.setTextureOffset(24, 0).addBox(-3.0F, -12.25F, -2.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(23, 23).addBox(1.0F, -12.25F, -2.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 23).addBox(1.0F, -12.25F, 2.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 0).addBox(-3.0F, -12.25F, 2.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 15).addBox(-2.0F, -9.0F, -1.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(16, 16).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(13, 21).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		bb_main.setTextureOffset(12, 15).addBox(-1.5F, -8.5F, -1.75F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 15).addBox(-0.5F, -7.5F, -2.1F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(16, 25).addBox(-1.0F, -8.0F, -2.05F, 2.0F, 2.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		tent1.render(matrixStack, buffer, packedLight, packedOverlay);
		tent2.render(matrixStack, buffer, packedLight, packedOverlay);
		tent3.render(matrixStack, buffer, packedLight, packedOverlay);
		tent4.render(matrixStack, buffer, packedLight, packedOverlay);
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}