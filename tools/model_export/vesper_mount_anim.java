
	/** Named attachment points other renderers hang effects and the rider from. */
	public enum Socket { SEAT, NECK_GRIP, WOUND_CARAPACE, WOUND_NECK, WOUND_TAIL, CLAW_R, CLAW_L, STINGER }

	private static final float DEG = Mth.DEG_TO_RAD;
	/** In flight the tail hooks under the belly with the stinger this far (radians) below dead ahead. */
	private static final float TAIL_HOOK_DROP = 0.5F;
	/*
	 * Right-claw offsets from the rest pose: arm twist, arm yaw, arm roll, elbow yaw, elbow roll, pincer yaw, finger.
	 * Folded: the upper arm hangs out, forward and down, twisted so the elbow bends level; the forearm crosses in
	 * under the chin and the pincer kinks forward. The left claw mirrors these.
	 */
	private static final float[] CLAW_FOLDED = { 0.785F, -0.185F, 0.5F, 0.96F, 0.52F, -0.53F, -0.25F };
	private static final float[] CLAW_COCKED = { 0.4F, -0.6F, 0.2F, 0.6F, 0.52F, -0.2F, 0.75F };
	private static final float[] CLAW_REACH = { 0.2F, 0.35F, 0.6F, -0.3F, 0.3F, 0.0F, 0.6F };
	private static final float[] FORE_FLARED = { 0.0F, -8.0F, -28.0F };
	private static final float[] FORE_FOLDED = { 0.0F, -86.0F, -10.0F };
	private static final float[] HIND_FLARED = { 0.0F, -30.0F, -16.0F };
	private static final float[] HIND_FOLDED = { 0.0F, -94.0F, -6.0F };
	/** Swing height of a stepping foot, and how far a sidestep reaches it in or out, in model units. */
	private static final float STEP_LIFT = 13.0F;
	private static final float STEP_REACH = 9.0F;
	private float mountScale = 1.0F;
	private float mountOpacity = 1.0F;
	private java.util.List<ModelPart> blendParts;

	@Override
	public void setupAnim(VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		VesperMountAnimState motion = VesperMountAnimState.of(entity);
		motion.update(entity, ageInTicks, partial);
		mountScale = 1.0F;
		mountOpacity = 1.0F;
		if (entity.getTransitionTick() > 0) {
			float absorbed = HemoEasing.smoothstep(VesperPhaseTransitionRules.absorptionProgress(
					motion.transitionTime()));
			mountOpacity = 1.0F - absorbed;
			mountScale = Math.max(0.03F, 1.0F - absorbed * 0.97F);
		}

		VesperWingedFlightRules.FlightMode flight = entity.getFlightMode();
		boolean airborne = flight.airborne();
		float flightTime = motion.flightTime();
		terrain(motion);
		idle(ageInTicks, entity.getActiveAnchor() >= 0);
		if (airborne) {
			fly(motion, flight, flightTime, ageInTicks, stingerAimDrop(entity, partial));
		} else {
			walk(motion, ageInTicks);
			VesperPhaseOneAttack attack = entity.getAttack();
			if (attack != VesperPhaseOneAttack.IDLE && attack != VesperPhaseOneAttack.WING_GROWTH) {
				attack(attack, motion.attackTime(), ageInTicks);
			}
			if (motion.staggerTime() > 0.0F) stagger(motion.staggerTime(), ageInTicks);
		}
		VesperBurrowRules.Stage burrow = entity.getBurrowStage();
		if (burrow != null) burrowPose(burrow, motion.burrowTime(), entity.getBurrowHuntTicks(), ageInTicks);
		wings(entity, motion, flight, flightTime, ageInTicks);
		applyCarapaceVisibility(entity);
		this.woundScute.visible = false;
		if (entity.getActiveAnchor() < 0 && !airborne) {
			this.neck.yRot += Mth.wrapDegrees(netHeadYaw) * DEG * 0.35F;
			this.head.yRot += Mth.wrapDegrees(netHeadYaw) * DEG * 0.25F;
			this.head.xRot += headPitch * DEG * 0.4F;
		}
		if (blendParts == null) {
			blendParts = this.root().getAllParts().filter(part -> part != this.woundScute).toList();
		}
		motion.blend(blendParts);
	}

	/** The whole mount rides the fitted ground plane: lifted or dropped onto it and tilted to its slope. */
	private void terrain(VesperMountAnimState motion) {
		this.root.y -= motion.bodyLift() * 16.0F;
		this.root.xRot += motion.terrainPitch();
		this.root.zRot += motion.terrainRoll();
	}

	// ---------------------------------------------------------------- idle and gait

	private void idle(float age, boolean wounded) {
		float breath = Mth.sin(age * 0.07F);
		this.abdomen.xRot += breath * 0.02F;
		this.body.y += breath * 0.35F;
		this.mandibleR.yRot += Math.max(0.0F, Mth.sin(age * 0.23F)) * 0.18F;
		this.mandibleL.yRot -= Math.max(0.0F, Mth.sin(age * 0.23F + 0.4F)) * 0.18F;
		this.antennaR.xRot += Mth.sin(age * 0.11F) * 0.12F;
		this.antennaL.xRot += Mth.sin(age * 0.11F + 1.3F) * 0.12F;
		this.antennaR.yRot += Mth.sin(age * 0.07F) * 0.1F;
		this.antennaL.yRot -= Mth.sin(age * 0.07F + 0.8F) * 0.1F;
		this.pincerFingerR.zRot += Math.max(0.0F, Mth.sin(age * 0.09F)) * 0.25F;
		this.pincerFingerL.zRot -= Math.max(0.0F, Mth.sin(age * 0.09F + 2.0F)) * 0.25F;
		this.clawArmR.yRot += Mth.sin(age * 0.05F) * 0.05F;
		this.clawArmL.yRot -= Mth.sin(age * 0.05F + 1.0F) * 0.05F;
		ModelPart[] tail = { this.tail0, this.tail1, this.tail2, this.tail3, this.tail4, this.tail5 };
		for (int i = 0; i < tail.length; i++) {
			tail[i].yRot += Mth.sin(age * 0.06F - i * 0.45F) * 0.05F;
			tail[i].xRot += Mth.sin(age * 0.08F - i * 0.5F) * 0.025F;
		}
		this.stinger.xRot += Mth.sin(age * 0.13F) * 0.08F;
		if (wounded) {
			float tremor = Mth.sin(age * 1.7F) * 0.02F;
			this.body.y += 3.0F;
			this.body.zRot += tremor;
			this.neck.xRot += 0.25F;
		}
	}

	/**
	 * The tripod gait, driven by how far the body actually travelled and turned rather than by vanilla limb swing,
	 * so planted feet hold still against the ground. Forward travel swings each coxa fore and aft, turning swings
	 * every coxa the same way around the body, and sidesteps reach the feet in and out. Each foot then reaches for
	 * the floor under it, so the legs follow steps and slopes instead of floating or clipping.
	 */
	private void walk(VesperMountAnimState motion, float age) {
		float cycle = motion.gaitPhase();
		float stride = motion.stride();
		float forward = motion.driveForward();
		float lateral = motion.driveLateral();
		float turn = motion.driveTurn();
		for (int pair = 0; pair < 3; pair++) {
			for (int side = 0; side < 2; side++) {
				boolean left = side == 1;
				int leg = VesperMountMotionRules.leg(pair, left);
				float sign = left ? -1.0F : 1.0F;
				float phase = VesperMountMotionRules.tripodPhase(leg, cycle);
				float lift = VesperMountMotionRules.footLift(phase) * stride;
				float sweep = VesperMountMotionRules.stepSweep(phase) * stride;
				ModelPart[] parts = legParts(pair, left);
				parts[0].yRot += sweep * (0.30F * forward * sign + 0.24F * turn);
				float down = motion.legDrop(leg) * 16.0F - STEP_LIFT * lift;
				float out = sweep * STEP_REACH * lateral * sign;
				plant(pair, left, Mth.clamp(down, -22.0F, 20.0F), out);
				// A swinging foot curls its hook under; a weight shift keeps a standing mount alive.
				float shift = Mth.sin(age * 0.05F + pair * 1.7F + side * 2.9F) * 0.025F * (1.0F - stride);
				parts[1].zRot += shift * sign;
				parts[2].zRot += (0.16F * lift - shift * 0.5F) * sign;
				parts[3].zRot += 0.32F * lift * sign;
			}
		}
		// Two tripod plants per cycle: the body settles onto each and rocks gently toward the planted side.
		float plants = cycle * 4.0F * Mth.PI;
		this.body.y += (0.5F + 0.5F * Mth.cos(plants - 0.6F)) * 0.9F * stride;
		this.body.zRot += Mth.sin(cycle * 2.0F * Mth.PI) * 0.03F * stride;
		this.body.xRot += -0.04F * forward * stride;
		this.abdomen.xRot += Mth.sin(plants - 0.6F) * 0.02F * stride;
		ModelPart[] tail = { this.tail0, this.tail1, this.tail2, this.tail3, this.tail4, this.tail5 };
		for (int i = 0; i < tail.length; i++) {
			tail[i].yRot += Mth.sin(cycle * 2.0F * Mth.PI - i * 0.5F) * 0.035F * stride;
		}
	}

	/** Moves one foot {@code down} and {@code out} model units with the femur and tibia, keeping the hook upright. */
	private void plant(int pair, boolean left, float down, float out) {
		if (down == 0.0F && out == 0.0F) return;
		float[] joints = VesperMountMotionRules.footOffset(down, out);
		ModelPart[] parts = legParts(pair, left);
		float sign = left ? -1.0F : 1.0F;
		parts[1].zRot += joints[0] * sign;
		parts[2].zRot += joints[1] * sign;
		parts[3].zRot += joints[2] * sign;
	}

	/** One leg: sweep turns the coxa (positive swings the foot forward), lift raises the femur and folds the tibia. */
	private void leg(int pair, boolean left, float sweep, float lift, float shift) {
		ModelPart[] parts = legParts(pair, left);
		float sign = left ? -1.0F : 1.0F;
		parts[0].yRot += sweep * sign;
		parts[1].zRot += (-0.42F * lift + shift) * sign;
		parts[2].zRot += (0.30F * lift - shift * 0.5F) * sign;
		parts[3].zRot += (0.35F * lift) * sign;
	}

	private ModelPart[] legParts(int pair, boolean left) {
		return switch (pair * 2 + (left ? 1 : 0)) {
			case 0 -> new ModelPart[] { this.leg0CoxaR, this.leg0FemurR, this.leg0TibiaR, this.leg0TarsusR };
			case 1 -> new ModelPart[] { this.leg0CoxaL, this.leg0FemurL, this.leg0TibiaL, this.leg0TarsusL };
			case 2 -> new ModelPart[] { this.leg1CoxaR, this.leg1FemurR, this.leg1TibiaR, this.leg1TarsusR };
			case 3 -> new ModelPart[] { this.leg1CoxaL, this.leg1FemurL, this.leg1TibiaL, this.leg1TarsusL };
			case 4 -> new ModelPart[] { this.leg2CoxaR, this.leg2FemurR, this.leg2TibiaR, this.leg2TarsusR };
			default -> new ModelPart[] { this.leg2CoxaL, this.leg2FemurL, this.leg2TibiaL, this.leg2TarsusL };
		};
	}

	/** Every leg crouches together: positive bends the knees so the body drops. */
	private void crouch(float amount) {
		for (int i = 0; i < 6; i++) {
			ModelPart[] parts = legParts(i / 2, i % 2 == 1);
			float sign = i % 2 == 1 ? -1.0F : 1.0F;
			parts[1].zRot += -0.28F * amount * sign;
			parts[2].zRot += 0.22F * amount * sign;
		}
		this.body.y += 5.0F * amount;
	}

	// ---------------------------------------------------------------- attacks

	private void attack(VesperPhaseOneAttack attack, float t, float age) {
		VesperMoveTimeline timeline = VesperMountControllerRules.timeline(attack);
		float windup = timeline.windup();
		float wind = HemoEasing.smoothstep(t / Math.max(1.0F, windup));
		float recover = HemoEasing.smoothstep(
				(t - windup - timeline.active()) / Math.max(1.0F, timeline.recovery()));
		float hold = 1.0F - recover;
		switch (attack) {
			case ROYAL_SCUTTLE -> {
				// Head down like a bull, claws swept back; the rush throws them forward and the skid rocks it back.
				float load = wind * (1.0F - ramp(t, windup - 1.0F, windup + 3.0F));
				float charge = ramp(t, windup - 1.0F, windup + 3.0F) * hold;
				float skid = pulse(t, windup + timeline.active(), 12.0F);
				crouch(0.9F * load + 0.35F * charge);
				this.body.xRot += 0.16F * load + 0.1F * charge - 0.12F * skid;
				this.neck.xRot += 0.3F * load + 0.15F * charge;
				this.clawArmR.yRot += -0.45F * load + 0.45F * charge;
				this.clawArmL.yRot -= -0.45F * load + 0.45F * charge;
				this.clawArmR.zRot += 0.25F * load - 0.2F * charge;
				this.clawArmL.zRot -= 0.25F * load - 0.2F * charge;
				this.pincerFingerR.zRot += 0.7F * charge;
				this.pincerFingerL.zRot -= 0.7F * charge;
				this.tail0.xRot += 0.2F * load + 0.12F * charge;
				this.mandibleR.yRot += 0.35F * charge;
				this.mandibleL.yRot -= 0.35F * charge;
				if (load > 0.0F) this.body.zRot += Mth.sin(age * 1.1F) * 0.012F * load;
			}
			case PINCER_VICE -> {
				// Rear back and gape, snap both claws shut at 24, re-cock, and snap again at 38.
				float firstCock = wind * (1.0F - ramp(t, 22.5F, 24.0F));
				float recock = ramp(t, 27.0F, 35.0F) * (1.0F - ramp(t, 36.5F, 38.0F));
				float open = Math.max(firstCock, 0.8F * recock);
				float strike = Math.max(pulse(t, 24.0F, 9.0F), pulse(t, 38.0F, 10.0F));
				float reach = Math.max(ramp(t, 22.5F, 24.0F) * (1.0F - ramp(t, 27.0F, 33.0F)),
						ramp(t, 36.5F, 38.0F)) * hold;
				this.body.xRot += -0.1F * open + 0.12F * strike + 0.05F * reach;
				this.body.y += -1.5F * open + 1.5F * strike;
				this.neck.xRot += -0.15F * open + 0.2F * strike;
				this.clawArmR.zRot += -0.85F * open + 0.2F * reach;
				this.clawArmL.zRot -= -0.85F * open + 0.2F * reach;
				this.clawArmR.yRot += -0.6F * open + 0.75F * reach + 0.15F * strike;
				this.clawArmL.yRot -= -0.6F * open + 0.75F * reach + 0.15F * strike;
				this.clawForeR.yRot += -0.3F * open + 0.35F * reach;
				this.clawForeL.yRot -= -0.3F * open + 0.35F * reach;
				this.pincerFingerR.zRot += 1.05F * open - 0.2F * strike;
				this.pincerFingerL.zRot -= 1.05F * open - 0.2F * strike;
				this.mandibleR.yRot += 0.3F * open;
				this.mandibleL.yRot -= 0.3F * open;
				for (int i = 0; i < 2; i++) leg(0, i == 1, 0.08F * open, 0.0F, -0.06F * strike);
			}
			case STINGER_SCRIPT -> {
				// The tail rears high and stays cocked; each volley it drives down and recoils, the body bracing.
				float rear = wind * hold;
				float stab = 0.0F;
				float cock = 0.0F;
				for (float volley = 16.0F; volley <= 52.0F; volley += 12.0F) {
					stab = Math.max(stab, pulse(t, volley, 7.0F));
					cock = Math.max(cock, ramp(t, volley - 5.0F, volley - 1.0F) * (1.0F - ramp(t, volley - 1.0F, volley)));
				}
				cock *= hold;
				this.tail0.xRot += 0.28F * rear + 0.1F * cock - 0.12F * stab;
				this.tail1.xRot += 0.12F * rear + 0.08F * cock;
				this.tail2.xRot += 0.2F * rear + 0.12F * cock;
				this.tail3.xRot += 0.15F * cock + 0.25F * stab;
				this.tail4.xRot += -0.15F * cock + 0.5F * stab;
				this.tail5.xRot += -0.15F * cock + 0.55F * stab;
				this.stinger.xRot += -0.3F * cock + 0.75F * stab;
				this.body.xRot += 0.05F * rear + 0.05F * stab;
				this.body.y += 1.2F * stab;
				crouch(0.25F * rear);
			}
			case BROOD_TRAMPLE -> {
				// Sink to load the legs, spring at 22 rearing with the front legs pawing, and stamp down at 38.
				float load = wind * (1.0F - ramp(t, 21.0F, 23.0F));
				float air = ramp(t, 21.0F, 25.0F) * (1.0F - ramp(t, 35.0F, 38.0F));
				float slam = pulse(t, 38.0F, 14.0F);
				float paw = Mth.sin((t - 22.0F) * 0.7F) * air;
				crouch(1.1F * load + 0.9F * slam);
				this.body.xRot += 0.1F * load - 0.42F * air + 0.14F * slam;
				this.body.y += -3.0F * air;
				this.neck.xRot += -0.25F * air + 0.35F * slam;
				for (int side = 0; side < 2; side++) {
					boolean left = side == 1;
					leg(0, left, 0.25F * air + 0.08F * paw * (left ? -1.0F : 1.0F), air * (0.85F + 0.15F * paw), 0.0F);
					leg(1, left, -0.1F * air, 0.55F * air, 0.0F);
					leg(2, left, -0.15F * air, -0.25F * air, 0.0F);
					for (int pair = 0; pair < 3; pair++) leg(pair, left, 0.0F, 0.0F, -0.12F * slam);
				}
				this.clawArmR.zRot += -0.65F * air + 0.2F * slam;
				this.clawArmL.zRot -= -0.65F * air + 0.2F * slam;
				this.tail0.xRot += 0.2F * air - 0.25F * slam;
			}
			case PUPPET_MUSTER, RECALL -> {
				crouch(0.6F * wind * hold);
				this.neck.xRot += 0.35F * wind * hold;
				this.tail0.xRot += 0.15F * wind * hold;
				this.antennaR.xRot += -0.3F * wind * hold;
				this.antennaL.xRot += -0.3F * wind * hold;
			}
			case CARAPACE_ANEURYSM -> {
				// Brace and swell, burst at 24, shudder while the carapace is open, and swell back as it regrows.
				float brace = wind * hold;
				float swell = wind * (1.0F - ramp(t, 24.0F, 27.0F));
				float burst = pulse(t, 24.0F, 8.0F);
				float exposed = ramp(t, 25.0F, 30.0F) * (1.0F - ramp(t, 85.0F, 105.0F));
				crouch(brace);
				for (int i = 0; i < 6; i++) leg(i / 2, i % 2 == 1, 0.0F, 0.0F, -0.08F * brace);
				this.thorax.xScale = this.thorax.zScale = 1.0F + 0.05F * swell + 0.08F * burst;
				this.abdomen.xScale = this.abdomen.zScale = 1.0F + 0.08F * swell + 0.12F * burst;
				this.abdomen.yScale = 1.0F + 0.06F * swell;
				this.body.y += -2.0F * burst;
				if (swell > 0.0F) this.body.zRot += Mth.sin(age * 2.1F) * 0.01F * swell;
				this.body.zRot += Mth.sin(age * 1.3F) * 0.025F * exposed;
				this.neck.xRot += 0.3F * exposed;
				this.tail0.xRot += -0.2F * exposed;
			}
			case GRAB_IMPALEMENT -> {
				float telegraph = wind * (1.0F - ramp(t, 13.0F, 15.0F));
				float lunge = pulse(t, 15.0F, 8.0F);
				float lift = ramp(t, 20.0F, 29.0F) * hold;
				float bite = pulse(t, 30.0F, 6.0F);
				float coil = ramp(t, 30.0F, 41.0F) * (1.0F - ramp(t, 41.0F, 42.0F));
				float stab = pulse(t, 42.0F, 8.0F);
				crouch(0.5F * telegraph);
				this.body.xRot += 0.08F * telegraph + 0.1F * lunge - 0.1F * lift;
				this.clawArmR.yRot += -0.35F * telegraph + 0.6F * lunge + 0.3F * lift;
				this.clawArmL.yRot -= -0.35F * telegraph + 0.6F * lunge + 0.3F * lift;
				this.clawArmR.zRot += -0.7F * lift;
				this.clawArmL.zRot -= -0.7F * lift;
				this.pincerFingerR.zRot += 0.7F * (telegraph + lunge) * (1.0F - lift);
				this.pincerFingerL.zRot -= 0.7F * (telegraph + lunge) * (1.0F - lift);
				this.head.xRot += -0.25F * bite;
				this.mandibleR.yRot += 0.5F * coil * (1.0F - bite) - 0.2F * bite;
				this.mandibleL.yRot -= 0.5F * coil * (1.0F - bite) - 0.2F * bite;
				this.tail0.xRot += -0.3F * coil + 0.25F * stab;
				this.tail3.xRot += 0.3F * coil;
				this.stinger.xRot += -0.5F * coil + 0.9F * stab;
			}
			default -> { }
		}
	}

	/** Smoothstep from 0 at {@code from} to 1 at {@code to}. */
	private static float ramp(float t, float from, float to) {
		return HemoEasing.smoothstep((t - from) / Math.max(0.001F, to - from));
	}

	/** Knocked off balance: the legs buckle, the head drops and the body wobbles out of it as the count runs down. */
	private void stagger(float ticksLeft, float age) {
		float s = HemoEasing.smoothstep(ticksLeft / VesperMountControllerRules.STAGGER_TICKS);
		crouch(0.8F * s);
		this.body.zRot += Mth.sin(age * 0.9F) * 0.07F * s;
		this.body.xRot += -0.08F * s;
		this.neck.xRot += 0.4F * s;
		this.head.zRot += Mth.sin(age * 0.6F) * 0.12F * s;
		this.tail0.xRot -= 0.3F * s;
		this.clawArmR.zRot += 0.3F * s;
		this.clawArmL.zRot -= 0.3F * s;
	}

	/** A strike curve: fast rise at `at`, overshoot, then decay over `length` ticks. */
	private static float pulse(float t, float at, float length) {
		float x = t - at + 2.0F;
		if (x <= 0.0F || x >= length + 2.0F) return 0.0F;
		if (x < 2.0F) return VesperMountMotionRules.overshoot(x / 2.0F);
		return 1.0F - HemoEasing.smoothstep((x - 2.0F) / length);
	}

	/** The burrow's body language: plunge, a claw or tail driven up through the floor, the eruption, the shake-off. */
	private void burrowPose(VesperBurrowRules.Stage stage, float t, int huntTicks, float age) {
		switch (stage) {
			case DIVE -> {
				float p = Mth.clamp(t / VesperBurrowRules.DIVE_TICKS, 0.0F, 1.0F);
				float rear = Mth.sin(Mth.clamp(p * 2.0F, 0.0F, 1.0F) * Mth.PI);
				float plunge = HemoEasing.smoothstep((p - 0.4F) / 0.6F);
				this.body.xRot += -0.35F * rear + 0.95F * plunge;
				this.body.y += 10.0F * plunge;
				this.neck.xRot += 0.6F * plunge;
				this.clawArmR.zRot += 0.4F * plunge;
				this.clawArmL.zRot -= 0.4F * plunge;
				this.clawArmR.yRot += 0.7F * plunge;
				this.clawArmL.yRot -= 0.7F * plunge;
				this.tail0.xRot += 0.3F * plunge;
			}
			case STRIKE_TELL -> {
				// Coiling under the floor for the next strike: claws drawn in, or the tail cocked back.
				int index = VesperBurrowRules.strikeIndexAt((int) t);
				float local = (t - VesperBurrowRules.DIVE_TICKS) % VesperBurrowRules.STRIKE_INTERVAL
						- (VesperBurrowRules.STRIKE_INTERVAL - VesperBurrowRules.STRIKE_TICKS - VesperBurrowRules.STRIKE_TELL_TICKS);
				float coil = HemoEasing.smoothstep(local / VesperBurrowRules.STRIKE_TELL_TICKS);
				if (VesperBurrowRules.strikeAt(index) == VesperBurrowRules.Strike.CLAW) {
					this.body.xRot += 0.25F * coil;
					this.clawArmR.zRot += 0.4F * coil;
					this.clawArmL.zRot -= 0.4F * coil;
					this.clawArmR.yRot += -0.3F * coil;
					this.clawArmL.yRot -= -0.3F * coil;
				} else {
					this.body.xRot += -0.2F * coil;
					this.tail0.xRot += 0.35F * coil;
					this.tail2.xRot += 0.25F * coil;
				}
			}
			case STRIKE -> {
				int index = VesperBurrowRules.strikeIndexAt((int) t);
				float local = (t - VesperBurrowRules.DIVE_TICKS) % VesperBurrowRules.STRIKE_INTERVAL
						- (VesperBurrowRules.STRIKE_INTERVAL - VesperBurrowRules.STRIKE_TICKS);
				float thrust = VesperMountMotionRules.overshoot(Mth.clamp(local / 3.0F, 0.0F, 1.0F))
						* (1.0F - HemoEasing.smoothstep((local - 4.0F) / 4.0F));
				if (VesperBurrowRules.strikeAt(index) == VesperBurrowRules.Strike.CLAW) {
					this.body.xRot += -0.75F * thrust;
					this.clawArmR.zRot += -1.1F * thrust;
					this.clawArmL.zRot -= -1.1F * thrust;
					this.clawArmR.yRot += 0.5F * thrust;
					this.clawArmL.yRot -= 0.5F * thrust;
					this.pincerFingerR.zRot += 0.9F * thrust;
					this.pincerFingerL.zRot -= 0.9F * thrust;
				} else {
					this.body.xRot += 0.55F * thrust;
					this.tail0.xRot -= 0.5F * thrust;
					this.tail1.xRot -= 0.45F * thrust;
					this.tail2.xRot -= 0.4F * thrust;
					this.stinger.xRot -= 0.6F * thrust;
				}
			}
			case ERUPTION -> {
				int start = VesperBurrowRules.eruptionStart(new VesperBurrowRules.Plan(huntTicks, false));
				float p = Mth.clamp((t - start) / VesperBurrowRules.ERUPTION_TICKS, 0.0F, 1.0F);
				float burst = VesperMountMotionRules.overshoot(p);
				this.body.xRot += -0.8F * burst;
				this.clawArmR.zRot += -0.9F * burst;
				this.clawArmL.zRot -= -0.9F * burst;
				for (int i = 0; i < 6; i++) leg(i / 2, i % 2 == 1, 0.0F, 0.8F * burst, 0.0F);
				this.tail0.xRot += 0.25F * burst;
			}
			case RECOVERY -> {
				float shake = Mth.sin(age * 0.85F) * 0.09F;
				crouch(0.7F);
				this.body.zRot += shake;
				this.neck.xRot += 0.45F;
				this.neck.yRot += shake * 2.0F;
				this.clawArmR.zRot += 0.25F;
				this.clawArmL.zRot -= 0.25F;
			}
			default -> { }
		}
	}

	/** The aneurysm blows the scutes off front to back, then they regrow in the same order. */
	private void applyCarapaceVisibility(VesperTheCrownedRefusalEntity entity) {
		boolean aneurysm = entity.getAttack() == VesperPhaseOneAttack.CARAPACE_ANEURYSM;
		int tick = entity.getAttackTick();
		this.scuteFront.visible = !aneurysm || tick < 25 || tick >= 91;
		this.scuteMid.visible = !aneurysm || tick < 25 || tick >= 98;
		this.scuteRear.visible = !aneurysm || tick < 25 || tick >= 105;
	}

	// ---------------------------------------------------------------- flight and wings

	private void fly(VesperMountAnimState motion, VesperWingedFlightRules.FlightMode mode, float t, float age,
			float aimDrop) {
		float bank = motion.bank() * DEG;
		this.body.zRot += bank;
		this.body.xRot += -motion.flightPitch() * DEG;
		// Each wingbeat heaves the body; the hanging limbs and tail answer a beat late, like carried weight.
		float beat = switch (mode) {
			case TAKEOFF -> 1.0F;
			case CIRCLING, TAIL_FUSILLADE, LANDING -> 0.7F;
			case DIVE_TELEGRAPH -> 0.35F;
			default -> 0.0F;
		};
		float phase = age * 1.35F;
		this.body.y += Mth.sin(phase) * 1.2F * beat;
		this.body.xRot += Mth.sin(phase + 0.6F) * 0.025F * beat;
		float lag = Mth.sin(phase - 1.2F) * beat;
		// Limbs fold once it is clear of the floor and reach for it again at the end of a landing.
		float tuck = switch (mode) {
			case TAKEOFF -> HemoEasing.smoothstep(Mth.clamp((t - 4.0F) / 10.0F, 0.0F, 1.0F));
			case LANDING -> 1.0F - HemoEasing.smoothstep(Mth.clamp(
					(t - VesperWingedFlightRules.LANDING_TICKS + 12.0F) / 10.0F, 0.0F, 1.0F));
			default -> 1.0F;
		};
		flightLegs(tuck, lag);
		flightClaws(mode, t, tuck, lag, bank);
		float drop = switch (mode) {
			case DIVING_REND -> 1.1F;
			case TAIL_FUSILLADE -> Mth.lerp(HemoEasing.smoothstep(Mth.clamp(t / 8.0F, 0.0F, 1.0F)),
					TAIL_HOOK_DROP, aimDrop);
			default -> TAIL_HOOK_DROP;
		};
		curlTailUnder(drop);
		this.tail0.xRot += lag * 0.03F;
		this.tail2.xRot += lag * 0.04F;
		// The head hangs to watch the ground below, and follows the stinger down onto the mark when it fires.
		float look = Mth.clamp(drop - TAIL_HOOK_DROP, -0.2F, 0.9F);
		this.neck.xRot += 0.6F + 0.3F * look;
		this.head.xRot += 0.55F + 0.35F * look;
		if (mode == VesperWingedFlightRules.FlightMode.TAIL_FUSILLADE) {
			// Each volley kicks the stinger back against the curl, then it settles on the mark again.
			float flick = Math.max(pulse(t, 12.0F, 4.0F), Math.max(pulse(t, 16.0F, 4.0F), pulse(t, 20.0F, 4.0F)));
			this.tail4.xRot -= 0.12F * flick;
			this.tail5.xRot -= 0.18F * flick;
			this.stinger.xRot += 0.35F * flick;
		}
	}

	/** Legs fold tight against the belly and trail back out of the airstream, the rear pairs furthest. */
	private void flightLegs(float tuck, float lag) {
		for (int i = 0; i < 6; i++) {
			ModelPart[] parts = legParts(i / 2, i % 2 == 1);
			float sign = i % 2 == 1 ? -1.0F : 1.0F;
			float sweep = 0.55F + 0.2F * (i / 2);
			parts[0].yRot += -sweep * sign * tuck;
			parts[1].zRot += 0.55F * sign * tuck;
			parts[2].zRot += (0.75F * tuck + lag * 0.06F) * sign;
			parts[3].zRot += 0.45F * sign * tuck;
		}
	}

	/**
	 * Claws ride folded under the chin like a crab's, pincers shut, swinging a little against the turns. The dive
	 * telegraph draws them back with the pincers gaping; the rend throws them forward and down to tear.
	 */
	private void flightClaws(VesperWingedFlightRules.FlightMode mode, float t, float tuck, float lag, float bank) {
		float[] pose = new float[CLAW_FOLDED.length];
		for (int i = 0; i < pose.length; i++) pose[i] = CLAW_FOLDED[i] * tuck;
		if (mode == VesperWingedFlightRules.FlightMode.DIVE_TELEGRAPH) {
			blendClaws(pose, CLAW_COCKED, HemoEasing.smoothstep(Mth.clamp(t / 10.0F, 0.0F, 1.0F)));
		} else if (mode == VesperWingedFlightRules.FlightMode.DIVING_REND) {
			blendClaws(pose, CLAW_COCKED, 1.0F - Mth.clamp(t / 3.0F, 0.0F, 1.0F));
			blendClaws(pose, CLAW_REACH, VesperMountMotionRules.overshoot(Mth.clamp(t / 6.0F, 0.0F, 1.0F)));
		}
		float sway = lag * 0.05F;
		this.clawArmR.xRot += pose[0];
		this.clawArmL.xRot += pose[0];
		this.clawArmR.yRot += pose[1];
		this.clawArmL.yRot -= pose[1];
		this.clawArmR.zRot += pose[2] - bank * 0.25F + sway;
		this.clawArmL.zRot -= pose[2] + bank * 0.25F + sway;
		this.clawForeR.yRot += pose[3];
		this.clawForeL.yRot -= pose[3];
		this.clawForeR.zRot += pose[4];
		this.clawForeL.zRot -= pose[4];
		this.pincerR.yRot += pose[5];
		this.pincerL.yRot -= pose[5];
		this.pincerFingerR.zRot += pose[6];
		this.pincerFingerL.zRot -= pose[6];
	}

	private static void blendClaws(float[] pose, float[] target, float amount) {
		for (int i = 0; i < pose.length; i++) pose[i] = Mth.lerp(amount, pose[i], target[i]);
	}

	/**
	 * Bends the tail down and forward under the body until the stinger points {@code drop} radians below dead ahead.
	 * The root takes most of the bend so the tail drops straight off the abdomen and runs forward under the belly;
	 * any aim past the resting hook goes to the last segments, so only the sting end turns onto the mark.
	 */
	private void curlTailUnder(float drop) {
		ModelPart[] chain = { this.tail0, this.tail1, this.tail2, this.tail3, this.tail4, this.tail5, this.stinger };
		float[] share = { 0.32F, 0.22F, 0.17F, 0.13F, 0.08F, 0.04F, 0.04F };
		float[] aimShare = { 0.0F, 0.0F, 0.0F, 0.2F, 0.25F, 0.25F, 0.3F };
		float bend = this.body.xRot + this.abdomen.xRot;
		for (ModelPart part : chain) bend += part.xRot;
		// A part along +z rotated by x points forward and below by (pi + x), so the chain needs drop - pi in total.
		float delta = TAIL_HOOK_DROP - Mth.PI - bend;
		float aim = drop - TAIL_HOOK_DROP;
		for (int i = 0; i < chain.length; i++) chain[i].xRot += delta * share[i] + aim * aimShare[i];
	}

	/** How far below dead ahead the locked flight aim sits from the mount, for the fusillade's stinger. */
	private static float stingerAimDrop(VesperTheCrownedRefusalEntity entity, float partial) {
		var aim = entity.getLockedFlightAim();
		float yaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot) * DEG;
		double dx = aim.x - Mth.lerp(partial, entity.xo, entity.getX());
		double dz = aim.z - Mth.lerp(partial, entity.zo, entity.getZ());
		double ahead = -Mth.sin(yaw) * dx + Mth.cos(yaw) * dz;
		double below = Mth.lerp(partial, entity.yo, entity.getY()) - aim.y;
		return (float) Mth.clamp(Math.atan2(below, ahead), 0.35D, 2.2D);
	}

	private void wings(VesperTheCrownedRefusalEntity entity, VesperMountAnimState motion,
			VesperWingedFlightRules.FlightMode mode, float t, float age) {
		boolean grown = entity.hasWingsGrown();
		ModelPart[] wingParts = { this.wingForeR, this.wingForeL, this.wingHindR, this.wingHindL };
		for (ModelPart wing : wingParts) wing.visible = grown;
		if (!grown) return;
		float spread = motion.wingSpread(VesperMountMotionRules.wingSpread(mode, t));
		float growth = mode == VesperWingedFlightRules.FlightMode.WING_GROWTH
				? VesperMountMotionRules.overshoot(Mth.clamp(t / 30.0F, 0.0F, 1.0F)) : 1.0F;
		float beat = 0.0F;
		float shiver = 0.0F;
		float cup = 0.0F;
		float sweepBack = 0.0F;
		switch (mode) {
			case WING_GROWTH -> shiver = Mth.sin(age * 2.6F) * 0.06F * (1.0F - Mth.clamp((t - 40.0F) / 20.0F, 0.0F, 1.0F));
			case TAKEOFF -> beat = 1.0F;
			case CIRCLING, TAIL_FUSILLADE, LANDING -> beat = 0.7F;
			case DIVE_TELEGRAPH -> cup = 1.0F;
			case DIVING_REND -> sweepBack = 1.0F;
			default -> { }
		}
		float phase = age * 1.35F;
		wing(this.wingForeR, this.wingForeL, FORE_FOLDED, FORE_FLARED, spread, growth,
				Mth.sin(phase) * 0.55F * beat + shiver, cup, sweepBack);
		wing(this.wingHindR, this.wingHindL, HIND_FOLDED, HIND_FLARED, spread, growth,
				Mth.sin(phase - Mth.HALF_PI) * 0.5F * beat + shiver, cup, sweepBack);
	}

	private void wing(ModelPart right, ModelPart left, float[] folded, float[] flared, float spread, float growth,
			float flap, float cup, float sweepBack) {
		float y = Mth.lerp(spread, folded[1], flared[1]) * DEG - 0.55F * sweepBack;
		float z = Mth.lerp(spread, folded[2], flared[2]) * DEG + flap + 0.35F * cup;
		float x = 0.45F * cup;
		// Rest pose is the flared rotation; replace it rather than add to it.
		right.xRot = x;
		right.yRot = y;
		right.zRot = z;
		left.xRot = x;
		left.yRot = -y;
		left.zRot = -z;
		right.xScale = right.yScale = right.zScale = growth;
		left.xScale = left.yScale = left.zScale = growth;
	}

	// ---------------------------------------------------------------- sockets and rendering

	public void translateToSeat(PoseStack poseStack) {
		chain(poseStack, this.root, this.body, this.thorax, this.seat);
	}

	public void translateToSocket(PoseStack poseStack, Socket socket) {
		switch (socket) {
			case SEAT -> translateToSeat(poseStack);
			case NECK_GRIP -> chain(poseStack, this.root, this.body, this.thorax, this.neck, this.burrowNeckGrip);
			case WOUND_NECK -> chain(poseStack, this.root, this.body, this.thorax, this.neck, this.woundNeck);
			case WOUND_CARAPACE -> chain(poseStack, this.root, this.body, this.abdomen, this.woundCarapace);
			case WOUND_TAIL -> chain(poseStack, this.root, this.body, this.abdomen, this.woundTail);
			case CLAW_R -> chain(poseStack, this.root, this.body, this.thorax, this.clawArmR, this.clawForeR,
					this.pincerR, this.clawTipR);
			case CLAW_L -> chain(poseStack, this.root, this.body, this.thorax, this.clawArmL, this.clawForeL,
					this.pincerL, this.clawTipL);
			case STINGER -> chain(poseStack, this.root, this.body, this.abdomen, this.tail0, this.tail1, this.tail2,
					this.tail3, this.tail4, this.tail5, this.stinger, this.stingerTip);
		}
	}

	private static void chain(PoseStack poseStack, ModelPart... parts) {
		for (ModelPart part : parts) part.translateAndRotate(poseStack);
	}

	public float mountOpacity() {
		return mountOpacity;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
			int packedColor) {
		setMembranesVisible(false);
		int alpha = Mth.clamp(Math.round((packedColor >>> 24) * mountOpacity), 0, 255);
		renderMountAssembly(poseStack, consumer, packedLight, packedOverlay, (alpha << 24) | (packedColor & 0x00FFFFFF));
		setMembranesVisible(true);
	}

	/** One split scute of an open wound, hinged open by {@code xRot} about its outer edge; drawn by the wound layer. */
	public void renderWoundScute(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
			float xRot) {
		this.woundScute.visible = true;
		this.woundScute.xRot = xRot;
		this.woundScute.render(poseStack, consumer, packedLight, packedOverlay);
		this.woundScute.visible = false;
	}

	/** The whole mount at its current absorption scale (the transition melts it toward the rider). */
	public void renderMountAssembly(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
			int packedColor) {
		poseStack.pushPose();
		if (mountScale < 1.0F) poseStack.scale(mountScale, Math.max(0.02F, mountScale * 0.72F), mountScale);
		this.root().render(poseStack, consumer, packedLight, packedOverlay, packedColor);
		poseStack.popPose();
	}

	/** Only the wing membranes, for the translucent pass. */
	public void renderMembranes(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
			int packedColor) {
		if (!this.wingForeR.visible && !this.wingHindR.visible) return;
		// Each membrane is drawn alone along its own chain, so nothing else enters the translucent pass.
		ModelPart[][] chains = {
				{ this.root, this.body, this.thorax, this.wingForeR, this.wingForeMembraneR },
				{ this.root, this.body, this.thorax, this.wingForeL, this.wingForeMembraneL },
				{ this.root, this.body, this.thorax, this.wingHindR, this.wingHindMembraneR },
				{ this.root, this.body, this.thorax, this.wingHindL, this.wingHindMembraneL } };
		poseStack.pushPose();
		if (mountScale < 1.0F) poseStack.scale(mountScale, Math.max(0.02F, mountScale * 0.72F), mountScale);
		for (int i = 0; i < chains.length; i++) {
			ModelPart[] chain = chains[i];
			if (!chain[3].visible) continue;
			poseStack.pushPose();
			for (int k = 0; k < chain.length - 1; k++) chain[k].translateAndRotate(poseStack);
			ModelPart membrane = chain[chain.length - 1];
			membrane.visible = true;
			membrane.render(poseStack, consumer, packedLight, packedOverlay, packedColor);
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	private void setMembranesVisible(boolean visible) {
		this.wingForeMembraneR.visible = visible;
		this.wingForeMembraneL.visible = visible;
		this.wingHindMembraneR.visible = visible;
		this.wingHindMembraneL.visible = visible;
	}
