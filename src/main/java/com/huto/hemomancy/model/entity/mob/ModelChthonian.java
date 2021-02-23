package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityChthonian;
import com.huto.hemomancy.model.animation.AnimatedEntityModel;
import com.huto.hemomancy.model.animation.AnimatedModelRenderer;
import com.huto.hemomancy.model.animation.ModelAnimator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class ModelChthonian extends AnimatedEntityModel<EntityChthonian> {
	private final AnimatedModelRenderer whole;
	private final AnimatedModelRenderer abdomen;
	private final AnimatedModelRenderer thorax;
	private final AnimatedModelRenderer rLegs;
	private final AnimatedModelRenderer rFrontLeg;
	private final AnimatedModelRenderer rFrontLeg2;
	private final AnimatedModelRenderer rFrontLeg3;
	private final AnimatedModelRenderer rFrontLeg4;
	private final AnimatedModelRenderer rMidLeg;
	private final AnimatedModelRenderer rMidLeg2;
	private final AnimatedModelRenderer rMidLeg3;
	private final AnimatedModelRenderer rMidLeg4;
	private final AnimatedModelRenderer rBackLeg;
	private final AnimatedModelRenderer rBackLeg2;
	private final AnimatedModelRenderer rBackLeg3;
	private final AnimatedModelRenderer rBackLeg4;
	private final AnimatedModelRenderer lLegs;
	private final AnimatedModelRenderer lFrontLeg;
	private final AnimatedModelRenderer lFrontLeg2;
	private final AnimatedModelRenderer lFrontLeg3;
	private final AnimatedModelRenderer lFrontLeg4;
	private final AnimatedModelRenderer lMidLeg;
	private final AnimatedModelRenderer lMidLeg2;
	private final AnimatedModelRenderer lMidLeg3;
	private final AnimatedModelRenderer lMidLeg4;
	private final AnimatedModelRenderer lBackLeg;
	private final AnimatedModelRenderer lBackLeg2;
	private final AnimatedModelRenderer lBackLeg3;
	private final AnimatedModelRenderer lBackLeg4;
	private final AnimatedModelRenderer skull;
	private final AnimatedModelRenderer lEye;
	private final AnimatedModelRenderer rEye;
	private final AnimatedModelRenderer rMandible;
	private final AnimatedModelRenderer lMandible;
	public ModelAnimator animator;
	public final AnimatedModelRenderer[] headArray;

	public ModelChthonian() {
		textureWidth = 64;
		textureHeight = 64;

		whole = new AnimatedModelRenderer(this);
		whole.setRotationPoint(0.5F, 19.5F, -4.0F);

		abdomen = new AnimatedModelRenderer(this);
		abdomen.setRotationPoint(-0.5F, 0.0F, 7.5F);
		whole.addChild(abdomen);
		abdomen.setTextureOffset(0, 17).addBox(-1.5F, -1.45F, 5.0F, 3.0F, 4.0F, 2.0F, 0.0F, false);
		abdomen.setTextureOffset(0, 9).addBox(-2.0F, -2.35F, 2.0F, 4.0F, 5.0F, 3.0F, 0.0F, false);
		abdomen.setTextureOffset(0, 0).addBox(-2.5F, -3.25F, 0.0F, 5.0F, 6.0F, 3.0F, 0.0F, false);

		thorax = new AnimatedModelRenderer(this);
		thorax.setRotationPoint(0.0F, 0.0F, 2.0F);
		whole.addChild(thorax);
		thorax.setTextureOffset(11, 14).addBox(-2.5F, -2.25F, -1.5F, 4.0F, 4.0F, 3.0F, 0.0F, false);
		thorax.setTextureOffset(14, 25).addBox(-1.5F, -2.75F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(22, 6).addBox(-1.5F, 1.25F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(13, 6).addBox(-2.0F, -1.5F, 2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		thorax.setTextureOffset(0, 24).addBox(-1.5F, -2.0F, 2.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(22, 12).addBox(-1.5F, 1.0F, 2.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(17, 29).addBox(-1.0F, -0.25F, -2.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(0, 29).addBox(-1.0F, -0.25F, 1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		thorax.setTextureOffset(28, 3).addBox(-1.0F, -0.25F, 4.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		rLegs = new AnimatedModelRenderer(this);
		rLegs.setRotationPoint(-2.0F, -1.0F, 2.0F);
		thorax.addChild(rLegs);

		rFrontLeg = new AnimatedModelRenderer(this);
		rFrontLeg.setRotationPoint(-0.25F, 0.5F, -2.5F);
		rLegs.addChild(rFrontLeg);
		setRotationAngle(rFrontLeg, 0.0F, 0.0F, 0.2618F);
		rFrontLeg.setTextureOffset(28, 12).addBox(-1.5559F, 0.2244F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		rFrontLeg2 = new AnimatedModelRenderer(this);
		rFrontLeg2.setRotationPoint(-1.75F, 0.0F, 0.0F);
		rFrontLeg.addChild(rFrontLeg2);
		setRotationAngle(rFrontLeg2, 0.0F, 0.0F, -0.7854F);
		rFrontLeg2.setTextureOffset(25, 17).addBox(-3.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rFrontLeg3 = new AnimatedModelRenderer(this);
		rFrontLeg3.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rFrontLeg2.addChild(rFrontLeg3);
		setRotationAngle(rFrontLeg3, 0.0F, -0.48F, 0.0F);
		rFrontLeg3.setTextureOffset(25, 9).addBox(-3.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rFrontLeg4 = new AnimatedModelRenderer(this);
		rFrontLeg4.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rFrontLeg3.addChild(rFrontLeg4);
		rFrontLeg4.setTextureOffset(0, 23).addBox(-3.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		rMidLeg = new AnimatedModelRenderer(this);
		rMidLeg.setRotationPoint(-0.25F, 0.5F, -0.5F);
		rLegs.addChild(rMidLeg);
		setRotationAngle(rMidLeg, 0.0F, 0.0F, 0.2618F);
		rMidLeg.setTextureOffset(28, 6).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		rMidLeg2 = new AnimatedModelRenderer(this);
		rMidLeg2.setRotationPoint(-1.75F, 0.0F, 0.0F);
		rMidLeg.addChild(rMidLeg2);
		setRotationAngle(rMidLeg2, 0.0F, 0.0F, -0.7854F);
		rMidLeg2.setTextureOffset(25, 0).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rMidLeg3 = new AnimatedModelRenderer(this);
		rMidLeg3.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rMidLeg2.addChild(rMidLeg3);
		setRotationAngle(rMidLeg3, 0.0F, -0.0873F, 0.0F);
		rMidLeg3.setTextureOffset(20, 25).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rMidLeg4 = new AnimatedModelRenderer(this);
		rMidLeg4.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rMidLeg3.addChild(rMidLeg4);
		rMidLeg4.setTextureOffset(16, 22).addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		rBackLeg = new AnimatedModelRenderer(this);
		rBackLeg.setRotationPoint(-0.25F, 0.5F, 2.25F);
		rLegs.addChild(rBackLeg);
		setRotationAngle(rBackLeg, 0.0F, 0.0F, 0.2618F);
		rBackLeg.setTextureOffset(7, 26).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		rBackLeg2 = new AnimatedModelRenderer(this);
		rBackLeg2.setRotationPoint(-1.75F, 0.0F, 0.0F);
		rBackLeg.addChild(rBackLeg2);
		setRotationAngle(rBackLeg2, 0.0F, 0.0F, -0.7854F);
		rBackLeg2.setTextureOffset(22, 15).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rBackLeg3 = new AnimatedModelRenderer(this);
		rBackLeg3.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rBackLeg2.addChild(rBackLeg3);
		setRotationAngle(rBackLeg3, 0.0F, 0.2618F, 0.0F);
		rBackLeg3.setTextureOffset(14, 12).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		rBackLeg4 = new AnimatedModelRenderer(this);
		rBackLeg4.setRotationPoint(-3.0F, 0.0F, 0.0F);
		rBackLeg3.addChild(rBackLeg4);
		rBackLeg4.setTextureOffset(16, 21).addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		lLegs = new AnimatedModelRenderer(this);
		lLegs.setRotationPoint(1.5F, -1.0F, 2.0F);
		thorax.addChild(lLegs);

		lFrontLeg = new AnimatedModelRenderer(this);
		lFrontLeg.setRotationPoint(-0.5F, 0.5F, -2.45F);
		lLegs.addChild(lFrontLeg);
		setRotationAngle(lFrontLeg, 0.0F, 0.0F, -0.2618F);
		lFrontLeg.setTextureOffset(21, 29).addBox(-0.1941F, 0.2244F, -0.55F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		lFrontLeg2 = new AnimatedModelRenderer(this);
		lFrontLeg2.setRotationPoint(2.0F, 0.0F, -0.05F);
		lFrontLeg.addChild(lFrontLeg2);
		setRotationAngle(lFrontLeg2, 0.0F, 0.0F, 0.7854F);
		lFrontLeg2.setTextureOffset(25, 21).addBox(0.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lFrontLeg3 = new AnimatedModelRenderer(this);
		lFrontLeg3.setRotationPoint(3.0F, 0.0F, 0.0F);
		lFrontLeg2.addChild(lFrontLeg3);
		setRotationAngle(lFrontLeg3, 0.0F, 0.48F, 0.0F);
		lFrontLeg3.setTextureOffset(25, 19).addBox(0.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lFrontLeg4 = new AnimatedModelRenderer(this);
		lFrontLeg4.setRotationPoint(3.0F, 0.0F, 0.0F);
		lFrontLeg3.addChild(lFrontLeg4);
		lFrontLeg4.setTextureOffset(16, 23).addBox(0.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		lMidLeg = new AnimatedModelRenderer(this);
		lMidLeg.setRotationPoint(-0.5F, 0.5F, -0.45F);
		lLegs.addChild(lMidLeg);
		setRotationAngle(lMidLeg, 0.0F, 0.0F, -0.2618F);
		lMidLeg.setTextureOffset(29, 14).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		lMidLeg2 = new AnimatedModelRenderer(this);
		lMidLeg2.setRotationPoint(2.0F, 0.0F, -0.05F);
		lMidLeg.addChild(lMidLeg2);
		setRotationAngle(lMidLeg2, 0.0F, 0.0F, 0.7854F);
		lMidLeg2.setTextureOffset(21, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lMidLeg3 = new AnimatedModelRenderer(this);
		lMidLeg3.setRotationPoint(3.0F, 0.0F, 0.0F);
		lMidLeg2.addChild(lMidLeg3);
		setRotationAngle(lMidLeg3, 0.0F, 0.0873F, 0.0F);
		lMidLeg3.setTextureOffset(0, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lMidLeg4 = new AnimatedModelRenderer(this);
		lMidLeg4.setRotationPoint(3.0F, 0.0F, 0.0F);
		lMidLeg3.addChild(lMidLeg4);
		lMidLeg4.setTextureOffset(24, 2).addBox(0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		lBackLeg = new AnimatedModelRenderer(this);
		lBackLeg.setRotationPoint(-0.5F, 0.5F, 2.3F);
		lLegs.addChild(lBackLeg);
		setRotationAngle(lBackLeg, 0.0F, 0.0F, -0.2618F);
		lBackLeg.setTextureOffset(6, 30).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F, 0.0F, false);

		lBackLeg2 = new AnimatedModelRenderer(this);
		lBackLeg2.setRotationPoint(2.0F, 0.0F, -0.05F);
		lBackLeg.addChild(lBackLeg2);
		setRotationAngle(lBackLeg2, 0.0F, 0.0F, 0.7854F);
		lBackLeg2.setTextureOffset(7, 28).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lBackLeg3 = new AnimatedModelRenderer(this);
		lBackLeg3.setRotationPoint(3.0F, 0.0F, 0.0F);
		lBackLeg2.addChild(lBackLeg3);
		setRotationAngle(lBackLeg3, 0.0F, -0.2618F, 0.0F);
		lBackLeg3.setTextureOffset(27, 24).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		lBackLeg4 = new AnimatedModelRenderer(this);
		lBackLeg4.setRotationPoint(3.0F, 0.0F, 0.0F);
		lBackLeg3.addChild(lBackLeg4);
		lBackLeg4.setTextureOffset(24, 11).addBox(0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, 0.0F, false);

		skull = new AnimatedModelRenderer(this);
		skull.setRotationPoint(-0.5F, 0.0F, 0.0F);
		whole.addChild(skull);
		skull.setTextureOffset(16, 0).addBox(-1.5F, -1.25F, -3.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);
		skull.setTextureOffset(6, 21).addBox(-0.5F, -2.0F, -4.0F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		skull.setTextureOffset(13, 0).addBox(-0.5F, -1.0F, -4.5F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		lEye = new AnimatedModelRenderer(this);
		lEye.setRotationPoint(1.5F, -1.0F, -1.5F);
		skull.addChild(lEye);
		setRotationAngle(lEye, 0.3054F, 0.0F, 0.0F);
		lEye.setTextureOffset(13, 28).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		rEye = new AnimatedModelRenderer(this);
		rEye.setRotationPoint(-1.5F, -1.0F, -1.5F);
		skull.addChild(rEye);
		setRotationAngle(rEye, 0.3054F, 0.0F, 0.0F);
		rEye.setTextureOffset(27, 27).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F, 0.0F, false);

		rMandible = new AnimatedModelRenderer(this);
		rMandible.setRotationPoint(-1.5F, 1.125F, -1.75F);
		skull.addChild(rMandible);
		rMandible.setTextureOffset(20, 21).addBox(-1.0F, -0.375F, -3.25F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		rMandible.setTextureOffset(26, 30).addBox(-0.5F, -0.625F, -3.75F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		lMandible = new AnimatedModelRenderer(this);
		lMandible.setRotationPoint(1.5F, 1.2F, -2.25F);
		skull.addChild(lMandible);
		lMandible.setTextureOffset(12, 21).addBox(0.0F, -0.45F, -2.75F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		lMandible.setTextureOffset(6, 23).addBox(-0.5F, -0.7F, -3.25F, 1.0F, 1.0F, 1.0F, 0.0F, false);

		animator = ModelAnimator.create();
		headArray = new AnimatedModelRenderer[] { skull };
		setDefaultPose();
	}

	@Override
	public void setRotationAngles(EntityChthonian entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(AnimatedModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}