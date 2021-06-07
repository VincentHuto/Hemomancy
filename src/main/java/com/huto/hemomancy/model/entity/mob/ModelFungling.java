package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityFungling;
import com.hutoslib.client.model.AnimatedEntityModel;
import com.hutoslib.client.model.AnimatedModelRenderer;
import com.hutoslib.client.model.ModelAnimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.math.MathHelper;

public class ModelFungling extends AnimatedEntityModel<EntityFungling> {
	private final AnimatedModelRenderer full;
	private final AnimatedModelRenderer core;
	private final AnimatedModelRenderer base;
	private final AnimatedModelRenderer cap;
	private final AnimatedModelRenderer mid;
	private final AnimatedModelRenderer eye;
	private final AnimatedModelRenderer tent1;
	private final AnimatedModelRenderer tent11;
	private final AnimatedModelRenderer tent12;
	private final AnimatedModelRenderer tent13;
	private final AnimatedModelRenderer tent2;
	private final AnimatedModelRenderer tent21;
	private final AnimatedModelRenderer tent22;
	private final AnimatedModelRenderer tent23;
	private final AnimatedModelRenderer tent3;
	private final AnimatedModelRenderer tent31;
	private final AnimatedModelRenderer tent32;
	private final AnimatedModelRenderer tent33;
	private final AnimatedModelRenderer tent4;
	private final AnimatedModelRenderer tent41;
	private final AnimatedModelRenderer tent42;
	private final AnimatedModelRenderer tent43;

	public ModelAnimator animator;
	public final AnimatedModelRenderer[] headArray;

	public ModelFungling() {
		textureWidth = 64;
		textureHeight = 64;
		full = new AnimatedModelRenderer(this);
		full.setRotationPoint(0.0F, 22.0F, 0.0F);

		core = new AnimatedModelRenderer(this);
		core.setRotationPoint(-1.0F, -6.0F, -1.0F);
		full.addChild(core);

		base = new AnimatedModelRenderer(this);
		base.setRotationPoint(1.0F, 6.0F, 2.0F);
		core.addChild(base);
		base.setTextureOffset(16, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		base.setTextureOffset(13, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);

		cap = new AnimatedModelRenderer(this);
		cap.setRotationPoint(1.0F, 5.05F, 2.0F);
		core.addChild(cap);
		cap.setTextureOffset(0, 23).addBox(1.0F, -9.3F, 1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		cap.setTextureOffset(23, 23).addBox(1.0F, -9.3F, -3.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		cap.setTextureOffset(0, 0).addBox(-3.0F, -9.3F, 1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		cap.setTextureOffset(24, 0).addBox(-3.0F, -9.3F, -3.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		cap.setTextureOffset(0, 0).addBox(-4.0F, -14.3F, -4.0F, 8.0F, 7.0F, 8.0F, 0.0F, false);
		cap.setTextureOffset(0, 0).addBox(-3.0F, -13.3F, 4.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		cap.setTextureOffset(11, 6).addBox(-3.0F, -13.3F, -5.0F, 6.0F, 6.0F, 1.0F, 0.0F, false);
		cap.setTextureOffset(4, 8).addBox(-3.0F, -15.3F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
		cap.setTextureOffset(11, 2).addBox(4.0F, -13.3F, -3.0F, 1.0F, 5.0F, 6.0F, 0.0F, false);
		cap.setTextureOffset(14, 2).addBox(-5.0F, -13.3F, -3.0F, 1.0F, 6.0F, 6.0F, 0.0F, false);

		mid = new AnimatedModelRenderer(this);
		mid.setRotationPoint(1.0F, 5.1667F, 1.25F);
		core.addChild(mid);
		mid.setTextureOffset(8, 25).addBox(-1.5F, -2.1667F, -0.75F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		mid.setTextureOffset(0, 15).addBox(-2.0F, -6.1667F, -1.25F, 4.0F, 4.0F, 4.0F, 0.0F, false);
		mid.setTextureOffset(12, 15).addBox(-1.5F, -5.6667F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, false);

		eye = new AnimatedModelRenderer(this);
		eye.setRotationPoint(0.0F, -4.1667F, -1.825F);
		mid.addChild(eye);
		eye.setTextureOffset(16, 25).addBox(-1.0F, -1.0F, -0.475F, 2.0F, 2.0F, 1.0F, 0.0F, false);
		eye.setTextureOffset(0, 15).addBox(-0.5F, -0.5F, -0.525F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent1 = new AnimatedModelRenderer(this);
		tent1.setRotationPoint(0.75F, 1.25F, 0.0F);
		full.addChild(tent1);
		tent1.setTextureOffset(29, 21).addBox(0.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent11 = new AnimatedModelRenderer(this);
		tent11.setRotationPoint(1.75F, -0.25F, -0.75F);
		tent1.addChild(tent11);
		tent11.setTextureOffset(28, 29).addBox(0.0F, -0.5F, -0.75F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent12 = new AnimatedModelRenderer(this);
		tent12.setRotationPoint(2.0F, 0.25F, -0.5F);
		tent11.addChild(tent12);
		tent12.setTextureOffset(28, 18).addBox(0.0F, -0.25F, -0.75F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent13 = new AnimatedModelRenderer(this);
		tent13.setRotationPoint(2.0F, 0.5F, 0.0F);
		tent12.addChild(tent13);
		tent13.setTextureOffset(7, 30).addBox(0.0F, -0.25F, -0.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent2 = new AnimatedModelRenderer(this);
		tent2.setRotationPoint(-0.75F, 1.25F, 2.5F);
		full.addChild(tent2);
		tent2.setTextureOffset(24, 29).addBox(-1.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent21 = new AnimatedModelRenderer(this);
		tent21.setRotationPoint(-1.75F, -0.25F, 0.5F);
		tent2.addChild(tent21);
		tent21.setTextureOffset(24, 6).addBox(-2.0F, -0.5F, -0.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent22 = new AnimatedModelRenderer(this);
		tent22.setRotationPoint(-2.0F, 0.25F, -0.5F);
		tent21.addChild(tent22);
		tent22.setTextureOffset(6, 23).addBox(-2.0F, -0.25F, -0.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent23 = new AnimatedModelRenderer(this);
		tent23.setRotationPoint(-2.1F, 0.5F, 1.0F);
		tent22.addChild(tent23);
		tent23.setTextureOffset(22, 29).addBox(-0.8F, -0.25F, -0.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent3 = new AnimatedModelRenderer(this);
		tent3.setRotationPoint(0.375F, 0.75F, 2.625F);
		full.addChild(tent3);
		tent3.setTextureOffset(22, 21).addBox(-1.125F, -0.75F, 0.125F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent31 = new AnimatedModelRenderer(this);
		tent31.setRotationPoint(0.625F, 0.25F, 0.875F);
		tent3.addChild(tent31);
		tent31.setTextureOffset(18, 29).addBox(-0.25F, -0.5F, 0.25F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent32 = new AnimatedModelRenderer(this);
		tent32.setRotationPoint(0.125F, 0.75F, 2.125F);
		tent31.addChild(tent32);
		tent32.setTextureOffset(0, 29).addBox(0.125F, -0.75F, 0.125F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent33 = new AnimatedModelRenderer(this);
		tent33.setRotationPoint(1.125F, 0.25F, 2.125F);
		tent32.addChild(tent33);
		tent33.setTextureOffset(4, 29).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		tent4 = new AnimatedModelRenderer(this);
		tent4.setRotationPoint(-0.25F, 0.25F, -0.5F);
		full.addChild(tent4);
		tent4.setTextureOffset(0, 6).addBox(-1.5F, -0.25F, -1.25F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		tent41 = new AnimatedModelRenderer(this);
		tent41.setRotationPoint(-1.25F, 0.75F, -1.25F);
		tent4.addChild(tent41);
		tent41.setTextureOffset(28, 15).addBox(-0.75F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent42 = new AnimatedModelRenderer(this);
		tent42.setRotationPoint(-0.375F, 0.75F, -2.125F);
		tent41.addChild(tent42);
		tent42.setTextureOffset(14, 28).addBox(-0.875F, -0.75F, -1.875F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		tent43 = new AnimatedModelRenderer(this);
		tent43.setRotationPoint(-0.875F, 0.25F, -2.375F);
		tent42.addChild(tent43);
		tent43.setTextureOffset(0, 17).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		animator = ModelAnimator.create();
		headArray = new AnimatedModelRenderer[] { eye };
		setDefaultPose();
	}

	@Override
	public void setLivingAnimations(EntityFungling entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.ticksExisted + partialTick;
		this.entity = entityIn;
		resetToDefaultPose();
		animator.update(entity, partialTick);
		if (animator.setAnimation(EntityFungling.HEADBUTT_ANIMATION)) {
			headbuttAnim();
		} else if (animator.setAnimation(EntityFungling.SPOREPUFF_ANIMATION)) {
			sporePuffAnim();
		}
		idle(frame);

	}

	private void headbuttAnim() {
		animator.startKeyframe(4);
		animator.rotate(mid, (float) Math.toRadians(18.5), 0, 0);
		animator.rotate(cap, (float) Math.toRadians(18.5), 0, 0);
		animator.endKeyframe();
		animator.resetKeyframe(8);
	}

	private void sporePuffAnim() {
		animator.startKeyframe(4);
		animator.move(cap, 0, 1.5f, 0);
		animator.endKeyframe();
		animator.resetKeyframe(8);
	}

	@Override
	public void idle(float frame) {

		this.tent1.rotateAngleY = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
		this.tent11.rotateAngleY = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
		this.tent12.rotateAngleY = (float) Math.sin((frame) * -0.6f) * 0.15f + 25.5f;
		this.tent13.rotateAngleY = (float) Math.cos((frame) * -0.7f) * 0.25f + 25.5f;

		this.tent2.rotateAngleY = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
		this.tent21.rotateAngleY = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
		this.tent22.rotateAngleY = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
		this.tent23.rotateAngleY = (float) Math.cos((frame) * 0.7f) * 0.25f + 25.5f;

		this.tent3.rotateAngleY = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
		this.tent31.rotateAngleY = (float) Math.cos((frame) * -0.5f) * 0.1f + 25.7f;
		this.tent32.rotateAngleY = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
		this.tent33.rotateAngleY = (float) Math.cos((frame) * -0.7f) * 0.25f + 25.5f;

		this.tent4.rotateAngleY = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
		this.tent41.rotateAngleY = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
		this.tent42.rotateAngleY = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
		this.tent43.rotateAngleY = (float) Math.cos((frame) * 0.7f) * 0.25f + 25.5f;

	}

	@Override
	public void setRotationAngles(EntityFungling entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.eye.rotateAngleX = netHeadYaw * 0.003f;
		this.eye.rotateAngleY = headPitch * 0.015f;

		netHeadYaw = MathHelper.wrapDegrees(netHeadYaw);
		faceTarget(netHeadYaw, headPitch, 1, headArray);

	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		full.render(matrixStack, buffer, packedLight, packedOverlay);
	}

}