
	private ModelPart[] plumes;
	private ModelPart[] crowns;

	private void collect() {
		if (plumes != null) return;
		plumes = new ModelPart[] {crown0, crown1, crown2, crown3, crown4, crown5, back0, back1, back2, back3,
				rightPlume0, rightPlume1, leftPlume0, leftPlume1};
		crowns = new ModelPart[] {crown0Crown, crown1Crown, crown2Crown, crown3Crown, crown4Crown, crown5Crown,
				back0Crown, back1Crown, back2Crown, back3Crown, rightPlume0Crown, rightPlume1Crown, leftPlume0Crown,
				leftPlume1Crown};
	}

	/**
	 * Rooted, the elder never walks: the root mass holds, the torso breathes against the bone, and every plume sways on
	 * its stalk in a slow current, each a little behind the last, the feathery crowns opening and closing as they
	 * comb the water. The look turns the head only. Speaking, the head lifts toward the visitor and the arms come
	 * forward a little. Near a shunned visitor every plume draws in close and the arms fold over the chest.
	 */
	@Override
	public void setupAnim(CourtMemberEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		collect();
		root.getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		float talk = entity.talkAmount(partial);
		float closed = Math.max(entity.withdrawAmount(partial), entity.waryAmount(partial));
		float open = 1.0F - closed;
		float breathe = Mth.sin(ageInTicks * 0.05F);
		body.xRot += 0.03F * breathe + talk * 0.18F;
		head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.7F;
		head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F - talk * 0.2F + closed * 0.45F;
		rightArm.xRot += -talk * 0.3F + closed * 0.5F;
		leftArm.xRot += -talk * 0.3F + closed * 0.5F;
		rightArm.zRot += closed * 0.6F;
		leftArm.zRot -= closed * 0.6F;
		rightForearm.xRot += -closed * 0.9F;
		leftForearm.xRot += -closed * 0.9F;
		for (int i = 0; i < plumes.length; i++) {
			float sway = Mth.sin(ageInTicks * 0.045F + i * 0.55F);
			plumes[i].xRot = plumes[i].xRot * (1.0F - 0.7F * closed) + 0.12F * sway * open;
			plumes[i].zRot = plumes[i].zRot * (1.0F - 0.7F * closed) + 0.08F * Mth.sin(ageInTicks * 0.035F + i * 0.9F) * open;
			float comb = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.09F + i * 0.7F);
			float spread = (0.6F + 0.5F * comb) * open + 0.3F * closed;
			crowns[i].xScale = spread;
			crowns[i].zScale = spread;
		}
	}
