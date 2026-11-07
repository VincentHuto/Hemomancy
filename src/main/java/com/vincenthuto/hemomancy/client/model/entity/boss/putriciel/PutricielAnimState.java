package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathPoseBlend;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathSpring;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Putriciel's per-entity replay state on the client. It reads the kit's move, opening and ending clocks (through
 * {@link SaintMoveView}, which owns their {@code BossReplayClock}s), the rise, the fire cycle's stage clock, the
 * Kindling and the absolutions, and picks the playing track and its authored time ({@link PutricielTracks}). It owns
 * the motion that is per body rather than per model: the inertial blend out of the last shown pose (Hemorath's
 * {@link HemorathPoseBlend}, resolution B-8), the bank of springs that carry the secondary motion and the censers'
 * pendulums ({@link HemorathSpring}), what the body is doing in the world (ground speed, the turn, a hit landing), and
 * the attachment points and world trails the renderer records each frame for the world geometry (core, palm, face,
 * staff foot and head, eight sacs, two censers), and the body fire's seventy anchors with their velocities
 * ({@link #fireAnchors()}, revision R1) and the moment the fire is read at ({@link #fireMoment()}).
 */
public final class PutricielAnimState {
	private static final Map<PutricielEntity, PutricielAnimState> STATES = new WeakHashMap<>();
	private static final int SACS = 8;
	/** Spring slots the model may use. */
	public static final int SPRINGS = 48;
	/** Body yaw rate (degrees a tick) that reads as a full turn: the stride's turn rate. */
	public static final float TURN_FULL = 10.0F;
	/** Breaths a tick (radians): the 120-tick rot breath, a quicker one in the fire. */
	public static final float BREATH_ROT = Mth.TWO_PI / 120.0F, BREATH_FIRE = Mth.TWO_PI / 60.0F;
	/** The out-of-rhythm sac pulse (radians a tick) and the slow weight sway. */
	public static final float SAC_RATE = 0.11F, SWAY_RATE = 0.035F;
	private static final float WRAP = (float) (Math.PI * 2.0D * 1000.0D);

	private final HemorathPoseBlend blend = new HemorathPoseBlend();
	private final HemorathSpring[] springs = new HemorathSpring[SPRINGS];
	private final HemorathSpring turn = new HemorathSpring();
	private final HemorathSpring busy = new HemorathSpring();
	private final HemorathSpring flinch = new HemorathSpring();
	private final PutricielTrail palmTrail = new PutricielTrail();
	private final PutricielTrail[] censerTrails = {new PutricielTrail(), new PutricielTrail()};
	private final PutricielTrail staffHeadTrail = new PutricielTrail();

	private boolean detached;
	private int moveId;
	private float moveTime = -1.0F;
	private int openingId;
	private float openingTime = -1.0F;
	private int openingLength;
	private float endingTime = -1.0F;
	private float riseTime = -1.0F;
	private PutricielStage stage = PutricielStage.ROT;
	private float stageTime;
	private float kindling;
	private int phase = 1;
	private int serial;
	private SaintMoveInputs inputs = SaintMoveInputs.NONE;
	private int absolutions = -1;
	private float acceptedAge = -1000.0F;
	private float age;
	private float lastAge = Float.NaN;
	private float frameDt;
	private float breath;
	private float sacPhase;
	private float sway;
	private float speed;
	private float bodyYaw = Float.NaN;
	private float yawTravel;
	private float turnTravel;
	private int lastHurt;
	/** The render age the body was last struck at; NaN before any hit is seen. */
	private float hitAge = Float.NaN;
	private double worldX, worldY, worldZ;
	/** The body fire's anchors this frame, and each one's last world point and velocity (by render age). */
	private List<PutricielFireAnchors.Anchor> anchors = List.of();
	private final Vec3[] anchorWorld = new Vec3[PutricielFireAnchors.COUNT];
	private final Vec3[] anchorVelocity = new Vec3[PutricielFireAnchors.COUNT];
	private float anchorAge = Float.NaN;
	private PutricielTracks.Track track = new PutricielTracks.Track(PutricielTracks.IDLE, 0, 0, 0);
	private PutricielVisualRules.Look look = PutricielVisualRules.look(PutricielStage.ROT, 0.0F, -1.0F);

	private Vec3 core = new Vec3(0, 1.8, 0), palm = new Vec3(1, 1.8, 0), face = new Vec3(0, 3.0, 0), staffFoot = Vec3.ZERO,
			staffHead = new Vec3(-1.2, 3.4, 0);
	private Vec3[] sacs = new Vec3[SACS];
	private Vec3[] censers = {new Vec3(-1.5, 2.2, 0), new Vec3(-1.7, 2, 0)};

	private PutricielAnimState() {
		for (int i = 0; i < SPRINGS; i++) springs[i] = new HemorathSpring();
		for (int i = 0; i < SACS; i++) sacs[i] = new Vec3(0, 1.6 + i * 0.05, 0);
	}

	public static PutricielAnimState of(PutricielEntity boss) {
		return STATES.computeIfAbsent(boss, ignored -> new PutricielAnimState());
	}

	public static void clear() {
		STATES.clear();
	}

	/**
	 * Reads the synced state; {@code age} is the render age with the partial tick. The model and the world geometry
	 * both call it each frame, in either order: only the first call in a frame advances the clocks and springs, and
	 * the springs the model steps later in that frame still see the whole frame's time.
	 */
	public void update(PutricielEntity boss, float partial, float age) {
		SaintMoveView view = boss.view();
		moveId = view.moveId();
		moveTime = view.moveTime(partial);
		openingId = view.openingId();
		openingTime = view.openingTime(partial);
		openingLength = view.openingLength();
		endingTime = view.endingTime(partial);
		riseTime = boss.riseTime(partial);
		inputs = view.inputs();
		serial = boss.syncedMoveSerial();
		stage = boss.stage();
		phase = boss.syncedPhase();
		stageTime = boss.stageTime(partial);
		float max = boss.syncedMeterMax();
		kindling = max <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, boss.syncedMeter() / max));
		// a later age is a new frame; a rewound one (a reload) starts again; the same or a hair earlier is this frame
		boolean fresh = Float.isNaN(lastAge) || age > lastAge + 1.0E-4F || age < lastAge - 1.0F;
		if (fresh) {
			frameDt = Float.isNaN(lastAge) || age < lastAge ? 0.0F : Math.min(5.0F, age - lastAge);
			lastAge = age;
		}
		float dt = fresh ? frameDt : 0.0F;
		this.age = age;
		int count = boss.absolutions();
		if (absolutions >= 0 && count > absolutions) acceptedAge = age;
		absolutions = count;
		look = PutricielVisualRules.look(stage, stageTime, endingTime);
		advance(dt);
		measure(boss, partial, dt);
		select(dt);
	}

	/** As {@link #update(PutricielEntity, float, float)} at the entity's own render age. */
	public void update(PutricielEntity boss, float partial) {
		update(boss, partial, AnimationTime.age(boss, partial));
	}

	/** The breath, the sacs' pulse and the sway run on phases carried here: their rates change with the fire. */
	private void advance(float dt) {
		float fire = fire();
		breath = (breath + dt * HemoEasing.lerp(BREATH_ROT, BREATH_FIRE, fire)) % WRAP;
		sacPhase = (sacPhase + dt * SAC_RATE * HemoEasing.lerp(1.0F, 0.4F, fire)) % WRAP;
		sway = (sway + dt * SWAY_RATE) % WRAP;
	}

	/** What the body is doing in the world: ground speed, the turn, a hit landing, where it stands. */
	private void measure(PutricielEntity boss, float partial, float dt) {
		float dx = (float) (boss.getX() - boss.xo), dz = (float) (boss.getZ() - boss.zo);
		speed = Mth.sqrt(dx * dx + dz * dz);
		float yawRate = Mth.wrapDegrees(boss.yBodyRot - boss.yBodyRotO);
		turn.step(Mth.clamp(yawRate / TURN_FULL, -1.0F, 1.0F), dt, 0.35F, 0.8F);
		float yaw = Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot);
		if (!Float.isNaN(bodyYaw)) {
			float delta = Mth.wrapDegrees(yaw - bodyYaw);
			yawTravel += delta * Mth.DEG_TO_RAD;
			turnTravel += Math.abs(delta);
		}
		bodyYaw = yaw;
		worldX = Mth.lerp(partial, boss.xo, boss.getX());
		worldY = Mth.lerp(partial, boss.yo, boss.getY());
		worldZ = Mth.lerp(partial, boss.zo, boss.getZ());
		if (boss.hurtTime > lastHurt) {
			flinch.kick(0.5F);
			hitAge = age;
		}
		lastHurt = boss.hurtTime;
		flinch.step(0.0F, dt, 0.5F, 0.3F);
	}

	private void select(float dt) {
		track = PutricielTracks.select(moveId, moveTime, openingId, openingTime, openingLength, endingTime, riseTime,
				sinceAccepted(), inputs, age);
		busy.step(track.id() == PutricielTracks.IDLE ? 0.0F : 1.0F, dt, 0.45F, 1.0F);
	}

	/** A state with no entity behind it: the review stills and the model tests pose through it. Its springs snap. */
	public static PutricielAnimState detached() {
		PutricielAnimState state = new PutricielAnimState();
		state.detached = true;
		return state;
	}

	/** Sets every replayed value by hand (a detached state's pose). */
	public void set(int moveId, float moveTime, PutricielStage stage, float stageTime, float endingTime, int openingId,
			float openingTime, float kindling, int absolutions, float sinceAccepted) {
		this.moveId = moveId;
		this.moveTime = moveTime;
		this.stage = stage;
		this.stageTime = stageTime;
		this.endingTime = endingTime;
		this.openingId = openingId;
		this.openingTime = openingTime;
		this.openingLength = openingId == 0 ? 0 : 60;
		this.kindling = kindling;
		this.absolutions = absolutions;
		this.age = 0.0F;
		this.acceptedAge = -sinceAccepted;
		this.riseTime = -1.0F;
		this.phase = stage.interval() ? PutricielPhase.INTERVAL.number() : PutricielPhase.ROT_I.number();
		this.inputs = new SaintMoveInputs(phase, 0, 0, 1);
		this.look = PutricielVisualRules.look(stage, stageTime, endingTime);
		select(0.0F);
	}

	/** For the review and the tests: a detached state rising {@code riseTime} ticks into the rise. */
	public void setRise(float riseTime) {
		this.riseTime = riseTime;
		select(0.0F);
	}

	// ---------------------------------------------------------------- the blend and the springs

	/**
	 * Blends {@code pose} out of the last shown pose whenever the track changes (a new move, even the same move again
	 * by its synced serial, an opening, the recoil, the ending), then remembers it. A detached state never blends.
	 */
	public void blend(float[] pose) {
		if (detached) return;
		long key = (long) track.id() << 32 | (track.move() ? serial & 0xFFFFFFFFL : 0L);
		blend.apply(pose, key, age, PutricielVisualRules.BLEND_TICKS);
	}

	/**
	 * Steps spring {@code slot} this frame toward {@code driver} and returns how far it trails it: the overlap of a
	 * part hanging off or riding on a moving one. A detached state's springs sit on their drivers.
	 */
	public float lag(int slot, float driver, float frequency, float damping) {
		if (detached) {
			springs[slot].reset(driver);
			return 0.0F;
		}
		return springs[slot].lag(driver, frameDt, frequency, damping);
	}

	/** Steps spring {@code slot} this frame toward {@code target} and returns its value. */
	public float follow(int slot, float target, float frequency, float damping) {
		if (detached) {
			springs[slot].reset(target);
			return target;
		}
		return springs[slot].step(target, frameDt, frequency, damping);
	}

	/** Snaps spring {@code slot} to {@code value} (a pendulum carried too far by a teleport). */
	public void reset(int slot, float value) {
		springs[slot].reset(value);
	}

	// ---------------------------------------------------------------- what the tracks read

	public PutricielTracks.Track track() {
		return track;
	}

	public int moveId() {
		return moveId;
	}

	/** Ticks since the running move started; -1 with none. */
	public float moveTime() {
		return moveTime;
	}

	public int openingId() {
		return openingId;
	}

	public float openingTime() {
		return openingTime;
	}

	/** Ticks into the ending; -1 while none runs. */
	public float endingTime() {
		return endingTime;
	}

	/** Ticks into the rise; negative once risen. */
	public float riseTime() {
		return riseTime;
	}

	public PutricielStage stage() {
		return stage;
	}

	/** The kit phase number the bar shows. */
	public int phase() {
		return phase;
	}

	public float stageTime() {
		return stageTime;
	}

	public float kindling() {
		return kindling;
	}

	public int absolutions() {
		return Math.max(0, absolutions);
	}

	/** Ticks since the last absolution was accepted (large when none has been seen). */
	public float sinceAccepted() {
		return age - acceptedAge;
	}

	public float age() {
		return age;
	}

	public PutricielVisualRules.Look look() {
		return look;
	}

	/**
	 * 0 hunched in the rot, 1 straightened in the fire, read off the look's hunch so it moves exactly as the look
	 * does (it never steps between stages).
	 */
	public float fire() {
		float span = PutricielVisualRules.HUNCH_ROT - PutricielVisualRules.HUNCH_FIRE;
		return HemoEasing.clamp01((PutricielVisualRules.HUNCH_ROT - look.hunchDegrees()) / span);
	}

	/** The Rot III shamble's quicker lunges (every 12 ticks rather than 16). */
	public boolean blight() {
		return phase == PutricielPhase.ROT_III.number();
	}

	/** 0 at rest between moves, easing to 1 through a move, an opening or a set piece: idle business fades under it. */
	public float busy() {
		return detached ? (track.id() == PutricielTracks.IDLE ? 0.0F : 1.0F) : HemoEasing.clamp01(busy.value());
	}

	/** -1 to 1: how hard the body is turning (positive to his right), smoothed. */
	public float turn() {
		return turn.value();
	}

	/** Degrees turned in total: the feet shuffle round on it when he turns in place. */
	public float turnTravel() {
		return turnTravel;
	}

	/** Radians turned in total, signed: the roots and chains drag round on it. */
	public float yawTravel() {
		return yawTravel;
	}

	/** The breath's phase, radians. */
	public float breath() {
		return breath;
	}

	/** The sacs' pulse phase, radians. */
	public float sacPhase() {
		return sacPhase;
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

	/** The body's yaw this frame, radians (0 in a detached state). */
	public float bodyYawRadians() {
		return Float.isNaN(bodyYaw) ? 0.0F : bodyYaw * Mth.DEG_TO_RAD;
	}

	/** Where the body stands this frame, in model units (eight a block), for the censers' pendulums. */
	public double worldX() {
		return worldX * PutricielGait.BLOCK;
	}

	public double worldY() {
		return worldY * PutricielGait.BLOCK;
	}

	public double worldZ() {
		return worldZ * PutricielGait.BLOCK;
	}

	/** The shelves' unfurling through the rise (design section 13): 0.6 at tick 30 to 1 at 60. */
	public float riseGrowth() {
		if (riseTime < 0.0F) return 1.0F;
		return HemoEasing.lerp(0.6F, 1.0F, HemoEasing.ramp(riseTime, 30.0F, 60.0F));
	}

	// ---------------------------------------------------------------- what the renderer records

	public Vec3 core() {
		return core;
	}

	public Vec3 palm() {
		return palm;
	}

	public Vec3 face() {
		return face;
	}

	public Vec3 staffFoot() {
		return staffFoot;
	}

	/** The staff head the censers hang from, relative to the body as the other points are. */
	public Vec3 staffHead() {
		return staffHead;
	}

	public Vec3[] sacs() {
		return sacs;
	}

	public Vec3[] censers() {
		return censers;
	}

	public void attachments(Vec3 core, Vec3 palm, Vec3 face, Vec3 staffFoot, Vec3[] sacs, Vec3[] censers) {
		this.core = core;
		this.palm = palm;
		this.face = face;
		this.staffFoot = staffFoot;
		this.sacs = sacs;
		this.censers = censers;
	}

	/**
	 * As {@link #attachments(Vec3, Vec3, Vec3, Vec3, Vec3[], Vec3[])}, also recording the staff head and keeping the
	 * world positions of the palm, both censers and the staff head at {@code age} for their trails.
	 */
	public void attachments(Vec3 core, Vec3 palm, Vec3 face, Vec3 staffFoot, Vec3 staffHead, Vec3[] sacs, Vec3[] censers,
			Vec3 origin, float age) {
		attachments(core, palm, face, staffFoot, sacs, censers);
		this.staffHead = staffHead;
		palmTrail.record(origin.add(palm), age);
		for (int i = 0; i < censerTrails.length && i < censers.length; i++) censerTrails[i].record(origin.add(censers[i]), age);
		staffHeadTrail.record(origin.add(staffHead), age);
	}

	/**
	 * Where the offering palm was in the world at {@code age} (the entity's render age), or null outside the kept
	 * frames: the Offering Grasp's reach and the Cauterising Lane's low sweep follow it.
	 */
	public Vec3 palmAt(float age) {
		return palmTrail.at(age);
	}

	/**
	 * Where censer {@code index} (0 the left chain's, 1 the right's) was in the world at {@code age}, or null: the
	 * Censer Swing and Backswing, the Orbit, the Drop and the Cast follow it.
	 */
	public Vec3 censerAt(int index, float age) {
		return index < 0 || index >= censerTrails.length ? null : censerTrails[index].at(age);
	}

	/** Where the staff head was in the world at {@code age}, or null: the Purging Sweep's reach follows it. */
	public Vec3 staffHeadAt(float age) {
		return staffHeadTrail.at(age);
	}

	// ---------------------------------------------------------------- the purifier on fire (revision R1)

	/**
	 * The moment the body fire is read at ({@link PutricielBodyFireRules}): the stage and its clock, the Kindling, the
	 * Repose deed, the running move with its commit and active span on this fight's timeline, the opening, the last
	 * accepted absolution, the last hit and the ending. The model's growth stagger and burning layer and the world
	 * geometry's flames all read this one.
	 */
	public PutricielBodyFireRules.Moment fireMoment() {
		PutricielMove move = moveId == 0 ? null : PutricielMove.byId(moveId);
		int commit = 0, active = 0;
		if (move != null) {
			BossMoveTimeline timeline = move.timeline(inputs);
			commit = timeline.windup();
			active = timeline.active();
		}
		float sinceHit = Float.isNaN(hitAge) ? -1.0F : age - hitAge;
		return new PutricielBodyFireRules.Moment(stage, stageTime, kindling, PutricielDeeds.repose(inputs.deedMask()),
				move == null ? 0 : moveId, move == null ? -1.0F : moveTime, commit, active, openingId, openingTime,
				sinceAccepted(), sinceHit, endingTime);
	}

	/**
	 * The body fire's anchors this frame (the generator's seventy, in table order): each one's point relative to the
	 * body as drawn, its outward world normal, its world velocity in blocks a tick, its region, its size in the region's
	 * tongue range and its kindle order. Empty until the renderer has drawn the body once.
	 */
	public List<PutricielFireAnchors.Anchor> fireAnchors() {
		return anchors;
	}

	/** A growth part's kindle order (PutricielFireAnchors.kindleOrder), -1 for a part that is not a growth. */
	public int kindleOrder(String part) {
		return PutricielFireAnchors.kindleOrder(part);
	}

	/** 0 to 1: how much the fire lights the body it is on (the renderer's block-light floor follows it). */
	public float selfLight() {
		return PutricielFireAnchors.selfLight(stage, stageTime, endingTime);
	}

	/**
	 * Keeps this frame's fire anchors: {@code points} relative to the body as drawn and {@code normals} in the world
	 * (table order), {@code origin} the body's world position and {@code age} its render age. Each anchor's velocity is
	 * its world point's change since the last frame over the render-age step (so it holds at any frame rate), eased
	 * over half a tick; a rewind or a long gap starts it again at rest, and a teleport's jump is never a velocity.
	 */
	public void recordFireAnchors(Vec3[] points, Vec3[] normals, Vec3 origin, float age) {
		int n = Math.min(PutricielFireAnchors.COUNT, Math.min(points.length, normals.length));
		float dt = Float.isNaN(anchorAge) ? -1.0F : age - anchorAge;
		if (dt >= 0.0F && dt < 1.0E-4F && anchors.size() == n) return;
		boolean restart = dt < 0.0F || dt > 5.0F;
		List<PutricielFireAnchors.Anchor> next = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			Vec3 world = origin.add(points[i]);
			Vec3 velocity = anchorVelocity[i] == null ? Vec3.ZERO : anchorVelocity[i];
			if (restart || anchorWorld[i] == null) {
				velocity = Vec3.ZERO;
			} else {
				Vec3 raw = world.subtract(anchorWorld[i]).scale(1.0D / dt);
				if (raw.lengthSqr() > 4.0D) raw = velocity;
				velocity = velocity.lerp(raw, 1.0D - Math.exp(-dt / 0.5D));
			}
			anchorWorld[i] = world;
			anchorVelocity[i] = velocity;
			next.add(new PutricielFireAnchors.Anchor(points[i], normals[i], velocity,
					PutricielFireAnchors.Region.byOrdinal(PutricielModel.ANCHOR_REGIONS[i]), PutricielFireAnchors.size(i),
					PutricielModel.ANCHOR_ORDERS[i]));
		}
		anchors = Collections.unmodifiableList(next);
		anchorAge = age;
	}

	/** For tests: the pose last shown. */
	float[] lastShown() {
		return blend.shown();
	}
}
