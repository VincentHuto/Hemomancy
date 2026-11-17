package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

/**
 * The jet: a straight strand to the first creature or block on the aim. While the aim stays on the same creature a
 * pressure counter climbs to full over {@link #FILL_TICKS}; each pulse hits harder as it climbs, and at full pressure
 * the jet pierces to the next creature behind. Looking away bleeds the pressure off quickly.
 */
public final class JetRules {
	/** The jet reaches twice the verb's reach. */
	public static final double REACH_SCALE = 2.0D;
	public static final int FILL_TICKS = 40;
	/** Pressure lost per tick while the aim is off the creature: empty from full in eight ticks. */
	public static final double BLEED_PER_TICK = 1.0D / 8.0D;
	public static final float BASE_DAMAGE = 1.5F, FULL_DAMAGE = 5.0F;
	/** A snap spends the pressure in one pulse: this much at no pressure, plus up to the full burst. */
	public static final float SNAP_BASE = 3.0F, SNAP_FULL = 9.0F;
	/** Pressure at or above which a snap also lays the school's state. */
	public static final double SNAP_STATE_PRESSURE = 0.5D;

	private JetRules() {
	}

	/** The pressure state: the creature it builds on and how full it is, from 0 to 1. */
	public static final class State {
		int target = -1;
		double pressure;

		public double pressure() {
			return pressure;
		}

		public int target() {
			return target;
		}
	}

	/** One tick: builds on {@code targetId} (a creature's entity id) or bleeds off when the aim holds none (-1). */
	public static void tick(State state, int targetId) {
		tick(state, targetId, 1.0D);
	}

	/**
	 * One tick with the pressure filling {@code quicken} times as fast (Taut Line): {@link #FILL_TICKS} over
	 * {@code quicken} ticks from empty to full. The bleed is the same.
	 */
	public static void tick(State state, int targetId, double quicken) {
		if (targetId >= 0 && targetId == state.target) {
			state.pressure = Math.min(1.0D, state.pressure + Math.max(0.1D, quicken) / FILL_TICKS);
			return;
		}
		if (targetId >= 0) {
			// Pressure belongs to one creature: a new one starts from nothing.
			state.target = targetId;
			state.pressure = 0.0D;
			return;
		}
		// Off any creature the pressure bleeds away; looking back in time keeps what is left.
		state.pressure = Math.max(0.0D, state.pressure - BLEED_PER_TICK);
		if (state.pressure <= 0.0D) state.target = -1;
	}

	/** A pulse's damage at {@code pressure}. */
	public static float pulseDamage(double pressure) {
		double p = Math.max(0.0D, Math.min(1.0D, pressure));
		return (float) (BASE_DAMAGE + (FULL_DAMAGE - BASE_DAMAGE) * p);
	}

	/** Whether the jet passes through to the creature behind. */
	public static boolean pierces(double pressure) {
		return pressure >= 1.0D;
	}

	/** A snap's one-pulse burst at {@code pressure}. */
	public static float snapDamage(double pressure) {
		double p = Math.max(0.0D, Math.min(1.0D, pressure));
		return (float) (SNAP_BASE + (SNAP_FULL - SNAP_BASE) * p);
	}

	/** Ticks of steady aim from empty to full at {@code quicken}. */
	public static int fillTicks(double quicken) {
		return (int) Math.ceil(FILL_TICKS / Math.max(0.1D, quicken) - 1.0E-9D);
	}

	/** Spends the pressure (a snap). */
	public static void spend(State state) {
		state.pressure = 0.0D;
	}
}
