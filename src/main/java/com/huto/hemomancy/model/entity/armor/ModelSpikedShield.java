package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelSpikedShield extends Model {
	private final ModelRenderer shield3;
	private final ModelRenderer bone;
	private final ModelRenderer bone9;
	private final ModelRenderer bone10;
	private final ModelRenderer bone2;
	private final ModelRenderer bone3;
	private final ModelRenderer bone4;
	private final ModelRenderer bone5;
	private final ModelRenderer bone6;
	private final ModelRenderer bone7;
	private final ModelRenderer bone11;
	private final ModelRenderer bone12;
	private final ModelRenderer bone8;

	public ModelSpikedShield() {
		super(RenderType::getEntitySolid);

		textureWidth = 64;
		textureHeight = 64;

		shield3 = new ModelRenderer(this);
		shield3.setRotationPoint(1.0F, 8.5F, 3.0F);
		shield3.setTextureOffset(14, 23).addBox(-1.0F, -15.5F, -3.0F, 2.0F, 6.0F, 6.0F, 0.0F, false);
		shield3.setTextureOffset(0, 13).addBox(-4.5F, -17.0F, -4.75F, 9.0F, 9.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(20, 13).addBox(-4.0F, -16.5F, -5.0F, 8.0F, 8.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(37, 31).addBox(-2.0F, -14.5F, -6.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(37, 31).addBox(-1.0F, -13.5F, -6.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(37, 31).addBox(0.0F, -13.5F, -8.5F, 0.0F, 2.0F, 2.0F, 0.0F, false);
		shield3.setTextureOffset(37, 31).addBox(0.0F, -13.0F, -9.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(37, 31).addBox(-1.0F, -12.5F, -8.5F, 2.0F, 0.0F, 2.0F, 0.0F, false);
		shield3.setTextureOffset(0, 0).addBox(-6.0F, -18.5F, -4.0F, 12.0F, 12.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(0, 24).addBox(-7.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(0, 24).addBox(6.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(24, 23).addBox(-5.0F, -6.5F, -3.75F, 10.0F, 1.0F, 1.0F, 0.0F, false);
		shield3.setTextureOffset(24, 23).addBox(-5.0F, -19.5F, -3.75F, 10.0F, 1.0F, 1.0F, 0.0F, false);

		bone = new ModelRenderer(this);
		bone.setRotationPoint(3.0F, -17.75F, -4.5833F);
		shield3.addChild(bone);
		setRotationAngle(bone, -0.2618F, 0.0F, 0.48F);
		bone.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(0.0F, -18.25F, -4.5833F);
		shield3.addChild(bone9);
		setRotationAngle(bone9, -0.2618F, 0.0F, 0.0F);
		bone9.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone9.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone9.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(0.0F, -6.75F, -4.5833F);
		shield3.addChild(bone10);
		setRotationAngle(bone10, -0.2618F, 0.0F, -3.1416F);
		bone10.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone10.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone10.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(-3.0F, -17.75F, -4.5833F);
		shield3.addChild(bone2);
		setRotationAngle(bone2, -0.2618F, 0.0F, -0.48F);
		bone2.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone2.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone2.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(-3.0F, -7.25F, -4.5833F);
		shield3.addChild(bone3);
		setRotationAngle(bone3, -0.2618F, 0.0F, -2.6616F);
		bone3.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone3.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone3.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(3.0F, -7.25F, -4.5833F);
		shield3.addChild(bone4);
		setRotationAngle(bone4, -0.2618F, 0.0F, 2.6616F);
		bone4.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone4.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone4.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(-5.0F, -16.25F, -4.5833F);
		shield3.addChild(bone5);
		setRotationAngle(bone5, -0.2618F, 0.0F, -1.0908F);
		bone5.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone5.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone5.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(5.0F, -16.25F, -4.5833F);
		shield3.addChild(bone6);
		setRotationAngle(bone6, -0.2618F, 0.0F, 1.0908F);
		bone6.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone6.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone6.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(5.0F, -8.25F, -4.5833F);
		shield3.addChild(bone7);
		setRotationAngle(bone7, -0.2618F, 0.0F, 2.0508F);
		bone7.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone7.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone7.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(5.5F, -12.25F, -4.5833F);
		shield3.addChild(bone11);
		setRotationAngle(bone11, -0.2618F, 0.0F, 1.5708F);
		bone11.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone11.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone11.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(-5.5F, -12.25F, -4.5833F);
		shield3.addChild(bone12);
		setRotationAngle(bone12, -0.2618F, 0.0F, -1.5708F);
		bone12.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone12.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone12.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(-5.0F, -8.25F, -4.5833F);
		shield3.addChild(bone8);
		setRotationAngle(bone8, -0.2618F, 0.0F, -2.0508F);
		bone8.setTextureOffset(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone8.setTextureOffset(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone8.setTextureOffset(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		shield3.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}