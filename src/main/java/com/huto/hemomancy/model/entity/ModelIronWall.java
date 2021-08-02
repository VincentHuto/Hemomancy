package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.blood.iron.EntityIronWall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelIronWall extends EntityModel<EntityIronWall> {
	private final ModelPart whole;

	public ModelIronWall() {
		texWidth = 128;
		texHeight = 128;

		whole = new ModelPart(this);
		whole.setPos(0.0F, 24.0F, 0.0F);
		whole.texOffs(0, 48).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(28, 72).addBox(-8.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.texOffs(54, 63).addBox(-8.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.texOffs(0, 16).addBox(-8.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(48, 32).addBox(8.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.texOffs(74, 43).addBox(10.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.texOffs(74, 11).addBox(10.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.texOffs(48, 48).addBox(8.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.texOffs(48, 16).addBox(-15.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.texOffs(56, 77).addBox(-14.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.texOffs(74, 27).addBox(-14.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);
		whole.texOffs(48, 0).addBox(-15.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
		whole.texOffs(24, 8).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(24, 40).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(0, 32).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(0, 66).addBox(0.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.texOffs(14, 80).addBox(1.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(70, 0).addBox(1.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(0, 80).addBox(1.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(38, 0).addBox(1.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(78, 55).addBox(-7.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(24, 0).addBox(-7.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(77, 62).addBox(-7.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(72, 77).addBox(-7.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		whole.texOffs(26, 58).addBox(0.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, 0.0F, false);
		whole.texOffs(0, 0).addBox(0.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		whole.texOffs(24, 24).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityIronWall entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}