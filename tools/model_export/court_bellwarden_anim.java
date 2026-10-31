
	/**
	 * Economy of movement: a shorter, even stride with the arms barely swinging, the left hand resting on the sealed
	 * lamp so it does not knock. The lamp and the tally cord hang plumb. Speaking, the right forearm comes up with the
	 * hand still, and the head does not move.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		rightLeg.xRot *= 0.75F;
		leftLeg.xRot *= 0.75F;
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		lamp.xRot = -hips.xRot + 0.04F * Mth.sin(age * 0.05F);
		lamp.zRot = 0.1F;
		tally.xRot = 0.03F * Mth.sin(age * 0.06F + 0.4F);
		if (!swimming && talk <= 0.0F) {
			rightArm.xRot *= 0.4F;
			leftArm.xRot = -0.1F;
			leftArm.zRot = 0.1F;
			leftForearm.xRot = -0.3F;
		}
		if (talk > 0.0F && !swimming) {
			head.xRot = Mth.lerp(talk, head.xRot, Math.min(head.xRot, 0.1F));
			toward(rightArm, talk, -0.7F, 0.0F, 0.1F);
			toward(rightForearm, talk, -1.2F, 0.0F, 0.0F);
		}
	}
