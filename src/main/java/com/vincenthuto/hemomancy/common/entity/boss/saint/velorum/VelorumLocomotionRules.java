package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

/**
 * Near-stillness (design section 8): Velorum never walks; he glides on the rime at 0.10 and 0.12 blocks a tick and not
 * at all in phase 3, turns 5, 6 and 8 degrees a tick, holds MID (6 to 9 blocks) and closes only for a melee move or a
 * target that has stayed FAR for 60 ticks, and settles 8 ticks after every move. JVM-tested.
 */
public final class VelorumLocomotionRules {
	public static final double[] GLIDE = {0.10D, 0.12D, 0.0D};
	public static final float[] TURN = {5.0F, 6.0F, 8.0F};
	public static final int[] DECISION_DELAY = {24, 18, 12};
	public static final double HOLD_MIN = 6.0D;
	public static final double HOLD_MAX = 9.0D;
	public static final int FAR_CLOSE_TICKS = 60;
	public static final int SETTLE_TICKS = 8;
	/** After the rise he stands 40 ticks on the dais: the meter's first lesson. */
	public static final int LESSON_TICKS = 40;
	/** A melee move's wind-up glides him in until he is this close. */
	public static final double MELEE_CLOSE = 2.5D;
	/** A target sprinting away faster than this (blocks a tick) is never chased. */
	public static final double CHASE_LIMIT = 0.25D;
	/** The blue-ice ring's centre, chamber-local, where phase 3 plants him. */
	public static final double RING_X = 16.0D;
	public static final double RING_Z = 13.0D;
	public static final double PLANTED_REACH = 0.4D;

	private VelorumLocomotionRules() {
	}

	private static int index(int phase) {
		return Math.max(0, Math.min(2, phase - 1));
	}

	/** Blocks a tick; the enrage's speed factor applies to both glide and turn. */
	public static double glide(int phase, double speedFactor) {
		return GLIDE[index(phase)] * speedFactor;
	}

	/** Degrees a tick; halved while two allies grip him. */
	public static float turn(int phase, double speedFactor, boolean gripped) {
		float rate = (float) (TURN[index(phase)] * speedFactor);
		return gripped ? rate * 0.5F : rate;
	}

	public static int decisionDelay(int phase, boolean repose) {
		return VelorumDeedRules.decisionDelay(DECISION_DELAY[index(phase)], repose);
	}

	public static int settle(boolean repose) {
		return repose ? 0 : SETTLE_TICKS;
	}

	/**
	 * Where he wants to be along the line to the target: -1 to back off, 0 to hold, 1 to close. He closes when the
	 * target has stayed FAR long enough and is not sprinting away, and backs off only when crowded inside HOLD_MIN while
	 * he has no move.
	 */
	public static int approach(double distance, int farTicks, double awaySpeed) {
		if (distance > HOLD_MAX && farTicks >= FAR_CLOSE_TICKS && awaySpeed < CHASE_LIMIT) return 1;
		return 0;
	}

	// ── the Shadow Step (revision 2026-11-06, R.5) ───────────────────

	/** Only phase 1 closes on a FAR target by gliding; phase 2 closes by the Shadow Step, phase 3 is planted. */
	public static boolean closesByGlide(int phase) {
		return phase <= 1;
	}

	/** Untouchable, and his presence off, from tick 14 of the Step to four ticks past its commit. */
	public static final int STEP_UNDER = 14;
	public static final int STEP_SURFACED = 4;
	/** A pool where he sinks at tick 2, one where he will rise at tick 4; he sinks from tick 8. */
	public static final int STEP_POOL_DEPART = 2;
	public static final int STEP_POOL_ARRIVE = 4;
	public static final int STEP_SINK = 8;
	/** He rises 4 to 6 blocks from the target, within 1.5 of a frozen or stilled figure, beside or behind them. */
	public static final double STEP_MIN = 4.0D;
	public static final double STEP_MAX = 6.0D;
	public static final double STEP_FIGURE = 1.5D;
	/** "Beside or behind": the angle at the target between him and the destination is at least 60 degrees. */
	public static final double STEP_BESIDE_COS = 0.5D;
	/**
	 * The phase-3 card's step to the ring: the Step's own track laid over the card's 50 ticks (its commit at 24 of 52 lands
	 * at 23), so he is under from 14, moved at 23 and surfaced by 27.
	 */
	public static final int CARD_UNDER = 14;
	public static final int CARD_MOVE = 23;
	public static final int CARD_SURFACED = 27;

	/** Whether the Step's tick {@code tick} (its commit at {@code commit}) has him under the dark. */
	public static boolean under(int tick, int commit) {
		return tick >= STEP_UNDER && tick <= commit + STEP_SURFACED;
	}

	/** Whether the phase-3 card's step has him under the dark at its tick {@code tick}. */
	public static boolean underInCard(int tick) {
		return tick >= CARD_UNDER && tick <= CARD_SURFACED;
	}

	/**
	 * Whether a cell centre ({@code x}, {@code z}) suits the Step's rise: inside the leash box, 4 to 6 blocks from the
	 * target ({@code tx}, {@code tz}), beside or behind them seen from him ({@code sx}, {@code sz}), and within 1.5 of
	 * a feeding figure when {@code nearFigure} is asked for ({@code figure} is the distance to the nearest one).
	 */
	public static boolean stepCell(double x, double z, double tx, double tz, double sx, double sz, double minX,
			double minZ, double maxX, double maxZ, double figure, boolean nearFigure) {
		if (x < minX || x > maxX || z < minZ || z > maxZ) return false;
		double dx = x - tx, dz = z - tz;
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance < STEP_MIN || distance > STEP_MAX) return false;
		if (nearFigure && figure > STEP_FIGURE) return false;
		double hx = sx - tx, hz = sz - tz;
		double him = Math.sqrt(hx * hx + hz * hz);
		if (him < 1.0E-6D) return true;
		return (dx * hx + dz * hz) / (distance * him) <= STEP_BESIDE_COS + 1.0E-9D;
	}
}
