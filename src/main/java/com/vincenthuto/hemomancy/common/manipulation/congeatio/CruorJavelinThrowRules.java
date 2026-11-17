package com.vincenthuto.hemomancy.common.manipulation.congeatio;

/**
 * Cruor Javelin under held Projection, as plain numbers: a javelin of frozen cruor is drawn into the hand through three
 * stages and thrown when Projection is let go. A longer draw throws a longer, faster, harder javelin. It sticks in the
 * block it lands in for a while, and its caster's Absorption takes part of its blood back.
 */
public final class CruorJavelinThrowRules {
	public static final int STAGES = 3;
	/** Ticks of drawing a stage takes, and the least draw that throws anything. */
	public static final int STAGE_TICKS = 15, MIN_DRAW_TICKS = 5;
	/** Blood a pulse costs before the cost ledger: 50 mL a second. */
	public static final double BLOOD_PER_PULSE = 25.0D;
	private static final double[] SPEED = {1.6D, 2.2D, 2.8D};
	private static final float[] DAMAGE = {4.0F, 7.0F, 10.0F};
	private static final int[] RIME = {1, 2, 3};
	private static final double[] LENGTH = {1.4D, 1.9D, 2.4D};
	/** How long a struck body's Rime lasts. */
	public static final int RIME_TICKS = 120;
	/** Ticks a javelin stays stuck in a block before it shatters. */
	public static final int STUCK_TICKS = 200;
	/** The share of the blood drawn into a javelin that Absorption takes back from it. */
	public static final double REFUND_SHARE = 0.5D;
	/** How near the look must pass a stuck javelin for Absorption to take it. */
	public static final double RECALL_AIM = 0.75D;
	/** How fast the caster walks while drawing, as a share of their pace. */
	public static final double PACE = 0.7D;

	private CruorJavelinThrowRules() {
	}

	/** The stage reached after {@code drawnTicks} of drawing, 1 to {@link #STAGES}. */
	public static int stage(int drawnTicks) {
		return Math.max(1, Math.min(STAGES, 1 + drawnTicks / STAGE_TICKS));
	}

	/** Whether a draw this long throws at all. */
	public static boolean throwable(int drawnTicks) {
		return drawnTicks >= MIN_DRAW_TICKS;
	}

	/** How far through the whole draw the hand is, 0 to 1. */
	public static double drawn(double drawnTicks) {
		return Math.max(0.0D, Math.min(1.0D, drawnTicks / (double) (STAGE_TICKS * (STAGES - 1))));
	}

	public static double speed(int stage) {
		return SPEED[index(stage)];
	}

	public static float damage(int stage) {
		return DAMAGE[index(stage)];
	}

	/** Rime levels a javelin of this stage drives in; one more into a body wet from water or rain. */
	public static int rimeLevels(int stage, boolean wet) {
		return RIME[index(stage)] + (wet ? 1 : 0);
	}

	public static double length(int stage) {
		return LENGTH[index(stage)];
	}

	/** Blood Absorption takes back from a stuck javelin that {@code paid} was drawn into. */
	public static double refund(double paid) {
		return Math.max(0.0D, paid) * REFUND_SHARE;
	}

	private static int index(int stage) {
		return Math.max(1, Math.min(STAGES, stage)) - 1;
	}
}
