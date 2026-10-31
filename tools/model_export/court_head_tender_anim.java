
	/**
	 * Slow and upright: the crook stays plumb in the left hand and its foot plants with the stride; the body leans back
	 * a touch, the stance of someone who keeps a door. Speaking keeps to the right hand, low and measured.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		tabard.xRot = -body.xRot * 0.6F + Math.max(0.0F, -Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.4F;
		boolean swimming = CourtHumanoidModel.swimming(entity);
		if (!swimming) {
			body.xRot -= 0.05F;
			float step = Mth.cos(limbSwing * 0.6662F) * stride;
			set(leftArm, -0.2F - 0.15F * step, 0.0F, -0.06F);
			set(leftForearm, -0.55F, 0.0F, 0.0F);
			if (talk > 0.0F) {
				toward(rightArm, talk, -0.35F, 0.05F, 0.1F);
				toward(rightForearm, talk, -0.75F - 0.08F * Mth.sin(age * 0.2F), 0.0F, 0.0F);
			}
		}
		crook.xRot = -(leftArm.xRot + leftForearm.xRot) - body.xRot;
		crook.zRot = -leftArm.zRot;
	}
