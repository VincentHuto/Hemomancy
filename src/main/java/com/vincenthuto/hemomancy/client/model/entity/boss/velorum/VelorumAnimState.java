package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathPoseBlend;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathSpring;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDeedRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * One Velorum's client animation state, kept per body rather than on the renderer's shared model. Its clocks are the
 * kit's synced move, opening and ending, each replayed through the {@code BossReplayClock}s of {@link SaintMoveView};
 * from them it picks the playing track and its time on the authored clock. It also carries what the pose needs from
 * one frame to the next: the inertial blend out of the last shown pose ({@link HemorathPoseBlend}, resolution B-8,
 * shared with Hemorath), the bank of springs that carry his secondary motion ({@link HemorathSpring}), the phase
 * weights the stance blends across, the breath and the crystals' turn as carried phases, what his body is doing in the
 * world (the glide and the turn, measured, so nothing new is synced), and the anchors and limb trails the renderer
 * records each frame for the world geometry: the lantern head, the staff's heel, the claw and the side lantern, so the
 * effects follow the animated limb and never disagree with it.
 *
 * <p>{@link Frame} keeps the crossfade weights the world geometry and the glow layer fade on; the body itself blends by
 * inertialization.
 *
 * <p>Track priority (revision 2026-11-06): the ending over an opening over a move over the stance, as before; the six
 * new moves are ordinary move tracks under it. Two overlays ride on top of whatever plays, neither a track of its own
 * so neither steals the blend: the phase-3 card's sink and rise ({@link #cardTime}, the Shadow Step's keys over the
 * card's 50 ticks, from the frame the synced phase first reads 3) and the relit Eclipse's jolt ({@link #relitTime},
 * from the frame the Eclipse is cut off inside its hold, by the Guttering Lantern or any other track). The sink is
 * also a translation of the whole body under the floor, {@link #under}, which the renderer reads from the synced
 * clocks so it never lags the pose.
 */
public final class VelorumAnimState {
	/** Every track blends from the last pose over 5 ticks (resolution B-8). */
	public static final float BLEND = 5.0F;
	/** A phase change blends over the card's length: the Bow, then a slow rise to full height. */
	public static final float PHASE_RISE = 50.0F;
	/** The ending's own blend: the killing hit stops him where he is, briefly. */
	public static final float ENDING_BLEND = 3.0F;
	public static final int TRACK_IDLE = 0;
	public static final int TRACK_MOVE = 100;
	public static final int TRACK_OPENING = 200;
	public static final int TRACK_ENDING = 300;
	/** Spring slots the model's secondary pass may use. */
	public static final int SPRINGS = 40;
	/** Glide speed (blocks a tick) at which the lean and the trailing cloth are full: his phase-2 glide. */
	public static final float GLIDE_FULL = 0.12F;
	/** Body yaw rate (degrees a tick) that reads as a full turn: his phase-3 turn rate. */
	public static final float TURN_FULL = 8.0F;
	/** The Eclipse on its authored clock: the shapes rise over 20 ticks from the end of the wind-up (40), the hold ends at
	 * 200, and unbroken the shapes sink over the first 20 ticks of the recovery; relit, they fall back over 12. */
	public static final float ECLIPSE_HOLD = 40.0F, ECLIPSE_HOLD_END = 200.0F, SHAPE_RISE = 20.0F, SHAPE_FALL = 12.0F;
	/** Breaths a tick (radians): one every 140 ticks, one every 200 in the Endless Hour. */
	public static final float BREATH_RATE = Mth.TWO_PI / 140.0F, BREATH_RATE_HOUR = Mth.TWO_PI / 200.0F;
	/** The carried phases wrap by this whole number of turns, so they never lose precision. */
	private static final float WRAP = (float) (Math.PI * 2.0D * 1000.0D);

	/** One frame of Velorum's pose inputs. */
	public record Frame(int move, float moveTime, float moveWeight, int last, float lastTime, float lastWeight,
			int opening, float openingTime, int openingLength, float openingWeight, float ending, int phase, float glide,
			boolean hour, int deeds) {
		public static final Frame REST = new Frame(0, -1.0F, 0.0F, 0, -1.0F, 0.0F, 0, -1.0F, 0, 0.0F, -1.0F, 1, 0.0F,
				false, 0);

		public static Frame move(VelorumMove move, float time) {
			return new Frame(move.id(), time, 1.0F, 0, -1.0F, 0.0F, 0, -1.0F, 0, 0.0F, -1.0F, 1, 0.0F,
					move == VelorumMove.ENDLESS_HOUR, 0);
		}

		public static Frame opening(int id, float time, int length) {
			return new Frame(0, -1.0F, 0.0F, 0, -1.0F, 0.0F, id, time, length, 1.0F, -1.0F, 1, 0.0F, false, 0);
		}

		public static Frame ending(float time) {
			return new Frame(0, -1.0F, 0.0F, 0, -1.0F, 0.0F, 0, -1.0F, 0, 0.0F, time, 1, 0.0F, false, 0);
		}

		/** The track this frame shows: the ending over an opening over a move. */
		public int track() {
			return select(move, opening, ending);
		}

		/** The shown track's time on its authored clock. */
		public float trackTime() {
			int track = track();
			if (track == TRACK_ENDING) return ending;
			if (track > TRACK_OPENING) return openingTime;
			if (track > TRACK_MOVE) return moveTime;
			return 0.0F;
		}
	}

	private static final Map<VelorumEntity, VelorumAnimState> STATES = new WeakHashMap<>();

	private final HemorathPoseBlend blend = new HemorathPoseBlend();
	private final HemorathSpring[] springs = new HemorathSpring[SPRINGS];
	private final HemorathSpring glideSpring = new HemorathSpring();
	private final HemorathSpring turnSpring = new HemorathSpring();
	private final HemorathSpring busySpring = new HemorathSpring();
	private final HemorathSpring congregationSpring = new HemorathSpring();
	private final HemorathSpring hourSpring = new HemorathSpring();
	private final VelorumLimbTrail lanternTrail = new VelorumLimbTrail();
	private final VelorumLimbTrail heelTrail = new VelorumLimbTrail();
	private final VelorumLimbTrail clawTrail = new VelorumLimbTrail();
	private final VelorumLimbTrail sideTrail = new VelorumLimbTrail();

	private int move;
	private float moveTime = -1.0F;
	private int last;
	private float lastTime = -1.0F;
	private float lastEnded = Float.NEGATIVE_INFINITY;
	private int opening;
	private float openingTime = -1.0F;
	private int openingLength;
	private int lastOpening;
	private float lastOpeningTime = -1.0F;
	private int lastOpeningLength;
	private float openingEnded = Float.NEGATIVE_INFINITY;
	private Frame frame = Frame.REST;

	private int track = TRACK_IDLE;
	private float time;
	private int length;
	private int serial;
	private int phase = 1;
	private int deeds;
	private float lastAge = Float.NaN;
	private float frameDt;
	private float breath;
	private float spin;
	private float speed;
	private float bodyYaw = Float.NaN;
	private float yawTravel;
	private long lastKey = Long.MIN_VALUE;
	private int seenPhase;
	private float cardStart = Float.NaN;
	private float relitAt = Float.NaN;
	private float eclipseTime = -1.0F;
	private float now;

	private Vec3 core = new Vec3(0.0D, 1.5D, 0.0D);
	private Vec3 claw = new Vec3(1.0D, 1.8D, 0.0D);
	private Vec3 seam = new Vec3(0.0D, 1.6D, -0.2D);
	private Vec3 heel = new Vec3(-1.0D, 0.0D, 0.0D);
	private Vec3[] lanterns = {new Vec3(-1.0D, 2.5D, 0.0D), new Vec3(-1.0D, 1.0D, 0.0D), new Vec3(0.5D, 1.0D, 0.0D)};

	private VelorumAnimState() {
		for (int i = 0; i < SPRINGS; i++) springs[i] = new HemorathSpring();
	}

	public static VelorumAnimState of(VelorumEntity boss) {
		return STATES.computeIfAbsent(boss, ignored -> new VelorumAnimState());
	}

	/** Forgets every body's state, and the woken devotees' with them. */
	public static void clear() {
		STATES.clear();
		VelorumDevoteeAnimState.clear();
	}

	/** The track a frame shows: the ending over an opening over a move over the stance. */
	public static int select(int move, int opening, float ending) {
		if (ending >= 0.0F) return TRACK_ENDING;
		if (opening != 0) return TRACK_OPENING + opening;
		if (move != 0) return TRACK_MOVE + move;
		return TRACK_IDLE;
	}

	/**
	 * Reads the synced state and the body's own motion once a frame; {@code age} is the body's age with the partial
	 * tick. A second call in the same frame steps nothing.
	 */
	public void update(VelorumEntity boss, float partial, float age) {
		SaintMoveView view = boss.view();
		frameDt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(5.0F, age - lastAge));
		lastAge = age;
		float now = AnimationTime.gameTime(boss.level(), partial);
		this.now = now;
		int id = view.moveId();
		int eclipse = VelorumMove.ECLIPSE.id();
		// the Eclipse cut off inside its hold (a new move, or an opening over it) is the light relit
		boolean cut = move == eclipse && eclipseTime > ECLIPSE_HOLD && eclipseTime < ECLIPSE_HOLD_END - 1.0F
				&& (id != eclipse || view.openingId() != 0);
		if (cut) relitAt = now;
		if (id != move) {
			if (move != 0) {
				last = move;
				lastTime = moveTime;
				lastEnded = now;
			}
			move = id;
		}
		moveTime = id == 0 ? -1.0F : trackTime(VelorumMove.byId(id), view.moveTime(partial), view.inputs());
		eclipseTime = id == eclipse && view.openingId() == 0 ? moveTime : -1.0F;
		int open = view.openingId();
		if (open != opening) {
			if (opening != 0) {
				lastOpening = opening;
				lastOpeningTime = openingTime;
				lastOpeningLength = openingLength;
				openingEnded = now;
			}
			opening = open;
		}
		openingTime = open == 0 ? -1.0F : Math.max(0.0F, view.openingTime(partial));
		openingLength = open == 0 ? 0 : view.openingLength();
		float ending = view.endingTime(partial);
		float moveWeight = move == 0 ? 0.0F : HemoEasing.ramp(moveTime, 0.0F, BLEND);
		float lastWeight = last == 0 ? 0.0F : 1.0F - HemoEasing.ramp(now - lastEnded, 0.0F, BLEND);
		if (lastWeight <= 0.0F) last = 0;
		boolean showOpening = opening != 0;
		int shownOpening = showOpening ? opening : lastOpening;
		float shownTime = showOpening ? openingTime : lastOpeningTime;
		int shownLength = showOpening ? openingLength : lastOpeningLength;
		float openingWeight = showOpening ? HemoEasing.ramp(openingTime, 0.0F, BLEND)
				: lastOpening == 0 ? 0.0F : 1.0F - HemoEasing.ramp(now - openingEnded, 0.0F, BLEND);
		if (!showOpening && openingWeight <= 0.0F) lastOpening = 0;
		phase = boss.syncedPhase();
		// the phase-3 card starts on the frame the phase is first read as 3 (never on a body first seen in phase 3)
		if (phase >= 3 && seenPhase > 0 && seenPhase < 3) cardStart = now;
		seenPhase = phase;
		deeds = boss.syncedDeeds();
		serial = boss.syncedMoveSerial();
		measure(boss, partial);
		frame = new Frame(move, moveTime, moveWeight, last, lastTime, lastWeight, shownOpening, shownTime, shownLength,
				openingWeight, ending, phase, glide(), move == VelorumMove.ENDLESS_HOUR.id(), deeds);
		// the body's own track: the live ending, opening or move, never a fading one (the blend carries the fade)
		track = select(move, opening, ending);
		time = track == TRACK_ENDING ? ending : track > TRACK_OPENING ? openingTime : track > TRACK_MOVE ? moveTime : age;
		length = track > TRACK_OPENING && track < TRACK_ENDING ? openingLength : 0;
		busySpring.step(track == TRACK_IDLE ? 0.0F : 1.0F, frameDt, 0.3F, 1.0F);
		congregationSpring.step(phase >= 2 ? 1.0F : 0.0F, frameDt, 0.12F, 1.0F);
		hourSpring.step(phase >= 3 ? 1.0F : 0.0F, frameDt, 0.12F, 1.0F);
		breath = (breath + frameDt * HemoEasing.lerp(BREATH_RATE, BREATH_RATE_HOUR, hour())) % WRAP;
	}

	/**
	 * A move's time as its track reads it: a phase-3 wind-up cut by the kit plays the authored wind-up keys faster, so
	 * the commit lands on the authored commit beat, and a recovery the Repose deed shortens plays the authored recovery
	 * keys faster, so the move still settles; the active span runs at its own pace.
	 */
	static float trackTime(VelorumMove move, float time, SaintMoveInputs inputs) {
		if (move == null || time < 0.0F) return time;
		BossMoveTimeline authored = move.authored();
		BossMoveTimeline live = move.timeline(inputs);
		int windup = live.windup();
		if (windup > 0 && windup != authored.windup() && time < windup) return time * authored.windup() / windup;
		float after = windup <= 0 ? time : time - windup + authored.windup();
		float recoveryStart = authored.windup() + authored.active();
		if (live.recovery() > 0 && live.recovery() != authored.recovery() && after > recoveryStart)
			return recoveryStart + (after - recoveryStart) * authored.recovery() / live.recovery();
		return after;
	}

	/** What the body is doing in the world: the glide's speed and the turn, smoothed so a stop settles. */
	private void measure(VelorumEntity boss, float partial) {
		float dx = (float) (boss.getX() - boss.xo), dz = (float) (boss.getZ() - boss.zo);
		speed = Mth.sqrt(dx * dx + dz * dz);
		float yawRate = Mth.wrapDegrees(boss.yBodyRot - boss.yBodyRotO);
		glideSpring.step(HemoEasing.clamp01(speed / GLIDE_FULL), frameDt, 0.25F, 0.95F);
		turnSpring.step(Mth.clamp(yawRate / TURN_FULL, -1.0F, 1.0F), frameDt, 0.3F, 0.85F);
		float yaw = Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot);
		if (!Float.isNaN(bodyYaw)) yawTravel += Mth.wrapDegrees(yaw - bodyYaw) * Mth.DEG_TO_RAD;
		bodyYaw = yaw;
	}

	/**
	 * Blends {@code pose} out of the last shown pose whenever the track changes (a new move, even the same move again by
	 * its synced serial, an opening, the ending, the stance) or the phase does, then remembers it. A phase change blends
	 * over the card's length, so he rises slowly out of the Bow that took him there.
	 */
	public void blend(float[] pose, float age) {
		long key = (long) track << 40 | (long) phase << 32
				| (track > TRACK_MOVE && track < TRACK_OPENING ? serial & 0xFFFFFFFFL : 0L);
		float duration = track == TRACK_ENDING ? ENDING_BLEND : BLEND;
		if (phaseRising(key)) duration = PHASE_RISE;
		blend.apply(pose, key, age, duration);
	}

	private boolean phaseRising(long key) {
		boolean rising = lastKey != Long.MIN_VALUE && key != lastKey && (int) (key >>> 32 & 0xFF) > (int) (lastKey >>> 32 & 0xFF);
		lastKey = key;
		return rising;
	}

	/**
	 * Steps spring {@code slot} this frame toward {@code driver} and returns how far it trails it: the overlap of a part
	 * hanging off or riding on a moving one. {@code frequency} is radians a tick, {@code damping} the ratio.
	 */
	public float lag(int slot, float driver, float frequency, float damping) {
		return springs[slot].lag(driver, frameDt, frequency, damping);
	}

	/** Steps spring {@code slot} this frame toward {@code target} and returns its value. */
	public float follow(int slot, float target, float frequency, float damping) {
		return springs[slot].step(target, frameDt, frequency, damping);
	}

	/** Turns the lanterns' crystals on by {@code rate} radians a tick for this frame and returns their angle. */
	public float spin(float rate) {
		spin = (spin + frameDt * rate) % WRAP;
		return spin;
	}

	public int track() {
		return track;
	}

	/** Ticks into the phase-3 card's sink and rise, or -1 outside it. */
	public float cardTime() {
		if (Float.isNaN(cardStart)) return -1.0F;
		float t = now - cardStart;
		return t >= 0.0F && t < VelorumModel.CARD_LENGTH ? t : -1.0F;
	}

	/** Ticks since the Eclipse was relit, or -1 once the jolt has run. */
	public float relitTime() {
		if (Float.isNaN(relitAt)) return -1.0F;
		float t = now - relitAt;
		return t >= 0.0F && t < VelorumModel.RELIT_LENGTH ? t : -1.0F;
	}

	/**
	 * How far under the floor his body is drawn this frame (blocks): the Shadow Step's sink and rise on the synced move
	 * clock, or the phase-3 card's. Read by the renderer before the pose, so it comes from the synced clocks directly.
	 */
	public float under(VelorumEntity boss, float partial) {
		SaintMoveView view = boss.view();
		float depth = 0.0F;
		if (view.moveId() == VelorumMove.UMBRAL_STEP.id() && view.openingId() == 0) {
			depth = VelorumModel.stepUnder(trackTime(VelorumMove.UMBRAL_STEP, view.moveTime(partial), view.inputs()));
		}
		if (!Float.isNaN(cardStart)) {
			float t = AnimationTime.gameTime(boss.level(), partial) - cardStart;
			if (t >= 0.0F && t < VelorumModel.CARD_LENGTH) depth = Math.max(depth, VelorumModel.cardUnder(t));
		}
		return depth;
	}

	/**
	 * 0 to 1: how far the congregation's shapes of liquid dark have stood up out of their figures. They rise from the end
	 * of the Eclipse's wind-up, hold, and sink as the light seeps back unbroken; relit, they fall back fast.
	 */
	public float shapes() {
		if (eclipseTime >= 0.0F) {
			return HemoEasing.ramp(eclipseTime, ECLIPSE_HOLD, ECLIPSE_HOLD + SHAPE_RISE)
					* (1.0F - HemoEasing.ramp(eclipseTime, ECLIPSE_HOLD_END, ECLIPSE_HOLD_END + SHAPE_RISE));
		}
		if (!Float.isNaN(relitAt)) {
			float t = now - relitAt;
			if (t >= 0.0F && t < SHAPE_FALL) return 1.0F - HemoEasing.ramp(t, 0.0F, SHAPE_FALL);
		}
		return 0.0F;
	}

	/** The Eclipse's time on its authored clock while it plays, else -1. */
	public float eclipseTime() {
		return eclipseTime;
	}

	public float time() {
		return time;
	}

	/** The open window's length in ticks (the Gutter, the Overheld, the Bow), else 0. */
	public int length() {
		return length;
	}

	public int phase() {
		return phase;
	}

	public int deeds() {
		return deeds;
	}

	public boolean repose() {
		return VelorumDeedRules.repose(deeds);
	}

	/** 0 at rest between moves, easing to 1 through a move, an opening or the ending: the idle business fades under it. */
	public float busy() {
		return HemoEasing.clamp01(busySpring.value());
	}

	/** 0 to 1: how far the stance has become the Congregation's (phase 2 and on), eased. */
	public float congregation() {
		return HemoEasing.clamp01(congregationSpring.value());
	}

	/** 0 to 1: how far the stance has become the Endless Hour's (phase 3), eased. */
	public float hour() {
		return HemoEasing.clamp01(hourSpring.value());
	}

	/** 0 to 1: the glide, from his measured ground speed, smoothed so the cloth settles after a stop. */
	public float glide() {
		return HemoEasing.clamp01(glideSpring.value());
	}

	/** -1 to 1: how hard he is turning (positive to his right), smoothed. */
	public float turn() {
		return turnSpring.value();
	}

	/** Radians turned in total, signed: the trains and the hanging chains drag round on it. */
	public float yawTravel() {
		return yawTravel;
	}

	/** The breath's phase, radians. */
	public float breath() {
		return breath;
	}

	/** Blocks a tick over the ground, from the last tick's move. */
	public float speed() {
		return speed;
	}

	/** This frame's length in ticks. */
	public float frameDt() {
		return frameDt;
	}

	public Frame frame() {
		return frame;
	}

	public Vec3 core() {
		return core;
	}

	public Vec3 claw() {
		return claw;
	}

	public Vec3 seam() {
		return seam;
	}

	/** The staff's iron heel, relative to his feet as the other anchors are. */
	public Vec3 heel() {
		return heel;
	}

	public Vec3[] lanterns() {
		return lanterns;
	}

	public void attachments(Vec3 core, Vec3 claw, Vec3 seam, Vec3[] lanterns) {
		this.core = core;
		this.claw = claw;
		this.seam = seam;
		this.lanterns = lanterns;
	}

	/**
	 * As {@link #attachments(Vec3, Vec3, Vec3, Vec3[])}, also recording the heel and keeping the world positions of the
	 * staff lantern, the heel, the claw and the side lantern at {@code age} (the body's render age) for their trails.
	 * {@code origin} is his feet in the world.
	 */
	public void attachments(Vec3 core, Vec3 claw, Vec3 seam, Vec3 heel, Vec3[] lanterns, Vec3 origin, float age) {
		attachments(core, claw, seam, lanterns);
		this.heel = heel;
		if (lanterns.length > 0) lanternTrail.record(age, origin.add(lanterns[0]));
		if (lanterns.length > 2) sideTrail.record(age, origin.add(lanterns[2]));
		heelTrail.record(age, origin.add(heel));
		clawTrail.record(age, origin.add(claw));
	}

	/** Where the staff lantern was in the world at {@code age} (his render age), or null outside the kept frames. */
	public Vec3 lanternAt(float age) {
		return lanternTrail.at(age);
	}

	/** Where the staff's heel was in the world at {@code age}, or null outside the kept frames. */
	public Vec3 heelAt(float age) {
		return heelTrail.at(age);
	}

	/** Where the claw was in the world at {@code age}, or null outside the kept frames. */
	public Vec3 clawAt(float age) {
		return clawTrail.at(age);
	}

	/** Where the side lantern (the Hanging Lantern's) was in the world at {@code age}, or null outside the kept frames. */
	public Vec3 sideLanternAt(float age) {
		return sideTrail.at(age);
	}
}
