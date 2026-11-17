package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

/**
 * The numbers of a kept creature's Vigor (core-loop Track 5). A creature in a jar or vat does not move, path or think;
 * what is left of it is Vigor, 0 to 100. Vigor falls slowly on its own and a step with each draw, and rises when the
 * vessel's small living buffer feeds it. A draw converts Vigor into Temper in the creature's schools and a little
 * blood, so a vat turns its keeper's blood into Temper at a loss, through an animal, at a rate the animal sets. At
 * zero the creature dies and leaves a husk.
 *
 * Settling is path-independent: the decay over any stretch of game time is the same whether it is worked out in one
 * step or many, and time a Sanguine Vigil tended does not count.
 */
public final class VatVigorRules {
	public static final double MAX = 100.0D;
	/** A freshly kept creature, weakened by its capture, starts here. */
	public static final double CAPTURED = 60.0D;
	/** Vigor falls one point in this many ticks of untended game time: a full creature starves in about 100 minutes. */
	public static final double DECAY_TICKS_PER_POINT = 1200.0D;
	/** The buffer feeds at most one point of Vigor this often. */
	public static final int FEED_INTERVAL = 20;
	/** A held draw pulls this often, and each pull costs this much Vigor; no draw at or under the floor. */
	public static final int DRAW_INTERVAL = 10;
	public static final double DRAW_STEP = 2.0D;
	public static final double DRAW_FLOOR = 10.0D;
	/** What one pull gives at full Vigor: Temper shared over the creature's schools, and blood. */
	public static final double TEMPER_PER_DRAW = 6.0D;
	public static final double BLOOD_PER_DRAW = 2.0D;
	/** A vial drawn from a kept creature costs it this much Vigor instead of health. */
	public static final double SAMPLE_COST = 5.0D;
	/** A Living Syringe draws a kept creature for less: a vial's 5 to the syringe's 3, as a wild draw costs 2 health to 1. */
	public static final double SYRINGE_SAMPLE_COST = 3.0D;
	/** A hostile creature rattles its glass under this, and a strong one may break out under the floor. */
	public static final double RATTLE_BELOW = 25.0D;
	public static final double BREAKOUT_BELOW = 10.0D;
	public static final float BREAKOUT_MIN_HEALTH = 20.0F;
	/** Chances a second, while under those marks. */
	public static final double RATTLE_CHANCE = 0.15D;
	public static final double BREAKOUT_CHANCE = 0.02D;
	/** Living buffers: the jar's small one and the vat's, the Cradle's 800. */
	public static final double JAR_BUFFER = 200.0D;
	public static final double VAT_BUFFER = 800.0D;
	/** Vigor bands for the "three Vigor levels" of second-level research: low, middling, high. */
	public static final double LOW_BAND = 34.0D;
	public static final double HIGH_BAND = 67.0D;

	private VatVigorRules() {
	}

	public static double bounded(double vigor) {
		return Double.isFinite(vigor) ? Math.max(0.0D, Math.min(MAX, vigor)) : 0.0D;
	}

	/**
	 * Vigor at {@code now} from {@code vigor} last settled at {@code settledAt}, decaying for every tick after
	 * {@code tendedUntil} (a Vigil's tending) and none before it.
	 */
	public static double settle(double vigor, long settledAt, long now, long tendedUntil) {
		if (now <= settledAt) return bounded(vigor);
		long from = Math.max(settledAt, Math.min(now, tendedUntil));
		long untended = Math.max(0L, now - from);
		return bounded(vigor - untended / DECAY_TICKS_PER_POINT);
	}

	/** Blood the buffer pays for one point of Vigor, by the creature's maximum health: a big body costs more to keep. */
	public static double mlPerVigor(float maxHealth) {
		return Math.max(2.0D, Math.max(0.0F, maxHealth) * 0.5D);
	}

	/** How much of a full draw a creature at {@code vigor} gives: a quarter at nothing, all of it at full. */
	public static double share(double vigor) {
		return 0.25D + 0.75D * bounded(vigor) / MAX;
	}

	/** Whether a creature at {@code vigor} can be drawn. */
	public static boolean drawable(double vigor) {
		return bounded(vigor) > DRAW_FLOOR;
	}

	/** One pull's Temper, all schools together, at {@code vigor} and the keeper's acclimation multiplier. */
	public static double temperPerDraw(double vigor, double acclimation) {
		return TEMPER_PER_DRAW * share(vigor) * Math.max(0.0D, acclimation);
	}

	/** One pull's blood, at {@code vigor} and the keeper's acclimation multiplier. */
	public static double bloodPerDraw(double vigor, double acclimation) {
		return BLOOD_PER_DRAW * share(vigor) * Math.max(0.0D, acclimation);
	}

	public static boolean rattles(double vigor, boolean hostile) {
		return hostile && bounded(vigor) < RATTLE_BELOW;
	}

	public static boolean mayBreakOut(double vigor, boolean hostile, float maxHealth) {
		return hostile && bounded(vigor) < BREAKOUT_BELOW && maxHealth >= BREAKOUT_MIN_HEALTH;
	}

	/** The research band of a Vigor level: 0 low, 1 middling, 2 high. */
	public static int band(double vigor) {
		double v = bounded(vigor);
		return v < LOW_BAND ? 0 : v < HIGH_BAND ? 1 : 2;
	}

	/** The word a vessel shows for a creature at {@code vigor}: thriving, wan or failing. */
	public static String word(double vigor) {
		if (vigor >= HIGH_BAND) return "thriving";
		if (vigor >= RATTLE_BELOW) return "wan";
		return "failing";
	}
}
