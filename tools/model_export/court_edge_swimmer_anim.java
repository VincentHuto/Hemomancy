
	/**
	 * In open water the Edge-swimmer drops the shared breast stroke for its own: arms laid back along the flanks, legs
	 * together in one kick from the hip that runs on down the tail to the fluke, the lance held point first. On land it
	 * carries the lance low and point forward; striking a hostile, it drives the bill out from the shoulder. The tail
	 * sways behind the walk, and the loincloth panels follow the thighs.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		if (swimming) {
			float kick = Mth.sin(age * 0.42F) * (0.28F + 0.22F * stride);
			rightArm.xRot = 0.12F;
			leftArm.xRot = 0.12F;
			rightArm.zRot = 0.1F;
			leftArm.zRot = -0.1F;
			rightForearm.xRot = 0.0F;
			leftForearm.xRot = 0.0F;
			rightLeg.xRot = kick;
			leftLeg.xRot = kick;
			rightLeg.zRot = 0.04F;
			leftLeg.zRot = -0.04F;
			rightShin.xRot = 0.15F + 0.25F * Math.max(0.0F, kick);
			leftShin.xRot = 0.15F + 0.25F * Math.max(0.0F, kick);
			head.xRot = -1.05F;
			tail.xRot = 0.49F + Mth.sin(age * 0.42F - 0.9F) * 0.35F;
			tailTip.xRot = 0.14F + Mth.sin(age * 0.42F - 1.8F) * 0.45F;
			tail.yRot = 0.0F;
		} else {
			float sway = Mth.sin(age * 0.07F) * 0.18F + Mth.cos(limbSwing * 0.6662F) * stride * 0.3F;
			tail.yRot = sway;
			tailTip.yRot = sway * 0.8F;
			tail.xRot = 0.49F - 0.12F * stride;
			float step = Mth.cos(limbSwing * 0.6662F) * stride;
			rightArm.xRot = -0.2F + 0.1F * step;
			rightArm.zRot = 0.06F;
			rightForearm.xRot = -0.5F;
		}
		// Point first: along the body when swimming (past the head, like the bill), level and forward on land.
		lance.xRot = (swimming ? 0.0F : Mth.HALF_PI) - (rightArm.xRot + rightForearm.xRot);
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		crest.xRot = 0.04F * Mth.sin(age * 0.09F);
		float strike = entity.getAttackAnim(age - entity.tickCount);
		if (strike > 0.0F) {
			float drive = Mth.sin(strike * Mth.PI);
			toward(body, drive, swimming ? 0.0F : 0.25F, -0.3F, 0.0F);
			toward(rightArm, drive, -1.5F, 0.1F, 0.0F);
			toward(rightForearm, drive, -0.1F, 0.0F, 0.0F);
			lance.xRot = Mth.lerp(drive, lance.xRot, Mth.HALF_PI - (rightArm.xRot + rightForearm.xRot) - body.xRot);
		}
		if (talk > 0.0F && !swimming) {
			// Short, quick gestures with the free hand: a point out to the water, then back.
			float beat = Mth.sin(age * 0.45F);
			toward(leftArm, talk, -0.7F + 0.15F * beat, 0.25F, -0.2F);
			toward(leftForearm, talk, -0.35F, 0.0F, 0.0F);
		}
	}
