package com.huto.hemomancy.model.entity.armor;

import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelDarkArrowVertical extends EntityModel<EntityBloodShot> {
	private final ModelPart bone;

	public ModelDarkArrowVertical() {
		texWidth = 16;
		texHeight = 16;

		bone = new ModelPart(this);
		bone.setPos(7.75F, 28.0F, -9.0F);
		bone.texOffs(0, 13).addBox(-9.0F, -5.0F, 1.0F, 2.0F, 1.0F, 13.0F, 0.0F, false);
		bone.texOffs(0, 1).addBox(-9.5F, -5.0F, 0.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
		bone.texOffs(0, 1).addBox(-9.0F, -5.0F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		bone.texOffs(0, 1).addBox(-8.5F, -5.0F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-7.0F, -5.0F, 15.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-10.0F, -5.0F, 15.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-11.0F, -5.0F, 17.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-6.0F, -5.0F, 17.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-8.0F, -5.0F, 14.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		bone.texOffs(0, 4).addBox(-9.0F, -5.0F, 14.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityBloodShot entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}