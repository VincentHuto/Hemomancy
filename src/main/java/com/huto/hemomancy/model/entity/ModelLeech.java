package com.huto.hemomancy.model.entity;

import com.huto.hemomancy.entity.EntityLeech;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class ModelLeech extends EntityModel<EntityLeech> {
	private final ModelPart Head;
	private final ModelPart headTop;
	private final ModelPart Body;
	private final ModelPart bodyTop;
	private final ModelPart Tail;
	private final ModelPart tailTop;

	public ModelLeech() {
		texWidth = 16;
		texHeight = 16;

		Head = new ModelPart(this);
		Head.setPos(-0.5F, 24.0F, -2.0F);
		Head.texOffs(5, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		Head.texOffs(5, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.01F, 2.0F, 0.0F, false);

		headTop = new ModelPart(this);
		headTop.setPos(-0.5F, 24.0F, -2.0F);
		headTop.texOffs(11, 11).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);

		Body = new ModelPart(this);
		Body.setPos(-0.5F, 24.0F, 0.0F);
		Body.texOffs(0, 4).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Body.texOffs(0, 4).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.01F, 3.0F, 0.0F, false);

		bodyTop = new ModelPart(this);
		bodyTop.setPos(-0.5F, 24.0F, 0.0F);
		bodyTop.texOffs(8, 11).addBox(-0.5F, -2.0F, -2.0F, 1.0F, 0.5F, 3.0F, 0.0F, false);

		Tail = new ModelPart(this);
		Tail.setPos(-0.5F, 23.5F, 1.0F);
		Tail.texOffs(0, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		Tail.texOffs(0, 0).addBox(-1.5F, 0.5F, 0.0F, 3.0F, 0.01F, 3.0F, 0.0F, false);

		tailTop = new ModelPart(this);
		tailTop.setPos(-0.5F, 23.5F, 1.0F);
		tailTop.texOffs(10, 13).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 0.5F, 2.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityLeech entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.Head.xRot = headPitch * ((float) Math.PI / 180F) * .5F;
		this.Head.yRot = netHeadYaw * ((float) Math.PI / 180F) * .5F;
		this.headTop.xRot = headPitch * ((float) Math.PI / 180F) * .5F;
		this.headTop.yRot = netHeadYaw * ((float) Math.PI / 180F) * .5F;
		this.Tail.yRot = Mth.sin(limbSwing * 7.6662F) * .5F * limbSwingAmount;
		this.tailTop.yRot = Mth.sin(limbSwing * 7.6662F) * .5F * limbSwingAmount;
		this.Body.yRot = Mth.cos(limbSwing * 7.6662F + (float) Math.PI) * .5F * limbSwingAmount;
		this.bodyTop.yRot = Mth.cos(limbSwing * 7.6662F + (float) Math.PI) * .5F * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		Tail.render(matrixStack, buffer, packedLight, packedOverlay);
		headTop.render(matrixStack, buffer, packedLight, packedOverlay);
		bodyTop.render(matrixStack, buffer, packedLight, packedOverlay);
		tailTop.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}