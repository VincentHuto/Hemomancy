
	// ------------------------------------------------------------------ animation (tools/model_export/mycophant_anim.java)
	//
	// Poses are authored in the blockout's frame (pixels at world scale, Y up, face toward -Z, root at the floor) so
	// they read the same as tools/model_export/reference/spawn-mother-v8.html; bo() converts a point to model space
	// (half size, Y down, the creature's right on -X). Rotations flip sign on X and Y for the same reason.

	/**
	 * Review-client hook: a pose name, with "_p2" for the opened head. The rebuild's poses are "stalk", "tending",
	 * "notice", "roar", "embrace", "hold", "spawn", "guard", "sweep", "siphon", "pluck", "bloom", "stagger", "yield" and
	 * "drag"; the moveset's are "rake_l", "rake_r", "slam", "settle_rise", "settle_drop", "pin", "tail_coil",
	 * "tail_lash", "vents_open", "hand_wind", "hand_sweep", "spit", "mortar", "nurse", "lances", "net", "gaze",
	 * "last_brood_rear", "last_brood_pull" and "last_brood_collapse" (the phase-2 ones always show the opened head).
	 */
	public static volatile String reviewPose;

	private static final class Pose {
		/** Scalars first, then the three-float targets, in {@link #write} order. */
		private static final int SCALARS = 19;
		private static final int VECTORS = 9;
		/** The pose as a flat array, for the blend memory each entity keeps in its MycophantAnimState. */
		static final int SIZE = SCALARS + VECTORS * 3;
		private static final float[] SCRATCH_A = new float[SIZE];
		private static final float[] SCRATCH_B = new float[SIZE];
		private static final float[] SCRATCH_OUT = new float[SIZE];

		float thorax, neck, head, frill, maw, tongue, tentFwd, height, vents;
		final float[] wing = new float[3];
		final float[] hand = new float[3];
		final float[] handPole = new float[3];
		float grip;
		final float[] siphonT = new float[3];
		final float[] siphonPole = new float[3];
		float curl;
		final float[] front = new float[3];
		final float[] second = new float[3];
		/** Offsets (blockout pixels, +x toward her right) added to the left and right front claws' footholds. */
		final float[] reachL = new float[3];
		final float[] reachR = new float[3];
		/** The tail tendrils' coil (+ curls them up over the abdomen) and swing (+ toward her right). */
		float tailCurl, tailSwing;
		/** The abdomen's lift (radians, + raises its end) and swing (radians, + toward her right). */
		float abdLift, abdYaw;
		/** The sac's swell: 1 is the Last Brood's opening (1.3 times its size, pushed forward); below 0 it sags. */
		float sac;
		/** Blockout pixels the pulp pushes forward out of the opened skull. */
		float pulpFwd;
		/** Radians the head turns toward her right (toward a larger yaw), shared down the neck and thorax. */
		float turn;
		/** How far the wing spars hook their tips down; below 0 they open flat. */
		float spar;

		Pose set(float thorax, float neck, float head, float frill, float maw, float tongue, float tentFwd, float height,
				float vents) {
			this.thorax = thorax; this.neck = neck; this.head = head; this.frill = frill; this.maw = maw;
			this.tongue = tongue; this.tentFwd = tentFwd; this.height = height; this.vents = vents;
			return this;
		}

		Pose wing(float x, float y, float z) { wing[0] = x; wing[1] = y; wing[2] = z; return this; }

		Pose hand(float x, float y, float z, float px, float py, float pz, float grip) {
			hand[0] = x; hand[1] = y; hand[2] = z; handPole[0] = px; handPole[1] = py; handPole[2] = pz; this.grip = grip;
			return this;
		}

		Pose siphon(float x, float y, float z, float px, float py, float pz, float curl) {
			siphonT[0] = x; siphonT[1] = y; siphonT[2] = z; siphonPole[0] = px; siphonPole[1] = py; siphonPole[2] = pz;
			this.curl = curl;
			return this;
		}

		Pose drag(float fx, float fy, float fz, float sx, float sy, float sz) {
			front[0] = fx; front[1] = fy; front[2] = fz; second[0] = sx; second[1] = sy; second[2] = sz;
			return this;
		}

		Pose reach(float lx, float ly, float lz, float rx, float ry, float rz) {
			reachL[0] = lx; reachL[1] = ly; reachL[2] = lz; reachR[0] = rx; reachR[1] = ry; reachR[2] = rz;
			return this;
		}

		Pose tail(float curl, float swing, float lift, float yaw) {
			tailCurl = curl; tailSwing = swing; abdLift = lift; abdYaw = yaw;
			return this;
		}

		Pose swell(float sac) { this.sac = sac; return this; }

		Pose pulp(float forward) { pulpFwd = forward; return this; }

		Pose turn(float turn) { this.turn = turn; return this; }

		Pose spar(float spar) { this.spar = spar; return this; }

		private float[][] vectors() {
			return new float[][] { wing, hand, handPole, siphonT, siphonPole, front, second, reachL, reachR };
		}

		void write(float[] out) {
			out[0] = thorax; out[1] = neck; out[2] = head; out[3] = frill; out[4] = maw; out[5] = tongue;
			out[6] = tentFwd; out[7] = height; out[8] = vents; out[9] = grip; out[10] = curl; out[11] = tailCurl;
			out[12] = tailSwing; out[13] = abdLift; out[14] = abdYaw; out[15] = sac; out[16] = pulpFwd; out[17] = turn;
			out[18] = spar;
			float[][] vectors = vectors();
			for (int a = 0; a < VECTORS; a++) System.arraycopy(vectors[a], 0, out, SCALARS + a * 3, 3);
		}

		Pose read(float[] in) {
			set(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8]);
			grip = in[9]; curl = in[10]; tailCurl = in[11]; tailSwing = in[12]; abdLift = in[13]; abdYaw = in[14];
			sac = in[15]; pulpFwd = in[16]; turn = in[17]; spar = in[18];
			float[][] vectors = vectors();
			for (int a = 0; a < VECTORS; a++) System.arraycopy(in, SCALARS + a * 3, vectors[a], 0, 3);
			return this;
		}

		Pose copy(Pose o) {
			o.write(SCRATCH_OUT);
			return read(SCRATCH_OUT);
		}

		/** this = mix(a, b, t). Either may be this pose. */
		Pose mix(Pose a, Pose b, float t) {
			a.write(SCRATCH_A);
			b.write(SCRATCH_B);
			for (int i = 0; i < SIZE; i++) SCRATCH_OUT[i] = Mth.lerp(t, SCRATCH_A[i], SCRATCH_B[i]);
			return read(SCRATCH_OUT);
		}

		/** this += (b - a) * t: layers part of the move from a to b over what is already posed, so swings can overlap. */
		Pose add(Pose a, Pose b, float t) {
			a.write(SCRATCH_A);
			b.write(SCRATCH_B);
			write(SCRATCH_OUT);
			for (int i = 0; i < SIZE; i++) SCRATCH_OUT[i] += (SCRATCH_B[i] - SCRATCH_A[i]) * t;
			return read(SCRATCH_OUT);
		}
	}

	private static final Pose STALK = new Pose().set(0.0F, 0.0F, 0.0F, 0.55F, 0.15F, 0.3F, 0.4F, 0.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose TENDING = new Pose().set(-0.25F, -0.2F, -0.35F, 0.0F, 0.05F, 0.5F, 0.8F, -2.0F, 0.0F).wing(-0.15F, 0.95F, -0.35F)
			.hand(-6, 38, -58, -1, -0.6F, 0.1F, 0.75F).siphon(14, 52, -56, 1, -0.3F, 0.2F, 0.55F).drag(42, 0, -54, 58, 0, -22);
	private static final Pose NOTICE = new Pose().set(-0.05F, 0.15F, 0.25F, 0.5F, 0.2F, 0.3F, 0.3F, -1.0F, 0.0F).wing(0.05F, 0.75F, -0.1F)
			.hand(-8, 40, -66, -1, -0.6F, 0.1F, 0.7F).siphon(18, 54, -60, 1, -0.3F, 0.2F, 0.2F).drag(44, 0, -56, 58, 0, -22);
	private static final Pose ROAR = new Pose().set(0.4F, -0.05F, -0.12F, 1.0F, 1.0F, 0.0F, -0.4F, 2.0F, 0.4F).wing(0.1F, 0.12F, 0.35F)
			.hand(-52, 92, -24, -1, -0.5F, 0.5F, 0.0F).siphon(52, 90, -20, 1, -0.5F, 0.5F, -0.3F).drag(54, 58, -50, 64, 18, -32);
	private static final Pose EMBRACE = new Pose().set(-0.08F, -0.05F, 0.1F, 0.75F, 0.8F, 0.1F, 1.0F, 1.0F, 0.0F).wing(0.05F, 1.0F, 0.2F)
			.hand(-30, 50, -96, -1, 0.2F, 0.2F, 0.0F).siphon(30, 52, -92, 1, 0.2F, 0.2F, 0.0F).drag(46, 14, -98, 62, 4, -62);
	private static final Pose HOLD = new Pose().set(-0.12F, -0.1F, -0.15F, 0.9F, 0.6F, 0.2F, 1.0F, 0.0F, 0.2F).wing(0.1F, 1.3F, -0.2F)
			.hand(-8, 34, -58, -1, -0.4F, 0.2F, 0.85F).siphon(10, 40, -58, 1, -0.3F, 0.2F, 0.6F).drag(40, 0, -66, 56, 0, -28);
	private static final Pose SPAWN = new Pose().set(-0.12F, -0.1F, -0.1F, 0.4F, 0.3F, 0.4F, 0.5F, -3.0F, 1.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-30, 34, -40, -1, -0.3F, 0.3F, 0.4F).siphon(30, 36, -34, 1, -0.3F, 0.3F, 0.4F).drag(50, 0, -56, 64, 0, -20);
	private static final Pose GUARD = new Pose().set(-0.15F, -0.2F, -0.2F, 0.3F, 0.1F, 0.4F, 0.6F, -2.0F, 0.0F).wing(0.15F, 1.3F, -0.25F)
			.hand(-14, 40, -40, -1, -0.3F, 0.3F, 0.6F).siphon(14, 42, -38, 1, -0.3F, 0.3F, 0.5F).drag(44, 0, -52, 58, 0, -20);
	private static final Pose SWEEP_WIND = new Pose().set(0.2F, 0.0F, 0.0F, 0.9F, 0.6F, 0.1F, 0.0F, 1.0F, 0.1F).wing(0.3F, -0.9F, 0.6F)
			.hand(-48, 66, -10, -1, -0.3F, 0.3F, 0.1F).siphon(48, 66, -10, 1, -0.3F, 0.3F, -0.2F).drag(50, 0, -56, 62, 0, -24);
	private static final Pose SWEEP = new Pose().set(0.15F, 0.0F, 0.0F, 1.0F, 0.9F, 0.1F, 0.2F, 1.0F, 0.2F).wing(0.25F, 0.45F, 0.65F)
			.hand(-44, 60, -40, -1, -0.3F, 0.3F, 0.0F).siphon(44, 60, -36, 1, -0.3F, 0.3F, -0.2F).drag(48, 0, -60, 60, 0, -24);
	private static final Pose SIPHON_AIM = new Pose().set(0.1F, 0.1F, 0.15F, 0.7F, 0.4F, 0.2F, 0.4F, 0.5F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(34, 78, -26, 1, -0.6F, 0.4F, -0.4F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose PLUCK_WIND = new Pose().set(0.12F, 0.05F, 0.1F, 0.7F, 0.3F, 0.2F, 0.4F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-34, 74, -36, -1, -0.5F, 0.4F, 0.0F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose PLUCK = new Pose().set(-0.1F, -0.05F, 0.0F, 0.6F, 0.4F, 0.2F, 0.6F, 0.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-12, 30, -84, -1, 0.3F, 0.2F, 0.95F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -64, 60, 0, -26);
	private static final Pose BLOOM_WIND = new Pose().set(-0.15F, -0.25F, -0.2F, 1.0F, 0.5F, 0.0F, -0.3F, -1.0F, 0.3F).wing(0.1F, 0.4F, 0.3F)
			.hand(-30, 40, -44, -1, -0.3F, 0.3F, 0.2F).siphon(30, 42, -40, 1, -0.3F, 0.3F, 0.1F).drag(48, 0, -58, 60, 0, -24);
	private static final Pose BLOOM = new Pose().set(0.25F, 0.05F, 0.05F, 1.0F, 1.0F, 0.0F, -0.6F, 2.0F, 0.6F).wing(0.15F, 0.2F, 0.5F)
			.hand(-40, 70, -30, -1, -0.5F, 0.5F, 0.0F).siphon(40, 70, -28, 1, -0.5F, 0.5F, -0.3F).drag(50, 10, -56, 62, 4, -26);
	private static final Pose STAGGER = new Pose().set(0.28F, -0.15F, 0.1F, 0.2F, 0.7F, 0.2F, -0.2F, 2.0F, 0.2F).wing(0.0F, -0.7F, 0.1F)
			.hand(-38, 52, -30, -1, -0.3F, 0.4F, 0.1F).siphon(38, 54, -26, 1, -0.3F, 0.4F, -0.1F).drag(52, 0, -52, 64, 0, -18);
	/** The split's strain (hunched, head bowed, the hand clutching toward the cracking face) and its wing display. */
	private static final Pose STRAIN = new Pose().set(-0.12F, -0.3F, -0.4F, 0.0F, 0.35F, 0.4F, 0.3F, -2.5F, 0.2F).wing(0.0F, -0.5F, 0.08F)
			.hand(-20, 64, -52, -1, -0.4F, 0.3F, 0.95F).siphon(22, 66, -50, 1, -0.4F, 0.3F, 0.7F).drag(46, 0, -58, 60, 0, -24);
	private static final Pose SPLIT_DISPLAY = new Pose().set(0.2F, 0.05F, 0.05F, 0.8F, 0.4F, 0.2F, 0.0F, 1.0F, 0.3F).wing(0.15F, 0.35F, 0.55F)
			.hand(-46, 70, -30, -1, -0.5F, 0.5F, 0.2F).siphon(46, 70, -28, 1, -0.5F, 0.5F, -0.2F).drag(50, 20, -54, 62, 4, -26);
	private static final Pose YIELDED = new Pose().set(-0.35F, -0.45F, -0.55F, -0.2F, 0.0F, 0.6F, 0.9F, -12.0F, 0.0F).wing(-0.3F, -1.2F, -0.4F)
			.hand(-30, 12, -54, -1, -0.5F, 0.2F, 0.2F).siphon(16, 26, -74, 1, 0.2F, 0.2F, -0.1F).drag(56, 0, -46, 66, 0, -14);

	// the moveset's poses (2026-10-24 expansion). Rakes: a claw lifts and draws back outward, then sweeps in across the
	// front, low; the slam cocks both front claws overhead in a crouch and brings them down together ahead of her
	private static final Pose RAKE_L_UP = new Pose().set(0.1F, 0.05F, 0.05F, 0.8F, 0.35F, 0.2F, 0.5F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-16, 56, -44, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(-14, 44, 6, 0, 0, 0).tail(0.0F, 0.0F, 0.0F, 0.05F);
	private static final Pose RAKE_L = new Pose().set(-0.12F, 0.0F, 0.05F, 0.9F, 0.6F, 0.1F, 0.8F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(-14, 50, -52, -1, -0.3F, 0.3F, 0.2F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(32, 12, -34, 0, 0, 0).tail(0.0F, 0.0F, 0.0F, -0.06F).turn(0.06F);
	private static final Pose RAKE_R_UP = new Pose().set(0.1F, 0.05F, 0.05F, 0.8F, 0.35F, 0.2F, 0.5F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 58, -36, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, 14, 44, 6).tail(0.0F, 0.0F, 0.0F, -0.05F);
	private static final Pose RAKE_R = new Pose().set(-0.12F, 0.0F, 0.05F, 0.9F, 0.6F, 0.1F, 0.8F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 52, -42, 1, -0.4F, 0.3F, 0.2F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -32, 12, -34).tail(0.0F, 0.0F, 0.0F, 0.06F).turn(-0.06F);
	private static final Pose SLAM_COCK = new Pose().set(0.05F, -0.1F, -0.1F, 1.0F, 0.3F, 0.1F, 0.7F, -3.0F, 0.2F).wing(0.1F, -0.2F, 0.4F)
			.hand(-24, 66, -40, -1, -0.4F, 0.3F, 0.2F).siphon(24, 68, -38, 1, -0.4F, 0.3F, 0.0F).drag(50, 0, -56, 64, 0, -22)
			.reach(14, 76, 14, -14, 76, 14);
	private static final Pose SLAM = new Pose().set(-0.25F, -0.1F, -0.1F, 1.0F, 1.0F, 0.0F, 1.0F, -2.0F, 0.3F).wing(0.2F, 0.3F, -0.2F)
			.hand(-30, 52, -50, -1, -0.3F, 0.3F, 0.1F).siphon(30, 54, -48, 1, -0.3F, 0.3F, -0.2F).drag(50, 0, -58, 64, 0, -24)
			.reach(32, 4, -26, -32, 4, -26);
	// the Brood Settle: she rears up on all four claws spread wide, then drops her belly to the floor and lies there
	private static final Pose SETTLE_RISE = new Pose().set(0.15F, 0.05F, 0.05F, 0.8F, 0.6F, 0.1F, 0.2F, 10.0F, 0.5F).wing(0.1F, 0.0F, 0.5F)
			.hand(-50, 74, -34, -1, -0.5F, 0.5F, 0.1F).siphon(50, 74, -32, 1, -0.5F, 0.5F, -0.2F).drag(64, 0, -64, 78, 0, -22)
			.swell(0.35F);
	private static final Pose SETTLE_DROP = new Pose().set(-0.18F, -0.2F, -0.15F, 0.4F, 0.5F, 0.3F, 0.7F, -11.0F, 0.3F).wing(0.0F, -0.8F, -0.1F)
			.hand(-40, 16, -62, -1, 0.3F, 0.3F, 0.3F).siphon(38, 18, -60, 1, 0.3F, 0.3F, 0.1F).drag(68, 0, -62, 82, 0, -20)
			.tail(-0.2F, 0.0F, -0.06F, 0.0F).swell(0.1F);
	// the Claw Pin: the right front claw rises and points, then stabs down onto the pin spot (MycophantHoldKind.PIN:
	// 3.6 blocks ahead and 1.6 to her right, on the floor, so blockout (25.6, 0, -57.6)) and presses there
	private static final Pose PIN_WIND = new Pose().set(0.12F, 0.05F, 0.1F, 0.75F, 0.3F, 0.2F, 0.5F, 1.5F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 44, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 60, -36, 1, -0.4F, 0.3F, 0.2F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -4, 42, -6);
	private static final Pose PIN = new Pose().set(-0.16F, -0.05F, -0.12F, 0.85F, 0.5F, 0.2F, 0.9F, -1.0F, 0.0F).wing(0.05F, -0.3F, 0.0F)
			.hand(-12, 30, -66, -1, 0.2F, 0.2F, 0.35F).siphon(14, 54, -40, 1, -0.4F, 0.3F, 0.3F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -20.4F, 0, 2.4F);
	// the Tail Lash: the tendrils coil up and to her left with the abdomen tilted, then lash across to her right (and back)
	private static final Pose TAIL_COIL = new Pose().set(0.05F, 0.0F, 0.05F, 0.6F, 0.2F, 0.3F, 0.4F, 1.0F, 0.2F).wing(0.05F, -0.6F, 0.15F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -58, 64, 0, -22)
			.tail(1.0F, -0.8F, 0.2F, -0.15F).turn(0.3F);
	private static final Pose TAIL_LASH = new Pose().set(-0.02F, 0.0F, 0.0F, 0.8F, 0.5F, 0.2F, 0.3F, 0.0F, 0.2F).wing(0.05F, -0.5F, 0.1F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -58, 64, 0, -22)
			.tail(0.15F, 1.0F, 0.1F, 0.25F).turn(0.15F);
	private static final Pose TAIL_LASH_BACK = new Pose().copy(TAIL_LASH).tail(0.15F, -1.0F, 0.1F, -0.25F).turn(-0.15F);
	// the Vent Bloom: the petal vents open wide with the abdomen raised and the tendrils lifted clear
	private static final Pose VENTS_OPEN = new Pose().set(-0.05F, -0.05F, 0.0F, 0.5F, 0.2F, 0.3F, 0.4F, 1.0F, 1.5F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.tail(0.5F, 0.0F, 0.25F, 0.0F);
	// the Hand Sweep: the grasping hand swings out wide and low to her left, then sweeps across the front at the floor
	private static final Pose HAND_WIND = new Pose().set(0.06F, 0.0F, 0.0F, 0.8F, 0.4F, 0.2F, 0.4F, 0.5F, 0.0F).wing(0.05F, -0.6F, 0.1F)
			.hand(-76, 30, -16, -1, 0.4F, 0.3F, 0.05F).siphon(26, 46, -34, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -60, 60, 0, -26)
			.tail(0.0F, 0.0F, 0.0F, 0.08F).turn(-0.2F);
	private static final Pose HAND_SWEEP = new Pose().set(-0.1F, 0.0F, 0.0F, 0.9F, 0.7F, 0.1F, 0.6F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(28, 24, -70, -0.5F, 0.6F, 0.2F, 0.15F).siphon(34, 56, -30, 1, -0.4F, 0.3F, 0.1F).drag(46, 0, -60, 60, 0, -26)
			.tail(0.0F, 0.0F, 0.0F, -0.08F).turn(0.2F);
	// the Siphon Spit: the siphon arches up high over her right shoulder, pulsing forward at each glob
	private static final Pose SPIT = new Pose().set(0.12F, 0.08F, 0.12F, 0.6F, 0.25F, 0.2F, 0.2F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 44, -48, -1, -0.3F, 0.3F, 0.3F).siphon(34, 96, -44, 1, -0.6F, 0.4F, -0.6F).drag(46, 0, -60, 60, 0, -26);
	// the Spore Mortar: the abdomen lifts, the vents open and it pumps the eggs up and out
	private static final Pose MORTAR = new Pose().set(-0.1F, -0.05F, 0.0F, 0.4F, 0.2F, 0.3F, 0.5F, -1.0F, 1.2F).wing(0.0F, -0.5F, 0.08F)
			.hand(-28, 36, -48, -1, -0.3F, 0.3F, 0.3F).siphon(26, 40, -36, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -58, 66, 0, -20)
			.tail(0.4F, 0.0F, 0.35F, 0.0F).swell(0.15F);
	// Nursing: bowed low over the fungling as she tends it (the siphon itself swings onto it through the latch)
	private static final Pose NURSE = new Pose().set(-0.22F, -0.25F, -0.3F, 0.05F, 0.05F, 0.5F, 0.8F, -2.0F, 0.0F).wing(-0.12F, 0.8F, -0.3F)
			.hand(-8, 34, -62, -1, -0.6F, 0.1F, 0.6F).siphon(16, 46, -62, 1, -0.3F, 0.2F, 0.5F).drag(42, 0, -56, 58, 0, -22);
	// the Hyphae Lances: the wings rise with the spars pointing down, then stab them into the floor
	private static final Pose LANCES_RISE = new Pose().set(0.22F, 0.05F, 0.1F, 0.9F, 0.6F, 0.1F, -0.2F, 2.0F, 0.2F).wing(0.15F, 0.15F, 1.05F)
			.hand(-44, 74, -30, -1, -0.5F, 0.5F, 0.1F).siphon(44, 74, -28, 1, -0.5F, 0.5F, -0.2F).drag(50, 0, -58, 62, 0, -24)
			.spar(-0.3F);
	private static final Pose LANCES = new Pose().set(-0.12F, -0.1F, -0.15F, 1.0F, 0.85F, 0.0F, 0.6F, -1.5F, 0.3F).wing(-0.2F, 0.55F, -0.6F)
			.hand(-34, 40, -56, -1, -0.3F, 0.3F, 0.2F).siphon(34, 42, -52, 1, -0.3F, 0.3F, 0.0F).drag(52, 0, -62, 64, 0, -24)
			.spar(0.8F);
	// the Lattice Net: the wings spread forward and open flat, the arms flung wide
	private static final Pose NET = new Pose().set(0.08F, 0.05F, 0.05F, 1.0F, 0.5F, 0.1F, 0.3F, 1.0F, 0.1F).wing(0.1F, 0.95F, 0.4F)
			.hand(-50, 66, -44, -1, -0.4F, 0.5F, 0.0F).siphon(50, 66, -42, 1, -0.4F, 0.5F, -0.3F).drag(46, 0, -60, 60, 0, -26)
			.spar(-0.35F);
	// the Gaze: the collar flared to frame the pulp, which pushes forward with the tentacles drawn back; the head turns
	// after the beam (added in attackPose)
	private static final Pose GAZE = new Pose().set(0.05F, 0.02F, -0.04F, 1.0F, 0.9F, 0.0F, -0.5F, 1.0F, 0.2F).wing(0.12F, 0.3F, 0.5F)
			.hand(-40, 56, -40, -1, -0.4F, 0.4F, 0.2F).siphon(40, 56, -38, 1, -0.4F, 0.4F, 0.0F).drag(48, 0, -60, 62, 0, -24)
			.pulp(4.0F);
	// the Last Brood: she rears and roars with the collar over-flared, then squats anchored with the sac swollen open
	// toward the player and the arms spread to receive them; cut free, she collapses with the sac torn and slack
	private static final Pose LAST_BROOD_REAR = new Pose().set(0.45F, -0.05F, -0.1F, 1.2F, 1.0F, 0.0F, -0.5F, 4.0F, 0.8F).wing(0.15F, 0.1F, 0.7F)
			.hand(-56, 96, -20, -1, -0.5F, 0.5F, 0.0F).siphon(56, 94, -18, 1, -0.5F, 0.5F, -0.3F).drag(54, 58, -50, 64, 18, -32)
			.tail(0.3F, 0.0F, 0.1F, 0.0F).swell(0.5F);
	private static final Pose LAST_BROOD_PULL = new Pose().set(0.1F, -0.1F, -0.15F, 1.0F, 0.7F, 0.2F, 0.9F, -3.0F, 0.6F).wing(0.2F, 0.7F, 0.3F)
			.hand(-60, 56, -54, -1, -0.2F, 0.4F, 0.0F).siphon(60, 56, -52, 1, -0.2F, 0.4F, -0.2F).drag(64, 0, -70, 76, 0, -18)
			.tail(-0.1F, 0.0F, -0.04F, 0.0F).swell(1.0F);
	private static final Pose LAST_BROOD_COLLAPSE = new Pose().set(-0.32F, -0.4F, -0.45F, -0.2F, 0.4F, 0.7F, 0.9F, -12.0F, 0.1F).wing(-0.3F, -1.1F, -0.4F)
			.hand(-34, 10, -56, -1, -0.5F, 0.2F, 0.15F).siphon(20, 22, -72, 1, 0.2F, 0.2F, -0.1F).drag(60, 0, -50, 72, 0, -14)
			.tail(-0.3F, 0.0F, -0.08F, 0.0F).swell(-0.2F).spar(0.6F);
	/** Ticks each rake or slam takes to strike (arriving on its hit tick) and to ease back out after it. */
	private static final float SWING_TICKS = 4.0F;

	private final Pose pose = new Pose();
	private final Pose poseA = new Pose();
	private final Pose poseB = new Pose();
	private static final Vector3f DOWN = new Vector3f(0.0F, 1.0F, 0.0F);
	private final Quaternionf upperQ = new Quaternionf();
	private final Quaternionf foreQ = new Quaternionf();
	private final Vector3f euler = new Vector3f();
	private final Vector3f siphonAim = new Vector3f();
	/** 0..1: how far the siphon has swung onto its latched target (0 when there is none). */
	private float siphonLatch;
	/** Intro accents: how still the facial tentacles hold (0..1), the roar's tremble (0..1), the dropped fungling's fall (-1 = none). */
	private float tentacleCalm;
	private float roarTremble;
	/** The split's strain before the skull gives (0..1): the head shudders on its neck. */
	private float splitShudder;
	private float fungDrop = -1.0F;
	private float bodyBaseY = Float.NaN;
	private float thoraxBaseY;
	private float thoraxBaseZ;

	/** A blockout point (pixels, Y up) as model units relative to the ground part. */
	private static float bx(float x) { return -x / 2.0F; }
	private static float by(float y) { return -y / 2.0F; }
	private static float bz(float z) { return z / 2.0F; }

	@Override
	public void setupAnim(MycophantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if (Float.isNaN(this.bodyBaseY)) {
			this.bodyBaseY = this.body.y;
			this.thoraxBaseY = this.thorax.y;
			this.thoraxBaseZ = this.thorax.z;
		}
		float partial = ageInTicks - entity.tickCount;
		float time = ageInTicks / 20.0F;
		// every synced counter is replayed on the client clock, so a burst or a gap in the server's updates never stalls
		// or skips a beat
		MycophantAnimState motion = MycophantAnimState.of(entity);
		motion.update(entity, partial);
		MycophantState state = entity.getState();
		float gait = motion.gait(partial);
		float split = entity.getPhase() >= 2 ? 1.0F : 0.0F;
		float splitT = state == MycophantState.SPLIT ? motion.splitTime() : (split > 0 ? 999.0F : 0.0F);
		// the siphon swings onto its target and, once it lets go, back off it along the last line it fed on
		float latch = motion.latch(partial);
		boolean aimed = entity.getSiphonTargetId() >= 0 && aimSiphon(entity, partial);
		if (aimed) motion.rememberAim(this.siphonAim);
		this.siphonLatch = latch > 0.0F && (aimed || motion.recallAim(this.siphonAim)) ? latch : 0.0F;
		this.tentacleCalm = 0.0F;
		this.roarTremble = 0.0F;
		this.splitShudder = 0.0F;
		this.fungDrop = -1.0F;
		boolean cradle = false;

		String review = reviewPose;
		if (review != null) {
			boolean opened = review.endsWith("_p2");
			String name = opened ? review.substring(0, review.length() - 3) : review;
			this.pose.copy(switch (name) {
				case "tending" -> TENDING;
				case "notice" -> NOTICE;
				case "roar" -> ROAR;
				case "embrace" -> EMBRACE;
				case "hold" -> HOLD;
				case "spawn" -> SPAWN;
				case "guard" -> GUARD;
				case "sweep" -> SWEEP;
				case "siphon" -> SIPHON_AIM;
				case "pluck" -> PLUCK;
				case "bloom" -> BLOOM;
				case "stagger" -> STAGGER;
				case "yield" -> YIELDED;
				case "rake_l" -> RAKE_L;
				case "rake_r" -> RAKE_R;
				case "slam" -> SLAM;
				case "settle_rise" -> SETTLE_RISE;
				case "settle_drop" -> SETTLE_DROP;
				case "pin" -> PIN;
				case "tail_coil" -> TAIL_COIL;
				case "tail_lash" -> TAIL_LASH;
				case "vents_open" -> VENTS_OPEN;
				case "hand_wind" -> HAND_WIND;
				case "hand_sweep" -> HAND_SWEEP;
				case "spit" -> SPIT;
				case "mortar" -> MORTAR;
				case "nurse" -> NURSE;
				case "lances" -> LANCES;
				case "net" -> NET;
				case "gaze" -> GAZE;
				case "last_brood_rear" -> LAST_BROOD_REAR;
				case "last_brood_pull" -> LAST_BROOD_PULL;
				case "last_brood_collapse" -> LAST_BROOD_COLLAPSE;
				default -> STALK;
			});
			gait = name.equals("drag") ? 1.0F : 0.0F;
			cradle = name.equals("tending");
			// the wing and pulp moves only exist with the head opened
			boolean phaseTwo = switch (name) {
				case "lances", "net", "gaze", "last_brood_rear", "last_brood_pull", "last_brood_collapse" -> true;
				default -> false;
			};
			splitT = opened || phaseTwo ? 999.0F : 0.0F;
		} else {
			switch (state) {
				case INTRO -> cradle = introPose(entity, motion.introTime());
				case ATTACK -> attackPose(entity, motion, partial, ageInTicks);
				case STAGGER -> {
					float s = motion.stateTime();
					if (motion.staggeredOutOfLastBrood()) {
						lastBroodCollapse(s, motion.lastBroodCutFree());
					} else {
						float rise = HemoEasing.smoothstep(s / 6.0F) * (1.0F - HemoEasing.smoothstep((s - 10.0F) / 20.0F));
						this.pose.mix(STALK, STAGGER, rise);
					}
				}
				case SPLIT -> splitPose(splitT);
				case YIELD -> {
					float y = motion.yieldTime();
					this.pose.mix(STALK, YIELDED, HemoEasing.smoothstep(MycophantYieldRules.lower(y) * 0.6F + MycophantYieldRules.fold(y) * 0.4F));
					this.pose.frill = Mth.lerp(MycophantYieldRules.fold(y), STALK.frill, -0.3F);
				}
				default -> this.pose.copy(STALK);
			}
			blendFromLast(entity, motion, ageInTicks);
		}
		apply(entity, time, gait, ageInTicks, splitT, cradle);
	}

	/**
	 * A new state, attack or hold starts from wherever she was drawn last, not from the authored start pose: a stagger
	 * landing mid-swing, a catch mid-lunge or a release mid-squeeze all ease over instead of popping.
	 */
	private void blendFromLast(MycophantEntity entity, MycophantAnimState motion, float age) {
		MycophantState state = entity.getState();
		int attack = state == MycophantState.ATTACK ? entity.getAttack().ordinal() + 1 : 0;
		motion.begin((state.ordinal() * 64 + attack) * 2 + (entity.isHolding() ? 1 : 0), age, Pose.SIZE);
		float blend = motion.blend(age);
		if (blend < 1.0F) this.pose.mix(this.poseA.read(motion.from()), this.pose, blend);
		this.pose.write(motion.last());
	}

	/**
	 * The waking, beat by beat: she tends the fungling with her back turned, perks up and goes still, pivots round on
	 * her claws, lets the fungling fall, draws back low, rears into the roar, then settles forward into the rush's
	 * starting stance so the Rushing Embrace's wind-up picks up without a pop. Returns true while she still cradles it.
	 */
	private boolean introPose(MycophantEntity entity, float t) {
		boolean rematch = entity.isRematchIntro();
		float notice = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.NOTICE, rematch);
		float roar = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.ROAR, rematch);
		float rush = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.RUSH, rematch);
		float drop = rematch ? -1.0F : MycophantIntroRules.tickOf(MycophantIntroRules.Beat.DROP, false);
		float settle = rush - 18.0F;
		if (t >= roar - 10.0F) {
			roarPose(t - roar, settle - roar, rush - settle);
			return false;
		}
		if (!rematch && t < notice) {
			// tending: rocking over the fungling, the siphon feeding it in slow swallows, the hand stroking
			this.pose.copy(TENDING);
			float rock = Mth.sin(t * 0.09F);
			this.pose.head += rock * 0.07F;
			this.pose.neck += rock * 0.03F;
			this.pose.curl += Mth.sin(t * 0.21F) * 0.18F;
			this.pose.grip += Mth.sin(t * 0.13F) * 0.08F;
			return true;
		}
		// the notice: the head snaps up a little past its mark and the tentacles freeze, listening
		float perk = HemoEasing.smoothstep((t - notice) / 7.0F);
		float overshoot = Mth.sin(Mth.clamp((t - notice) / 14.0F, 0.0F, 1.0F) * Mth.PI) * 0.12F;
		if (rematch) this.pose.copy(NOTICE);
		else this.pose.mix(TENDING, NOTICE, perk);
		this.pose.head += overshoot;
		this.tentacleCalm = perk;
		if (rematch || t < drop) return !rematch;
		// the drop: the hand opens and lowers, and the fungling falls away into the nectar
		float release = HemoEasing.smoothstep((t - drop) / 6.0F);
		this.pose.mix(this.poseA.copy(this.pose), STALK, HemoEasing.smoothstep((t - drop) / 16.0F));
		this.pose.grip = Mth.lerp(release, NOTICE.grip, 0.0F);
		this.fungDrop = Mth.clamp((t - drop) / 9.0F, 0.0F, 1.0F);
		return t < drop + 9.0F;
	}

	/** The roar from ten ticks before it (r = ticks since the roar): draw back low, rear and bellow, then settle forward. */
	private void roarPose(float r, float hold, float settleTicks) {
		Pose crouch = this.poseB.copy(STALK);
		crouch.thorax = -0.18F;
		crouch.neck = -0.2F;
		crouch.head = -0.15F;
		crouch.height = -2.5F;
		crouch.frill = 0.2F;
		crouch.maw = 0.1F;
		if (r < 0.0F) {
			this.pose.mix(STALK, crouch, HemoEasing.smoothstep((r + 10.0F) / 10.0F));
			this.tentacleCalm = 1.0F - HemoEasing.smoothstep((r + 10.0F) / 10.0F);
		} else if (r < hold) {
			// a fast rear with a small overshoot, then the bellow held with a tremble
			float rear = HemoEasing.smoothstep(r / 6.0F);
			this.pose.mix(crouch, ROAR, rear);
			this.pose.thorax += Mth.sin(Mth.clamp(r / 12.0F, 0.0F, 1.0F) * Mth.PI) * 0.08F;
			this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((r - hold + 8.0F) / 8.0F));
			this.pose.maw = ROAR.maw * (0.85F + Mth.sin(r * 1.7F) * 0.15F * this.roarTremble);
		} else {
			// settle low and forward: the stance the Rushing Embrace's wind-up starts from
			this.pose.mix(ROAR, STALK, HemoEasing.smoothstep((r - hold) / settleTicks));
		}
	}

	/**
	 * The split, by the controller's tick: she hunches and strains with her head bowed while the seam opens, rears into a
	 * bellow as the halves snap apart, throbs forward at the push, lifts into a display as the wings unfurl, then lowers
	 * into her ordinary stance so phase 2 picks up without a pop.
	 */
	private void splitPose(float s) {
		float snap = MycophantSplitRules.snapTick();
		if (s < snap) {
			this.pose.mix(STALK, STRAIN, HemoEasing.smoothstep(s / 8.0F));
			this.splitShudder = MycophantSplitRules.crack(s);
			return;
		}
		float r = s - snap;
		float rear = HemoEasing.smoothstep(r / 5.0F);
		float wings = MycophantSplitRules.wings(s);
		this.pose.mix(STRAIN, this.poseB.mix(ROAR, SPLIT_DISPLAY, wings), rear);
		this.pose.thorax += Mth.sin(Mth.clamp(r / 10.0F, 0.0F, 1.0F) * Mth.PI) * 0.1F;
		// the push: a forward throb as the pulp blooms and the shove lands
		float throb = Mth.sin(Mth.clamp((s - MycophantSplitRules.pushTick() + 3.0F) / 10.0F, 0.0F, 1.0F) * Mth.PI);
		this.pose.thorax -= throb * 0.12F;
		this.pose.head -= throb * 0.1F;
		this.pose.tentFwd += throb * 0.6F;
		// the bellow trembles until the wings take over
		this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((s - 46.0F) / 12.0F));
		this.pose.maw *= 0.85F + Mth.sin(r * 1.7F) * 0.15F * this.roarTremble;
		// the collar stays folded through the snap so the halves swing clear in silhouette, then flares as the pulp blooms
		this.pose.frill = Mth.lerp(HemoEasing.smoothstep((s - 24.0F) / 14.0F), STRAIN.frill, this.pose.frill);
		float settle = MycophantSplitRules.settle(s);
		if (settle > 0.0F) this.pose.mix(this.poseA.copy(this.pose), STALK, settle);
	}

	private void attackPose(MycophantEntity entity, MycophantAnimState motion, float partial, float ageInTicks) {
		MycophantAttack attack = entity.getAttack();
		int phase = entity.getPhase();
		int variant = entity.getAttackVariant();
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase, variant);
		float t = motion.attackTime();
		float w = Math.max(1, timeline.windup());
		float a = Math.max(1, timeline.active());
		float r = Math.max(1, timeline.recovery());
		float wind = HemoEasing.smoothstep(t / w);
		float act = HemoEasing.smoothstep((t - w) / a);
		float rec = HemoEasing.smoothstep((t - w - a) / r);
		switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> {
				if (entity.isHolding()) {
					// the attack clock stands still through a hold, so the slow head work runs on the entity's age
					this.pose.copy(HOLD);
					float squeeze = motion.claimMeter(partial);
					this.pose.grip = Mth.lerp(squeeze, 0.7F, 1.0F);
					this.pose.thorax -= squeeze * 0.08F;
					this.pose.head += Mth.sin(ageInTicks * 0.2F) * 0.05F;
				} else if (t < w) {
					this.pose.mix(STALK, ROAR, wind);
				} else if (t < w + a) {
					this.pose.mix(ROAR, EMBRACE, HemoEasing.smoothstep((t - w) / (a * 0.5F)));
				} else {
					this.pose.mix(EMBRACE, STALK, rec);
				}
				if (attack == MycophantAttack.FUSION_EMBRACE && !entity.isHolding()) this.pose.wing[1] = Mth.lerp(act, 0.4F, 1.3F);
			}
			case SIPHON_FEED -> {
				if (t < w) this.pose.mix(STALK, SIPHON_AIM, wind);
				else if (t < w + a) this.pose.copy(SIPHON_AIM);
				else this.pose.mix(SIPHON_AIM, STALK, rec);
			}
			case PLUCK -> {
				if (t < w) this.pose.mix(STALK, PLUCK_WIND, wind);
				else if (t < w + a) this.pose.mix(PLUCK_WIND, PLUCK, act);
				else this.pose.mix(PLUCK, STALK, rec);
			}
			case RAKE_STRING -> rakeString(variant, phase, t, w, w + a + r);
			case BROOD_CALL, NECTAR_SURGE -> {
				if (t < w) this.pose.mix(STALK, SPAWN, wind);
				else if (t < w + a) this.pose.copy(SPAWN);
				else this.pose.mix(SPAWN, STALK, rec);
				if (attack == MycophantAttack.NECTAR_SURGE) this.pose.frill = Math.max(this.pose.frill, wind * 0.9F);
			}
			case WING_GUARD -> {
				if (t < w) this.pose.mix(STALK, GUARD, wind);
				else if (t < w + a) this.pose.copy(GUARD);
				else this.pose.mix(GUARD, STALK, rec);
			}
			case WING_SWEEP -> {
				if (t < w) this.pose.mix(STALK, SWEEP_WIND, wind);
				else if (t < w + a) this.pose.mix(SWEEP_WIND, SWEEP, HemoEasing.smoothstep((t - w) / (a * 0.4F)));
				else this.pose.mix(SWEEP, STALK, rec);
			}
			case PULP_BLOOM -> {
				if (t < w) this.pose.mix(STALK, BLOOM_WIND, wind);
				else if (t < w + a) this.pose.mix(BLOOM_WIND, BLOOM, HemoEasing.smoothstep((t - w) / (a * 0.5F)));
				else this.pose.mix(BLOOM, STALK, rec);
			}
			case BROOD_SETTLE -> {
				if (t < w) {
					this.pose.mix(STALK, SETTLE_RISE, wind);
				} else if (t < w + a) {
					// the belly comes down fast, squashing a little as it lands
					this.pose.mix(SETTLE_RISE, SETTLE_DROP, HemoEasing.smoothstep((t - w) / 3.0F));
				} else {
					// she lies low and heaves through the punish window, getting up only at its end
					float lie = t - w - a;
					float up = HemoEasing.smoothstep((lie - (r - 14.0F)) / 14.0F);
					this.pose.mix(SETTLE_DROP, STALK, up);
					this.pose.height += Mth.sin(lie * 0.25F) * 0.5F * (1.0F - up);
				}
				this.pose.height -= bump((t - w - 2.0F) / 4.0F) * 1.5F;
			}
			case CLAW_PIN -> {
				if (entity.isHolding()) {
					// pressing down on the pinned player; the attack clock stands still through the pin
					this.pose.copy(PIN);
					this.pose.thorax += Mth.sin(ageInTicks * 0.35F) * 0.02F;
					this.pose.reachR[1] -= Math.abs(Mth.sin(ageInTicks * 0.3F)) * 1.5F;
				} else if (t < w) {
					this.pose.mix(STALK, PIN_WIND, wind);
				} else if (t < w + a) {
					this.pose.mix(PIN_WIND, PIN, act);
				} else {
					this.pose.mix(PIN, STALK, rec);
				}
			}
			case TAIL_LASH -> {
				// coil through the wind-up, then lash in the swing ticks up to each hit, across and (variant 1) back
				int[] hits = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.tailHits(variant);
				this.pose.mix(STALK, TAIL_COIL, HemoEasing.smoothstep(t / Math.max(1.0F, w + hits[0] - SWING_TICKS)));
				for (int i = 0; i < hits.length; i++) {
					float lash = HemoEasing.smoothstep((t - (w + hits[i] - SWING_TICKS)) / SWING_TICKS);
					if (lash > 0.0F) this.pose.mix(this.poseA.copy(this.pose), i % 2 == 0 ? TAIL_LASH : TAIL_LASH_BACK, lash);
				}
				if (rec > 0.0F) this.pose.mix(this.poseA.copy(this.pose), STALK, rec);
			}
			case VENT_BLOOM -> {
				if (t < w) this.pose.mix(STALK, VENTS_OPEN, wind);
				else if (t < w + a) this.pose.copy(VENTS_OPEN);
				else this.pose.mix(VENTS_OPEN, STALK, rec);
				// the cloud puffs out as the move commits
				float puff = bump((t - w) / a);
				this.pose.vents += puff * 0.4F;
				this.pose.abdLift += puff * 0.08F;
			}
			case HAND_SWEEP -> {
				if (t < w) {
					this.pose.mix(STALK, HAND_WIND, wind);
				} else if (t < w + a) {
					// the hand swings out ahead of her on its way across, staying low
					float s = HemoEasing.smoothstep((t - w) / a);
					this.pose.mix(HAND_WIND, HAND_SWEEP, s);
					float arc = Mth.sin(s * Mth.PI);
					this.pose.hand[2] -= arc * 24.0F;
					this.pose.hand[1] -= arc * 4.0F;
				} else {
					this.pose.mix(HAND_SWEEP, STALK, rec);
				}
			}
			case SIPHON_SPIT -> {
				if (t < w) this.pose.mix(STALK, SPIT, wind);
				else if (t < w + a) this.pose.copy(SPIT);
				else this.pose.mix(SPIT, STALK, rec);
				// each glob swells the siphon and spits forward, peaking on its release tick
				float spit = 0.0F;
				int[] globs = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.globTicks(phase);
				for (int g : globs) spit += bump((t - w - g + 2.0F) / 4.0F);
				this.pose.siphonT[1] -= spit * 8.0F;
				this.pose.siphonT[2] -= spit * 14.0F;
				this.pose.curl += spit * 0.6F;
				this.pose.thorax += spit * 0.04F;
				this.pose.maw += spit * 0.2F;
			}
			case SPORE_MORTAR -> {
				if (t < w) this.pose.mix(STALK, MORTAR, wind);
				else if (t < w + a) this.pose.copy(MORTAR);
				else this.pose.mix(MORTAR, STALK, rec);
				// the vents pump twice through the tell, then three times as the eggs go up
				float pump = t < w ? 0.4F * sinSquared(t / w * 2.0F) : (t < w + a ? sinSquared((t - w) / a * 3.0F) : 0.0F);
				this.pose.abdLift += pump * 0.14F;
				this.pose.vents += pump * 0.35F;
				this.pose.height += pump * 0.8F;
			}
			case NURSING -> {
				if (t < w) this.pose.mix(STALK, NURSE, wind);
				else if (t < w + a) this.pose.copy(NURSE);
				else this.pose.mix(NURSE, STALK, rec);
				// rocking over the fungling in slow swallows, on the entity's age so a broken feeding never jumps
				float tend = HemoEasing.smoothstep((t - w) / 6.0F) * (1.0F - rec);
				float rock = Mth.sin(ageInTicks * 0.09F);
				this.pose.head += rock * 0.07F * tend;
				this.pose.neck += rock * 0.03F * tend;
				this.pose.curl += Mth.sin(ageInTicks * 0.21F) * 0.18F * tend;
				this.pose.grip += Mth.sin(ageInTicks * 0.13F) * 0.08F * tend;
			}
			case HYPHAE_LANCES -> {
				if (t < w) {
					this.pose.mix(STALK, LANCES_RISE, wind);
				} else if (t < w + a) {
					this.pose.mix(LANCES_RISE, LANCES, HemoEasing.smoothstep((t - w) / SWING_TICKS));
				} else {
					this.pose.mix(LANCES, STALK, rec);
				}
				// the spars stay driven in, straining, while the line erupts
				this.pose.thorax += Mth.sin(ageInTicks * 1.7F) * 0.012F * act * (1.0F - rec);
			}
			case LATTICE_NET -> {
				if (t < w) this.pose.mix(STALK, NET, wind);
				else if (t < w + a) this.pose.copy(NET);
				else this.pose.mix(NET, STALK, rec);
				// the cast: the wings throw forward as the net goes out
				float cast = bump((t - w) / a);
				this.pose.wing[1] += cast * 0.3F;
				this.pose.thorax -= cast * 0.1F;
				this.pose.spar -= cast * 0.2F;
			}
			case GAZE -> {
				if (t < w) this.pose.mix(STALK, GAZE, wind);
				else if (t < w + a) this.pose.copy(GAZE);
				else this.pose.mix(GAZE, STALK, rec);
				// once the beam sweeps, the head follows it (relative to her body, as far as the neck allows)
				float follow = HemoEasing.smoothstep((t - w) / SWING_TICKS) * (1.0F - rec);
				float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
				float relative = Mth.clamp(Mth.wrapDegrees(motion.gazeYaw(partial) - bodyYaw), -70.0F, 70.0F);
				this.pose.turn += relative * Mth.DEG_TO_RAD * follow;
				this.pose.pulpFwd += Mth.sin(ageInTicks * 0.9F) * 0.4F * follow;
			}
			case LAST_BROOD -> lastBrood(entity, t, w, ageInTicks);
			case NONE -> this.pose.copy(STALK);
		}
	}

	/**
	 * The Rake String: left, right (and left again in phase 2's long form), then for variants 1 and 2 a crouched
	 * hesitation with both claws cocked overhead and the slam. Each blow strikes in the {@link #SWING_TICKS} up to its
	 * hit and eases back out over as many after it (the last one over the recovery); the next claw lifts while the last
	 * one returns, so the string flows without a pause between blows.
	 */
	private void rakeString(int variant, int phase, float t, float w, float end) {
		int[] hits = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.stringHits(variant, phase);
		this.pose.copy(STALK);
		float previous = 0.0F;
		for (int i = 0; i < hits.length; i++) {
			float hit = w + hits[i];
			boolean slam = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.isSlam(variant, i, hits.length);
			boolean last = i == hits.length - 1;
			boolean left = i % 2 == 0;
			Pose up = slam ? SLAM_COCK : (left ? RAKE_L_UP : RAKE_R_UP);
			Pose struck = slam ? SLAM : (left ? RAKE_L : RAKE_R);
			float strikeStart = hit - SWING_TICKS;
			// the slam cocks quickly, then holds the hesitation until it drops
			float raiseTicks = Math.max(1.0F, slam ? Math.min(6.0F, strikeStart - previous) : strikeStart - previous);
			float raise = HemoEasing.smoothstep((t - previous) / raiseTicks);
			float strike = HemoEasing.smoothstep((t - strikeStart) / SWING_TICKS);
			float back = last ? HemoEasing.smoothstep((t - hit - 3.0F) / Math.max(SWING_TICKS, (end - hit - 3.0F) * 0.7F))
					: HemoEasing.smoothstep((t - hit) / SWING_TICKS);
			this.pose.add(STALK, up, raise * (1.0F - strike));
			this.pose.add(STALK, struck, strike * (1.0F - back));
			if (slam) {
				// the hesitation creeps: she sinks a little lower and the claws lift a little higher before they drop
				float creep = HemoEasing.smoothstep((t - previous - raiseTicks) / Math.max(1.0F, strikeStart - previous - raiseTicks))
						* (1.0F - strike);
				this.pose.height -= creep * 1.2F;
				this.pose.thorax += creep * 0.04F;
				this.pose.reachL[1] += creep * 6.0F;
				this.pose.reachR[1] += creep * 6.0F;
			}
			previous = hit;
		}
	}

	/**
	 * The Last Brood: she rears and roars through the tell, sinks into the anchored pull with the sac swollen open as it
	 * runs out, and strains there; a cocooned player is held against the opened sac.
	 */
	private void lastBrood(MycophantEntity entity, float t, float w, float ageInTicks) {
		if (entity.isHolding()) {
			this.pose.copy(HOLD);
			this.pose.sac = 1.0F;
			this.pose.frill = 1.0F;
			this.pose.grip = 0.9F;
			this.pose.thorax -= 0.06F;
			this.pose.head += Mth.sin(ageInTicks * 0.2F) * 0.05F;
			return;
		}
		if (t < w) {
			float rear = HemoEasing.smoothstep(t / 8.0F);
			this.pose.mix(STALK, LAST_BROOD_REAR, rear);
			this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((t - (w - 14.0F)) / 6.0F));
			this.pose.maw *= 0.85F + Mth.sin(t * 1.7F) * 0.15F * this.roarTremble;
			this.pose.mix(this.poseA.copy(this.pose), LAST_BROOD_PULL, HemoEasing.smoothstep((t - (w - 8.0F)) / 8.0F));
			return;
		}
		this.pose.copy(LAST_BROOD_PULL);
		float strain = HemoEasing.smoothstep((t - w) / 10.0F);
		this.pose.thorax += Mth.sin(ageInTicks * 1.9F) * 0.02F * strain;
		this.pose.head += Mth.sin(ageInTicks * 1.3F) * 0.03F * strain;
		this.pose.sac += Mth.sin(ageInTicks * 0.45F) * 0.06F * strain;
	}

	/**
	 * The stagger after the Last Brood: cut free, she falls from the pull onto her belly with the sac torn and lies
	 * there through the collapse, rising only at its end; spent, she sags halfway for the shorter stagger.
	 */
	private void lastBroodCollapse(float s, boolean cutFree) {
		float duration = cutFree ? com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.COLLAPSE
				: com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.SPENT;
		Pose low = cutFree ? LAST_BROOD_COLLAPSE : this.poseB.mix(STALK, LAST_BROOD_COLLAPSE, 0.55F);
		this.pose.mix(LAST_BROOD_PULL, low, HemoEasing.smoothstep(s / 10.0F));
		this.pose.height -= bump((s - 7.0F) / 5.0F) * (cutFree ? 1.5F : 0.8F);
		this.pose.mix(this.poseA.copy(this.pose), STALK, HemoEasing.smoothstep((s - (duration - 16.0F)) / 16.0F));
	}

	/** 0 outside 0..1, rising to 1 at the middle and back with no jolt at either end. */
	private static float bump(float x) {
		return x <= 0.0F || x >= 1.0F ? 0.0F : sinSquared(x);
	}

	/** sin squared of x half-turns: x whole beats from 0 to 1 and back, smooth at every end. */
	private static float sinSquared(float x) {
		float s = Mth.sin(x * Mth.PI);
		return s * s;
	}

	/** Points the siphon at its latched target; returns false when the target is gone. */
	private boolean aimSiphon(MycophantEntity entity, float partial) {
		net.minecraft.world.entity.Entity target = entity.level().getEntity(entity.getSiphonTargetId());
		if (target == null) return false;
		double dx = Mth.lerp(partial, target.xo, target.getX()) - Mth.lerp(partial, entity.xo, entity.getX());
		double dy = Mth.lerp(partial, target.yo, target.getY()) + target.getBbHeight() * 0.6D - Mth.lerp(partial, entity.yo, entity.getY());
		double dz = Mth.lerp(partial, target.zo, target.getZ()) - Mth.lerp(partial, entity.zo, entity.getZ());
		float yaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
		double fwd = dx * -Mth.sin(yaw) + dz * Mth.cos(yaw);
		double right = dx * -Mth.cos(yaw) + dz * -Mth.sin(yaw);
		// model space relative to the ground part: right is -X, up is -Y, forward is -Z; 1 block = 8 units at 2x
		this.siphonAim.set((float) (-right * 8.0D), (float) (-dy * 8.0D), (float) (-fwd * 8.0D));
		return true;
	}

	/** {@code drag} is 0..1, how far the claws have fallen into the drag gait, so starting and stopping never pops. */
	private void apply(MycophantEntity entity, float time, float drag, float ageInTicks, float splitT, boolean cradle) {
		Pose p = this.pose;
		float breathe = Mth.sin(time * 1.3F);
		float gait = MycophantMotionRules.dragPhase(ageInTicks);
		float haul = Mth.sin(gait * Mth.TWO_PI * 2.0F) * drag;

		// body: bob, haul surge and sway
		this.body.y = this.bodyBaseY - (p.height + breathe * 0.6F + Math.max(0.0F, haul) * 1.2F) / 2.0F;
		this.body.z = -haul * 2.5F / 2.0F;
		this.body.zRot = Mth.sin(gait * Mth.TWO_PI) * 0.03F * drag;
		this.body.yRot = -Mth.sin(gait * Mth.TWO_PI) * 0.03F * drag;

		// thorax, neck and head
		this.thorax.xRot = -(p.thorax + breathe * 0.015F);
		this.thorax.yRot += p.turn * 0.15F;
		this.neck.xRot = -p.neck;
		this.neck.yRot += p.turn * 0.3F;
		this.head.xRot = -p.head;
		this.head.yRot = p.turn * 0.55F - Mth.sin(time * 0.6F) * 0.06F * (1.0F - this.tentacleCalm) + Mth.sin(ageInTicks * 2.3F) * 0.05F * this.roarTremble
				+ Mth.sin(ageInTicks * 3.3F) * 0.05F * this.splitShudder;
		this.head.zRot = Mth.sin(time * 0.8F) * 0.04F + Mth.sin(ageInTicks * 1.9F + 1.0F) * 0.06F * this.roarTremble
				+ Mth.sin(ageInTicks * 2.6F + 2.0F) * 0.06F * this.splitShudder;
		this.head.xRot += Mth.sin(ageInTicks * 4.1F) * 0.03F * this.splitShudder;

		// the abdomen breathes and pulses when spawning
		this.abdomen.xRot = p.abdLift - (0.03F + breathe * 0.01F + p.vents * Mth.sin(time * 6.0F) * 0.01F);
		this.abdomen.yRot = -p.abdYaw - Mth.sin(gait * Mth.TWO_PI) * 0.03F * drag;
		float pulse = 1.0F + p.vents * 0.03F * Mth.sin(time * 5.0F);
		this.abdomen.xScale = pulse;
		this.abdomen.yScale = pulse;
		for (ModelPart[] vent : this.ventPetals)
			for (int i = 0; i < vent.length; i++) vent[i].zRot = -p.vents * (1.0F + Mth.sin(time * 4.0F + i) * 0.12F);
		// the tendrils run along -Y: +xRot curls them up, +zRot (and +yRot at the root) swings them to her left
		for (int i = 0; i < this.tails.length; i++) {
			ModelPart[] chain = this.tails[i];
			for (int k = 0; k < chain.length; k++) {
				chain[k].xRot += -(0.12F + Mth.sin(time * 1.8F - k * 0.5F + i) * 0.1F) + p.tailCurl * (k == 0 ? 0.45F : 0.38F);
				chain[k].zRot += Mth.cos(time * 1.4F - k * 0.4F + i * 2.0F) * 0.08F;
				if (k == 0) chain[k].yRot -= p.tailSwing * 0.7F;
				else chain[k].zRot -= p.tailSwing * 0.12F * (1.0F + k * 0.15F);
			}
		}

		// the gestation sac breathes; its shell is drawn by the translucent sac layer only
		float sacPulse = (1.0F + Mth.sin(time * 3.1F) * 0.02F) * (1.0F + p.sac * 0.3F);
		this.sac.xScale = sacPulse;
		this.sac.yScale = sacPulse;
		this.sac.zScale = sacPulse;
		this.sac.z -= Math.max(0.0F, p.sac) * 3.0F;
		this.sacShell.visible = false;

		// the collar: folded back along the neck at rest, flared out around the face on display
		for (int i = 0; i < this.collarHinges.length; i++) {
			int row = i < 16 ? 0 : 1;
			this.collarHinges[i].xRot = -((1.45F - p.frill * 1.25F) + row * 0.12F + Mth.sin(time * 2.0F + i * 1.7F) * 0.03F);
		}
		float collarScale = 0.85F + Math.max(0.0F, p.frill) * 0.25F;
		this.collar.xScale = collarScale;
		this.collar.yScale = collarScale;
		this.collar.zScale = collarScale;

		// the mouth
		this.mandibleL.zRot = -p.maw * 0.5F;
		this.mandibleL.yRot = -p.maw * 0.25F;
		this.mandibleR.zRot = p.maw * 0.5F;
		this.mandibleR.yRot = p.maw * 0.25F;
		for (int k = 0; k < this.tongue.length; k++)
			this.tongue[k].xRot += -((k == 0 ? -p.tongue * 0.6F : 0.05F + p.tongue * 0.05F) + Mth.sin(time * 2.2F - k * 0.6F) * 0.07F);

		// facial tentacles: the whips, the short feeding ones and the forward reachers
		for (int i = 0; i < this.faceTentacles.length; i++) {
			ModelPart[] chain = this.faceTentacles[i];
			boolean whip = i < 2;
			boolean fwd = i >= 5;
			float side = i == 0 || i == 2 || i == 5 ? -1.0F : (i == 3 ? 0.2F : (i == 7 ? -0.3F : 1.0F));
			float phase = i * 1.37F;
			float sway = 1.0F - this.tentacleCalm * 0.85F;
			if (whip) { chain[0].xRot = 0.3F + p.tentFwd * 0.4F; chain[0].zRot = side * (0.9F - p.tentFwd * 0.4F); }
			else if (fwd) { chain[0].xRot = 0.9F + p.tentFwd * 0.5F; chain[0].zRot = side * 0.25F; }
			else { chain[0].xRot = 0.2F; chain[0].zRot = side * 0.1F; }
			for (int k = 1; k < chain.length; k++) {
				float speed = whip ? 1.6F : 2.4F;
				chain[k].xRot = (whip ? 0.06F : 0.08F) - Mth.sin(time * speed - k * 0.5F + phase) * (0.07F + k * 0.006F) * sway;
				chain[k].zRot = side * (whip ? 0.05F : 0.02F) + Mth.sin(time * speed - (k + 2) * 0.5F + phase) * 0.05F * sway;
			}
		}

		// phase 2: the skull strains along its seam, snaps, flings its halves out past their rest and lets them drop to hang
		// off the sides; the pulp swells forward through the gap and throbs at the push; the wings bud and unfurl
		boolean opened = splitT >= 999.0F;
		float crack = opened ? 1.0F : MycophantSplitRules.crack(splitT);
		float hinge = opened ? 1.0F : MycophantSplitRules.hinge(splitT);
		float droop = opened ? 1.0F : MycophantSplitRules.droop(splitT);
		float bloom = opened ? 1.0F : MycophantSplitRules.pulp(splitT);
		float wings = opened ? 1.0F : MycophantSplitRules.wings(splitT);
		// before the snap the seam only ever parts outward, in shudders, so the halves never pass through each other
		float seam = hinge > 0.0F ? 0.0F : crack * (0.025F + 0.035F * Math.abs(Mth.sin(ageInTicks * 2.7F)));
		float swing = hinge * 1.25F + seam;
		this.skullHalfL.xRot = -droop * 0.35F;
		this.skullHalfL.yRot = -swing;
		this.skullHalfL.zRot = droop * 0.55F;
		this.skullHalfR.xRot = -droop * 0.35F;
		this.skullHalfR.yRot = swing;
		this.skullHalfR.zRot = -droop * 0.55F;
		this.pulp.visible = opened || hinge > 0.0F;
		float pushThrob = opened ? 0.0F : Mth.sin(Mth.clamp((splitT - MycophantSplitRules.pushTick() + 2.0F) / 10.0F, 0.0F, 1.0F) * Mth.PI);
		float pulpScale = Mth.lerp(bloom, 0.5F, 1.0F) + pushThrob * 0.08F;
		this.pulp.xScale = pulpScale;
		this.pulp.yScale = pulpScale;
		this.pulp.zScale = pulpScale;
		this.pulp.z += (1.0F - bloom) * 2.0F - p.pulpFwd / 2.0F;
		float writhe = Mth.lerp(bloom, 0.3F, 1.0F);
		for (int i = 0; i < this.pulpTentacles.length; i++) {
			ModelPart[] chain = this.pulpTentacles[i];
			for (int k = 0; k < chain.length; k++) {
				chain[k].xRot += (1.0F - bloom) * 0.5F - Mth.sin(time * 3.0F + i + k * 0.8F) * 0.25F * writhe;
				chain[k].zRot += Mth.cos(time * 2.4F + i * 2.0F + k * 0.6F) * 0.25F * writhe;
			}
		}
		// each spar buds coiled like a fiddlehead with the lattice bunched, swells fast, then unrolls base first while the
		// spars fan out to their angles; a shiver runs through the lattice once it is open
		this.wingL.visible = opened || wings > 0.0F;
		this.wingR.visible = this.wingL.visible;
		float swell = 1.0F - (1.0F - wings) * (1.0F - wings) * (1.0F - wings);
		float wingScale = Mth.lerp(swell, 0.08F, 1.0F);
		this.wingL.xScale = this.wingL.yScale = this.wingL.zScale = wingScale;
		this.wingR.xScale = this.wingR.yScale = this.wingR.zScale = wingScale;
		float flap = Mth.sin(time * 1.1F) * 0.03F;
		this.wingL.xRot = -p.wing[0];
		this.wingL.yRot = p.wing[1];
		this.wingL.zRot = -p.wing[2] + flap;
		this.wingR.xRot = -p.wing[0];
		this.wingR.yRot = -p.wing[1];
		this.wingR.zRot = p.wing[2] - flap;
		float fan = HemoEasing.smoothstep((wings - 0.2F) / 0.6F);
		float shiver = opened ? 0.0F : Mth.sin(Mth.clamp((splitT - 62.0F) / 16.0F, 0.0F, 1.0F) * Mth.PI);
		for (int s = 0; s < this.wingSpars.length; s++) {
			ModelPart[] chain = this.wingSpars[s];
			boolean left = s < this.wingSpars.length / 2;
			float bunch = Mth.wrapDegrees(((left ? Mth.PI - 0.55F : 0.55F) - chain[0].zRot) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
			chain[0].zRot += bunch * (1.0F - fan);
			for (int k = 1; k < chain.length; k++) {
				float start = 0.15F + 0.6F * (k - 1) / (chain.length - 1);
				chain[k].zRot += (left ? 0.75F : -0.75F) * (1.0F - HemoEasing.smoothstep((wings - start) / 0.25F))
						+ (left ? 0.15F : -0.15F) * p.spar
						+ (left ? -1.0F : 1.0F) * Mth.sin(time * 1.4F - k * 0.5F + s) * 0.025F
						+ Mth.sin(time * 9.0F - k * 0.9F + s) * 0.05F * shiver;
			}
		}

		// the four claws drag the belly along; the front pair rake, slam and pin through their reach offsets
		float[] footholds = new float[3];
		for (int c = 0; c < 4; c++) {
			boolean front = c < 2;
			float side = c % 2 == 0 ? -1.0F : 1.0F;
			ModelPart upper = front ? (side < 0 ? this.clawFrontLUpper : this.clawFrontRUpper) : (side < 0 ? this.clawSecondLUpper : this.clawSecondRUpper);
			ModelPart fore = front ? (side < 0 ? this.clawFrontLFore : this.clawFrontRFore) : (side < 0 ? this.clawSecondLFore : this.clawSecondRFore);
			ModelPart hand = front ? (side < 0 ? this.clawFrontLHand : this.clawFrontRHand) : (side < 0 ? this.clawSecondLHand : this.clawSecondRHand);
			ModelPart dactyl = front ? (side < 0 ? this.clawFrontLDactyl : this.clawFrontRDactyl) : (side < 0 ? this.clawSecondLDactyl : this.clawSecondRDactyl);
			float[] base = front ? p.front : p.second;
			float tx = base[0] * side;
			float ty = base[1];
			float tz = base[2];
			if (drag > 0.0F) {
				float pair = (front == (side > 0)) ? 0.5F : 0.0F;
				float ph = (gait + pair) % 1.0F;
				if (ph < 0.6F) tz += 22.0F * (ph / 0.6F - 0.5F) * drag;
				else {
					float u = (ph - 0.6F) / 0.4F;
					tz += 22.0F * (0.5F - u) * drag;
					ty += Mth.sin(u * Mth.PI) * 16.0F * drag;
				}
			}
			float[] reach = side < 0 ? p.reachL : p.reachR;
			if (front) {
				tx += reach[0];
				ty += reach[1];
				tz += reach[2];
			}
			footholds[0] = bx(tx) - this.body.x;
			footholds[1] = by(ty) - this.body.y;
			footholds[2] = bz(tz) - this.body.z;
			float k = front ? (side > 0 ? 1.05F : 1.0F) : 0.85F;
			float l1 = (front ? 26.0F : 24.0F) / 2.0F;
			float l2 = ((front ? 28.0F : 26.0F) + 24.0F * k) / 2.0F;
			solve(upper, fore, upper.x, upper.y, upper.z, footholds[0], footholds[1], footholds[2], -side * 0.7F, -1.0F, -0.2F, l1, l2);
			hand.xRot = -0.35F;
			// the dactyls open as the front claws lift or one reaches up to strike, eased so a blend through the threshold
			// never pops
			float open = Math.max(Mth.clamp((p.front[1] - 15.0F) / 10.0F, 0.0F, 1.0F),
					front ? Mth.clamp((reach[1] - 15.0F) / 10.0F, 0.0F, 1.0F) : 0.0F);
			dactyl.zRot = Mth.lerp(drag, 0.1F, 0.25F) + open * 0.6F;
		}

		// the upper arms work in the thorax's frame
		this.cradledFungling.visible = cradle;
		if (this.fungDrop >= 0.0F) {
			// it slips out of the opened hand, tumbles and shrinks as it falls into the nectar
			float fall = this.fungDrop * this.fungDrop;
			this.cradledFungling.y += fall * 26.0F;
			this.cradledFungling.z -= this.fungDrop * 4.0F;
			this.cradledFungling.xRot += fall * 2.4F;
			float shrink = 1.0F - fall * 0.45F;
			this.cradledFungling.xScale = this.cradledFungling.yScale = this.cradledFungling.zScale = shrink;
		}
		armIk(this.armHandUpper, this.armHandFore, p.hand, p.handPole, 11.0F, 21.0F);
		this.armHandWrist.xRot = -0.3F;
		for (int i = 0; i < this.fingers.length; i++) {
			ModelPart[] f = this.fingers[i];
			f[0].xRot = p.grip * 0.9F;
			f[0].zRot = (i - 1) * 0.2F * (1.0F - p.grip);
			f[1].xRot = p.grip;
			f[2].xRot = p.grip * 0.8F;
		}
		this.thumb0.xRot = 0.3F + p.grip * 0.5F;
		this.thumb0.zRot = -(0.6F - p.grip * 0.7F);
		this.thumb1.xRot = p.grip * 0.9F;
		float latch = this.siphonLatch;
		float[] siphonTarget = p.siphonT;
		if (latch > 0.0F) {
			// aim the siphon arm along the line to its target, as far as it reaches, swinging over by the latch weight:
			// the shoulder in ground space, then a point 30 units along the line to the target, back in blockout units
			float sx = this.body.x + this.thorax.x + this.armSiphonUpper.x;
			float sy = this.body.y + this.thorax.y + this.armSiphonUpper.y;
			float sz = this.body.z + this.thorax.z + this.armSiphonUpper.z;
			Vector3f dir = new Vector3f(this.siphonAim).sub(sx, sy, sz);
			if (dir.lengthSquared() > 1.0E-4F) dir.normalize().mul(30.0F);
			siphonTarget = new float[] { Mth.lerp(latch, p.siphonT[0], -(sx + dir.x) * 2.0F),
					Mth.lerp(latch, p.siphonT[1], -(sy + dir.y) * 2.0F), Mth.lerp(latch, p.siphonT[2], (sz + dir.z) * 2.0F) };
		}
		armIk(this.armSiphonUpper, this.armSiphonFore, siphonTarget, p.siphonPole, 11.0F, 12.0F);
		for (int k = 0; k < this.siphon.length; k++) {
			float rest = p.curl * 0.22F - Mth.sin(time * 2.2F - k * 0.55F) * 0.1F;
			float feeding = Mth.sin(time * 4.0F - k * 0.7F) * 0.05F;
			this.siphon[k].xRot += Mth.lerp(latch, rest, feeding);
			this.siphon[k].zRot += Mth.cos(time * 1.6F - k * 0.5F) * 0.08F * (1.0F - latch);
		}
	}

	/** Two-bone IK for an upper arm, in the thorax's (x-rotated) frame; target and pole in blockout ground coordinates. */
	private void armIk(ModelPart upper, ModelPart fore, float[] target, float[] pole, float l1, float l2) {
		float tx = bx(target[0]) - this.body.x - this.thorax.x;
		float ty = by(target[1]) - this.body.y - this.thorax.y;
		float tz = bz(target[2]) - this.body.z - this.thorax.z;
		float c = Mth.cos(-this.thorax.xRot);
		float s = Mth.sin(-this.thorax.xRot);
		float ly = ty * c - tz * s;
		float lz = ty * s + tz * c;
		solve(upper, fore, upper.x, upper.y, upper.z, tx, ly, lz, -pole[0], -pole[1], pole[2], l1, l2);
	}

	/** Two-bone IK: aims {@code upper} and bends {@code fore} so the chain's tip reaches the target; limbs hang along +Y. */
	private void solve(ModelPart upper, ModelPart fore, float sx, float sy, float sz, float tx, float ty, float tz,
			float px, float py, float pz, float l1, float l2) {
		Vector3f s = new Vector3f(sx, sy, sz);
		Vector3f d = new Vector3f(tx - sx, ty - sy, tz - sz);
		float dist = Mth.clamp(d.length(), 3.0F, l1 + l2 - 0.25F);
		Vector3f n = d.lengthSquared() < 1.0E-6F ? new Vector3f(DOWN) : d.normalize();
		float cosA = (l1 * l1 + dist * dist - l2 * l2) / (2.0F * l1 * dist);
		float sinA = Mth.sqrt(Math.max(0.0F, 1.0F - cosA * cosA));
		Vector3f pv = new Vector3f(px, py, pz);
		Vector3f b = new Vector3f(pv).sub(new Vector3f(n).mul(n.dot(pv)));
		if (b.lengthSquared() < 1.0E-6F) b.set(1.0F, 0.0F, 0.0F);
		b.normalize();
		Vector3f elbow = new Vector3f(s).add(new Vector3f(n).mul(l1 * cosA)).add(new Vector3f(b).mul(l1 * sinA));
		Vector3f tip = new Vector3f(s).add(new Vector3f(n).mul(dist));
		this.upperQ.rotationTo(DOWN, new Vector3f(elbow).sub(s).normalize());
		this.foreQ.rotationTo(DOWN, new Vector3f(tip).sub(elbow).normalize());
		setRotation(upper, this.upperQ);
		setRotation(fore, new Quaternionf(this.upperQ).conjugate().mul(this.foreQ));
	}

	private void setRotation(ModelPart part, Quaternionf q) {
		q.getEulerAnglesZYX(this.euler);
		part.xRot = this.euler.x;
		part.yRot = this.euler.y;
		part.zRot = this.euler.z;
	}

	/** Draws only the sac's shell, through its ancestors' transforms (the translucent sac layer calls this). */
	public void renderSacShell(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
		poseStack.pushPose();
		this.root.translateAndRotate(poseStack);
		this.ground.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		this.sac.translateAndRotate(poseStack);
		this.sacShell.visible = true;
		this.sacShell.render(poseStack, consumer, light, overlay, color);
		this.sacShell.visible = false;
		poseStack.popPose();
	}
