
	/**
	 * Hung in the network, not kneeling: the shared kneel is undone and the elder hangs from the line above it, both
	 * hands up on the line, legs loose and a little drawn, the bead chains drifting on the current. Speaking, it lets
	 * go: the arms come down and open toward the visitor while the chains settle. Withdrawing near a shunned visitor
	 * it draws in, knees up and arms round them, still hanging.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		float closed = entity.withdrawAmount(age - entity.tickCount);
		float hold = (1.0F - talk) * (1.0F - closed);
		float drift = Mth.sin(age * 0.04F);
		hips.y -= 6.0F;
		hips.y += 0.4F * drift;
		body.xRot = 0.04F + 0.03F * drift;
		set(rightLeg, -0.1F + 0.06F * drift, 0.0F, 0.06F);
		set(leftLeg, 0.06F - 0.06F * drift, 0.0F, -0.06F);
		set(rightShin, 0.3F, 0.0F, 0.0F);
		set(leftShin, 0.2F, 0.0F, 0.0F);
		// Hands up on the line.
		set(rightArm, -2.95F, 0.0F, -0.2F);
		set(leftArm, -2.95F, 0.0F, 0.2F);
		set(rightForearm, -0.2F, 0.0F, 0.0F);
		set(leftForearm, -0.2F, 0.0F, 0.0F);
		head.xRot = Math.min(head.xRot, 0.2F) - 0.2F * hold;
		if (talk > 0.0F) {
			float beat = Mth.sin(age * 0.3F);
			toward(rightArm, talk, -0.7F + 0.08F * beat, 0.1F, 0.1F);
			toward(leftArm, talk, -0.5F, -0.1F, -0.1F);
			toward(rightForearm, talk, -0.9F, 0.0F, 0.0F);
			toward(leftForearm, talk, -0.7F, 0.0F, 0.0F);
		}
		if (closed > 0.0F) {
			toward(rightLeg, closed, -1.2F, 0.0F, 0.1F);
			toward(leftLeg, closed, -1.2F, 0.0F, -0.1F);
			toward(rightShin, closed, 1.6F, 0.0F, 0.0F);
			toward(leftShin, closed, 1.6F, 0.0F, 0.0F);
			toward(rightArm, closed, -0.9F, -0.3F, -0.2F);
			toward(leftArm, closed, -0.9F, 0.3F, 0.2F);
			toward(rightForearm, closed, -1.3F, 0.5F, 0.0F);
			toward(leftForearm, closed, -1.3F, -0.5F, 0.0F);
			toward(head, closed, 0.5F, head.yRot * 0.3F, 0.0F);
		}
		ModelPart[] chains = {chain0, chain1, chain2, chain3, chain4, chain5};
		for (int i = 0; i < chains.length; i++) {
			chains[i].xRot = 0.2F - body.xRot + 0.12F * Mth.sin(age * 0.05F + i * 0.9F) + 0.25F * talk;
			chains[i].zRot = 0.1F * Mth.sin(age * 0.04F + i * 1.3F);
		}
		// The arm chains hang plumb whatever the arm does.
		chain4.xRot -= rightArm.xRot;
		chain5.xRot -= leftArm.xRot;
	}
