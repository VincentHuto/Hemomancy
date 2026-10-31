
	private ModelPart[] neck;
	private ModelPart[] crown;
	private ModelPart[] crownTips;
	private ModelPart[] buds;
	private ModelPart[] tail;
	private ModelPart[] fingers;

	private void collect() {
		if (neck != null) return;
		neck = new ModelPart[] {neck0, neck1, neck2, neck3};
		crown = new ModelPart[] {crown0, crown1, crown2, crown3, crown4, crown5, crown6, crown7};
		crownTips = new ModelPart[] {crown0Tip, crown1Tip, crown2Tip, crown3Tip, crown4Tip, crown5Tip, crown6Tip, crown7Tip};
		buds = new ModelPart[] {bud0, bud1, bud2, bud3, bud4, bud5, bud6, bud7};
		tail = new ModelPart[] {tail0, tail1, tail2, tail3, tail4, tail5};
		fingers = new ModelPart[] {rightFinger0, rightFinger1, rightFinger2, leftFinger0, leftFinger1, leftFinger2};
	}

	/**
	 * The elder's gait and breathing neck (court_kindled_elder_anim.java), and the Champion's fight over them. The crown
	 * buds four at a time as the Champion passes each threshold. In the opening it rears up on its neck and flares the
	 * crown. A neck sweep coils the neck to one side and swings it low through the arc; the sting rears the head and
	 * drives the crown down; the grab throws both tentacle arms forward and draws them in. Yielding, it lowers onto its
	 * knees with the neck bowed and the crown folded. Moves replay through the entity's BossReplayClock.
	 */
	@Override
	public void setupAnim(CourtMemberEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		collect();
		root.getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
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
			neck[i].xRot += 0.04F * Mth.sin(ageInTicks * 0.05F + i * 0.7F);
		}
		head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
		int stage = entity instanceof com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity c ? c.budStage() : 0;
		float flare = 0.1F * stage;
		for (int i = 0; i < crown.length; i++) {
			float ripple = Mth.sin(ageInTicks * 0.18F + i * 0.8F);
			float a = 2.0F * Mth.PI * i / crown.length;
			crown[i].xRot += -Mth.sin(a) * (0.1F * ripple + flare);
			crown[i].zRot += Mth.cos(a) * (0.1F * ripple + flare);
			crownTips[i].xRot += -Mth.sin(a) * 0.16F * Mth.sin(ageInTicks * 0.18F + i * 0.8F - 0.9F);
			crownTips[i].zRot += Mth.cos(a) * 0.16F * Mth.sin(ageInTicks * 0.18F + i * 0.8F - 0.9F);
		}
		for (int i = 0; i < buds.length; i++) {
			buds[i].visible = i < stage * 4;
			buds[i].xRot += 0.1F * Mth.sin(ageInTicks * 0.2F + i * 1.3F);
		}
		for (int i = 0; i < tail.length; i++) {
			tail[i].yRot += (0.12F + 0.15F * stride) * Mth.sin(ageInTicks * 0.07F + phase * 0.5F - i * 0.55F);
		}
		float curl = 0.15F * Mth.sin(ageInTicks * 0.05F);
		rightArm.xRot += -swing * 0.2F + curl;
		leftArm.xRot += swing * 0.2F + curl;
		rightForearm.xRot += -0.2F * curl;
		leftForearm.xRot += -0.2F * curl;
		for (int i = 0; i < fingers.length; i++) fingers[i].xRot += 0.25F * Mth.sin(ageInTicks * 0.09F + i);
		if (!(entity instanceof com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity champion)) return;

		float intro = champion.introTime(partial);
		if (intro >= 0.0F) {
			float rear = window(intro, 6.0F, 34.0F, 60.0F, 78.0F);
			for (ModelPart part : neck) part.xRot -= rear * 0.18F;
			head.xRot -= rear * 0.4F;
			for (ModelPart part : crown) part.xRot -= rear * 0.2F;
			rightArm.xRot -= rear * 0.9F;
			leftArm.xRot -= rear * 0.9F;
		}
		float t = champion.moveTime(partial);
		switch (champion.move()) {
			case NECK_SWEEP -> {
				float coil = window(t, 0.0F, 20.0F, 22.0F, 24.0F);
				float sweep = window(t, 21.0F, 24.0F, 28.0F, 46.0F);
				float through = com.vincenthuto.hemomancy.common.util.HemoEasing.smoothstep(21.0F, 28.0F, t);
				for (int i = 0; i < neck.length; i++) {
					neck[i].yRot += coil * 0.35F + sweep * Mth.lerp(through, 0.35F, -0.45F);
					neck[i].xRot += (coil + sweep) * 0.12F;
				}
				body.yRot += coil * 0.3F - sweep * 0.4F;
			}
			case CROWN_STING -> {
				float rear = window(t, 0.0F, 16.0F, 18.0F, 20.0F);
				float strike = window(t, 18.0F, 20.0F, 24.0F, 36.0F);
				for (ModelPart part : neck) part.xRot += -rear * 0.2F + strike * 0.3F;
				head.xRot += -rear * 0.5F + strike * 0.9F;
				for (int i = 0; i < crown.length; i++) {
					float a = 2.0F * Mth.PI * i / crown.length;
					crown[i].xRot += -Mth.sin(a) * rear * 0.4F;
					crown[i].zRot += Mth.cos(a) * rear * 0.4F;
				}
			}
			case CROWN_GRAB -> {
				float reach = window(t, 0.0F, 24.0F, 28.0F, 34.0F);
				float hold = window(t, 26.0F, 32.0F, 54.0F, 72.0F);
				toward(rightArm, reach, -1.9F, 0.0F, 0.1F);
				toward(leftArm, reach, -1.9F, 0.0F, -0.1F);
				toward(rightArm, hold, -1.1F, -0.3F, -0.2F);
				toward(leftArm, hold, -1.1F, 0.3F, 0.2F);
				toward(rightForearm, hold, -1.3F, 0.0F, 0.0F);
				toward(leftForearm, hold, -1.3F, 0.0F, 0.0F);
				for (ModelPart finger : fingers) finger.xRot += hold * 0.9F;
				for (ModelPart part : neck) part.xRot += (reach + hold) * 0.1F;
			}
			default -> {
			}
		}
		if (champion.state() == com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity.YIELDING) {
			float kneel = Mth.clamp(t / 24.0F, 0.0F, 1.0F);
			hips.y += 5.0F * kneel;
			rightShin.xRot += 0.9F * kneel;
			leftShin.xRot += 0.9F * kneel;
			for (ModelPart part : neck) part.xRot += 0.28F * kneel;
			head.xRot += 0.5F * kneel;
			for (ModelPart part : crown) part.xRot *= 1.0F - 0.6F * kneel;
			toward(rightArm, kneel, -0.2F, 0.0F, 0.1F);
			toward(leftArm, kneel, -0.2F, 0.0F, -0.1F);
		}
	}

