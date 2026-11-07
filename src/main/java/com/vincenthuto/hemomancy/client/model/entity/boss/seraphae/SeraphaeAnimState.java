package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathPoseBlend;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathSpring;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * One Seraphae's client animation state, kept per body rather than on the renderer's shared model: which track plays
 * and its time on the authored clock (the model picks it from the kit's synced move, opening, hold and ending, each
 * replayed through the move view's {@code BossReplayClock}s), the inertial blend out of the last shown pose (resolution
 * B-8, through Hemorath's general {@link HemorathPoseBlend}), the bank of springs that carry her secondary motion
 * ({@link HemorathSpring}), what her body is doing in the world (her glide in her own frame, her turn, a touch landing),
 * her phase weights and the phase card, and the points the renderer records each frame: her irons, hands, heart and
 * nerve tips as offsets, and the world trails of her hands and tips that her slashes and reaches follow.
 */
public final class SeraphaeAnimState {
	public static final int TRACK_IDLE = 0, TRACK_MOVE = 100, TRACK_OPENING = 200, TRACK_HOLD = 300, TRACK_RISE = 301,
			TRACK_ENDING = 302;
	/** Spring slots the model's secondary pass may use. */
	public static final int SPRINGS = 40;
	/**
	 * The blend into the hold is quicker (the hands are already out), the ending's slower, and the chains' snap into a
	 * Recoil or a Seized is a jerk, so it blends in over very little.
	 */
	public static final float BLEND_HOLD = 4.0F, BLEND_ENDING = 8.0F, BLEND_SNAP = 2.5F;
	/** Her roaming turn rate in degrees a tick: a full turn for the lean into it. */
	public static final float TURN_FULL = 6.0F;
	/** Breaths a tick (radians) in each phase: slow and even, tighter, then quick and deep as the light escapes. */
	public static final float BREATH_REACHING = 0.052F, BREATH_RESTRAINT = 0.064F, BREATH_ESCAPING = 0.085F;
	/** The phase card's length. */
	public static final float CARD_TICKS = 50.0F;
	/** The trails: both hands, then the six trunk tips. */
	public static final int TRAIL_LEFT_HAND = 0, TRAIL_RIGHT_HAND = 1, TRAIL_TIPS = 2;
	private static final float WRAP = (float) (Math.PI * 2.0D * 1000.0D);

	private static final Map<SeraphaeEntity, SeraphaeAnimState> STATES = new WeakHashMap<>();

	private final HemorathPoseBlend blend = new HemorathPoseBlend();
	private final HemorathSpring[] springs = new HemorathSpring[SPRINGS];
	private final HemorathSpring forward = new HemorathSpring(), side = new HemorathSpring(), turn = new HemorathSpring();
	private final HemorathSpring busy = new HemorathSpring(), flinch = new HemorathSpring();
	private final HemorathSpring strain = new HemorathSpring(), reeled = new HemorathSpring();
	private final HemorathSpring[] phaseWeight = {new HemorathSpring(), new HemorathSpring(), new HemorathSpring()};
	private final SeraphaeLimbTrail[] trails = new SeraphaeLimbTrail[TRAIL_TIPS + 6];

	private int track = -1;
	private float time;
	private int serial;
	private int variant;
	private float aim;
	private int openingLength;
	@Nullable
	private SeraphaeRestraint seized;
	private float lastAge = Float.NaN;
	private float frameDt;
	private float breath;
	private float holdSince = Float.NaN;
	private int phase = -1;
	private int cardPhase;
	private float cardSince = -1000.0F;
	private float age;
	private int lastHurt;
	private float speed;
	private float bodyYaw = Float.NaN;
	private float yawTravel;

	private final Vec3[] shackles = new Vec3[SeraphaeRestraint.values().length];
	private final Vec3[] tips = new Vec3[6];
	private Vec3 heart = new Vec3(0.0D, 2.0D, 0.0D);
	private Vec3 leftHand = new Vec3(-0.6D, 1.6D, -0.4D), rightHand = new Vec3(0.6D, 1.6D, -0.4D);

	private SeraphaeAnimState() {
		for (int i = 0; i < SPRINGS; i++) springs[i] = new HemorathSpring();
		for (int i = 0; i < trails.length; i++) trails[i] = new SeraphaeLimbTrail();
		for (int i = 0; i < shackles.length; i++) shackles[i] = new Vec3(0.0D, 2.0D, 0.0D);
		for (int i = 0; i < tips.length; i++) tips[i] = new Vec3(i < 3 ? 2.0D : -2.0D, 3.0D, 0.5D);
	}

	public static SeraphaeAnimState of(SeraphaeEntity entity) {
		return STATES.computeIfAbsent(entity, ignored -> new SeraphaeAnimState());
	}

	public static void clear() {
		STATES.clear();
	}

	/**
	 * Reads her synced state and her body's own motion once a frame; a second call in the same frame steps nothing.
	 * {@code age} is the body's age in ticks with the partial tick.
	 */
	public void update(SeraphaeEntity entity, float partial, float age) {
		frameDt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(5.0F, age - lastAge));
		lastAge = age;
		this.age = age;
		int synced = Math.max(1, Math.min(3, entity.syncedPhase()));
		if (phase < 0) {
			phase = synced;
			for (int i = 0; i < 3; i++) phaseWeight[i].reset(i + 1 == synced ? 1.0F : 0.0F);
		} else if (synced != phase) {
			phase = synced;
			cardPhase = synced;
			cardSince = age;
		}
		for (int i = 0; i < 3; i++) phaseWeight[i].step(i + 1 == phase ? 1.0F : 0.0F, frameDt, 0.12F, 1.0F);
		// the breath's rate changes with the phase, so its phase is carried, not recomputed from the age
		breath = (breath + frameDt * (BREATH_REACHING * reaching() + BREATH_RESTRAINT * restraint()
				+ BREATH_ESCAPING * escaping())) % WRAP;
		variant = entity.syncedMoveVariant();
		seized = entity.syncedSeized();
		openingLength = entity.view().openingLength();
		measure(entity, partial);
		Vec3 origin = entity.syncedMoveOrigin(), at = entity.syncedMoveAim();
		aim = SeraphaeHover.bearing(at.x - origin.x, at.z - origin.z, entity.yBodyRot);
		int glide = entity.syncedGlide();
		strain.step(glide == SeraphaeEntity.GLIDE_STRAIN ? 1.0F : 0.0F, frameDt, 0.22F, 0.9F);
		reeled.step(glide == SeraphaeEntity.GLIDE_REELED ? 1.0F : 0.0F, frameDt, 0.25F, 0.85F);
	}

	/** Her glide in her own frame, her turn, a touch landing: read off her real movement, so no flag is synced. */
	private void measure(SeraphaeEntity entity, float partial) {
		double dx = entity.getX() - entity.xo, dz = entity.getZ() - entity.zo;
		speed = (float) Math.sqrt(dx * dx + dz * dz);
		float[] local = SeraphaeHover.local(dx, dz, entity.yBodyRot);
		// a little under-damped: when she stops short she rocks past upright and settles
		forward.step(SeraphaeHover.glide(local[0], 4.0F), frameDt, 0.3F, 0.55F);
		side.step(SeraphaeHover.glide(local[1], 2.0F), frameDt, 0.3F, 0.6F);
		float yawRate = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
		turn.step(Mth.clamp(yawRate / TURN_FULL, -1.5F, 1.5F), frameDt, 0.3F, 0.8F);
		float yaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
		if (!Float.isNaN(bodyYaw)) yawTravel += Mth.wrapDegrees(yaw - bodyYaw) * Mth.DEG_TO_RAD;
		bodyYaw = yaw;
		if (entity.hurtTime > lastHurt) flinch.kick(0.45F);
		lastHurt = entity.hurtTime;
		flinch.step(0.0F, frameDt, 0.6F, 0.3F);
	}

	/**
	 * Sets the playing track and its time on the authored clock. {@code serial} tells two runs of the same move apart, so
	 * the same move again blends from the pose it left. Entering the hold starts its own count from this frame: the hold
	 * has no synced counter, so the catch and the rocking run from when this client saw it begin.
	 */
	public void play(int track, float time, int serial) {
		if (track == TRACK_HOLD && (this.track != TRACK_HOLD || Float.isNaN(holdSince))) holdSince = age;
		if (track != TRACK_HOLD) holdSince = Float.NaN;
		this.track = track;
		this.time = track == TRACK_HOLD ? age - holdSince : time;
		this.serial = serial;
		busy.step(track == TRACK_IDLE ? 0.0F : 1.0F, frameDt, 0.4F, 1.0F);
	}

	/**
	 * A move's time on its authored clock: the wind-up keys stretch to the wind-up that actually runs (the thrown reach
	 * after a Wing Grasp is shorter), the active span keeps its own clock, and the recovery's keys squeeze into a
	 * recovery the No Rest Given deed has cut, so the settle still lands before the next move.
	 */
	public static float authored(float t, int windup, int active, int recovery, int baseWindup, int baseRecovery) {
		if (t < windup) return windup <= 0 ? baseWindup : t * baseWindup / (float) windup;
		float afterCommit = t - windup;
		if (afterCommit < active) return baseWindup + afterCommit;
		float settle = afterCommit - active;
		float scale = recovery <= 0 ? 1.0F : Math.max(1.0F, baseRecovery / (float) recovery);
		return baseWindup + active + settle * scale;
	}

	/**
	 * Lays the inertial blend onto {@code pose} whenever the track changes (a new move, even the same move again, an
	 * opening, the hold, the ending), then remembers what was shown.
	 */
	public void blend(float[] pose, float age) {
		long key = (long) track << 32 | (track > TRACK_MOVE && track < TRACK_OPENING ? serial & 0xFFFFFFFFL : 0L);
		float duration = switch (track) {
			case TRACK_HOLD -> BLEND_HOLD;
			case TRACK_ENDING -> BLEND_ENDING;
			case TRACK_OPENING + SeraphaeOpenings.RECOIL_ID, TRACK_OPENING + SeraphaeOpenings.SEIZED_ID -> BLEND_SNAP;
			default -> SeraphaePose.BLEND_TICKS;
		};
		blend.apply(pose, key, age, duration);
	}

	/** Steps spring {@code slot} this frame toward {@code driver} and returns how far it trails it. */
	public float lag(int slot, float driver, float frequency, float damping) {
		return springs[slot].lag(driver, frameDt, frequency, damping);
	}

	/** Steps spring {@code slot} this frame toward {@code target} and returns its value. */
	public float follow(int slot, float target, float frequency, float damping) {
		return springs[slot].step(target, frameDt, frequency, damping);
	}

	public int track() {
		return track;
	}

	public float time() {
		return time;
	}

	public int variant() {
		return variant;
	}

	/** Where the move aims off her facing, radians, positive to her right. */
	public float aim() {
		return aim;
	}

	public int openingLength() {
		return openingLength;
	}

	@Nullable
	public SeraphaeRestraint seized() {
		return seized;
	}

	public int phase() {
		return Math.max(1, phase);
	}

	/** The phases as smooth weights that sum to one: the idle never steps from one phase to the next. */
	public float reaching() {
		return HemoEasing.clamp01(phaseWeight[0].value());
	}

	public float restraint() {
		return HemoEasing.clamp01(phaseWeight[1].value());
	}

	public float escaping() {
		return HemoEasing.clamp01(phaseWeight[2].value());
	}

	/** The phase whose card plays, or 0. */
	public int cardPhase() {
		return cardTime() < CARD_TICKS ? cardPhase : 0;
	}

	/** Ticks into the phase card, from when this client saw the phase change. */
	public float cardTime() {
		return age - cardSince;
	}

	/** The breath's phase, radians. */
	public float breath() {
		return breath;
	}

	/** 0 at rest between moves, easing to 1 through a move, an opening or a set piece: idle business fades under it. */
	public float busy() {
		return HemoEasing.clamp01(busy.value());
	}

	/** Her glide along her facing as a share of her full glide speed, smoothed and a little under-damped. */
	public float forward() {
		return forward.value();
	}

	/** Her slide to her right as a share of her full glide speed. */
	public float side() {
		return side.value();
	}

	/** -1 to 1: how hard she is turning (positive to her right), smoothed. */
	public float turn() {
		return turn.value();
	}

	/** Radians turned in total, signed: her wings and her gown swing off it. */
	public float yawTravel() {
		return yawTravel;
	}

	/** 0 to 1: straining at her chain's end. */
	public float strain() {
		return HemoEasing.clamp01(strain.value());
	}

	/** 0 to 1: reeled back toward the dais by her shortening chains. */
	public float reeled() {
		return HemoEasing.clamp01(reeled.value());
	}

	/** Blocks a tick over the ground, from the last tick's move. */
	public float speed() {
		return speed;
	}

	/** The recoil of the last touch: kicked on the hit, ringing out. */
	public float flinch() {
		return flinch.value();
	}

	// ── points the renderer records for the chains and geometry ──────

	public void shackle(SeraphaeRestraint restraint, Vec3 offset) {
		shackles[restraint.ordinal()] = offset;
	}

	/** An iron's offset from her feet, in world axes, as last drawn. */
	public Vec3 shackle(SeraphaeRestraint restraint) {
		return shackles[restraint.ordinal()];
	}

	public void tip(int trunk, Vec3 offset) {
		tips[trunk] = offset;
	}

	/** A trunk's tip fan as an offset from her feet (0..2 left upper, middle, lower; 3..5 right). */
	public Vec3 tip(int trunk) {
		return tips[trunk];
	}

	public void heart(Vec3 offset) {
		heart = offset;
	}

	public Vec3 heart() {
		return heart;
	}

	public void hands(Vec3 left, Vec3 right) {
		leftHand = left;
		rightHand = right;
	}

	/** A hand's palm as an offset from her feet. */
	public Vec3 hand(boolean left) {
		return left ? leftHand : rightHand;
	}

	/**
	 * Keeps this frame's hands and tips in the world for their trails: {@code origin} is her position at the frame's
	 * partial tick, {@code age} her render age. Call after the offsets are recorded.
	 */
	public void trail(Vec3 origin, float age) {
		record(TRAIL_LEFT_HAND, origin.add(leftHand), age);
		record(TRAIL_RIGHT_HAND, origin.add(rightHand), age);
		for (int i = 0; i < tips.length; i++) record(TRAIL_TIPS + i, origin.add(tips[i]), age);
	}

	private void record(int index, Vec3 at, float age) {
		trails[index].record(age, at.x, at.y, at.z);
	}

	/**
	 * Where a hand's palm was in the world at {@code age} (her render age, {@code AnimationTime.age}), or null outside
	 * the kept frames: a reach's trail or a slash laid along it follows the hand the animation drew.
	 */
	@Nullable
	public Vec3 handAt(boolean left, float age) {
		return at(left ? TRAIL_LEFT_HAND : TRAIL_RIGHT_HAND, age);
	}

	/** Where a trunk's tip fan was in the world at {@code age}, or null outside the kept frames: a lash follows it. */
	@Nullable
	public Vec3 tipAt(int trunk, float age) {
		return at(TRAIL_TIPS + Math.max(0, Math.min(5, trunk)), age);
	}

	@Nullable
	private Vec3 at(int index, float age) {
		double[] out = new double[3];
		return trails[index].at(age, out) ? new Vec3(out[0], out[1], out[2]) : null;
	}
}
