package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.world.phys.Vec3;

/** The controller's pure rules: aim, interrupt boundary, floors and the recovery an opening stretches. JVM-tested. */
public final class SaintControllerRules {
	/** The aim resends while tracking when it moves this far. */
	public static final double AIM_SYNC_STEP = 0.05D;

	private SaintControllerRules() {
	}

	/**
	 * Where the move aims this tick. TRACK follows the target until the commit, then keeps the locked point;
	 * LOCK_AT_START keeps the point taken at the start; LEAD aims {@code leadTicks} ahead of the target's motion until
	 * the commit, then keeps it.
	 */
	public static Vec3 aim(SaintMove.Aim aim, boolean committed, Vec3 locked, Vec3 target, Vec3 targetMotion,
			int leadTicks) {
		return switch (aim) {
			case LOCK_AT_START -> locked;
			case TRACK -> committed ? locked : target;
			case LEAD -> committed ? locked : target.add(targetMotion.x * leadTicks, 0.0D, targetMotion.z * leadTicks);
		};
	}

	/** Staggers, openings and ordinary phase changes wait for this: no move, or a move in its recovery. */
	public static boolean interruptible(boolean moving, BossMoveTimeline.Stage stage) {
		return !moving || stage == BossMoveTimeline.Stage.RECOVERY || stage == BossMoveTimeline.Stage.COMPLETE;
	}

	/** The timeline a move runs: its own, with the wind-up raised to the kind's floor. */
	public static BossMoveTimeline effective(SaintMove.Kind kind, BossMoveTimeline timeline) {
		return SaintMoves.floored(kind, timeline);
	}

	/** A recovery opening stretches the recovery to the opening's length. */
	public static BossMoveTimeline withOpening(BossMoveTimeline timeline, int openingTicks) {
		if (openingTicks <= timeline.recovery()) return timeline;
		return new BossMoveTimeline(timeline.windup(), timeline.active(), openingTicks);
	}

	/**
	 * The tick a footprint first shows: the spec's reveal tick, pulled earlier so it always shows for the kind's floor
	 * before the commit, whatever a deed hides; never before the move's start.
	 */
	public static int revealTick(TelegraphSpec spec, BossMoveTimeline timeline, SaintMove.Kind kind) {
		int latest = timeline.commitTick() - SaintMoves.revealFloor(kind);
		return Math.max(0, Math.min(spec.revealTick(), latest));
	}

	public static boolean aimMoved(Vec3 sent, Vec3 aim) {
		return sent.distanceToSqr(aim) >= AIM_SYNC_STEP * AIM_SYNC_STEP;
	}
}
