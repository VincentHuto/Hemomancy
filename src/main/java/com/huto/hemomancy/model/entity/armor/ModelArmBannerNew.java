package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class ModelArmBannerNew extends Model {
	public final ModelPart leftShoulder;

	public ModelArmBannerNew() {
		super(RenderType::entitySolid);
		texWidth = 32;
		texHeight = 32;

		leftShoulder = new ModelPart(this);
		leftShoulder.setPos(5.25F, 1.75F, 0.0F);
		leftShoulder.texOffs(0, 0).addBox(-1.0F, -4.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, false);
		leftShoulder.texOffs(0, 7).addBox(3.0F, -2.0F, -2.5F, 1.0F, 3.0F, 5.0F, 0.0F, false);
		leftShoulder.texOffs(0, 15).addBox(4.0F, -3.75F, -2.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		leftShoulder.texOffs(7, 7).addBox(-1.0F, -5.0F, -1.5F, 4.0F, 1.0F, 3.0F, 0.0F, false);
		leftShoulder.texOffs(15, 0).addBox(-1.0F, -2.5F, 2.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		leftShoulder.texOffs(12, 11).addBox(-1.0F, -2.5F, -3.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		leftShoulder.texOffs(14, 16).addBox(-1.0F, -3.5F, -3.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
		leftShoulder.texOffs(6, 16).addBox(-1.0F, -3.5F, 2.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		leftShoulder.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}