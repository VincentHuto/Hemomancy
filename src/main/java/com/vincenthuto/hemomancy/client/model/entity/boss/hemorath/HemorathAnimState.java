package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * One Hemorath's client animation state, kept per body rather than on the renderer's shared model: which track plays
 * and its time on the authored clock (the kit's synced move, opening and ending, and the Last Thirst's counter, each
 * replayed through a {@code BossReplayClock}), the swell smoothed from the synced meter, the patch count and when it
 * last changed, the inertial blend out of the last shown pose ({@link HemorathPoseBlend}, resolution B-8), the bank of
 * springs that carry his secondary motion ({@link HemorathSpring}), what his body is actually doing in the world (the
 * lurch, the turn, a hit landing), and the attachment points the renderer records each frame for the world geometry.
 */
public final class HemorathAnimState {
	public static final int TRACK_IDLE = 0;
	public static final int TRACK_MOVE = 100;
	public static final int TRACK_OPENING = 200;
	public static final int TRACK_ENDING = 300;
	public static final int TRACK_CARD = 301;
	public static final int TRACK_THIRST = 302;
	/** The blend out of the last pose: quick when he is hollow, slower and heavier when full. */
	public static final float BLEND_SUNKEN = 4.0F, BLEND_BRIMMING = 7.0F;
	/** Spring slots the model's secondary pass may use. */
	public static final int SPRINGS = 32;
	/** Ground speed (blocks a tick) over which the walk turns into the Sunken lurch (2.5 blocks in 6 ticks). */
	public static final float LURCH_FROM = 0.27F, LURCH_FULL = 0.38F;
	/** Body yaw rate (degrees a tick) that reads as a full turn: his Sunken turn rate. */
	public static final float TURN_FULL = 8.0F;
	/** Breaths a tick (radians): quick and shallow hollow, slow and laboured full; the weight sway is slower still. */
	public static final float BREATH_SUNKEN = 0.17F, BREATH_BRIMMING = 0.065F;
	public static final float SWAY_SUNKEN = 0.06F, SWAY_BRIMMING = 0.035F;
	/** The breath and sway phases wrap by this whole number of turns, so they never lose precision. */
	private static final float WRAP = (float) (Math.PI * 2.0D * 1000.0D);

	private static final Map<HemorathEntity, HemorathAnimState> STATES = new WeakHashMap<>();

	private final BossReplayClock thirstClock = new BossReplayClock();
	private final HemorathPoseBlend blend = new HemorathPoseBlend();
	private final HemorathSpring[] springs = new HemorathSpring[SPRINGS];
	private final HemorathSpring lurch = new HemorathSpring();
	private final HemorathSpring turn = new HemorathSpring();
	private final HemorathSpring busy = new HemorathSpring();
	private final HemorathSpring flinch = new HemorathSpring();

	private int track = -1;
	private float time;
	private int variant;
	private int serial;
	private int debt;
	private float swell = -1.0F;
	private float lastAge = Float.NaN;
	private float frameDt;
	private int patches = -1;
	private float patchAge = 1000.0F;
	private float patchChangedAt = -1000.0F;
	private int phase = 1;
	private int lastHurt;
	private float speed;
	private float breath;
	private float sway;
	private float bodyYaw = Float.NaN;
	private float yawTravel;
	private float turnTravel;
	private Vec3 core = new Vec3(0, 2.0, 0), claw = new Vec3(1, 2.2, 0), staff = new Vec3(-1.5, 4, 0);

	private HemorathAnimState() {
		for (int i = 0; i < SPRINGS; i++) springs[i] = new HemorathSpring();
	}

	public static HemorathAnimState of(HemorathEntity boss) {
		return STATES.computeIfAbsent(boss, ignored -> new HemorathAnimState());
	}

	public static void clear() {
		STATES.clear();
	}

	/** Reads the synced state and the body's own motion once a frame; a second call in the same frame steps nothing. */
	public void update(HemorathEntity boss, float partial, float age) {
		float target = boss.syncedFraction();
		frameDt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(5.0F, age - lastAge));
		lastAge = age;
		swell = swell < 0.0F ? target : swell + (target - swell) * (1.0F - (float) Math.exp(-frameDt / HemorathVisualRules.SWELL_SMOOTHING));
		// the rates change with the swell, so the phases are carried, not recomputed from the age
		breath = (breath + frameDt * HemoEasing.lerp(BREATH_SUNKEN, BREATH_BRIMMING, swell())) % WRAP;
		sway = (sway + frameDt * HemoEasing.lerp(SWAY_SUNKEN, SWAY_BRIMMING, swell())) % WRAP;
		// the eruption beat runs on this client's own age from the moment the synced count changed
		int synced = boss.syncedPatches();
		if (synced != patches) {
			patchChangedAt = patches < 0 ? age - 1000.0F : age;
			patches = synced;
		}
		patchAge = age - patchChangedAt;
		phase = boss.syncedPhase();
		debt = boss.syncedTargetDebt();
		serial = boss.syncedMoveSerial();
		measure(boss, partial);
		selectTrack(boss, partial, age);
		busy.step(track == TRACK_IDLE ? 0.0F : 1.0F, frameDt, 0.45F, 1.0F);
	}

	private void selectTrack(HemorathEntity boss, float partial, float age) {
		SaintMoveView view = boss.view();
		variant = boss.syncedMoveVariant();
		float ending = view.endingTime(partial);
		if (ending >= 0.0F) {
			track = TRACK_ENDING;
			time = ending;
			return;
		}
		HemorathMove move = HemorathMove.byId(view.moveId());
		float moveTime = view.moveTime(partial);
		if (move != null && moveTime >= 0.0F) {
			BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(view.inputs()));
			int opening = view.openingId();
			// the Tithe opens during the wind-up and the halo's Haloless window runs through its own catch: both stay
			// on the move's track, which shows them
			if (opening != 0 && moveTime >= timeline.windup() + timeline.active() && move != HemorathMove.TITHE_RECKONING
					&& move != HemorathMove.SPIKED_HALO) {
				track = TRACK_OPENING + opening;
				time = Math.max(0.0F, view.openingTime(partial));
				return;
			}
			if (move == HemorathMove.EXSANGUINATING_TIDE && HemorathMove.forced(variant)) {
				if (moveTime < SaintPhaseTable.CARD_TICKS) {
					track = TRACK_CARD;
					time = moveTime;
					return;
				}
				track = TRACK_MOVE + move.id();
				time = HemorathVisualRules.authoredTime(moveTime - SaintPhaseTable.CARD_TICKS,
						timeline.windup() - SaintPhaseTable.CARD_TICKS, move.baseWindup());
				return;
			}
			track = TRACK_MOVE + move.id();
			time = move == HemorathMove.RAKING_CLAW && HemorathMove.held(variant) ? HemorathVisualRules.heldTime(moveTime)
					: HemorathVisualRules.authoredTime(moveTime, timeline.windup(), move.baseWindup());
			return;
		}
		int thirst = boss.syncedThirst();
		if (thirst >= 0) {
			track = TRACK_THIRST;
			time = HemorathMove.LAST_THIRST.baseWindup() + thirstClock.sample(0, thirst, boss.tickCount, partial, 1);
			return;
		}
		int opening = view.openingId();
		if (opening != 0) {
			track = TRACK_OPENING + opening;
			time = Math.max(0.0F, view.openingTime(partial));
			return;
		}
		track = TRACK_IDLE;
		time = age;
	}

	/** What the body is doing in the world: ground speed, the turn, the lurch, a hit landing. */
	private void measure(HemorathEntity boss, float partial) {
		float dx = (float) (boss.getX() - boss.xo), dz = (float) (boss.getZ() - boss.zo);
		speed = Mth.sqrt(dx * dx + dz * dz);
		float yawRate = Mth.wrapDegrees(boss.yBodyRot - boss.yBodyRotO);
		lurch.step(HemoEasing.ramp(speed, LURCH_FROM, LURCH_FULL), frameDt, 0.55F, 0.9F);
		turn.step(Mth.clamp(yawRate / TURN_FULL, -1.0F, 1.0F), frameDt, 0.35F, 0.8F);
		float yaw = Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot);
		if (!Float.isNaN(bodyYaw)) {
			float delta = Mth.wrapDegrees(yaw - bodyYaw);
			yawTravel += delta * Mth.DEG_TO_RAD;
			turnTravel += Math.abs(delta);
		}
		bodyYaw = yaw;
		if (boss.hurtTime > lastHurt) flinch.kick(0.55F);
		lastHurt = boss.hurtTime;
		flinch.step(0.0F, frameDt, 0.55F, 0.32F);
	}

	/**
	 * Blends {@code pose} out of the last shown pose whenever the track changes (a new move, even the same move again,
	 * counts as a change), then remembers it. {@code age} is the body's age in ticks with the partial tick.
	 */
	public void blend(float[] pose, float age) {
		long key = (long) track << 32 | (track > TRACK_MOVE && track < TRACK_OPENING ? serial & 0xFFFFFFFFL : 0L);
		blend.apply(pose, key, age, HemoEasing.lerp(BLEND_SUNKEN, BLEND_BRIMMING, swell()));
	}

	/**
	 * Steps spring {@code slot} this frame toward {@code driver} and returns how far it trails it: the overlap of a
	 * part hanging off or riding on a moving one. {@code frequency} is radians a tick, {@code damping} the ratio.
	 */
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

	/** The challenger's Tithe debt as synced: the Reckoning's claw clenches once per hundred. */
	public int debt() {
		return debt;
	}

	/** The meter fraction smoothed over ten ticks: what the body shows. */
	public float swell() {
		return Math.max(0.0F, swell);
	}

	public int patches() {
		return Math.max(0, patches);
	}

	/** Ticks since the patch count last changed. */
	public float patchAge() {
		return patchAge;
	}

	public int phase() {
		return phase;
	}

	/** 0 at rest between moves, easing to 1 through a move, an opening or a set piece: idle fidgets fade under it. */
	public float busy() {
		return HemoEasing.clamp01(busy.value());
	}

	/** 0 to 1: how far the stalk has become the Sunken lurch, from his measured ground speed. */
	public float lurch() {
		return HemoEasing.clamp01(lurch.value());
	}

	/** -1 to 1: how hard he is turning (positive to his right), smoothed. */
	public float turn() {
		return turn.value();
	}

	/** Degrees turned in total: the feet shuffle round on it when he turns in place. */
	public float turnTravel() {
		return turnTravel;
	}

	/** Radians turned in total, signed: the hanging chains and the train swing off it. */
	public float yawTravel() {
		return yawTravel;
	}

	/** The breath's phase, radians. */
	public float breath() {
		return breath;
	}

	/** The slow weight sway's phase, radians. */
	public float sway() {
		return sway;
	}

	/** Blocks a tick over the ground, from the last tick's move. */
	public float speed() {
		return speed;
	}

	/** The recoil of the last hit taken: kicked on the hit, ringing out. */
	public float flinch() {
		return flinch.value();
	}

	public Vec3 core() {
		return core;
	}

	public Vec3 claw() {
		return claw;
	}

	public Vec3 staff() {
		return staff;
	}

	public void attachments(Vec3 core, Vec3 claw, Vec3 staff) {
		this.core = core;
		this.claw = claw;
		this.staff = staff;
	}

	/** Frames of the claw hand's world position kept for its slash trail: about a second of frames at 60 a second. */
	private static final int CLAW_TRAIL = 64;
	private final float[] clawAge = new float[CLAW_TRAIL];
	private final Vec3[] clawAt = new Vec3[CLAW_TRAIL];
	private int clawHead, clawCount;

	/** As {@link #attachments(Vec3, Vec3, Vec3)}, also keeping the claw's world position at {@code age} for its trail. */
	public void attachments(Vec3 core, Vec3 claw, Vec3 staff, Vec3 origin, float age) {
		attachments(core, claw, staff);
		if (clawCount > 0) {
			float last = clawAge[Math.floorMod(clawHead - 1, CLAW_TRAIL)];
			if (age < last - 1.0F) clawCount = 0;
			else if (age <= last) return;
		}
		clawAge[clawHead] = age;
		clawAt[clawHead] = origin.add(claw);
		clawHead = (clawHead + 1) % CLAW_TRAIL;
		clawCount = Math.min(CLAW_TRAIL, clawCount + 1);
	}

	/** Where the claw hand was in the world at {@code age} (the entity's render age), or null outside the kept frames. */
	public Vec3 clawAt(float age) {
		if (clawCount < 2) return null;
		int newest = Math.floorMod(clawHead - 1, CLAW_TRAIL);
		if (age > clawAge[newest] + 0.5F) return null;
		if (age >= clawAge[newest]) return clawAt[newest];
		for (int i = 1; i < clawCount; i++) {
			int older = Math.floorMod(newest - i, CLAW_TRAIL), newer = Math.floorMod(older + 1, CLAW_TRAIL);
			if (clawAge[older] <= age) {
				float span = clawAge[newer] - clawAge[older];
				double t = span <= 1e-4F ? 1.0D : (age - clawAge[older]) / span;
				return clawAt[older].lerp(clawAt[newer], t);
			}
		}
		return null;
	}

	/** For tests: a fresh state's pose memory. */
	float[] lastShown() {
		return blend.shown();
	}
}
