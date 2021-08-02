package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class ModelSpikedShield extends Model {
	private final ModelPart shield3;
	private final ModelPart bone;
	private final ModelPart bone9;
	private final ModelPart bone10;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone11;
	private final ModelPart bone12;
	private final ModelPart bone8;

	public ModelSpikedShield() {
		super(RenderType::entitySolid);

		texWidth = 64;
		texHeight = 64;

		shield3 = new ModelPart(this);
		shield3.setPos(1.0F, 8.5F, 3.0F);
		shield3.texOffs(14, 23).addBox(-1.0F, -15.5F, -3.0F, 2.0F, 6.0F, 6.0F, 0.0F, false);
		shield3.texOffs(0, 13).addBox(-4.5F, -17.0F, -4.75F, 9.0F, 9.0F, 1.0F, 0.0F, false);
		shield3.texOffs(20, 13).addBox(-4.0F, -16.5F, -5.0F, 8.0F, 8.0F, 1.0F, 0.0F, false);
		shield3.texOffs(37, 31).addBox(-2.0F, -14.5F, -6.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		shield3.texOffs(37, 31).addBox(-1.0F, -13.5F, -6.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);
		shield3.texOffs(37, 31).addBox(0.0F, -13.5F, -8.5F, 0.0F, 2.0F, 2.0F, 0.0F, false);
		shield3.texOffs(37, 31).addBox(0.0F, -13.0F, -9.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		shield3.texOffs(37, 31).addBox(-1.0F, -12.5F, -8.5F, 2.0F, 0.0F, 2.0F, 0.0F, false);
		shield3.texOffs(0, 0).addBox(-6.0F, -18.5F, -4.0F, 12.0F, 12.0F, 1.0F, 0.0F, false);
		shield3.texOffs(0, 24).addBox(-7.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, 0.0F, false);
		shield3.texOffs(0, 24).addBox(6.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, 0.0F, false);
		shield3.texOffs(24, 23).addBox(-5.0F, -6.5F, -3.75F, 10.0F, 1.0F, 1.0F, 0.0F, false);
		shield3.texOffs(24, 23).addBox(-5.0F, -19.5F, -3.75F, 10.0F, 1.0F, 1.0F, 0.0F, false);

		bone = new ModelPart(this);
		bone.setPos(3.0F, -17.75F, -4.5833F);
		shield3.addChild(bone);
		setRotationAngle(bone, -0.2618F, 0.0F, 0.48F);
		bone.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone9 = new ModelPart(this);
		bone9.setPos(0.0F, -18.25F, -4.5833F);
		shield3.addChild(bone9);
		setRotationAngle(bone9, -0.2618F, 0.0F, 0.0F);
		bone9.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone9.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone9.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone10 = new ModelPart(this);
		bone10.setPos(0.0F, -6.75F, -4.5833F);
		shield3.addChild(bone10);
		setRotationAngle(bone10, -0.2618F, 0.0F, -3.1416F);
		bone10.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone10.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone10.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone2 = new ModelPart(this);
		bone2.setPos(-3.0F, -17.75F, -4.5833F);
		shield3.addChild(bone2);
		setRotationAngle(bone2, -0.2618F, 0.0F, -0.48F);
		bone2.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone2.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone2.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone3 = new ModelPart(this);
		bone3.setPos(-3.0F, -7.25F, -4.5833F);
		shield3.addChild(bone3);
		setRotationAngle(bone3, -0.2618F, 0.0F, -2.6616F);
		bone3.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone3.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone3.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone4 = new ModelPart(this);
		bone4.setPos(3.0F, -7.25F, -4.5833F);
		shield3.addChild(bone4);
		setRotationAngle(bone4, -0.2618F, 0.0F, 2.6616F);
		bone4.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone4.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone4.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone5 = new ModelPart(this);
		bone5.setPos(-5.0F, -16.25F, -4.5833F);
		shield3.addChild(bone5);
		setRotationAngle(bone5, -0.2618F, 0.0F, -1.0908F);
		bone5.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone5.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone5.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone6 = new ModelPart(this);
		bone6.setPos(5.0F, -16.25F, -4.5833F);
		shield3.addChild(bone6);
		setRotationAngle(bone6, -0.2618F, 0.0F, 1.0908F);
		bone6.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone6.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone6.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone7 = new ModelPart(this);
		bone7.setPos(5.0F, -8.25F, -4.5833F);
		shield3.addChild(bone7);
		setRotationAngle(bone7, -0.2618F, 0.0F, 2.0508F);
		bone7.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone7.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone7.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone11 = new ModelPart(this);
		bone11.setPos(5.5F, -12.25F, -4.5833F);
		shield3.addChild(bone11);
		setRotationAngle(bone11, -0.2618F, 0.0F, 1.5708F);
		bone11.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone11.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone11.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone12 = new ModelPart(this);
		bone12.setPos(-5.5F, -12.25F, -4.5833F);
		shield3.addChild(bone12);
		setRotationAngle(bone12, -0.2618F, 0.0F, -1.5708F);
		bone12.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone12.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone12.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);

		bone8 = new ModelPart(this);
		bone8.setPos(-5.0F, -8.25F, -4.5833F);
		shield3.addChild(bone8);
		setRotationAngle(bone8, -0.2618F, 0.0F, -2.0508F);
		bone8.texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bone8.texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		bone8.texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		shield3.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}