package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client-only motion memory for one Mycophant. The model is shared between entities and posed from synced counters
 * that arrive in uneven bursts, so what needs continuity lives here: the replayed state, attack, intro, split and yield
 * clocks, the eased claim squeeze, glow, drag gait, siphon latch and Gaze beam, the move a stagger interrupted, and the
 * last rendered pose that a state, attack or hold change blends out of instead of snapping.
 */
public final class MycophantAnimState {
	private static final Map<MycophantEntity, MycophantAnimState> STATES = new WeakHashMap<>();

	/** Ticks a state, attack or hold change takes to blend out of the last rendered pose. */
	private static final float BLEND_TICKS = 6.0F;
	/** Ticks the claws take to fall into or out of the drag gait. */
	private static final float GAIT_EASE_TICKS = 6.0F;
	/** Ticks the siphon takes to swing onto or off its latched target. */
	private static final float LATCH_EASE_TICKS = 5.0F;
	/** Share of the gap to the synced claim meter and glow closed each tick. */
	private static final float FOLLOW = 0.45F;

	private final BossReplayClock stateClock = new BossReplayClock();
	private final BossReplayClock attackClock = new BossReplayClock();
	private final BossReplayClock introClock = new BossReplayClock();
	private final BossReplayClock splitClock = new BossReplayClock();
	private final BossReplayClock yieldClock = new BossReplayClock();

	private float stateTime;
	private float attackTime;
	private float introTime;
	private float splitTime;
	private float yieldTime;

	private int lastTick = Integer.MIN_VALUE;
	private float meterO;
	private float meter;
	private float glowO = 0.6F;
	private float glow = 0.6F;
	private float gaitO;
	private float gait;
	private float latchO;
	private float latch;
	private float gazeO;
	private float gaze;

	/** The last move seen in the attack state and how far it ran, kept through the stagger that follows it. */
	private MycophantAttack lastAttack = MycophantAttack.NONE;
	private boolean staggered;
	/** How the Last Brood ended, as synced (a MycophantLastBroodRules.Outcome ordinal, -1 before it ends). */
	private int lastBroodOutcome = -1;

	private final Vector3f aim = new Vector3f();
	private boolean hasAim;

	private int signature = Integer.MIN_VALUE;
	private float blendStart = -1000.0F;
	private float[] from;
	private float[] last;

	private MycophantAnimState() {
	}

	public static MycophantAnimState of(MycophantEntity entity) {
		return STATES.computeIfAbsent(entity, e -> new MycophantAnimState());
	}

	/** Advances to this frame. Safe to call more than once per frame (the renderer and the model both do). */
	public void update(MycophantEntity entity, float partial) {
		int tick = entity.tickCount;
		stateTime = stateClock.sample(entity.getState().ordinal(), entity.getStateTick(), tick, partial, 1);
		// keyed on the variant as well, so a string's two- and three-hit forms never share a replayed beat
		attackTime = attackClock.sample(entity.getAttack().ordinal() * 4 + entity.getAttackVariant(), entity.getAttackTick(), tick,
				partial, 1);
		introTime = introClock.sample(entity.isRematchIntro() ? 1 : 0, entity.getIntroTick(), tick, partial, 1);
		splitTime = splitClock.sample(0, entity.getSplitTick(), tick, partial, 1);
		yieldTime = yieldClock.sample(0, entity.getYieldTick(), tick, partial, 1);
		rememberInterruptedMove(entity);
		lastBroodOutcome = entity.getLastBroodOutcome();
		if (tick == lastTick) return;
		// a fresh entity, a long unload or a reload: start at the present instead of easing toward it
		boolean snap = lastTick == Integer.MIN_VALUE || tick < lastTick || tick - lastTick > 20;
		int steps = snap ? 1 : Math.min(3, tick - lastTick);
		lastTick = tick;
		for (int i = 0; i < steps; i++) step(entity, snap);
	}

	private void step(MycophantEntity entity, boolean snap) {
		meterO = meter;
		glowO = glow;
		gaitO = gait;
		latchO = latch;
		gazeO = gaze;
		float meterTarget = entity.isHolding() ? entity.getClaimMeter() : 0.0F;
		float gaitTarget = entity.getState() == MycophantState.DRAG || introPivoting(entity) ? 1.0F : 0.0F;
		float latchTarget = entity.getSiphonTargetId() >= 0 ? 1.0F : 0.0F;
		if (snap) {
			meter = meterO = meterTarget;
			glow = glowO = entity.getGlowLevel();
			gait = gaitO = gaitTarget;
			latch = latchO = latchTarget;
			gaze = gazeO = entity.getGazeYaw();
			return;
		}
		meter += (meterTarget - meter) * FOLLOW;
		glow += (entity.getGlowLevel() - glow) * FOLLOW;
		gait = approach(gait, gaitTarget, 1.0F / GAIT_EASE_TICKS);
		latch = approach(latch, latchTarget, 1.0F / LATCH_EASE_TICKS);
		// the beam's yaw arrives once a tick; follow it along the short way round so it interpolates between ticks, and
		// jump straight to it outside the Gaze so a new sweep never swings in from the last one
		if (entity.getAttack() == MycophantAttack.GAZE) gaze += Mth.wrapDegrees(entity.getGazeYaw() - gaze);
		else gaze = gazeO = entity.getGazeYaw();
	}

	/** Notes the move in progress, so the stagger that cuts it off knows what it came out of. */
	private void rememberInterruptedMove(MycophantEntity entity) {
		MycophantState state = entity.getState();
		if (state == MycophantState.ATTACK && entity.getAttack() != MycophantAttack.NONE) {
			lastAttack = entity.getAttack();
			staggered = false;
		} else if (state == MycophantState.STAGGER) {
			staggered = true;
		} else if (state != MycophantState.ATTACK) {
			lastAttack = MycophantAttack.NONE;
			staggered = false;
		}
	}

	/** She hauls round on her claws during the intro's turn instead of spinning in place. */
	private static boolean introPivoting(MycophantEntity entity) {
		if (entity.getState() != MycophantState.INTRO) return false;
		boolean rematch = entity.isRematchIntro();
		int t = entity.getIntroTick();
		int turn = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.TURN, rematch);
		return t >= turn && t < turn + MycophantIntroRules.turnTicks(rematch)
				&& Math.abs(Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO)) > 0.3F;
	}

	private static float approach(float value, float target, float step) {
		return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
	}

	public float stateTime() { return stateTime; }
	public float attackTime() { return attackTime; }
	public float introTime() { return introTime; }
	public float splitTime() { return splitTime; }
	public float yieldTime() { return yieldTime; }

	public float claimMeter(float partial) { return Mth.lerp(partial, meterO, meter); }
	public float glow(float partial) { return Mth.lerp(partial, glowO, glow); }
	/** 0..1: how far the claws have fallen into the drag gait. */
	public float gait(float partial) { return HemoEasing.smoothstep(Mth.lerp(partial, gaitO, gait)); }
	/** 0..1: how far the siphon has swung onto its latched target. */
	public float latch(float partial) { return HemoEasing.smoothstep(Mth.lerp(partial, latchO, latch)); }
	/** The Gaze beam's yaw (degrees, Minecraft convention), interpolated between its synced ticks. */
	public float gazeYaw(float partial) { return Mth.lerp(partial, gazeO, gaze); }

	/** True through the stagger that ends the Last Brood (her collapse when cut free, or the spent stagger). */
	public boolean staggeredOutOfLastBrood() {
		return staggered && lastAttack == MycophantAttack.LAST_BROOD
				&& (lastBroodCutFree() || lastBroodOutcome == MycophantLastBroodRules.Outcome.SPENT.ordinal());
	}

	/**
	 * Whether the Last Brood ended with the player cutting free (the long collapse) rather than the pull running out
	 * (the spent stagger), as the server synced it.
	 */
	public boolean lastBroodCutFree() {
		return lastBroodOutcome == MycophantLastBroodRules.Outcome.CUT_FREE.ordinal();
	}

	/** Keeps the siphon's line to its latched target, so it can ease back off along it once the latch lets go. */
	void rememberAim(Vector3f aim) {
		this.aim.set(aim);
		hasAim = true;
	}

	/** Writes the last remembered siphon line into {@code out}; false if the siphon has never latched. */
	boolean recallAim(Vector3f out) {
		if (hasAim) out.set(aim);
		return hasAim;
	}

	/**
	 * Notes the pose's identity (state, attack, hold) at frame time {@code age}; a change starts a blend out of the last
	 * remembered pose.
	 */
	void begin(int signature, float age, int poseSize) {
		if (last == null) {
			last = new float[poseSize];
			from = new float[poseSize];
			this.signature = signature;
			return;
		}
		if (signature == this.signature) return;
		this.signature = signature;
		System.arraycopy(last, 0, from, 0, poseSize);
		blendStart = age;
	}

	/** 0..1 through the current blend; 1 once it is done (or before any pose has been remembered). */
	float blend(float age) {
		return HemoEasing.smoothstep((age - blendStart) / BLEND_TICKS);
	}

	/** The pose the current blend starts from. */
	float[] from() { return from; }

	/** Where the model writes the pose it drew this frame. */
	float[] last() { return last; }
}
