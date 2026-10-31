
	/**
	 * Anchored standing, not kneeling: the shared kneel is replaced by the brace, feet set wide with the knees a little
	 * bent and both arms raised out to grip the jambs. Withdrawing (near a shunned visitor) the elder lets go of the
	 * walls and folds its arms across the doorway, head down: a closed door, never a blow. Speaking, the head lowers
	 * toward the visitor; the hands stay on the stone.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		float closed = entity.withdrawAmount(age - entity.tickCount);
		float hold = 1.0F - closed;
		float sway = Mth.sin(age * 0.025F);
		hips.y -= 5.4F;
		set(rightLeg, -0.14F, 0.0F, 0.17F);
		set(leftLeg, -0.14F, 0.0F, -0.17F);
		set(rightShin, 0.3F, 0.0F, 0.0F);
		set(leftShin, 0.3F, 0.0F, 0.0F);
		body.xRot = 0.08F + 0.01F * sway;
		set(rightArm, -0.3F, 0.0F, 1.25F + 0.01F * sway);
		set(leftArm, -0.3F, 0.0F, -1.25F - 0.01F * sway);
		set(rightForearm, 0.0F, 0.0F, 0.6F);
		set(leftForearm, 0.0F, 0.0F, -0.6F);
		if (closed > 0.0F) {
			toward(body, closed, 0.25F, 0.0F, 0.0F);
			toward(head, closed, 0.4F, head.yRot * 0.3F, 0.0F);
			toward(rightArm, closed, -0.7F, -0.2F, -0.2F);
			toward(leftArm, closed, -0.7F, 0.2F, 0.2F);
			toward(rightForearm, closed, -1.7F, 0.6F, 0.0F);
			toward(leftForearm, closed, -1.7F, -0.6F, 0.0F);
		}
		if (talk > 0.0F) head.xRot += talk * hold * (0.25F + 0.04F * Mth.sin(age * 0.3F));
		// The grip strands splay into the wall while the elder holds and draw in as it lets go.
		float splay = 0.3F * hold;
		rightGrip0.zRot -= splay;
		rightGrip2.zRot -= splay;
		leftGrip0.zRot += splay;
		leftGrip2.zRot += splay;
		hairFall.xRot = 0.1F - head.xRot * 0.5F;
		mantle.xRot = 0.07F - body.xRot * 0.6F;
	}
