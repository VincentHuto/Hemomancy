
	/**
	 * The hands stay folded at the waist on the walk, a host's stillness; the long robe's front panels part over the
	 * stride and the back panel trails. Speaking, the right hand leaves the left and opens a little, palm up, as if
	 * returning the question; the head stays level and does not lean in.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		robeFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.5F - body.xRot * 0.5F;
		robe.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.25F + (swimming ? 0.3F : 0.0F);
		rightSleeve.xRot = -rightForearm.xRot * 0.3F;
		leftSleeve.xRot = -leftForearm.xRot * 0.3F;
		if (!swimming && entity.waryAmount(age - entity.tickCount) <= 0.0F) {
			// Hands folded at the waist.
			set(rightArm, -0.45F, 0.3F, 0.0F);
			set(leftArm, -0.45F, -0.3F, 0.0F);
			set(rightForearm, -1.08F, 0.45F, 0.0F);
			set(leftForearm, -1.08F, -0.45F, 0.0F);
			if (talk > 0.0F) {
				float beat = Mth.sin(age * 0.25F);
				toward(rightArm, talk, -0.52F + 0.05F * beat, 0.0F, 0.14F);
				toward(rightForearm, talk, -0.87F, 0.0F, 0.0F);
			}
		}
	}
