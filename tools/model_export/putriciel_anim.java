
	// ---------------------------------------------------------------- Putriciel's pose code (putriciel_anim.java)
	// Visual pass 2 (2026-11-06). A frame is built in layers of channels: the stance (the body at rest, blended
	// continuously from the rot's hunch to the fire's upright purifier by the look), the gait (the rot's limping shamble
	// or the fire's even stride, PutricielGait, keyed to distance), then the playing track as deltas over both
	// (Catmull-Rom keys, HemoEasing.catmullRom, on the track's own replayed clock through PutricielTracks: a coil, a
	// fast arc through the commit, follow-through past the line and a weighted settle back to zero). PutricielAnimState
	// blends a changed track out of the last shown pose by inertialization (Hemorath's HemorathPoseBlend) and its springs
	// write the overlap (the hood trailing the chest, the hanging chains, the robe, the mantles, the shelves, sacs and
	// drips). pose() lays it on the rig: the knees fold and the hips sink until a sole or a kneecap meets the floor, the
	// arms reach their targets, the staff is one pole planted or swung, and the censers hang on real pendulums.

	private static final float DEG = Mth.DEG_TO_RAD;
	/** The chest's generated rest pitch, which the look's hunch replaces. */
	private static final float REST_HUNCH = 18.0F;
	private static final int ROBE_PANELS = 14;

	static final int BOW = 0, TWIST = 1, LEAN = 2, CROUCH = 3, HIP_X = 4, HIP_TWIST = 5, HIP_SWAY = 6, STEP_L = 7,
			STEP_R = 8, LIFT_L = 9, LIFT_R = 10, KNEEL = 11, KNEEL_BOTH = 12, TIPTOE = 13, HEAD = 14, HEAD_TURN = 15,
			HEAD_ROLL = 16, HOOD = 17, L_ARM_X = 18, L_ARM_Y = 19, L_ARM_Z = 20, L_FORE = 21, L_GRIP_X = 22,
			L_GRIP_Y = 23, L_GRIP_Z = 24, STAFF_PLANT = 25, STAFF_TILT = 26, STAFF_AZ = 27, STAFF_FOOT_X = 28,
			STAFF_FOOT_Y = 29, STAFF_FOOT_Z = 30, STAFF_SPIN = 31, CENSER_FLING = 32, CENSER_OUT = 33, CAST = 34,
			CAST_THROW = 35, STUCK = 36, R_ARM_X = 37, R_ARM_Y = 38, R_ARM_Z = 39, R_FORE = 40, PALM = 41,
			FINGERS = 42, DRIPS = 43, PALM_OFFER = 44, PALM_FLOOR = 45, SPREAD = 46, SAC = 47, SAC_SPENT = 55,
			COLONY = 56, BLOOM = 57, ROOTS = 58, SHAKE = 59,
			// the overlap, written by the springs after the blend
			LAG_HEAD_X = 60, LAG_HEAD_Y = 61, LAG_HEAD_Z = 62, LAG_SPINE = 63, LAG_FORE_R = 64, LAG_FINGERS = 65,
			LAG_FORE_L = 66, VEST_X = 67, VEST_Z = 68, VEST_BEND_X = 69, VEST_BEND_Z = 70, ROBE_X = 71, ROBE_Z = 72,
			MANTLE = 73, COLONY_JIG = 74, SAC_JIG = 75, CROWN_JIG = 76, DRIP_X = 77, DRIP_Z = 78, ROOT_DRAG = 79,
			FLINCH = 80, CHANNELS = 81;
	/** The first channel the springs own. */
	static final int SECONDARY = LAG_HEAD_X;
	/** A spring's trail never turns a part further than this (a body snapped round by a teleport settles from it). */
	static final float TRAIL_LIMIT = 0.7F;
	/** The grip slides along the shaft at most this far either way, model units. */
	static final float GRIP_SLIDE = 6.0F;
	/** A censer's chain never swings further than this from plumb, radians. */
	static final float CENSER_LIMIT = 1.45F;
	/** Spring slots: the censers' pendulums (x and z each). */
	private static final int PENDULUM = 30;
	/**
	 * Where the Offering holds the hand, model units: the palm at the brow, 0.7 blocks ahead and 2.45 up as the server's
	 * absolution aim reads it ({@code PutricielEntity.PALM_FORWARD}, {@code PALM_UP}); the palm is 1.28 above the hand.
	 */
	static final org.joml.Vector3f OFFER_HAND = new org.joml.Vector3f(0.47F, 5.25F, -5.7F);
	/** Where the hand lays the palm on the floor ahead of the kneeling body, model units. */
	static final org.joml.Vector3f FLOOR_HAND = new org.joml.Vector3f(-4.5F, 23.4F, -8.0F);
	/** Where a fouled censer lies, model units: on the floor ahead of the staff's swing. */
	static final org.joml.Vector3f STUCK_AT = new org.joml.Vector3f(4.0F, 24.0F, -14.0F);

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		PutricielAnimState motion = PutricielAnimState.of(entity);
		motion.update(entity, AnimationTime.partialTick(), ageInTicks);
		float[] act = frame(motion, ageInTicks, limbSwing, limbSwingAmount);
		motion.blend(act);
		secondary(act, motion);
		pose(act, motion, ageInTicks, netHeadYaw, headPitch);
	}

	/** The stance, the gait and the playing track for one frame, before the blend and the springs. */
	static float[] frame(PutricielAnimState m, float age, float stride, float walking) {
		float[] a = new float[CHANNELS];
		float fire = m.fire();
		stance(a, fire, m.kindling(), age, m.breath(), m.sacPhase(), m.sway(), 1.0F - m.busy());
		gait(a, fire, stride, walking, m.turn(), m.turnTravel(), m.busy());
		track(a, m.track());
		return a;
	}

	/** The playing track's deltas at its time, over the stance and the gait. */
	static void track(float[] a, PutricielTracks.Track track) {
		float t = track.time();
		int id = track.id();
		if (id == PutricielTracks.ENDING) ending(a, t);
		else if (id == PutricielTracks.RISE) rise(a, t);
		else if (id == PutricielTracks.RECOIL) recoil(a, t);
		else if (track.opening()) opening(a, id - PutricielTracks.OPENING, t, track.length());
		else if (track.move()) move(a, PutricielMove.byId(id - PutricielTracks.MOVE), t, track.length(), track.count());
	}

	// ---------------------------------------------------------------- the stance

	/**
	 * The body at rest, continuous from the rot ({@code fire} 0) to the fire (1). In the rot: sunk onto the planted
	 * staff on soft knees, listing toward it, the hood lolling with a slow sway, the palm held low and its fingers slowly
	 * curling, the eight sacs pulsing out of rhythm, the 120-tick breath; every 70 ticks a wet shudder runs through the
	 * growths, and the hood ticks between held looks; from 75% Kindling it trembles. In the fire: straight, shoulders
	 * back, the palm half open and lifted, the hood level, a fine steady tremor, the censers swinging lit; every 50 ticks
	 * the purifier tests the palm. {@code fidget} fades the idle business under a move.
	 */
	static void stance(float[] a, float fire, float kindling, float age, float breath, float sacs, float sway,
			float fidget) {
		float rot = 1.0F - fire;
		float inhale = Mth.sin(breath);
		a[STAFF_PLANT] = 1.0F;
		a[CROUCH] += 0.28F * rot + 0.06F * fire;
		a[BOW] += inhale * (0.035F * rot + 0.02F * fire);
		a[HEAD] += 0.14F * rot - 0.04F * fire - inhale * 0.02F;
		a[LEAN] += 0.05F * rot + Mth.sin(sway + 0.8F) * 0.025F * rot;
		a[HIP_SWAY] += Mth.sin(sway) * 0.03F * rot;
		a[HEAD_ROLL] += Mth.sin(sway + 1.6F) * 0.07F * rot;
		a[R_ARM_X] += 0.12F * rot - 0.3F * fire;
		a[R_FORE] += -0.18F * fire;
		a[FINGERS] += 0.3F * rot - 0.25F * fire + Mth.sin(age * 0.05F) * 0.15F * rot * fidget;
		a[DRIPS] += 0.15F * rot;
		for (int i = 0; i < 8; i++) a[SAC + i] += Mth.sin(sacs - i * 0.9F) * (0.05F * rot + 0.015F * fire);
		a[COLONY] += inhale * 0.04F * rot;
		a[SPREAD] += 0.08F * fire;
		a[CENSER_OUT] += 0.05F * fire;
		a[SHAKE] += PutricielVisualRules.tremor(kindling, true) * 0.7F * rot + 0.22F * fire;
		// the idle business: a wet shudder through the growths, the hood ticking toward sounds; the purifier's test
		float shudder = HemoEasing.window(age % 70.0F, 0.0F, 1.5F, 3.0F, 9.0F) * rot * fidget;
		a[COLONY] -= shudder * 0.2F;
		a[SHAKE] += shudder * 0.6F;
		a[SAC_SPENT] += shudder * 0.08F;
		a[HEAD_TURN] += tics(age / 37.0F, 3.0F) * 0.14F * rot * fidget;
		float test = HemoEasing.window(age % 50.0F, 0.0F, 6.0F, 14.0F, 24.0F) * fire * fidget;
		a[R_ARM_X] -= test * 0.18F;
		a[FINGERS] -= test * 0.3F;
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

	// ---------------------------------------------------------------- the shamble, the stride and the turn

	/**
	 * The rot's shamble (PutricielGait): the left foot lurches out ahead, the weight is thrown over it (the chest
	 * dropping, the hips swinging onto it), the right foot drags up behind, low; the staff swings ahead with the lead
	 * foot and taps down as it lands. The fire's stride: an even walk, knees lifting, shoulders countering the hips, the
	 * staff walking with the left foot. Over both the turn: the shoulders and the hood lead, the hips lag, the staff
	 * swings out, the feet shuffle round when the body turns in place.
	 */
	static void gait(float[] a, float fire, float stride, float walking, float turn, float turnTravel, float busy) {
		float w = HemoEasing.clamp01(walking * 1.8F);
		float upper = w * (1.0F - busy * 0.75F);
		float ws = w * (1.0F - fire), wf = w * fire;
		// the shamble
		float p = PutricielGait.phase(stride, PutricielGait.SHAMBLE_BLOCKS);
		float lurch = PutricielGait.lurch(p);
		float lands = PutricielGait.landing(p, PutricielGait.LEAD_END);
		a[STEP_L] += PutricielGait.thigh(PutricielGait.shambleLead(p)) * ws;
		a[STEP_R] += PutricielGait.thigh(PutricielGait.shambleTrail(p)) * ws;
		a[LIFT_L] += PutricielGait.shambleLift(p, true) * ws;
		a[LIFT_R] += PutricielGait.shambleLift(p, false) * ws;
		a[BOW] += (0.14F * lurch + 0.07F * lands) * ws;
		a[HEAD] -= 0.08F * lurch * ws;
		a[LEAN] += 0.07F * lurch * ws;
		a[HIP_SWAY] += 0.06F * lurch * ws;
		a[CROUCH] += 0.3F * lands * ws;
		a[TWIST] += Mth.sin(p * Mth.TWO_PI) * 0.09F * upper * (1.0F - fire);
		a[R_ARM_X] += 0.25F * lurch * upper * (1.0F - fire);
		a[STAFF_FOOT_Z] += PutricielGait.shambleLead(p) * ws;
		a[STAFF_FOOT_Y] -= PutricielGait.shambleLift(p, true) * 2.0F * ws;
		a[L_ARM_X] += 0.1F * lands * ws;
		// the stride
		float q = PutricielGait.phase(stride, PutricielGait.STRIDE_BLOCKS);
		float jolt = PutricielGait.landing(q, 0.5F) + PutricielGait.landing(q, 0.0F);
		a[STEP_L] += PutricielGait.thigh(PutricielGait.strideFoot(q, PutricielGait.LEFT)) * wf;
		a[STEP_R] += PutricielGait.thigh(PutricielGait.strideFoot(q, PutricielGait.RIGHT)) * wf;
		a[LIFT_L] += PutricielGait.strideLift(q, PutricielGait.LEFT) * 1.2F * wf;
		a[LIFT_R] += PutricielGait.strideLift(q, PutricielGait.RIGHT) * 1.2F * wf;
		a[CROUCH] += 0.14F * jolt * wf;
		a[BOW] += 0.05F * wf;
		a[TWIST] += Mth.sin(q * Mth.TWO_PI) * 0.08F * upper * fire;
		a[HIP_TWIST] -= Mth.sin(q * Mth.TWO_PI) * 0.05F * wf;
		a[R_ARM_X] += Mth.sin(q * Mth.TWO_PI) * 0.2F * upper * fire;
		a[STAFF_FOOT_Z] += PutricielGait.strideFoot(q, PutricielGait.LEFT) * wf;
		a[STAFF_FOOT_Y] -= PutricielGait.strideLift(q, PutricielGait.LEFT) * 1.5F * wf;
		// the turn
		a[TWIST] += 0.22F * turn;
		a[HEAD_TURN] += 0.25F * turn;
		a[HIP_TWIST] -= 0.06F * turn;
		a[STAFF_FOOT_X] += 1.5F * turn;
		float still = 1.0F - HemoEasing.clamp01(walking * 2.0F);
		float shuffle = Mth.sin(turnTravel * Mth.PI / 20.0F) * Math.min(1.0F, Math.abs(turn) * 1.5F) * still;
		a[LIFT_L] += Math.max(0.0F, shuffle) * 0.6F;
		a[LIFT_R] += Math.max(0.0F, -shuffle) * 0.6F;
		a[STEP_L] -= Math.max(0.0F, shuffle) * 0.1F;
		a[STEP_R] -= Math.max(0.0F, -shuffle) * 0.1F;
	}

	// ---------------------------------------------------------------- the moves

	/** For tests and the review: a move at {@code t} with its authored length and five volley discs. */
	static void move(float[] a, PutricielMove move, float t) {
		move(a, move, t, move == null ? 0 : PutricielTracks.authoredTimeline(move, SaintMoveInputs.NONE).total() + 60, 5);
	}

	/**
	 * Every move as deltas on its authored clock: each starts and ends at zero, so it rises out of the stance and
	 * settles back into it. The commit (the end of the authored wind-up) is where the strike's arc runs fastest.
	 * {@code length} is the track's whole length (the kneeling beats hold to it); {@code count} the volley's discs.
	 */
	static void move(float[] a, PutricielMove move, float t, float length, int count) {
		if (move == null) return;
		switch (move) {
			case CENSER_SWING -> censerSwing(a, t);
			case CENSER_BACKSWING -> censerBackswing(a, t);
			case CENSER_DROP -> censerDrop(a, t);
			case OFFERING_GRASP -> offeringGrasp(a, t);
			case ROT_BREATH -> rotBreath(a, t);
			case SAC_SWELL -> sacSwell(a, t);
			case SHELF_VOLLEY -> shelfVolley(a, t, Math.max(1, count));
			case ROOT_SNARE -> rootSnare(a, t);
			case CENSER_ORBIT -> censerOrbit(a, t);
			case MOULD_COMMUNION -> mouldCommunion(a, t);
			case BLOOM_OF_ROT -> bloomOfRot(a, t);
			case IGNITION -> ignition(a, t);
			case PURGING_SWEEP -> purgingSweep(a, t);
			case CENSER_CAST -> censerCast(a, t);
			case CAUTERISING_LANE -> cauterisingLane(a, t);
			case PYRE_HEART -> pyreHeart(a, t);
			case PYRE_LINES -> pyreLines(a, t);
			case FULL_PURGE -> fullPurge(a, t);
			case OFFERING -> offering(a, t, Math.max(30.0F, length));
			case EMBERING -> embering(a, t, Math.max(30.0F, length));
		}
	}

	/**
	 * 16 / 6 / 14. The staff comes up off the floor and is drawn back across the body to his right, the shoulders
	 * winding with it, the censers lifting on their chains; at 14 the coil is deepest. From 16 the staff whips across
	 * from right to left as the cone sweeps, the left foot stamping in and the censers flung out along the staff; it
	 * carries on past his left, off balance, then swings down and plants again.
	 */
	static void censerSwing(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 5, 27, 33);
		a[STAFF_TILT] += catmullRom(t, 0, 0, 5, 0.55F, 11, 1.0F, 15, 1.1F, 17, 1.3F, 22, 1.3F, 26, 0.95F, 31, 0.3F, 36, 0);
		a[STAFF_AZ] += catmullRom(t, 0, 0, 5, 0.6F, 11, 1.35F, 14.5F, 1.6F, 16, 1.25F, 17.5F, 0.45F, 19, -0.2F, 22, -1.1F,
				24.5F, -1.5F, 28, -1.2F, 32, -0.4F, 36, 0);
		a[CENSER_FLING] += catmullRom(t, 0, 0, 8, 0.3F, 15, 0.5F, 17, 1.0F, 21, 1.1F, 25, 0.6F, 30, 0.15F, 36, 0);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 8, -4.0F, 14, -5.0F, 17, -2.0F, 21, 1.0F, 28, 0.5F, 36, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 8, -2.0F, 14, -3.0F, 18, -1.0F, 24, 0, 36, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 8, -3.0F, 14, -3.5F, 18, -4.0F, 22, -2.0F, 30, -0.5F, 36, 0);
		a[TWIST] += catmullRom(t, 0, 0, 6, 0.3F, 14, 0.55F, 16, 0.45F, 18, -0.1F, 21, -0.55F, 24, -0.62F, 29, -0.25F, 36, 0);
		a[BOW] += catmullRom(t, 0, 0, 8, -0.08F, 14, -0.12F, 17, 0.25F, 21, 0.35F, 27, 0.12F, 36, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 8, 0.2F, 14, 0.32F, 17, 0.5F, 22, 0.45F, 30, 0.12F, 36, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 14, -0.3F, 18, 0, 22, 0.35F, 28, 0.15F, 36, 0);
		a[HIP_TWIST] += catmullRom(t, 0, 0, 14, 0.12F, 18, -0.1F, 24, -0.15F, 36, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 14, 0.05F, 17, -0.25F, 24, -0.2F, 32, -0.05F, 36, 0);
		a[LIFT_L] += HemoEasing.window(t, 14.5F, 15.5F, 16, 17.5F) * 0.6F;
		a[STEP_R] += catmullRom(t, 0, 0, 21, 0, 24, 0.15F, 30, 0.1F, 36, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 14, 0.25F, 18, -0.3F, 24, -0.2F, 36, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 14, 0.1F, 18, 0.35F, 24, 0.25F, 36, 0);
	}

	/**
	 * 12 / 6 / 18. Out of the swing's follow-through the censers check, the staff is hauled back across to his left
	 * and the shoulders wind the other way; at 12 it comes back left to right, the right foot stepping across, flung
	 * past his right, and down.
	 */
	static void censerBackswing(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 0, 3, 26, 32);
		a[STAFF_TILT] += catmullRom(t, 0, 0, 4, 0.7F, 10, 1.15F, 13, 1.3F, 18, 1.3F, 23, 0.9F, 29, 0.25F, 36, 0);
		a[STAFF_AZ] += catmullRom(t, 0, 0, 4, -0.6F, 9, -1.45F, 11, -1.6F, 12, -1.25F, 13.5F, -0.45F, 15, 0.2F, 18, 1.1F,
				20.5F, 1.5F, 25, 1.15F, 31, 0.3F, 36, 0);
		a[CENSER_FLING] += catmullRom(t, 0, 0, 8, 0.4F, 12, 0.5F, 14, 1.0F, 19, 1.1F, 24, 0.5F, 30, 0.1F, 36, 0);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 6, 1.5F, 11, 2.0F, 14, -1.0F, 18, -4.0F, 24, -2.0F, 32, 0, 36, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 12, -1.0F, 17, -2.0F, 26, -0.5F, 36, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 6, -1.0F, 12, -2.0F, 16, -4.0F, 20, -3.0F, 30, -0.5F, 36, 0);
		a[TWIST] += catmullRom(t, 0, 0, 5, -0.35F, 11, -0.6F, 12, -0.5F, 14, 0.05F, 18, 0.55F, 21, 0.65F, 27, 0.2F, 36, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.06F, 13, 0.25F, 18, 0.35F, 26, 0.1F, 36, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.3F, 13, 0.5F, 19, 0.45F, 28, 0.1F, 36, 0);
		a[HEAD_TURN] += catmullRom(t, 0, 0, 11, 0.3F, 14, 0, 18, -0.3F, 26, -0.1F, 36, 0);
		a[HIP_TWIST] += catmullRom(t, 0, 0, 11, -0.12F, 15, 0.1F, 21, 0.15F, 36, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 12, 0, 15, -0.25F, 22, -0.2F, 30, -0.05F, 36, 0);
		a[LIFT_R] += HemoEasing.window(t, 11, 12.5F, 13, 15) * 0.5F;
		a[R_ARM_Z] += catmullRom(t, 0, 0, 11, 0.3F, 15, 0.05F, 22, 0.1F, 36, 0);
	}

	/**
	 * 22 / 4 / 30. The staff is hoisted high and tipped back, the right censer swung up and over on its chain; it is
	 * held at the top, trembling, through the delay that catches an early roll. At 22 the staff comes down over his
	 * head and forward, the censer flung at the quarry, his body lunging after it; it bursts and fouls in the floor,
	 * and he hauls on the stuck chain in jerks (the Fouled Chain takes the recovery).
	 */
	static void censerDrop(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 4, 44, 52);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 8, -4.0F, 22, -5.0F, 24, -3.0F, 30, -3.0F, 44, -2.5F, 56, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 6, -6.0F, 12, -8.5F, 22, -8.5F, 23.5F, -3.0F, 26, 3.0F, 30, 4.0F, 44, 3.5F,
				52, 1.0F, 56, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 10, 0, 22, 0.5F, 24, -4.0F, 28, -5.0F, 44, -4.5F, 52, -1.0F, 56, 0);
		// the head comes over and down past level, driven at the floor ahead where the censer fouls
		a[STAFF_TILT] += catmullRom(t, 0, 0, 8, -0.35F, 14, -0.55F, 16, -0.6F, 22, -0.6F, 23, 0.3F, 24.5F, 1.3F,
				26, 1.75F, 28, 1.85F, 44, 1.7F, 52, 0.4F, 56, 0);
		a[CENSER_FLING] += catmullRom(t, 0, 0, 10, -0.4F, 16, -0.8F, 22, -0.85F, 23.5F, 0.3F, 25, 1.0F, 27, 0.2F, 56, 0);
		a[STUCK] += HemoEasing.window(t, 25, 26.5F, 46, 54);
		a[SHAKE] += HemoEasing.window(t, 14, 16, 21, 22) * 0.6F;
		a[TIPTOE] += HemoEasing.window(t, 6, 12, 21, 22.5F) * 0.5F;
		a[BOW] += catmullRom(t, 0, 0, 12, -0.25F, 22, -0.3F, 24, 0.45F, 26, 0.6F, 30, 0.5F, 44, 0.35F, 56, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 22, 0, 24.5F, 0.6F, 27, 0.75F, 40, 0.5F, 56, 0);
		a[HEAD] += catmullRom(t, 0, 0, 12, -0.3F, 22, -0.35F, 24, 0.15F, 30, 0.1F, 56, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 21, 0, 24, -0.35F, 30, -0.3F, 48, -0.1F, 56, 0);
		a[LIFT_L] += HemoEasing.window(t, 21, 22.5F, 23, 24.5F) * 0.7F;
		a[R_ARM_Z] += catmullRom(t, 0, 0, 12, 0.35F, 22, 0.4F, 25, 0.1F, 56, 0);
		// the haul on the fouled chain: the body thrown back in jerks, the grip dragged back
		float haul = HemoEasing.window(t, 28, 30, 46, 50) * (0.5F + 0.5F * Mth.sin((t - 28) * 0.9F));
		a[BOW] -= haul * 0.3F;
		a[L_GRIP_Z] += haul * 2.5F;
		a[STEP_R] += haul * 0.15F;
	}

	/**
	 * 20 / 5 / 16. The palm comes up with its drips running long and the fingers spread, then turns out and draws back
	 * to the shoulder; at 20 the arm is thrown out at the quarry in a lunge, the right foot stepping in, and at 23 the
	 * fingers close on what they caught and the arm hauls it back (the pull of two blocks) before he settles.
	 */
	static void offeringGrasp(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.25F, 16, -0.4F, 19, -0.5F, 20, -1.0F, 21, -1.45F, 22, -1.55F, 24, -1.45F,
				26, -0.7F, 29, -0.25F, 35, -0.05F, 41, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 8, -0.4F, 18, -0.6F, 20.5F, 0.1F, 22, 0.15F, 26, -0.3F, 41, 0);
		a[PALM] += catmullRom(t, 0, 0, 6, 0, 12, 0.6F, 18, 1.0F, 22, 1.0F, 25, 0.6F, 32, 0.1F, 41, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 8, -0.5F, 20, -0.6F, 22.5F, -0.6F, 23.5F, 0.9F, 26, 0.8F, 34, 0.2F, 41, 0);
		a[DRIPS] += catmullRom(t, 0, 0, 8, 1.0F, 20, 1.0F, 25, 0.3F, 41, 0);
		a[BOW] += catmullRom(t, 0, 0, 16, -0.1F, 18, -0.12F, 20.5F, 0.3F, 22, 0.35F, 25, 0.05F, 28, -0.1F, 34, 0, 41, 0);
		a[TWIST] += catmullRom(t, 0, 0, 16, 0.2F, 18, 0.22F, 20.5F, -0.2F, 23, -0.25F, 28, 0.05F, 41, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 16, 0.25F, 21, 0.45F, 26, 0.3F, 41, 0);
		a[HEAD] += catmullRom(t, 0, 0, 16, -0.1F, 21, 0.05F, 41, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 18, 0, 21, -0.3F, 26, -0.25F, 34, -0.05F, 41, 0);
		a[LIFT_R] += HemoEasing.window(t, 18, 19, 19.5F, 21) * 0.6F;
	}

	/**
	 * 20 / 30 / 18. The hood tips back and the face vines part as spore motes gather in the hollow, the chest rising
	 * and the arms drawing out; at 20 the head is thrown forward and the chest heaves the cloud out, the head sweeping
	 * the cone, the body heaving through the exhale; it closes with a cough and settles.
	 */
	static void rotBreath(float[] a, float t) {
		a[HEAD] += catmullRom(t, 0, 0, 8, -0.35F, 16, -0.55F, 19, -0.62F, 20.5F, 0.05F, 22, 0.25F, 30, 0.18F, 40, 0.2F,
				50, 0.15F, 56, -0.05F, 62, 0.05F, 68, 0);
		a[HOOD] += catmullRom(t, 0, 0, 6, 0.4F, 12, 1.0F, 20, 1.1F, 50, 1.0F, 58, 0.2F, 68, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.2F, 18, -0.28F, 20.5F, 0.25F, 23, 0.35F, 50, 0.3F, 56, 0.1F, 68, 0)
				+ HemoEasing.window(t, 20, 24, 46, 50) * Mth.sin((t - 20) * 0.45F) * 0.06F;
		a[SPREAD] += catmullRom(t, 0, 0, 12, 0.2F, 19, 0.25F, 21, -0.1F, 50, -0.05F, 68, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 18, 0.1F, 22, 0.35F, 50, 0.3F, 68, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 19, 0, 21, -0.15F, 50, -0.12F, 68, 0);
		a[HEAD_TURN] += HemoEasing.window(t, 22, 26, 44, 50) * Mth.sin((t - 22) * 0.2F) * 0.25F;
		a[SHAKE] += HemoEasing.window(t, 54, 55, 57, 60) * 0.5F;
	}

	/**
	 * 30 / 36 / 34. The eight sacs swell one after another, three ticks apart and out of rhythm, the body hunching
	 * deeper with each and trembling harder; at 30 they burst together, the chest snapping up and the arms flung out,
	 * then the body sags forward over the split sacs, heaving (Spent Sacs takes the recovery).
	 */
	static void sacSwell(float[] a, float t) {
		for (int i = 0; i < 8; i++) {
			float at = i * 3.0F;
			a[SAC + i] += catmullRom(t, at, 0, at + 2.5F, 0.6F, at + 4.5F, 0.48F, 29.5F, 0.55F, 31, -0.15F, 36, -0.05F, 100, 0);
		}
		a[SAC_SPENT] += catmullRom(t, 0, 0, 29.5F, 0, 31, 1.0F, 70, 1.0F, 90, 0.3F, 100, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, 0.12F, 12, 0.24F, 18, 0.36F, 24, 0.46F, 29.5F, 0.52F, 31, 0.18F, 32.5F, 0.12F,
				36, 0.3F, 50, 0.42F, 70, 0.45F, 90, 0.15F, 100, 0)
				+ HemoEasing.window(t, 36, 42, 80, 92) * Mth.sin((t - 36) * 0.25F) * 0.05F;
		a[CROUCH] += catmullRom(t, 0, 0, 29, 0.5F, 31, 0.2F, 36, 0.45F, 70, 0.5F, 90, 0.15F, 100, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 29, -0.15F, 31, 0.55F, 34, 0.4F, 42, 0.05F, 100, 0);
		a[HEAD] += catmullRom(t, 0, 0, 29, 0.25F, 31, -0.3F, 34, -0.2F, 42, 0.35F, 70, 0.4F, 90, 0.1F, 100, 0);
		a[COLONY] += catmullRom(t, 0, 0, 29, 0.2F, 31, -0.25F, 36, 0, 100, 0);
		a[SHAKE] += HemoEasing.window(t, 6, 24, 29, 30.5F) * 0.6F;
	}

	/**
	 * 24 / 4n+4 / 16. The back colony's shelves flex up together and the staff is raised to point at the quarry; at 24
	 * the shelves snap flat with each launch, four ticks apart, the body jerking with each, then settle.
	 */
	static void shelfVolley(float[] a, float t, int discs) {
		float end = 24.0F + 4.0F * discs + 4.0F, total = end + 16.0F;
		a[COLONY] += catmullRom(t, 0, 0, 10, 0.35F, 22, 0.42F, end - 2, 0.38F, end + 6, 0.05F, total, 0);
		float launches = 0.0F;
		for (int i = 0; i < discs; i++) launches += HemoEasing.pulse(t - (24.0F + 4.0F * i), 0.5F, 3.5F);
		a[COLONY] -= launches * 0.5F;
		a[BOW] += launches * 0.1F;
		a[SAC_SPENT] += launches * 0.1F;
		a[STAFF_PLANT] -= HemoEasing.window(t, 4, 9, end + 2, end + 10);
		a[STAFF_TILT] += catmullRom(t, 0, 0, 8, 0.6F, 12, 1.15F, end, 1.1F, end + 8, 0.3F, total, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 12, -1.5F, end, -1.5F, total, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 12, -4.0F, end, -4.0F, end + 8, -1.0F, total, 0);
		a[HEAD] += catmullRom(t, 0, 0, 12, -0.15F, end, -0.15F, total, 0);
		a[BOW] += catmullRom(t, 0, 0, 12, -0.08F, end, -0.08F, total, 0);
		a[SHAKE] += HemoEasing.window(t, 6, 12, 22, 24) * 0.35F;
	}

	/**
	 * 26 / 8 / 18. The roots under the robe pull taut and the body leans back against them, the palm closing, straining
	 * and trembling through the wind-up; at 26 it snaps forward, the roots released, the palm thrust down and out.
	 */
	static void rootSnare(float[] a, float t) {
		a[ROOTS] += catmullRom(t, 0, 0, 6, 1.0F, 25, 1.1F, 26.5F, -0.3F, 30, -0.2F, 40, 0, 52, 0);
		a[BOW] += catmullRom(t, 0, 0, 6, -0.25F, 20, -0.34F, 25, -0.38F, 26.5F, 0.28F, 28, 0.42F, 33, 0.3F, 42, 0.08F, 52, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 6, 0.35F, 25, 0.45F, 27, 0.6F, 33, 0.5F, 52, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 6, 0.2F, 25, 0.25F, 27, -0.6F, 30, -0.7F, 40, -0.2F, 52, 0);
		a[PALM] += catmullRom(t, 0, 0, 20, 0, 26, 1.4F, 34, 1.4F, 46, 0.2F, 52, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 6, 0.6F, 25, 0.8F, 26.5F, -0.6F, 32, -0.4F, 52, 0);
		a[HEAD] += catmullRom(t, 0, 0, 6, -0.2F, 25, -0.25F, 27, 0.15F, 52, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 6, 0.2F, 25, 0.2F, 27, -0.15F, 40, -0.05F, 52, 0);
		a[SHAKE] += HemoEasing.window(t, 6, 12, 24, 26) * 0.5F;
	}

	/** An angle brought into (-pi, pi]: a spin that ends on a whole turn shows nothing when its track ends. */
	static float wrapped(float angle) {
		return angle - Mth.TWO_PI * Math.round(angle / Mth.TWO_PI);
	}

	/**
	 * 22 / 40 / 26. The staff rises overhead and starts to turn, one slow accelerating turn through the wind-up, the
	 * censers swinging out on their chains; through the active span the staff turns two revolutions (the censers on
	 * the footprint's two arcs, front and back at the commit), the body swaying under it; then it slows, the censers
	 * swing in and the staff comes down and plants.
	 */
	static void censerOrbit(float[] a, float t) {
		float spin = catmullRom(t, 0, 0, 4, 0, 22, 2.5F * Mth.PI, 62, 6.5F * Mth.PI, 80, 8.0F * Mth.PI, 88, 8.0F * Mth.PI);
		a[STAFF_SPIN] += wrapped(spin);
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 6, 74, 84);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 10, -4.0F, 70, -4.0F, 84, 0, 88, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 10, -7.0F, 70, -7.0F, 84, 0, 88, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 10, 0.5F, 70, 0.5F, 84, 0, 88, 0);
		a[CENSER_OUT] += catmullRom(t, 0, 0, 8, 0.1F, 22, 0.9F, 30, 1.15F, 62, 1.15F, 72, 0.4F, 80, 0.1F, 88, 0);
		float active = HemoEasing.window(t, 18, 24, 60, 68);
		a[TWIST] += Mth.sin(spin) * 0.08F * active;
		a[LEAN] += Mth.cos(spin) * 0.05F * active;
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.3F, 70, -0.3F, 88, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.2F, 70, -0.18F, 88, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.2F, 70, 0.25F, 88, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 12, 0.3F, 70, 0.3F, 88, 0);
	}

	/**
	 * 16 / 60 / 20. Down on one knee, bowed deep, the palm turned over and pressed down at the floor, its drips running
	 * down into the moss (the arm is too short to lay the hand itself flat); through the communion it breathes slowly
	 * with the shelves, the sacs swelling and easing in turn; then it rises.
	 */
	static void mouldCommunion(float[] a, float t) {
		float down = HemoEasing.window(t, 0, 10, 80, 92);
		float breathe = HemoEasing.window(t, 16, 22, 70, 76) * Mth.sin((t - 16) * 0.12F);
		a[KNEEL] += down;
		a[CROUCH] += HemoEasing.pulse(t - 9, 1.0F, 6.0F) * 0.4F;
		a[PALM_FLOOR] += HemoEasing.window(t, 6, 16, 74, 84);
		a[PALM] += catmullRom(t, 0, 0, 8, 1.5F, 16, 2.0F, 76, 2.0F, 86, 0.4F, 96, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, 0.7F, 16, 0.85F, 76, 0.8F, 88, 0.15F, 96, 0) + breathe * 0.05F;
		a[HIP_X] += 0.25F * down;
		a[HEAD] += catmullRom(t, 0, 0, 10, 0.3F, 76, 0.35F, 96, 0);
		a[HOOD] += catmullRom(t, 0, 0, 16, 0.5F, 76, 0.5F, 96, 0);
		a[COLONY] += breathe * 0.15F;
		for (int i = 0; i < 8; i++)
			a[SAC + i] += HemoEasing.window(t, 16, 22, 70, 76) * Mth.sin((t - 16) * 0.12F - i * 0.5F) * 0.12F;
		a[STAFF_FOOT_X] += 1.5F * down;
	}

	/**
	 * 50 / 60 / 40. Down onto both knees in the colony, arms spread low, palm turned to the floor; every sac and shelf
	 * swells and the body shudders as the mould sheet spreads; at 50, 70 and 90 the back heaves with each pulse, the
	 * colony snapping up and the sacs swelling; then it slumps (Spent Bloom takes the recovery).
	 */
	static void bloomOfRot(float[] a, float t) {
		float pulses = HemoEasing.pulse(t - 50, 1.2F, 8) + HemoEasing.pulse(t - 70, 1.2F, 8) + HemoEasing.pulse(t - 90, 1.2F, 8);
		a[KNEEL_BOTH] += HemoEasing.window(t, 0, 24, 126, 144);
		a[BOW] += catmullRom(t, 0, 0, 24, 0.5F, 40, 0.55F, 48, 0.4F, 110, 0.45F, 130, 0.65F, 144, 0.2F, 150, 0) - pulses * 0.3F;
		a[COLONY] += pulses * 0.45F;
		for (int i = 0; i < 8; i++) a[SAC + i] += pulses * 0.4F;
		a[BLOOM] += catmullRom(t, 0, 0, 30, 0.2F, 50, 1.0F, 110, 1.0F, 130, 0.3F, 150, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 24, 0.35F, 50, 0.45F, 110, 0.3F, 150, 0);
		a[PALM] += catmullRom(t, 0, 0, 20, 2.0F, 130, 2.0F, 150, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 24, -0.3F, 110, -0.3F, 150, 0);
		a[HEAD] += catmullRom(t, 0, 0, 24, 0.3F, 40, 0.1F, 50, -0.2F, 110, 0.4F, 150, 0) - pulses * 0.15F;
		a[SHAKE] += HemoEasing.window(t, 30, 44, 49, 51) * 0.6F + pulses * 0.4F;
	}

	/**
	 * 30 / 6 / 20. The rot stills and the body shakes, harder and harder, drawing in on itself (the palm pressed to the
	 * chest, the head bowed) while the growths blacken and the chest straightens; at 30 everything flings open, arms
	 * wide, the head thrown back, up onto the toes, past open, and it settles into the purifier's stance.
	 */
	static void ignition(float[] a, float t) {
		a[SHAKE] += HemoEasing.window(t, 0, 10, 28, 31) * (0.5F + 0.7F * HemoEasing.ramp(t, 0, 28));
		// the body answers each region as the fire takes it (revision R1.2): the chest tears catch at 16 and it jerks;
		// the shoulders at 18 and they hunch up against the heat; the back at 20, arching off it, the colony's shelves
		// flaring up; the crown's spires light one a tick from 24 and the head ticks up with each; the commit's flare
		// settles with a breath, dipping at 36
		float chest = HemoEasing.pulse(t - 16, 1.0F, 6.0F);
		float shoulders = HemoEasing.pulse(t - 18, 1.0F, 7.0F);
		float back = HemoEasing.pulse(t - 20, 1.5F, 8.0F);
		float crown = 0.0F;
		for (int i = 0; i < 6; i++) crown += HemoEasing.pulse(t - 24 - i, 0.5F, 2.5F);
		float breath = HemoEasing.pulse(t - 33, 3.0F, 9.0F);
		a[BOW] += chest * 0.07F - back * 0.08F + breath * 0.05F;
		a[L_ARM_X] -= shoulders * 0.1F;
		a[R_FORE] -= shoulders * 0.12F;
		a[COLONY] += back * 0.3F;
		a[HEAD] -= crown * 0.03F;
		a[CROUCH] += breath * 0.06F;
		a[SHAKE] += (chest + shoulders + back) * 0.2F;
		a[SPREAD] += catmullRom(t, 0, 0, 10, -0.15F, 26, -0.35F, 29, -0.4F, 30.5F, 0.7F, 32, 1.1F, 35, 1.15F, 42, 0.7F,
				50, 0.2F, 56, 0);
		a[BOW] += catmullRom(t, 0, 0, 12, 0.2F, 28, 0.32F, 30.5F, -0.2F, 33, -0.38F, 40, -0.15F, 56, 0);
		a[HEAD] += catmullRom(t, 0, 0, 14, 0.3F, 28, 0.4F, 30.5F, -0.3F, 33, -0.5F, 42, -0.2F, 56, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 12, -0.5F, 28, -0.6F, 31, 0.1F, 56, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 12, 0.6F, 28, 0.8F, 30.5F, -0.7F, 40, -0.3F, 56, 0);
		a[TIPTOE] += catmullRom(t, 0, 0, 29, 0, 31, 0.5F, 36, 0.4F, 50, 0, 56, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 12, 0.3F, 28, 0.4F, 30.5F, 0, 34, 0.1F, 56, 0);
		a[HOOD] += catmullRom(t, 0, 0, 28, 0.2F, 31, 1.0F, 40, 0.6F, 56, 0);
	}

	/**
	 * 20 / 6 / 12. The censers flare and the staff is drawn back low behind him, the weight sinking onto the back foot;
	 * at 20 it is brought over and down along the lane in one long reach, the left foot lunging, the censers flung out
	 * ahead; it holds at full reach, then he hauls it back up and plants it.
	 */
	static void purgingSweep(float[] a, float t) {
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 5, 30, 36);
		a[STAFF_TILT] += catmullRom(t, 0, 0, 6, -0.5F, 14, -0.85F, 18, -0.95F, 20, -0.6F, 21.5F, 0.5F, 23, 1.3F, 26, 1.45F,
				30, 1.0F, 35, 0.3F, 38, 0);
		a[CENSER_FLING] += catmullRom(t, 0, 0, 10, 0.3F, 18, 0.4F, 21, -0.2F, 23, 0.9F, 27, 1.0F, 32, 0.3F, 38, 0);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 18, -2.0F, 24, -3.0F, 38, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 18, 1.5F, 23, -1.0F, 38, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 8, 2.5F, 18, 3.0F, 21, -2.0F, 24, -5.0F, 30, -3.0F, 38, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.1F, 18, -0.15F, 21, 0.35F, 24, 0.55F, 30, 0.3F, 38, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.3F, 18, 0.45F, 21.5F, 0.5F, 24, 0.7F, 30, 0.4F, 38, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 18, 0.1F, 21, -0.4F, 26, -0.38F, 34, -0.1F, 38, 0);
		a[LIFT_L] += HemoEasing.window(t, 18.5F, 19.5F, 20.5F, 22) * 0.7F;
		a[STEP_R] += catmullRom(t, 0, 0, 18, 0.05F, 24, 0.2F, 38, 0);
		a[TWIST] += catmullRom(t, 0, 0, 14, 0.25F, 19, 0.3F, 22, -0.15F, 26, -0.2F, 38, 0);
		a[HEAD] += catmullRom(t, 0, 0, 18, 0.1F, 22, -0.1F, 38, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 18, 0.2F, 23, 0.45F, 32, 0.2F, 38, 0);
	}

	/**
	 * 22 / 4 / 18. The staff goes up overhead and the left censer is whirled round it twice on its chain, trailing
	 * embers, the body swaying with it; at 22 the staff chops forward and the censer is thrown out at the quarry, a
	 * step following it; the chain snaps taut at 26 and jerks him, and it is reeled back in.
	 */
	static void censerCast(float[] a, float t) {
		float spin = catmullRom(t, 0, 0, 3, 0, 22, 3.5F * Mth.PI, 30, 4.0F * Mth.PI, 44, 4.0F * Mth.PI);
		a[STAFF_SPIN] += wrapped(spin);
		a[STAFF_PLANT] -= HemoEasing.window(t, 1, 5, 34, 42);
		a[L_GRIP_X] += catmullRom(t, 0, 0, 6, -3.5F, 20, -3.5F, 23, -4.0F, 30, -3.5F, 40, 0, 44, 0);
		a[L_GRIP_Y] += catmullRom(t, 0, 0, 6, -7.0F, 20, -7.0F, 23, -3.0F, 30, -2.0F, 44, 0);
		a[L_GRIP_Z] += catmullRom(t, 0, 0, 6, 0, 20, 0, 23, -4.0F, 30, -4.0F, 44, 0);
		a[STAFF_TILT] += catmullRom(t, 0, 0, 6, 0, 20, -0.1F, 21.5F, -0.25F, 22.5F, 0.4F, 24, 1.0F, 27, 1.05F, 33, 0.7F,
				40, 0.15F, 44, 0);
		a[CAST] += catmullRom(t, 0, 0, 4, 0.2F, 12, 0.9F, 21, 1.25F, 22.5F, 0.6F, 24, 0, 44, 0);
		a[CAST_THROW] += catmullRom(t, 0, 0, 21, 0, 22.5F, 0.8F, 24, 1.0F, 28, 1.0F, 36, 0.4F, 42, 0, 44, 0);
		a[BOW] += catmullRom(t, 0, 0, 8, -0.2F, 20, -0.25F, 23, 0.35F, 26, 0.45F, 33, 0.2F, 44, 0)
				- HemoEasing.pulse(t - 26, 0.5F, 4.0F) * 0.2F;
		a[TWIST] += HemoEasing.window(t, 4, 8, 18, 22) * Mth.sin(spin) * 0.1F
				+ catmullRom(t, 0, 0, 20, 0.2F, 23, -0.2F, 30, -0.1F, 44, 0);
		a[STEP_L] += catmullRom(t, 0, 0, 20, 0, 23, -0.3F, 30, -0.25F, 40, -0.05F, 44, 0);
		a[LIFT_L] += HemoEasing.window(t, 20, 21, 22, 23.5F) * 0.6F;
		a[HEAD] += catmullRom(t, 0, 0, 8, -0.2F, 20, -0.2F, 23, 0.05F, 44, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 8, 0.2F, 20, 0.2F, 24, 0, 44, 0);
	}

	/**
	 * 26 / 24 / 20. The palm turns down and is drawn across the body to the left, low, the body coiling after it; at
	 * 26 it sweeps low across to the right as the fire runs, the right foot stepping out, and holds there over the
	 * lane, trembling with the heat, before it rises.
	 */
	static void cauterisingLane(float[] a, float t) {
		a[PALM] += catmullRom(t, 0, 0, 6, 1.0F, 10, 2.0F, 60, 2.0F, 66, 0.4F, 70, 0);
		a[R_ARM_Y] += catmullRom(t, 0, 0, 10, -0.5F, 22, -0.85F, 25, -0.9F, 26.5F, -0.3F, 28, 0.4F, 31, 0.75F, 40, 0.7F,
				50, 0.6F, 60, 0.3F, 70, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -0.6F, 25, -0.7F, 28, -0.9F, 50, -0.85F, 62, -0.3F, 70, 0);
		a[R_ARM_Z] += catmullRom(t, 0, 0, 10, -0.2F, 25, -0.3F, 28, 0.15F, 50, 0.1F, 70, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, 0.15F, 25, 0.2F, 27, 0.5F, 32, 0.55F, 50, 0.5F, 62, 0.15F, 70, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 10, 0.3F, 25, 0.35F, 28, 0.7F, 50, 0.65F, 64, 0.1F, 70, 0);
		a[TWIST] += catmullRom(t, 0, 0, 10, -0.3F, 25, -0.4F, 27, 0.1F, 31, 0.35F, 50, 0.3F, 70, 0);
		a[STEP_R] += catmullRom(t, 0, 0, 25, 0, 28, -0.3F, 50, -0.3F, 64, -0.05F, 70, 0);
		a[LIFT_R] += HemoEasing.window(t, 25, 26, 26.5F, 28) * 0.6F;
		a[FINGERS] += catmullRom(t, 0, 0, 10, -0.5F, 60, -0.6F, 70, 0);
		a[SHAKE] += HemoEasing.window(t, 30, 34, 48, 52) * 0.25F;
	}

	/**
	 * 28 / 8 / 30. The body straightens and the arms open slowly, rising onto the toes; then draws in tight, arms
	 * clutched to the chest and knees bending, as the core brightens; at 28 it flares, the arms flung wide, the chest
	 * thrown out and the head back, shaking in the heat, and comes down out of it.
	 */
	static void pyreHeart(float[] a, float t) {
		a[SPREAD] += catmullRom(t, 0, 0, 10, 0.45F, 20, 0.5F, 25, -0.1F, 27.5F, -0.3F, 29, 0.6F, 30.5F, 1.2F, 34, 1.25F,
				38, 1.0F, 50, 0.4F, 66, 0);
		a[TIPTOE] += catmullRom(t, 0, 0, 10, 0.3F, 20, 0.4F, 25, 0, 30, 0.6F, 36, 0.5F, 50, 0, 66, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 20, 0, 26, 0.4F, 28, 0.42F, 30, 0, 66, 0);
		a[BOW] += catmullRom(t, 0, 0, 10, -0.1F, 20, -0.1F, 26, 0.25F, 28, 0.28F, 30, -0.3F, 34, -0.38F, 44, -0.15F, 66, 0);
		a[HEAD] += catmullRom(t, 0, 0, 10, -0.1F, 26, 0.2F, 28, 0.2F, 30, -0.4F, 34, -0.5F, 46, -0.2F, 66, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0, 20, -0.2F, 26, -0.5F, 28, -0.55F, 30, -0.1F, 66, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 20, -0.4F, 26, 0.8F, 28, 0.85F, 30, -0.8F, 40, -0.5F, 66, 0);
		a[HOOD] += catmullRom(t, 0, 0, 28, 0.2F, 30, 0.8F, 46, 0.5F, 66, 0);
		a[SHAKE] += HemoEasing.window(t, 20, 26, 34, 40) * 0.6F;
	}

	/**
	 * 30 / 20 / 24. The palm is raised high, open, the head following it up and the body rising onto the toes as the
	 * pyres answer; at 30 the fist closes and drags down to the chest, the body hunching over it as the lines run; held
	 * through the active span, then released.
	 */
	static void pyreLines(float[] a, float t) {
		a[R_ARM_X] += catmullRom(t, 0, 0, 10, -1.8F, 18, -2.4F, 30, -2.45F, 31.5F, -1.6F, 33, -0.9F, 40, -0.7F, 52, -0.6F,
				64, -0.2F, 74, 0);
		a[FINGERS] += catmullRom(t, 0, 0, 10, -0.6F, 18, -0.8F, 29, -0.85F, 30.5F, 0.4F, 31.5F, 1.0F, 34, 1.05F, 50, 0.95F,
				62, 0.3F, 74, 0);
		a[BOW] += catmullRom(t, 0, 0, 18, -0.2F, 29, -0.22F, 31, 0.3F, 34, 0.35F, 52, 0.3F, 66, 0.05F, 74, 0);
		a[HEAD] += catmullRom(t, 0, 0, 18, -0.45F, 29, -0.5F, 31, 0.1F, 40, 0.2F, 74, 0);
		a[TIPTOE] += catmullRom(t, 0, 0, 18, 0.3F, 29, 0.35F, 31, 0, 74, 0);
		a[SPREAD] += catmullRom(t, 0, 0, 18, 0.2F, 29, 0.2F, 31, -0.25F, 50, -0.2F, 74, 0);
		a[CROUCH] += catmullRom(t, 0, 0, 29, 0, 31, 0.35F, 50, 0.3F, 74, 0);
		a[SHAKE] += HemoEasing.window(t, 18, 22, 28, 30) * 0.6F;
	}

	/**
	 * 40 / 48 / 20. The fire goes white at the core and the body rises onto the toes with both arms out, the head back,
	 * shaking harder; at 40, 56 and 72 a ring goes out with a jolt through it, the heels dropping and the arms flung
	 * wider; then it comes down off the toes into the Offering.
	 */
	static void fullPurge(float[] a, float t) {
		float rings = HemoEasing.pulse(t - 40, 0.6F, 7) + HemoEasing.pulse(t - 56, 0.6F, 7) + HemoEasing.pulse(t - 72, 0.6F, 7);
		a[TIPTOE] += catmullRom(t, 0, 0, 16, 0.9F, 40, 1.0F, 88, 1.0F, 100, 0.2F, 108, 0) - rings * 0.4F;
		a[SPREAD] += catmullRom(t, 0, 0, 16, 1.0F, 40, 1.1F, 88, 1.05F, 100, 0.3F, 108, 0) + rings * 0.25F;
		a[HEAD] += catmullRom(t, 0, 0, 16, -0.35F, 40, -0.4F, 88, -0.35F, 104, 0, 108, 0);
		a[BOW] += catmullRom(t, 0, 0, 16, -0.2F, 40, -0.22F, 88, -0.2F, 108, 0) - rings * 0.18F;
		a[CROUCH] += rings * 0.25F;
		a[FINGERS] += catmullRom(t, 0, 0, 16, -0.6F, 88, -0.6F, 108, 0);
		a[HOOD] += catmullRom(t, 0, 0, 16, 0.6F, 88, 0.6F, 108, 0);
		a[SHAKE] += HemoEasing.window(t, 16, 36, 88, 96) * 0.5F + rings * 0.8F;
	}

	/**
	 * The Offering, 12 / 1 / its opening. Down onto the right knee with a jolt, the staff planted out to the side,
	 * the palm raised open to the brow (where the server's absolution aim reads it), the hood bowed; held, breathing,
	 * the palm trembling; it rises over the last eight ticks of {@code length}.
	 */
	static void offering(float[] a, float t, float length) {
		float down = HemoEasing.window(t, 0, 8, length - 8, length - 1);
		float held = HemoEasing.window(t, 4, 12, length - 10, length - 2);
		a[KNEEL] += down;
		a[CROUCH] += HemoEasing.pulse(t - 7, 1.0F, 6.0F) * 0.4F;
		a[PALM_OFFER] += held;
		a[R_ARM_X] -= 1.15F * held;
		a[R_FORE] -= 0.15F * held;
		a[FINGERS] -= 0.5F * held;
		a[BOW] += catmullRom(t, 0, 0, 6, 0.35F, 10, 0.2F, 14, 0.05F, length, 0.05F) * down + Mth.sin(t * 0.2F) * 0.025F * held;
		a[HEAD] += 0.2F * held;
		a[SHAKE] += 0.15F * held;
		a[STAFF_FOOT_X] += 1.0F * down;
	}

	/**
	 * The Embering, 12 / 1 / 60. The fire gutters and the body slumps onto both knees, head dropping, arms slack, the
	 * palm rolled over; as the growths rush back over it, it spasms three times; it rises over the last ten ticks.
	 */
	static void embering(float[] a, float t, float length) {
		float down = HemoEasing.window(t, 0, 7, length - 12, length - 2);
		float spasms = HemoEasing.pulse(t - 15, 1.0F, 7) + HemoEasing.pulse(t - 30, 1.0F, 7) + HemoEasing.pulse(t - 45, 1.0F, 7);
		a[KNEEL_BOTH] += down;
		a[BOW] += catmullRom(t, 0, 0, 5, 0.6F, 8, 0.45F, 12, 0.55F, length, 0.55F) * down + spasms * 0.2F * down;
		a[HEAD] += 0.5F * down;
		a[R_ARM_X] += 0.35F * down;
		a[PALM] += 1.3F * down;
		a[SPREAD] -= 0.1F * down;
		a[COLONY] += spasms * 0.35F;
		for (int i = 0; i < 8; i++) a[SAC + i] += spasms * 0.3F;
		a[SHAKE] += spasms * 0.6F;
		a[STAFF_FOOT_X] += 2.0F * down;
	}

	/** Each opening's body over the stance, from the moment it opens, for {@code length} ticks. */
	static void opening(float[] a, int opening, float t, float length) {
		float w = HemoEasing.window(t, 0, 2, length - 10, length);
		if (opening == PutricielOpenings.SPENT_SACS_ID) {
			// sagging forward over the split sacs, heaving, the hood hanging and rolling
			a[SAC_SPENT] += HemoEasing.window(t, 0, 1, length - 10, length);
			a[BOW] += 0.45F * w + Mth.sin(t * 0.25F) * 0.05F * w;
			a[HEAD] += 0.4F * w;
			a[CROUCH] += 0.45F * w;
			a[R_ARM_X] += 0.3F * w;
			a[FINGERS] += 0.5F * w;
			a[SPREAD] -= 0.1F * w;
			a[HEAD_ROLL] += Mth.sin(t * 0.08F) * 0.08F * w;
		} else if (opening == PutricielOpenings.FOULED_CHAIN_ID) {
			// the censer fouled in the floor; he hauls on the chain in jerks and tears it free at the end
			float haul = Math.max(0.0F, Mth.sin(t * 0.9F));
			a[STUCK] += HemoEasing.window(t, 0, 1, length - 6, length - 3);
			a[STAFF_PLANT] -= HemoEasing.window(t, 0, 1, length - 4, length);
			a[STAFF_TILT] += (1.8F - 0.15F * haul) * w;
			a[L_GRIP_Y] += 3.5F * w;
			a[L_GRIP_Z] += (-4.5F + haul * 2.5F) * w;
			a[BOW] += (0.3F - 0.25F * haul) * w;
			a[CROUCH] += (0.5F + 0.1F * haul) * w;
			a[STEP_R] += 0.2F * w;
			a[STEP_L] -= 0.15F * w;
			a[SHAKE] += 0.3F * w;
			float free = HemoEasing.pulse(t - (length - 5), 1.0F, 6.0F);
			a[BOW] -= free * 0.3F;
			a[STEP_R] += free * 0.2F;
		} else if (opening == PutricielOpenings.BROKEN_COMMUNION_ID) {
			// a jerk upright as the communion breaks, the palm torn off the floor, then a stagger back two steps
			a[KNEEL] += catmullRom(t, 0, 1.0F, 3, 0.4F, 8, 0);
			a[PALM_FLOOR] += catmullRom(t, 0, 1.0F, 1.5F, 0);
			a[BOW] += catmullRom(t, 0, 0.5F, 1.5F, -0.35F, 4, -0.25F, 8, 0.1F, 16, 0.05F, length, 0);
			a[HEAD] += catmullRom(t, 0, 0.3F, 1.5F, -0.4F, 5, -0.1F, length, 0);
			a[SPREAD] += catmullRom(t, 0, 0, 1.5F, 0.7F, 6, 0.3F, 20, 0, length, 0);
			a[STEP_L] += catmullRom(t, 0, 0, 4, 0, 8, 0.35F, 12, 0.3F, 22, 0.05F, length, 0);
			a[LIFT_L] += HemoEasing.window(t, 4, 6, 7, 9) * 0.7F;
			a[STEP_R] += catmullRom(t, 0, 0, 10, 0, 14, 0.3F, 18, 0.25F, 28, 0, length, 0);
			a[LIFT_R] += HemoEasing.window(t, 10, 12, 13, 15) * 0.7F;
			a[LEAN] += catmullRom(t, 0, 0, 6, -0.12F, 12, 0.1F, 20, -0.05F, 30, 0, length, 0);
			a[SHAKE] += HemoEasing.window(t, 0, 0.5F, 8, 20) * 0.7F;
			a[SAC_SPENT] += HemoEasing.pulse(t, 0.5F, 6) * 0.3F;
		} else if (opening == PutricielOpenings.SPENT_BLOOM_ID) {
			// slumped in the colony on both knees, sagging with each breath; it hauls itself up at the end
			float down = HemoEasing.window(t, 0, 1, length - 12, length - 2);
			a[KNEEL_BOTH] += down;
			a[BOW] += 0.65F * down + Mth.sin(t * 0.18F) * 0.05F * down;
			a[HEAD] += 0.55F * down;
			a[SPREAD] += 0.2F * down;
			a[PALM] += 2.0F * down;
			a[R_ARM_X] -= 0.2F * down;
			a[COLONY] -= 0.15F * down;
			a[HEAD_ROLL] += Mth.sin(t * 0.07F) * 0.08F * down;
		} else if (opening == PutricielOpenings.OFFERING_ID) {
			offering(a, t + 12.0F, length + 12.0F);
		} else if (opening == PutricielOpenings.EMBERING_ID) {
			embering(a, t + 12.0F, length + 12.0F);
		}
	}

	/**
	 * Beneath an accepted Offering, 18 ticks: the chest thrown back and the palm jerked up as the nodule lights, the
	 * hood snapping back, a shudder, then up off the knee.
	 */
	static void recoil(float[] a, float t) {
		a[KNEEL] += HemoEasing.window(t, -1, 0, 6, 15);
		a[PALM_OFFER] += HemoEasing.window(t, -1, 0, 1, 4);
		a[BOW] += catmullRom(t, 0, 0.05F, 1.5F, -0.4F, 4, -0.32F, 10, -0.05F, 18, 0);
		a[R_ARM_X] += catmullRom(t, 0, -1.15F, 1.5F, -1.6F, 4, -1.4F, 12, -0.3F, 18, 0);
		a[R_FORE] += catmullRom(t, 0, 0, 1.5F, -0.4F, 8, 0, 18, 0);
		a[HEAD] += catmullRom(t, 0, 0.2F, 1.5F, -0.35F, 6, -0.2F, 18, 0);
		a[FINGERS] += catmullRom(t, 0, -0.5F, 1.5F, -0.9F, 8, -0.3F, 18, 0);
		a[SHAKE] += HemoEasing.window(t, 0, 0.5F, 4, 10) * 0.6F;
	}

	/**
	 * The rise, 60 ticks (the kit's sink lifts the body out of the tomb): it comes up bowed deep, hood down, arms
	 * hanging, the shelves still folded; as it surfaces the shelves unfurl (the growth, 30 to 60), and at 44 it heaves
	 * upright with a shudder, the hood lifting to the room, and settles into the rot stance.
	 */
	static void rise(float[] a, float t) {
		a[BOW] += catmullRom(t, 0, 0.7F, 30, 0.6F, 44, 0.5F, 50, -0.1F, 54, 0.05F, 60, 0);
		a[HEAD] += catmullRom(t, 0, 0.6F, 36, 0.5F, 46, -0.2F, 52, 0.05F, 60, 0);
		a[CROUCH] += catmullRom(t, 0, 0.6F, 44, 0.5F, 52, 0.1F, 60, 0);
		a[SPREAD] += catmullRom(t, 0, -0.2F, 44, -0.2F, 48, 0.3F, 54, 0.1F, 60, 0);
		a[R_ARM_X] += catmullRom(t, 0, 0.4F, 44, 0.3F, 52, 0, 60, 0);
		a[COLONY] += catmullRom(t, 0, -0.3F, 30, -0.3F, 56, 0.15F, 60, 0);
		a[SHAKE] += HemoEasing.window(t, 40, 46, 50, 56) * 0.6F;
	}

	/**
	 * The ending, 101 ticks (section 11). The rot stills and the body goes down onto both knees with the palm still
	 * raised; through the burn (20 to 50) the chest lifts and the hood tips back as the fire climbs, and from 26 to 46
	 * it holds utterly still while the man's face shows; as the fire dies it slackens, the hood bowing and the palm
	 * lowering; through the sink it bows deep and the staff leans away.
	 */
	static void ending(float[] a, float t) {
		a[KNEEL_BOTH] += HemoEasing.ramp(t, 0, 16);
		a[CROUCH] += HemoEasing.pulse(t - 14, 1.0F, 6.0F) * 0.4F;
		a[R_ARM_X] += catmullRom(t, 0, 0, 8, -0.8F, 20, -1.0F, 50, -1.05F, 62, -0.8F, 75, -0.35F, 100, -0.15F);
		a[BOW] += catmullRom(t, 0, 0, 8, 0.25F, 20, 0.2F, 30, -0.15F, 46, -0.2F, 56, 0.15F, 70, 0.35F, 100, 0.65F);
		a[HEAD] += catmullRom(t, 0, 0, 8, 0.2F, 20, 0.15F, 28, -0.35F, 46, -0.38F, 56, 0.2F, 75, 0.4F, 100, 0.55F);
		a[SPREAD] += catmullRom(t, 0, 0, 20, 0, 28, 0.25F, 46, 0.25F, 60, 0, 100, -0.1F);
		a[FINGERS] += catmullRom(t, 0, -0.3F, 46, -0.6F, 70, 0.3F, 100, 0.5F);
		a[HOOD] += catmullRom(t, 0, 0, 22, 0.2F, 28, 0.8F, 46, 0.8F, 60, 0.2F, 100, 0);
		a[STAFF_FOOT_X] += catmullRom(t, 0, 0, 60, 0, 90, 2.5F, 100, 3.0F);
		a[SHAKE] = a[SHAKE] * (1.0F - HemoEasing.window(t, 20, 26, 46, 50)) + HemoEasing.window(t, 0, 2, 16, 22) * 0.5F
				+ HemoEasing.window(t, 50, 54, 60, 70) * 0.3F;
	}

	// ---------------------------------------------------------------- the overlap

	/**
	 * The springs' pass, after the blend: what trails the motion. In the rot the springs are loose and wet, in the fire
	 * tighter. Each value is how far a part trails what carries it.
	 */
	static void secondary(float[] a, PutricielAnimState m) {
		float fire = m.fire();
		float freq = HemoEasing.lerp(0.42F, 0.6F, fire), damping = HemoEasing.lerp(0.3F, 0.45F, fire);
		float spine = a[BOW] + a[HIP_X];
		float speed = m.speed();
		float kneel = a[KNEEL] + a[KNEEL_BOTH];
		// the hood trails the chest, the chest dips on through a sudden crouch or kneel
		a[LAG_HEAD_X] = trail(m.lag(0, spine + a[CROUCH] * 0.15F, freq * 1.1F, damping + 0.1F) * 0.5F);
		a[LAG_HEAD_Y] = trail(m.lag(1, a[TWIST] + a[HIP_TWIST], freq, damping + 0.1F) * 0.55F);
		a[LAG_HEAD_Z] = trail(m.lag(2, a[LEAN] + a[HIP_SWAY], freq, damping) * 0.5F);
		a[LAG_SPINE] = trail(-m.lag(3, a[CROUCH] * 0.5F + kneel * 0.4F, freq * 0.9F, damping) * 0.35F);
		// the palm arm trails its swing and whips through when it stops; the staff arm gives under the staff
		a[LAG_FORE_R] = trail(m.lag(4, a[R_ARM_X] + a[R_ARM_Y] * 0.5F, freq * 1.3F, damping) * 0.45F);
		a[LAG_FINGERS] = trail(m.lag(5, a[R_ARM_X] + a[R_FORE], freq * 1.6F, damping) * 0.35F);
		a[LAG_FORE_L] = trail(m.lag(6, (a[STAFF_AZ] + a[STAFF_TILT]) * 0.3F + a[L_ARM_X], freq * 1.2F, damping) * 0.3F);
		// the vestment chains swing off the chest and the walk, their tips trailing their roots
		a[VEST_X] = trail(-m.lag(7, spine * 0.8F + a[CROUCH] * 0.1F, 0.3F, 0.22F) * 1.1F + m.follow(8, speed * 2.5F, 0.32F, 0.3F));
		a[VEST_Z] = trail(-m.lag(9, a[LEAN] + a[HIP_SWAY] + a[TWIST] * 0.3F, 0.3F, 0.25F)
				- m.lag(10, m.yawTravel(), 0.3F, 0.25F) * 0.8F);
		a[VEST_BEND_X] = trail(m.lag(11, a[VEST_X], 0.55F, 0.3F) * 0.9F);
		a[VEST_BEND_Z] = trail(m.lag(12, a[VEST_Z], 0.55F, 0.3F) * 0.9F);
		// the robe trails the walk and settles forward when it stops; the mantles bounce
		a[ROBE_X] = trail(m.follow(13, speed * 1.6F, 0.35F, 0.32F) - m.lag(14, a[CROUCH] * 0.3F + a[HIP_X], 0.4F, 0.3F) * 0.5F);
		a[ROBE_Z] = trail(-m.lag(15, a[HIP_SWAY] + a[HIP_TWIST], 0.35F, 0.3F) * 0.8F);
		a[MANTLE] = trail(-m.lag(16, spine + a[SPREAD] * 0.3F, 0.7F, 0.25F) * 0.6F);
		// the growths: shelves wobble, sacs jiggle, the crown's spires and mushroom sway, the drips swing
		a[COLONY_JIG] = trail(-m.lag(17, spine + a[CROUCH] * 0.4F + a[COLONY], 0.75F, 0.2F) * 0.5F);
		a[SAC_JIG] = trail(-m.lag(18, spine + a[CROUCH] * 0.5F + a[LEAN], 0.9F, 0.18F) * 2.0F);
		a[CROWN_JIG] = trail(-m.lag(19, a[HEAD] + a[LAG_HEAD_X] + a[HEAD_TURN] + a[LEAN], 0.6F, 0.22F) * 0.5F);
		a[DRIP_X] = trail(-m.lag(20, a[R_ARM_X] + a[R_FORE] + spine, 0.4F, 0.2F) * 0.8F);
		a[DRIP_Z] = trail(-m.lag(21, a[R_ARM_Y] + a[R_ARM_Z] + a[LEAN] + a[TWIST], 0.4F, 0.2F) * 0.8F);
		// the roots drag round after a turn
		a[ROOT_DRAG] = trail(m.lag(22, m.yawTravel(), 0.18F, 0.5F) * 0.9F);
		a[FLINCH] = trail(m.flinch());
	}

	private static float trail(float value) {
		return Mth.clamp(value, -TRAIL_LIMIT, TRAIL_LIMIT);
	}

	// ---------------------------------------------------------------- laying it on the rig

	private ModelPart[] sacParts, backShelves, hemShelves, shoulderShelves, crownSpires, growthMushrooms, rotDrips, robes,
			rootTrails, offeringFingers, offeringTips;
	private ModelPart[][] vestLinks;
	private float[][] vestRest;
	/** The staff's length from the grip to its iron foot, and where that foot stands at rest (model units). */
	float staffLength = Float.NaN;
	final org.joml.Vector3f staffFootRest = new org.joml.Vector3f();
	/** The legs' contact depth at rest (the soles on the floor), each censer chain's length, each robe panel's. */
	private float restContact;
	private final float[] chainLength = new float[2];
	private final float[] robeLength = new float[ROBE_PANELS];
	private float footRestL, footRestR;
	/** The longest offering drip's reach below the hand, model units. */
	private float dripLength;
	/** The generated fire tables' parts (revision R1): each anchor's chain, each growth, its ancestors and its tree, each region's. */
	private ModelPart[][] anchorChains, growthChains, growthTrees, regionParts;
	private ModelPart[] growthPartList, allParts;
	/** Each rot drip's growth (its mushroom), as an index into the growth table. */
	private final int[] dripGrowth = new int[4];
	/** This frame's fire scale of each growth part. */
	private final float[] growthBurn = new float[GROWTH_COUNT];

	/** Finds the parts the pass works through once, and measures the rig at rest. */
	private void bind() {
		sacParts = new ModelPart[]{spore0, spore1, spore2, spore3, shoulderSpore, hemSpore, backSpore0, backSpore1};
		backShelves = new ModelPart[11];
		for (int i = 0; i < 11; i++) backShelves[i] = backColony.getChild("back_shelf_" + i);
		hemShelves = new ModelPart[6];
		for (int i = 0; i < 6; i++) hemShelves[i] = hips.getChild("hem_shelf_" + i);
		shoulderShelves = new ModelPart[8];
		for (int i = 0; i < 3; i++) shoulderShelves[i] = chest.getChild("left_shoulder_shelf_" + i);
		for (int i = 0; i < 5; i++) shoulderShelves[3 + i] = chest.getChild("right_shoulder_shelf_" + i);
		crownSpires = new ModelPart[6];
		for (int i = 0; i < 6; i++) crownSpires[i] = crown.getChild("crown_spire_" + i);
		growthMushrooms = new ModelPart[4];
		rotDrips = new ModelPart[4];
		for (int i = 0; i < 4; i++) {
			growthMushrooms[i] = backColony.getChild("growth_mushroom_" + i);
			rotDrips[i] = backColony.getChild("rot_drip_" + i);
		}
		robes = new ModelPart[ROBE_PANELS];
		for (int i = 0; i < ROBE_PANELS; i++) {
			robes[i] = hips.getChild("robe_panel_" + i);
			float length = 0.0F;
			for (ModelPart.Cube cube : robes[i].cubes) length = Math.max(length, cube.maxY);
			robeLength[i] = length;
		}
		rootTrails = new ModelPart[8];
		for (int i = 0; i < 8; i++) rootTrails[i] = groundRoots.getChild("root_trail_" + i);
		offeringFingers = new ModelPart[4];
		offeringTips = new ModelPart[4];
		for (int i = 0; i < 4; i++) {
			offeringFingers[i] = rightHand.getChild("offering_finger_" + i);
			offeringTips[i] = offeringFingers[i].getChild("offering_finger_" + i + "_tip");
		}
		vestLinks = new ModelPart[][]{links(leftVestmentChain, "left_vestment_chain", 9),
				links(rightVestmentChain, "right_vestment_chain", 7)};
		vestRest = new float[2][];
		for (int c = 0; c < 2; c++) {
			vestRest[c] = new float[vestLinks[c].length];
			for (int i = 0; i < vestLinks[c].length; i++) vestRest[c][i] = vestLinks[c][i].getInitialPose().y;
		}
		chainLength[0] = censerLeft.getInitialPose().y;
		chainLength[1] = censerRight.getInitialPose().y;
		staffLength = staffFoot.getInitialPose().y;
		staffFootRest.set(world(new org.joml.Vector3f(0, staffLength, 0), root, hips, chest, leftArm, leftForearm, leftHand,
				staff));
		restContact = Math.max(PutricielGait.contact(hips.xRot + leftThigh.xRot, leftShin.xRot),
				PutricielGait.contact(hips.xRot + rightThigh.xRot, rightShin.xRot));
		for (ModelPart.Cube cube : offeringDrips.cubes) dripLength = Math.max(dripLength, cube.maxY);
		footRestL = leftFoot.xRot;
		footRestR = rightFoot.xRot;
		anchorChains = anchorChains();
		growthChains = growthChains();
		growthTrees = growthTrees();
		regionParts = regionParts();
		growthPartList = new ModelPart[GROWTH_COUNT];
		for (int i = 0; i < GROWTH_COUNT; i++) growthPartList[i] = growthTrees[i][0];
		allParts = root.getAllParts().toArray(ModelPart[]::new);
		for (int i = 0; i < 4; i++) dripGrowth[i] = PutricielFireAnchors.growthIndex("growth_mushroom_" + i);
	}

	private static ModelPart[] links(ModelPart chain, String name, int count) {
		ModelPart[] links = new ModelPart[count];
		for (int i = 0; i < count; i++) links[i] = chain.getChild(name + "_link_" + i);
		return links;
	}

	/** Lays the channels on the rig, then the additive layers: the growths, the face, the cloth and what hangs. */
	void pose(float[] a, PutricielAnimState m, float age, float yaw, float pitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		if (Float.isNaN(staffLength)) bind();
		var look = m.look();
		float shake = a[SHAKE] * (Mth.sin(age * 2.3F) * 0.6F + Mth.sin(age * 3.7F + 1.0F) * 0.4F) * 0.025F;
		float jolt = a[FLINCH] * 0.12F;
		hips.xRot += a[HIP_X];
		hips.yRot += a[HIP_TWIST];
		hips.zRot += a[HIP_SWAY];
		float drop = legs(a);
		// the spine and the hood, the hood trailing the chest
		chest.xRot += (look.hunchDegrees() - REST_HUNCH) * DEG + a[BOW] + a[LAG_SPINE] - jolt;
		chest.yRot += a[TWIST];
		chest.zRot += a[LEAN] + shake;
		float follow = m.endingTime() >= 0.0F ? 0.0F : 1.0F - 0.6F * HemoEasing.clamp01(a[KNEEL_BOTH]);
		float range = HemoEasing.lerp(25.0F, 35.0F, m.fire());
		head.xRot += a[HEAD] + a[LAG_HEAD_X] + Mth.clamp(pitch, -20, 20) * DEG * follow - jolt * 1.2F;
		head.yRot += Mth.clamp(yaw, -range, range) * DEG * follow + a[HEAD_TURN] + a[LAG_HEAD_Y];
		head.zRot += a[HEAD_ROLL] + a[LAG_HEAD_Z] - chest.zRot * 0.5F;
		// the arms, then each hand to its target
		float spread = a[SPREAD];
		rightArm.xRot += a[R_ARM_X] - spread * 0.25F;
		rightArm.yRot += a[R_ARM_Y];
		rightArm.zRot += a[R_ARM_Z] + spread * 0.9F;
		rightForearm.xRot += a[R_FORE] + a[LAG_FORE_R];
		leftArm.xRot += a[L_ARM_X] - spread * 0.25F;
		leftArm.yRot += a[L_ARM_Y];
		leftArm.zRot += a[L_ARM_Z] - spread * 0.9F;
		leftForearm.xRot += a[L_FORE] + a[LAG_FORE_L];
		reachArms(a);
		palmPose(a, age);
		staffPose(a);
		censerPose(a, m);
		growths(a, m.riseGrowth() * (1.0F + 0.2F * Math.max(0.0F, a[BLOOM])), age);
		burn(m);
		face(look.face(), a);
		drape(a, age, drop);
	}

	/** The legs: steps and lifts, the crouch's fold, the kneels, then the hips sink until a sole or a knee meets the floor. */
	private float legs(float[] a) {
		float fold = PutricielGait.fold(a[CROUCH]);
		float liftL = Math.max(0.0F, a[LIFT_L]), liftR = Math.max(0.0F, a[LIFT_R]);
		float restThighL = leftThigh.xRot, restKneeL = leftShin.xRot, restThighR = rightThigh.xRot, restKneeR = rightShin.xRot;
		float thighL = restThighL + a[STEP_L] - fold - liftL * 0.45F, kneeL = restKneeL + fold * 2.0F + liftL * 0.9F;
		float thighR = restThighR + a[STEP_R] - fold - liftR * 0.45F, kneeR = restKneeR + fold * 2.0F + liftR * 0.9F;
		// one knee: the left foot planted ahead, the right knee down; both knees: sitting up on them
		float k = HemoEasing.clamp01(a[KNEEL]), kb = HemoEasing.clamp01(a[KNEEL_BOTH]);
		thighL = HemoEasing.lerp(thighL, -72.0F * DEG, k);
		kneeL = HemoEasing.lerp(kneeL, 72.0F * DEG, k);
		thighR = HemoEasing.lerp(thighR, 5.0F * DEG, k);
		kneeR = HemoEasing.lerp(kneeR, 95.0F * DEG, k);
		thighL = HemoEasing.lerp(thighL, -8.0F * DEG, kb);
		kneeL = HemoEasing.lerp(kneeL, 100.0F * DEG, kb);
		thighR = HemoEasing.lerp(thighR, -8.0F * DEG, kb);
		kneeR = HemoEasing.lerp(kneeR, 100.0F * DEG, kb);
		leftThigh.xRot = thighL;
		leftShin.xRot = kneeL;
		rightThigh.xRot = thighR;
		rightShin.xRot = kneeR;
		float contact = Math.max(PutricielGait.contact(hips.xRot + thighL, kneeL),
				PutricielGait.contact(hips.xRot + thighR, kneeR));
		float drop = restContact - contact;
		float tiptoe = HemoEasing.clamp01(a[TIPTOE]);
		hips.y += drop - tiptoe * 1.1F;
		hips.z -= k * 1.0F;
		// the soles stay flat on the floor; a kneeling leg lays its instep on the floor behind the knee; a lifted
		// foot's toes droop
		float flatL = footRestL - (hips.xRot + thighL + kneeL - restThighL - restKneeL) + liftL * 0.35F + tiptoe * 0.7F;
		float flatR = footRestR - (hips.xRot + thighR + kneeR - restThighR - restKneeR) + liftR * 0.35F + tiptoe * 0.7F;
		leftFoot.xRot = HemoEasing.lerp(flatL, Mth.PI - (hips.xRot + thighL + kneeL), kb);
		rightFoot.xRot = HemoEasing.lerp(flatR, Mth.PI - (hips.xRot + thighR + kneeR), Math.max(k, kb));
		return Math.max(0.0F, drop);
	}

	/**
	 * Each hand to its target: the staff hand by its grip offsets, the palm to the Offering's brow or onto the floor.
	 * A damped least-squares solve on the shoulder's three angles and the elbow, started from the authored angles, so
	 * the authored arm picks the elbow and the solve only closes the distance.
	 */
	private void reachArms(float[] a) {
		float grip = Math.abs(a[L_GRIP_X]) + Math.abs(a[L_GRIP_Y]) + Math.abs(a[L_GRIP_Z]);
		if (grip > 0.02F) {
			org.joml.Vector3f target = handAt(leftArm, leftForearm, leftHand).add(a[L_GRIP_X], a[L_GRIP_Y], a[L_GRIP_Z]);
			reach(leftArm, leftForearm, leftHand, target, 1.0F);
		}
		float offer = HemoEasing.clamp01(a[PALM_OFFER]), floor = HemoEasing.clamp01(a[PALM_FLOOR]);
		if (offer > 0.001F || floor > 0.001F) {
			org.joml.Vector3f target = handAt(rightArm, rightForearm, rightHand);
			target.lerp(OFFER_HAND, offer);
			target.lerp(new org.joml.Vector3f(FLOOR_HAND.x, FLOOR_HAND.y, FLOOR_HAND.z), floor);
			reach(rightArm, rightForearm, rightHand, target, 1.0F);
		}
	}

	private static final float IK_STEP = 0.01F, IK_DAMPING = 0.8F, IK_LIMIT = 0.4F;

	/** Moves the arm's shoulder (x, y, z) and elbow (x) so the hand's pivot meets {@code target}, at most eight steps. */
	private void reach(ModelPart arm, ModelPart fore, ModelPart hand, org.joml.Vector3f target, float weight) {
		for (int iteration = 0; iteration < 8; iteration++) {
			org.joml.Vector3f at = handAt(arm, fore, hand);
			org.joml.Vector3f error = new org.joml.Vector3f(target).sub(at).mul(weight);
			if (error.lengthSquared() < 0.0025F) return;
			float[][] jacobian = new float[4][3];
			for (int k = 0; k < 4; k++) {
				nudge(arm, fore, k, IK_STEP);
				org.joml.Vector3f moved = handAt(arm, fore, hand).sub(at).div(IK_STEP);
				nudge(arm, fore, k, -IK_STEP);
				jacobian[k][0] = moved.x;
				jacobian[k][1] = moved.y;
				jacobian[k][2] = moved.z;
			}
			float[] m = new float[9];
			for (int r = 0; r < 3; r++)
				for (int c = 0; c < 3; c++) {
					float sum = r == c ? IK_DAMPING * IK_DAMPING : 0.0F;
					for (int k = 0; k < 4; k++) sum += jacobian[k][r] * jacobian[k][c];
					m[r * 3 + c] = sum;
				}
			Matrix3f inverse = new Matrix3f(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]).invert();
			org.joml.Vector3f y = inverse.transform(new org.joml.Vector3f(error));
			for (int k = 0; k < 4; k++) {
				float step = jacobian[k][0] * y.x + jacobian[k][1] * y.y + jacobian[k][2] * y.z;
				nudge(arm, fore, k, Mth.clamp(step, -IK_LIMIT, IK_LIMIT));
			}
		}
	}

	private static void nudge(ModelPart arm, ModelPart fore, int k, float by) {
		switch (k) {
			case 0 -> arm.xRot += by;
			case 1 -> arm.yRot += by;
			case 2 -> arm.zRot += by;
			default -> fore.xRot += by;
		}
	}

	/** The hand's pivot in model units. */
	private org.joml.Vector3f handAt(ModelPart arm, ModelPart fore, ModelPart hand) {
		return world(new org.joml.Vector3f(), root, hips, chest, arm, fore, hand);
	}

	/**
	 * The offering hand keeps its orientation in the world through the arm's pose: palm up at rest ({@code PALM} 0),
	 * turned out toward where the arm faces at 1, turned down at 2. Then the fingers, the thumb and the drips, which
	 * hang plumb off it.
	 */
	private void palmPose(float[] a, float age) {
		float turn = a[PALM];
		float facing = hips.yRot + chest.yRot + a[R_ARM_Y] * 0.6F * HemoEasing.clamp01(Math.abs(turn));
		float tremble = a[SHAKE] * Mth.sin(age * 2.9F) * 0.02F;
		Quaternionf target = new Quaternionf().rotationY(facing).rotateX(turn * Mth.HALF_PI + tremble);
		Quaternionf carried = rotation(hips, chest, rightArm, rightForearm);
		setRotationFromMatrix(rightHand, carried.conjugate().mul(target).get(new Matrix3f()));
		float curl = a[FINGERS] + a[LAG_FINGERS];
		for (int i = 0; i < 4; i++) {
			offeringFingers[i].xRot -= curl * (0.7F + i * 0.08F);
			offeringFingers[i].yRot += curl < 0.0F ? curl * (i - 1.5F) * 0.15F : 0.0F;
			offeringTips[i].xRot -= Math.max(-0.2F, curl) * 0.6F;
		}
		offeringThumb.zRot -= Math.max(-0.3F, curl) * 0.4F;
		// the drips run long and hang plumb, swinging after the arm; pressed at the floor they run down into it
		float drips = Math.max(0.3F, 1.0F + a[DRIPS] * 0.6F + Mth.sin(age * 0.12F) * 0.04F);
		float floor = HemoEasing.clamp01(a[PALM_FLOOR]);
		if (floor > 0.0F && dripLength > 0.0F) {
			float hand = world(new org.joml.Vector3f(), root, hips, chest, rightArm, rightForearm, rightHand).y;
			drips = HemoEasing.lerp(drips, Math.max(drips, (24.0F - hand) / dripLength), floor);
		}
		offeringDrips.yScale = drips;
		Quaternionf plumb = rotation(hips, chest, rightArm, rightForearm, rightHand).conjugate()
				.mul(new Quaternionf().rotationZYX(a[DRIP_Z], 0, a[DRIP_X]));
		setRotationFromMatrix(offeringDrips, plumb.get(new Matrix3f()));
	}

	/**
	 * The staff. Planted, its foot stays where the stance, the gait or the move put it and the grip slides along the
	 * shaft, so the body leans on it and it tilts as he moves past it; lifted, its head points where the move says
	 * ({@code STAFF_TILT} from upright toward {@code STAFF_AZ}, positive to his right). Either way it stays one rigid
	 * pole through his hand, turned about its own length by {@code STAFF_SPIN}.
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
		org.joml.Vector3f free = freeAxis(a[STAFF_TILT], a[STAFF_AZ]);
		org.joml.Vector3f axis = free.lerp(planted, plant, new org.joml.Vector3f());
		if (axis.lengthSquared() < 1.0E-6F) axis.set(planted);
		axis.normalize();
		Quaternionf pole = new Quaternionf().rotationTo(0, 1, 0, axis.x, axis.y, axis.z).rotateY(a[STAFF_SPIN]);
		setRotationFromMatrix(staff, new Quaternionf(hand).conjugate().mul(pole).get(new Matrix3f()));
		float slide = Mth.clamp(span - staffLength, -GRIP_SLIDE, GRIP_SLIDE) * plant;
		org.joml.Vector3f along = new Quaternionf(hand).conjugate().transform(new org.joml.Vector3f(axis).mul(slide));
		staff.x += along.x;
		staff.y += along.y;
		staff.z += along.z;
	}

	/** The lifted staff's axis from the grip to its foot: the head tilted {@code tilt} from upright toward {@code az}. */
	static org.joml.Vector3f freeAxis(float tilt, float az) {
		float s = Mth.sin(tilt);
		org.joml.Vector3f head = new org.joml.Vector3f(-Mth.sin(az) * s, -Mth.cos(tilt), -Mth.cos(az) * s);
		return head.negate();
	}

	/**
	 * The censers on their chains: each chain hangs on a pendulum carried in the world (the anchor at the staff head
	 * moves with the staff, the walk and the turn; the censer lags it and swings past when it stops), tilted by what the
	 * move asks: flung out along the swung staff, swung out round the turning staff, the left one whirled and thrown,
	 * or fouled in the floor ahead. The links stay straight under the censer's weight.
	 */
	private void censerPose(float[] a, PutricielAnimState m) {
		Quaternionf headRotation = rotation(hips, chest, leftArm, leftForearm, leftHand, staff, staffHead);
		org.joml.Vector3f crownAt = world(new org.joml.Vector3f(), root, hips, chest, leftArm, leftForearm, leftHand, staff,
				staffHead);
		org.joml.Vector3f grip = world(new org.joml.Vector3f(), root, hips, chest, leftArm, leftForearm, leftHand, staff);
		float hx = crownAt.x - grip.x, hz = crownAt.z - grip.z, along = Mth.sqrt(hx * hx + hz * hz);
		float yaw = m.bodyYawRadians();
		float cos = Mth.cos(yaw), sin = Mth.sin(yaw);
		for (int i = 0; i < 2; i++) {
			ModelPart chain = i == 0 ? staffChainLeft : staffChainRight;
			float length = Math.max(1.0F, chainLength[i]);
			org.joml.Vector3f pivot = world(new org.joml.Vector3f(chain.x, chain.y, chain.z), root, hips, chest, leftArm,
					leftForearm, leftHand, staff, staffHead);
			// the pendulum: the anchor into the world's frame (the renderer's mirror and the body's yaw, then its
			// position), the bob lagging it
			float wx = (float) (cos * pivot.x + sin * pivot.z + m.worldX());
			float wz = (float) (sin * pivot.x - cos * pivot.z + m.worldZ());
			int slot = PENDULUM + i * 2;
			float bx = m.follow(slot, wx, 0.28F, 0.16F), bz = m.follow(slot + 1, wz, 0.28F, 0.16F);
			float dx = bx - wx, dz = bz - wz;
			if (dx * dx + dz * dz > 9.0F * length * length) {
				m.reset(slot, wx);
				m.reset(slot + 1, wz);
				dx = dz = 0.0F;
			}
			// back into the model's frame (the same mirror, its own inverse)
			float ox = cos * dx + sin * dz, oz = sin * dx - cos * dz;
			org.joml.Vector3f down = new org.joml.Vector3f(ox, length, oz);
			// flung out along the swung staff
			if (along > 1.0E-3F) down.add(hx / along * tan(a[CENSER_FLING]) * length, 0, hz / along * tan(a[CENSER_FLING]) * length);
			// swung out round the staff's own axis
			float rx = pivot.x - crownAt.x, rz = pivot.z - crownAt.z, radial = Mth.sqrt(rx * rx + rz * rz);
			float out = a[CENSER_OUT] + (i == 0 ? a[CAST] : 0.0F);
			if (radial > 1.0E-3F) down.add(rx / radial * tan(out) * length, 0, rz / radial * tan(out) * length);
			if (i == 0) {
				// thrown at the quarry, ahead and a little down
				float thrown = HemoEasing.clamp01(a[CAST_THROW]);
				down.lerp(new org.joml.Vector3f(0, length * 0.35F, -length), thrown);
			} else {
				// fouled in the floor ahead
				float stuck = HemoEasing.clamp01(a[STUCK]);
				org.joml.Vector3f toward = new org.joml.Vector3f(STUCK_AT).sub(pivot);
				if (toward.lengthSquared() > 1.0E-4F) down.lerp(toward.normalize(length), stuck);
			}
			clampTilt(down, CENSER_LIMIT);
			down.normalize();
			Quaternionf hang = new Quaternionf().rotationTo(0, 1, 0, down.x, down.y, down.z);
			setRotationFromMatrix(chain, new Quaternionf(headRotation).conjugate().mul(hang).get(new Matrix3f()));
		}
	}

	private static float tan(float angle) {
		return (float) Math.tan(Mth.clamp(angle, -CENSER_LIMIT, CENSER_LIMIT));
	}

	/** Keeps a hanging direction within {@code limit} of plumb (down is +y). */
	private static void clampTilt(org.joml.Vector3f down, float limit) {
		float horizontal = Mth.sqrt(down.x * down.x + down.z * down.z);
		float angle = (float) Math.atan2(horizontal, down.y);
		if (angle <= limit || horizontal < 1.0E-5F) return;
		float length = down.length();
		float h = Mth.sin(limit) * length, v = Mth.cos(limit) * length;
		down.set(down.x / horizontal * h, v, down.z / horizontal * h);
	}

	/**
	 * The growths at {@code scale} (1 in the rot, 0.35 burnt in the fire, regrowing past 1, unfurling through the
	 * rise): the sacs swell by their channels and wobble, split and slack when spent; the shelves flex with the colony
	 * and wobble; the mushrooms and the crown's spires sway; the drips hang plumb and swing.
	 */
	private void growths(float[] a, float scale, float age) {
		float spent = HemoEasing.clamp01(a[SAC_SPENT]);
		for (int i = 0; i < 8; i++) {
			ModelPart sac = sacParts[i];
			float swell = Math.max(-0.4F, a[SAC + i]);
			float wobble = a[SAC_JIG] * (i % 2 == 0 ? 0.06F : -0.05F);
			float wide = (1.0F + swell * 0.6F + wobble) * (1.0F - spent * 0.3F);
			float tall = (1.0F + swell * 0.3F - wobble) * (1.0F - spent * 0.45F);
			sac.xScale = sac.zScale = wide * scale;
			sac.yScale = tall * scale;
		}
		for (int i = 0; i < backShelves.length; i++) {
			ModelPart shelf = backShelves[i];
			shelf.xRot += a[COLONY] * (0.6F + 0.2F * (i % 3)) + a[COLONY_JIG] * (i % 2 == 0 ? 1.0F : -0.6F);
			shelf.xScale = shelf.yScale = shelf.zScale = scale;
		}
		for (int i = 0; i < hemShelves.length; i++) {
			hemShelves[i].xRot += a[COLONY] * 0.3F + a[COLONY_JIG] * (i % 2 == 0 ? 0.7F : -0.5F);
			hemShelves[i].xScale = hemShelves[i].yScale = hemShelves[i].zScale = scale;
		}
		for (int i = 0; i < shoulderShelves.length; i++) shoulderShelves[i].xRot += a[COLONY_JIG] * (i % 2 == 0 ? 0.5F : -0.4F);
		for (int i = 0; i < 4; i++) {
			ModelPart mushroom = growthMushrooms[i];
			mushroom.xScale = mushroom.zScale = scale;
			mushroom.yScale = scale + Mth.sin(age * 0.045F + i) * 0.025F;
			mushroom.zRot += a[COLONY_JIG] * (i % 2 == 0 ? 0.5F : -0.5F);
			ModelPart drip = rotDrips[i];
			drip.yScale = (0.8F + (Mth.sin(age * 0.08F - i * 1.3F) + 1.0F) * 0.2F) * scale;
			drip.xRot += a[COLONY_JIG] * 0.6F + a[DRIP_X] * 0.4F;
		}
		for (ModelPart part : new ModelPart[]{crownMushroom, shoulderMushroom, backMushroom, headShelfLeft, headShelfRight}) {
			part.xScale = part.yScale = part.zScale = scale;
		}
		crownMushroom.zRot += a[CROWN_JIG];
		backMushroom.zRot += a[COLONY_JIG] * 0.6F;
		shoulderMushroom.zRot += a[COLONY_JIG] * -0.5F;
		for (int i = 0; i < crownSpires.length; i++) crownSpires[i].zRot += a[CROWN_JIG] * (i % 2 == 0 ? 0.35F : -0.3F);
	}

	/**
	 * The fire on the growths (revision R1.8), over what {@link #growths} set: each shelf, sac and mushroom shrinks to
	 * 0.35 as the Ignition's fire reaches it (one tick a kindle step), the two outermost back shelves and the back
	 * mushroom are eaten on to 0.2 as the Kindling burns down, and through the Embering the colony regrows from its root
	 * outward and the arms and crown from their tips inward, three ticks a step, each with its overshoot. The rot drips
	 * shrink with their mushrooms. In the rot every scale is 1, so nothing here moves a rot pose.
	 */
	private void burn(PutricielAnimState m) {
		var moment = m.fireMoment();
		for (int i = 0; i < GROWTH_COUNT; i++) {
			float g = PutricielFireAnchors.growth(i, moment);
			growthBurn[i] = g;
			if (g == 1.0F) continue;
			ModelPart part = growthPartList[i];
			part.xScale *= g;
			part.yScale *= g;
			part.zScale *= g;
		}
		for (int i = 0; i < rotDrips.length; i++) if (dripGrowth[i] >= 0) rotDrips[i].yScale *= growthBurn[dripGrowth[i]];
	}

	// ---------------------------------------------------------------- the fire on the body (revision R1)

	/**
	 * Every fire anchor's posed point and outward normal (unit), in the model's block units as {@link #core()} and
	 * {@link #palm()} are, written into the arrays in table order. Call it after the pose.
	 */
	public void fireAnchors(org.joml.Vector3f[] points, org.joml.Vector3f[] normals) {
		if (Float.isNaN(staffLength)) bind();
		for (int i = 0; i < ANCHOR_COUNT; i++) {
			PoseStack poses = new PoseStack();
			for (ModelPart part : anchorChains[i]) part.translateAndRotate(poses);
			PoseStack.Pose last = poses.last();
			points[i] = last.pose().transformPosition(new org.joml.Vector3f(ANCHOR_POINTS[i * 3], ANCHOR_POINTS[i * 3 + 1],
					ANCHOR_POINTS[i * 3 + 2]).div(16.0F));
			org.joml.Vector3f normal = last.normal().transform(new org.joml.Vector3f(ANCHOR_NORMALS[i * 3],
					ANCHOR_NORMALS[i * 3 + 1], ANCHOR_NORMALS[i * 3 + 2]));
			normals[i] = normal.lengthSquared() < 1.0E-8F ? new org.joml.Vector3f(0.0F, -1.0F, 0.0F) : normal.normalize();
		}
	}

	/**
	 * The char pass (PutricielCharLayer): every growth part, with its children, at its own staggered char alpha
	 * (blackening in kindle order at the Ignition, clearing shelf by shelf as it regrows), and the rest of the body at
	 * the look's. Nothing draws when every alpha is spent.
	 */
	public void renderCharred(PoseStack poses, VertexConsumer buffer, int light, int overlay, PutricielAnimState m) {
		if (Float.isNaN(staffLength)) return;
		var moment = m.fireMoment();
		float body = m.look().charAlpha();
		for (ModelPart[] tree : growthTrees) for (ModelPart part : tree) part.skipDraw = true;
		if (body > 0.01F) root.render(poses, buffer, light, overlay, white(body));
		for (int i = 0; i < GROWTH_COUNT; i++) {
			float alpha = PutricielFireAnchors.charAlpha(i, moment);
			if (alpha <= 0.01F) continue;
			for (ModelPart part : growthTrees[i]) part.skipDraw = false;
			poses.pushPose();
			for (ModelPart part : growthChains[i]) part.translateAndRotate(poses);
			growthPartList[i].render(poses, buffer, light, overlay, white(alpha));
			poses.popPose();
			for (ModelPart part : growthTrees[i]) part.skipDraw = true;
		}
		for (ModelPart[] tree : growthTrees) for (ModelPart part : tree) part.skipDraw = false;
	}

	/**
	 * The burning pass (PutricielBurnLayer): the model once per region with every other part skipped, each region at
	 * {@code levels[region]} (0 to 1, {@code PutricielFireAnchors.Region}'s order), as the strength of an additive
	 * emissive colour, so the model's own burn climbs the staff and spreads across the back with the flames.
	 */
	public void renderBurning(PoseStack poses, VertexConsumer buffer, int light, int overlay, float[] levels, PutricielAnimState m) {
		if (Float.isNaN(staffLength)) return;
		var moment = m.fireMoment();
		for (int region = 0; region < regionParts.length && region < levels.length; region++) {
			float level = Mth.clamp(levels[region], 0.0F, 1.0F);
			if (level <= 0.01F) continue;
			for (ModelPart part : allParts) part.skipDraw = true;
			for (ModelPart part : regionParts[region]) part.skipDraw = false;
			// a growth that has regrown out of its char no longer burns: crimson added over its cream reads as pink flesh
			for (int i = 0; i < GROWTH_COUNT; i++) {
				if (PutricielFireAnchors.charAlpha(i, moment) < 0.5F) for (ModelPart part : growthTrees[i]) part.skipDraw = true;
			}
			int c = (int) (level * 255.0F);
			root.render(poses, buffer, light, overlay, 0xFF000000 | c << 16 | c << 8 | c);
		}
		for (ModelPart part : allParts) part.skipDraw = false;
	}

	/** White at {@code alpha}, ARGB. */
	private static int white(float alpha) {
		return (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24 | 0xFFFFFF;
	}

	/** The purifier's face pushed forward through the hollow, the face vines burning back or parting with the hood. */
	private void face(float face, float[] a) {
		purifierFace.z -= face * 1.0F;
		faceVines.yScale = 1.0F - face * 0.8F;
		faceVines.xRot -= face * 0.3F + a[HOOD] * 0.45F - a[CROWN_JIG] * 0.5F;
	}

	/**
	 * The cloth and what hangs: the vestment chains plumb off the chest and bending as they swing, the mantles, the
	 * robe panels following the thighs, flaring as the hips sink so they heap on the floor and trailing the walk, and
	 * the ground roots dragging round after a turn or pulled taut.
	 */
	private void drape(float[] a, float age, float drop) {
		Quaternionf chestPlumb = rotation(hips, chest).conjugate();
		for (int c = 0; c < 2; c++) {
			ModelPart chain = c == 0 ? leftVestmentChain : rightVestmentChain;
			float idle = Mth.sin(age * 0.07F + c) * 0.04F;
			Quaternionf swing = new Quaternionf().rotationZYX(a[VEST_Z] * (1.0F + c * 0.1F) + idle, 0, a[VEST_X] * (1.0F + c * 0.12F));
			setRotationFromMatrix(chain, new Quaternionf(chestPlumb).mul(swing).get(new Matrix3f()));
			bend(vestLinks[c], vestRest[c], a[VEST_BEND_X], a[VEST_BEND_Z]);
		}
		leftMantle.xRot += a[MANTLE] + Mth.sin(age * 0.07F) * 0.018F;
		rightMantle.xRot += a[MANTLE] * 0.9F + Mth.sin(age * 0.07F + 1.4F) * 0.018F;
		leftMantle.zRot -= Math.max(0.0F, a[SPREAD]) * 0.15F;
		rightMantle.zRot += Math.max(0.0F, a[SPREAD]) * 0.15F;
		float height = 24.0F - hips.getInitialPose().y;
		for (int i = 0; i < ROBE_PANELS; i++) {
			float angle = i * Mth.TWO_PI / ROBE_PANELS;
			float out = Mth.cos(angle);
			ModelPart thigh = Mth.sin(angle) >= 0.0F ? leftThigh : rightThigh;
			float stride = (thigh.xRot - thigh.getInitialPose().xRot) * out * (Math.abs(out) > 0.5F ? 0.7F : 0.4F);
			float flare = Math.max(0.0F, acos((height - drop) / robeLength[i]) - acos(height / robeLength[i]));
			float trailing = a[ROBE_X] * out + a[ROBE_Z] * Mth.sin(angle);
			robes[i].xRot += Mth.clamp(stride, -0.6F, 0.6F) + flare + Math.max(-0.15F, trailing)
					+ Mth.sin(age * 0.07F - i * 0.7F) * (0.018F + Math.abs(a[SHAKE]) * 0.04F);
		}
		groundRoots.yRot += a[ROOT_DRAG];
		float taut = a[ROOTS];
		for (int i = 0; i < rootTrails.length; i++) {
			rootTrails[i].xRot -= Math.max(0.0F, taut) * 0.12F;
			rootTrails[i].zScale = Math.max(0.5F, 1.0F - taut * 0.3F);
			rootTrails[i].yRot += Mth.sin(age * 0.05F + i) * 0.03F;
		}
	}

	/**
	 * Curves a hanging chain: each link turns a little more than the one above it and hangs from the end of it. Zero
	 * bend leaves the links as authored.
	 */
	private static void bend(ModelPart[] links, float[] rest, float bendX, float bendZ) {
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
			float step = i + 1 < n ? rest[i + 1] - rest[i] : rest[i] - (i > 0 ? rest[i - 1] : 0.0F);
			x -= step * Mth.sin(az);
			y += step * Mth.cos(ax) * Mth.cos(az);
			z += step * Mth.sin(ax);
		}
	}

	private static float acos(float cosine) {
		return (float) Math.acos(Mth.clamp(cosine, -1.0F, 1.0F));
	}

	public org.joml.Vector3f core() { return point(0, 0, -.2F, root, hips, chest); }
	public org.joml.Vector3f palm() { return point(0, -.08F, 0, root, hips, chest, rightArm, rightForearm, rightHand); }
	public org.joml.Vector3f faceAnchor() { return point(0, 0, -.05F, root, hips, chest, head, purifierFace); }
	public org.joml.Vector3f staffFootPoint() { return point(0, 0, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffFoot); }
	public org.joml.Vector3f staffHeadPoint() { return point(0, 0, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffHead); }
	public org.joml.Vector3f[] sacPoints() {
		return new org.joml.Vector3f[]{point(0, 0, 0, root, hips, chest, sporeCluster, spore0),
				point(0, 0, 0, root, hips, chest, sporeCluster, spore1),
				point(0, 0, 0, root, hips, chest, sporeCluster, spore2),
				point(0, 0, 0, root, hips, chest, sporeCluster, spore3),
				point(0, 0, 0, root, hips, chest, rightArm, shoulderSpore),
				point(0, 0, 0, root, hips, hemSpore),
				point(0, 0, 0, root, hips, chest, backColony, backSpore0),
				point(0, 0, 0, root, hips, chest, backColony, backSpore1)};
	}
	public org.joml.Vector3f[] censers() {
		return new org.joml.Vector3f[]{point(0, -.08F, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffHead, staffChainLeft, censerLeft),
				point(0, -.08F, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffHead, staffChainRight, censerRight)};
	}
	private static org.joml.Vector3f point(float x, float y, float z, ModelPart... parts) {
		PoseStack pose = new PoseStack();
		for (ModelPart part : parts) part.translateAndRotate(pose);
		return pose.last().pose().transformPosition(new org.joml.Vector3f(x, y, z));
	}

	/** A point in the last part's units, carried out to model units in the space the chain's first part hangs in. */
	private static org.joml.Vector3f world(org.joml.Vector3f at, ModelPart... chain) {
		PoseStack poses = new PoseStack();
		for (ModelPart part : chain) part.translateAndRotate(poses);
		return poses.last().pose().transformPosition(at.div(16.0F)).mul(16.0F);
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
		part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1, 1));
		part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
	}
