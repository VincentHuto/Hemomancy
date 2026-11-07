	// ---------------------------------------------------------------- animation (tools/model_export/velorum_anim.java)
	// A frame is built in layers, each a set of channels: the stance (his body at rest, blended continuously across the
	// three phases), the glide and the turn (read off his real motion), then the playing track as deltas over both
	// (Catmull-Rom keys, HemoEasing.catmullRom, on the move's own replayed clock). He barely moves, so his tracks are
	// restraint: long still holds, then one decisive motion with the whole body behind it (a coil, a fast arc through
	// the commit, follow-through past the line), then a slow weighted settle back to zero. VelorumAnimState blends a
	// changed track out of the last shown pose by inertialization (HemorathPoseBlend), and its springs then write the
	// overlap: the head held a beat behind the shoulders, the claw's fingers, the chains and lanterns, the mantle, the
	// robe, the stoles, the sleeves, the veil and the cord. pose() lays it all on the rig: the staff is one rigid pole
	// whose heel stays where it is planted while the grip slides on the shaft, and the trains and the robe heap on the
	// floor instead of passing through it. The crooked hand never falls to his side until his body fails.

	static final int HIP_X = 0, HIP_Y = 1, HIP_Z = 2, SINK = 3, BOW = 4, TWIST = 5, LEAN = 6, HEAD = 7, HEAD_TURN = 8,
			HEAD_ROLL = 9, LOOK = 10, R_ARM_X = 11, R_ARM_Y = 12, R_ARM_Z = 13, R_FORE = 14, R_FORE_Z = 15, R_HAND = 16,
			R_HAND_Z = 17, FINGERS = 18, CLENCH = 19, L_ARM_X = 20, L_ARM_Y = 21, L_ARM_Z = 22, L_FORE = 23,
			L_FORE_Z = 24, L_HAND = 25, STAFF_PLANT = 26, STAFF_X = 27, STAFF_Y = 28, STAFF_Z = 29, FOOT_X = 30,
			FOOT_Y = 31, FOOT_Z = 32, GRIP = 33, CORE = 34, SIDE_CORE = 35, SIDE_X = 36, SIDE_Z = 37, HALO_TURN = 38,
			HALO_TILT = 39, HALO_SLACK = 40, ICICLE_TILT = 41, ICICLE = 42, HOOD = 43, CALM = 44, TREMOR = 45,
			SHUDDER = 46, FLARE = 47, BREATH = 48,
			// the overlap, written by the springs after the blend
			LAG_HEAD_X = 49, LAG_HEAD_Y = 50, LAG_SPINE = 51, LAG_FORE = 52, LAG_FINGERS = 53, SIDE_SWING_X = 54,
			SIDE_SWING_Z = 55, SIDE_BEND_X = 56, SIDE_BEND_Z = 57, PENDANT_X = 58, PENDANT_Z = 59, PENDANT_BEND_X = 60,
			PENDANT_BEND_Z = 61, WAIST_X = 62, WAIST_Z = 63, HALO_SWING_X = 64, HALO_SWING_Z = 65, HALO_RING = 66,
			MANTLE_X = 67, MANTLE_Z = 68, ROBE_X = 69, ROBE_Z = 70, STOLE_X = 71, STOLE_Z = 72, SLEEVE_RX = 73,
			SLEEVE_RZ = 74, SLEEVE_LX = 75, SLEEVE_LZ = 76, VEIL_X = 77, CORD_X = 78, CORD_Z = 79,
			// how far below the floor he has sunk into the dark (the Shadow Step), model units: the floor the cloth heaps on
			// rises by it, and the renderer carries the body down by the same amount (stepUnder)
			UNDER = 80, CHANNELS = 81;
	/** The first channel the springs own. */
	static final int SECONDARY = LAG_HEAD_X;
	/** A spring's trail never turns a part further than this (a body snapped round by a teleport settles from it). */
	static final float TRAIL_LIMIT = 0.7F;
	/** The grip slides along the shaft at most this far either way while the heel is planted, model units. */
	static final float GRIP_SLIDE = 8.0F;
	/** The floor, in model units (y grows downward), and how far above it cloth comes to rest. */
	static final float FLOOR = 24.0F, CLOTH_CLEARANCE = 0.25F;
	/** How far the hanging lanterns hang below their chains' last links, model units. */
	static final float SIDE_GAP = 4.0F, PENDANT_GAP = 4.0F;
	/** The hour counted on his fingers: once every 200 ticks they fold one at a time and open together. */
	static final float COUNT_PERIOD = 200.0F;
	/** The crystals' slow turn, radians a tick. */
	static final float SPIN = 0.006F;
	/** The Shadow Step: how deep he sinks (blocks), enough that the lantern's cage goes under last. */
	public static final float STEP_DEPTH = 3.0F;
	/** The Shadow Step's authored length, which the phase-3 card replays over its own 50 ticks. */
	static final float STEP_LENGTH = 52.0F, CARD_LENGTH = 50.0F;
	/** A relit Eclipse's jolt runs this long over whatever plays next (the Guttering Lantern). */
	public static final float RELIT_LENGTH = 20.0F;
	private static final float DEG = Mth.DEG_TO_RAD;

	@Override
	public void setupAnim(T entity, float stride, float movement, float age, float yaw, float pitch) {
		float partial = AnimationTime.partialTick();
		VelorumAnimState motion = VelorumAnimState.of(entity);
		motion.update(entity, partial, age);
		float[] a = new float[CHANNELS];
		stance(a, motion.congregation(), motion.hour(), age, 1.0F - motion.busy());
		glide(a, motion.glide(), motion.turn(), motion.busy());
		track(a, motion.track(), motion.time(), motion.length(), motion.phase());
		// the phase-3 card: he sinks at the end of the Bow and rises on the ring (the Step's tracks, over the card)
		float card = motion.cardTime();
		if (card >= 0.0F) umbralStep(a, card * STEP_LENGTH / CARD_LENGTH);
		// the Eclipse relit: the claw flies off the cage, over the Guttering Lantern that takes him
		float relit = motion.relitTime();
		if (relit >= 0.0F) relit(a, relit);
		motion.blend(a, age);
		secondary(a, motion, age);
		float calm = HemoEasing.clamp01(a[CALM]);
		pose(a, age, yaw, pitch, motion.breath(), motion.spin(SPIN * Math.max(0.03F, calm)));
	}

	/**
	 * A frame posed without the blend or the springs, for the tests and the review: the stance for the frame's phase,
	 * then the frame's track (the ending over an opening over a move). The breath and the crystals run on {@code age}.
	 */
	public void pose(VelorumAnimState.Frame f, float age, float yaw, float pitch) {
		pose(channels(f, age), age, yaw, pitch, age * VelorumAnimState.BREATH_RATE, age * SPIN);
	}

	/** The channels of a frame without the blend or the springs. */
	static float[] channels(VelorumAnimState.Frame f, float age) {
		float[] a = new float[CHANNELS];
		int track = f.track();
		stance(a, f.phase() >= 2 ? 1.0F : 0.0F, f.phase() >= 3 ? 1.0F : 0.0F, age,
				track == VelorumAnimState.TRACK_IDLE ? 1.0F : 0.0F);
		track(a, track, f.trackTime(), f.openingLength(), f.phase());
		return a;
	}

	/** The playing track's deltas at its time, over the stance and the glide; {@code phase} counts the javelins. */
	static void track(float[] a, int track, float t, int length, int phase) {
		if (track == VelorumAnimState.TRACK_ENDING) failing(a, t);
		else if (track > VelorumAnimState.TRACK_OPENING) opening(a, track - VelorumAnimState.TRACK_OPENING, t, length);
		else if (track > VelorumAnimState.TRACK_MOVE) move(a, VelorumMove.byId(track - VelorumAnimState.TRACK_MOVE), t, phase);
	}

	// ---------------------------------------------------------------- the stance

	/**
	 * His body at rest, as continuous weights over the phases so nothing steps. The Hour: upright and rigid, the crooked
	 * hand raised by his face (the invocation is his constant), the staff planted at his side. The Congregation: the hand
	 * higher on the shaft so the lantern stands taller, the palm opening toward them, the hood turning slowly between the
	 * aisles and holding each look. The Endless Hour: the heel set a little ahead, the claw higher, the head lowered, the
	 * cloth stiller. The only business is the hour counted on his fingers, which fades under a move ({@code fidget}).
	 */
	static void stance(float[] a, float congregation, float hour, float age, float fidget) {
		a[STAFF_PLANT] = 1.0F;
		a[CORE] = 1.0F;
		a[SIDE_CORE] = 1.0F;
		a[ICICLE] = 1.0F;
		a[BREATH] = 1.0F;
		a[LOOK] = 1.0F;
		a[CALM] = 1.0F - 0.6F * hour;
		float keeping = congregation * (1.0F - hour);
		a[R_ARM_X] += -0.14F - 0.06F * keeping - 0.3F * hour;
		a[R_FORE_Z] += 0.12F * hour;
		a[FINGERS] -= 0.25F * keeping;
		a[L_ARM_Z] -= 0.12F * congregation;
		a[BOW] += 0.035F * hour;
		a[HEAD] += 0.07F * hour;
		a[FOOT_Z] -= 3.0F * hour;
		a[FOOT_X] -= 1.5F * hour;
		a[HEAD_TURN] += holds(age / 160.0F, 3.0F) * 0.32F * keeping * fidget;
		a[HEAD] += holds(age / 160.0F, 9.0F) * 0.04F * keeping * fidget;
		// the hour, counted: the fingers fold one at a time, hold, and open together
		float count = age % COUNT_PERIOD;
		float fold = count < 24.0F ? count / 6.0F : count < 30.0F ? 4.0F : Math.max(0.0F, 4.0F - (count - 30.0F) * 0.7F);
		a[CLENCH] += fold * fidget * (1.0F - 0.5F * hour);
	}

	/** Slow looks held between turns: a value in [-1, 1] that eases to a new hold once per unit of {@code t}. */
	static float holds(float t, float seed) {
		float k = (float) Math.floor(t);
		float u = HemoEasing.smoothstep(Math.min(1.0F, (t - k) * 2.5F));
		return HemoEasing.lerp(noise(k - 1.0F, seed), noise(k, seed), u);
	}

	private static float noise(float k, float seed) {
		float h = Mth.sin(k * 12.9898F + seed * 78.233F) * 43758.547F;
		return (h - (float) Math.floor(h)) * 2.0F - 1.0F;
	}

	// ---------------------------------------------------------------- the glide and the turn

	/**
	 * He never walks: he glides on the rime. A lean into the travel, the staff carried a hair off the floor and trailing,
	 * the claw held a little back; the cloth's trailing is the springs'. Turning, the hood leads, the shoulders follow and
	 * the hips lag. Under a move the upper body belongs to the move.
	 */
	static void glide(float[] a, float glide, float turn, float busy) {
		float free = 1.0F - busy * 0.7F;
		float moving = Math.max(glide, Math.min(1.0F, Math.abs(turn) * 1.5F));
		a[HIP_X] += 0.02F * glide;
		a[BOW] += 0.035F * glide;
		a[STAFF_PLANT] -= moving * free;
		a[GRIP] += 1.25F * moving * free;
		a[STAFF_X] += 0.07F * glide * free;
		a[R_ARM_X] += 0.05F * glide * free;
		a[HEAD_TURN] += 0.3F * turn;
		a[TWIST] += 0.1F * turn;
		a[HIP_Y] -= 0.06F * turn;
		a[LEAN] -= 0.03F * turn;
	}

	// ---------------------------------------------------------------- the moves

	/**
	 * Every move as deltas on its authored clock: each starts and ends at zero, so it rises out of the stance and settles
	 * back into it (the Long Night and the Eclipse hand their last pose to the Guttering Lantern). The commit is the end
	 * of the authored wind-up; a strike runs fastest through it.
	 */
	static void move(float[] a, VelorumMove move, float t, int phase) {
		if (move == null || t < 0.0F) return;
		switch (move) {
			case STAFF_TOLL -> staffToll(a, t);
			case LANTERN_SWEEP -> lanternSweep(a, t);
			case HANGING_LANTERN -> hangingLantern(a, t);
			case CRUOR_RELEASE -> cruorRelease(a, t);
			case STILL_FLOOR -> stillFloor(a, t);
			case BID_TO_KNEEL -> bidToKneel(a, t);
			case LONG_NIGHT -> longNight(a, t);
			case SET_IN_PLACE -> setInPlace(a, t);
			case RECALL -> recall(a, t);
			case CALL_TO_PRAYER -> callToPrayer(a, t);
			case PROCESSION -> procession(a, t);
			case SPLINTER_CROWN -> splinterCrown(a, t);
			case ENDLESS_HOUR -> endlessHour(a, t);
			case HOLLOW_TOLL -> hollowToll(a, t);
			case SILENCE_DRAW -> silenceDraw(a, t);
			case ARRESTED_PULSE -> arrestedPulse(a, t);
			case CRUOR_JAVELINS -> cruorJavelins(a, t, javelins(phase));
			case VEINS_OF_FROST -> veinsOfFrost(a, t);
			case UMBRAL_STEP -> umbralStep(a, t);
			case GLOAM_CUTS -> gloamCuts(a, t);
			case ECLIPSE -> eclipse(a, t);
		}
	}

	/** Cruor Javelins: three shafts, five in phase 3. */
	static int javelins(int phase) {
		return phase >= 3 ? 5 : 3;
	}

	/**
	 * 16 / 3 / 18. The hood lowers to the target first. The staff comes off the floor, the hand rises to the shoulder and
	 * the heel swings forward and up while the lantern goes back over his shoulder; he draws up and back, the left
	 * shoulder cocked, and holds. At 16 the heel is driven down onto the floor ahead in a tick and a half, the chest
	 * thrown after it; he carries on over the planted staff and a shudder runs through him, then draws himself upright,
	 * lifts the staff and sets it back at his side.
	 */
	static void staffToll(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 3, 16.9F, 17.6F) + HemoEasing.window(t, 25, 27, 29.5F, 31.5F);
		a[STAFF_X] += catmullRom(t, 0, 0, 5, -0.45F, 12, -1.15F, 16, -1.25F, 17.5F, -0.62F, 25, -0.6F, 28.5F, -0.2F, 31, 0);
		a[FOOT_Z] += catmullRom(t, 0, 0, 16, 0, 17, -15, 25, -15, 27, -15, 29.5F, 0);
		a[FOOT_X] += catmullRom(t, 0, 0, 16, 0, 17, -9, 25, -9, 27, -9, 29.5F, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 6, -0.35F, 13, -0.75F, 16, -0.8F, 17.5F, -0.55F, 20, -0.5F, 26, -0.4F, 31, -0.05F,
				37, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 6, -0.2F, 13, -0.45F, 16, -0.48F, 17.5F, 0.05F, 20, 0.1F, 26, 0.05F, 37, 0);
		a[L_FORE_Z] += catmullRom(t, 0, 0, 13, -0.3F, 16, -0.32F, 17.5F, 0.1F, 26, 0.05F, 37, 0);
		a[BOW] += catmullRom(t, 0, 0, 8, -0.06F, 14, -0.1F, 16, -0.11F, 17.5F, 0.19F, 19, 0.26F, 23, 0.15F, 30, 0.03F,
				37, 0);
		a[TWIST] += catmullRom(t, 0, 0, 8, -0.12F, 14, -0.2F, 16, -0.21F, 17.5F, 0.1F, 19.5F, 0.13F, 30, 0, 37, 0);
		a[HIP_Y] += catmullRom(t, 0, 0, 14, -0.06F, 17.5F, 0.03F, 20, 0.06F, 30, 0, 37, 0);
		a[HIP_X] += catmullRom(t, 0, 0, 16, -0.02F, 18.5F, 0.08F, 24, 0.04F, 37, 0);
		a[SINK] += catmullRom(t, 0, 0, 10, -0.3F, 16, -0.45F, 18, 0.9F, 24, 0.45F, 32, 0.05F, 37, 0);
		a[HEAD] += catmullRom(t, 0, 0, 5, 0.07F, 14, 0.09F, 16, 0.03F, 18.5F, 0.15F, 26, 0.05F, 37, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 14, 0.08F, 16, 0.09F, 18, -0.06F, 26, -0.02F, 37, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 12, 0.35F, 16, 0.4F, 18, -0.5F, 24, -0.2F, 32, 0, 37, 0);
		a[SHUDDER] += HemoEasing.pulse(t - 17.5F, 0.5F, 8.0F);
		a[FLARE] += HemoEasing.pulse(t - 17.5F, 1.0F, 9.0F) * 0.08F;
	}

	/**
	 * 14 / 6 / 16, the focal move. The staff comes up and is drawn back across his body, the hand sliding down the shaft
	 * so the lantern swings far out and low behind his left hip, its light brightening; he coils to his left and sinks
	 * into it and holds. At 14 he unwinds: one wide low sweep carries the lantern across his front from his left to his
	 * right in six ticks, the chest turning through the line and on past it, the hips following late. The lantern swings
	 * on to his right before he rights the staff and sets it down at his side.
	 */
	static void lanternSweep(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 3, 28.5F, 31);
		a[GRIP] += catmullRom(t, 0, 0, 10, 6, 21, 6.5F, 27, 2, 30, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 5, -1.1F, 11, -1.85F, 14, -1.9F, 16.5F, -1.72F, 19, -1.78F, 23, -1.6F, 26.5F,
				-0.75F, 29.5F, 0);
		a[STAFF_Y] += catmullRom(t, 0, 0, 5, 0.35F, 11, 0.7F, 14, 0.75F, 16.5F, 2.1F, 18.5F, 3.5F, 20, 4.2F, 23, 4.55F,
				28.5F, 4.6F, 29.5F, 4.6F, 31, 0);
		a[TWIST] += catmullRom(t, 0, 0, 6, -0.18F, 11, -0.36F, 14, -0.38F, 16.5F, 0.08F, 19, 0.45F, 21, 0.52F, 25, 0.3F,
				31, 0.04F, 33, -0.02F, 36, 0);
		a[HIP_Y] += catmullRom(t, 0, 0, 12, -0.1F, 16, -0.05F, 20, 0.17F, 25, 0.12F, 33, 0, 36, 0);
		a[LEAN] += catmullRom(t, 0, 0, 12, -0.05F, 18, 0.04F, 24, 0.02F, 31, 0, 36, 0);
		a[BOW] += catmullRom(t, 0, 0, 12, 0.06F, 17, 0.1F, 24, 0.04F, 33, 0, 36, 0);
		a[SINK] += catmullRom(t, 0, 0, 12, 0.5F, 17, 0.85F, 21, 0.6F, 27, 0.15F, 33, 0, 36, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 6, 0.2F, 11, 0.45F, 14, 0.48F, 16.5F, -0.35F, 19, -0.55F, 22, -0.45F, 27, -0.15F,
				31, 0, 36, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 11, 0.3F, 14, 0.32F, 17, 0.15F, 21, 0.05F, 30, 0, 36, 0);
		a[L_ARM_Y] += catmullRom(t, 0, 0, 14, 0, 16.5F, -0.25F, 19, -0.5F, 22, -0.55F, 27, -0.2F, 31, 0, 36, 0);
		a[HEAD] += catmullRom(t, 0, 0, 3, 0.05F, 14, 0.06F, 18, 0.02F, 28, 0.02F, 36, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 14, 0.08F, 17, -0.05F, 21, -0.12F, 28, -0.04F, 36, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 12, 0.22F, 14, 0.24F, 18, -0.12F, 22, -0.16F, 30, 0, 36, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 12, 0.2F, 15, -0.3F, 19, -0.5F, 26, -0.1F, 36, 0);
		a[CORE] += catmullRom(t, 0, 0, 12, 0.3F, 15, 0.35F, 20, 0.25F, 28, 0, 36, 0);
	}

	/**
	 * 14 / 4 / 16. He does not turn. His hips twist to his left and roll, drawing the side chain and its small lantern
	 * forward along his right hip while its light brightens, and he holds there. At 14 the hips snap the other way and
	 * the chain is flung back and up through the arc behind him; it swings on high behind him, then falls back and swings
	 * itself out (the springs carry the ring-out) while he straightens.
	 */
	static void hangingLantern(float[] a, float t) {
		a[SIDE_X] += catmullRom(t, 0, 0, 7, -0.35F, 12, -0.62F, 14, -0.66F, 16.5F, 1.25F, 18, 1.05F, 22, 0.3F, 27, 0, 34, 0);
		a[SIDE_Z] += catmullRom(t, 0, 0, 12, 0.18F, 14, 0.2F, 16.5F, -0.28F, 20, -0.15F, 26, 0, 34, 0);
		a[SIDE_CORE] += catmullRom(t, 0, 0, 12, 0.4F, 16, 0.45F, 22, 0.15F, 30, 0, 34, 0);
		a[HIP_Y] += catmullRom(t, 0, 0, 7, -0.1F, 12, -0.17F, 14, -0.18F, 16, 0.14F, 17.5F, 0.17F, 24, 0.05F, 34, 0);
		a[TWIST] += catmullRom(t, 0, 0, 12, -0.06F, 14, -0.07F, 16.5F, 0.08F, 24, 0.02F, 34, 0);
		a[HIP_Z] += catmullRom(t, 0, 0, 12, 0.07F, 14, 0.08F, 16.5F, -0.06F, 22, -0.03F, 34, 0);
		a[SINK] += catmullRom(t, 0, 0, 12, 0.35F, 16.5F, 0.55F, 22, 0.25F, 30, 0, 34, 0);
		a[HEAD_ROLL] += catmullRom(t, 0, 0, 10, 0.05F, 16, 0.03F, 28, 0, 34, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 12, -0.08F, 16.5F, 0.05F, 26, 0, 34, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 12, 0.3F, 16, -0.3F, 24, 0, 34, 0);
	}

	/**
	 * 24 / 12 / 22, the Release of Cruor. The claw rises to his face and the head tips down; the fingers fold shut one at
	 * a time on the gathered cold and the fist trembles; he draws up taller and the claw rises a last hand's breadth. At
	 * 24 it flings open and drops forward toward the floor ahead, the chest bowing after it, fingers splayed; it stays low
	 * and spread over the seams as the old blood wells and freezes, then rises slowly back to his face.
	 */
	static void cruorRelease(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.75F, 20, -0.82F, 23.5F, -1.05F, 25.5F, -0.55F, 28, -0.48F, 36, -0.55F,
				48, -0.2F, 58, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 8, -0.15F, 20, -0.15F, 23.5F, -0.12F, 25.5F, -0.85F, 28, -0.95F, 36, -0.85F,
				48, -0.25F, 58, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 8, 0.7F, 20, 0.75F, 23.5F, 0.8F, 25.5F, -1.1F, 28, -1.2F, 36, -1.05F, 48, -0.2F,
				58, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 20, -0.15F, 23.5F, -0.3F, 25.5F, 0.35F, 30, 0.25F, 40, 0.1F, 58, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 8, 0.3F, 20, 4.2F, 23.5F, 4.3F, 24.6F, 0, 58, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 23.5F, 0.15F, 25.5F, -1.0F, 36, -0.8F, 46, -0.2F, 58, 0);
		a[TREMOR] += HemoEasing.window(t, 8, 13, 22, 24.2F) * 0.012F;
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.1F, 22, 0.1F, 26, 0.2F, 36, 0.16F, 50, 0.03F, 58, 0);
		a[SINK] += catmullRom(t, 0, 0, 12, -0.35F, 20, -0.5F, 23.5F, -0.6F, 26, 0.7F, 30, 0.55F, 38, 0.35F, 52, 0.05F, 58, 0);
		a[BOW] += catmullRom(t, 0, 0, 14, -0.03F, 23.5F, -0.05F, 26, 0.15F, 28.5F, 0.17F, 36, 0.1F, 52, 0.01F, 58, 0);
		a[TWIST] += catmullRom(t, 0, 0, 16, -0.06F, 23.5F, -0.09F, 26, 0.12F, 30, 0.13F, 45, 0.02F, 58, 0);
		a[HIP_Y] += catmullRom(t, 0, 0, 23.5F, -0.03F, 27, 0.05F, 45, 0, 58, 0);
		a[CORE] += catmullRom(t, 0, 0, 20, -0.15F, 24, -0.2F, 27, 0.1F, 40, 0, 58, 0);
	}

	/**
	 * 30 / 3 / 30. The staff comes up off the floor and he draws up tall; then it is lowered with immense control for
	 * eighteen ticks, the heel driven home the last span in two. At 30 the crystals ring, a shudder runs through him and
	 * the cold lifts the mantle's hem; then he holds the planted staff perfectly still, even his cloth, until it lets go.
	 */
	static void stillFloor(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 4, 28, 30);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 10, -0.5F, 20, -0.35F, 28, -0.2F, 30, 0.04F, 34, 0.02F, 63, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, -0.2F, 28, -0.1F, 30, 0.02F, 63, 0);
		a[SINK] += catmullRom(t, 0, 0, 10, -0.6F, 27, -0.8F, 31, 0.5F, 34, 0.3F, 42, 0.12F, 60, 0.04F, 63, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.06F, 28, -0.07F, 31, 0.06F, 40, 0.03F, 63, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.03F, 28, -0.04F, 31, 0.035F, 36, 0.02F, 63, 0);
		a[SHUDDER] += HemoEasing.pulse(t - 30, 0.5F, 9.0F);
		a[CORE] += HemoEasing.pulse(t - 30, 1.0F, 10.0F) * 0.45F;
		a[FINGERS] += HemoEasing.pulse(t - 30, 1.0F, 12.0F) * -0.5F;
		a[FLARE] += HemoEasing.pulse(t - 30, 1.0F, 14.0F) * 0.14F;
		a[CALM] -= HemoEasing.window(t, 33, 40, 58, 63) * 0.85F;
	}

	/**
	 * 28 / 30 / 16, the lantern's gaze. The hood turns to the target; the staff comes off the floor and the lantern is
	 * lifted out toward them at head height, the hand low on the shaft, while the crooked hand rises palm out. At 28 the
	 * palm pushes once and the gaze locks: he leans in and holds utterly still, the palm pressing down a step every ten
	 * ticks, and at the end of the hold presses it down hard, the bid to kneel. Then he lowers it all and sets the staff
	 * down.
	 */
	static void bidToKneel(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 2, 8, 62, 68);
		a[GRIP] += catmullRom(t, 0, 0, 8, 2, 22, 5, 60, 5, 66, 1, 70, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 10, 0.25F, 22, 0.45F, 28, 0.48F, 29.5F, 0.55F, 58, 0.5F, 62, 0.4F, 67, 0.05F, 70, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, -0.5F, 22, -0.85F, 28, -0.88F, 29.5F, -0.95F, 58, -0.9F, 62, -0.7F, 68, -0.1F,
				74, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 22, -0.15F, 58, -0.15F, 66, 0, 74, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -0.5F, 24, -0.95F, 28, -0.97F, 29.5F, -1.12F, 31, -1.08F, 38, -1.04F,
				48, -0.98F, 57, -0.92F, 59.5F, -0.55F, 62, -0.5F, 74, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 14, -0.25F, 28, -0.3F, 58, -0.3F, 62, -0.15F, 74, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 24, -0.2F, 29.5F, -0.3F, 58, -0.25F, 60, 0.25F, 64, 0.15F, 74, 0)
				+ (HemoEasing.pulse(t - 32, 1.0F, 6.0F) + HemoEasing.pulse(t - 42, 1.0F, 6.0F)
				+ HemoEasing.pulse(t - 52, 1.0F, 6.0F)) * 0.14F;
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.6F, 28, -0.65F, 29.5F, -0.85F, 58, -0.75F, 60, -0.95F, 66, -0.3F, 74, 0);
		a[HEAD] += catmullRom(t, 0, 0, 6, 0.05F, 28, 0.03F, 30, 0.06F, 58, 0.06F, 60, 0.1F, 74, 0);
		a[BOW] += catmullRom(t, 0, 0, 24, 0.02F, 29.5F, 0.05F, 58, 0.05F, 60.5F, 0.09F, 66, 0.03F, 74, 0);
		a[SINK] += catmullRom(t, 0, 0, 24, -0.2F, 29.5F, 0.15F, 58, 0.15F, 60.5F, 0.45F, 68, 0.1F, 74, 0);
		a[CORE] += catmullRom(t, 0, 0, 24, 0.2F, 29.5F, 0.35F, 58, 0.3F, 64, 0, 74, 0);
		a[CALM] -= HemoEasing.window(t, 29, 33, 56, 60) * 0.6F;
	}

	/**
	 * 32 / 10 / 0, then the Guttering Lantern. The staff is lifted off the floor and raised high overhead, the hand
	 * sliding down the shaft so the light stands far above him; his head goes back and the crooked hand rises beside it,
	 * palm up, offering it upward. He rises a last inch, then at 32 brings it down in two ticks before his face, the claw
	 * cupping the light as it gutters, and hands that pose to the opening.
	 */
	static void longNight(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.ramp(t, 0, 4);
		a[GRIP] += catmullRom(t, 0, 0, 8, 4, 20, 8, 30, 8.5F, 32, 9.5F, 34, 1, 42, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 8, -0.7F, 20, -1.45F, 30, -1.5F, 32, -1.6F, 34, -0.45F, 42, GUTTER_ARM_X);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 20, -0.45F, 30, -0.48F, 32, -0.52F, 34, 0.25F, 42, GUTTER_ARM_Z);
		a[STAFF_X] += catmullRom(t, 0, 0, 20, 0.05F, 32, 0.05F, 34, 0.7F, 42, GUTTER_STAFF_X);
		a[HEAD] += catmullRom(t, 0, 0, 12, -0.1F, 20, -0.14F, 32, -0.16F, 34, 0.18F, 42, GUTTER_HEAD);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 32, 0, 34, -0.12F, 42, GUTTER_TURN);
		a[BOW] += catmullRom(t, 0, 0, 20, -0.05F, 32, -0.06F, 34, 0.08F, 42, GUTTER_BOW);
		a[SINK] += catmullRom(t, 0, 0, 20, -0.4F, 30, -0.5F, 32, -0.85F, 34.5F, 0.35F, 42, 0.15F);
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -0.7F, 20, -1.35F, 32, -1.4F, 34.5F, -0.55F, 42, -0.45F);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 20, -0.2F, 32, -0.2F, 34.5F, 0.55F, 42, 0.5F);
		a[R_HAND] += catmullRom(t, 0, 0, 20, -0.45F, 32, -0.45F, 34.5F, 0.1F, 42, 0.05F);
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.6F, 32, -0.65F, 34.5F, 0.55F, 42, 0.5F);
		a[CORE] += catmullRom(t, 0, 0, 20, 0.3F, 32, 0.35F, 35, -0.5F, 42, GUTTER_CORE);
		a[FLARE] += HemoEasing.pulse(t - 32, 1.5F, 10.0F) * 0.1F;
	}

	/** The Guttering Lantern's held pose, which the Long Night hands it. */
	static final float GUTTER_ARM_X = -0.35F, GUTTER_ARM_Z = 0.3F, GUTTER_STAFF_X = 0.8F, GUTTER_HEAD = 0.21F,
			GUTTER_TURN = -0.17F, GUTTER_BOW = 0.07F, GUTTER_CORE = -0.75F;

	/**
	 * 20 / 2 / 24. The hood looks down at the figure; the crooked hand rises high over it, the fingers spread, and he
	 * draws up tall; a hitch at the top. At 20 the claw is driven down onto it in two ticks with his weight behind it,
	 * closing into a fist on the blow; it carries on low past the figure while he hangs over it, then he rises slowly
	 * and the hand opens and returns to his face.
	 */
	static void setInPlace(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 6, 0.17F, 17, 0.12F, 21.5F, 0.3F, 24, 0.32F, 34, 0.1F, 46, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, 0.08F, 17, -0.05F, 19.5F, -0.06F, 21.5F, 0.26F, 23.5F, 0.3F, 32, 0.12F, 46, 0);
		a[HIP_X] += catmullRom(t, 0, 0, 19.5F, -0.02F, 22, 0.07F, 30, 0.03F, 46, 0);
		a[SINK] += catmullRom(t, 0, 0, 17, -0.5F, 19.5F, -0.55F, 22, 0.85F, 30, 0.4F, 46, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -1.3F, 17, -2.15F, 19.5F, -2.3F, 21.5F, -0.55F, 23.5F, -0.42F, 30, -0.5F,
				46, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 17, -0.1F, 19.5F, -0.08F, 21.5F, -0.85F, 23.5F, -0.95F, 32, -0.6F, 46, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 17, -0.45F, 19.5F, -0.5F, 21.5F, -1.1F, 23.5F, -1.2F, 32, -0.7F, 46, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 17, -0.3F, 19.5F, -0.35F, 21.5F, 0.4F, 26, 0.3F, 46, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 17, -0.8F, 20.5F, -0.85F, 21.5F, 0.5F, 32, 0.4F, 42, 0, 46, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 20.5F, 0, 21.5F, 4.3F, 30, 4.3F, 38, 0, 46, 0);
		a[TREMOR] += HemoEasing.window(t, 16, 18, 19, 20) * 0.015F;
		a[SHUDDER] += HemoEasing.pulse(t - 21.5F, 0.5F, 6.0F) * 0.6F;
	}

	/**
	 * 20 / 40 / 18. He lowers the lantern toward the floor and raises the crooked hand palm out at the devotee, the
	 * fingers beckoning; at 20 they catch the threads and snap half shut. Through the hold he draws the hand back toward
	 * his breast in four pulls, ten ticks apart, a finger folding at each; then lets it go.
	 */
	static void recall(float[] a, float t) {
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, -0.25F, 18, -0.32F, 60, -0.32F, 70, -0.05F, 78, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 10, 0.18F, 18, 0.25F, 60, 0.25F, 70, 0.05F, 78, 0);
		a[CORE] += catmullRom(t, 0, 0, 18, -0.2F, 60, -0.25F, 72, 0, 78, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.6F, 18, -0.9F, 20, -0.95F, 23, -0.82F, 30, -0.82F, 33, -0.68F, 40, -0.68F,
				43, -0.55F, 50, -0.55F, 53, -0.42F, 60, -0.42F, 70, -0.1F, 78, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 18, 0.2F, 23, 0.3F, 33, 0.4F, 43, 0.5F, 53, 0.6F, 60, 0.6F, 72, 0.1F, 78, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 14, -0.25F, 20, -0.3F, 60, -0.1F, 74, 0, 78, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 6, -0.6F, 10, -0.2F, 14, -0.65F, 17, -0.25F, 19, -0.5F, 20.5F, 0.2F, 60, 0.3F,
				68, -0.2F, 78, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 20, 0, 23, 1, 30, 1, 33, 2, 40, 2, 43, 3, 50, 3, 53, 4.2F, 60, 4.2F, 66, 0, 78, 0);
		a[BOW] += catmullRom(t, 0, 0, 18, 0.03F, 23, -0.01F, 33, -0.02F, 43, -0.03F, 53, -0.04F, 60, -0.04F, 72, 0, 78, 0);
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.05F, 20, 0.07F, 60, 0.04F, 78, 0);
		a[CALM] -= HemoEasing.window(t, 21, 26, 58, 62) * 0.4F;
	}

	/**
	 * 24 / 6 / 20. The hood turns to an aisle; the staff comes off the floor and the lantern is raised high and out toward
	 * it, the crooked hand lifting palm up beside it, as though lifting them. At 24 the light is lifted a hand's span
	 * higher in two ticks and brightens; then he lowers it slowly and sets the staff down.
	 */
	static void callToPrayer(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 2, 6, 40, 44);
		a[GRIP] += catmullRom(t, 0, 0, 20, 4, 23.5F, 4.2F, 25.5F, 7, 30, 6.5F, 42, 1, 46, 0);
		a[STAFF_Z] += catmullRom(t, 0, 0, 20, 0.35F, 25.5F, 0.38F, 32, 0.3F, 42, 0.05F, 46, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 20, -1.1F, 23.5F, -1.15F, 25.5F, -1.3F, 32, -1.2F, 44, -0.1F, 50, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 20, -0.5F, 25.5F, -0.6F, 32, -0.55F, 44, -0.05F, 50, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 8, -0.32F, 30, -0.35F, 44, -0.1F, 50, 0);
		a[HEAD] += catmullRom(t, 0, 0, 20, -0.08F, 25.5F, -0.12F, 32, -0.08F, 50, 0);
		a[TWIST] += catmullRom(t, 0, 0, 12, -0.1F, 30, -0.12F, 46, 0, 50, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 20, -0.55F, 25.5F, -0.7F, 32, -0.6F, 48, 0, 50, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 20, -0.4F, 25.5F, -0.5F, 40, -0.3F, 50, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.6F, 24, -0.6F, 25.5F, -1.0F, 34, -0.6F, 50, 0);
		a[SINK] += catmullRom(t, 0, 0, 20, -0.3F, 25.5F, -0.55F, 32, -0.3F, 50, 0);
		a[CORE] += catmullRom(t, 0, 0, 20, 0.15F, 25.5F, 0.45F, 34, 0.2F, 50, 0);
	}

	/**
	 * 26 / 44 / 14. The hood turns along the line and the lantern is lifted out to his side at head height; the crooked
	 * hand reaches out low before him, palm down, fingers trailing, leading them. At 26 the head ticks to the line's end;
	 * then he holds still, watching them come, until he lowers it all.
	 */
	static void procession(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 2, 7, 72, 77);
		a[GRIP] += catmullRom(t, 0, 0, 22, 3, 70, 3, 76, 0, 84, 0);
		a[STAFF_Z] += catmullRom(t, 0, 0, 22, 0.3F, 70, 0.3F, 76, 0.02F, 84, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 22, -0.7F, 70, -0.72F, 78, -0.1F, 84, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 22, -0.35F, 70, -0.35F, 78, -0.05F, 84, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 22, -0.45F, 70, -0.45F, 80, 0, 84, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 22, -0.45F, 70, -0.45F, 80, 0, 84, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 22, -0.35F, 70, -0.35F, 80, 0, 84, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 22, 0.35F, 70, 0.35F, 80, 0, 84, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 22, -0.4F, 70, -0.45F, 80, 0, 84, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, 0.04F, 26, 0.04F, 27.5F, 0.09F, 70, 0.06F, 84, 0);
		a[HEAD_TURN] += HemoEasing.pulse(t - 26, 1.0F, 6.0F) * 0.08F;
		a[TWIST] += catmullRom(t, 0, 0, 22, -0.06F, 70, -0.06F, 84, 0);
		a[CALM] -= HemoEasing.window(t, 28, 34, 68, 74) * 0.5F;
	}

	/**
	 * 26 / 4 / 20. He lifts his crown: the head tips back and the halo turns slowly, its hanging icicles leaning outward
	 * and shivering, while the crooked hand turns palm up before him over the stains. At 26 they snap free and the palm
	 * lifts sharply, closing as it rises, drawing the splinters up out of the blood on the floor; the halo rings on its
	 * chains while the icicles grow back, overshooting before they set.
	 */
	static void splinterCrown(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.09F, 25, -0.1F, 27.5F, 0.08F, 34, 0.05F, 50, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.5F, 18, -0.62F, 25.5F, -0.66F, 27.5F, -1.6F, 32, -1.5F, 42, -0.6F, 50,
				0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 8, -0.55F, 18, -0.75F, 25.5F, -0.78F, 27.5F, -0.35F, 32, -0.3F, 50, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 8, -0.7F, 18, -0.95F, 25.5F, -1, 27.5F, -0.6F, 32, -0.55F, 50, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 18, -0.45F, 25.5F, -0.5F, 27.5F, -0.75F, 40, -0.2F, 50, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 14, -0.7F, 25.5F, -0.75F, 27, 0.35F, 40, 0.1F, 50, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 25.5F, 0, 27, 4.3F, 36, 4.3F, 46, 0, 50, 0);
		a[HALO_TURN] += catmullRom(t, 0, 0, 10, 0.12F, 25, 0.35F, 27, 0.36F, 40, 0.3F, 50, 0);
		a[ICICLE_TILT] += catmullRom(t, 0, 0, 12, 0.15F, 18, 0.32F, 20, 0.27F, 22, 0.44F, 24, 0.4F, 25.5F, 0.62F, 26.5F,
				0.62F, 36, 0, 50, 0);
		a[ICICLE] += catmullRom(t, 0, 0, 25.8F, 0, 26.2F, -1, 36, -1, 40, -0.6F, 44, 0.18F, 46.5F, -0.05F, 49, 0);
		a[SINK] += catmullRom(t, 0, 0, 20, 0.2F, 25.5F, 0.3F, 28, -0.5F, 36, -0.15F, 50, 0);
		a[BOW] += catmullRom(t, 0, 0, 20, 0.02F, 25.5F, 0.03F, 28, -0.04F, 40, -0.01F, 50, 0);
	}

	/**
	 * 30 / 200 / 30. He sets the staff and the crooked hand rises high, palm out; the breath stops and everything on him
	 * comes to rest, the cloth and chains and crystals damped to nothing; a last small settling at 30. Then nothing moves
	 * at all for two hundred ticks: the one absolutely still pose in the mod, not even the head following. If the hold
	 * runs out he lowers the hand and breathes out.
	 */
	static void endlessHour(float[] a, float t) {
		float held = HemoEasing.window(t, 0, 30, 230, 260);
		a[CALM] -= held;
		a[BREATH] -= held;
		a[LOOK] -= HemoEasing.window(t, 18, 30, 230, 244);
		a[CORE] -= held * 0.5F;
		a[SIDE_CORE] -= held * 0.5F;
		a[R_ARM_X] += catmullRom(t, 0, 0, 14, -0.65F, 26, -0.92F, 30, -0.9F, 34, -0.9F, 226, -0.9F, 230, -0.9F, 248, -0.2F, 260, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 26, 0.12F, 34, 0.12F, 226, 0.12F, 230, 0.12F, 252, 0, 260, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 26, -0.25F, 34, -0.25F, 226, -0.25F, 230, -0.25F, 252, 0, 260, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.5F, 34, -0.5F, 226, -0.5F, 230, -0.5F, 250, 0, 260, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 12, -0.15F, 34, -0.15F, 226, -0.15F, 230, -0.15F, 250, 0, 260, 0);
		a[HEAD] += catmullRom(t, 0, 0, 24, -0.08F, 30, -0.07F, 34, -0.07F, 226, -0.07F, 230, -0.07F, 250, 0, 260, 0);
		a[SINK] += catmullRom(t, 0, 0, 24, -0.15F, 30, 0.25F, 34, 0.25F, 226, 0.25F, 230, 0.25F, 250, 0.05F, 260, 0);
		a[BOW] += catmullRom(t, 0, 0, 24, -0.02F, 30, 0.015F, 34, 0.015F, 226, 0.015F, 230, 0.015F, 233, 0.045F, 245, 0, 260, 0);
	}

	/**
	 * 24 / 20 / 20. He lifts the staff straight up off the floor and rises with it, the crooked hand rising too; a held
	 * breath at the top. At 24 it is driven straight down in a tick and his whole body drops with it, sinking into the
	 * blow and rebounding; a shudder; the cold lifts the hem. He holds the planted staff still while the ring runs out,
	 * then settles.
	 */
	static void hollowToll(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 3, 6, 23.5F, 24.5F);
		a[GRIP] += catmullRom(t, 0, 0, 20, 2, 23, 2.5F, 24.5F, -0.5F, 30, 0, 64, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 20, -0.35F, 23, -0.38F, 24.5F, 0.05F, 30, 0.02F, 64, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 20, -0.55F, 23, -0.6F, 24.5F, 0.1F, 30, 0.04F, 44, 0.02F, 64, 0);
		a[SINK] += catmullRom(t, 0, 0, 20, -0.8F, 23, -0.9F, 25.5F, 1.0F, 29, -0.2F, 33, 0.1F, 44, 0, 64, 0);
		a[BOW] += catmullRom(t, 0, 0, 20, -0.05F, 23, -0.05F, 25.5F, 0.1F, 30, 0.02F, 64, 0);
		a[HEAD] += catmullRom(t, 0, 0, 20, -0.07F, 23, -0.08F, 25.5F, 0.1F, 32, 0.02F, 64, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 20, -0.3F, 23, -0.32F, 25.5F, 0.02F, 40, 0, 64, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 20, 0.3F, 24.5F, -0.6F, 34, -0.2F, 50, 0, 64, 0);
		a[SHUDDER] += HemoEasing.pulse(t - 24.5F, 0.5F, 9.0F) * 1.2F;
		a[FLARE] += HemoEasing.pulse(t - 24.5F, 1.0F, 14.0F) * 0.15F;
		a[CORE] += HemoEasing.pulse(t - 24.5F, 1.0F, 10.0F) * 0.3F;
		a[CALM] -= HemoEasing.window(t, 30, 34, 42, 46) * 0.6F;
	}

	/**
	 * 24 / 20 / 18. The crooked hand rises and opens wide toward the room, the fingers bent back; then it turns palm up
	 * and closes slowly, a finger every three ticks. At 24 the fist closes hard and is drawn to his breast, his chest and
	 * head bowing round it; he holds it there, trembling with the draw, then opens it and lets it go.
	 */
	static void silenceDraw(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -0.6F, 22, -0.65F, 24, -0.68F, 26, -0.25F, 44, -0.25F, 56, -0.05F, 62, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 10, 0.2F, 22, 0.15F, 26, -0.3F, 44, -0.3F, 56, -0.05F, 62, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 10, -0.2F, 22, -0.1F, 26, 0.65F, 44, 0.65F, 56, 0.1F, 62, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 10, 0.2F, 18, -0.45F, 24, -0.5F, 26, 0.2F, 44, 0.2F, 62, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 10, -1.0F, 13, -0.9F, 24, -0.2F, 26, 0.45F, 44, 0.45F, 50, -0.3F, 62, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 12, 0, 22, 3.4F, 24, 4.0F, 25.5F, 4.3F, 44, 4.3F, 50, 0, 62, 0);
		a[TREMOR] += HemoEasing.window(t, 26, 28, 42, 45) * 0.01F;
		a[BOW] += catmullRom(t, 0, 0, 10, -0.03F, 24, -0.03F, 26.5F, 0.08F, 44, 0.07F, 58, 0, 62, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.05F, 24, -0.04F, 26.5F, 0.09F, 44, 0.08F, 58, 0, 62, 0);
		a[SINK] += catmullRom(t, 0, 0, 24, -0.2F, 26.5F, 0.3F, 44, 0.3F, 58, 0, 62, 0);
		a[CORE] += catmullRom(t, 0, 0, 24, 0, 27, -0.2F, 44, -0.2F, 56, 0, 62, 0);
	}

	/**
	 * 22 / 4 / 16. The hood turns to the marked; the crooked hand leaves his face and reaches out to arm's length at
	 * chest height, palm up, the fingers curling as round a heart; they close ever more slowly to a loose cage. At 21.5
	 * one hard clench, the forearm dropping and the chest dipping after it, held; then the hand opens and returns.
	 */
	static void arrestedPulse(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 6, 0.06F, 21.5F, 0.06F, 23, 0.09F, 26, 0.09F, 42, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 4, 0, 12, -0.72F, 18, -0.96F, 21.5F, -0.98F, 23, -0.8F, 26, -0.8F, 34, -0.35F,
				42, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 4, 0, 12, -0.7F, 18, -0.95F, 21.5F, -0.95F, 23, -0.9F, 26, -0.9F, 34, -0.4F,
				42, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 4, 0, 12, -0.9F, 18, -1.2F, 21.5F, -1.2F, 23, -1, 26, -1, 34, -0.45F, 42, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 12, -0.2F, 18, -0.25F, 23, -0.1F, 26, -0.1F, 42, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 4, -0.2F, 12, -0.05F, 18, 0.3F, 21.5F, 0.6F, 23, 0.75F, 26, 0.75F, 34, 0.1F,
				42, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 21.5F, 0, 23, 4.3F, 26, 4.3F, 34, 0, 42, 0);
		a[BOW] += catmullRom(t, 0, 0, 18, -0.02F, 21.5F, -0.02F, 23, 0.035F, 26, 0.035F, 36, 0, 42, 0);
		a[SINK] += catmullRom(t, 0, 0, 18, -0.2F, 21.5F, -0.2F, 23, 0.3F, 26, 0.25F, 36, 0, 42, 0);
		a[CALM] += HemoEasing.window(t, 18, 21, 26, 30) * -0.5F;
		a[SHUDDER] += HemoEasing.pulse(t - 23, 0.5F, 6) * 0.5F;
	}

	/**
	 * 24 / 20 / 18 (24 / 28 / 18 in phase 3, five shafts). The staff comes off the floor and is levelled, pitched
	 * forward with the cage aimed at the target, the hand sliding up the shaft. The claw passes slowly over the cage,
	 * palm down, three times, drawing the cruor up; then lifts a hand's breadth and tenses. From 24 each loose is a
	 * flick of the claw outward and down in a tick and a half, back in two and a half, the cage rocking back with it and
	 * the head following each shaft a beat late. When the last has flown the staff is set down and the hand returns.
	 */
	static void cruorJavelins(float[] a, float t, int shots) {
		float end = 32 + 4 * shots;
		float flick = 0.0F, nod = 0.0F;
		for (int i = 0; i < shots; i++) {
			flick += HemoEasing.pulse(t - 24 - 4 * i, 1.5F, 4.0F);
			nod += HemoEasing.pulse(t - 25 - 4 * i, 1.0F, 5.0F) * (i == 0 ? 0.0F : i % 2 == 1 ? 1.0F : -1.0F);
		}
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 4, end + 9, end + 15);
		a[GRIP] += catmullRom(t, 0, 0, 5, 0, 9, -2, end, -2, end + 8, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 3, 0.2F, 8, 1.05F, end, 1.05F, end + 6, 0.5F, end + 11, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 8, -0.366F, end, -0.366F, end + 11, 0);
		a[L_ARM_Y] += catmullRom(t, 0, 0, 8, 0.047F, end, 0.047F, end + 11, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 8, -0.09F, end, -0.09F, end + 11, 0);
		a[L_FORE_Z] += catmullRom(t, 0, 0, 8, -1.572F, end, -1.572F, end + 11, 0);
		a[TWIST] += catmullRom(t, 0, 0, 8, -0.12F, end, -0.12F, end + 14, 0, end + 18, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 4, -0.726F, 8, -1.816F, 22, -1.816F, 24, -1.95F, end, -1.95F, end + 10,
				-0.585F, end + 18, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 4, 0.202F, 8, 0.356F, 12.7F, 0.656F, 17.3F, 0.356F, 22, 0.506F, end, 0.506F,
				end + 10, 0.152F, end + 18, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 4, 0.059F, 8, 0.147F, 22, 0.147F, 24, 0.147F, end, 0.147F, end + 10, 0.044F,
				end + 18, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 4, -0.61F, 8, -1.525F, 22, -1.525F, 24, -1.525F, end, -1.525F, end + 10,
				-0.457F, end + 18, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 4, 0.1F, 8, 0.25F, 22, 0.25F, 24, 0.25F, end, 0.25F, end + 10, 0.075F,
				end + 18, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 8, 0.3F, 22, 0.3F, 24, 0.1F, end, 0.1F, end + 18, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, -0.6F, 22, -0.6F, 24, 0.3F, end, 0.3F, end + 10, 0, end + 18, 0);
		a[SINK] += catmullRom(t, 0, 0, 8, -0.2F, 22, -0.25F, 24, -0.4F, end, -0.3F, end + 18, 0);
		a[CORE] += catmullRom(t, 0, 0, 8, 0.15F, 22, 0.45F, 24, 0.3F, end, 0.1F, end + 18, 0);
		a[R_ARM_X] += flick * 0.443F;
		a[R_ARM_Y] += flick * -0.197F;
		a[R_ARM_Z] += flick * -0.231F;
		a[R_FORE_Z] += flick * -0.078F;
		a[R_FORE] += flick * 0.059F;
		a[FINGERS] -= flick * 1.2F;
		a[STAFF_X] -= flick * 0.052F;
		a[HEAD_TURN] += nod * 0.07F;
		a[HEAD] += Math.abs(nod) * 0.03F;
	}

	/**
	 * 30 / 6 / 22. The hood lowers to the floor; the crooked hand lowers to his waist, palm down and the fingers spread
	 * wide over the seams, the planted staff tilting toward the target. The fingers draw in slowly as the veins run, his
	 * body leaning in after them. At 28 the hand turns palm up and lifts sharply, pulling the spines up out of the
	 * floor; held raised, then it returns to his face.
	 */
	static void veinsOfFrost(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 6, 0.16F, 28, 0.17F, 30, 0.08F, 36, 0.08F, 58, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 2, -0.032F, 14, -0.319F, 28, -0.332F, 30, -0.785F, 36, -0.785F, 46, -0.314F,
				58, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 2, -0.063F, 14, -0.631F, 28, -0.656F, 30, -0.218F, 36, -0.218F, 46, -0.087F,
				58, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 2, -0.045F, 14, -0.45F, 28, -0.468F, 30, -0.297F, 36, -0.297F, 46, -0.119F,
				58, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 2, -0.097F, 14, -0.966F, 28, -1.005F, 30, -0.353F, 36, -0.353F, 46, -0.141F,
				58, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 2, -0.007F, 14, -0.069F, 28, -0.072F, 30, -0.397F, 36, -0.397F, 46, -0.159F,
				58, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 14, 0.35F, 28, 0.38F, 30, -0.6F, 36, -0.6F, 48, -0.15F, 58, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 2, -0.3F, 14, -1, 28, -0.25F, 30, 0.35F, 36, 0.35F, 48, 0, 58, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 14, -0.12F, 28, -0.16F, 36, -0.12F, 50, 0, 58, 0);
		a[BOW] += catmullRom(t, 0, 0, 14, 0.03F, 28, 0.08F, 30, -0.02F, 36, -0.01F, 58, 0);
		a[SINK] += catmullRom(t, 0, 0, 14, 0.3F, 28, 0.45F, 30, -0.45F, 36, -0.35F, 50, -0.05F, 58, 0);
		a[CORE] += catmullRom(t, 0, 0, 14, -0.1F, 28, -0.15F, 30, 0.2F, 40, 0, 58, 0);
		a[CALM] += HemoEasing.window(t, 16, 20, 27, 30) * -0.4F;
		a[SHUDDER] += HemoEasing.pulse(t - 30, 0.5F, 6) * 0.4F;
	}

	/**
	 * 24 / 10 / 18, the Shadow Step. The hood bows; he sinks straight down, slowly then faster, upright, the crooked
	 * hand still raised and the staff lifted plumb so the lantern goes under last (the depth is {@link #stepUnder}, laid
	 * on the floor the cloth heaps on and on the renderer's translation). At the commit he is moved. He rises the hood
	 * first, the shoulders by 4 and the hem by 10, the claw raised as it comes up; then the cloth falls in a slow
	 * straightening and the side lantern swings once on its spring.
	 */
	static void umbralStep(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 6, 0.18F, 22, 0.18F, 24, 0.12F, 27, 0.04F, 34, 0.02F, 52, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, 0.06F, 10, 0.03F, 24, 0.02F, 34, 0.01F, 52, 0);
		a[STAFF_PLANT] -= HemoEasing.window(t, 4, 8, 34, 40);
		a[GRIP] += catmullRom(t, 0, 0, 8, 1, 34, 1, 40, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.1F, 24, -0.15F, 26, -0.28F, 34, -0.12F, 52, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, -0.3F, 34, -0.3F, 52, 0);
		a[SIDE_X] += catmullRom(t, 0, 0, 32, 0, 35, 0.5F, 39, -0.25F, 45, 0, 52, 0);
		a[CORE] += catmullRom(t, 0, 0, 8, -0.2F, 24, -0.5F, 30, 0.2F, 40, 0, 52, 0);
		a[FLARE] += catmullRom(t, 0, 0, 24, 0, 27, 0.12F, 34, 0.05F, 52, 0);
		a[UNDER] += stepUnder(t) * 16.0F;
	}

	/**
	 * 22 / 26 / 16. The crooked hand turns palm out, the fingers spread, and draws back to his left shoulder, coiled,
	 * the staff planted; a held breath. Cut 1 at the commit: an underhand slash down and across to his right knee, the
	 * chest bowing after it. Cut 2 at +6: the low backhand return across to his left. Cut 3 at +12: the claw rises over
	 * his head and slashes flat across at face height from his right to his left, the chest rising and the head going
	 * back. The arm is held across his chest while the cuts fly, then the hand returns to his face. The world geometry
	 * follows the hand through {@code VelorumAnimState.clawAt}.
	 */
	static void gloamCuts(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.337F, 20, -1.347F, 22, -1.347F, 24, -1.559F, 27, -1.606F, 28, -1.559F,
				30, -1.959F, 31, -1.959F, 32.5F, -2.653F, 34, -2.181F, 35.5F, -1.687F, 37, -1.718F, 48, -1.718F, 64,
				0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 8, -0.015F, 20, -0.059F, 22, -0.059F, 24, -0.99F, 27, -1.02F, 28, -0.99F, 30,
				-0.831F, 31, -0.831F, 32.5F, -0.584F, 34, -0.193F, 35.5F, -0.921F, 37, -1.471F, 48, -1.471F, 64, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 8, 0.197F, 20, 0.788F, 22, 0.788F, 24, 1.025F, 27, 1.056F, 28, 1.025F, 30,
				0.859F, 31, 0.859F, 32.5F, -0.585F, 34, -1.319F, 35.5F, -0.903F, 37, -0.894F, 48, -0.894F, 64, 0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 8, 0.005F, 20, 0.019F, 22, 0.019F, 24, -1.372F, 27, -1.413F, 28, -1.372F, 30,
				-1.566F, 31, -1.566F, 32.5F, -1.247F, 34, -0.631F, 35.5F, -1.4F, 37, -0.178F, 48, -0.178F, 64, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 8, -0.209F, 20, -0.838F, 22, -0.838F, 24, 0.021F, 27, 0.022F, 28, 0.021F, 30,
				0.224F, 31, 0.224F, 32.5F, 0.105F, 34, 0.364F, 35.5F, 0.408F, 37, 0.664F, 48, 0.664F, 64, 0);
		a[R_HAND] += catmullRom(t, 0, 0, 8, -0.3F, 22, -0.3F, 24, 0.3F, 30, 0.2F, 34, 0, 48, 0, 64, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, -0.9F, 48, -0.9F, 58, -0.2F, 64, 0);
		a[TWIST] += catmullRom(t, 0, 0, 8, -0.05F, 20, -0.2F, 22, -0.21F, 24, 0.25F, 28, 0.22F, 30, -0.15F, 32.5F,
				-0.05F, 34, 0.1F, 36, -0.15F, 48, -0.12F, 64, 0);
		a[BOW] += catmullRom(t, 0, 0, 20, -0.02F, 22, -0.02F, 24, 0.1F, 28, 0.09F, 30, 0.1F, 32.5F, -0.04F, 34, -0.07F,
				36, -0.07F, 48, -0.03F, 64, 0);
		a[HEAD] += catmullRom(t, 0, 0, 20, 0.04F, 24, 0.1F, 30, 0.1F, 34, -0.1F, 37, -0.14F, 48, -0.05F, 64, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 14, 0.1F, 22, 0.12F, 24, 0.02F, 64, 0);
		a[SINK] += catmullRom(t, 0, 0, 20, -0.4F, 22, -0.45F, 24, 0.6F, 30, 0.6F, 32.5F, -0.3F, 36, 0.1F, 48, 0.1F, 64,
				0);
		a[CORE] += catmullRom(t, 0, 0, 22, -0.1F, 48, -0.1F, 64, 0);
	}

	/**
	 * 40 / 160 / 30, the Eclipse. The staff comes off the floor and the lantern is lifted before his face at arm's
	 * length, the head bowing to it; the crooked hand comes over the cage and closes a finger every five ticks as the
	 * light dies; held; a last tight clench, and the light is out. Then he is utterly still, the dark lantern held in the
	 * closed claw before his face, but for one motion: at each reach (80, 130, 180) the head turns toward the reaching
	 * shapes and back. Unbroken, the fingers open slowly and the lantern is lowered into the Guttering Lantern's pose,
	 * which the Gutter picks up (relit, {@link #relit} jolts him out of the hold instead).
	 */
	static void eclipse(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 4, 300, 301);
		a[GRIP] += catmullRom(t, 0, 0, 6, 1.5F, 16, 1.5F, 194, 1.5F, 200, 1.5F, 230, 0);
		a[L_ARM_X] += catmullRom(t, 0, 0, 10, -0.325F, 16, -0.325F, 194, -0.325F, 200, -0.325F, 212, -0.325F, 230,
				GUTTER_ARM_X);
		a[L_ARM_Y] += catmullRom(t, 0, 0, 10, 0.637F, 16, 0.637F, 194, 0.637F, 200, 0.637F, 212, 0.637F, 230, 0);
		a[L_ARM_Z] += catmullRom(t, 0, 0, 10, 0.334F, 16, 0.334F, 194, 0.334F, 200, 0.334F, 212, 0.334F, 230,
				GUTTER_ARM_Z);
		a[L_FORE_Z] += catmullRom(t, 0, 0, 10, 0.15F, 16, 0.15F, 194, 0.15F, 200, 0.15F, 212, 0.15F, 230, 0);
		a[STAFF_X] += catmullRom(t, 0, 0, 10, 0.312F, 16, 0.312F, 194, 0.312F, 200, 0.312F, 212, 0.312F, 230,
				GUTTER_STAFF_X);
		a[STAFF_Z] += catmullRom(t, 0, 0, 10, -0.509F, 16, -0.509F, 194, -0.509F, 200, -0.509F, 212, -0.509F, 230, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.421F, 14, -1.403F, 20, -1.403F, 194, -1.403F, 200, -1.403F, 212,
				-1.193F, 230, -0.45F);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 8, 0.063F, 14, 0.209F, 20, 0.209F, 194, 0.209F, 200, 0.209F, 212, 0.178F, 230,
				0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 8, 0.086F, 14, 0.287F, 20, 0.287F, 194, 0.287F, 200, 0.287F, 212, 0.244F, 230,
				0);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 8, -0.39F, 14, -1.3F, 20, -1.3F, 194, -1.3F, 200, -1.3F, 212, -1.105F, 230,
				0.5F);
		a[R_FORE] += catmullRom(t, 0, 0, 8, 0.024F, 14, 0.081F, 20, 0.081F, 194, 0.081F, 200, 0.081F, 212, 0.069F, 230,
				0);
		a[R_HAND] += catmullRom(t, 0, 0, 14, 0.25F, 20, 0.25F, 194, 0.25F, 200, 0.25F, 230, 0);
		a[CLENCH] += catmullRom(t, 0, 0, 10, 0, 15, 1, 20, 2, 25, 3, 30, 4, 36, 4, 194, 4, 200, 4, 212, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 10, -0.4F, 30, 0.15F, 38, 0.2F, 40, 0.6F, 46, 0.6F, 194, 0.6F, 200, 0.6F, 212,
				0.2F, 230, 0.5F);
		a[HEAD] += catmullRom(t, 0, 0, 10, 0.12F, 16, 0.12F, 194, 0.12F, 200, 0.12F, 212, 0.14F, 230, GUTTER_HEAD);
		a[BOW] += catmullRom(t, 0, 0, 10, 0.03F, 16, 0.03F, 194, 0.03F, 200, 0.03F, 230, GUTTER_BOW);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 200, 0, 212, 0, 230, GUTTER_TURN);
		a[HEAD_TURN] += HemoEasing.pulse(t - 80, 4, 14) * 0.17F;
		a[HEAD_TURN] += HemoEasing.pulse(t - 130, 4, 14) * -0.17F;
		a[HEAD_TURN] += HemoEasing.pulse(t - 180, 4, 14) * 0.17F;
		a[CORE] += catmullRom(t, 0, 0, 10, 0.1F, 30, -0.6F, 38, -0.85F, 40, -1, 46, -1, 194, -1, 200, -1, 212, -0.9F,
				230, GUTTER_CORE);
		a[SIDE_CORE] += catmullRom(t, 0, 0, 30, -0.6F, 40, -1, 46, -1, 194, -1, 200, -1, 230, -0.6F);
		a[SINK] += catmullRom(t, 0, 0, 10, -0.3F, 40, -0.35F, 46, -0.35F, 194, -0.35F, 200, -0.35F, 212, -0.1F, 230,
				0.15F);
		a[CALM] -= HemoEasing.window(t, 30, 40, 200, 212);
		a[LOOK] -= HemoEasing.window(t, 30, 40, 300, 301);
		a[LOOK] += catmullRom(t, 0, 0, 194, 0, 200, 0, 230, 0.3F);
		a[BREATH] -= HemoEasing.window(t, 34, 40, 200, 206);
		a[BREATH] += catmullRom(t, 0, 0, 200, 0, 206, 0, 230, 1.2F);
		a[SHUDDER] += HemoEasing.pulse(t - 40, 0.5F, 6) * 0.4F;
	}

	/**
	 * How far under the floor the Shadow Step has him at {@code t} (blocks): nothing until 8, then straight down,
	 * slowly then faster, to {@link #STEP_DEPTH} at the commit (24), where he is moved; then up again, the hood first
	 * (out by 25), the shoulders by 28 and the hem by 34.
	 */
	public static float stepUnder(float t) {
		if (t < 8.0F || t >= 34.0F) return 0.0F;
		if (t < 24.0F) {
			float s = (t - 8.0F) / 16.0F;
			return STEP_DEPTH * s * s;
		}
		return Math.max(0.0F, catmullRom(t, 24, STEP_DEPTH, 25, 2.1F, 28, 1.2F, 31, 0.45F, 34, 0));
	}

	/** The phase-3 card's sink and rise (blocks under the floor): the Step's, over the card's 50 ticks. */
	public static float cardUnder(float cardTime) {
		return cardTime < 0.0F ? 0.0F : stepUnder(cardTime * STEP_LENGTH / CARD_LENGTH);
	}

	/**
	 * The Eclipse relit, laid over whatever plays next: in a tick and a half the claw flies open off the cage, the head
	 * snaps back, the staff is knocked back and the light bursts through his fingers; it fades as the Gutter takes him.
	 */
	static void relit(float[] a, float r) {
		if (r < 0.0F || r >= RELIT_LENGTH) return;
		float jolt = HemoEasing.pulse(r, 1.5F, RELIT_LENGTH);
		a[FINGERS] -= 1.1F * jolt;
		a[CLENCH] -= 4.3F * HemoEasing.pulse(r, 0.5F, 8.0F);
		a[R_ARM_X] += 0.25F * jolt;
		a[R_FORE_Z] += 0.2F * jolt;
		a[HEAD] -= 0.14F * jolt;
		a[STAFF_X] -= 0.07F * jolt;
		a[CORE] += 0.9F * HemoEasing.pulse(r, 0.5F, 12.0F);
		a[SHUDDER] += HemoEasing.pulse(r, 0.5F, 8.0F);
		a[FLARE] += 0.1F * jolt;
	}

	// ---------------------------------------------------------------- the openings and the ending

	/** The three openings: the Gutter's lean toward the shrunken light, the Overheld's jolt, the Bow beneath a wound. */
	static void opening(float[] a, int id, float t, int length) {
		if (t < 0.0F) return;
		float len = Math.max(id == 3 ? 36.0F : id == 2 ? 24.0F : 12.0F, length);
		switch (id) {
			case 1 -> gutter(a, t, len);
			case 2 -> overheld(a, t, len);
			case 3 -> bow(a, t, len);
			default -> {
			}
		}
	}

	/**
	 * The Guttering Lantern. The staff lowered to his hip with the lantern held before him, the light shrinking to a point;
	 * his head leans and turns toward it; the crooked hand stays raised. Over the window he drifts lower and his breath
	 * shows; in its last ten ticks he straightens.
	 */
	static void gutter(float[] a, float t, float len) {
		float drift = HemoEasing.ramp(t, 0, len);
		float shown = 1.0F - HemoEasing.ramp(t, len - 10.0F, len);
		a[STAFF_PLANT] -= shown;
		a[L_ARM_X] += (GUTTER_ARM_X - 0.05F * drift) * shown;
		a[L_ARM_Z] += (GUTTER_ARM_Z + 0.1F * drift) * shown;
		a[STAFF_X] += GUTTER_STAFF_X * shown;
		a[HEAD] += (GUTTER_HEAD + 0.06F * drift) * shown;
		a[HEAD_TURN] += GUTTER_TURN * shown;
		a[BOW] += (GUTTER_BOW + 0.04F * drift) * shown;
		a[SINK] += (0.15F + 0.35F * drift) * shown;
		a[CORE] += GUTTER_CORE * (1.0F - HemoEasing.ramp(t, len - 6.0F, len));
		a[R_ARM_X] -= 0.45F * shown;
		a[R_FORE_Z] += 0.5F * shown;
		a[FINGERS] += 0.5F * shown;
		a[BREATH] += 1.2F * shown;
		a[LOOK] -= 0.7F * shown;
	}

	/**
	 * The Overheld Invocation. A jolt in a tick and a half: the head snaps up, the hood falls back, the raised arm jerks
	 * back and the fingers fling open; the hand shakes, fading over eight ticks; then a stiff, wary hold, the claw drawn in
	 * before his chest, breathing hard, the hood slipping forward again.
	 */
	static void overheld(float[] a, float t, float len) {
		a[HEAD] += catmullRom(t, 0, 0, 1.5F, -0.2F, 6, -0.16F, 20, -0.08F, len, -0.05F);
		a[HOOD] += catmullRom(t, 0, 0, 1.5F, 0.3F, 8, 0.26F, len * 0.8F, 0.1F, len, 0.08F);
		a[R_ARM_X] += catmullRom(t, 0, 0, 1.5F, 0.35F, 5, 0.3F, 12, -0.15F, len, -0.12F);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 1.5F, -0.3F, 8, -0.2F, 14, 0.35F, len, 0.3F);
		a[FINGERS] += catmullRom(t, 0, 0, 1.5F, -0.9F, 6, -0.7F, 14, 0.3F, len, 0.25F);
		a[BOW] += catmullRom(t, 0, 0, 1.5F, -0.07F, 6, -0.05F, 14, 0.02F, len, 0.02F);
		a[SINK] += catmullRom(t, 0, 0, 1.5F, -0.4F, 8, 0.1F, len, 0.15F);
		a[TREMOR] += 0.07F * (1.0F - HemoEasing.ramp(t, 0, 8)) * HemoEasing.ramp(t, 0, 1);
		a[SHUDDER] += HemoEasing.pulse(t, 0.5F, 8.0F) * 0.8F;
		a[BREATH] += 1.0F * HemoEasing.ramp(t, 4, 12);
		a[CORE] += HemoEasing.pulse(t, 0.5F, 6.0F) * -0.3F;
	}

	/**
	 * The Bow, beneath a wound, without losing the invocation. A hit-stop shudder, then he bows: hips, chest and hood
	 * dipping to his breast with weight, sagging a little past and sinking so the robe heaps, the staff, its heel still where
	 * it stood, leaning in onto his shoulder; the crooked hand stays raised. Held, breathing heavily. In the last
	 * fourteen ticks the chest rises first and the head last.
	 */
	static void bow(float[] a, float t, float len) {
		float rise = len - 14.0F;
		float down = catmullRom(t, 0, 0, 3, -0.05F, 10, 0.85F, 13, 1.06F, 18, 1.0F, rise, 1.0F, len - 4.0F, 0.15F, len, 0);
		float head = catmullRom(t, 0, 0, 4, -0.1F, 12, 0.8F, 15, 1.05F, 20, 1.0F, rise + 3.0F, 1.0F, len, 0);
		a[HIP_X] += 0.24F * down;
		a[BOW] += 0.31F * down;
		a[HEAD] += 0.35F * head;
		a[SINK] += 1.6F * down;
		a[L_ARM_X] -= 0.2F * down;
		a[L_ARM_Z] -= 0.35F * down;
		a[L_FORE_Z] -= 0.45F * down;
		a[R_ARM_X] -= 0.15F * down;
		a[FINGERS] += 0.3F * down;
		a[HALO_TILT] += 0.12F * head;
		a[SHUDDER] += HemoEasing.pulse(t, 0.5F, 6.0F) * 0.9F;
		a[BREATH] += 2.0F * HemoEasing.window(t, 8, 14, rise, len);
		a[LOOK] -= HemoEasing.window(t, 2, 10, rise, len);
	}

	/**
	 * His body fails (200 ticks, ending rules). A hit-stop; then the crooked hand lowers for the first time in the fight,
	 * slowly at first and then falling, unbending as it goes, bouncing once at his side. The hood bows to the chest and
	 * he settles lower, the hand sliding down the planted staff as it leans away, the halo sagging on its slackening
	 * chains; everything on him comes to rest, and he breathes out once more.
	 */
	static void failing(float[] a, float t) {
		float hood = HemoEasing.ramp(t, 16, 40);
		a[R_ARM_X] += catmullRom(t, 0, 0, 3, -0.04F, 9, 0.1F, 14, 0.3F, 16, 0.38F, 20, 0.31F, 26, 0.33F);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 3, 0.03F, 10, -0.35F, 15, -0.85F, 16.5F, -0.95F, 20, -0.88F, 26, -0.9F);
		a[R_FORE_Z] += catmullRom(t, 0, 0, 4, 0, 12, -0.7F, 16, -1.3F, 20, -1.2F, 26, -1.25F);
		a[R_HAND] += catmullRom(t, 0, 0, 8, 0, 16, 0.45F, 26, 0.4F);
		a[FINGERS] += catmullRom(t, 0, 0, 6, 0, 16, -0.7F, 30, -0.55F);
		a[HEAD] += 0.6F * hood;
		a[BOW] += catmullRom(t, 0, 0, 16, 0.02F, 40, 0.26F, 64, 0.26F, 72, 0.3F);
		a[HIP_X] += 0.1F * hood;
		a[SINK] += catmullRom(t, 0, 0, 16, 0.1F, 40, 1.8F, 64, 1.8F, 72, 2.1F);
		a[L_ARM_Z] += 0.25F * hood;
		a[HALO_SLACK] += hood;
		a[HALO_TILT] += 0.2F * hood;
		a[LOOK] -= HemoEasing.ramp(t, 0, 16);
		a[CALM] -= HemoEasing.ramp(t, 30, 64);
		a[SHUDDER] += HemoEasing.pulse(t, 0.4F, 5.0F) * 0.9F;
	}

	// ---------------------------------------------------------------- the overlap

	/**
	 * The springs, after the blend, so a changed track's overlap carries on from what was shown. He is the still thing,
	 * so they are tight and well damped, and every trail is scaled by his calm: the Endless Hour damps them to nothing.
	 * The head is held a beat behind the shoulders; the claw's forearm and fingers trail the arm; the side lantern is a
	 * pendulum off his chest, the pendant lantern off the staff, the halo's chains off the halo; the mantle and robe trail
	 * his glide and settle when he stops, and drag round after a turn; the stoles, sleeves, veil and cord swing off what
	 * carries them. Under the Repose deed they never come to rest.
	 */
	static void secondary(float[] a, VelorumAnimState m, float age) {
		float calm = HemoEasing.clamp01(a[CALM]);
		float drift = 0.0F;
		if (m.repose()) {
			calm = Math.max(calm, 0.45F);
			drift = Mth.sin(age * 0.07F) * 0.05F + Mth.sin(age * 0.113F + 1.3F) * 0.03F;
		}
		a[CALM] = Math.max(a[CALM], calm);
		float spine = a[BOW] + a[HIP_X];
		float twist = a[TWIST] + a[HIP_Y];
		float lean = a[LEAN] + a[HIP_Z];
		float glide = m.glide();
		float turn = m.turn();
		a[LAG_HEAD_X] = trail(m.lag(0, spine + a[SINK] * 0.02F, 0.5F, 0.75F) * 0.45F) * calm;
		a[LAG_HEAD_Y] = trail(m.lag(1, twist, 0.45F, 0.8F) * 0.5F) * calm;
		a[LAG_SPINE] = trail(-m.lag(2, a[SINK] * 0.04F, 0.5F, 0.55F)) * calm;
		a[LAG_FORE] = trail(m.lag(3, a[R_ARM_X] + a[R_ARM_Z] * 0.5F, 0.6F, 0.5F) * 0.45F) * calm;
		a[LAG_FINGERS] = trail(m.lag(4, a[R_ARM_X] + a[R_FORE] + a[R_FORE_Z], 0.8F, 0.45F) * 0.35F) * calm;
		// the side lantern: a pendulum following the track's own swing, kicked by the chest and dragged by the glide
		float side = m.follow(5, a[SIDE_X] + glide * 0.22F + drift, 0.3F, 0.16F) - a[SIDE_X];
		a[SIDE_SWING_X] = trail(side - m.lag(6, spine * 0.8F + a[SINK] * 0.03F, 0.32F, 0.2F) * 0.9F) * calm;
		a[SIDE_SWING_Z] = trail(m.follow(7, a[SIDE_Z] - turn * 0.18F + drift * 0.5F, 0.3F, 0.18F) - a[SIDE_Z]
				- m.lag(8, lean + twist * 0.4F, 0.3F, 0.2F) * 0.8F) * calm;
		a[SIDE_BEND_X] = trail(m.lag(9, a[SIDE_X] + a[SIDE_SWING_X], 0.55F, 0.3F) * 0.8F) * calm;
		a[SIDE_BEND_Z] = trail(m.lag(10, a[SIDE_Z] + a[SIDE_SWING_Z], 0.55F, 0.3F) * 0.8F) * calm;
		// the pendant lantern swings off the staff: off its tilt and its turn, the hand that carries it, the glide
		float staffX = a[STAFF_X] * (1.0F - HemoEasing.clamp01(a[STAFF_PLANT])) + a[L_ARM_X] * 0.5F + spine * 0.4F;
		float staffZ = a[STAFF_Z] + a[STAFF_Y] * 0.3F + a[L_ARM_Z] * 0.5F + lean * 0.5F;
		a[PENDANT_X] = trail(-m.lag(11, staffX, 0.32F, 0.18F) + m.follow(12, glide * 0.2F + drift, 0.3F, 0.3F)) * calm;
		a[PENDANT_Z] = trail(-m.lag(13, staffZ, 0.32F, 0.18F) - turn * 0.1F) * calm;
		a[PENDANT_BEND_X] = trail(m.lag(14, a[PENDANT_X], 0.55F, 0.3F) * 0.8F) * calm;
		a[PENDANT_BEND_Z] = trail(m.lag(15, a[PENDANT_Z], 0.55F, 0.3F) * 0.8F) * calm;
		a[WAIST_X] = trail(-m.lag(16, spine + a[SINK] * 0.03F, 0.4F, 0.25F) + glide * 0.1F) * calm;
		a[WAIST_Z] = trail(-m.lag(17, lean + twist * 0.5F, 0.4F, 0.25F)) * calm;
		// the halo's chains off the halo; the halo rings when its icicles snap free
		float crown = spine + a[HEAD] + a[HALO_TILT];
		a[HALO_SWING_X] = trail(-m.lag(18, crown, 0.45F, 0.2F) + drift * 0.6F) * calm;
		a[HALO_SWING_Z] = trail(-m.lag(19, lean + a[HEAD_TURN] + a[HALO_TURN] + a[ICICLE_TILT] * 0.4F, 0.45F, 0.2F)) * calm;
		a[HALO_RING] = trail(m.lag(20, a[ICICLE] * 0.25F + a[HALO_TURN], 0.75F, 0.14F)) * calm;
		// the mantle and the robe trail the glide, settle forward when he stops, swing off a drop and drag round a turn
		a[MANTLE_X] = trail(m.follow(21, glide * 0.14F + drift, 0.28F, 0.35F) - m.lag(22, spine + a[SINK] * 0.05F, 0.3F,
				0.3F) * 0.5F + a[FLARE]) * calm;
		a[MANTLE_Z] = trail(m.lag(23, m.yawTravel(), 0.22F, 0.45F) * 0.5F - m.lag(24, lean, 0.3F, 0.3F) * 0.5F) * calm;
		a[ROBE_X] = trail(m.follow(25, glide * 0.1F + drift * 0.5F, 0.32F, 0.35F) - m.lag(26, a[HIP_X] + a[SINK] * 0.05F,
				0.35F, 0.3F) * 0.5F + a[FLARE] * 0.7F) * calm;
		a[ROBE_Z] = trail(m.lag(27, m.yawTravel(), 0.25F, 0.45F) * 0.4F - m.lag(28, a[HIP_Z], 0.35F, 0.3F) * 0.5F) * calm;
		a[STOLE_X] = trail(m.follow(29, glide * 0.12F, 0.35F, 0.35F) - m.lag(30, spine, 0.4F, 0.3F) * 0.6F) * calm;
		a[STOLE_Z] = trail(-m.lag(31, lean + twist * 0.3F, 0.4F, 0.3F) * 0.6F) * calm;
		// the sleeves off the arms that carry them; the veil off the head; the cord's tails off the hips
		a[SLEEVE_RX] = trail(-m.lag(32, a[R_ARM_X] + a[R_FORE] + spine, 0.4F, 0.25F) * 0.6F + glide * 0.08F) * calm;
		a[SLEEVE_RZ] = trail(-m.lag(33, a[R_ARM_Z] + a[R_FORE_Z] + lean, 0.4F, 0.25F) * 0.6F) * calm;
		a[SLEEVE_LX] = trail(-m.lag(34, a[L_ARM_X] + a[L_FORE] + spine, 0.4F, 0.25F) * 0.6F + glide * 0.08F) * calm;
		a[SLEEVE_LZ] = trail(-m.lag(35, a[L_ARM_Z] + a[L_FORE_Z] + lean, 0.4F, 0.25F) * 0.6F) * calm;
		a[VEIL_X] = trail(-m.lag(36, spine + a[HEAD] + a[LAG_HEAD_X], 0.4F, 0.3F) * 0.7F + glide * 0.1F) * calm;
		a[CORD_X] = trail(-m.lag(37, a[HIP_X] + a[SINK] * 0.04F, 0.5F, 0.22F) + glide * 0.12F + drift) * calm;
		a[CORD_Z] = trail(-m.lag(38, a[HIP_Z] + a[HIP_Y] * 0.5F, 0.5F, 0.22F)) * calm;
	}

	private static float trail(float value) {
		return Mth.clamp(value, -TRAIL_LIMIT, TRAIL_LIMIT);
	}

	// ---------------------------------------------------------------- laying it on the rig

	private ModelPart[] fingers, fingerTips, sideLinks, pendantLinks, waistLinks, mantles, trains, robes, icicles;
	private ModelPart[][] haloLinks;
	private ModelPart[] haloChains;
	private Quaternionf[] mantleRest, robeRest;
	private Quaternionf stoleLeftRest, stoleRightRest, veilRest, bodyRest, headRest;
	/** The staff's length from the grip to its heel, and where the heel stands at rest (model units). */
	float staffLength = Float.NaN;
	final org.joml.Vector3f staffFootRest = new org.joml.Vector3f();

	/** Finds the parts the pass works through once, and measures the staff and the cloth at rest. */
	private void bind() {
		fingers = new ModelPart[4];
		fingerTips = new ModelPart[4];
		for (int i = 0; i < 4; i++) {
			fingers[i] = rightHand.getChild("right_finger_" + i);
			fingerTips[i] = fingers[i].getChild("right_finger_" + i + "_tip");
		}
		sideLinks = links(sideChain, "side_chain", 10);
		pendantLinks = links(pendantChain, "pendant_chain", 9);
		waistLinks = links(waistChain, "waist_chain", 7);
		haloChains = new ModelPart[4];
		haloLinks = new ModelPart[4][];
		icicles = new ModelPart[4];
		for (int i = 0; i < 4; i++) {
			haloChains[i] = halo.getChild("halo_chain_" + i);
			haloLinks[i] = links(haloChains[i], "halo_chain_" + i, 2 + i % 2);
			icicles[i] = haloChains[i].getChild("halo_icicle_" + i);
		}
		mantles = new ModelPart[9];
		trains = new ModelPart[9];
		mantleRest = new Quaternionf[9];
		for (int i = 0; i < 9; i++) {
			mantles[i] = chest.getChild("mantle_panel_" + i);
			trains[i] = mantles[i].getChild("mantle_panel_" + i + "_train");
			mantleRest[i] = rotation(mantles[i]);
		}
		robes = new ModelPart[16];
		robeRest = new Quaternionf[16];
		for (int i = 0; i < 16; i++) {
			robes[i] = hips.getChild("robe_panel_" + i);
			robeRest[i] = rotation(robes[i]);
		}
		stoleLeftRest = rotation(leftStole);
		stoleRightRest = rotation(rightStole);
		veilRest = rotation(backVeil);
		bodyRest = rotation(hips, chest);
		headRest = rotation(hips, chest, head);
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

	/**
	 * Lays the channels on the rig. {@code breath} is the breath's phase and {@code spin} the crystals' angle, both
	 * carried by the state; the breath shows by his calm, so it stops in the Endless Hour.
	 */
	void pose(float[] a, float age, float yaw, float pitch, float breath, float spin) {
		root.getAllParts().forEach(ModelPart::resetPose);
		if (Float.isNaN(staffLength)) bind();
		float calm = HemoEasing.clamp01(a[CALM]);
		float look = HemoEasing.clamp01(a[LOOK]);
		float inhale = Mth.sin(breath) * Math.max(0.0F, a[BREATH]) * calm;
		float shudder = a[SHUDDER] * (Mth.sin(age * 2.3F) * 0.6F + Mth.sin(age * 3.7F + 1.3F) * 0.4F) * 0.035F;
		float tremor = a[TREMOR] * (Mth.sin(age * 2.09F) * 0.7F + Mth.sin(age * 3.3F + 0.6F) * 0.3F);
		hips.xRot += a[HIP_X];
		hips.yRot += a[HIP_Y];
		hips.zRot += a[HIP_Z];
		hips.y += a[SINK] - inhale * 0.08F;
		chest.xRot += a[BOW] + a[LAG_SPINE] + inhale * 0.0105F + shudder;
		chest.yRot += a[TWIST];
		chest.zRot += a[LEAN] + shudder * 0.4F;
		head.xRot += a[HEAD] + a[LAG_HEAD_X] - inhale * 0.006F + Mth.clamp(pitch, -15.0F, 15.0F) * DEG * 0.5F * look;
		head.yRot += a[HEAD_TURN] + a[LAG_HEAD_Y] + Mth.clamp(yaw, -25.0F, 25.0F) * DEG * look;
		head.zRot += a[HEAD_ROLL];
		rightArm.xRot += a[R_ARM_X] + tremor;
		rightArm.yRot += a[R_ARM_Y];
		rightArm.zRot += a[R_ARM_Z] + tremor * 0.4F;
		rightForearm.xRot += a[R_FORE] + a[LAG_FORE];
		rightForearm.zRot += a[R_FORE_Z];
		rightHand.xRot += a[R_HAND] + a[LAG_FORE] * 0.5F;
		rightHand.zRot += a[R_HAND_Z];
		leftArm.xRot += a[L_ARM_X];
		leftArm.yRot += a[L_ARM_Y];
		leftArm.zRot += a[L_ARM_Z];
		leftForearm.xRot += a[L_FORE];
		leftForearm.zRot += a[L_FORE_Z];
		leftHand.xRot += a[L_HAND];
		claw(a);
		monasticHood.xRot -= a[HOOD];
		cowlLeft.xRot -= a[HOOD] * 0.6F;
		cowlRight.xRot -= a[HOOD] * 0.6F;
		halo.zRot += a[HALO_TURN] + a[HALO_RING];
		halo.xRot += a[HALO_TILT];
		halo.y += a[HALO_SLACK] * 1.5F;
		staffPose(a);
		hang(a, age, calm);
		lanterns(a, spin);
	}

	/**
	 * The crooked hand: a curl (positive closes, negative opens and splays), the overlap's trailing fingers, the hour or
	 * a fist closing a finger at a time, the thumb with them.
	 */
	private void claw(float[] a) {
		for (int i = 0; i < 4; i++) {
			float fold = HemoEasing.smoothstep(Mth.clamp(a[CLENCH] - i, 0.0F, 1.0F)) * 0.8F;
			float curl = a[FINGERS] + a[LAG_FINGERS] + fold;
			float bend = curl > 0.0F ? curl * 0.5F : curl * 1.1F;
			fingers[i].xRot -= bend * (1.0F + i * 0.06F);
			fingers[i].zRot += curl < 0.0F ? curl * (i - 1.5F) * 0.12F : 0.0F;
			fingerTips[i].xRot -= Math.max(-0.9F, bend) * 0.8F;
		}
		float thumb = a[FINGERS] + HemoEasing.smoothstep(Mth.clamp(a[CLENCH] - 3.0F, 0.0F, 1.0F)) * 0.8F;
		rightThumb.xRot -= thumb > 0.0F ? thumb * 0.5F : thumb * 0.8F;
	}

	/**
	 * The staff. Planted, its heel stays where the stance or the move puts it and the grip slides along the shaft, so it
	 * tilts as he leans and as his hand moves; lifted, it holds the angle the move gives it (pitched, turned about the
	 * vertical, rolled) and slides through the hand by the grip channel. Either way it stays one rigid pole through his
	 * hand, and the pendant lantern hangs plumb off it.
	 */
	private void staffPose(float[] a) {
		float plant = HemoEasing.clamp01(a[STAFF_PLANT]);
		Quaternionf hand = rotation(hips, chest, leftArm, leftForearm, leftHand);
		org.joml.Vector3f grip = world(new org.joml.Vector3f(staff.x, staff.y, staff.z), root, hips, chest, leftArm,
				leftForearm, leftHand);
		org.joml.Vector3f foot = new org.joml.Vector3f(staffFootRest).add(a[FOOT_X], Math.min(1.5F, a[FOOT_Y]), a[FOOT_Z]);
		org.joml.Vector3f planted = foot.sub(grip, new org.joml.Vector3f());
		float span = planted.length();
		if (span > 1.0E-4F) planted.div(span);
		else planted.set(0, 1, 0);
		org.joml.Vector3f free = new org.joml.Vector3f(0, 1, 0).rotateZ(a[STAFF_Z]).rotateX(a[STAFF_X]).rotateY(a[STAFF_Y]);
		org.joml.Vector3f axis = free.lerp(planted, plant, new org.joml.Vector3f());
		if (axis.lengthSquared() < 1.0E-6F) axis.set(planted);
		axis.normalize();
		Quaternionf pole = new Quaternionf().rotationTo(0, 1, 0, axis.x, axis.y, axis.z);
		setRotationFromMatrix(staff, new Quaternionf(hand).conjugate().mul(pole).get(new Matrix3f()));
		float slide = HemoEasing.lerp(-a[GRIP], Mth.clamp(span - staffLength, -GRIP_SLIDE, GRIP_SLIDE), plant);
		org.joml.Vector3f along = new Quaternionf(hand).conjugate().transform(new org.joml.Vector3f(axis).mul(slide));
		staff.x += along.x;
		staff.y += along.y;
		staff.z += along.z;
		Quaternionf plumb = rotation(hips, chest, leftArm, leftForearm, leftHand, staff).conjugate();
		setRotationFromMatrix(pendantChain, plumb.mul(swing(a[PENDANT_X], a[PENDANT_Z])).get(new Matrix3f()));
		bend(pendantLinks, pendantLantern, PENDANT_GAP, a[PENDANT_BEND_X], a[PENDANT_BEND_Z]);
	}

	/** The chains, the cloth and the hanging ice: plumb or partly plumb, swinging off what carries them. */
	private void hang(float[] a, float age, float calm) {
		float idle = calm * 0.5F;
		Quaternionf body = rotation(hips, chest);
		Quaternionf bodyPlumb = new Quaternionf(body).conjugate();
		setRotationFromMatrix(sideChain, new Quaternionf(bodyPlumb).mul(swing(a[SIDE_X] + a[SIDE_SWING_X]
				+ Mth.sin(age * 0.055F + 0.8F) * 0.025F * idle, a[SIDE_Z] + a[SIDE_SWING_Z])).get(new Matrix3f()));
		bend(sideLinks, sideLantern, SIDE_GAP, a[SIDE_BEND_X], a[SIDE_BEND_Z]);
		setRotationFromMatrix(waistChain, new Quaternionf(bodyPlumb).mul(swing(a[WAIST_X] + Mth.sin(age * 0.06F) * 0.02F
				* idle, a[WAIST_Z])).get(new Matrix3f()));
		bend(waistLinks, null, 0.0F, a[WAIST_X] * 0.5F, a[WAIST_Z] * 0.5F);
		if (halo.visible) {
			Quaternionf crownPlumb = rotation(hips, chest, head, halo).conjugate();
			float slack = HemoEasing.clamp01(a[HALO_SLACK]);
			for (int i = 0; i < 4; i++) {
				float idleSwing = Mth.sin(age * 0.04F + i) * 0.03F * idle;
				setRotationFromMatrix(haloChains[i], new Quaternionf(crownPlumb).mul(swing(a[HALO_SWING_X] * (1.0F + i * 0.1F)
						+ slack * 0.15F, a[HALO_SWING_Z] * (1.0F + i * 0.08F) + idleSwing)).get(new Matrix3f()));
				bend(haloLinks[i], icicles[i], 0.0F, a[HALO_SWING_X] * 0.5F, a[HALO_SWING_Z] * 0.5F);
			}
		}
		// the cloth: mostly hanging as it hung at rest whatever the body does, swinging off it, heaped on the floor
		Quaternionf hipsNow = rotation(hips);
		for (int i = 0; i < 9; i++) {
			float flutter = Mth.sin(age * 0.05F - i * 0.65F) * 0.014F * idle;
			drape(mantles[i], body, bodyRest, mantleRest[i], 0.75F, a[MANTLE_X] + flutter, a[MANTLE_Z]);
			trains[i].xRot += Mth.sin(age * 0.055F - i * 0.65F - 1.0F) * 0.02F * idle + Math.max(0.0F, a[MANTLE_X]) * 0.3F;
		}
		for (int i = 0; i < 16; i++) {
			float angle = i * Mth.TWO_PI / 16.0F;
			float flutter = Mth.sin(age * 0.05F - i * 0.7F) * 0.012F * idle;
			drape(robes[i], hipsNow, new Quaternionf(), robeRest[i], 0.85F, a[ROBE_X] * (0.6F + 0.4F * Mth.cos(angle))
					+ flutter, a[ROBE_Z]);
		}
		drape(leftStole, body, bodyRest, stoleLeftRest, 0.6F, a[STOLE_X], a[STOLE_Z]);
		drape(rightStole, body, bodyRest, stoleRightRest, 0.6F, a[STOLE_X], a[STOLE_Z]);
		drape(backVeil, rotation(hips, chest, head), headRest, veilRest, 0.5F, a[VEIL_X] + Mth.sin(age * 0.055F) * 0.02F
				* idle, 0.0F);
		heap(Math.max(0.0F, a[UNDER]));
		// the sleeves and the claw's ice hang plumb, swinging off the arms
		setRotationFromMatrix(rightSleeve, rotation(hips, chest, rightArm, rightForearm).conjugate()
				.mul(swing(a[SLEEVE_RX], a[SLEEVE_RZ])).get(new Matrix3f()));
		setRotationFromMatrix(leftSleeve, rotation(hips, chest, leftArm, leftForearm).conjugate()
				.mul(swing(a[SLEEVE_LX], a[SLEEVE_LZ])).get(new Matrix3f()));
		setRotationFromMatrix(clawIce, rotation(hips, chest, rightArm, rightForearm, rightHand).conjugate()
				.mul(swing(a[SLEEVE_RX] * 0.5F, a[SLEEVE_RZ] * 0.5F)).get(new Matrix3f()));
		setRotationFromMatrix(knottedCordTails, rotation(hips, knottedCord).conjugate().mul(swing(a[CORD_X], a[CORD_Z]))
				.get(new Matrix3f()));
	}

	/**
	 * A cloth panel hanging from {@code parentNow}: {@code share} of the way from carried with its parent to hanging as it
	 * hung at rest, then swung in the world ({@code swingX} the bottom backward, {@code swingZ} toward his right).
	 */
	private static void drape(ModelPart part, Quaternionf parentNow, Quaternionf parentRest, Quaternionf authored,
			float share, float swingX, float swingZ) {
		Quaternionf carried = new Quaternionf(parentNow).mul(authored);
		Quaternionf hung = new Quaternionf(parentRest).mul(authored);
		Quaternionf world = swing(swingX, swingZ).mul(carried.slerp(hung, share));
		setRotationFromMatrix(part, new Quaternionf(parentNow).conjugate().mul(world).get(new Matrix3f()));
	}

	/**
	 * The trains and the robe come to rest on the floor instead of passing through it: each folds outward to meet it.
	 * Sinking into the dark ({@code under}, model units), the floor rises up his body: the cloth heaps on the surface
	 * and is drawn under after him, a panel whose root has gone under following it down.
	 */
	private void heap(float under) {
		for (int i = 0; i < 9; i++) floor(trains[i], 12.0F, under, root, hips, chest, mantles[i], trains[i]);
		for (int i = 0; i < 16; i++) floor(robes[i], 12.0F + (i % 3) * 0.5F, under, root, hips, robes[i]);
	}

	private static void floor(ModelPart part, float length, float under, ModelPart... chain) {
		PoseStack poses = new PoseStack();
		for (ModelPart link : chain) link.translateAndRotate(poses);
		org.joml.Vector3f top = poses.last().pose().transformPosition(new org.joml.Vector3f()).mul(16.0F);
		org.joml.Vector3f down = poses.last().pose().transformDirection(new org.joml.Vector3f(0, 1, 0)).normalize();
		float limit = FLOOR - under - CLOTH_CLEARANCE;
		if (top.y >= limit) return;
		if (top.y + length * down.y <= limit) return;
		float needed = (float) Math.acos(Mth.clamp((limit - top.y) / length, -1.0F, 1.0F));
		float current = (float) Math.acos(Mth.clamp(down.y, -1.0F, 1.0F));
		part.xRot += Math.max(0.0F, needed - current);
	}

	/** The crystals turn by {@code spin}; the lights scale (the Gutter shrinks the light, never the cage); the icicles. */
	private void lanterns(float[] a, float spin) {
		staffLanternCrystal.yRot += spin;
		pendantLanternCrystal.yRot -= spin * 0.8F;
		sideLanternCrystal.yRot += spin * 0.65F;
		// the light shrinks to a point in the Gutter and goes out only in the Eclipse
		float core = Mth.clamp(a[CORE], 0.0F, 1.4F);
		staffLanternCore.xScale = staffLanternCore.yScale = staffLanternCore.zScale = Math.max(0.02F, core);
		staffLanternCore.visible = core > 0.02F;
		float side = Mth.clamp(a[SIDE_CORE], 0.0F, 1.5F);
		sideLanternCore.xScale = sideLanternCore.yScale = sideLanternCore.zScale = Math.max(0.02F, side);
		sideLanternCore.visible = side > 0.02F;
		float grow = Math.max(0.0F, a[ICICLE]);
		for (int i = 0; i < 4; i++) {
			ModelPart icicle = icicles[i];
			icicle.xRot += a[ICICLE_TILT] * (i % 2 == 0 ? 1.0F : -1.0F);
			icicle.zRot += a[ICICLE_TILT] * 0.4F * (i < 2 ? 1.0F : -1.0F);
			icicle.xScale = icicle.yScale = icicle.zScale = grow;
			icicle.visible = grow > 0.02F;
		}
	}

	/**
	 * Curves a hanging chain: each link turns a little more than the one above it and hangs from the end of it, the
	 * pendant riding {@code gap} below the last. Zero bend leaves the links as authored.
	 */
	private static void bend(ModelPart[] links, ModelPart pendant, float gap, float bendX, float bendZ) {
		float x = 0, y = 0, z = 0;
		int n = links.length;
		float ax = 0, az = 0;
		for (int i = 0; i < n; i++) {
			float share = (i + 1.0F) / n;
			ax = bendX * share;
			az = bendZ * share;
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
		pendant.x = x - gap * Mth.sin(az);
		pendant.y = y + gap * Mth.cos(ax) * Mth.cos(az);
		pendant.z = z + gap * Mth.sin(ax);
		pendant.xRot += bendX;
		pendant.zRot += bendZ;
	}

	/** A rotation in the world: the bottom of a hanging part swung back by {@code x}, toward his right by {@code z}. */
	private static Quaternionf swing(float x, float z) {
		return new Quaternionf().rotationZYX(z, 0.0F, x);
	}

	/** The glow layer shows the breast seam only in the Bow. */
	public void glowSeam(boolean shown) {
		breastSeam.visible = shown;
	}

	public org.joml.Vector3f core() {
		return point(new org.joml.Vector3f(0, 0, -.18F), root, hips, chest);
	}

	public org.joml.Vector3f seam() {
		return point(new org.joml.Vector3f(), root, hips, chest, breastSeam);
	}

	public org.joml.Vector3f claw() {
		return point(new org.joml.Vector3f(0, .08F, 0), root, hips, chest, rightArm, rightForearm, rightHand);
	}

	/** The staff's iron heel. */
	public org.joml.Vector3f heel() {
		float length = Float.isNaN(staffLength) ? 26.0F : staffLength;
		return point(new org.joml.Vector3f(0, length / 16.0F, 0), root, hips, chest, leftArm, leftForearm, leftHand, staff);
	}

	public org.joml.Vector3f[] lanterns() {
		return new org.joml.Vector3f[]{
				point(new org.joml.Vector3f(), root, hips, chest, leftArm, leftForearm, leftHand, staff, staffLantern,
						staffLanternCrystal),
				point(new org.joml.Vector3f(), root, hips, chest, leftArm, leftForearm, leftHand, staff, pendantChain,
						pendantLantern, pendantLanternCrystal),
				point(new org.joml.Vector3f(), root, hips, chest, sideChain, sideLantern, sideLanternCrystal)};
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
		// Matrix extraction avoids the quaternion ZYX X denominator error in JOML 1.10.5.
		part.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
		part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1.0F, 1.0F));
		part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
	}
