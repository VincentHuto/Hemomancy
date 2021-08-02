package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelIronSpike extends EntityModel<EntityIronSpike> {
	private final ModelPart pillar;

	public ModelIronSpike() {
		texWidth = 64;
		texHeight = 64;

		pillar = new ModelPart(this);
		pillar.setPos(0.0F, 24.0F, 0.0F);
		pillar.texOffs(18, 18).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
		pillar.texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 4.0F, 8.0F, 0.0F, false);
		pillar.texOffs(0, 21).addBox(-2.0F, -11.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
		pillar.texOffs(0, 0).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		pillar.texOffs(24, 0).addBox(-0.5F, -23.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		pillar.texOffs(28, 28).addBox(-0.5F, -10.0F, -3.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(16, 25).addBox(-0.5F, -13.0F, -2.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(22, 12).addBox(-0.5F, -13.0F, 1.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(0, 28).addBox(-0.5F, -6.0F, -4.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(12, 28).addBox(-0.5F, -10.0F, 2.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(24, 25).addBox(-0.5F, -6.0F, 3.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(8, 28).addBox(2.5F, -10.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(18, 12).addBox(1.5F, -13.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(0, 12).addBox(-2.5F, -13.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(26, 12).addBox(3.5F, -6.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(4, 28).addBox(-3.5F, -10.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(20, 25).addBox(-4.5F, -6.0F, -0.5F, 1.0F, 5.0F, 1.0F, 0.0F, false);
		pillar.texOffs(0, 12).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 3.0F, 6.0F, 0.0F, false);
		pillar.texOffs(24, 31).addBox(3.0F, -4.5F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(20, 31).addBox(4.0F, -4.5F, -4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(16, 31).addBox(4.0F, -4.5F, 3.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(30, 12).addBox(3.0F, -4.5F, 4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(28, 4).addBox(-4.0F, -4.5F, 4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(28, 0).addBox(-5.0F, -4.5F, 3.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(12, 21).addBox(-5.0F, -4.5F, -4.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		pillar.texOffs(0, 21).addBox(-4.0F, -4.5F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityIronSpike entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		pillar.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}