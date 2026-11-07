	// ---------------------------------------------------------------- animation (tools/model_export/hemorath_anim.java)
	// A frame is built in layers, each a set of channels: the stance (his body at rest, blended continuously by the
	// meter: frail and quick hollow, slow and swaying full), the stalk (a three-beat gait keyed to distance, with the
	// lurch and the turn read off his real motion), then the playing track as deltas over both (Catmull-Rom keys,
	// HemoEasing.catmullRom, on the move's own replayed clock: anticipation, a committed strike, follow-through past the
	// line and a weighted settle back to zero). HemorathAnimState blends a changed track out of the last shown pose by
	// inertialization, and its springs then write the overlap: the head and spine lagging the shoulders, the hanging
	// chains, the vestments, the ribs and the face cords. pose() lays it all on the rig: the knees fold and the hips sink
	// to the floor, the staff's foot stays planted where it is put while the grip slides on the shaft.

	static final int BOW = 0, TWIST = 1, LEAN = 2, CROUCH = 3, R_ARM_X = 4, R_ARM_Z = 5, R_ARM_Y = 6, R_FORE = 7,
			R_HAND = 8, FINGERS = 9, L_ARM_X = 10, L_ARM_Z = 11, L_FORE = 12, HEAD = 13, RIBS = 14, CLAVICLE = 15,
			STEP_L = 16, STEP_R = 17, FACE = 18, HALO_AWAY = 19, CHAIN = 20, SHAKE = 21, SLUMP = 22, HEAD_TURN = 23,
			HIP_SWAY = 24, HALO_HELD = 25, SPREAD = 26, HALO_DROP = 27, HIP_X = 28, HIP_TWIST = 29, HEAD_ROLL = 30,
			L_ARM_Y = 31, LIFT_L = 32, LIFT_R = 33, CLAW_FLEX = 34, CLENCH = 35, STAFF_PLANT = 36, STAFF_X = 37,
			STAFF_Z = 38, STAFF_FOOT_X = 39, STAFF_FOOT_Y = 40, STAFF_FOOT_Z = 41, HALO_GROUND = 42, L_HAND = 43,
			// the overlap, written by the springs after the blend
			LAG_HEAD_X = 44, LAG_HEAD_Y = 45, LAG_HEAD_Z = 46, LAG_SPINE = 47, LAG_FORE = 48, LAG_FINGERS = 49,
			SWING_X = 50, SWING_Z = 51, STAFF_SWING_X = 52, STAFF_SWING_Z = 53, ROBE_X = 54, ROBE_Z = 55,
			SHROUD_X = 56, TRAIN_Y = 57, RIB_JIG = 58, CORD_X = 59, CORD_Z = 60, HALO_SWING_X = 61,
			HALO_SWING_Z = 62, MASS_JIG = 63, FLINCH = 64, BEND_X = 65, BEND_Z = 66, STAFF_BEND_X = 67,
			STAFF_BEND_Z = 68, TURN_SWING = 69, CHANNELS = 70;
	/** The first channel the springs own. */
	static final int SECONDARY = LAG_HEAD_X;
	/** A spring's trail never turns a part further than this (a body snapped round by a teleport settles from it). */
	static final float TRAIL_LIMIT = 0.7F;
	/** The grip slides along the shaft at most this far either way, model units. */
	static final float GRIP_SLIDE = 7.0F;

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float partial = AnimationTime.partialTick();
		HemorathAnimState motion = HemorathAnimState.of(entity);
		motion.update(entity, partial, ageInTicks);
		float swell = motion.swell();
		float[] act = new float[CHANNELS];
		stance(act, swell, ageInTicks, motion.breath(), motion.sway(), 1.0F - motion.busy());
		stalk(act, swell, limbSwing, limbSwingAmount, motion.lurch(), motion.turn(), motion.turnTravel(), motion.busy());
		track(act, motion);
		motion.blend(act, ageInTicks);
		secondary(act, motion, swell);
		pose(act, swell, motion.patches(), motion.patchAge(), 0.0F, ageInTicks, limbSwing, limbSwingAmount, netHeadYaw,
				headPitch);
	}

	/** The playing track's deltas at its time, over the stance and the stalk. */
	static void track(float[] a, HemorathAnimState motion) {
		int track = motion.track();
		float t = motion.time();
		if (track == HemorathAnimState.TRACK_ENDING) ending(a, t);
		else if (track == HemorathAnimState.TRACK_CARD) vesselEmpties(a, t);
		else if (track == HemorathAnimState.TRACK_THIRST) lastThirst(a, t);
		else if (track > HemorathAnimState.TRACK_OPENING) opening(a, track - HemorathAnimState.TRACK_OPENING, t);
		else if (track > HemorathAnimState.TRACK_MOVE) move(a, HemorathMove.byId(track - HemorathAnimState.TRACK_MOVE), t,
				motion.variant(), motion.debt());
	}

	// ---------------------------------------------------------------- the stance

	/**
	 * His body at rest by the meter, as continuous weights so the sag and the swell never step: hollow, a skulking
	 * hunch with the claw held up and creeping, quick shallow breaths and sharp little turns of the skull; filling,
	 * deeper breaths, the ribs working, the staff hand tightening; full, upright on bowed legs, the ribs held wide,
	 * laboured breaths, the hips rolling his weight over the staff and a shudder through him. {@code breath} and
	 * {@code sway} are carried phases (radians); {@code fidget} fades the idle business under a move.
	 */
	static void stance(float[] a, float f, float age, float breath, float sway, float fidget) {
		float sunk = 1.0F - HemoEasing.smoothstep(0.15F, 0.5F, f);
		float full = HemoEasing.smoothstep(0.55F, 0.9F, f);
		float mid = Math.max(0.0F, 1.0F - sunk - full);
		float inhale = Mth.sin(breath);
		float depth = 0.015F * sunk + 0.03F * mid + 0.05F * full;
		a[STAFF_PLANT] = 1.0F;
		a[BOW] += 0.16F * sunk + 0.06F * mid - 0.06F * full + inhale * depth;
		a[HEAD] += -0.12F * sunk + 0.02F * mid + 0.14F * full - inhale * depth * 0.6F;
		a[CROUCH] += 0.28F * sunk + 0.1F * mid + 0.32F * full;
		a[CLAVICLE] += 0.12F * sunk + 0.04F * mid + 0.22F * full + inhale * depth * 0.8F;
		a[RIBS] += -0.04F * sunk + 0.1F * full + inhale * (0.01F * sunk + 0.05F * mid + 0.04F * full);
		a[R_ARM_X] += -0.38F * sunk - 0.12F * mid + 0.12F * full;
		a[R_FORE] += -0.4F * sunk - 0.1F * mid + 0.1F * full;
		a[R_HAND] += -0.2F * sunk;
		a[FINGERS] += -0.15F * sunk + 0.05F * mid + 0.22F * full;
		a[STAFF_FOOT_Z] += -1.5F * sunk - 0.5F * mid;
		a[STAFF_FOOT_X] += 1.0F * full;
		// full: his weight rolls side to side over the staff
		a[HIP_SWAY] += Mth.sin(sway) * 0.05F * full;
		a[LEAN] += 0.05F * full - Mth.sin(sway + 0.9F) * 0.035F * full;
		a[HEAD_ROLL] += Mth.sin(sway + 1.7F) * 0.05F * full;
		// the idle business, fading under a move
		a[CLAW_FLEX] += (0.3F * sunk + 0.1F * mid) * fidget
				+ HemoEasing.window(age % 40.0F, 0.0F, 1.5F, 3.0F, 7.0F) * 0.45F * sunk * fidget;
		a[HEAD_TURN] += tics(age / 26.0F, 0.0F) * 0.2F * sunk * fidget;
		a[HEAD] += tics(age / 31.0F, 7.0F) * 0.07F * sunk * fidget;
		a[L_FORE] -= HemoEasing.window(age % 64.0F, 0.0F, 3.0F, 10.0F, 16.0F) * 0.14F * mid * fidget;
		a[L_HAND] -= HemoEasing.window(age % 64.0F, 0.0F, 3.0F, 10.0F, 16.0F) * 0.1F * mid * fidget;
		a[SHAKE] += HemoEasing.window(age % 26.0F, 0.0F, 1.0F, 3.0F, 6.0F) * 0.55F * full * fidget;
	}

	/** Sharp little turns between held looks: a value in [-1, 1] that snaps to a new hold once per unit of {@code t}. */
	static float tics(float t, float seed) {
		float k = (float) Math.floor(t);
		float u = HemoEasing.smoothstep(Math.min(1.0F, (t - k) * 6.0F));
		return HemoEasing.lerp(noise(k - 1.0F, seed), noise(k, seed), u);
	}

	private static float noise(float k, float seed) {
		float h = Mth.sin(k * 12.9898F + seed * 78.233F) * 43758.547F;
		return (h - (float) Math.floor(h)) * 2.0F - 1.0F;
	}

	// ---------------------------------------------------------------- the stalk, the lurch and the turn

	/**
	 * The three-beat stalk (HemorathGait: the left foot, the right foot, then the staff swings ahead and plants), the
	 * chest pitching on each landing and the claw dragging behind; full, his hips roll over each step. Over it the
	 * Sunken lurch (torso thrown forward, claw reaching, legs trailing) and the turn (shoulders and skull lead, the hips
	 * lag, the feet shuffle round when he turns in place), both read off his real motion.
	 */
	static void stalk(float[] a, float f, float stride, float walking, float lurch, float turn, float turnTravel,
			float busy) {
		float w = HemoEasing.clamp01(walking * 1.4F) * (1.0F - lurch * 0.6F);
		float upper = w * (1.0F - busy * 0.75F);
		float full = HemoEasing.smoothstep(0.55F, 0.9F, f);
		float p = HemorathGait.phase(stride, f);
		float amplitude = HemorathGait.amplitude(f);
		a[STEP_L] += HemorathGait.legSwing(p, HemorathGait.LEFT, amplitude) * w;
		a[STEP_R] += HemorathGait.legSwing(p, HemorathGait.RIGHT, amplitude) * w;
		a[LIFT_L] += HemorathGait.lift(p, HemorathGait.LEFT) * w;
		a[LIFT_R] += HemorathGait.lift(p, HemorathGait.RIGHT) * w;
		a[STAFF_FOOT_Z] += HemorathGait.staffFoot(p, f) * upper;
		a[STAFF_FOOT_Y] -= HemorathGait.lift(p, HemorathGait.STAFF) * HemorathGait.STAFF_LIFT * upper;
		float feet = HemorathGait.landing(p, HemorathGait.RIGHT) + HemorathGait.landing(p, HemorathGait.STAFF);
		float staffDown = HemorathGait.landing(p, HemorathGait.LEFT);
		a[BOW] += (0.1F + feet * HemoEasing.lerp(0.09F, 0.06F, f)) * w;
		a[HEAD] -= (0.07F + feet * 0.04F) * w;
		a[CROUCH] += feet * HemoEasing.lerp(0.12F, 0.22F, f) * w;
		a[L_ARM_X] += staffDown * 0.1F * upper;
		a[LEAN] += staffDown * 0.04F * upper;
		a[R_ARM_X] += 0.32F * upper;
		a[R_FORE] += 0.2F * upper;
		a[FINGERS] -= 0.2F * upper;
		float rock = Mth.sin((p - 1.0F / 6.0F) * Mth.TWO_PI);
		a[HIP_SWAY] += rock * 0.07F * full * w;
		a[LEAN] -= rock * 0.05F * full * w;
		a[TWIST] += Mth.sin(p * Mth.TWO_PI) * HemoEasing.lerp(0.08F, 0.05F, f) * upper;
		// the lurch
		a[BOW] += 0.5F * lurch;
		a[HEAD] -= 0.45F * lurch;
		a[CROUCH] += 0.35F * lurch;
		a[R_ARM_X] -= 1.45F * lurch;
		a[R_ARM_Z] += 0.15F * lurch;
		a[R_FORE] += 0.15F * lurch;
		a[FINGERS] -= 0.75F * lurch;
		a[CLAW_FLEX] += 0.4F * lurch;
		a[STEP_L] += 0.45F * lurch;
		a[STEP_R] += 0.25F * lurch;
		a[L_ARM_X] -= 0.3F * lurch;
		a[STAFF_FOOT_Z] += 3.0F * lurch;
		// the turn: the shoulders and the skull lead into it, the hips lag, the staff swings out
		a[TWIST] += 0.28F * turn;
		a[HEAD_TURN] += 0.3F * turn;
		a[HIP_TWIST] -= 0.08F * turn;
		a[LEAN] -= 0.06F * turn;
		a[STAFF_FOOT_X] += 2.0F * turn;
		float still = 1.0F - HemoEasing.clamp01(walking * 2.0F);
		float shuffle = Mth.sin(turnTravel * Mth.PI / 22.5F) * Math.min(1.0F, Math.abs(turn) * 1.5F) * still;
		a[LIFT_L] += Math.max(0.0F, shuffle) * 0.8F;
		a[LIFT_R] += Math.max(0.0F, -shuffle) * 0.8F;
		a[STEP_L] -= Math.max(0.0F, shuffle) * 0.12F;
		a[STEP_R] -= Math.max(0.0F, -shuffle) * 0.12F;
	}

	// ---------------------------------------------------------------- the moves

	/** For tests and the review: a move with a middling Tithe debt. */
	static void move(float[] a, HemorathMove move, float t, int variant) {
		move(a, move, t, variant, 400);
	}

	/**
	 * Every move as deltas on its authored clock: each starts and ends at zero, so it rises out of the stance and
	 * settles back into it. The commit (the end of the authored wind-up) is where the strike's arc runs fastest.
	 */
	static void move(float[] a, HemorathMove move, float t, int variant, int debt) {
		if (move == null) return;
		switch (move) {
			case RAKING_CLAW -> rakingClaw(a, t, HemorathMove.held(variant));
			case BACKHAND -> backhand(a, t);
			case STAFF_HEEL -> staffHeel(a, t);
			case STAFF_FALL -> staffFall(a, t);
			case IRON_RAKE -> ironRake(a, t);
			case EMPTY_PULSE -> emptyPulse(a, t);
			case MARROW_RUPTURE -> marrowRupture(a, t);
			case MARROW_SIPHON -> marrowSiphon(a, t);
			case CHAINED_DEBT -> chainedDebt(a, t);
			case TITHE_RECKONING -> titheReckoning(a, t, debt);
			case WELLS_ANSWER -> wellsAnswer(a, t);
			case SPIKED_HALO -> spikedHalo(a, t);
			case CROWN_FALL -> crownFall(a, t);
			case IRON_THICKET -> ironThicket(a, t);
			case EXSANGUINATING_TIDE -> exsanguinatingTide(a, t);
			case RIB_BURST -> ribBurst(a, t);
			case GORGE -> gorge(a, t);
			case LAST_THIRST -> lastThirst(a, t);
		}
	}

	/**
	 * 14 / 5 / 16. The right shoulder rises and draws back while the fingers splay and hook; he coils on bent knees
	 * (the Beholding feint freezes the clock at 10, deep in the coil, and he trembles there). At 14 the claw tears down
	 * and across his body in three ticks, the right foot stamping in, the arm whipping on past the line, then he hangs
	 * off balance and hauls himself upright.
	 */
	static void rakingClaw(float[] a, float t, boolean held) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 5, -1.1F, 10, -2.0F, 13, -2.15F, 16.5F, 0.55F, 18.5F, 0.85F, 22, 0.45F,
				27, 0.08F, 31, -0.04F, 35, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 8, 0.45F, 13, 0.75F, 16.5F, -0.75F, 19, -0.95F, 24, -0.35F, 30, 0.02F, 35, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 8, 0.35F, 13, 0.45F, 16.5F, -0.1F, 20, -0.2F, 28, 0, 35, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 6, -0.6F, 13, -1.0F, 15, -0.15F, 17, 0.2F, 21, 0.05F, 35, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 10, -0.45F, 13, -0.5F, 15.5F, 0.35F, 19, 0.2F, 28, 0, 35, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 5, -0.8F, 11, -0.55F, 13, -0.7F, 15.5F, 0.95F, 19, 0.6F, 26, 0.1F, 35, 0);
		a[CLAW_FLEX] += HemoEasing.window(t, 1, 4, 9, 13) * 0.5F;
		a[TWIST] += catmullRom(t, 0, 0, 7, 0.25F, 13, 0.42F, 16.5F, -0.45F, 19, -0.55F, 25, -0.12F, 31, 0.03F, 35, 0);
		a[BOW] += catmullRom(t, 0, 0, 8, -0.12F, 13, -0.16F, 16.5F, 0.38F, 19, 0.42F, 26, 0.1F, 35, 0);
		a[LEAN] += catmullRom(t, 0, 0, 9, -0.08F, 13, -0.1F, 17, 0.1F, 21, 0.12F, 28, 0, 35, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 9, 0.25F, 13, 0.3F, 16.5F, 0.6F, 20, 0.55F, 28, 0.1F, 35, 0);
		a[HEAD] += catmullRom(t, 0, 0, 9, -0.18F, 13, -0.2F, 17, 0.1F, 22, 0.05F, 35, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 10, 0.12F, 14, 0.1F, 17, -0.2F, 24, -0.05F, 35, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 13, 0.1F, 16, -0.38F, 22, -0.3F, 30, -0.05F, 35, 0);
		a[LIFT_R] += HemoEasing.window(t, 13, 14.5F, 15, 16.5F) * 0.5F;
		a[STEP_L] += catmullRom(t, 0, 0, 13, 0, 16, 0.15F, 22, 0.12F, 35, 0);
		a[HIP_TWIST] += catmullRom(t, 0, 0, 13, 0.12F, 16.5F, -0.15F, 22, -0.12F, 35, 0);
		if (held) a[SHAKE] += HemoEasing.window(t, 9.5F, 10, 10, 10.5F) * 0.55F;
	}

	/**
	 * 12 / 4 / 18. The claw folds back across his chest high to his left as the shoulders wind the other way, holds,
	 * then the back of the hand flings out to his right in three ticks, fingers flung open after it; the swing pulls
	 * him off balance and the left foot stumbles out to catch him.
	 */
	static void backhand(float[] a, float t) {
		a[R_ARM_Y] += catmullRom(t, 0, 0, 6, -0.85F, 11, -1.15F, 12, -1.18F, 14.5F, 0.85F, 17, 1.2F, 22, 0.6F, 28, 0.1F,
				34, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 6, -0.9F, 11, -1.25F, 14.5F, -1.15F, 17, -0.85F, 23, -0.3F, 34, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 11, -0.2F, 14.5F, 0.45F, 17, 0.6F, 24, 0.2F, 34, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 6, -1.1F, 11, -1.4F, 14, -0.1F, 16, 0.15F, 22, -0.05F, 34, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 11, 0.4F, 14, -0.35F, 18, -0.2F, 28, 0, 34, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, 0.5F, 11, 0.6F, 14, -0.5F, 18, -0.3F, 26, 0, 34, 0);
		a[TWIST] += catmullRom(t, 0, 0, 7, -0.42F, 11.5F, -0.55F, 14.5F, 0.48F, 17, 0.62F, 23, 0.25F, 34, 0);
		a[BOW] += catmullRom(t, 0, 0, 11, 0.08F, 14.5F, 0.2F, 18, 0.12F, 34, 0);
		a[LEAN] += catmullRom(t, 0, 0, 11, 0.1F, 15, -0.12F, 19, -0.18F, 26, -0.05F, 34, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.2F, 14, 0.35F, 19, 0.45F, 27, 0.15F, 34, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 15, 0, 18, -0.3F, 21, -0.32F, 26, -0.08F, 34, 0);
		a[LIFT_L] += HemoEasing.window(t, 15, 17, 18, 20) * 0.6F;
		a[HEAD_TURN] += catmullRom(t, 0, 0, 9, -0.15F, 14, 0.18F, 20, 0.12F, 34, 0);
		a[HEAD] += catmullRom(t, 0, 0, 12, -0.08F, 16, 0.12F, 34, 0);
		a[HIP_TWIST] += catmullRom(t, 0, 0, 11, -0.1F, 15, 0.14F, 22, 0.06F, 34, 0);
	}

	/**
	 * 12 / 4 / 16. He leans forward onto the braced claw and lifts the staff, drawing its iron foot up and back past
	 * his hip with a look over his shoulder; at 12 the arm drives it back like a ram, past the line, and the staff
	 * swings down and plants again as he straightens.
	 */
	static void staffHeel(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 4, 18, 25);
		a[STAFF_X] += catmullRom(t, 0, 0, 4, -0.15F, 10, 0.4F, 12, 0.42F, 13.5F, 1.15F, 15.5F, 1.25F, 19, 0.8F, 25, 0.1F,
				32, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 4, -0.35F, 10, -0.55F, 12, -0.58F, 14, 0.55F, 16, 0.68F, 21, 0.3F, 27, 0.05F,
				32, 0);
		a[L_FORE] += catmullRom(t, 0, 0, 10, -0.5F, 13, 0.1F, 18, -0.1F, 32, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, 0.3F, 11, 0.42F, 14, 0.5F, 17, 0.4F, 26, 0.08F, 32, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 6, -0.55F, 11, -0.7F, 15, -0.75F, 24, -0.2F, 32, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 8, 0.25F, 24, 0.05F, 32, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, -0.45F, 13, 0.35F, 24, 0, 32, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 8, 0.45F, 12, 0.5F, 14, 0.7F, 18, 0.55F, 28, 0.05F, 32, 0);
		a[TWIST] += catmullRom(t, 0, 0, 9, 0.15F, 12, 0.18F, 14, -0.3F, 17, -0.35F, 26, -0.05F, 32, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 8, -0.15F, 13, -0.38F, 20, -0.3F, 30, 0, 32, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 11, 0, 14, 0.25F, 20, 0.2F, 30, 0, 32, 0);
	}

	/**
	 * 22 / 6 / 24. Both hands take the staff up overhead (the crown chains swing out), it tips back behind him as he
	 * rears; at 22 he hauls it over and down in two ticks so the crown strikes the floor ahead, the left foot stepping
	 * into the blow, his whole body dropping after it with a judder. Then he levers it upright and plants it ahead to
	 * lean on (Planted takes the recovery).
	 */
	static void staffFall(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 2, 6, 30, 38);
		a[L_ARM_X] += catmullRom(t, 0, 0, 8, -1.6F, 16, -2.55F, 20, -2.7F, 22, -2.65F, 24.5F, -0.75F, 27, -0.6F,
				34, -0.55F, 44, -0.3F, 52, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 10, 0.25F, 20, 0.3F, 24, 0.1F, 52, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -1.4F, 16, -2.3F, 20, -2.45F, 22, -2.4F, 24.5F, -0.85F, 27, -0.7F,
				34, -0.6F, 44, -0.3F, 52, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 10, -0.45F, 22, -0.5F, 25, -0.4F, 40, -0.3F, 52, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 10, -0.5F, 22, -0.4F, 25, -0.1F, 52, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, 0.6F, 46, 0.6F, 52, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 10, -0.25F, 18, -0.6F, 21, -0.72F, 22, -0.7F, 24, 1.35F, 25.5F, 1.5F,
				28, 1.42F, 34, 1.0F, 40, 0.2F, 52, 0);
		a[STAFF_FOOT_Z] += catmullRom(t, 28, 0, 38, -4.5F, 46, -3.5F, 52, 0);
		a[BOW] += catmullRom(t, 0, 0, 12, -0.2F, 19, -0.38F, 22, -0.4F, 24, 0.55F, 26, 0.72F, 30, 0.55F, 40, 0.35F,
				52, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 14, -0.1F, 21, -0.15F, 24, 1.1F, 26, 1.3F, 30, 1.0F, 40, 0.6F, 52, 0);
		a[HEAD] += catmullRom(t, 0, 0, 16, -0.3F, 22, -0.32F, 24, 0.35F, 27, 0.42F, 36, 0.2F, 52, 0);
		a[RIBS] += catmullRom(t, 0, 0, 18, 0.15F, 22, 0.18F, 24, -0.08F, 30, 0.05F, 52, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 20, 0, 23, -0.3F, 30, -0.28F, 44, -0.1F, 52, 0);
		a[LIFT_L] += HemoEasing.window(t, 20.5F, 22, 22.5F, 24) * 0.7F;
		a[SHAKE] += HemoEasing.window(t, 24, 24.5F, 26, 32) * 0.6F;
	}

	/**
	 * 28 / 12 / 20. He drops into a deep crouch with the claw slammed to the floor out to his right and drags it
	 * across in front of him, grinding, the staff braced wide; at the end of the drag the fingers dig in, and at 28 he
	 * rips the claw up and over as the spikes run, rearing, conducting the eruption with small beats, then rises.
	 */
	static void ironRake(float[] a, float t) {
		a[CROUCH] += catmullRom(t, 0, 0, 6, 1.5F, 9, 1.65F, 24, 1.55F, 27, 1.65F, 30, 0.55F, 34, 0.5F, 40, 0.55F,
				48, 0.2F, 60, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, 0.55F, 9, 0.62F, 24, 0.55F, 27, 0.62F, 30, -0.12F, 33, -0.18F, 40, -0.05F,
				50, 0.05F, 60, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 4, -1.0F, 7, -0.55F, 9, -0.45F, 24, -0.35F, 27, 0.15F, 29.5F, -1.75F,
				32, -2.05F, 36, -1.85F, 40, -1.7F, 48, -0.6F, 54, -0.05F, 60, 0)
				+ Mth.sin((t - 30) * 2.1F) * 0.07F * HemoEasing.window(t, 30, 31, 38, 40);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 5, 0.85F, 9, 0.95F, 24, -0.25F, 27, -0.3F, 30, 0.05F, 40, 0.1F, 60, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 9, 0.5F, 24, -0.75F, 27, -0.7F, 30, -0.1F, 60, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 7, -0.2F, 24, -0.25F, 27, -0.45F, 30, 0.05F, 40, -0.1F, 60, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 7, 0.4F, 24, 0.35F, 27, 0.5F, 30, -0.3F, 40, -0.15F, 60, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 5, -0.6F, 8, 0.45F, 24, 0.5F, 27, 0.75F, 29.5F, -0.75F, 33, -0.6F, 40, -0.4F,
				50, 0, 60, 0);
		a[CLAW_FLEX] += HemoEasing.window(t, 9, 11, 22, 25) * 0.3F;
		a[SHAKE] += HemoEasing.window(t, 9, 11, 22, 25) * 0.45F + HemoEasing.window(t, 30, 31, 38, 42) * 0.25F;
		a[TWIST] += catmullRom(t, 0, 0, 9, 0.32F, 24, -0.3F, 27, -0.32F, 30, 0.12F, 40, 0.08F, 60, 0);
		a[LEAN] += catmullRom(t, 0, 0, 9, -0.12F, 24, 0.1F, 27, 0.12F, 30, 0, 60, 0);
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.3F, 24, 0.28F, 27, 0.25F, 30, -0.25F, 36, -0.3F, 44, -0.1F, 60, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 5, -0.35F, 26, -0.3F, 30, -0.1F, 50, 0, 60, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 5, 0.3F, 26, 0.3F, 30, 0.1F, 50, 0, 60, 0);
		a[STAFF_FOOT_X] += catmullRom(t, 0, 0, 6, 1.5F, 26, 1.5F, 34, 0, 60, 0);
		a[STAFF_FOOT_Z] += catmullRom(t, 0, 0, 6, -1.5F, 26, -1.5F, 34, 0, 60, 0);
	}

	/**
	 * 24 / 4 / 18. The staff is lifted and rammed down; he folds over it, claw pressed to his breastbone, the ribs
	 * crushing inward and a shudder building; at 24 everything flings open (ribs, arms, the head thrown back), past
	 * open, and he gasps as it rings out.
	 */
	static void emptyPulse(float[] a, float t) {
		a[STAFF_FOOT_Z] += catmullRom(t, 0, 0, 3, -2.0F, 40, -2.0F, 46, 0);
		a[STAFF_FOOT_Y] += catmullRom(t, 0, 0, 2, -1.5F, 3.5F, 0.6F, 5, 0.3F, 40, 0.3F, 46, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 2, -0.3F, 3.5F, 0.18F, 6, 0.12F, 40, 0.1F, 46, 0);
		a[RIBS] += catmullRom(t, 0, 0, 8, -0.15F, 18, -0.28F, 23, -0.32F, 24.5F, 0.5F, 26, 0.66F, 27.5F, 0.7F, 29, 0.5F,
				31, 0.55F, 35, 0.3F, 46, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, 0.3F, 20, 0.45F, 23.5F, 0.5F, 25, -0.32F, 26.5F, -0.4F, 30, -0.2F, 38, 0.05F,
				46, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.35F, 22, 0.55F, 24, 0.6F, 25.5F, 0.15F, 28, 0.25F, 46, 0);
		a[CLAVICLE] += catmullRom(t, 0, 0, 18, -0.15F, 23, -0.18F, 25, 0.35F, 28, 0.25F, 46, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 18, -0.15F, 23.5F, -0.18F, 25.5F, 0.8F, 27, 0.9F, 32, 0.4F, 40, 0.1F, 46, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.7F, 22, -0.95F, 24, -1.0F, 25.5F, 0.1F, 28, 0.25F, 36, 0.05F, 46, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 8, -0.55F, 23, -0.65F, 25.5F, 0.1F, 46, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 8, -1.1F, 23, -1.35F, 25.5F, -0.1F, 30, 0.1F, 46, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 10, 0.6F, 23, 0.8F, 25.5F, -0.8F, 30, -0.5F, 40, 0, 46, 0);
		a[HEAD] += catmullRom(t, 0, 0, 12, 0.3F, 23, 0.4F, 25, -0.4F, 27, -0.48F, 32, -0.2F, 46, 0);
		a[SHAKE] += HemoEasing.window(t, 8, 20, 23, 24.5F) * 0.55F;
	}

	/**
	 * 20 / 24 / 24. Out of the pulse's fold: he stays bowed with a fist clenched and lifts his skull; then the ribs
	 * pump twice, each a heave of the shoulders and a bob of the knees, and he comes up out of it.
	 */
	static void marrowRupture(float[] a, float t) {
		a[BOW] += catmullRom(t, 0, 0.3F, 14, 0.32F, 20, 0.28F, 24, 0.05F, 29, 0.32F, 34, 0, 39, 0.3F, 44, 0.15F,
				56, 0.05F, 68, 0);
		a[RIBS] += catmullRom(t, 0, -0.1F, 16, -0.15F, 20, -0.18F, 23, 0.48F, 26, 0.2F, 29, -0.12F, 33, 0.5F, 36, 0.2F,
				40, -0.05F, 44, 0.05F, 56, 0, 68, 0);
		a[HEAD] += catmullRom(t, 0, 0.35F, 12, 0, 20, -0.15F, 23, -0.35F, 28, 0.1F, 33, -0.38F, 38, 0.1F, 46, 0, 68, 0);
		a[CROUCH] += catmullRom(t, 0, 0.4F, 18, 0.45F, 21, 0.2F, 24, 0.55F, 30, 0.25F, 34, 0.6F, 40, 0.3F, 52, 0.1F,
				68, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 20, 0, 23, 0.45F, 27, 0.15F, 33, 0.5F, 37, 0.15F, 46, 0, 68, 0);
		a[R_ARM_X] += catmullRom(t, 0, -0.5F, 18, -0.6F, 23, 0.1F, 29, -0.3F, 33, 0.15F, 40, -0.1F, 56, 0, 68, 0);
		a[FINGERS] += catmullRom(t, 0, 0.6F, 18, 0.9F, 22, -0.4F, 28, 0.5F, 33, -0.5F, 40, 0.2F, 56, 0, 68, 0);
		a[CLAVICLE] += catmullRom(t, 0, -0.1F, 20, -0.1F, 23, 0.25F, 28, 0.05F, 33, 0.3F, 40, 0.05F, 56, 0, 68, 0);
		a[SHAKE] += HemoEasing.window(t, 0, 4, 16, 20) * 0.35F + HemoEasing.window(t, 21, 22, 40, 44) * 0.4F;
	}

	/**
	 * 24 / 40 / 20. He lifts the staff and rams it down, then tips his skull back and opens his ribs, the claw raised
	 * and open; through the draw his chest heaves as it fills, he swallows every ten ticks and the claw beckons. Then
	 * the head comes forward and he hunches over what he took (Gorged may take the recovery).
	 */
	static void marrowSiphon(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 3, 6, 8);
		a[L_ARM_X] += catmullRom(t, 0, 0, 4, -0.9F, 7, 0.35F, 9, 0.25F, 64, 0.2F, 76, 0.05F, 84, 0);
		a[STAFF_FOOT_Y] += catmullRom(t, 0, 0, 4, -2.5F, 7, 0.8F, 9, 0.5F, 70, 0.5F, 84, 0);
		a[STAFF_FOOT_Z] += catmullRom(t, 0, 0, 7, -2.0F, 74, -2.0F, 84, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 6, 0.1F, 8, 0.55F, 12, 0.35F, 24, 0.3F, 64, 0.35F, 74, 0.15F, 84, 0);
		a[BOW] += catmullRom(t, 0, 0, 8, 0.3F, 12, 0.2F, 22, -0.28F, 25, -0.32F, 64, -0.3F, 70, 0.15F, 76, 0.18F, 84, 0);
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.25F, 12, 0.15F, 22, -0.6F, 25, -0.65F, 64, -0.6F, 70, 0.2F, 76, 0.22F, 84, 0);
		a[RIBS] += catmullRom(t, 0, 0, 12, 0, 22, 0.3F, 25, 0.36F, 64, 0.4F, 72, 0.05F, 84, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 12, 0, 22, 0.35F, 64, 0.42F, 72, 0.05F, 84, 0);
		a[CLAVICLE] += catmullRom(t, 0, 0, 12, 0, 24, 0.22F, 64, 0.25F, 72, 0, 84, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 12, -0.2F, 22, -0.85F, 64, -0.8F, 70, -0.1F, 84, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 22, 0.35F, 64, 0.4F, 72, 0, 84, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.6F, 64, -0.5F, 70, 0.2F, 84, 0);
		float draw = HemoEasing.window(t, 22, 26, 62, 66);
		float heave = Mth.sin(t * 0.55F);
		a[RIBS] += heave * 0.07F * draw;
		a[BOW] -= heave * 0.04F * draw;
		a[HEAD] += HemoEasing.pulse((t - 24) % 10.0F, 1.5F, 6) * 0.12F * draw;
		a[CLAW_FLEX] += 0.55F * draw;
		a[SHAKE] += 0.25F * draw;
	}

	/**
	 * 20 / 8 / 14. The claw draws back and up, elbow cocked, the chain uncoiling through the fingers; a held beat; at
	 * 20 the arm throws out straight with a lunge, fingers flung open, then hauls back hard on the reel (the elbow
	 * folding, his weight thrown back onto the left foot) and recovers.
	 */
	static void chainedDebt(float[] a, float t) {
		a[CHAIN] = HemoEasing.window(t, 0, 2, 30, 34);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, 0.35F, 15, 0.55F, 19, 0.6F, 21.5F, -1.65F, 23, -1.8F, 25, -1.2F,
				27.5F, 0.35F, 31, 0.3F, 37, 0.05F, 42, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 10, 0.4F, 19, 0.45F, 21.5F, 0.05F, 28, 0.15F, 42, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 10, -1.1F, 19, -1.3F, 21.5F, -0.05F, 24, 0, 27.5F, -1.25F, 32, -0.8F, 42, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 12, -0.4F, 19, -0.45F, 21.5F, 0.3F, 27.5F, -0.3F, 42, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 12, 0.4F, 19, 0.5F, 21, -0.85F, 24, -0.6F, 26, 0.9F, 32, 0.6F, 42, 0);
		a[CLAW_FLEX] += HemoEasing.window(t, 2, 6, 14, 18) * 0.45F;
		a[TWIST] += catmullRom(t, 0, 0, 12, 0.38F, 19, 0.48F, 21.5F, -0.35F, 24, -0.4F, 27.5F, 0.35F, 32, 0.2F, 42, 0);
		a[BOW] += catmullRom(t, 0, 0, 12, -0.08F, 19, -0.1F, 21.5F, 0.32F, 24, 0.35F, 27.5F, -0.2F, 32, -0.1F, 42, 0);
		a[HEAD] += catmullRom(t, 0, 0, 15, -0.12F, 21, -0.2F, 25, 0, 28, 0.1F, 42, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 15, 0.2F, 19, 0.25F, 22, 0.5F, 26, 0.45F, 28, 0.3F, 42, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 19, 0, 22, -0.35F, 25, -0.32F, 28, 0.1F, 34, 0, 42, 0);
		a[LIFT_R] += HemoEasing.window(t, 19, 20.5F, 21, 22.5F) * 0.5F;
		a[STEP_L] += catmullRom(t, 0, 0, 22, 0, 28, 0.2F, 36, 0, 42, 0);
		a[HIP_TWIST] += catmullRom(t, 0, 0, 19, 0.12F, 22, -0.15F, 28, 0.1F, 42, 0);
	}

	/**
	 * 36 / 2 / 22. The claw goes up high and stays there, tracking, trembling harder as he waits, and the fingers close
	 * one at a time, once per hundred of the debt; it draws back a hair at 34, then at 36 the fist yanks down and his
	 * whole body follows it, past the line, and settles.
	 */
	static void titheReckoning(float[] a, float t, int debt) {
		int clenches = Math.max(1, Math.min(8, debt / 100));
		float steps = HemoEasing.ramp(t, 4, 34) * clenches;
		float done = (float) Math.floor(steps);
		float stepped = Math.min(clenches, done + HemoEasing.smoothstep((steps - done - 0.6F) / 0.4F));
		a[CLENCH] += 4.0F * stepped / clenches * (1.0F - HemoEasing.ramp(t, 44, 56));
		a[CHAIN] = HemoEasing.window(t, 0, 2, 38, 44);
		a[R_ARM_X] += catmullRom(t, 0, 0, 6, -2.2F, 10, -2.65F, 32, -2.7F, 35, -2.95F, 36, -2.98F, 38.5F, -0.15F,
				40, 0.25F, 44, 0.1F, 52, -0.05F, 60, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 10, 0.3F, 35, 0.35F, 38.5F, 0.05F, 60, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 8, -0.3F, 35, -0.25F, 37, -0.6F, 38.5F, 0, 42, 0.15F, 60, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 6, -0.6F, 34, -0.2F, 36, 0.2F, 38, 0.9F, 46, 0.6F, 60, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.22F, 33, -0.25F, 36, -0.32F, 38.5F, 0.5F, 40.5F, 0.6F, 46, 0.3F, 60, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.3F, 34, -0.32F, 38.5F, 0.28F, 42, 0.32F, 60, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, -0.1F, 34, -0.12F, 38.5F, 0.5F, 41, 0.6F, 50, 0.2F, 60, 0);
		a[TWIST] += catmullRom(t, 0, 0, 10, 0.12F, 36, 0.15F, 39, -0.15F, 46, -0.05F, 60, 0);
		a[SHAKE] += HemoEasing.ramp(t, 10, 34) * 0.45F * (1.0F - HemoEasing.ramp(t, 36, 38));
	}

	/**
	 * 30 / 40 / 30. He lifts the staff and drives it into the pit with his whole weight; rises and opens his arms
	 * toward the wells; through the draw his skull goes back and his chest swells with each stream's arrival, the arms
	 * drawing slowly in as he pulls; then he lowers and hunches (Gorged may take the recovery).
	 */
	static void wellsAnswer(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 2, 7, 9);
		a[L_ARM_X] += catmullRom(t, 0, 0, 5, -1.3F, 8, 0.4F, 10, 0.3F, 12, 0.25F, 90, 0.15F, 100, 0);
		a[STAFF_FOOT_Y] += catmullRom(t, 0, 0, 5, -3.5F, 8, 1.4F, 10, 1.2F, 92, 1.2F, 100, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 5, -0.12F, 8, 0.7F, 11, 0.55F, 14, 0.4F, 28, 0.15F, 70, 0.15F, 84, 0.25F,
				100, 0);
		a[BOW] += catmullRom(t, 0, 0, 5, -0.2F, 8, 0.45F, 11, 0.35F, 16, 0.1F, 28, -0.18F, 34, -0.3F, 68, -0.32F,
				78, 0.15F, 86, 0.18F, 100, 0);
		a[HEAD] += catmullRom(t, 0, 0, 5, -0.2F, 8, 0.3F, 12, 0.2F, 28, -0.15F, 36, -0.55F, 68, -0.58F, 78, 0.2F,
				88, 0.15F, 100, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 12, 0, 22, 0.75F, 28, 0.9F, 68, 0.8F, 78, 0.15F, 100, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 12, 0, 26, -0.35F, 70, -0.3F, 80, 0, 100, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 14, 0, 26, 0.3F, 70, 0.25F, 80, 0, 100, 0);
		a[RIBS] += catmullRom(t, 0, 0, 28, 0.1F, 36, 0.32F, 68, 0.38F, 76, 0.05F, 100, 0);
		a[CLAVICLE] += catmullRom(t, 0, 0, 26, 0, 36, 0.25F, 68, 0.28F, 78, 0, 100, 0);
		float draw = HemoEasing.window(t, 30, 34, 66, 70);
		float stream = HemoEasing.pulse((t - 30) % 10.0F, 2, 9);
		a[RIBS] += stream * 0.12F * draw;
		a[BOW] -= stream * 0.05F * draw;
		a[SPREAD] -= HemoEasing.ramp(t, 34, 66) * 0.2F * draw;
		a[CLAW_FLEX] += 0.5F * draw;
		a[SHAKE] += 0.2F * draw;
	}

	/**
	 * 26 / 34 / 22. The claw reaches up behind his skull, lifts the halo off (from 12) and draws it back to his right
	 * with the shoulders coiled; at 26 it slings it out across his body and lets go. Bare-headed, he follows it out and
	 * round its return arc to his staff side, raises the open claw and catches it at 60 with a jolt through his knees,
	 * lifts it overhead and settles it back onto his skull.
	 */
	static void spikedHalo(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 6, -1.9F, 12, -2.75F, 15, -2.8F, 19, -2.25F, 23, -2.0F, 24.5F, -1.3F,
				27, -1.05F, 31, -0.75F, 40, -0.3F, 50, -0.45F, 56, -2.1F, 59, -2.55F, 60.5F, -2.15F, 63, -2.4F,
				68, -2.7F, 73, -1.2F, 78, -0.2F, 82, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 12, 0.05F, 16, 0.35F, 23.5F, 0.92F, 26, -0.75F, 28, -0.98F, 32, -0.85F,
				40, -0.3F, 50, 0, 82, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 12, 0.2F, 23, 0.55F, 26, 0.2F, 31, -0.1F, 40, 0, 56, 0.35F, 60, 0.4F, 70, 0.2F,
				82, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 10, -0.7F, 16, -0.5F, 23, -1.1F, 25.5F, -0.05F, 28, 0.15F, 34, 0, 56, -0.3F,
				60, -0.6F, 66, -0.9F, 72, -0.4F, 82, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 10, -0.5F, 13, 0.55F, 23, 0.6F, 25.5F, -0.8F, 30, -0.5F, 40, 0, 54, -0.7F,
				59.5F, -0.75F, 61, 0.7F, 72, 0.4F, 82, 0);
		a[HALO_HELD] = HemoEasing.window(t, 12, 16, 25.4F, 25.6F) + HemoEasing.window(t, 59.8F, 60.4F, 70, 74);
		a[HALO_AWAY] = HemoEasing.window(t, 25.5F, 26, 26 + HemorathHaloPath.FLIGHT_TICKS,
				26.5F + HemorathHaloPath.FLIGHT_TICKS);
		a[TWIST] += catmullRom(t, 0, 0, 16, 0.1F, 23, 0.5F, 25.5F, -0.4F, 29, -0.58F, 36, -0.2F, 50, -0.05F, 60, 0.05F,
				82, 0);
		a[LEAN] += catmullRom(t, 0, 0, 23, -0.12F, 26, 0.1F, 32, 0.06F, 50, 0, 82, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, 0.15F, 16, 0.05F, 23, -0.1F, 25.5F, 0.3F, 29, 0.35F, 36, 0.1F, 56, -0.05F,
				60.5F, 0.2F, 64, 0.1F, 82, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 18, 0.1F, 23, 0.35F, 26, 0.45F, 32, 0.2F, 56, 0.15F, 60.5F, 0.45F, 64, 0.3F,
				76, 0, 82, 0);
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.25F, 14, 0.3F, 18, -0.05F, 28, -0.15F, 40, -0.25F, 56, -0.28F, 60.5F, -0.1F,
				70, -0.05F, 82, 0);
		a[HEAD_TURN] += catmullRom(t, 26, 0, 38, -0.02F, 46, -0.25F, 54, -0.4F, 58, -0.2F, 61, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 22, 0.1F, 25.5F, -0.35F, 32, -0.25F, 50, 0, 82, 0);
		a[LIFT_R] += HemoEasing.window(t, 23, 24.5F, 25, 26.5F) * 0.6F;
	}

	/**
	 * 20 / 4 / 26. The returned halo held overhead in both hands, the staff lifted with them; he rises onto his toes
	 * and rears further back; at 20 he brings it down to his feet in a tick and a half, folding hard over it, a
	 * judder through him; the halo rises back to his skull as he comes up.
	 */
	static void crownFall(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 3, 28, 36);
		a[R_ARM_X] += catmullRom(t, 0, -2.4F, 12, -2.65F, 18, -2.9F, 19.5F, -2.95F, 21.5F, -0.35F, 23, -0.1F, 28, -0.2F,
				36, -0.6F, 42, -0.4F, 50, 0);
		a[L_ARM_X] += catmullRom(t, 0, -2.2F, 12, -2.45F, 18, -2.75F, 19.5F, -2.8F, 21.5F, -0.4F, 23, -0.2F, 28, -0.25F,
				36, -0.2F, 50, 0);
		a[R_ARM_Y] += catmullRom(t, 0, -0.35F, 19.5F, -0.4F, 22, -0.25F, 36, -0.1F, 50, 0);
		a[L_ARM_Y] += catmullRom(t, 0, 0.35F, 19.5F, 0.4F, 22, 0.25F, 36, 0.1F, 50, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 10, -0.4F, 19.5F, -0.55F, 22, 0.3F, 30, 0.2F, 40, 0, 50, 0);
		a[STAFF_Z] += catmullRom(t, 0, 0, 12, 0.35F, 20, 0.4F, 24, 0.1F, 40, 0, 50, 0);
		a[BOW] += catmullRom(t, 0, -0.25F, 12, -0.32F, 19.5F, -0.42F, 21.5F, 0.62F, 23.5F, 0.78F, 30, 0.6F, 40, 0.2F,
				50, 0);
		a[CROUCH] += catmullRom(t, 0, -0.1F, 18, -0.15F, 21.5F, 1.35F, 23.5F, 1.55F, 30, 1.3F, 40, 0.5F, 50, 0);
		a[HEAD] += catmullRom(t, 0, -0.3F, 19.5F, -0.35F, 21.5F, 0.38F, 24, 0.45F, 34, 0.3F, 50, 0);
		a[RIBS] += catmullRom(t, 0, 0.1F, 19.5F, 0.2F, 22, -0.1F, 30, 0, 50, 0);
		a[FINGERS] += catmullRom(t, 0, 0.6F, 40, 0.6F, 50, 0);
		a[HALO_HELD] = HemoEasing.window(t, 0, 0.5F, 20.5F, 21.5F);
		a[HALO_GROUND] = HemoEasing.window(t, 20.5F, 21.8F, 32, 40);
		a[SHAKE] += HemoEasing.window(t, 21.5F, 22, 24, 30) * 0.6F;
	}

	/**
	 * 24 / 6 / 20. Three tearing beats: the claw grips the iron in his left arm and rips it out, twice, then at his
	 * ribs, the body flinching from each; it cocks low behind him, then flings the iron straight up overhead, letting
	 * go at 24 and arching after it, and he recovers hunched round the torn arm.
	 */
	static void ironThicket(float[] a, float t) {
		float rips = HemoEasing.window(t, 2, 3.5F, 4.5F, 7) + HemoEasing.window(t, 7, 8.5F, 9.5F, 12)
				+ HemoEasing.window(t, 12, 13.5F, 14.5F, 17);
		float grips = HemoEasing.window(t, 1, 2, 3.5F, 4.5F) + HemoEasing.window(t, 6, 7, 8.5F, 9.5F)
				+ HemoEasing.window(t, 11, 12, 13.5F, 14.5F);
		a[R_ARM_X] += catmullRom(t, 0, 0, 2, -0.9F, 12, -0.95F, 14, -0.55F, 17, -0.5F, 20, 0.55F, 22.5F, 0.75F, 25, -2.8F,
				26.5F, -3.05F, 29, -2.6F, 34, -1.0F, 40, -0.6F, 50, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 2, -1.05F, 11, -1.0F, 13, -0.45F, 17, -0.4F, 21, 0.2F, 25, -0.1F, 31, -0.2F,
				37, -0.65F, 44, -0.4F, 50, 0) + rips * 0.35F;
		a[R_FORE] += catmullRom(t, 0, 0, 2, -1.4F, 12, -1.45F, 14, -1.2F, 18, -0.6F, 22.5F, -0.9F, 25, -0.05F, 29, 0.05F,
				37, -1.3F, 44, -0.9F, 50, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 17, 0, 21, 0.6F, 24, 0.7F, 25.5F, -0.85F, 31, -0.5F, 37, 0.3F, 50, 0)
				+ grips * 0.9F - rips * 0.5F;
		a[L_ARM_X] += catmullRom(t, 0, 0, 3, -0.6F, 11, -0.65F, 13, -0.2F, 20, 0, 29, -0.25F, 37, -0.45F, 44, -0.3F,
				50, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 3, 0.3F, 11, 0.3F, 14, 0, 50, 0);
		a[L_FORE] += catmullRom(t, 0, 0, 29, 0, 37, -0.45F, 44, -0.3F, 50, 0);
		a[BOW] += rips * 0.18F + catmullRom(t, 0, 0, 2, 0.2F, 17, 0.2F, 21, 0.35F, 25, -0.35F, 27, -0.42F, 33, 0,
				39, 0.28F, 50, 0);
		a[HEAD] += rips * 0.2F + catmullRom(t, 0, 0, 2, 0.25F, 17, 0.3F, 25, -0.4F, 29, -0.38F, 37, 0.2F, 50, 0);
		a[TWIST] += catmullRom(t, 0, 0, 2, -0.3F, 12, -0.25F, 17, -0.05F, 21.5F, 0.45F, 25, -0.32F, 29, -0.35F, 37, -0.1F,
				50, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 4, 0.25F, 17, 0.3F, 22, 0.55F, 25, 0.1F, 27, 0.2F, 37, 0.35F, 50, 0);
		a[SHAKE] += rips * 0.7F + HemoEasing.window(t, 27, 31, 40, 50) * 0.3F;
	}

	/**
	 * 30 / 100 / 20. Arms raised wide, the staff lifted with the left, ribs prised open, the mass pressing out, arching
	 * back with a shudder building; a hitch at 29; at 30 everything drops: the arms fall, the ribs cave, he folds deep
	 * over his knees and bounces. Wrung, he hangs there breathing shallow, the staff planted to hold him; then he
	 * tries to stand twice and slips back before he makes it as the tide stands.
	 */
	static void exsanguinatingTide(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 2, 8, 34, 44);
		a[STAFF_FOOT_Z] += catmullRom(t, 34, 0, 44, -2.5F, 120, -2.5F, 150, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -1.4F, 22, -2.0F, 28, -2.15F, 29.5F, -2.2F, 31.5F, 0.2F, 33, 0.4F, 36, 0.2F,
				70, 0.25F, 110, 0.15F, 150, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, -1.2F, 22, -1.8F, 28, -1.95F, 29.5F, -2.0F, 31.5F, 0.05F, 33, 0.2F,
				38, 0.05F, 70, 0.1F, 150, 0);
		a[STAFF_Z] += catmullRom(t, 0, 0, 12, 0.25F, 29.5F, 0.3F, 33, 0.05F, 44, 0, 150, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 14, 0.6F, 26, 0.9F, 29.5F, 1.0F, 31.5F, 0.15F, 34, 0, 150, 0);
		a[RIBS] += catmullRom(t, 0, 0, 14, 0.3F, 26, 0.45F, 29.5F, 0.52F, 31.5F, -0.28F, 34, -0.32F, 70, -0.2F,
				110, -0.05F, 150, 0);
		a[BOW] += catmullRom(t, 0, 0, 14, -0.25F, 26, -0.38F, 29.5F, -0.44F, 31.5F, 0.72F, 34, 0.88F, 40, 0.78F,
				70, 0.72F, 86, 0.55F, 92, 0.62F, 104, 0.38F, 112, 0.45F, 130, 0.15F, 150, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 20, -0.1F, 29.5F, -0.15F, 31.5F, 1.15F, 34, 1.32F, 40, 1.15F, 70, 1.1F,
				86, 0.75F, 92, 0.9F, 104, 0.5F, 112, 0.6F, 132, 0.15F, 150, 0);
		a[HEAD] += catmullRom(t, 0, 0, 14, -0.3F, 29.5F, -0.5F, 31.5F, 0.5F, 34, 0.6F, 70, 0.55F, 110, 0.3F, 150, 0);
		a[CLAVICLE] += catmullRom(t, 0, 0, 20, 0.3F, 29.5F, 0.38F, 32, -0.1F, 70, -0.05F, 150, 0);
		a[SHAKE] += HemoEasing.ramp(t, 6, 29) * 0.7F * (1.0F - HemoEasing.ramp(t, 30, 31))
				+ HemoEasing.window(t, 31, 32, 36, 46) * 0.4F;
		float sag = HemoEasing.window(t, 34, 40, 66, 90);
		a[RIBS] += Mth.sin(t * 0.4F) * 0.04F * sag;
		a[HEAD_ROLL] += Mth.sin(t * 0.05F) * 0.08F * sag;
	}

	/**
	 * 24 / 10 / 24. The ribs prise apart in jerks with the shoulders drawn back and a whine and a shudder building;
	 * a locked breath at 23; at 24 the vent flings them wide and throws him back on his heels, past open, held
	 * trembling (Strained takes the recovery).
	 */
	static void ribBurst(float[] a, float t) {
		a[RIBS] += catmullRom(t, 0, 0, 5, 0.18F, 7, 0.15F, 11, 0.3F, 13, 0.27F, 18, 0.42F, 22, 0.47F, 23.5F, 0.5F,
				25, 0.8F, 27, 0.88F, 30, 0.78F, 34, 0.72F, 46, 0.5F, 58, 0);
		a[CLAVICLE] += catmullRom(t, 0, 0, 10, 0.3F, 22, 0.42F, 25, 0.55F, 30, 0.45F, 58, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 10, 0.25F, 22, 0.4F, 25, 0.9F, 27, 1.0F, 32, 0.75F, 46, 0.5F, 58, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.25F, 22, -0.35F, 23.5F, -0.42F, 25, -0.58F, 27, -0.62F, 32, -0.45F,
				46, -0.3F, 58, 0) - (HemoEasing.window(t, 4.5F, 5, 5.5F, 7) + HemoEasing.window(t, 10.5F, 11, 11.5F, 13)
				+ HemoEasing.window(t, 17.5F, 18, 18.5F, 20)) * 0.06F;
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.2F, 22, -0.32F, 25, -0.6F, 27, -0.65F, 34, -0.4F, 58, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, 0.3F, 22, 0.4F, 25, -0.2F, 28, -0.3F, 40, 0, 58, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, 0.15F, 22, 0.2F, 25, -0.1F, 40, 0, 58, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 20, 0.15F, 23.5F, 0.25F, 25, 0.55F, 28, 0.62F, 34, 0.4F, 58, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 22, 0.7F, 24.5F, -0.9F, 30, -0.6F, 58, 0);
		a[SHAKE] += HemoEasing.ramp(t, 2, 23) * 0.75F * (1.0F - HemoEasing.ramp(t, 24, 25))
				+ HemoEasing.window(t, 26, 28, 50, 58) * 0.45F;
	}

	/**
	 * 20 / 30 / 20. He drops low with his arms flung wide, the ribs prised open and the face cords parting over the
	 * void, leaning in; through the pull the arms beckon in a seven-tick rhythm, the claw curling, the chest heaving,
	 * his weight hauled back; at 48 the arms snap shut and he straightens.
	 */
	static void gorge(float[] a, float t) {
		float pull = HemoEasing.window(t, 20, 22, 46, 48);
		float beckon = Mth.sin((t - 20) * Mth.TWO_PI / 7.0F);
		a[CROUCH] += catmullRom(t, 0, 0, 8, 1.0F, 16, 1.2F, 20, 1.3F, 48, 1.15F, 50, 0.9F, 60, 0.3F, 70, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 8, 0.7F, 16, 1.0F, 20, 1.05F, 47, 0.95F, 49, -0.25F, 52, -0.1F, 62, 0, 70, 0)
				- (0.5F + 0.5F * beckon) * 0.3F * pull;
		a[FACE] += catmullRom(t, 0, 0, 10, 0.5F, 18, 1.0F, 47, 1.0F, 49, 0.1F, 60, 0, 70, 0);
		a[RIBS] += catmullRom(t, 0, 0, 10, 0.3F, 18, 0.45F, 47, 0.4F, 49, 0.05F, 60, 0, 70, 0)
				+ Mth.sin((t - 20) * 0.9F) * 0.08F * pull;
		a[BOW] += catmullRom(t, 0, 0, 10, 0.3F, 17, 0.45F, 20, 0.55F, 26, 0.3F, 47, 0.28F, 49, 0.45F, 56, 0.2F, 70, 0);
		a[HEAD] += catmullRom(t, 0, 0, 12, -0.3F, 20, -0.38F, 47, -0.3F, 49, 0.15F, 60, 0, 70, 0);
		a[R_FORE] += (-0.55F + 0.35F * beckon) * pull;
		a[CLAW_FLEX] += 0.6F * pull;
		a[SHAKE] += 0.25F * pull;
	}

	/**
	 * The Last Thirst, 40 ticks of tell then the rooted draw to 240. He drags himself to the pit low over his knees,
	 * the claw hauling at the floor in ten-tick pulls and the staff his crutch; raises the staff and drives it into
	 * the pit; then rises and spreads his arms to all four wells, turning slowly between them as the swell climbs, a
	 * shudder through him every twenty ticks.
	 */
	static void lastThirst(float[] a, float t) {
		float drag = HemoEasing.window(t, 0, 3, 34, 38);
		float haul = Mth.sin(t * Mth.TWO_PI / 10.0F);
		a[BOW] += 0.5F * drag;
		a[CROUCH] += (0.5F + 0.1F * haul) * drag;
		a[HEAD] -= 0.3F * drag;
		a[R_ARM_X] += (-0.75F + 0.45F * haul) * drag;
		a[R_FORE] += (-0.2F - 0.25F * haul) * drag;
		a[FINGERS] += (0.2F + 0.6F * Math.max(0.0F, -haul)) * drag;
		a[CLAW_FLEX] += 0.4F * drag;
		a[TWIST] += haul * 0.12F * drag;
		a[STAFF_PLANT] -= HemoEasing.window(t, 34, 37, 40, 42);
		a[L_ARM_X] += catmullRom(t, 0, 0, 34, 0, 38, -1.35F, 41, 0.4F, 44, 0.3F, 60, 0.2F, 240, 0.2F);
		a[STAFF_FOOT_Y] += catmullRom(t, 34, 0, 38, -3.5F, 41, 1.4F, 44, 1.2F, 240, 1.2F);
		a[CROUCH] += catmullRom(t, 38, 0, 41, 0.7F, 46, 0.5F, 60, 0.2F, 240, 0.2F);
		a[BOW] += catmullRom(t, 38, 0, 41, 0.45F, 46, 0.3F, 60, -0.12F, 240, -0.12F);
		float root = HemoEasing.ramp(t, 42, 60);
		float turning = HemoEasing.ramp(t, 60, 80);
		a[SPREAD] += 0.9F * root;
		a[HEAD] -= 0.42F * root;
		a[RIBS] += (0.15F + 0.25F * HemoEasing.ramp(t, 60, 240)) * root + Mth.sin(t * 0.35F) * 0.05F * root;
		a[CLAVICLE] += 0.25F * root;
		a[CLAW_FLEX] += 0.45F * root;
		a[TWIST] += Mth.sin((t - 60) * Mth.TWO_PI / 80.0F) * 0.22F * turning;
		a[HEAD_TURN] += Mth.sin((t - 66) * Mth.TWO_PI / 80.0F) * 0.25F * turning;
		a[SHAKE] += t > 60 ? HemoEasing.window((t - 60) % 20.0F, 0, 1, 3, 6) * 0.55F : 0.0F;
	}

	// ---------------------------------------------------------------- openings, the card and the ending

	/** Each opening's body over the stance, from the moment it opens. */
	static void opening(float[] a, int opening, float t) {
		if (opening == HemorathOpenings.GORGED.id()) {
			// a wet gulp, then the knees buckle and he sags onto the staff, sated and heaving
			a[HEAD] += catmullRom(t, 0, 0, 1.5F, -0.25F, 3, 0.3F, 6, 0.5F, 10, 0.48F, 50, 0.42F)
					- HemoEasing.pulse(t - 24, 1.5F, 6) * 0.2F;
			a[HEAD_ROLL] += catmullRom(t, 0, 0, 6, 0.12F, 50, 0.18F);
			a[CROUCH] += catmullRom(t, 0, 0, 3, 0.4F, 7, 1.0F, 10, 0.82F, 44, 0.75F, 50, 0.6F);
			a[BOW] += catmullRom(t, 0, 0, 3, 0.1F, 8, 0.4F, 12, 0.35F, 50, 0.3F)
					+ Mth.sin(t * 0.22F + 0.6F) * 0.03F * HemoEasing.ramp(t, 6, 12);
			a[LEAN] += catmullRom(t, 0, 0, 8, 0.18F, 50, 0.15F);
			a[STAFF_FOOT_X] += catmullRom(t, 0, 0, 8, 1.5F, 50, 1.5F);
			a[STAFF_FOOT_Z] += catmullRom(t, 0, 0, 8, -1.0F, 50, -1.0F);
			a[L_ARM_X] += catmullRom(t, 0, 0, 8, -0.3F, 50, -0.25F);
			a[R_ARM_X] += catmullRom(t, 0, 0, 8, 0.3F, 50, 0.25F);
			a[R_FORE] += catmullRom(t, 0, 0, 8, 0.35F, 50, 0.3F);
			a[FINGERS] += catmullRom(t, 0, 0, 8, 0.25F, 50, 0.2F);
			a[RIBS] += catmullRom(t, 0, 0, 2, 0.18F, 6, 0.3F, 50, 0.3F) + Mth.sin(t * 0.22F) * 0.06F * HemoEasing.ramp(t, 6, 12);
		} else if (opening == HemorathOpenings.STRAINED.id()) {
			// the ribs lock wide with a crack; the shoulders shake at half a beat a second
			a[RIBS] += catmullRom(t, 0, 0.35F, 1, 0.55F, 3, 0.48F, 40, 0.5F);
			a[CLAVICLE] += catmullRom(t, 0, 0.2F, 2, 0.35F, 40, 0.35F);
			a[BOW] += -0.3F - HemoEasing.window(t, 0, 0.5F, 1, 3) * 0.12F;
			a[SPREAD] += catmullRom(t, 0, 0.4F, 2, 0.6F, 40, 0.55F);
			a[HEAD] += -0.3F;
			a[CROUCH] += 0.3F;
			a[SHAKE] += 0.35F + 0.3F * (0.5F + 0.5F * Mth.sin(t * Mth.TWO_PI / 40.0F));
		} else if (opening == HemorathOpenings.PLANTED.id()) {
			// from the blow into both hands on the staff stuck ahead of him, heaving
			a[STAFF_FOOT_Z] += -4.5F;
			a[L_ARM_X] += catmullRom(t, 0, -0.6F, 6, -0.75F, 24, -0.7F);
			a[R_ARM_X] += catmullRom(t, 0, -0.7F, 6, -0.95F, 24, -0.9F);
			a[R_ARM_Y] += -0.55F;
			a[R_FORE] += -0.55F;
			a[FINGERS] += 0.6F;
			a[BOW] += catmullRom(t, 0, 0.6F, 6, 0.42F, 24, 0.38F);
			a[CROUCH] += catmullRom(t, 0, 1.0F, 6, 0.6F, 24, 0.55F);
			a[HEAD] += catmullRom(t, 0, 0.4F, 6, 0.25F, 24, 0.22F);
			a[RIBS] += Mth.sin(t * 0.45F) * 0.07F;
		} else if (opening == HemorathOpenings.HALOLESS.id()) {
			// the bare iron crown of his skull raised, searching
			a[HEAD] -= 0.3F * HemoEasing.ramp(t, 0, 6);
			a[HEAD_TURN] += Mth.sin(t * 0.11F) * 0.2F;
			a[R_ARM_X] -= 0.35F * HemoEasing.ramp(t, 0, 8);
			a[FINGERS] -= 0.3F;
			a[HALO_AWAY] = 1.0F;
		} else if (opening == HemorathOpenings.CLOTTED.id()) {
			// a jerk as the streams freeze, then a stiff stagger back
			a[BOW] += catmullRom(t, 0, 0, 1, -0.38F, 3, -0.25F, 6, 0.3F, 10, 0.15F, 30, 0);
			a[HEAD] += catmullRom(t, 0, 0, 1, -0.35F, 4, 0.1F, 30, 0);
			a[SPREAD] += catmullRom(t, 0, 0, 1, 0.5F, 4, 0.2F, 12, 0, 30, 0);
			a[STEP_L] += catmullRom(t, 0, 0, 3, 0.3F, 7, 0.35F, 16, 0.1F, 30, 0);
			a[LIFT_L] += HemoEasing.window(t, 1, 2.5F, 3, 4.5F) * 0.6F;
			a[STEP_R] += catmullRom(t, 0, 0, 5, 0, 8, 0.3F, 12, 0.32F, 22, 0.05F, 30, 0);
			a[LIFT_R] += HemoEasing.window(t, 5, 6.5F, 7, 8.5F) * 0.6F;
			a[LEAN] += catmullRom(t, 0, 0, 3, 0.15F, 6, -0.14F, 10, 0.1F, 15, -0.05F, 30, 0);
			a[CROUCH] += catmullRom(t, 0, 0, 4, 0.3F, 12, 0.2F, 30, 0);
			a[SHAKE] += HemoEasing.window(t, 0, 0.5F, 6, 14) * 0.8F + 0.2F * (1.0F - HemoEasing.ramp(t, 14, 30));
		} else if (opening == HemorathOpenings.WRUNG.id()) {
			// emptied: the ribs fold in, he bows over his knees, the arms hang slack
			float w = HemoEasing.ramp(t, 0, 6);
			a[RIBS] -= 0.2F * w;
			a[BOW] += 0.7F * w;
			a[CROUCH] += 1.0F * w;
			a[HEAD] += 0.55F * w;
			a[R_ARM_X] += 0.2F * w;
			a[HEAD_ROLL] += Mth.sin(t * 0.08F) * 0.06F * w;
		} else if (opening == HemorathOpenings.BROKEN_GRASP.id()) {
			// the arms snap shut on nothing and he reels back two steps, flailing for balance
			a[SPREAD] += catmullRom(t, 0, 0.9F, 2, -0.25F, 6, 0.4F, 14, 0.2F, 30, 0);
			a[BOW] += catmullRom(t, 0, 0, 3, -0.35F, 8, -0.25F, 16, -0.05F, 30, 0);
			a[HEAD] += catmullRom(t, 0, 0, 3, -0.3F, 10, -0.1F, 30, 0);
			a[STEP_L] += catmullRom(t, 0, 0, 2, 0, 6, 0.4F, 10, 0.3F, 20, 0.05F, 30, 0);
			a[LIFT_L] += HemoEasing.window(t, 2, 3.5F, 4.5F, 6.5F) * 0.7F;
			a[STEP_R] += catmullRom(t, 0, 0, 7, 0, 12, 0.4F, 16, 0.32F, 24, 0.05F, 30, 0);
			a[LIFT_R] += HemoEasing.window(t, 7, 8.5F, 9.5F, 11.5F) * 0.7F;
			a[LEAN] += catmullRom(t, 0, 0, 4, -0.1F, 9, 0.12F, 14, -0.06F, 22, 0, 30, 0);
			a[CROUCH] += catmullRom(t, 0, 0, 6, 0.3F, 14, 0.35F, 30, 0);
			a[R_ARM_Z] += catmullRom(t, 0, 0, 2, 0, 6, 0.5F, 14, 0.3F, 30, 0);
			a[L_ARM_Z] += catmullRom(t, 0, 0, 2, 0, 6, -0.35F, 14, -0.2F, 30, 0);
		} else if (opening == HemorathOpenings.TITHE.id()) {
			titheReckoning(a, Math.min(t, 34.0F), 400);
		}
	}

	/**
	 * The card, 50 ticks. He locks rigid and trembles; at 10 the ribs fling wide and the arms with them, the face cords
	 * part and the skull is thrown back as the mass empties; it drains, he bows deep over his knees, then straightens
	 * into phase two's stance.
	 */
	static void vesselEmpties(float[] a, float t) {
		a[SHAKE] += HemoEasing.window(t, 0, 0.5F, 9, 10.5F) * 0.6F;
		a[CLAVICLE] += catmullRom(t, 0, 0, 2, 0.2F, 10, 0.22F, 12, 0.4F, 16, 0.3F, 30, 0.05F, 50, 0);
		a[RIBS] += catmullRom(t, 0, 0.05F, 10, 0.1F, 11.5F, 0.6F, 13, 0.7F, 16, 0.6F, 30, 0.12F, 50, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 10, 0, 11.5F, 0.85F, 13, 0.95F, 16, 0.7F, 30, 0.15F, 50, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.12F, 11.5F, -0.45F, 13, -0.52F, 18, -0.35F, 30, 0.62F, 36, 0.7F, 42, 0.3F,
				47, -0.05F, 50, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.05F, 12, -0.55F, 14, -0.6F, 22, -0.4F, 30, 0.4F, 38, 0.45F, 46, 0.05F, 50, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 12, 0.3F, 18, 0.25F, 32, 0.75F, 38, 0.8F, 46, 0.2F, 50, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 12, -0.3F, 30, 0.25F, 50, 0);
		a[FACE] += HemoEasing.window(t, 10, 14, 30, 34);
	}

	/**
	 * The ending, 125 ticks (section 11). Locked rigid in hit-stop to 6; the burst flings the ribs and arms wide and
	 * throws the skull back, the halo dropping off it to the floor; he empties and sinks to 80, arms slack, swaying;
	 * then the fold: the knees go, then the chest, then the bowed head goes down into the robes, which spread into a
	 * heap; the staff leans away as he goes.
	 */
	static void ending(float[] a, float t) {
		a[SHAKE] += HemoEasing.window(t, 0, 0.3F, 5, 6) * 0.7F + HemoEasing.window(t, 30, 36, 70, 85) * 0.3F;
		a[CLAVICLE] += catmullRom(t, 0, 0, 6, 0.15F, 10, 0.5F, 30, 0.2F, 80, -0.1F, 125, -0.1F);
		a[RIBS] += catmullRom(t, 0, 0.1F, 6, 0.15F, 9, 0.75F, 12, 0.82F, 30, 0.45F, 80, -0.05F, 125, -0.1F);
		a[SPREAD] += catmullRom(t, 0, 0, 6, 0, 9, 1.0F, 12, 1.08F, 30, 0.45F, 60, 0.1F, 125, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, -0.08F, 9, -0.5F, 12, -0.55F, 30, -0.1F, 50, 0.25F, 80, 0.45F, 92, 0.6F,
				102, 1.05F, 110, 1.12F, 125, 1.15F);
		a[HEAD] += catmullRom(t, 0, 0, 6, 0, 9, -0.45F, 12, -0.5F, 30, 0.05F, 80, 0.35F, 100, 0.5F, 108, 0.92F, 125, 0.95F);
		a[CROUCH] += catmullRom(t, 0, 0, 30, 0, 55, 0.5F, 80, 1.1F, 92, 2.3F, 110, 2.45F, 125, 2.5F);
		a[SLUMP] += catmullRom(t, 80, 0, 95, 0.35F, 110, 1.0F, 125, 1.3F);
		a[HALO_DROP] = catmullRom(t, 6, 0, 9, 0.2F, 14, 1.0F, 125, 1.0F);
		a[R_ARM_X] += catmullRom(t, 0, 0, 9, -0.6F, 12, -0.7F, 30, 0.15F, 80, 0.3F, 110, 0.1F, 125, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 9, -0.4F, 30, 0.1F, 80, 0.2F, 125, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 9, -0.9F, 30, -0.4F, 80, 0.3F, 125, 0.4F);
		a[STAFF_FOOT_X] += catmullRom(t, 0, 0, 80, 0, 110, 5.0F, 125, 5.5F);
		a[STAFF_FOOT_Z] += catmullRom(t, 0, 0, 80, 0, 110, 2.0F, 125, 2.0F);
		a[HEAD_ROLL] += Mth.sin(t * 0.06F) * 0.06F * HemoEasing.window(t, 30, 40, 75, 85);
	}

	// ---------------------------------------------------------------- the overlap

	/**
	 * The springs' pass, after the blend: what trails his motion. Hollow, the springs are tight and quick; full, slow
	 * and loose, so the heavy body sways after itself. Each value is how far a part trails what carries it.
	 */
	static void secondary(float[] a, HemorathAnimState motion, float swell) {
		float freq = HemoEasing.lerp(0.62F, 0.38F, swell), damping = HemoEasing.lerp(0.42F, 0.3F, swell);
		float spine = a[BOW] + a[HIP_X];
		float speed = motion.speed() + motion.lurch() * 0.35F;
		// the skull and the spine lag the shoulders and the hips
		a[LAG_HEAD_X] = trail(motion.lag(0, spine + a[CROUCH] * 0.15F, freq * 1.15F, damping + 0.1F) * 0.55F);
		a[LAG_HEAD_Y] = trail(motion.lag(1, a[TWIST] + a[HIP_TWIST], freq * 1.1F, damping + 0.1F) * 0.6F);
		a[LAG_HEAD_Z] = trail(motion.lag(2, a[LEAN] + a[HIP_SWAY], freq, damping) * 0.5F);
		a[LAG_SPINE] = trail(-motion.lag(3, a[CROUCH] * 0.5F, freq * 0.9F, damping) * 0.35F);
		// the forearm and the claw trail the arm's swing and whip through when it stops
		a[LAG_FORE] = trail(motion.lag(4, a[R_ARM_X] + a[R_ARM_Y] * 0.5F, freq * 1.3F, damping) * 0.5F);
		a[LAG_FINGERS] = trail(motion.lag(5, a[R_ARM_X] + a[R_FORE], freq * 1.6F, damping) * 0.4F);
		// the hanging chains swing off the chest and his stride, and their tips trail their roots
		a[SWING_X] = trail(-motion.lag(6, spine * 0.8F + a[CROUCH] * 0.1F, 0.3F, 0.22F) * 1.1F
				+ motion.follow(7, speed, 0.32F, 0.3F));
		a[SWING_Z] = trail(-motion.lag(8, a[LEAN] + a[HIP_SWAY] + a[TWIST] * 0.3F, 0.3F, 0.25F));
		a[TURN_SWING] = trail(-motion.lag(9, motion.yawTravel(), 0.3F, 0.25F) * 1.2F);
		a[BEND_X] = trail(motion.lag(10, a[SWING_X], 0.55F, 0.3F) * 0.9F);
		a[BEND_Z] = trail(motion.lag(11, a[SWING_Z] + a[TURN_SWING], 0.55F, 0.3F) * 0.9F);
		// the staff's crown chains swing off the staff
		a[STAFF_SWING_X] = trail(-motion.lag(12, a[STAFF_X] + a[L_ARM_X] * 0.5F + a[STAFF_FOOT_Z] * 0.06F + spine * 0.4F,
				0.3F, 0.2F) * 1.1F + motion.follow(13, speed, 0.32F, 0.3F));
		a[STAFF_SWING_Z] = trail(-motion.lag(14, a[STAFF_Z] + a[L_ARM_Z] * 0.5F + a[STAFF_FOOT_X] * 0.06F + a[LEAN] * 0.5F,
				0.3F, 0.2F) * 1.1F);
		a[STAFF_BEND_X] = trail(motion.lag(15, a[STAFF_SWING_X], 0.55F, 0.3F) * 0.9F);
		a[STAFF_BEND_Z] = trail(motion.lag(16, a[STAFF_SWING_Z], 0.55F, 0.3F) * 0.9F);
		// the vestments trail his stride and settle forward when he stops; the train drags round after a turn
		a[ROBE_X] = trail(motion.follow(17, speed * 0.65F, 0.35F, 0.32F) - motion.lag(18, a[CROUCH] * 0.3F + a[HIP_X], 0.4F,
				0.3F) * 0.5F);
		a[ROBE_Z] = trail(-motion.lag(19, a[HIP_SWAY] + a[HIP_TWIST], 0.35F, 0.3F) * 0.8F);
		a[SHROUD_X] = trail(-motion.lag(20, spine, 0.32F, 0.28F) * 0.8F + motion.follow(21, speed * 0.5F, 0.3F, 0.3F));
		a[TRAIN_Y] = trail(motion.lag(22, motion.yawTravel(), 0.22F, 0.45F) * 0.8F);
		// the ribs ring after they fling, the cords swing off the skull, the halo's chains off the halo
		a[RIB_JIG] = trail(-motion.lag(23, a[RIBS] + a[SPREAD] * 0.3F, 0.9F, 0.22F) * 0.5F);
		a[CORD_X] = trail(-motion.lag(24, a[HEAD] + a[LAG_HEAD_X] + spine, 0.45F, 0.2F) * 0.9F);
		a[CORD_Z] = trail(-motion.lag(25, a[HEAD_TURN] + a[HEAD_ROLL] + a[LEAN] + a[TWIST], 0.45F, 0.2F) * 0.7F);
		a[HALO_SWING_X] = trail(-motion.lag(26, a[HALO_HELD] * 1.5F + spine + a[R_ARM_X] * a[HALO_HELD] * 0.4F, 0.4F, 0.2F)
				* 0.9F);
		a[HALO_SWING_Z] = trail(-motion.lag(27, a[LEAN] + a[R_ARM_Y] * a[HALO_HELD] * 0.4F, 0.4F, 0.2F) * 0.9F);
		// the heart mass wobbles inside the ribs when the body jolts
		a[MASS_JIG] = trail(-motion.lag(28, a[RIBS] + a[CROUCH] * 0.3F + spine * 0.5F, 0.8F, 0.25F) * 0.12F);
		a[FLINCH] = trail(motion.flinch());
	}

	private static float trail(float value) {
		return Mth.clamp(value, -TRAIL_LIMIT, TRAIL_LIMIT);
	}

	// ---------------------------------------------------------------- laying it on the rig

	private ModelPart[] leftRibs, rightRibs, fingers, fingerTips, robes, shrouds, trains, trainBends, patchHosts;
	private ModelPart[][] shoulderChains, staffChains, haloChains, waistLinks;
	private ModelPart[] shoulderPendants, staffPendants, haloPendants;
	/** The staff's length from the grip to its iron foot, and where that foot stands at rest (model units). */
	float staffLength = Float.NaN;
	final org.joml.Vector3f staffFootRest = new org.joml.Vector3f();
	private static final float[] ROBE_LENGTH = new float[14];

	static {
		for (int i = 0; i < 14; i++) ROBE_LENGTH[i] = 12.0F + (i * 3 % 4);
	}

	/** Finds the parts the pass works through once, and measures the staff at rest. */
	private void bind() {
		leftRibs = new ModelPart[7];
		rightRibs = new ModelPart[7];
		for (int i = 0; i < 7; i++) {
			leftRibs[i] = ribcage.getChild("left_rib_" + i);
			rightRibs[i] = ribcage.getChild("right_rib_" + i);
		}
		fingers = new ModelPart[4];
		fingerTips = new ModelPart[4];
		for (int i = 0; i < 4; i++) {
			fingers[i] = rightHand.getChild("right_finger_" + i);
			fingerTips[i] = fingers[i].getChild("right_finger_" + i + "_tip");
		}
		robes = new ModelPart[14];
		for (int i = 0; i < 14; i++) robes[i] = hips.getChild("robe_strip_" + i);
		shrouds = new ModelPart[8];
		for (int i = 0; i < 8; i++) shrouds[i] = chest.getChild("shroud_" + i);
		trains = new ModelPart[9];
		trainBends = new ModelPart[9];
		for (int i = 0; i < 9; i++) {
			trains[i] = train.getChild("train_" + i);
			trainBends[i] = trains[i].getChild("train_" + i + "_bend");
		}
		patchHosts = new ModelPart[]{rightForearm, rightArm, leftRib2, rightRib5, leftForearm, leftArm, rightShin, leftShin,
				head};
		shoulderChains = new ModelPart[][]{links(leftShoulderChain, "left_shoulder_chain", 9),
				links(leftShoulderChainFront, "left_shoulder_chain_front", 11),
				links(rightShoulderChain, "right_shoulder_chain", 9),
				links(rightShoulderChainFront, "right_shoulder_chain_front", 11)};
		shoulderPendants = new ModelPart[]{leftShoulderChainPendant, null, rightShoulderChainPendant, null};
		staffChains = new ModelPart[][]{links(staffChainLeft, "staff_chain_left", 5),
				links(staffChainRight, "staff_chain_right", 7), links(staffChainCenter, "staff_chain_center", 4)};
		staffPendants = new ModelPart[]{staffChainLeftPendant, staffChainRightPendant, null};
		haloChains = new ModelPart[][]{links(haloChainLeft, "halo_chain_left", 4), links(haloChainRight, "halo_chain_right", 4)};
		haloPendants = new ModelPart[]{haloChainLeftPendant, haloChainRightPendant};
		waistLinks = new ModelPart[][]{links(waistChain, "waist_chain", 7)};
		float length = 0.0F;
		for (ModelPart.Cube cube : staff.cubes) length = Math.max(length, cube.maxY);
		staffLength = length;
		staffFootRest.set(world(new org.joml.Vector3f(0, length, 0), root, hips, chest, leftArm, leftForearm, leftHand, staff));
	}

	private static ModelPart[] links(ModelPart chain, String name, int count) {
		ModelPart[] links = new ModelPart[count];
		for (int i = 0; i < count; i++) links[i] = chain.getChild(name + "_link_" + i);
		return links;
	}

	/** Lays the channels on the rig, then the additive layers: swell, patches, the heartbeat and what trails. */
	void pose(float[] a, float swell, int patchCount, float patchAge, float flinch, float age, float stride,
			float movement, float yaw, float pitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		if (Float.isNaN(staffLength)) bind();
		float shake = a[SHAKE] * (Mth.sin(age * 2.1F) * 0.65F + Mth.sin(age * 3.3F + 1.0F) * 0.35F) * 0.03F;
		float spread = a[SPREAD];
		float jolt = flinch / 10.0F * 0.08F + a[FLINCH] * 0.1F;
		float chestRest = chest.xRot;
		// the hips and legs: the knees fold, then the hips sink until the lower sole meets the floor
		float restReach = HemorathGait.reach(hips.xRot + leftLeg.xRot, leftShin.xRot);
		hips.xRot += a[HIP_X];
		hips.yRot += a[HIP_TWIST];
		hips.zRot += a[HIP_SWAY];
		float fold = HemorathGait.fold(a[CROUCH], a[SLUMP]);
		float foldLeft = Math.min(HemorathGait.FOLD_MAX, fold + Math.max(0.0F, a[LIFT_L]) * 0.45F);
		float foldRight = Math.min(HemorathGait.FOLD_MAX, fold + Math.max(0.0F, a[LIFT_R]) * 0.45F);
		leftLeg.xRot += a[STEP_L] - foldLeft;
		rightLeg.xRot += a[STEP_R] - foldRight;
		leftShin.xRot += foldLeft * 2.0F;
		rightShin.xRot += foldRight * 2.0F;
		float reach = Math.max(HemorathGait.reach(hips.xRot + leftLeg.xRot, leftShin.xRot),
				HemorathGait.reach(hips.xRot + rightLeg.xRot, rightShin.xRot));
		float drop = Math.max(0.0F, restReach - reach);
		hips.y += restReach - reach;
		// the spine and the skull, the skull trailing the shoulders
		chest.xRot += a[BOW] + a[SLUMP] * 0.5F + a[LAG_SPINE] - jolt;
		chest.yRot += a[TWIST];
		chest.zRot += a[LEAN] + shake;
		float slumped = Math.min(1.0F, a[SLUMP]);
		head.xRot += a[HEAD] + a[LAG_HEAD_X] + Mth.clamp(pitch, -20, 25) * Mth.DEG_TO_RAD * (1 - slumped)
				+ a[SLUMP] * 0.6F - jolt * 1.2F;
		head.yRot += Mth.clamp(yaw, -30, 30) * Mth.DEG_TO_RAD * (1 - slumped) + a[HEAD_TURN] + a[LAG_HEAD_Y];
		head.zRot += a[HEAD_ROLL] + a[LAG_HEAD_Z];
		head.zRot -= chest.zRot * 0.7F;
		// the arms
		rightArm.xRot += a[R_ARM_X] - spread * 0.3F;
		rightArm.zRot += a[R_ARM_Z] + spread * 0.9F;
		rightArm.yRot += a[R_ARM_Y];
		rightForearm.xRot += a[R_FORE] + a[LAG_FORE];
		rightHand.xRot += a[R_HAND] + a[LAG_FORE] * 0.6F;
		leftArm.xRot += a[L_ARM_X] - spread * 0.3F;
		leftArm.zRot += a[L_ARM_Z] - spread * 0.9F;
		leftArm.yRot += a[L_ARM_Y];
		leftForearm.xRot += a[L_FORE];
		leftHand.xRot += a[L_HAND];
		// the ribs part with the meter and ring after a fling
		float ribs = HemorathVisualRules.ribSpread(swell) + a[RIBS] + a[RIB_JIG];
		for (int side = -1; side <= 1; side += 2) {
			ModelPart[] cage = side == 1 ? leftRibs : rightRibs;
			for (int i = 0; i < 7; i++) {
				float lag = Mth.sin(age * 0.17F - i * 0.65F + side * 0.3F);
				cage[i].zRot += side * (ribs * 0.55F + lag * (0.006F + Math.max(0, ribs) * 0.02F));
				cage[i].yRot += side * ribs * 0.12F;
			}
			ModelPart clavicle = side == 1 ? leftClavicle : rightClavicle;
			clavicle.zRot -= side * a[CLAVICLE];
		}
		// the claw: a curl, a creeping flex finger after finger, a fist closing a finger at a time
		for (int i = 0; i < 4; i++) {
			float flex = a[CLAW_FLEX] * Mth.sin(age * 0.45F - i * 0.9F) * 0.35F;
			float curl = a[FINGERS] + a[LAG_FINGERS] + flex + Mth.clamp(a[CLENCH] - i, 0, 1) * 0.9F;
			fingers[i].xRot += curl * (0.8F + i * 0.1F);
			fingers[i].zRot += curl < 0 ? -curl * (i - 1.5F) * 0.12F : 0;
			fingerTips[i].xRot += Math.max(-0.3F, curl) * 0.55F;
		}
		rightThumb.xRot += Math.max(-0.3F, a[FINGERS] + Mth.clamp(a[CLENCH] - 3, 0, 1)) * 0.5F;
		// the face cords part over the void and swing off the skull
		faceCordsLeft.zRot += a[FACE] * 0.55F + a[CORD_Z];
		faceCordsRight.zRot -= a[FACE] * 0.55F - a[CORD_Z];
		faceCordsLeft.xRot += a[CORD_X];
		faceCordsRight.xRot += a[CORD_X];
		faceCords.xRot += Mth.sin(age * 0.065F - 0.8F) * 0.045F;
		// the vessel's three stages and the arm vessels, scaled only between 0.8 and 1.15 once shown
		scale(vesselCords, HemorathVisualRules.stage(HemorathVisualRules.Stage.CORDS, swell), true);
		scale(vesselLobes, HemorathVisualRules.stage(HemorathVisualRules.Stage.LOBES, swell), false);
		scale(vesselMass, HemorathVisualRules.stage(HemorathVisualRules.Stage.MASS, swell), false);
		float arms = HemorathVisualRules.stage(HemorathVisualRules.Stage.ARMS, swell);
		scale(leftArmVessel, arms, false);
		scale(rightArmVessel, arms, false);
		float beat = HemorathVisualRules.heartbeat(age, swell) * 0.04F + a[MASS_JIG];
		if (vesselMass.visible) {
			vesselMass.xScale += beat;
			vesselMass.yScale += beat;
			vesselMass.zScale += beat;
		}
		// spike patches: erupted ones stand, the newest overshoots as it breaks out and jolts the bone it is in
		ModelPart[] sites = {spikePatch0, spikePatch1, spikePatch2, spikePatch3, spikePatch4, spikePatch5, spikePatch6,
				spikePatch7, spikePatch8};
		for (int i = 0; i < sites.length; i++) {
			boolean shown = i < patchCount;
			float size = shown ? (i == patchCount - 1 ? HemorathVisualRules.patch(patchAge) : 1.0F) : 0.0F;
			scale(sites[i], size, false);
		}
		if (patchCount > 0 && patchCount <= patchHosts.length)
			patchHosts[patchCount - 1].xRot -= HemoEasing.pulse(patchAge, 1.0F, HemorathVisualRules.PATCH_ERUPT) * 0.12F;
		ModelPart[] spikes = {haloSpike0, haloSpike1, haloSpike2, haloSpike3, haloSpike4, haloSpike5, haloSpike6, haloSpike7};
		float length = 0.4F + 0.6F * patchCount / 9.0F;
		for (ModelPart spike : spikes) spike.yScale = length;
		clawChain.visible = a[CHAIN] > 0.5F;
		haloPose(a);
		staffPose(a);
		hang(a, age, swell);
		// the vestments: they flare out as the hips sink so they heap on the floor, and trail his motion
		float floor = 13.0F - drop;
		for (int i = 0; i < 14; i++) {
			float angle = i * Mth.TWO_PI / 14.0F;
			float flare = Math.max(0.0F, acos(floor / ROBE_LENGTH[i]) - acos(13.0F / ROBE_LENGTH[i]));
			float trailing = a[ROBE_X] * Mth.cos(angle) + a[ROBE_Z] * Mth.sin(angle);
			robes[i].xRot += flare + Math.max(-0.15F, trailing)
					+ Mth.sin(age * 0.07F - i * 0.6F) * (0.025F + Math.abs(a[SHAKE]) * 0.05F);
		}
		float bowed = chest.xRot - chestRest + hips.xRot;
		for (int i = 0; i < 8; i++)
			shrouds[i].xRot += -bowed * 0.7F + a[SHROUD_X] + drop * 0.06F + Mth.sin(age * 0.06F - i * 0.7F) * 0.02F;
		train.yRot += a[TRAIN_Y];
		for (int i = 0; i < 9; i++)
			trainBends[i].xRot += Mth.sin(age * 0.08F - i) * 0.03F - Math.max(0.0F, a[ROBE_X]) * 0.12F;
	}

	/** The halo: on the skull, in the claw, away in flight (the world geometry draws it), driven down, or dropped. */
	private void haloPose(float[] a) {
		halo.visible = a[HALO_AWAY] < 0.5F;
		float held = HemoEasing.clamp01(a[HALO_HELD]);
		if (held > 0.0F) {
			// the claw's grip, in the chest's space (the arm hangs off the chest, as the halo does)
			org.joml.Vector3f grip = world(new org.joml.Vector3f(0, 3.0F, -0.25F), rightArm, rightForearm, rightHand);
			halo.x = HemoEasing.lerp(halo.x, grip.x, held);
			halo.y = HemoEasing.lerp(halo.y, grip.y - 6.25F, held);
			halo.z = HemoEasing.lerp(halo.z, grip.z, held);
		}
		float ground = HemoEasing.clamp01(a[HALO_GROUND]), dropped = HemoEasing.clamp01(a[HALO_DROP]);
		if (ground <= 0.0F && dropped <= 0.0F) return;
		PoseStack frame = new PoseStack();
		root.translateAndRotate(frame);
		hips.translateAndRotate(frame);
		chest.translateAndRotate(frame);
		Matrix4f toChest = new Matrix4f(frame.last().pose()).invert();
		if (ground > 0.0F) {
			org.joml.Vector3f at = toChest.transformPosition(new org.joml.Vector3f(0, 18.5F / 16.0F, -6.0F / 16.0F)).mul(16.0F);
			halo.x = HemoEasing.lerp(halo.x, at.x, ground);
			halo.y = HemoEasing.lerp(halo.y, at.y, ground);
			halo.z = HemoEasing.lerp(halo.z, at.z, ground);
		}
		if (dropped > 0.0F) {
			org.joml.Vector3f at = toChest.transformPosition(new org.joml.Vector3f(1.5F / 16.0F, 23.25F / 16.0F, 5.0F / 16.0F))
					.mul(16.0F);
			halo.x = HemoEasing.lerp(halo.x, at.x, dropped);
			halo.y = HemoEasing.lerp(halo.y, at.y, dropped);
			halo.z = HemoEasing.lerp(halo.z, at.z, dropped);
			// it comes to lie flat on the floor
			Quaternionf current = new Quaternionf().rotationZYX(halo.zRot, halo.yRot, halo.xRot);
			Quaternionf flat = rotation(hips, chest).conjugate().mul(new Quaternionf().rotationX(Mth.HALF_PI));
			setRotationFromMatrix(halo, current.slerp(flat, dropped).get(new Matrix3f()));
		}
	}

	/**
	 * The staff. Planted, its foot stays where the gait or the move put it and the grip slides along the shaft, so he
	 * leans on it and it tilts as he moves past it; lifted, it holds the angle the move gives it. Either way it stays
	 * one rigid pole through his hand.
	 */
	private void staffPose(float[] a) {
		float plant = HemoEasing.clamp01(a[STAFF_PLANT]);
		Quaternionf hand = rotation(hips, chest, leftArm, leftForearm, leftHand);
		org.joml.Vector3f grip = world(new org.joml.Vector3f(staff.x, staff.y, staff.z), root, hips, chest, leftArm,
				leftForearm, leftHand);
		org.joml.Vector3f foot = new org.joml.Vector3f(staffFootRest).add(a[STAFF_FOOT_X], Math.min(1.5F, a[STAFF_FOOT_Y]),
				a[STAFF_FOOT_Z]);
		org.joml.Vector3f planted = foot.sub(grip, new org.joml.Vector3f());
		float span = planted.length();
		if (span > 1.0E-4F) planted.div(span);
		else planted.set(0, 1, 0);
		org.joml.Vector3f free = new org.joml.Vector3f(0, 1, 0).rotateX(a[STAFF_X]).rotateZ(a[STAFF_Z]);
		org.joml.Vector3f axis = free.lerp(planted, plant, new org.joml.Vector3f());
		if (axis.lengthSquared() < 1.0E-6F) axis.set(planted);
		axis.normalize();
		Quaternionf pole = new Quaternionf().rotationTo(0, 1, 0, axis.x, axis.y, axis.z);
		setRotationFromMatrix(staff, new Quaternionf(hand).conjugate().mul(pole).get(new Matrix3f()));
		float slide = Mth.clamp(span - staffLength, -GRIP_SLIDE, GRIP_SLIDE) * plant;
		org.joml.Vector3f along = new Quaternionf(hand).conjugate().transform(new org.joml.Vector3f(axis).mul(slide));
		staff.x += along.x;
		staff.y += along.y;
		staff.z += along.z;
		// the crown's chains hang plumb whatever the staff does, swinging off it
		Quaternionf plumb = new Quaternionf(pole).conjugate();
		for (int j = 0; j < staffChains.length; j++) {
			ModelPart chain = j == 0 ? staffChainLeft : j == 1 ? staffChainRight : staffChainCenter;
			Quaternionf swing = new Quaternionf().rotationZYX(a[STAFF_SWING_Z] * (1.0F + j * 0.15F) + (j == 1 ? -0.03F : 0.03F),
					0, a[STAFF_SWING_X] * (1.0F + j * 0.1F) + a[TURN_SWING]);
			setRotationFromMatrix(chain, new Quaternionf(plumb).mul(swing).get(new Matrix3f()));
			bend(staffChains[j], staffPendants[j], a[STAFF_BEND_X], a[STAFF_BEND_Z]);
		}
	}

	/** The chains that hang off his shoulders, his waist and the halo: plumb, swinging off what carries them. */
	private void hang(float[] a, float age, float swell) {
		float strain = Math.max(Math.abs(a[SHAKE]), swell * 0.5F);
		Quaternionf chestPlumb = rotation(hips, chest).conjugate();
		for (int i = 0; i < 4; i++) {
			ModelPart chain = i == 0 ? leftShoulderChain : i == 1 ? leftShoulderChainFront : i == 2 ? rightShoulderChain
					: rightShoulderChainFront;
			float side = i < 2 ? 1.0F : -1.0F;
			float idle = Mth.sin(age * 0.09F - i * 0.8F) * (0.025F + strain * 0.055F);
			Quaternionf swing = new Quaternionf().rotationZYX(a[SWING_Z] * (1.0F + i * 0.1F) + idle, 0,
					a[SWING_X] * (1.0F + i * 0.12F) + side * a[TURN_SWING]);
			setRotationFromMatrix(chain, new Quaternionf(chestPlumb).mul(swing).get(new Matrix3f()));
			bend(shoulderChains[i], shoulderPendants[i], a[BEND_X], a[BEND_Z]);
		}
		Quaternionf hipsPlumb = rotation(hips).conjugate();
		Quaternionf waistSwing = new Quaternionf().rotationZYX(a[ROBE_Z] * 0.8F + Mth.sin(age * 0.065F) * 0.03F, 0,
				a[ROBE_X] * 0.6F);
		setRotationFromMatrix(waistChain, new Quaternionf(hipsPlumb).mul(waistSwing).get(new Matrix3f()));
		bend(waistLinks[0], null, a[BEND_X] * 0.6F, a[BEND_Z] * 0.6F);
		if (!halo.visible) return;
		Quaternionf haloPlumb = rotation(hips, chest, halo).conjugate();
		for (int i = 0; i < 2; i++) {
			ModelPart chain = i == 0 ? haloChainLeft : haloChainRight;
			float side = i == 0 ? 1.0F : -1.0F;
			float idle = Mth.sin(age * 0.08F - i * 1.3F) * (0.02F + strain * 0.04F);
			Quaternionf swing = new Quaternionf().rotationZYX(a[HALO_SWING_Z] + idle, 0, a[HALO_SWING_X] + side * a[TURN_SWING]);
			setRotationFromMatrix(chain, new Quaternionf(haloPlumb).mul(swing).get(new Matrix3f()));
			bend(haloChains[i], haloPendants[i], a[HALO_SWING_X] * 0.5F, a[HALO_SWING_Z] * 0.5F);
		}
	}

	/**
	 * Curves a hanging chain: each link turns a little more than the one above it and hangs from the end of it, the
	 * pendant riding the last. Zero bend leaves the links as authored.
	 */
	private static void bend(ModelPart[] links, ModelPart pendant, float bendX, float bendZ) {
		float x = 0, y = 0, z = 0;
		int n = links.length;
		for (int i = 0; i < n; i++) {
			float share = (i + 1.0F) / n;
			float ax = bendX * share, az = bendZ * share;
			ModelPart link = links[i];
			link.x = x;
			link.y = y;
			link.z = z;
			if (i % 2 == 0) link.xRot += ax;
			link.zRot += az;
			x -= 1.5F * Mth.sin(az);
			y += 1.5F * Mth.cos(ax) * Mth.cos(az);
			z += 1.5F * Mth.sin(ax);
		}
		if (pendant == null) return;
		pendant.x = x;
		pendant.y = y;
		pendant.z = z;
		pendant.xRot += bendX;
		pendant.zRot += bendZ;
	}

	private static float acos(float cosine) {
		return (float) Math.acos(Mth.clamp(cosine, -1.0F, 1.0F));
	}

	/** A stage at {@code size}: hidden at 0; cords keep their length and thin out instead. */
	private static void scale(ModelPart part, float size, boolean thinOnly) {
		part.visible = size > .001F;
		if (thinOnly) {
			part.xScale = size;
			part.zScale = size;
			return;
		}
		part.xScale = size;
		part.yScale = size;
		part.zScale = size;
	}

	public org.joml.Vector3f core() {
		return point(new org.joml.Vector3f(0, -.18F, -.1F), root, hips, chest);
	}

	public org.joml.Vector3f claw() {
		return point(new org.joml.Vector3f(0, .12F, 0), root, hips, chest, rightArm, rightForearm, rightHand);
	}

	public org.joml.Vector3f staffHead() {
		return point(new org.joml.Vector3f(0, -.12F, 0), root, hips, chest, leftArm, leftForearm, leftHand, staff, staffCrown);
	}

	private static org.joml.Vector3f point(org.joml.Vector3f at, ModelPart... chain) {
		PoseStack poses = new PoseStack();
		for (ModelPart part : chain) part.translateAndRotate(poses);
		return poses.last().pose().transformPosition(at);
	}

	/** A point in the last part's units, carried out to model units in the space the chain's first part hangs in. */
	private static org.joml.Vector3f world(org.joml.Vector3f at, ModelPart... chain) {
		return point(at.div(16.0F), chain).mul(16.0F);
	}

	/** The rotation a chain of parts applies, root first. */
	private static Quaternionf rotation(ModelPart... chain) {
		Quaternionf rotation = new Quaternionf();
		for (ModelPart part : chain) rotation.rotateZYX(part.zRot, part.yRot, part.xRot);
		return rotation;
	}

	private static void setRotationFromMatrix(ModelPart part, Matrix3f rotation) {
		// Avoid the incorrect X denominator in JOML 1.10.5's quaternion ZYX extraction.
		part.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
		part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1.0F, 1.0F));
		part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
	}
