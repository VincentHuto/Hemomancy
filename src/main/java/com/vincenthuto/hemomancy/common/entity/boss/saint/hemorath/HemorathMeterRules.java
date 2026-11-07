package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;

/**
 * Hemorath's absorbed-blood meter as pure numbers (design section 4): its maximum by challenger count, the leak that
 * grows with the square of how full he is, the share of the leak that runs to the wells, the phase-2 intake, the
 * thresholds that change his body, and the three meter states. JVM-tested.
 */
public final class HemorathMeterRules {
	/** The solo maximum, in blood points; {@code OVERLOAD_THRESHOLD} of the old fight. */
	public static final float BASE_MAX = 3000.0F;
	/** leak per tick = M x (LEAK_BASE + LEAK_CURVE x f^2) x the factors. */
	public static final float LEAK_BASE = 0.0004F;
	public static final float LEAK_CURVE = 0.0040F;
	public static final float PHASE_2_LEAK = 1.3F;
	/** Flammeus Searing cauterises his vessels. */
	public static final float SEARING_LEAK = 0.5F;
	/** The Downfall deed's fresh start: half the leak for the first FRESH_TICKS of the fight (resolution D-1). */
	public static final float FRESH_LEAK = 0.5F;
	public static final int FRESH_TICKS = 1200;
	/** What the leak credits to the two nearest wells, by phase. */
	public static final float WELL_SHARE_1 = 0.25F;
	public static final float WELL_SHARE_2 = 0.40F;
	/** All intake in phase 2 (the old OVERLOAD_PHASE_2_MULTIPLIER). */
	public static final float PHASE_2_INTAKE = 1.35F;

	/** The state thresholds, fractions of the maximum. */
	public static final float FILLING_FROM = 0.35F;
	public static final float BRIMMING_FROM = 0.70F;
	/** Above this he tries to vent, and a Brimming recovery that ends here opens Strained. */
	public static final float VENT_FROM = 0.85F;
	public static final float FULL = 1.0F;
	/** The meter's crossing points, in {@code SaintMeter.Rule.thresholds} order. */
	public static final int THRESHOLD_FILLING = 0, THRESHOLD_BRIMMING = 1, THRESHOLD_VENT = 2, THRESHOLD_FULL = 3;

	/** What the phase-1 overload leaves him with, and what an ordinary tide dumps. */
	public static final float AFTER_EMPTIES = 0.30F;
	public static final float TIDE_DUMP = 0.40F;
	/** The Downfall deed's start (resolution D-1). */
	public static final float DOWNFALL_START = 0.25F;
	/** A RIB_BURST vents this share of the meter. */
	public static final float RIB_BURST_VENT = 0.12F;

	/** His body, speed and hardness by meter (design section 3, "Meter states"). */
	public enum State {
		SUNKEN(0.70F, 0.20D, 24, 8.0F, 14),
		FILLING(1.00F, 0.15D, 30, 6.0F, 18),
		BRIMMING(1.30F, 0.10D, 38, 4.0F, 24);

		/** Damage he takes. */
		public final float damageTaken;
		/** Walking speed, blocks a tick. */
		public final double speed;
		/** The stalk's gait cycle in ticks. */
		public final int gait;
		/** Degrees a tick he may turn. */
		public final float turnRate;
		public final int decisionDelay;

		State(float damageTaken, double speed, int gait, float turnRate, int decisionDelay) {
			this.damageTaken = damageTaken;
			this.speed = speed;
			this.gait = gait;
			this.turnRate = turnRate;
			this.decisionDelay = decisionDelay;
		}
	}

	private HemorathMeterRules() {
	}

	/** {@code 3000 x (1 + 0.5 (n - 1))}: 3000, 4500, 6000, 7500 (resolution B-5). */
	public static float max(int challengers) {
		return BASE_MAX * SaintKit.scale(challengers);
	}

	public static float[] thresholds() {
		return new float[]{FILLING_FROM, BRIMMING_FROM, VENT_FROM, FULL};
	}

	public static State state(float fraction) {
		if (fraction >= BRIMMING_FROM) return State.BRIMMING;
		if (fraction >= FILLING_FROM) return State.FILLING;
		return State.SUNKEN;
	}

	/**
	 * Blood lost this tick. {@code gorged} is true while he digests (the Gorged opening, the Last Thirst) and stops it;
	 * {@code emptying} is the phase-1 overload's hold until the tide, which also stops it.
	 */
	public static float leak(float max, float fraction, boolean phase2, boolean searing, boolean gorged, boolean fresh) {
		if (gorged) return 0.0F;
		float f = Math.max(0.0F, Math.min(1.0F, fraction));
		float leak = max * (LEAK_BASE + LEAK_CURVE * f * f);
		if (phase2) leak *= PHASE_2_LEAK;
		if (searing) leak *= SEARING_LEAK;
		if (fresh) leak *= FRESH_LEAK;
		return leak;
	}

	/** The share of each leaked point credited to the wells (half to each of the two nearest). */
	public static float wellShare(boolean phase2) {
		return phase2 ? WELL_SHARE_2 : WELL_SHARE_1;
	}

	/** Every intake, raised in phase 2. */
	public static float intake(float amount, boolean phase2) {
		return phase2 ? amount * PHASE_2_INTAKE : amount;
	}

	/** Whether the Downfall deed's half leak still runs at {@code fightTicks} into the fight. */
	public static boolean fresh(boolean downfall, int fightTicks) {
		return downfall && fightTicks < FRESH_TICKS;
	}
}
