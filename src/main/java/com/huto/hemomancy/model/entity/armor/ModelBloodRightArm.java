package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBloodRightArm extends BipedModel<PlayerEntity> implements IHasArm {

	public ModelRenderer head;
	public ModelRenderer headwear;
	public ModelRenderer body;
	public ModelRenderer rightArm;
	public ModelRenderer leftArm;
	public ModelRenderer rightLeg;
	public ModelRenderer leftLeg;

	public ModelBloodRightArm(float modelSizeIn) {
		super(modelSizeIn);

		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, modelSizeIn);
		head.setRotationPoint(0.0F, 0.0F , 0.0F);
		headwear = new ModelRenderer(this, 32, 0);
		headwear.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, modelSizeIn + 0.5F);
		headwear.setRotationPoint(0.0F, 0.0F , 0.0F);
		body = new ModelRenderer(this, 16, 16);
		body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, modelSizeIn);
		body.setRotationPoint(0.0F, 0.0F , 0.0F);
		rightArm = new ModelRenderer(this, 40, 16);
		rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		rightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		leftArm = new ModelRenderer(this, 40, 16);
		leftArm.mirror = true;
		leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		leftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		rightLeg = new ModelRenderer(this, 0, 16);
		rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		rightLeg.setRotationPoint(-1.9F, 12.0F , 0.0F);
		leftLeg = new ModelRenderer(this, 0, 16);
		leftLeg.mirror = true;
		leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		leftLeg.setRotationPoint(1.9F, 12.0F , 0.0F);
		

		bipedLeftArm.addChild(leftArm);
		bipedLeftLeg.addChild(leftLeg);
		bipedRightArm.addChild(rightArm);
		bipedRightLeg.addChild(rightLeg);
		bipedHead.addChild(head);
		bipedBody.addChild(body);
		bipedHeadwear.addChild(headwear);
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		leftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
		rightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
	}

	@Override
	public void setRotationAngles(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

	}
}
