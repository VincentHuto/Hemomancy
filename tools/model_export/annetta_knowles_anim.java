	/** Chooses hair, tick and head visibility for the look about to render. */
	public void prepare(AnnettaLooks.Look look, float tickSwell, boolean headVisible) {
		this.look = look;
		this.tickSwell = tickSwell;
		boolean purged = AnnettaLooks.purged(look);
		this.longHair.visible = !purged;
		this.shortHair.visible = purged;
		this.neckTick.visible = AnnettaLooks.showsTick(look) && headVisible;
		this.head.visible = headVisible;
		this.neckStump.visible = !headVisible;
	}

	/** Whether the blade is mid-cut this frame, so the held-item layer draws its trail. */
	public boolean bladeTrail() {
		return this.bladeTrail;
	}

	// ---------------------------------------------------------------- timing helpers

	private static float ramp(float t, float from, float to) {
		float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}

	/** In, held, out: 0 before a, 1 from b to c, 0 after d. */
	private static float window(float t, float a, float b, float c, float d) {
		return ramp(t, a, b) * (1.0F - ramp(t, c, d));
	}

	/**
	 * Catmull-Rom through keys {t0, v0, t1, v1, ...}, held flat before the first and after the last. Velocity carries
	 * through each key, so a wind-up flows into its strike instead of stopping on every pose.
	 */
	private static float curve(float t, float... keys) {
		int n = keys.length / 2;
		if (t <= keys[0]) return keys[1];
		if (t >= keys[(n - 1) * 2]) return keys[(n - 1) * 2 + 1];
		int i = 0;
		while (i < n - 2 && t > keys[(i + 1) * 2]) i++;
		float t1 = keys[i * 2], t2 = keys[(i + 1) * 2];
		float p1 = keys[i * 2 + 1], p2 = keys[(i + 1) * 2 + 1];
		float p0 = i > 0 ? keys[(i - 1) * 2 + 1] : p1;
		float t0 = i > 0 ? keys[(i - 1) * 2] : t1 - (t2 - t1);
		float p3 = i + 2 < n ? keys[(i + 2) * 2 + 1] : p2;
		float t3 = i + 2 < n ? keys[(i + 2) * 2] : t2 + (t2 - t1);
		float s = (t - t1) / (t2 - t1);
		// Tangents scaled to each segment's length so uneven key spacing does not overshoot wildly.
		float m1 = (p2 - p0) / (t2 - t0) * (t2 - t1);
		float m2 = (p3 - p1) / (t3 - t1) * (t2 - t1);
		float s2 = s * s, s3 = s2 * s;
		return (2 * s3 - 3 * s2 + 1) * p1 + (s3 - 2 * s2 + s) * m1 + (-2 * s3 + 3 * s2) * p2 + (s3 - s2) * m2;
	}

	private static void rot(ModelPart part, float x, float y, float z) {
		part.xRot = x;
		part.yRot = y;
		part.zRot = z;
	}

	/** Blend a part toward a target rotation by w; the target replaces what the stance set. */
	private static void toward(ModelPart part, float w, float x, float y, float z) {
		part.xRot = Mth.lerp(w, part.xRot, x);
		part.yRot = Mth.lerp(w, part.yRot, y);
		part.zRot = Mth.lerp(w, part.zRot, z);
	}

	// ---------------------------------------------------------------- the pass

	@Override
	public void setupAnim(AnnettaKnowlesEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.bladeTrail = false;
		float partial = ageInTicks - entity.tickCount;
		float phase = limbSwing * 0.6662F;
		float stride = Math.min(1.0F, limbSwingAmount * 1.4F);
		float walk = Mth.cos(phase) * stride;
		float breathe = Mth.sin(ageInTicks * 0.08F);
		int action = entity.getAction();
		float actionTime = entity.actionTime(partial);
		float scene = entity.sceneTime(partial);
		EncounterState state = entity.getEncounterState();

		switch (state) {
			case COWERING -> cower(ageInTicks);
			case PHASE_ONE -> {
				fightStance(ageInTicks, walk, phase, stride);
				fight(action, actionTime, ageInTicks);
			}
			case BREAKING -> {
				this.tickSwell = AnnettaEncounterRules.breakSwell(scene, this.tickSwell);
				breaking(scene, ageInTicks);
			}
			case PURGING -> purging(scene, ageInTicks);
			case CURED_SUPPORT -> {
				supportStance(ageInTicks, walk, phase, stride);
				support(action, actionTime, ageInTicks);
			}
			case RESOLVED -> resolved(walk, phase, stride, breathe);
		}

		boolean tracking = state == EncounterState.PHASE_ONE || state == EncounterState.CURED_SUPPORT
				|| state == EncounterState.RESOLVED;
		if (tracking) {
			// The head keeps on its mark whatever the hips and torso are doing under it.
			this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD - this.body.yRot - this.hips.yRot;
			this.head.xRot += headPitch * Mth.DEG_TO_RAD - this.body.xRot * 0.8F;
			this.head.zRot -= this.body.zRot * 0.6F;
		}
		if (state != EncounterState.COWERING && !(state == EncounterState.BREAKING && scene > AnnettaEncounterRules.FALL_START)
				&& !(state == EncounterState.PURGING)) {
			plantFeet();
		}
		drapeRobe();

		float pulse = Mth.sin(ageInTicks * 0.3F) * 0.04F * this.tickSwell;
		this.tickAbdomen.xScale = 1.0F + 1.6F * this.tickSwell + pulse;
		this.tickAbdomen.zScale = 1.0F + 1.6F * this.tickSwell + pulse;
		this.tickAbdomen.yScale = 1.0F + 1.2F * this.tickSwell + pulse;
	}

	/** Leg swing for walking, laid over whatever stance the legs hold. */
	private void stride(float walk, float phase, float stride, float amount) {
		this.rightLeg.xRot += walk * 1.0F * amount;
		this.leftLeg.xRot -= walk * 1.0F * amount;
		this.rightShin.xRot += Math.max(0.0F, Mth.sin(phase)) * stride * 1.2F * amount;
		this.leftShin.xRot += Math.max(0.0F, -Mth.sin(phase)) * stride * 1.2F * amount;
		this.robeSkirt.xRot += Math.abs(walk) * 0.1F * amount;
	}

	// ---------------------------------------------------------------- Phase 1

	/**
	 * Fencer's stance, shield side leading: hips turned so the shield faces her foe, the buckler strapped across her
	 * forearm in front of her chest, the dagger held reversed and low by her hip, knees soft. The breath heaves; her
	 * eyes slide off and back. Nothing here moves faster than a breath, so the pose reads as tense, not twitchy.
	 */
	private void fightStance(float age, float walk, float phase, float stride) {
		float heave = Mth.sin(age * 0.16F);
		float glance = Mth.sin(age * 0.047F) * Mth.sin(age * 0.13F + 1.3F);
		this.hips.yRot = 0.3F;
		rot(this.body, 0.12F + heave * 0.025F, 0.1F, 0.0F);
		this.head.yRot = glance * 0.14F;
		this.head.zRot = glance * -0.05F;
		rot(this.leftArm, -0.135F, 1.1F, -0.374F);
		rot(this.leftForearm, -2.2F + heave * 0.04F, 0.0F, -0.144F);
		rot(this.rightArm, -0.35F, -0.15F, 0.18F - heave * 0.03F);
		rot(this.rightForearm, -1.15F, 0.0F, 0.0F);
		float settle = 1.0F - stride;
		this.leftLeg.xRot = -0.4F * settle;
		this.leftShin.xRot = 0.5F * settle;
		this.rightLeg.xRot = 0.3F * settle;
		this.rightShin.xRot = 0.55F * settle;
		this.leftLeg.yRot = -0.15F;
		this.rightLeg.yRot = 0.2F;
		stride(walk, phase, stride, 1.0F);
	}

	private void fight(int action, float t, float age) {
		switch (action) {
			case AnnettaKnowlesEntity.ACTION_SCREAM -> scream(t, age);
			case AnnettaKnowlesEntity.ACTION_DAGGER_COMBO -> daggerCombo(t);
			case AnnettaKnowlesEntity.ACTION_STILL_DASH -> stillDash(t, 12.0F);
			case AnnettaKnowlesEntity.ACTION_STILL_BOLT -> stillBolt(t);
			case AnnettaKnowlesEntity.ACTION_SHIELD_GUARD -> shieldGuard(t, age);
			case AnnettaKnowlesEntity.ACTION_SHIELD_BASH -> shieldBash(t);
			case AnnettaKnowlesEntity.ACTION_SILVER_AURA -> silverAura(t);
			case AnnettaKnowlesEntity.ACTION_FLINCH -> flinch(t);
			default -> {
			}
		}
	}

	/** The tick bites: she claws at her neck, head thrown back and wrenched aside, the dagger arm flung wide. */
	private void scream(float t, float age) {
		float w = window(t, 0.0F, 3.0F, 24.0F, 30.0F);
		float writhe = Mth.sin(age * 0.7F) * 0.5F + Mth.sin(age * 0.31F) * 0.5F;
		toward(this.leftArm, w, -1.378F, 0.0F, -0.171F);
		toward(this.leftForearm, w, -2.65F, 0.0F, 0.0F);
		toward(this.rightArm, w, -0.9F + writhe * 0.25F, 0.2F, 0.9F);
		toward(this.rightForearm, w, -0.6F, 0.0F, 0.0F);
		toward(this.body, w, -0.18F + writhe * 0.06F, 0.0F, -0.12F + writhe * 0.08F);
		this.head.xRot -= 0.55F * w;
		this.head.zRot -= (0.3F + writhe * 0.12F) * w;
		this.hips.yRot *= 1.0F - w;
		// Her knees go.
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, -0.35F);
		this.rightLeg.xRot = Mth.lerp(w, this.rightLeg.xRot, -0.2F);
		this.leftShin.xRot = Mth.lerp(w, this.leftShin.xRot, 0.8F);
		this.rightShin.xRot = Mth.lerp(w, this.rightShin.xRot, 0.65F);
	}

	/**
	 * Three cuts with her whole body behind them. Each wind-up twists the torso away and cocks the elbow, the strike
	 * uncoils hips then shoulder then forearm, and the front foot drives in on the hit. Cut one falls from her right
	 * shoulder across to her left hip; cut two rises back the other way; the third goes up behind her head and comes
	 * down point first. Server hits land on ticks 6, 14 and 22.
	 */
	private void daggerCombo(float t) {
		float w = window(t, 0.0F, 2.0F, 25.0F, 28.0F);
		this.bladeTrail = t > 3.5F && t < 24.0F;
		// Keys: stance -> wind 1 (3) -> through (7.5) -> wind 2 (11) -> through (15.5) -> raise (19.5) -> driven (22.5) -> hold (25).
		float ax = curve(t, 0, -0.35F, 3, -2.1F, 5.5F, -1.5F, 7.5F, -0.9F, 11, -0.95F, 13.5F, -1.5F, 15.5F, -2.0F, 19.5F, -2.95F, 22.5F, -0.85F, 25, -0.8F);
		float ay = curve(t, 0, -0.15F, 3, 0.95F, 5.5F, 0.0F, 7.5F, -0.85F, 11, -0.95F, 13.5F, 0.1F, 15.5F, 0.9F, 19.5F, 0.25F, 22.5F, -0.15F, 25, -0.1F);
		float az = curve(t, 0, 0.18F, 3, 0.35F, 7.5F, -0.15F, 11, -0.2F, 15.5F, 0.45F, 19.5F, 0.1F, 22.5F, 0.0F, 25, 0.0F);
		float fx = curve(t, 0, -1.15F, 3, -1.7F, 5.5F, -0.35F, 7.5F, -0.7F, 11, -1.6F, 13.5F, -0.3F, 15.5F, -0.6F, 19.5F, -1.5F, 22.5F, -0.2F, 25, -0.35F);
		toward(this.rightArm, w, ax, ay, az);
		toward(this.rightForearm, w, fx, 0.0F, 0.0F);
		float by = curve(t, 0, 0.1F, 3, 0.55F, 7.5F, -0.4F, 11, -0.35F, 15.5F, 0.45F, 19.5F, 0.2F, 22.5F, -0.15F, 25, -0.1F);
		float bx = curve(t, 0, 0.12F, 3, 0.05F, 7.5F, 0.3F, 11, 0.15F, 15.5F, 0.25F, 19.5F, -0.08F, 22.5F, 0.5F, 25, 0.45F);
		float hy = curve(t, 0, 0.3F, 3, 0.45F, 7.5F, 0.1F, 11, 0.15F, 15.5F, 0.4F, 19.5F, 0.3F, 22.5F, 0.15F, 25, 0.2F);
		this.body.xRot = Mth.lerp(w, this.body.xRot, bx);
		this.body.yRot = Mth.lerp(w, this.body.yRot, by);
		this.hips.yRot = Mth.lerp(w, this.hips.yRot, hy);
		// The shield arm counterweights the cut, swinging against it.
		this.leftArm.yRot -= 0.35F * (by - 0.1F) * w;
		this.leftArm.xRot += 0.15F * Math.abs(by - 0.1F) * w;
		// The front foot drives into each strike; the overhead finisher drops her into a deep lunge.
		float lunge = curve(t, 0, 0.0F, 3, 0.1F, 6.5F, 1.0F, 9, 0.6F, 11, 0.3F, 14.5F, 1.0F, 17, 0.6F, 19.5F, 0.35F, 22.5F, 1.4F, 25, 1.3F) * w;
		this.leftLeg.xRot += -0.42F * lunge;
		this.leftShin.xRot += 0.45F * lunge;
		this.rightLeg.xRot += 0.3F * lunge;
		this.rightShin.xRot += 0.1F * lunge;
	}

	/** Still arts: she pours across the floor low and leaning, arms trailing behind her like water drawn after. */
	private void stillDash(float t, float length) {
		float w = window(t, 0.0F, 2.5F, length - 4.0F, length);
		float glide = Mth.sin(Mth.clamp(t / length, 0.0F, 1.0F) * Mth.PI);
		toward(this.body, w, 0.5F, 0.1F, 0.0F);
		toward(this.leftArm, w, 0.897F, -0.284F, 0.293F);
		toward(this.leftForearm, w, -1.861F, -0.065F, -1.326F);
		toward(this.rightArm, w, 0.75F, 0.15F, 0.35F);
		toward(this.rightForearm, w, -0.35F, 0.0F, 0.0F);
		this.hips.yRot = Mth.lerp(w, this.hips.yRot, 0.2F);
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, -0.95F);
		this.leftShin.xRot = Mth.lerp(w, this.leftShin.xRot, 1.15F);
		this.rightLeg.xRot = Mth.lerp(w, this.rightLeg.xRot, 0.7F);
		this.rightShin.xRot = Mth.lerp(w, this.rightShin.xRot, 0.35F);
		this.robeSkirt.xRot += 0.35F * glide;
		this.head.xRot -= 0.2F * w;
	}

	/** A vial of hemolytic humor: drawn back behind her ear with her whole side turned, then whipped overhand. */
	private void stillBolt(float t) {
		float w = window(t, 0.0F, 2.0F, 14.0F, 18.0F);
		float ax = curve(t, 0, -0.35F, 7, -2.75F, 9, -2.2F, 11, -0.9F, 14, -0.7F);
		float ay = curve(t, 0, -0.15F, 7, 0.55F, 9, 0.2F, 11, -0.35F, 14, -0.3F);
		float fx = curve(t, 0, -1.15F, 7, -1.55F, 9, -0.6F, 11, -0.1F, 14, -0.3F);
		toward(this.rightArm, w, ax, ay, 0.15F);
		toward(this.rightForearm, w, fx, 0.0F, 0.0F);
		float by = curve(t, 0, 0.1F, 7, 0.6F, 9, 0.2F, 11, -0.45F, 14, -0.35F);
		float bx = curve(t, 0, 0.12F, 7, -0.12F, 9, 0.1F, 11, 0.35F, 14, 0.3F);
		this.body.yRot = Mth.lerp(w, this.body.yRot, by);
		this.body.xRot = Mth.lerp(w, this.body.xRot, bx);
		this.hips.yRot = Mth.lerp(w, this.hips.yRot, curve(t, 0, 0.3F, 7, 0.5F, 11, 0.0F, 14, 0.05F));
		float step = ramp(t, 8.0F, 11.0F) * w;
		this.leftLeg.xRot -= 0.35F * step;
		this.leftShin.xRot += 0.3F * step;
		this.rightLeg.xRot += 0.2F * step;
	}

	/** Shield raised to her eyes and braced, the dagger cocked behind it waiting for an opening. */
	private void shieldGuard(float t, float age) {
		float w = window(t, 0.0F, 3.0F, 28.0F, 32.0F);
		float brace = Mth.sin(age * 0.21F) * 0.02F;
		toward(this.leftArm, w, -1.098F, 0.832F, -0.199F);
		toward(this.leftForearm, w, -2.392F + brace, 0.433F, 0.013F);
		toward(this.rightArm, w, -0.6F, -0.35F, 0.25F);
		toward(this.rightForearm, w, -1.65F, 0.0F, 0.0F);
		this.body.xRot = Mth.lerp(w, this.body.xRot, 0.2F);
		this.head.xRot += 0.12F * w;
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, -0.55F);
		this.leftShin.xRot = Mth.lerp(w, this.leftShin.xRot, 0.8F);
		this.rightLeg.xRot = Mth.lerp(w, this.rightLeg.xRot, 0.4F);
		this.rightShin.xRot = Mth.lerp(w, this.rightShin.xRot, 0.7F);
	}

	/**
	 * The counter: the blow knocks the shield back into her, she gathers behind it, then drives it out with her
	 * shoulder and a lunge (hit on tick 7) and holds it there a moment before falling back to her stance.
	 */
	private void shieldBash(float t) {
		float w = window(t, 0.0F, 1.0F, 12.0F, 16.0F);
		float gather = window(t, 1.0F, 4.0F, 5.0F, 7.0F);
		float drive = window(t, 5.0F, 7.0F, 10.0F, 15.0F);
		// Start from the raised guard the blow struck.
		toward(this.leftArm, w, -1.098F, 0.832F, -0.199F);
		toward(this.leftForearm, w, -2.392F, 0.433F, 0.013F);
		toward(this.leftArm, gather, 0.811F, 0.18F, 0.331F);
		toward(this.leftForearm, gather, -2.708F, -0.002F, -0.732F);
		toward(this.leftArm, drive, -1.24F, 0.248F, -1.034F);
		toward(this.leftForearm, drive, -1.882F, 0.16F, 0.768F);
		float recoil = window(t, 0.0F, 1.0F, 1.5F, 4.0F);
		this.body.xRot = Mth.lerp(w, this.body.xRot, 0.2F) - 0.25F * recoil + (0.05F - 0.2F) * gather + (0.35F - 0.2F) * drive;
		this.body.yRot = Mth.lerp(w, this.body.yRot, 0.1F) + 0.25F * gather - 0.25F * drive;
		this.hips.yRot += 0.05F * gather + 0.15F * drive;
		toward(this.rightArm, w, -0.4F, -0.2F, 0.35F);
		toward(this.rightForearm, w, -1.4F, 0.0F, 0.0F);
		this.rightArm.xRot += 0.5F * drive;
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, -0.45F) - 0.45F * drive;
		this.leftShin.xRot = Mth.lerp(w, this.leftShin.xRot, 0.7F) + 0.35F * gather;
		this.rightLeg.xRot = Mth.lerp(w, this.rightLeg.xRot, 0.35F) + 0.25F * drive;
		this.rightShin.xRot = Mth.lerp(w, this.rightShin.xRot, 0.6F) + 0.2F * gather;
		this.head.xRot += 0.15F * recoil;
	}

	/** She draws everything in, then throws her arms wide as the pale ring leaves her (fires on tick 8). */
	private void silverAura(float t) {
		float w = window(t, 0.0F, 2.0F, 15.0F, 20.0F);
		float gather = window(t, 0.0F, 4.0F, 6.0F, 8.0F);
		float spread = ramp(t, 7.0F, 9.5F) * (1.0F - ramp(t, 15.0F, 20.0F));
		toward(this.rightArm, gather, -1.2F, -0.7F, 0.0F);
		toward(this.rightForearm, gather, -1.9F, 0.0F, 0.0F);
		toward(this.leftArm, gather, -1.2F, 0.7F, 0.0F);
		toward(this.leftForearm, gather, -1.9F, 0.0F, 0.0F);
		toward(this.rightArm, spread, -0.35F, 0.0F, 1.35F);
		toward(this.leftArm, spread, -0.35F, 0.0F, -1.35F);
		toward(this.rightForearm, spread, -0.15F, 0.0F, 0.0F);
		toward(this.leftForearm, spread, -0.15F, 0.0F, 0.0F);
		this.body.xRot = Mth.lerp(w, this.body.xRot, 0.3F * gather - 0.22F * spread);
		this.body.yRot *= 1.0F - w;
		this.hips.yRot *= 1.0F - w;
		this.head.xRot += 0.35F * gather - 0.4F * spread;
		float crouch = gather - 0.4F * spread;
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, -0.3F - 0.3F * crouch);
		this.rightLeg.xRot = Mth.lerp(w, this.rightLeg.xRot, 0.1F - 0.3F * crouch);
		this.leftShin.xRot = Mth.lerp(w, this.leftShin.xRot, 0.35F + 0.5F * crouch);
		this.rightShin.xRot = Mth.lerp(w, this.rightShin.xRot, 0.35F + 0.5F * crouch);
	}

	private void flinch(float t) {
		float jolt = curve(t, 0, 0.0F, 1.5F, 1.0F, 4, 0.55F, 8, 0.0F);
		this.body.xRot -= 0.35F * jolt;
		this.body.zRot += 0.12F * jolt;
		this.head.xRot -= 0.35F * jolt;
		this.leftArm.xRot -= 0.25F * jolt;
		this.rightArm.zRot += 0.35F * jolt;
		this.rightShin.xRot += 0.3F * jolt;
		this.leftShin.xRot += 0.3F * jolt;
	}

	// ---------------------------------------------------------------- the break

	/**
	 * The falter; her free hand finds the tick; her left hand closes over her right on the reversed hilt and both lift
	 * it; the point goes into the tick; it swells against the blade, her head bulging with it, until it pops and takes
	 * her head; her arms are blown open; she stands headless a moment, swaying, then falls and lies while the blood
	 * climbs her.
	 */
	private void breaking(float t, float age) {
		float slow = Mth.sin(age * 0.18F);
		float falter = ramp(t, 0.0F, 10.0F);
		// Standing, knees softening.
		this.body.xRot = 0.18F * falter;
		this.body.zRot = 0.08F * slow * falter;
		this.leftLeg.xRot = -0.25F * falter;
		this.rightLeg.xRot = 0.1F * falter;
		this.leftShin.xRot = 0.45F * falter;
		this.rightShin.xRot = 0.35F * falter;
		this.head.xRot = 0.3F * falter + 0.05F * slow;
		// The shield arm sags before she lets it go (the shield falls at tick 12); the dagger hangs.
		rot(this.leftArm, Mth.lerp(falter, -0.135F, -0.15F), Mth.lerp(falter, 1.1F, 0.1F), Mth.lerp(falter, -0.374F, -0.12F));
		rot(this.leftForearm, Mth.lerp(falter, -2.2F, -0.35F), 0.0F, 0.0F);
		rot(this.rightArm, -0.2F, -0.1F, 0.1F);
		rot(this.rightForearm, -0.7F, 0.0F, 0.0F);
		// Her free hand finds the tick; her head turns from it.
		float clutch = window(t, 12.0F, 20.0F, AnnettaEncounterRules.GRIP, AnnettaEncounterRules.GRIP + 6.0F);
		toward(this.leftArm, clutch, -1.378F, 0.0F, -0.171F);
		toward(this.leftForearm, clutch, -2.65F, 0.0F, 0.0F);
		this.head.zRot = -0.25F * clutch;
		// Both hands on the hilt, held out at arm's length with the point turned back at her own neck. It trembles.
		float raise = ramp(t, AnnettaEncounterRules.GRIP, AnnettaEncounterRules.STAB - 4.0F);
		float shake = Mth.sin(age * 1.1F) * 0.035F * raise;
		toward(this.rightArm, raise, -1.665F + shake, -0.32F, -0.262F);
		toward(this.rightForearm, raise, -0.05F, 1.475F, -0.335F);
		toward(this.leftArm, raise, -2.071F + shake, 0.168F, -1.429F);
		toward(this.leftForearm, raise, -0.05F, 1.542F, -0.594F);
		this.body.xRot = Mth.lerp(raise, this.body.xRot, -0.12F);
		this.body.yRot = Mth.lerp(raise, this.body.yRot, -0.2F);
		this.head.xRot = Mth.lerp(raise, this.head.xRot, 0.25F);
		// The stab: two ticks, both fists yanked in to the left of her chin and the point into the tick, her head
		// wrenched away to bare it.
		float drive = ramp(t, AnnettaEncounterRules.STAB - 1.0F, AnnettaEncounterRules.STAB + 1.0F);
		float shudder = Mth.sin(age * 1.7F) * 0.025F * drive;
		toward(this.rightArm, drive, -1.718F + shudder, -0.835F, -0.28F);
		toward(this.rightForearm, drive, -0.41F, 0.667F, -0.882F);
		toward(this.leftArm, drive, -2.641F + shudder, 0.32F, -1.358F);
		toward(this.leftForearm, drive, -0.05F, 1.556F, -1.576F);
		this.body.xRot = Mth.lerp(drive, this.body.xRot, 0.2F);
		this.body.yRot = Mth.lerp(drive, this.body.yRot, -0.45F);
		this.body.zRot = Mth.lerp(drive, this.body.zRot, -0.1F);
		this.head.xRot = Mth.lerp(drive, this.head.xRot, -0.15F);
		this.head.zRot = Mth.lerp(drive, this.head.zRot, -0.4F);
		this.leftShin.xRot += 0.25F * drive;
		this.rightShin.xRot += 0.2F * drive;
		this.bladeTrail = t > AnnettaEncounterRules.STAB - 2.0F && t < AnnettaEncounterRules.STAB + 2.0F;
		// The tick fills against the blade and the pressure goes up into her head: it bulges and throbs, faster.
		float swell = ramp(t, AnnettaEncounterRules.STAB + 4.0F, AnnettaEncounterRules.BURST);
		if (t < AnnettaEncounterRules.BURST) {
			float throb = Mth.sin(age * (0.9F + 1.4F * swell)) * 0.035F * swell;
			float bulge = 1.0F + 0.2F * swell * swell + throb;
			this.head.xScale = bulge;
			this.head.yScale = bulge;
			this.head.zScale = bulge;
			this.head.zRot -= 0.12F * swell;
		}
		if (t >= AnnettaEncounterRules.BURST) {
			// The pop throws her arms open, then they fall slack; the body stands, swaying like a held breath.
			float blown = window(t, AnnettaEncounterRules.BURST, AnnettaEncounterRules.BURST + 2.0F,
					AnnettaEncounterRules.BURST + 4.0F, AnnettaEncounterRules.BURST + 14.0F);
			float slack = ramp(t, AnnettaEncounterRules.BURST + 3.0F, AnnettaEncounterRules.BURST + 16.0F);
			toward(this.rightArm, blown, -1.6F, -0.3F, 1.2F);
			toward(this.leftArm, blown, -1.7F, 0.3F, -1.25F);
			toward(this.rightForearm, blown, -0.4F, 0.0F, 0.0F);
			toward(this.leftForearm, blown, -0.3F, 0.0F, 0.0F);
			toward(this.rightArm, slack, 0.05F, 0.0F, 0.12F + 0.05F * slow);
			toward(this.leftArm, slack, 0.05F, 0.0F, -0.1F + 0.05F * slow);
			toward(this.rightForearm, slack, -0.15F, 0.0F, 0.0F);
			toward(this.leftForearm, slack, -0.1F, 0.0F, 0.0F);
			this.body.xRot = Mth.lerp(slack, this.body.xRot - 0.3F * blown, 0.1F);
			this.body.yRot *= 1.0F - slack;
			this.body.zRot = Mth.lerp(slack, this.body.zRot, 0.16F * slow);
			this.rightShin.xRot += 0.3F * slack;
			this.leftShin.xRot += 0.35F * slack;
		}
		float fall = ramp(t, AnnettaEncounterRules.FALL_START, AnnettaEncounterRules.FALL_END);
		fall = fall * fall;
		if (fall > 0.0F) {
			// Knees first, then forward onto her stomach.
			float buckle = ramp(t, AnnettaEncounterRules.FALL_START, AnnettaEncounterRules.FALL_START + 8.0F)
					* (1.0F - ramp(t, AnnettaEncounterRules.FALL_END - 6.0F, AnnettaEncounterRules.FALL_END));
			this.rightShin.xRot += 0.9F * buckle;
			this.leftShin.xRot += 0.9F * buckle;
			this.root.xRot = fall * 1.5F;
			this.root.y += fall * 20.3F;
			this.root.z -= fall * 2.0F;
			// Arms trail above her head as she goes down.
			toward(this.rightArm, fall, -2.8F, 0.0F, -0.25F);
			toward(this.leftArm, fall, -2.5F, 0.0F, 0.3F);
			toward(this.rightForearm, fall, -0.3F, 0.0F, 0.0F);
			toward(this.leftForearm, fall, -0.5F, 0.0F, 0.0F);
			this.body.xRot *= 1.0F - fall;
			this.body.zRot *= 1.0F - fall;
			this.rightLeg.xRot = Mth.lerp(fall, this.rightLeg.xRot, 0.0F);
			this.leftLeg.xRot = Mth.lerp(fall, this.leftLeg.xRot, 0.1F);
			this.rightShin.xRot = Mth.lerp(fall, this.rightShin.xRot, 0.15F);
			this.leftShin.xRot = Mth.lerp(fall, this.leftShin.xRot, 0.35F);
		}
		if (t > AnnettaEncounterRules.FALL_END) {
			// Lying in the pool, she begins to twitch as the blood takes hold.
			float creep = AnnettaEncounterRules.creep(t);
			float twitch = Mth.sin(age * 1.3F) * Mth.sin(age * 0.31F);
			this.rightArm.zRot += 0.25F * creep * twitch;
			this.leftArm.zRot -= 0.2F * creep * twitch;
			this.rightForearm.xRot -= 0.4F * creep * Math.max(0.0F, twitch);
			this.leftShin.xRot += 0.3F * creep * Math.max(0.0F, -twitch);
			this.leftLeg.xRot += 0.15F * creep * twitch;
			this.body.xRot = 0.1F * creep * Math.max(0.0F, twitch);
		}
	}

	// ---------------------------------------------------------------- cowering, purge and after

	/** Sitting on the floor with her knees drawn up, arms wrapped round them, face pressed down, trembling. */
	private void cower(float ageInTicks) {
		float tremble = Mth.sin(ageInTicks * 0.9F) * 0.03F;
		this.root.y += 10.0F;
		this.rightLeg.xRot = -2.15F;
		this.leftLeg.xRot = -2.15F;
		this.rightLeg.yRot = 0.14F;
		this.leftLeg.yRot = -0.14F;
		this.rightShin.xRot = 1.75F;
		this.leftShin.xRot = 1.75F;
		this.body.xRot = 0.5F + tremble * 0.3F;
		this.head.xRot = 0.45F;
		// Arms round her knees: elbows drawn in, forearms crossing in front of the shins, hands meeting.
		rot(this.rightArm, -1.45F + tremble, -0.35F, -0.32F);
		rot(this.leftArm, -1.45F - tremble, 0.35F, 0.32F);
		rot(this.rightForearm, -0.1F, -1.3F, 0.0F);
		rot(this.leftForearm, -0.1F, 1.3F, 0.0F);
	}

	/** She drinks, convulses as the red drains out of her, is thrown to her knees as it tears free, and rises. */
	private void purging(float t, float ageInTicks) {
		float drink = ramp(t, 0.0F, 8.0F) * (1.0F - ramp(t, AnnettaEncounterRules.DRINK_END - 6.0F, AnnettaEncounterRules.DRINK_END));
		this.rightForearm.xRot = -0.18F;
		this.leftForearm.xRot = -0.18F;
		// The vial comes up to her mouth on a bent arm.
		this.rightArm.xRot = -1.35F * drink;
		this.rightArm.yRot = -0.4F * drink;
		this.rightForearm.xRot = Mth.lerp(drink, this.rightForearm.xRot, -1.5F);
		this.head.xRot = -0.35F * drink;
		float convulse = ramp(t, AnnettaEncounterRules.DRINK_END, AnnettaEncounterRules.TEAR - 10.0F)
				* (1.0F - ramp(t, AnnettaEncounterRules.TEAR, AnnettaEncounterRules.TEAR + 4.0F));
		float shake = Mth.sin(ageInTicks * 1.4F) * Mth.sin(ageInTicks * 0.43F);
		this.body.xRot += (0.35F + 0.06F * shake) * convulse;
		this.body.zRot += 0.08F * shake * convulse;
		// Clutching her stomach, knees knocking.
		this.rightArm.xRot = Mth.lerp(convulse, this.rightArm.xRot, -0.95F + 0.15F * shake);
		this.leftArm.xRot = Mth.lerp(convulse, this.leftArm.xRot, -0.95F - 0.15F * shake);
		this.rightForearm.xRot = Mth.lerp(convulse, this.rightForearm.xRot, -1.2F);
		this.leftForearm.xRot = Mth.lerp(convulse, this.leftForearm.xRot, -1.2F);
		this.rightForearm.yRot = -0.5F * convulse;
		this.leftForearm.yRot = 0.5F * convulse;
		this.rightArm.zRot = 0.3F * convulse;
		this.leftArm.zRot = -0.3F * convulse;
		this.root.y += 1.0F * convulse;
		this.rightShin.xRot += (0.4F + 0.08F * shake) * convulse;
		this.leftShin.xRot += (0.4F - 0.08F * shake) * convulse;
		this.head.xRot += 0.15F * convulse + 0.08F * shake * convulse;
		// The tear: her back arches as it leaves her, arms flung out, then she drops to her knees.
		float tear = ramp(t, AnnettaEncounterRules.TEAR - 2.0F, AnnettaEncounterRules.TEAR + 2.0F)
				* (1.0F - ramp(t, AnnettaEncounterRules.TEAR + 4.0F, AnnettaEncounterRules.TEAR + 10.0F));
		this.body.xRot -= 0.7F * tear;
		this.head.xRot -= 0.5F * tear;
		this.rightArm.zRot += 0.9F * tear;
		this.leftArm.zRot -= 0.9F * tear;
		this.rightForearm.xRot = Mth.lerp(tear, this.rightForearm.xRot, -0.1F);
		this.leftForearm.xRot = Mth.lerp(tear, this.leftForearm.xRot, -0.1F);
		float kneel = ramp(t, AnnettaEncounterRules.TEAR + 4.0F, AnnettaEncounterRules.TEAR + 14.0F)
				* (1.0F - ramp(t, AnnettaEncounterRules.KNEEL_END, AnnettaEncounterRules.PURGE_END));
		// Kneeling: thighs upright, shins flat on the floor behind her.
		this.root.y += 6.0F * kneel;
		this.rightLeg.xRot = Mth.lerp(kneel, this.rightLeg.xRot, -0.05F);
		this.leftLeg.xRot = Mth.lerp(kneel, this.leftLeg.xRot, 0.05F);
		this.rightShin.xRot = Mth.lerp(kneel, this.rightShin.xRot, 1.55F);
		this.leftShin.xRot = Mth.lerp(kneel, this.leftShin.xRot, 1.55F);
		this.robeSkirt.xRot = Mth.lerp(kneel, this.robeSkirt.xRot, 0.2F);
		this.body.xRot = Mth.lerp(kneel, this.body.xRot, 0.45F);
		this.head.xRot = Mth.lerp(kneel, this.head.xRot, 0.15F);
		// Palms braced on her thighs (the arms hang from a leaning torso, so they swing back against it).
		this.rightArm.xRot = Mth.lerp(kneel, this.rightArm.xRot, -0.8F);
		this.leftArm.xRot = Mth.lerp(kneel, this.leftArm.xRot, -0.8F);
		this.rightForearm.xRot = Mth.lerp(kneel, this.rightForearm.xRot, -0.6F);
		this.leftForearm.xRot = Mth.lerp(kneel, this.leftForearm.xRot, -0.6F);
		this.rightArm.zRot = Mth.lerp(kneel, this.rightArm.zRot, 0.1F);
		this.leftArm.zRot = Mth.lerp(kneel, this.leftArm.zRot, -0.1F);
	}

	private void resolved(float walk, float phase, float stride, float breathe) {
		this.body.xRot = breathe * 0.015F;
		this.rightArm.zRot = 0.08F;
		this.leftArm.zRot = -0.08F;
		this.rightArm.xRot = -walk * 0.5F;
		this.leftArm.xRot = walk * 0.5F;
		// Hands folded before her.
		this.rightForearm.xRot = -0.9F + breathe * 0.03F;
		this.leftForearm.xRot = -0.9F - breathe * 0.03F;
		this.rightForearm.yRot = -0.35F;
		this.leftForearm.yRot = 0.35F;
		stride(walk, phase, stride, 1.0F);
	}

	// ---------------------------------------------------------------- beside the purifier

	/**
	 * Weakened but on her feet: she keeps low and side-on to the infection, dagger reversed at her chest and her left
	 * hand open toward it, ready to cast. The breath is still heavy from the purge.
	 */
	private void supportStance(float age, float walk, float phase, float stride) {
		float heave = Mth.sin(age * 0.13F);
		this.hips.yRot = 0.2F;
		rot(this.body, 0.16F + heave * 0.03F, 0.05F, 0.0F);
		rot(this.leftArm, -0.85F, 0.35F, -0.15F);
		rot(this.leftForearm, -0.75F + heave * 0.04F, 0.0F, 0.0F);
		rot(this.rightArm, -0.45F, -0.3F, 0.2F);
		rot(this.rightForearm, -1.55F, 0.0F, 0.0F);
		float settle = 1.0F - stride;
		this.leftLeg.xRot = -0.3F * settle;
		this.leftShin.xRot = 0.45F * settle;
		this.rightLeg.xRot = 0.25F * settle;
		this.rightShin.xRot = 0.45F * settle;
		this.leftLeg.yRot = -0.12F;
		this.rightLeg.yRot = 0.15F;
		stride(walk, phase, stride, 1.0F);
	}

	private void support(int action, float t, float age) {
		switch (action) {
			case AnnettaKnowlesEntity.ACTION_HEAL -> {
				// Both hands gather the still water at her breast, then open it out toward them.
				float w = window(t, 0.0F, 3.0F, 18.0F, 24.0F);
				float gather = window(t, 0.0F, 4.0F, 6.0F, 9.0F);
				float give = ramp(t, 7.0F, 11.0F) * w;
				toward(this.leftArm, gather, -1.0F, 0.6F, 0.0F);
				toward(this.leftForearm, gather, -1.6F, 0.0F, 0.0F);
				toward(this.rightArm, gather, -0.9F, -0.5F, 0.0F);
				toward(this.rightForearm, gather, -1.7F, 0.0F, 0.0F);
				toward(this.leftArm, give, -1.45F, 0.25F, -0.25F);
				toward(this.leftForearm, give, -0.15F, 0.0F, 0.0F);
				toward(this.rightArm, give, -1.3F, -0.25F, 0.25F);
				toward(this.rightForearm, give, -0.3F, 0.0F, 0.0F);
				this.body.xRot += 0.15F * gather - 0.08F * give;
				this.head.xRot += 0.25F * gather - 0.15F * give;
				this.head.zRot -= 0.1F * give;
			}
			case AnnettaKnowlesEntity.ACTION_ALLY_BOLT -> {
				// Palm drawn back to her shoulder with her side turned, then thrust out flat (the lance leaves on 9).
				float w = window(t, 0.0F, 2.0F, 12.0F, 18.0F);
				float ax = curve(t, 0, -0.85F, 5, -1.35F, 7.5F, -1.6F, 9, -1.55F, 14, -1.45F);
				float ay = curve(t, 0, 0.35F, 5, -0.35F, 7.5F, 0.0F, 9, 0.1F, 14, 0.1F);
				float fx = curve(t, 0, -0.75F, 5, -1.9F, 7.5F, -0.1F, 9, -0.05F, 14, -0.15F);
				toward(this.leftArm, w, ax, ay, -0.1F);
				toward(this.leftForearm, w, fx, 0.0F, 0.0F);
				float by = curve(t, 0, 0.05F, 5, -0.45F, 7.5F, 0.35F, 9, 0.4F, 14, 0.3F);
				this.body.yRot = Mth.lerp(w, this.body.yRot, by);
				this.body.xRot += 0.15F * ramp(t, 6.0F, 8.0F) * w;
				this.rightArm.xRot += 0.4F * ramp(t, 6.0F, 8.0F) * w;
				float step = ramp(t, 6.0F, 8.5F) * w;
				this.leftLeg.xRot -= 0.3F * step;
				this.leftShin.xRot += 0.3F * step;
				this.rightLeg.xRot += 0.15F * step;
			}
			case AnnettaKnowlesEntity.ACTION_ALLY_STRIKE -> {
				// Two quick cuts stepped into (hits on 7 and 14), and away.
				float w = window(t, 0.0F, 2.0F, 17.0F, 22.0F);
				this.bladeTrail = t > 3.5F && t < 16.5F;
				float ax = curve(t, 0, -0.45F, 4, -2.0F, 6, -1.4F, 8, -0.9F, 11, -0.95F, 13, -1.5F, 15, -2.0F, 18, -0.6F);
				float ay = curve(t, 0, -0.3F, 4, 0.95F, 6, 0.0F, 8, -0.85F, 11, -0.95F, 13, 0.1F, 15, 0.9F, 18, -0.2F);
				float fx = curve(t, 0, -1.55F, 4, -1.7F, 6, -0.35F, 8, -0.7F, 11, -1.6F, 13, -0.3F, 15, -0.6F, 18, -1.3F);
				toward(this.rightArm, w, ax, ay, 0.15F);
				toward(this.rightForearm, w, fx, 0.0F, 0.0F);
				float by = curve(t, 0, 0.05F, 4, 0.55F, 8, -0.5F, 11, -0.55F, 15, 0.4F, 18, 0.1F);
				this.body.yRot = Mth.lerp(w, this.body.yRot, by);
				this.hips.yRot = Mth.lerp(w, this.hips.yRot, curve(t, 0, 0.2F, 4, 0.4F, 8, 0.0F, 11, 0.0F, 15, 0.35F, 18, 0.2F));
				this.leftArm.yRot -= 0.3F * by * w;
				float lunge = curve(t, 0, 0.0F, 4, 0.1F, 7, 1.0F, 9.5F, 0.5F, 11, 0.3F, 14, 1.0F, 17, 0.3F, 20, 0.0F) * w;
				this.leftLeg.xRot -= 0.4F * lunge;
				this.leftShin.xRot += 0.4F * lunge;
				this.rightLeg.xRot += 0.25F * lunge;
				this.body.xRot += 0.15F * lunge;
			}
			case AnnettaKnowlesEntity.ACTION_STILL_DASH -> stillDash(t, 12.0F);
			case AnnettaKnowlesEntity.ACTION_FLINCH -> flinch(t);
			default -> {
			}
		}
	}

	// ---------------------------------------------------------------- grounding and cloth

	/**
	 * Drops the hips until the lower foot meets the floor, so a bent knee crouches her instead of shortening her leg
	 * into the air, and a stride gives her walk its bob.
	 */
	private void plantFeet() {
		float reach = Math.max(legReach(this.rightLeg, this.rightShin), legReach(this.leftLeg, this.leftShin));
		this.hips.y += 12.0F - reach;
	}

	/** How far below the hip joint this leg's lowest corner hangs, in model units. */
	private static float legReach(ModelPart thigh, ModelPart shin) {
		float shinAngle = thigh.xRot + shin.xRot;
		return 6.0F * Mth.cos(thigh.xRot) + 6.0F * Mth.cos(shinAngle) + 2.0F * Math.abs(Mth.sin(shinAngle));
	}

	/**
	 * The robe hangs from her hips, not her shoulders. The side panels hang halfway between the thighs so the robe
	 * opens at the sides as little as it can; the front and back panels each follow the thigh that pushes into them.
	 */
	private void drapeRobe() {
		float swing = this.robeSkirt.xRot;
		float front = Math.min(swing, Math.min(this.rightLeg.xRot, this.leftLeg.xRot));
		float back = Math.max(swing, Math.max(this.rightLeg.xRot, this.leftLeg.xRot));
		float sides = (front + back) * 0.5F;
		this.robeSkirt.xRot = sides;
		this.skirtFront.xRot = front - sides;
		this.skirtBack.xRot = back - sides;
	}

	// ---------------------------------------------------------------- held items

	@Override
	public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
		// Through the hips, torso, shoulder and elbow to the hand. Slim arms sit half a pixel inward as on the player
		// model, and held items expect the hand ten pixels down the arm, so the forearm frame is lifted back to a
		// virtual shoulder.
		ModelPart upper = arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
		ModelPart lower = arm == HumanoidArm.LEFT ? this.leftForearm : this.rightForearm;
		float inset = arm == HumanoidArm.RIGHT ? 0.5F : -0.5F;
		this.root.translateAndRotate(poseStack);
		this.hips.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		upper.x += inset;
		upper.translateAndRotate(poseStack);
		upper.x -= inset;
		lower.translateAndRotate(poseStack);
		poseStack.translate(-lower.x / 16.0F, -lower.y / 16.0F, 0.0F);
	}

	/**
	 * The frame the Unstained Shield is drawn in: strapped across the outside of her left forearm, its grip against the
	 * sleeve and its face looking out from the arm. Item space is y-up with the face toward -z, so the item is turned
	 * half round the (1, 0, -1) axis: its -z becomes the forearm's outward +x and its up runs back toward the elbow.
	 */
	public void translateToShield(PoseStack poseStack) {
		this.root.translateAndRotate(poseStack);
		this.hips.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		this.leftArm.translateAndRotate(poseStack);
		this.leftForearm.translateAndRotate(poseStack);
		poseStack.translate(SHIELD_OUT / 16.0F, SHIELD_ALONG / 16.0F, 0.0F);
		poseStack.mulPose(new Quaternionf().rotationAxis(Mth.PI, Mth.SQRT_OF_TWO * 0.5F, 0.0F, -Mth.SQRT_OF_TWO * 0.5F));
		poseStack.scale(SHIELD_SCALE, SHIELD_SCALE, SHIELD_SCALE);
	}
