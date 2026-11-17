package com.vincenthuto.hemomancy.common.world.chum;

/**
 * Chum (core-loop Track 7), the pure numbers. Blood projected into water makes a pool: its radius grows with the
 * blood in it, from {@link #MIN_RADIUS} to {@link #MAX_RADIUS}; its strength is the share of {@link #FULL_ML} it holds.
 * It fades by half every {@link #HALF_LIFE_TICKS} (45 seconds) in still water and four times as fast in flowing water or rain, and
 * is gone below {@link #GONE_ML}. Inside it a fishing hook is lured faster and a catch can be a Hemomancy one.
 */
public final class ChumRules {
	/** Blood a bare hand pours into the water a tick. */
	public static final double ML_PER_TICK = 2.0D;
	public static final double MIN_RADIUS = 3.0D;
	public static final double MAX_RADIUS = 12.0D;
	/** How fast the radius grows with the blood: {@code MIN + RADIUS_PER_ROOT_ML * sqrt(mL)}. */
	private static final double RADIUS_PER_ROOT_ML = 0.45D;
	/** A pool holding this much is at full strength. */
	public static final double FULL_ML = 400.0D;
	/** The most one pool holds; more blood only keeps it full. */
	public static final double MAX_ML = 800.0D;
	/** A pool fades out below this: low enough that the first tick's pour survives to the next. */
	public static final double GONE_ML = 1.0D;
	public static final long HALF_LIFE_TICKS = 900L;
	/** Flowing water and rain carry blood away this many times faster. */
	public static final int FAST_DISPERSAL = 4;

	// ── Lure ──
	/** Ticks the hook never lures faster than, so a fish still has to come to it. */
	private static final int MIN_APPROACH_TICKS = 20;
	private static final int BASE_EXTRA_LURE = 1;
	private static final int EXTRA_LURE_AT_FULL = 4;

	// ── Catch ──
	private static final double BASE_CATCH = 0.04D;
	private static final double CATCH_AT_FULL = 0.21D;
	/** Near the waters its catches come from, a pool draws them much more often. */
	public static final double HOME_WATERS_MULTIPLIER = 3.0D;
	private static final double MAX_CATCH = 0.75D;

	// ── Drawing a creature ──
	/** A pool this strong in water this deep may draw a creature to it. */
	public static final double DRAW_STRENGTH = 0.5D;
	public static final int DRAW_DEPTH = 3;
	public static final float DRAW_CHANCE = 0.15F;
	public static final int DRAW_INTERVAL_TICKS = 200;
	public static final int DRAW_CAP = 3;

	private ChumRules() {
	}

	/** The radius of a pool holding {@code ml}. */
	public static double radius(double ml) {
		if (ml <= 0.0D) return 0.0D;
		return Math.min(MAX_RADIUS, MIN_RADIUS + RADIUS_PER_ROOT_ML * Math.sqrt(ml));
	}

	/** A pool's strength, 0 to 1. */
	public static double strength(double ml) {
		return Math.max(0.0D, Math.min(1.0D, ml / FULL_ML));
	}

	/** What a pool holding {@code ml} holds {@code ticks} later. */
	public static double fade(double ml, long ticks, boolean fast) {
		if (ml <= 0.0D || ticks <= 0L) return Math.max(0.0D, ml);
		double halfLives = ticks * (fast ? FAST_DISPERSAL : 1) / (double) HALF_LIFE_TICKS;
		double left = ml * Math.pow(0.5D, halfLives);
		return left < GONE_ML ? 0.0D : left;
	}

	/** A pool holding {@code ml} after {@code added} more is poured in. */
	public static double pour(double ml, double added) {
		return Math.min(MAX_ML, Math.max(0.0D, ml) + Math.max(0.0D, added));
	}

	/** Extra ticks a hook's lure counts down each tick inside a pool of {@code strength}. */
	public static int extraLurePerTick(double strength) {
		return BASE_EXTRA_LURE + (int) Math.round(EXTRA_LURE_AT_FULL * Math.max(0.0D, Math.min(1.0D, strength)));
	}

	/** A hook's lure countdown after a tick's extra pull, never below the approach a fish still needs. */
	public static int adjustedLureTime(int timeUntilLured, double strength) {
		if (timeUntilLured <= MIN_APPROACH_TICKS || strength <= 0.0D) return timeUntilLured;
		return Math.max(MIN_APPROACH_TICKS, timeUntilLured - extraLurePerTick(strength));
	}

	/** The chance a catch inside a pool of {@code strength} is one of the pool's own. */
	public static double catchChance(double strength, boolean homeWaters) {
		if (strength <= 0.0D) return 0.0D;
		double chance = BASE_CATCH + CATCH_AT_FULL * Math.min(1.0D, strength);
		return Math.min(MAX_CATCH, homeWaters ? chance * HOME_WATERS_MULTIPLIER : chance);
	}

	/** Whether a pool may draw a creature now: strong enough and deep enough. */
	public static boolean mayDraw(double strength, int depth) {
		return strength >= DRAW_STRENGTH && depth >= DRAW_DEPTH;
	}

	/** Where a pool's centre moves when {@code added} mL is poured at {@code at}: weighted by the blood. */
	public static double blend(double centre, double ml, double at, double added) {
		double total = Math.max(0.0D, ml) + Math.max(0.0D, added);
		return total <= 0.0D ? at : (centre * Math.max(0.0D, ml) + at * Math.max(0.0D, added)) / total;
	}
}
