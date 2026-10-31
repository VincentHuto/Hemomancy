
	/**
	 * The pole stays upright in the right hand on the walk, so the slate weight hangs plumb and swings on its cord.
	 * Striking a hostile, the Tidewarden levels the pole and drives it forward from the waist; the topknot and skirt
	 * panels follow the head and thighs.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		topknot.xRot = -head.xRot * 0.2F + 0.03F * Mth.sin(age * 0.06F);
		boolean swimming = CourtHumanoidModel.swimming(entity);
		if (!swimming) {
			float step = Mth.cos(limbSwing * 0.6662F) * stride;
			rightArm.xRot = -0.25F + 0.12F * step;
			rightArm.yRot = 0.0F;
			rightArm.zRot = 0.08F;
			rightForearm.xRot = -0.6F;
			rightForearm.yRot = 0.0F;
		}
		pole.xRot = -(rightArm.xRot + rightForearm.xRot) - body.xRot;
		pole.zRot = -rightArm.zRot;
		weight.xRot = -pole.xRot * 0.15F + 0.06F * Mth.sin(age * 0.08F);
		weight.zRot = 0.05F * Mth.sin(age * 0.06F + 0.9F);
		float strike = entity.getAttackAnim(age - entity.tickCount);
		if (strike > 0.0F && !swimming) {
			// Level the pole, then drive it out: a thrust peaking a third of the way through the swing.
			float drive = Mth.sin(strike * Mth.PI);
			toward(body, drive, 0.2F, -0.35F, 0.0F);
			toward(rightArm, drive, -1.45F, 0.15F, 0.0F);
			toward(rightForearm, drive, -0.15F, 0.0F, 0.0F);
			toward(leftArm, drive, -1.1F, 0.55F, 0.0F);
			toward(leftForearm, drive, -0.6F, 0.0F, 0.0F);
			pole.xRot = Mth.lerp(drive, pole.xRot, -0.05F);
		}
		if (talk > 0.0F && !swimming) {
			// Speaking, the free left hand marks each point: short and flat, a promise counted out.
			float beat = Mth.sin(age * 0.3F);
			toward(leftArm, talk, -0.45F + 0.08F * beat, 0.0F, -0.1F);
			toward(leftForearm, talk, -0.85F, 0.0F, 0.0F);
		}
	}
