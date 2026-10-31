
	/**
	 * The Warden over the shared kneel. Rooted, it kneels with its arms lifted toward the light and its root masses
	 * spread into the floor; uprooted, it stands stooped and lumbers on its torn roots. The opening tears it up out of the
	 * kneel. A root lash lifts both arms overhead and drives them down, a frond spear draws the right arm back and throws
	 * it forward, sowing sweeps both arms wide; re-anchoring sinks it back into the tier. Moves replay through the
	 * entity's BossReplayClock, the stance eases on the entity's client tick.
	 */
	@Override
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
		hairFall.xRot = 0.16F - head.xRot * 0.6F;
		ModelPart[] shoots = {shoot0, shoot1, shoot2, shoot3, shoot4};
		for (int i = 0; i < shoots.length; i++) shoots[i].zRot += 0.08F * Mth.sin(age * 0.05F + i * 1.1F);
		ModelPart[] crests = {crest0, crest1, crest2, crest3};
		for (int i = 0; i < crests.length; i++) crests[i].xRot += 0.06F * Mth.sin(age * 0.06F + i * 0.8F);
		if (!(entity instanceof com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity warden)) return;
		float partial = age - entity.tickCount;
		float intro = warden.introTime(partial);
		float up = intro >= 0.0F ? window(intro, 10.0F, 40.0F, 1.0E6F, 1.0E6F) : warden.uprootAmount(partial);
		float rooted = 1.0F - up;
		// Rooted: arms lifted to the light, the roots splayed into the floor.
		toward(rightArm, rooted, -2.5F, 0.0F, 0.35F);
		toward(leftArm, rooted, -2.5F, 0.0F, -0.35F);
		toward(rightForearm, rooted, -0.3F, 0.0F, 0.0F);
		toward(leftForearm, rooted, -0.3F, 0.0F, 0.0F);
		ModelPart[] roots = {rightRoot0, rightRoot1, rightRoot2, rightRoot3, leftRoot0, leftRoot1, leftRoot2, leftRoot3};
		for (int i = 0; i < roots.length; i++) {
			roots[i].xRot += rooted * 0.5F + up * 0.15F * Mth.sin(age * 0.2F + i);
		}
		if (up > 0.0F) stand(up, limbSwing, stride);
		float t = warden.moveTime(partial);
		switch (warden.move()) {
			case ROOT_LASH -> {
				float lift = window(t, 0.0F, 18.0F, 19.0F, 22.0F);
				float slam = window(t, 19.0F, 22.0F, 26.0F, 40.0F);
				toward(rightArm, lift, -3.0F, 0.0F, 0.2F);
				toward(leftArm, lift, -3.0F, 0.0F, -0.2F);
				toward(body, lift, -0.25F, 0.0F, 0.0F);
				toward(rightArm, slam, -0.2F, 0.0F, 0.5F);
				toward(leftArm, slam, -0.2F, 0.0F, -0.5F);
				toward(body, slam, 0.55F, 0.0F, 0.0F);
				head.xRot += slam * 0.3F;
			}
			case FROND_SPEAR -> {
				float draw = window(t, 0.0F, 20.0F, 24.0F, 26.0F);
				float throwing = window(t, 23.0F, 27.0F, 32.0F, 48.0F);
				toward(rightArm, draw, 0.9F, 0.0F, 0.35F);
				toward(rightForearm, draw, -1.2F, 0.0F, 0.0F);
				body.yRot += draw * 0.45F - throwing * 0.35F;
				toward(rightArm, throwing, -1.7F, 0.0F, 0.05F);
				toward(rightForearm, throwing, -0.1F, 0.0F, 0.0F);
				toward(leftArm, Math.max(draw, throwing), -0.9F, 0.0F, -0.4F);
			}
			case SOW -> {
				float gather = window(t, 0.0F, 12.0F, 16.0F, 18.0F);
				float spread = window(t, 15.0F, 19.0F, 22.0F, 32.0F);
				toward(rightArm, gather, -1.2F, 0.4F, -0.3F);
				toward(leftArm, gather, -1.2F, -0.4F, 0.3F);
				toward(rightArm, spread, -1.5F, 0.0F, 1.4F);
				toward(leftArm, spread, -1.5F, 0.0F, -1.4F);
				toward(rightForearm, spread, 0.0F, 0.0F, 0.0F);
				toward(leftForearm, spread, 0.0F, 0.0F, 0.0F);
			}
			default -> {
			}
		}
		if (warden.phase() == com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Phase.REANCHORING) {
			float sink = Mth.clamp(t / com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.REANCHOR_TICKS, 0.0F, 1.0F);
			hips.y += 14.0F * sink * sink;
			toward(body, sink, 0.6F, 0.0F, 0.0F);
			toward(head, sink, 0.7F, 0.0F, 0.0F);
			toward(rightArm, sink, -0.6F, -0.2F, -0.3F);
			toward(leftArm, sink, -0.6F, 0.2F, 0.3F);
		}
	}

	/** Standing out of the kneel, stooped, the torn roots trailing: a slow heavy walk. */
	private void stand(float up, float limbSwing, float stride) {
		float phase = limbSwing * 0.5F;
		float swing = Mth.cos(phase) * stride;
		hips.y -= 6.0F * up;
		toward(rightLeg, up, swing * 0.6F, 0.05F, 0.0F);
		toward(leftLeg, up, -swing * 0.6F, -0.05F, 0.0F);
		toward(rightShin, up, 0.2F + 0.6F * Math.max(0.0F, Mth.sin(phase)) * stride, 0.0F, 0.0F);
		toward(leftShin, up, 0.2F + 0.6F * Math.max(0.0F, -Mth.sin(phase)) * stride, 0.0F, 0.0F);
		toward(body, up, 0.28F, 0.0F, 0.0F);
		toward(rightArm, up, -0.3F - swing * 0.4F, 0.0F, 0.25F);
		toward(leftArm, up, -0.3F + swing * 0.4F, 0.0F, -0.25F);
		toward(rightForearm, up, -0.5F, 0.0F, 0.0F);
		toward(leftForearm, up, -0.5F, 0.0F, 0.0F);
	}
