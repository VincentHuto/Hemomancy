
	/**
	 * Anchored: the shared kneel holds the body. The face lifts a little toward the light while the arbor is open, the
	 * frond shoots in the hair sway, and the hair fall settles back against the shoulders.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		float open = 1.0F - entity.withdrawAmount(age - entity.tickCount);
		head.xRot -= 0.2F * open;
		shoot0.zRot += 0.08F * Mth.sin(age * 0.05F);
		shoot1.xRot += 0.06F * Mth.sin(age * 0.045F + 1.3F);
		shoot2.zRot -= 0.08F * Mth.sin(age * 0.05F + 2.1F);
		hairFall.xRot = 0.14F - head.xRot * 0.6F;
		// Speaking, an arbor's open hand turns up toward the visitor.
		if (talk > 0.0F) toward(leftForearm, talk * open, -1.2F, 0.0F, 0.0F);
	}
