
	/**
	 * The gaff rides upright in the right hand on the walk, hook forward, swinging a little against the stride; in
	 * open water it trails along the stroke. The rope of hair and the coil at the hip follow the body. Speaking, the
	 * free left hand opens palm up and out, the line offered rather than pressed on anyone.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		hairRope.xRot = 0.18F - head.xRot * 0.6F - body.xRot + (swimming ? 1.0F : 0.05F * Mth.sin(age * 0.06F));
		hairRope.zRot = 0.06F * Mth.sin(age * 0.05F + 1.1F);
		coil.zRot = 0.18F + 0.12F * Mth.cos(limbSwing * 0.6662F) * stride;
		if (!swimming) {
			float step = Mth.cos(limbSwing * 0.6662F) * stride;
			rightArm.xRot = -0.2F + 0.15F * step;
			rightArm.yRot = 0.0F;
			rightArm.zRot = 0.06F;
			rightForearm.xRot = -0.55F;
			rightForearm.yRot = 0.0F;
			gaff.xRot = -(rightArm.xRot + rightForearm.xRot) - body.xRot + 0.12F;
		} else {
			gaff.xRot = 0.3F;
		}
		if (talk > 0.0F && !swimming) {
			float beat = Mth.sin(age * 0.3F);
			toward(leftArm, talk, -0.85F + 0.08F * beat, 0.0F, -0.25F);
			toward(leftForearm, talk, -0.55F, 0.0F, 0.0F);
		}
	}
