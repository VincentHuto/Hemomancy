package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityChitinite;
import com.hutoslib.client.model.AnimatedEntityModel;
import com.hutoslib.client.model.AnimatedModelRenderer;
import com.hutoslib.client.model.ModelAnimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.math.MathHelper;

public class ModelChitinite extends AnimatedEntityModel<EntityChitinite> {
	private final AnimatedModelRenderer whole;
	private final AnimatedModelRenderer shell;
	private final AnimatedModelRenderer plate4;
	private final AnimatedModelRenderer rLeg4;
	private final AnimatedModelRenderer rLeg;
	private final AnimatedModelRenderer lLeg5;
	private final AnimatedModelRenderer lLeg4;
	private final AnimatedModelRenderer plate5;
	private final AnimatedModelRenderer plate3;
	private final AnimatedModelRenderer rLeg3;
	private final AnimatedModelRenderer lLeg3;
	private final AnimatedModelRenderer plate2;
	private final AnimatedModelRenderer lLeg2;
	private final AnimatedModelRenderer rLeg5;
	private final AnimatedModelRenderer rLeg2;
	private final AnimatedModelRenderer lLeg;
	private final AnimatedModelRenderer plate;
	private final AnimatedModelRenderer head;
	private final AnimatedModelRenderer leftEye;
	private final AnimatedModelRenderer rightEye;
	public ModelAnimator animator;
	public final AnimatedModelRenderer[] headArray;

	public ModelChitinite() {
		textureWidth = 64;
		textureHeight = 64;

		whole = new AnimatedModelRenderer(this);
		whole.setRotationPoint(0.0F, 24.0F, 0.0F);

		shell = new AnimatedModelRenderer(this);
		shell.setRotationPoint(0.0F, -3.0F, 0.0F);
		whole.addChild(shell);

		plate4 = new AnimatedModelRenderer(this);
		plate4.setRotationPoint(0.0F, -0.7637F, 2.3841F);
		shell.addChild(plate4);
		setRotationAngle(plate4, -0.2182F, 0.0F, 0.0F);
		plate4.setTextureOffset(21, 12).addBox(-2.5F, 0.849F, -0.3726F, 5.0F, 2.0F, 3.0F, 0.0F, false);
		plate4.setTextureOffset(0, 5).addBox(-4.0F, -0.3232F, -1.0217F, 8.0F, 1.0F, 4.0F, 0.0F, false);
		plate4.setTextureOffset(24, 27).addBox(-2.0F, 0.7951F, -0.1097F, 4.0F, 1.0F, 2.0F, 0.0F, false);
		plate4.setTextureOffset(24, 30).addBox(3.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate4.setTextureOffset(12, 30).addBox(-4.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		rLeg4 = new AnimatedModelRenderer(this);
		rLeg4.setRotationPoint(-3.375F, 1.2069F, 2.4821F);
		plate4.addChild(rLeg4);
		setRotationAngle(rLeg4, 0.2182F, 0.0F, 0.0F);
		rLeg4.setTextureOffset(2, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		rLeg4.setTextureOffset(0, 5).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		rLeg = new AnimatedModelRenderer(this);
		rLeg.setRotationPoint(-3.875F, 1.5309F, 0.7202F);
		plate4.addChild(rLeg);
		setRotationAngle(rLeg, 0.2182F, 0.0F, 0.0F);
		rLeg.setTextureOffset(0, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		rLeg.setTextureOffset(16, 16).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		lLeg5 = new AnimatedModelRenderer(this);
		lLeg5.setRotationPoint(2.625F, 1.2069F, 2.4821F);
		plate4.addChild(lLeg5);
		setRotationAngle(lLeg5, 0.2182F, 0.0F, 0.0F);
		lLeg5.setTextureOffset(2, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		lLeg5.setTextureOffset(20, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		lLeg4 = new AnimatedModelRenderer(this);
		lLeg4.setRotationPoint(3.125F, 1.5797F, 0.731F);
		plate4.addChild(lLeg4);
		setRotationAngle(lLeg4, 0.2182F, 0.0F, 0.0F);
		lLeg4.setTextureOffset(16, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		lLeg4.setTextureOffset(20, 6).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		plate5 = new AnimatedModelRenderer(this);
		plate5.setRotationPoint(0.0F, 0.5851F, 1.8947F);
		plate4.addChild(plate5);
		setRotationAngle(plate5, -0.3054F, 0.0F, 0.0F);
		plate5.setTextureOffset(0, 22).addBox(-2.0F, -0.1149F, -0.0043F, 4.0F, 1.0F, 3.0F, 0.0F, false);
		plate5.setTextureOffset(20, 1).addBox(-3.0F, -0.3367F, -0.8838F, 6.0F, 1.0F, 4.0F, 0.0F, false);
		plate5.setTextureOffset(10, 24).addBox(-1.0F, 0.256F, -0.0315F, 2.0F, 1.0F, 4.0F, 0.0F, false);
		plate5.setTextureOffset(32, 22).addBox(2.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate5.setTextureOffset(32, 17).addBox(-3.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		plate3 = new AnimatedModelRenderer(this);
		plate3.setRotationPoint(0.0F, 0.4226F, -0.0225F);
		shell.addChild(plate3);
		setRotationAngle(plate3, -0.0436F, 0.0F, 0.0F);
		plate3.setTextureOffset(0, 15).addBox(-3.0F, -1.6226F, -1.9775F, 6.0F, 3.0F, 4.0F, 0.0F, false);
		plate3.setTextureOffset(12, 35).addBox(3.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate3.setTextureOffset(30, 31).addBox(-4.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate3.setTextureOffset(0, 0).addBox(-4.0F, -2.2445F, -2.0571F, 8.0F, 1.0F, 4.0F, 0.0F, false);
		plate3.setTextureOffset(18, 27).addBox(-4.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, 0.0F, false);
		plate3.setTextureOffset(0, 26).addBox(3.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, 0.0F, false);

		rLeg3 = new AnimatedModelRenderer(this);
		rLeg3.setRotationPoint(-3.625F, 0.3774F, 0.5225F);
		plate3.addChild(rLeg3);
		setRotationAngle(rLeg3, 0.0436F, 0.0F, 0.0F);
		rLeg3.setTextureOffset(0, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		rLeg3.setTextureOffset(0, 10).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		lLeg3 = new AnimatedModelRenderer(this);
		lLeg3.setRotationPoint(2.875F, 0.3774F, 0.5225F);
		plate3.addChild(lLeg3);
		setRotationAngle(lLeg3, 0.0436F, 0.0F, 0.0F);
		lLeg3.setTextureOffset(0, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		lLeg3.setTextureOffset(20, 11).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		plate2 = new AnimatedModelRenderer(this);
		plate2.setRotationPoint(0.0F, -0.7759F, -2.2109F);
		shell.addChild(plate2);
		setRotationAngle(plate2, 0.1745F, 0.0F, 0.0F);
		plate2.setTextureOffset(0, 10).addBox(-4.0F, -0.5197F, -3.1395F, 8.0F, 1.0F, 4.0F, 0.0F, false);
		plate2.setTextureOffset(16, 18).addBox(-3.0F, 0.2803F, -3.1395F, 6.0F, 2.0F, 4.0F, 0.0F, false);
		plate2.setTextureOffset(0, 32).addBox(3.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate2.setTextureOffset(6, 29).addBox(-4.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		lLeg2 = new AnimatedModelRenderer(this);
		lLeg2.setRotationPoint(3.125F, 1.625F, 0.2997F);
		plate2.addChild(lLeg2);
		setRotationAngle(lLeg2, -0.1745F, 0.0F, 0.0F);
		lLeg2.setTextureOffset(2, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		lLeg2.setTextureOffset(0, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		rLeg5 = new AnimatedModelRenderer(this);
		rLeg5.setRotationPoint(-3.375F, 1.2803F, -1.6395F);
		plate2.addChild(rLeg5);
		setRotationAngle(rLeg5, -0.1745F, 0.0F, 0.0F);
		rLeg5.setTextureOffset(0, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		rLeg5.setTextureOffset(0, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		rLeg2 = new AnimatedModelRenderer(this);
		rLeg2.setRotationPoint(-3.875F, 1.625F, 0.2997F);
		plate2.addChild(rLeg2);
		setRotationAngle(rLeg2, -0.1745F, 0.0F, 0.0F);
		rLeg2.setTextureOffset(2, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		rLeg2.setTextureOffset(0, 15).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		lLeg = new AnimatedModelRenderer(this);
		lLeg.setRotationPoint(2.625F, 1.2803F, -1.6395F);
		plate2.addChild(lLeg);
		setRotationAngle(lLeg, -0.1745F, 0.0F, 0.0F);
		lLeg.setTextureOffset(18, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, false);
		lLeg.setTextureOffset(6, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		plate = new AnimatedModelRenderer(this);
		plate.setRotationPoint(-0.5F, 0.727F, -2.5342F);
		plate2.addChild(plate);
		setRotationAngle(plate, 0.3491F, 0.0F, 0.0F);
		plate.setTextureOffset(20, 6).addBox(-2.5F, -0.7866F, -2.7514F, 6.0F, 1.0F, 4.0F, 0.0F, false);
		plate.setTextureOffset(6, 34).addBox(-2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		plate.setTextureOffset(18, 33).addBox(2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, 0.0F, false);

		head = new AnimatedModelRenderer(this);
		head.setRotationPoint(0.5F, 0.2301F, -0.668F);
		plate.addChild(head);
		head.setTextureOffset(18, 24).addBox(-2.5F, -0.2667F, -1.8333F, 5.0F, 1.0F, 2.0F, 0.0F, false);

		leftEye = new AnimatedModelRenderer(this);
		leftEye.setRotationPoint(2.75F, 0.0333F, -1.0833F);
		head.addChild(leftEye);
		leftEye.setTextureOffset(10, 26).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		rightEye = new AnimatedModelRenderer(this);
		rightEye.setRotationPoint(-2.75F, 0.0333F, -1.0833F);
		head.addChild(rightEye);
		rightEye.setTextureOffset(11, 22).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		animator = ModelAnimator.create();
		headArray = new AnimatedModelRenderer[] { plate };
		setDefaultPose();

	}

	@Override
	public void setLivingAnimations(EntityChitinite entityIn, float limbSwing, float limbSwingAmount,
			float partialTick) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.ticksExisted + partialTick;
		this.entity = entityIn;
		resetToDefaultPose();
		animator.update(entity, partialTick);
		if (animator.setAnimation(EntityChitinite.ROLLUP_ANIMATION)) {
			rollupAnim();
		}
		idle(frame, limbSwing, limbSwingAmount, partialTick);

	}

	public void idle(float frame, float limbSwing, float limbSwingAmount, float partialTick) {

		this.lLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.lLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.lLeg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.lLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.lLeg5.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

		this.rLeg5.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.rLeg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.rLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
	}

	private void rollupAnim() {
		animator.startKeyframe(96);
		animator.rotate(plate, (float) Math.toRadians(51.5) * 1.25f, 0, 0);
		animator.rotate(plate2, -(float) Math.toRadians(-38.5) * 1.5f, 0, 0);
		animator.rotate(plate4, -(float) Math.toRadians(55) * 1.1f, 0, 0);
		animator.rotate(plate5, -(float) Math.toRadians(50) * 1.1f, 0, 0);
		animator.move(whole, 0, 1, 0);
		animator.endKeyframe();
		animator.resetKeyframe(32);
	}

	@Override
	public void setRotationAngles(EntityChitinite entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {

		this.plate.rotateAngleX = headPitch * ((float) Math.PI / 180F);
		this.plate.rotateAngleY = netHeadYaw * 0.5f * ((float) Math.PI / 180F);

	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(AnimatedModelRenderer AnimatedModelRenderer, float x, float y, float z) {
		AnimatedModelRenderer.rotateAngleX = x;
		AnimatedModelRenderer.rotateAngleY = y;
		AnimatedModelRenderer.rotateAngleZ = z;
	}
}