
	// ------------------------------------------------------------------ animation (tools/model_export/mycophant_anim.java)
	//
	// Poses are authored in the blockout's frame (pixels at world scale, Y up, face toward -Z, root at the floor) so
	// they read the same as tools/model_export/reference/spawn-mother-v8.html; bo() converts a point to model space
	// (half size, Y down, the creature's right on -X). Rotations flip sign on X and Y for the same reason.

	/** Review-client hook: a pose name ("stalk", "tending", "roar", "embrace", "spawn", "guard", "sweep", "yield"), with "_p2" for the opened head. */
	public static volatile String reviewPose;

	private static final class Pose {
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
		float rake;

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

		Pose copy(Pose o) {
			set(o.thorax, o.neck, o.head, o.frill, o.maw, o.tongue, o.tentFwd, o.height, o.vents);
			System.arraycopy(o.wing, 0, wing, 0, 3);
			System.arraycopy(o.hand, 0, hand, 0, 3);
			System.arraycopy(o.handPole, 0, handPole, 0, 3);
			System.arraycopy(o.siphonT, 0, siphonT, 0, 3);
			System.arraycopy(o.siphonPole, 0, siphonPole, 0, 3);
			System.arraycopy(o.front, 0, front, 0, 3);
			System.arraycopy(o.second, 0, second, 0, 3);
			grip = o.grip; curl = o.curl; rake = o.rake;
			return this;
		}

		/** this = mix(a, b, t). */
		Pose mix(Pose a, Pose b, float t) {
			thorax = Mth.lerp(t, a.thorax, b.thorax); neck = Mth.lerp(t, a.neck, b.neck); head = Mth.lerp(t, a.head, b.head);
			frill = Mth.lerp(t, a.frill, b.frill); maw = Mth.lerp(t, a.maw, b.maw); tongue = Mth.lerp(t, a.tongue, b.tongue);
			tentFwd = Mth.lerp(t, a.tentFwd, b.tentFwd); height = Mth.lerp(t, a.height, b.height);
			vents = Mth.lerp(t, a.vents, b.vents); grip = Mth.lerp(t, a.grip, b.grip); curl = Mth.lerp(t, a.curl, b.curl);
			rake = Mth.lerp(t, a.rake, b.rake);
			for (int i = 0; i < 3; i++) {
				wing[i] = Mth.lerp(t, a.wing[i], b.wing[i]);
				hand[i] = Mth.lerp(t, a.hand[i], b.hand[i]);
				handPole[i] = Mth.lerp(t, a.handPole[i], b.handPole[i]);
				siphonT[i] = Mth.lerp(t, a.siphonT[i], b.siphonT[i]);
				siphonPole[i] = Mth.lerp(t, a.siphonPole[i], b.siphonPole[i]);
				front[i] = Mth.lerp(t, a.front[i], b.front[i]);
				second[i] = Mth.lerp(t, a.second[i], b.second[i]);
			}
			return this;
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

	private final Pose pose = new Pose();
	private final Pose poseA = new Pose();
	private final Pose poseB = new Pose();
	private static final Vector3f DOWN = new Vector3f(0.0F, 1.0F, 0.0F);
	private final Quaternionf upperQ = new Quaternionf();
	private final Quaternionf foreQ = new Quaternionf();
	private final Vector3f euler = new Vector3f();
	private final Vector3f siphonAim = new Vector3f();
	private boolean siphonLatched;
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
		MycophantState state = entity.getState();
		boolean walking = state == MycophantState.DRAG;
		float split = entity.getPhase() >= 2 ? 1.0F : 0.0F;
		float splitT = state == MycophantState.SPLIT ? entity.getSplitTick() + partial : (split > 0 ? 999.0F : 0.0F);
		this.siphonLatched = false;
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
				default -> STALK;
			});
			walking = name.equals("drag");
			cradle = name.equals("tending");
			splitT = opened ? 999.0F : 0.0F;
		} else {
			switch (state) {
				case INTRO -> {
					cradle = introPose(entity, partial);
					walking = introPivoting(entity, partial);
				}
				case ATTACK -> attackPose(entity, partial);
				case STAGGER -> {
					float s = entity.getStateTick() + partial;
					float rise = HemoEasing.smoothstep(s / 6.0F) * (1.0F - HemoEasing.smoothstep((s - 10.0F) / 20.0F));
					this.pose.mix(STALK, STAGGER, rise);
				}
				case SPLIT -> splitPose(splitT);
				case YIELD -> {
					float y = entity.getYieldTick() + partial;
					this.pose.mix(STALK, YIELDED, HemoEasing.smoothstep(MycophantYieldRules.lower((int) y) * 0.6F + MycophantYieldRules.fold((int) y) * 0.4F));
					this.pose.frill = Mth.lerp(MycophantYieldRules.fold((int) y), STALK.frill, -0.3F);
				}
				default -> this.pose.copy(STALK);
			}
		}
		apply(entity, time, walking, ageInTicks, splitT, cradle);
	}

	/**
	 * The waking, beat by beat: she tends the fungling with her back turned, perks up and goes still, pivots round on
	 * her claws, lets the fungling fall, draws back low, rears into the roar, then settles forward into the rush's
	 * starting stance so the Rushing Embrace's wind-up picks up without a pop. Returns true while she still cradles it.
	 */
	private boolean introPose(MycophantEntity entity, float partial) {
		boolean rematch = entity.isRematchIntro();
		float t = entity.getIntroTick() + partial;
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

	/** She pivots round on her claws during the intro's turn instead of spinning in place. */
	private static boolean introPivoting(MycophantEntity entity, float partial) {
		boolean rematch = entity.isRematchIntro();
		float t = entity.getIntroTick() + partial;
		float turn = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.TURN, rematch);
		return t >= turn && t < turn + MycophantIntroRules.turnTicks(rematch)
				&& Math.abs(Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO)) > 0.3F;
	}

	private void attackPose(MycophantEntity entity, float partial) {
		MycophantAttack attack = entity.getAttack();
		VesperMoveTimeline timeline = MycophantControllerRules.timeline(attack, entity.getPhase());
		float t = entity.getAttackTick() + partial;
		float w = Math.max(1, timeline.windup());
		float a = Math.max(1, timeline.active());
		float r = Math.max(1, timeline.recovery());
		float wind = HemoEasing.smoothstep(t / w);
		float act = HemoEasing.smoothstep((t - w) / a);
		float rec = HemoEasing.smoothstep((t - w - a) / r);
		switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> {
				if (entity.isHolding()) {
					this.pose.copy(HOLD);
					float squeeze = entity.getClaimMeter();
					this.pose.grip = Mth.lerp(squeeze, 0.7F, 1.0F);
					this.pose.thorax -= squeeze * 0.08F;
					this.pose.head += Mth.sin(t * 0.2F) * 0.05F;
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
				else if (t < w + a) {
					this.pose.copy(SIPHON_AIM);
					this.siphonLatched = entity.getSiphonTargetId() >= 0 && aimSiphon(entity, partial);
				} else this.pose.mix(SIPHON_AIM, STALK, rec);
			}
			case PLUCK -> {
				if (t < w) this.pose.mix(STALK, PLUCK_WIND, wind);
				else if (t < w + a) this.pose.mix(PLUCK_WIND, PLUCK, act);
				else this.pose.mix(PLUCK, STALK, rec);
			}
			case CLAW_RAKE -> {
				this.pose.copy(STALK);
				this.pose.rake = t < w ? wind : (t < w + a ? 1.0F + act : 2.0F - rec * 2.0F);
				this.pose.thorax = Mth.lerp(Math.min(1.0F, this.pose.rake), 0.0F, 0.12F) - (t >= w && t < w + a ? act * 0.18F : 0.0F);
			}
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
			case NONE -> this.pose.copy(STALK);
		}
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

	private void apply(MycophantEntity entity, float time, boolean walking, float ageInTicks, float splitT, boolean cradle) {
		Pose p = this.pose;
		float breathe = Mth.sin(time * 1.3F);
		float gait = MycophantMotionRules.dragPhase(ageInTicks);
		float haul = walking ? Mth.sin(gait * Mth.TWO_PI * 2.0F) : 0.0F;

		// body: bob, haul surge and sway
		this.body.y = this.bodyBaseY - (p.height + breathe * 0.6F + Math.max(0.0F, haul) * 1.2F) / 2.0F;
		this.body.z = -haul * 2.5F / 2.0F;
		this.body.zRot = walking ? Mth.sin(gait * Mth.TWO_PI) * 0.03F : 0.0F;
		this.body.yRot = walking ? -Mth.sin(gait * Mth.TWO_PI) * 0.03F : 0.0F;

		// thorax, neck and head
		this.thorax.xRot = -(p.thorax + breathe * 0.015F);
		this.neck.xRot = -p.neck;
		this.head.xRot = -p.head;
		this.head.yRot = -Mth.sin(time * 0.6F) * 0.06F * (1.0F - this.tentacleCalm) + Mth.sin(ageInTicks * 2.3F) * 0.05F * this.roarTremble
				+ Mth.sin(ageInTicks * 3.3F) * 0.05F * this.splitShudder;
		this.head.zRot = Mth.sin(time * 0.8F) * 0.04F + Mth.sin(ageInTicks * 1.9F + 1.0F) * 0.06F * this.roarTremble
				+ Mth.sin(ageInTicks * 2.6F + 2.0F) * 0.06F * this.splitShudder;
		this.head.xRot += Mth.sin(ageInTicks * 4.1F) * 0.03F * this.splitShudder;

		// the abdomen breathes and pulses when spawning
		this.abdomen.xRot = -(0.03F + breathe * 0.01F + p.vents * Mth.sin(time * 6.0F) * 0.01F);
		this.abdomen.yRot = walking ? -Mth.sin(gait * Mth.TWO_PI) * 0.03F : 0.0F;
		float pulse = 1.0F + p.vents * 0.03F * Mth.sin(time * 5.0F);
		this.abdomen.xScale = pulse;
		this.abdomen.yScale = pulse;
		for (ModelPart[] vent : this.ventPetals)
			for (int i = 0; i < vent.length; i++) vent[i].zRot = -p.vents * (1.0F + Mth.sin(time * 4.0F + i) * 0.12F);
		for (int i = 0; i < this.tails.length; i++) {
			ModelPart[] chain = this.tails[i];
			for (int k = 0; k < chain.length; k++) {
				chain[k].xRot += -(0.12F + Mth.sin(time * 1.8F - k * 0.5F + i) * 0.1F);
				chain[k].zRot += Mth.cos(time * 1.4F - k * 0.4F + i * 2.0F) * 0.08F;
			}
		}

		// the gestation sac breathes; its shell is drawn by the translucent sac layer only
		float sacPulse = 1.0F + Mth.sin(time * 3.1F) * 0.02F;
		this.sac.xScale = sacPulse;
		this.sac.yScale = sacPulse;
		this.sac.zScale = sacPulse;
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
		this.pulp.z += (1.0F - bloom) * 2.0F;
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
						+ (left ? -1.0F : 1.0F) * Mth.sin(time * 1.4F - k * 0.5F + s) * 0.025F
						+ Mth.sin(time * 9.0F - k * 0.9F + s) * 0.05F * shiver;
			}
		}

		// the four claws drag the belly along; the right front one rakes
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
			if (walking) {
				float pair = (front == (side > 0)) ? 0.5F : 0.0F;
				float ph = (gait + pair) % 1.0F;
				if (ph < 0.6F) tz += 22.0F * (ph / 0.6F - 0.5F);
				else {
					float u = (ph - 0.6F) / 0.4F;
					tz += 22.0F * (0.5F - u);
					ty += Mth.sin(u * Mth.PI) * 16.0F;
				}
			}
			if (front && side > 0 && p.rake > 0.0F) {
				float raise = Math.min(1.0F, p.rake);
				float slam = Math.max(0.0F, p.rake - 1.0F);
				ty += raise * 46.0F - slam * 46.0F;
				tz += -raise * 6.0F - slam * 34.0F;
				tx -= slam * 18.0F;
			}
			footholds[0] = bx(tx) - this.body.x;
			footholds[1] = by(ty) - this.body.y;
			footholds[2] = bz(tz) - this.body.z;
			float k = front ? (side > 0 ? 1.05F : 1.0F) : 0.85F;
			float l1 = (front ? 26.0F : 24.0F) / 2.0F;
			float l2 = ((front ? 28.0F : 26.0F) + 24.0F * k) / 2.0F;
			solve(upper, fore, upper.x, upper.y, upper.z, footholds[0], footholds[1], footholds[2], -side * 0.7F, -1.0F, -0.2F, l1, l2);
			hand.xRot = -0.35F;
			dactyl.zRot = (walking ? 0.25F : 0.1F) + (p.front[1] > 20.0F || p.rake > 0.5F ? 0.6F : 0.0F);
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
		if (this.siphonLatched) {
			// aim the siphon arm along the line to its target, as far as it reaches
			// the shoulder in ground space, then a point 30 units along the line to the target, back in blockout units
			float sx = this.body.x + this.thorax.x + this.armSiphonUpper.x;
			float sy = this.body.y + this.thorax.y + this.armSiphonUpper.y;
			float sz = this.body.z + this.thorax.z + this.armSiphonUpper.z;
			Vector3f dir = new Vector3f(this.siphonAim).sub(sx, sy, sz);
			if (dir.lengthSquared() > 1.0E-4F) dir.normalize().mul(30.0F);
			float[] aim = { -(sx + dir.x) * 2.0F, -(sy + dir.y) * 2.0F, (sz + dir.z) * 2.0F };
			armIk(this.armSiphonUpper, this.armSiphonFore, aim, p.siphonPole, 11.0F, 12.0F);
			for (int k = 0; k < this.siphon.length; k++) this.siphon[k].xRot += Mth.sin(time * 4.0F - k * 0.7F) * 0.05F;
		} else {
			armIk(this.armSiphonUpper, this.armSiphonFore, p.siphonT, p.siphonPole, 11.0F, 12.0F);
			for (int k = 0; k < this.siphon.length; k++) {
				this.siphon[k].xRot += p.curl * 0.22F - Mth.sin(time * 2.2F - k * 0.55F) * 0.1F;
				this.siphon[k].zRot += Mth.cos(time * 1.6F - k * 0.5F) * 0.08F;
			}
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
