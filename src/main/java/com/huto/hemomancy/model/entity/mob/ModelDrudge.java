package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityDrudge;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDrudge extends EntityModel<EntityDrudge> {
	private final ModelRenderer tent;
	private final ModelRenderer tent2;
	private final ModelRenderer tent3;
	private final ModelRenderer tent4;
	private final ModelRenderer tent5;
	private final ModelRenderer tent6;
	private final ModelRenderer tent7;
	private final ModelRenderer tent8;
	private final ModelRenderer brain;

	public ModelDrudge() {
		textureWidth = 32;
		textureHeight = 32;

		tent = new ModelRenderer(this);
		tent.setRotationPoint(2.0F, 17.0F, -2.0F);
		tent.setTextureOffset(0, 17).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent.setTextureOffset(16, 13).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent.setTextureOffset(4, 15).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent2 = new ModelRenderer(this);
		tent2.setRotationPoint(0.0F, 16.0F, -2.5F);
		tent2.setTextureOffset(2, 15).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent2.setTextureOffset(0, 15).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent2.setTextureOffset(4, 13).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent3 = new ModelRenderer(this);
		tent3.setRotationPoint(0.5F, 17.25F, 1.0F);
		tent3.setTextureOffset(0, 1).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 3.0F, 1.0F, 0.0F, false);
		tent3.setTextureOffset(2, 13).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent3.setTextureOffset(0, 13).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent4 = new ModelRenderer(this);
		tent4.setRotationPoint(-1.0F, 17.0F, -1.0F);
		tent4.setTextureOffset(6, 4).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent4.setTextureOffset(6, 2).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent4.setTextureOffset(6, 0).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent5 = new ModelRenderer(this);
		tent5.setRotationPoint(1.0F, 16.0F, -2.0F);
		tent5.setTextureOffset(6, 6).addBox(-1.0F, 3.0F, 2.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent5.setTextureOffset(4, 6).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent5.setTextureOffset(2, 6).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent6 = new ModelRenderer(this);
		tent6.setRotationPoint(1.5F, 16.25F, -0.25F);
		tent6.setTextureOffset(0, 6).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent6.setTextureOffset(4, 1).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent6.setTextureOffset(4, 4).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent7 = new ModelRenderer(this);
		tent7.setRotationPoint(-0.5F, 17.0F, 1.0F);
		tent7.setTextureOffset(2, 4).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent7.setTextureOffset(0, 4).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent7.setTextureOffset(2, 2).addBox(-1.0F, -1.0F, 0.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent8 = new ModelRenderer(this);
		tent8.setRotationPoint(2.5F, 16.0F, 0.5F);
		tent8.setTextureOffset(2, 15).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent8.setTextureOffset(0, 15).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent8.setTextureOffset(4, 13).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		brain = new ModelRenderer(this);
		brain.setRotationPoint(0.5F, 15.0F, -1.0F);
		brain.setTextureOffset(0, 14).addBox(-3.5F, 0.0F, -1.5F, 5.0F, 1.0F, 6.0F, 0.0F, false);
		brain.setTextureOffset(16, 14).addBox(-2.5F, 1.0F, 1.25F, 3.0F, 2.0F, 3.0F, 0.0F, false);
		brain.setTextureOffset(0, 0).addBox(-2.0F, 1.0F, 0.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		brain.setTextureOffset(0, 0).addBox(-4.0F, -5.0F, -4.0F, 6.0F, 5.0F, 9.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityDrudge entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		tent.render(matrixStack, buffer, packedLight, packedOverlay);
		tent2.render(matrixStack, buffer, packedLight, packedOverlay);
		tent3.render(matrixStack, buffer, packedLight, packedOverlay);
		tent4.render(matrixStack, buffer, packedLight, packedOverlay);
		tent5.render(matrixStack, buffer, packedLight, packedOverlay);
		tent6.render(matrixStack, buffer, packedLight, packedOverlay);
		tent7.render(matrixStack, buffer, packedLight, packedOverlay);
		tent8.render(matrixStack, buffer, packedLight, packedOverlay);
		brain.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}