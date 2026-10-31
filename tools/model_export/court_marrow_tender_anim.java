
	/**
	 * A working walk: the hook carried point down in the right hand, the satchel swinging at the hip, the scapula guard
	 * riding the left shoulder. Speaking, the left hand comes out open and low, as if pointing at what needs doing,
	 * and the body leans in a little.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		satchel.zRot = 0.14F + 0.12F * Mth.cos(limbSwing * 0.6662F) * stride;
		hook.xRot = swimming ? 0.4F : -rightForearm.xRot * 0.3F;
		if (talk > 0.0F && !swimming) {
			float beat = Mth.sin(age * 0.3F);
			body.xRot += talk * 0.12F;
			toward(leftArm, talk, -0.75F + 0.08F * beat, 0.0F, -0.1F);
			toward(leftForearm, talk, -0.4F, 0.0F, 0.0F);
		}
	}
