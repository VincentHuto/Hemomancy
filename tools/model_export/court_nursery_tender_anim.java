
	/**
	 * The tender moves like someone carrying something fragile: the net of nest cups swings a little behind the hip and
	 * the left hand drifts to steady it on the walk. In open water the shared breast stroke stays (this body still
	 * swims like a person) with the hair and the tail stub trailing. Speaking, the right hand opens toward the listener
	 * and the head tips, as if counting heads.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		boolean swimming = CourtHumanoidModel.swimming(entity);
		skirtFront.xRot = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F;
		hairTail.xRot = 0.24F - head.xRot * 0.6F - body.xRot + (swimming ? 0.9F : 0.04F * Mth.sin(age * 0.06F));
		net.zRot = -0.14F + 0.1F * Mth.cos(limbSwing * 0.6662F) * stride;
		tail.yRot = Mth.sin(age * (swimming ? 0.3F : 0.06F)) * (swimming ? 0.35F : 0.12F);
		if (!swimming) {
			// The left hand steadies the net on the hip.
			toward(leftArm, 0.6F * stride + 0.2F, -0.15F, 0.0F, -0.35F);
			toward(leftForearm, 0.6F * stride + 0.2F, -0.75F, 0.25F, 0.0F);
		}
		if (talk > 0.0F && !swimming) {
			head.zRot += talk * 0.08F * Mth.sin(age * 0.2F);
		}
	}
