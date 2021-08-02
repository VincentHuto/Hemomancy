package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBloodArm extends HumanoidModel<Player> implements ArmedModel {

	public ModelPart head;
	public ModelPart headwear;
	public ModelPart body;
	public ModelPart rightArm;
	public ModelPart leftArm;
	public ModelPart rightLeg;
	public ModelPart leftLeg;

	public ModelBloodArm(float modelSizeIn) {
		super(modelSizeIn);

		head = new ModelPart(this, 0, 0);
		head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, modelSizeIn);
		head.setPos(0.0F, 0.0F, 0.0F);
		headwear = new ModelPart(this, 32, 0);
		headwear.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, modelSizeIn + 0.5F);
		headwear.setPos(0.0F, 0.0F, 0.0F);
		body = new ModelPart(this, 16, 16);
		body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, modelSizeIn);
		body.setPos(0.0F, 0.0F, 0.0F);
		rightArm = new ModelPart(this, 40, 16);
		rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		rightArm.setPos(-5.0F, 2.0F, 0.0F);
		leftArm = new ModelPart(this, 40, 16);
		leftArm.mirror = true;
		leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		leftArm.setPos(5.0F, 2.0F, 0.0F);
		rightLeg = new ModelPart(this, 0, 16);
		rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		leftLeg = new ModelPart(this, 0, 16);
		leftLeg.mirror = true;
		leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSizeIn);
		leftLeg.setPos(1.9F, 12.0F, 0.0F);

		leftArm.addChild(leftArm);
		leftLeg.addChild(leftLeg);
		rightArm.addChild(rightArm);
		rightLeg.addChild(rightLeg);
		head.addChild(head);
		body.addChild(body);
		hat.addChild(headwear);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		leftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
		rightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
	}

	@Override
	public void setupAnim(Player entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

	}
}
