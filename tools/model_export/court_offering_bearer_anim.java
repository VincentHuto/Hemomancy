
	/**
	 * Both hands stay under the bowl whatever the legs do; walking only lifts and settles it with each step. Speaking,
	 * the bearer raises the bowl a little toward the visitor, the way it is raised at a vent mouth.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		tabard.xRot = -body.xRot * 0.5F;
		if (CourtHumanoidModel.swimming(entity)) return;
		float bob = 0.04F * Mth.sin(limbSwing * 1.3324F) * stride;
		float lift = 0.25F * talk;
		set(rightArm, -0.8F - lift + bob, 0.0F, -0.18F);
		set(leftArm, -0.8F - lift + bob, 0.0F, 0.18F);
		set(rightForearm, -0.6F, 0.35F, 0.0F);
		set(leftForearm, -0.6F, -0.35F, 0.0F);
		bowl.y = -5.0F - lift * 6.0F + bob * 10.0F;
	}
