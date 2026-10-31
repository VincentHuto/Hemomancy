
	/**
	 * The staff stays upright in the right hand whatever the stance does, so the bloom pod hangs plumb and swings a
	 * little; speaking moves the free left hand instead. The robe's front and back panels follow the thighs.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.9F;
		tabard.xRot = -body.xRot * 0.5F;
		boolean swimming = CourtHumanoidModel.swimming(entity);
		if (!swimming) {
			float step = Mth.cos(limbSwing * 0.6662F) * stride;
			rightArm.xRot = -0.25F + 0.12F * step;
			rightArm.yRot = 0.0F;
			rightArm.zRot = 0.08F;
			rightForearm.xRot = -0.6F;
			rightForearm.yRot = 0.0F;
		}
		staff.xRot = -(rightArm.xRot + rightForearm.xRot) - body.xRot;
		staff.zRot = -rightArm.zRot;
		// The staff already cancels the arm, so the pod only swings on its cord.
		lamp.xRot = 0.06F * Mth.sin(age * 0.07F);
		lamp.zRot = 0.05F * Mth.sin(age * 0.05F + 0.7F);
		for (int i = 0; i < 5; i++) {
			crest(i).xRot = 0.05F * Mth.sin(age * 0.04F + i * 0.8F);
		}
		if (talk > 0.0F && !swimming) {
			float beat = Mth.sin(age * 0.35F);
			toward(leftArm, talk, -0.55F + 0.1F * beat, -0.1F, -0.15F);
			toward(leftForearm, talk, -0.95F - 0.15F * beat, 0.0F, 0.0F);
		}
	}

	private net.minecraft.client.model.geom.ModelPart crest(int index) {
		return switch (index) {
			case 0 -> crest0;
			case 1 -> crest1;
			case 2 -> crest2;
			case 3 -> crest3;
			default -> crest4;
		};
	}
