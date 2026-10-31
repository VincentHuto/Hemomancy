
	private ModelPart[] neck;
	private ModelPart[] crown;
	private ModelPart[] crownTips;
	private ModelPart[] tail;
	private ModelPart[] fingers;

	private void collect() {
		if (neck != null) return;
		neck = new ModelPart[] {neck0, neck1, neck2, neck3};
		crown = new ModelPart[] {crown0, crown1, crown2, crown3, crown4, crown5, crown6, crown7};
		crownTips = new ModelPart[] {crown0Tip, crown1Tip, crown2Tip, crown3Tip, crown4Tip, crown5Tip, crown6Tip, crown7Tip};
		tail = new ModelPart[] {tail0, tail1, tail2, tail3, tail4, tail5};
		fingers = new ModelPart[] {rightFinger0, rightFinger1, rightFinger2, leftFinger0, leftFinger1, leftFinger2};
	}

	/**
	 * The elder walks on its toes: thigh, shin and foot fold against each other with the stride so the claws stay
	 * under it. The look turn runs down the whole neck, a quarter per segment; the neck breathes in a slow wave, the
	 * crown ripples like flame from root to tip, the tail sweeps, and the tentacle arms curl and open. Speaking, the
	 * crown flares and the arms open toward the visitor; near a shunned visitor the neck draws back and the arms close.
	 */
	@Override
	public void setupAnim(CourtMemberEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		collect();
		root.getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		float talk = entity.talkAmount(partial);
		float wary = entity.waryAmount(partial);
		float phase = limbSwing * 0.55F;
		float stride = Math.min(1.0F, limbSwingAmount * 1.3F);
		float swing = Mth.cos(phase) * stride;
		rightLeg.xRot += swing * 0.6F;
		leftLeg.xRot -= swing * 0.6F;
		rightShin.xRot += Math.max(0.0F, Mth.sin(phase)) * 0.6F * stride;
		leftShin.xRot += Math.max(0.0F, -Mth.sin(phase)) * 0.6F * stride;
		rightFoot.xRot -= swing * 0.35F;
		leftFoot.xRot += swing * 0.35F;
		hips.y += Math.abs(Mth.sin(phase)) * -0.8F * stride;

		float yaw = netHeadYaw * Mth.DEG_TO_RAD / neck.length;
		for (int i = 0; i < neck.length; i++) {
			neck[i].yRot += yaw + 0.05F * Mth.sin(ageInTicks * 0.04F + i * 0.6F);
			neck[i].xRot += 0.04F * Mth.sin(ageInTicks * 0.05F + i * 0.7F) + wary * 0.12F;
		}
		head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F - wary * 0.3F;
		float flare = 0.25F * talk;
		for (int i = 0; i < crown.length; i++) {
			float ripple = Mth.sin(ageInTicks * 0.14F + i * 0.8F);
			float a = 2.0F * Mth.PI * i / crown.length;
			crown[i].xRot += -Mth.sin(a) * (0.08F * ripple + flare);
			crown[i].zRot += Mth.cos(a) * (0.08F * ripple + flare);
			crownTips[i].xRot += -Mth.sin(a) * 0.14F * Mth.sin(ageInTicks * 0.14F + i * 0.8F - 0.9F);
			crownTips[i].zRot += Mth.cos(a) * 0.14F * Mth.sin(ageInTicks * 0.14F + i * 0.8F - 0.9F);
		}
		for (int i = 0; i < tail.length; i++) {
			tail[i].yRot += (0.1F + 0.15F * stride) * Mth.sin(ageInTicks * 0.06F + phase * 0.5F - i * 0.55F);
			tail[i].xRot += 0.03F * Mth.sin(ageInTicks * 0.05F - i * 0.4F);
		}
		float curl = 0.15F * Mth.sin(ageInTicks * 0.05F);
		rightArm.xRot += -swing * 0.2F + curl - talk * 0.6F - wary * 0.3F;
		leftArm.xRot += swing * 0.2F + curl - talk * 0.45F - wary * 0.3F;
		rightArm.zRot += talk * 0.25F + wary * 0.3F;
		leftArm.zRot -= talk * 0.25F + wary * 0.3F;
		rightForearm.xRot += -0.2F * curl - talk * 0.3F - wary * 0.7F;
		leftForearm.xRot += -0.2F * curl - talk * 0.2F - wary * 0.7F;
		for (int i = 0; i < fingers.length; i++) {
			fingers[i].xRot += 0.25F * Mth.sin(ageInTicks * 0.09F + i) * (1.0F - wary) + wary * 0.6F;
		}
	}
