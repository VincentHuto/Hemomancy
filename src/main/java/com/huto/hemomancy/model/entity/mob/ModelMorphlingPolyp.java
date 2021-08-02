package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityMorphlingPolyp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelMorphlingPolyp extends EntityModel<EntityMorphlingPolyp> {
	private final ModelPart bone;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart bone2;
	private final ModelPart bone9;
	private final ModelPart bone17;
	private final ModelPart bone10;
	private final ModelPart bone11;
	private final ModelPart bone12;
	private final ModelPart bone13;
	private final ModelPart bone16;
	private final ModelPart bone15;
	private final ModelPart eye;
	private final ModelPart eye2;
	private final ModelPart eye3;
	private final ModelPart eye7;
	private final ModelPart eye12;
	private final ModelPart eye8;
	private final ModelPart eye9;
	private final ModelPart eye13;
	private final ModelPart eye10;
	private final ModelPart eye11;
	private final ModelPart eye4;
	private final ModelPart eye5;
	private final ModelPart eye6;
	private final ModelPart bone14;

	public ModelMorphlingPolyp() {
		texWidth = 128;
		texHeight = 128;

		bone = new ModelPart(this);
		bone.setPos(0.0F, 25.0F, 0.0F);

		bone3 = new ModelPart(this);
		bone3.setPos(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.texOffs(56, 70).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new ModelPart(this);
		bone4.setPos(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.texOffs(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new ModelPart(this);
		bone5.setPos(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.texOffs(32, 78).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new ModelPart(this);
		bone6.setPos(1.4F, -5.0F, 5.35F);
		bone.addChild(bone6);
		bone6.texOffs(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new ModelPart(this);
		bone7.setPos(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.texOffs(32, 62).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new ModelPart(this);
		bone8.setPos(-3.6F, -7.55F, -1.25F);
		bone.addChild(bone8);
		bone8.texOffs(26, 29).addBox(-3.5F, -4.25F, -2.5F, 5.0F, 4.0F, 6.0F, 0.0F, false);

		bone2 = new ModelPart(this);
		bone2.setPos(2.4F, -3.3F, -4.75F);
		bone.addChild(bone2);
		bone2.texOffs(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new ModelPart(this);
		bone9.setPos(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.texOffs(58, 54).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone17 = new ModelPart(this);
		bone17.setPos(-2.6F, -6.75F, 7.85F);
		bone.addChild(bone17);
		bone17.texOffs(58, 54).addBox(-2.5F, -3.5F, -3.0F, 5.0F, 7.0F, 6.0F, 0.0F, false);

		bone10 = new ModelPart(this);
		bone10.setPos(-6.1F, -4.55F, 2.55F);
		bone.addChild(bone10);
		bone10.texOffs(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new ModelPart(this);
		bone11.setPos(3.65F, -5.0F, -2.35F);
		bone.addChild(bone11);
		bone11.texOffs(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new ModelPart(this);
		bone12.setPos(3.4F, -4.0F, 3.35F);
		bone.addChild(bone12);
		bone12.texOffs(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new ModelPart(this);
		bone13.setPos(-0.1F, -4.35F, 5.85F);
		bone.addChild(bone13);
		bone13.texOffs(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new ModelPart(this);
		bone16.setPos(-3.85F, -3.3F, -2.4F);
		bone.addChild(bone16);
		bone16.texOffs(0, 71).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new ModelPart(this);
		bone15.setPos(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.texOffs(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new ModelPart(this);
		eye.setPos(1.0F, -12.0F, -1.5F);
		bone.addChild(eye);
		eye.texOffs(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new ModelPart(this);
		eye2.setPos(-7.5F, -10.0F, 4.5F);
		bone.addChild(eye2);
		eye2.texOffs(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new ModelPart(this);
		eye3.setPos(-0.75F, -11.0F, 4.5F);
		bone.addChild(eye3);
		eye3.texOffs(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new ModelPart(this);
		eye7.setPos(-4.75F, -5.0F, 10.5F);
		bone.addChild(eye7);
		eye7.texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new ModelPart(this);
		eye12.setPos(5.25F, -8.25F, 9.5F);
		bone.addChild(eye12);
		eye12.texOffs(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new ModelPart(this);
		eye8.setPos(-2.75F, -3.0F, -6.5F);
		bone.addChild(eye8);
		eye8.texOffs(76, 16).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new ModelPart(this);
		eye9.setPos(8.0F, -3.0F, -6.5F);
		bone.addChild(eye9);
		eye9.texOffs(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new ModelPart(this);
		eye13.setPos(8.0F, -4.0F, 5.5F);
		bone.addChild(eye13);
		eye13.texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new ModelPart(this);
		eye10.setPos(-6.75F, -5.0F, -5.5F);
		bone.addChild(eye10);
		eye10.texOffs(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new ModelPart(this);
		eye11.setPos(-9.75F, -3.0F, 8.5F);
		bone.addChild(eye11);
		eye11.texOffs(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new ModelPart(this);
		eye4.setPos(7.25F, -10.0F, 4.5F);
		bone.addChild(eye4);
		eye4.texOffs(80, 70).addBox(-3.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new ModelPart(this);
		eye5.setPos(7.25F, -10.0F, -4.5F);
		bone.addChild(eye5);
		eye5.texOffs(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new ModelPart(this);
		eye6.setPos(-3.75F, -10.0F, -7.5F);
		bone.addChild(eye6);
		eye6.texOffs(76, 22).addBox(-1.5F, -2.3F, 1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		bone14 = new ModelPart(this);
		bone14.setPos(-1.75F, -11.8F, 1.5F);
		bone.addChild(bone14);
		bone14.texOffs(76, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(EntityMorphlingPolyp entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void prepareMobModel(EntityMorphlingPolyp entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.tickCount + partialTick;
		this.bone.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone2.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone3.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone4.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone5.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone6.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone7.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone8.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone9.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone10.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone11.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone12.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone13.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.bone14.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone15.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.bone16.yRot = (float) (Math.cos((frame)) * 0.0325);

		this.eye.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye2.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye2.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye3.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye3.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye4.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye4.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye5.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye5.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye6.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye6.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye7.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye7.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye8.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye8.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye9.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye9.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye10.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye10.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye11.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye11.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye12.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye12.yRot = (float) (Math.cos((frame)) * 0.0325);
		this.eye13.zRot = (float) (Math.sin((frame)) * 0.0325);
		this.eye13.yRot = (float) (Math.cos((frame)) * 0.0325);

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