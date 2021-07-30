package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.drudge.EntityDrudge;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelDrudge extends EntityModel<EntityDrudge> {
	private final ModelPart tent;
	private final ModelPart tent2;
	private final ModelPart tent3;
	private final ModelPart tent4;
	private final ModelPart tent5;
	private final ModelPart tent6;
	private final ModelPart tent7;
	private final ModelPart tent8;
	private final ModelPart brain;
	private final ModelPart device;

	public ModelDrudge() {
		textureWidth = 64;
		textureHeight = 64;

		tent = new ModelRenderer(this);
		tent.setRotationPoint(2.1F, 17.0F, -2.0F);
		tent.setTextureOffset(0, 20).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent.setTextureOffset(0, 0).addBox(-1.0F, 5.0F, 2.25F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		tent.setTextureOffset(19, 16).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent.setTextureOffset(19, 14).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent2 = new ModelRenderer(this);
		tent2.setRotationPoint(0.0F, 16.0F, -2.5F);
		tent2.setTextureOffset(19, 19).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent2.setTextureOffset(6, 18).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent2.setTextureOffset(4, 18).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent3 = new ModelRenderer(this);
		tent3.setRotationPoint(0.5F, 17.25F, 1.0F);
		tent3.setTextureOffset(6, 6).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 3.0F, 1.0F, 0.0F, false);
		tent3.setTextureOffset(19, 18).addBox(-1.0F, 6.0F, 1.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		tent3.setTextureOffset(2, 18).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent3.setTextureOffset(0, 18).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent4 = new ModelRenderer(this);
		tent4.setRotationPoint(-1.1F, 17.0F, -1.0F);
		tent4.setTextureOffset(2, 20).addBox(-1.0F, 5.0F, 1.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		tent4.setTextureOffset(6, 1).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent4.setTextureOffset(6, 3).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent4.setTextureOffset(4, 16).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent5 = new ModelRenderer(this);
		tent5.setRotationPoint(1.0F, 16.0F, -2.25F);
		tent5.setTextureOffset(2, 16).addBox(-1.0F, 3.0F, 2.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent5.setTextureOffset(2, 16).addBox(-1.0F, 5.0F, 2.3F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		tent5.setTextureOffset(0, 16).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent5.setTextureOffset(6, 14).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent6 = new ModelRenderer(this);
		tent6.setRotationPoint(1.5F, 16.25F, -0.25F);
		tent6.setTextureOffset(4, 14).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent6.setTextureOffset(2, 14).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent6.setTextureOffset(0, 14).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent7 = new ModelRenderer(this);
		tent7.setRotationPoint(-0.5F, 17.0F, 1.0F);
		tent7.setTextureOffset(8, 7).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent7.setTextureOffset(6, 5).addBox(-1.0F, 5.0F, 2.25F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		tent7.setTextureOffset(8, 5).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent7.setTextureOffset(8, 3).addBox(-1.0F, -1.0F, 0.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		tent8 = new ModelRenderer(this);
		tent8.setRotationPoint(2.5F, 16.0F, 0.5F);
		tent8.setTextureOffset(8, 1).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent8.setTextureOffset(0, 7).addBox(-1.0F, 5.0F, 1.75F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent8.setTextureOffset(4, 7).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, 0.0F, false);
		tent8.setTextureOffset(2, 7).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		brain = new ModelRenderer(this);
		brain.setRotationPoint(0.5F, 15.0F, -1.0F);
		brain.setTextureOffset(19, 15).addBox(-3.5F, 0.0F, -3.5F, 5.0F, 1.0F, 8.0F, 0.0F, false);
		brain.setTextureOffset(22, 0).addBox(-2.5F, 1.0F, 0.55F, 3.0F, 2.0F, 4.0F, 0.0F, false);
		brain.setTextureOffset(0, 0).addBox(-1.5F, 0.0F, 2.7F, 1.0F, 6.0F, 2.0F, 0.0F, false);
		brain.setTextureOffset(0, 0).addBox(-1.5F, 6.0F, 3.9F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		brain.setTextureOffset(4, 0).addBox(-2.0F, 1.0F, -0.75F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		brain.setTextureOffset(0, 0).addBox(-3.95F, -4.9F, -5.25F, 6.0F, 5.0F, 10.0F, 0.0F, false);
		brain.setTextureOffset(11, 25).addBox(-3.45F, -4.0F, -5.75F, 5.0F, 4.0F, 1.0F, 0.0F, false);
		brain.setTextureOffset(30, 24).addBox(-3.45F, -3.5F, 4.25F, 5.0F, 4.0F, 1.0F, 0.0F, false);
		brain.setTextureOffset(0, 28).addBox(1.55F, -4.0F, -4.75F, 1.0F, 4.0F, 9.0F, 0.0F, false);
		brain.setTextureOffset(19, 24).addBox(-4.45F, -4.0F, -4.75F, 1.0F, 4.0F, 9.0F, 0.0F, false);
		brain.setTextureOffset(0, 15).addBox(-3.45F, -5.5F, -4.75F, 5.0F, 1.0F, 9.0F, 0.0F, false);

		device = new ModelRenderer(this);
		device.setRotationPoint(3.05F, -5.0F, -1.25F);
		brain.addChild(device);
		device.setTextureOffset(1, 43).addBox(-3.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		device.setTextureOffset(0, 42).addBox(-5.5F, -1.25F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		device.setTextureOffset(3, 44).addBox(-5.0F, -1.0F, 1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		device.setTextureOffset(1, 43).addBox(-6.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		device.setTextureOffset(3, 44).addBox(-5.0F, -1.0F, -2.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityDrudge entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
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