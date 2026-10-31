
	/**
	 * Moving, the resident drifts more than it walks: a short stride under the drape, sleeves hanging. Still for a
	 * while, it folds down into the drape (knees under it, head bowed into the hood, arms folded in the sleeves) while
	 * the renderer fades it nearly out of sight; it unfolds when spoken to. Speaking, one sleeve lifts a little and the
	 * face stays half turned away.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		float folded = entity.withdrawAmount(age - entity.tickCount);
		boolean swimming = CourtHumanoidModel.swimming(entity);
		rightLeg.xRot *= 0.6F;
		leftLeg.xRot *= 0.6F;
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.9F + (swimming ? 0.4F : 0.0F);
		drape.xRot = -body.xRot * 0.4F;
		rightSleeve.xRot = -rightForearm.xRot * 0.25F;
		leftSleeve.xRot = -leftForearm.xRot * 0.25F;
		if (talk > 0.0F && !swimming) {
			head.yRot += talk * 0.3F;
			toward(rightArm, talk, -0.45F, 0.0F, 0.1F);
			toward(rightForearm, talk, -0.6F, 0.0F, 0.0F);
		}
		if (folded > 0.0F) {
			hips.y += folded * 7.0F;
			toward(body, folded, 0.7F, 0.0F, 0.0F);
			toward(head, folded, 0.5F, head.yRot * 0.3F, 0.0F);
			toward(rightLeg, folded, -1.4F, 0.0F, 0.0F);
			toward(leftLeg, folded, -1.4F, 0.0F, 0.0F);
			toward(rightShin, folded, 2.6F, 0.0F, 0.0F);
			toward(leftShin, folded, 2.6F, 0.0F, 0.0F);
			toward(rightArm, folded, -0.7F, -0.35F, -0.2F);
			toward(leftArm, folded, -0.7F, 0.35F, 0.2F);
			toward(rightForearm, folded, -1.5F, 0.5F, 0.0F);
			toward(leftForearm, folded, -1.5F, -0.5F, 0.0F);
			skirtBack.xRot += folded * 0.9F;
		}
	}
