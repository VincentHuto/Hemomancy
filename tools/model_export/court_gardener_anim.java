
	/**
	 * Standing still and not speaking, the Gardener tends: every twelve seconds a stoop with both hands down to the
	 * beds, then back up. The skirt panels follow the thighs and the hat brim lags the head a little.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		hat.zRot = 0.04F * Mth.sin(age * 0.05F);
		if (entity.role().anchored() || CourtHumanoidModel.swimming(entity)) return;
		float cycle = (age % 240.0F) / 240.0F;
		float stoop = Mth.clamp((1.0F - stride * 4.0F) * (1.0F - talk), 0.0F, 1.0F)
				* window(cycle, 0.55F, 0.62F, 0.85F, 0.93F);
		if (stoop <= 0.0F) return;
		toward(body, stoop, 0.45F, 0.0F, 0.0F);
		toward(head, stoop, 0.35F, head.yRot * 0.4F, 0.0F);
		float pick = 0.08F * Mth.sin(age * 0.4F);
		toward(rightArm, stoop, -0.75F + pick, 0.0F, 0.1F);
		toward(leftArm, stoop, -0.7F - pick, 0.0F, -0.1F);
		toward(rightForearm, stoop, -0.45F, 0.0F, 0.0F);
		toward(leftForearm, stoop, -0.4F, 0.0F, 0.0F);
	}
