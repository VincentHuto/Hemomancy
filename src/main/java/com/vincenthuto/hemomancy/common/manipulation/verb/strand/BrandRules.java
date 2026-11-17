package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

/**
 * The brand: the player holds the strand on one creature for about a second and it takes. The creature carries the
 * school's state at long duration and a brand that names the caster, and Absorption then reaches it from twice as
 * far and picks it before anything nearer. Looking away for a moment keeps the progress; longer starts it over.
 */
public final class BrandRules {
	public static final int SET_TICKS = 20;
	/** Ticks the aim may leave the creature before the progress is lost. */
	public static final int GRACE_TICKS = 5;
	/** How long a set brand lasts. */
	public static final int BRAND_TICKS = 600;
	/** A branded creature is valid for Absorption at this many times the reach. */
	public static final double ABSORPTION_REACH = 2.0D;

	private BrandRules() {
	}

	/** The progress toward setting a brand on one creature. */
	public static final class State {
		int target = -1;
		int held;
		int away;

		public int target() {
			return target;
		}

		public int held() {
			return held;
		}
	}

	/**
	 * One tick aimed at {@code targetId} (-1 for none). Returns true on the tick the brand sets; the progress then
	 * starts over, so holding on keeps refreshing the brand once a second.
	 */
	public static boolean tick(State state, int targetId) {
		return tick(state, targetId, SET_TICKS);
	}

	/** {@link #tick(State, int)} with the brand taking in {@code setTicks} (Deep Brand). */
	public static boolean tick(State state, int targetId, int setTicks) {
		if (targetId >= 0 && targetId == state.target) {
			state.away = 0;
			if (++state.held >= Math.max(1, setTicks)) {
				state.held = 0;
				return true;
			}
			return false;
		}
		if (targetId >= 0) {
			state.target = targetId;
			state.held = 1;
			state.away = 0;
			return false;
		}
		if (++state.away > GRACE_TICKS) {
			state.target = -1;
			state.held = 0;
		}
		return false;
	}

	/** Progress toward the set, from 0 to 1. */
	public static double progress(State state) {
		return progress(state, SET_TICKS);
	}

	/** Progress toward a set that takes {@code setTicks}, from 0 to 1. */
	public static double progress(State state, int setTicks) {
		return Math.max(0.0D, Math.min(1.0D, state.held / (double) Math.max(1, setTicks)));
	}

	/** Whether a brand set at {@code setAt} still holds at {@code now}. */
	public static boolean holds(long until, long now) {
		return now < until;
	}
}
