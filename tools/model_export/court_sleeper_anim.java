
	/**
	 * Sealed and hung, not kneeling: the shared kneel is undone and the sleeper hangs plumb from its hook, arms crossed
	 * on the chest under the mantle, legs together and a little drawn, head bowed. One slow breath every ten seconds or
	 * so lifts the chest and the mantle, then nothing. It does not turn to look and does not speak with its hands.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		float breath = Mth.sin(age * 0.03F);
		float held = breath > 0.0F ? breath * breath : 0.0F;
		hips.y -= 6.0F;
		head.yRot *= 0.08F;
		set(head, 0.48F - 0.06F * held, head.yRot, 0.0F);
		set(body, 0.1F - 0.03F * held, 0.0F, 0.0F);
		set(rightArm, -0.52F, -0.42F, -0.14F);
		set(leftArm, -0.52F, 0.42F, 0.14F);
		set(rightForearm, -1.75F, 0.63F, 0.0F);
		set(leftForearm, -1.75F, -0.63F, 0.0F);
		set(rightLeg, -0.1F, 0.0F, 0.03F);
		set(leftLeg, -0.1F, 0.0F, -0.03F);
		set(rightShin, 0.2F, 0.0F, 0.0F);
		set(leftShin, 0.2F, 0.0F, 0.0F);
		mantle.xRot = -0.04F * held;
		hangCord.xRot = -body.xRot;
		root.zRot = 0.015F * Mth.sin(age * 0.011F);
	}
