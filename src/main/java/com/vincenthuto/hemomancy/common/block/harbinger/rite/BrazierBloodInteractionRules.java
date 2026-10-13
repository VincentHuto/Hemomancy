package com.vincenthuto.hemomancy.common.block.harbinger.rite;

/**
 * How blood moves into and out of an Iron Brazier's bowl. Projection pours the lighting cost in over about a second
 * and the brazier lights once the bowl is full; absorbing a lit, empty-handed brazier drains it at the same pace and
 * puts it out once the bowl is dry. Amounts are in mL; the bowl holds exactly the lighting cost.
 */
public final class BrazierBloodInteractionRules {
	/** Blood poured into or drawn out of the bowl per channelled tick. */
	public static final double FILL_PER_TICK = 2.5D;
	public static final double DRAIN_PER_TICK = 2.5D;
	private static final double EPSILON = 1.0E-6D;

	private BrazierBloodInteractionRules() {
	}

	public static boolean shouldExtinguishOnAbsorption(boolean lit, boolean empty, double maxAmount) {
		return lit && empty && maxAmount > 0.0D;
	}

	public static boolean shouldDrawAbsorptionParticles(boolean lit) {
		return lit;
	}

	/** Blood one tick of projection pours: never more than the tool offers, the caster has, or the bowl has room for. */
	public static double fillStep(double pooled, double capacity, double offered, double available) {
		return Math.max(0.0D, Math.min(Math.min(FILL_PER_TICK, capacity - pooled), Math.min(offered, available)));
	}

	public static boolean isFull(double pooled, double capacity) {
		return pooled >= capacity - EPSILON;
	}

	/** Blood one tick of absorption draws back out of a lit bowl. */
	public static double drainStep(double pooled, double maxAmount) {
		return Math.max(0.0D, Math.min(DRAIN_PER_TICK, Math.min(pooled, maxAmount)));
	}

	public static boolean isDry(double pooled) {
		return pooled <= EPSILON;
	}

	/**
	 * The bowl after the fire changes without passing through the bowl: lighting by any means fills it, and any other
	 * way of putting the fire out (a finished craft, water) leaves it burned dry.
	 */
	public static double settle(boolean wasLit, boolean lit, double pooled, double capacity) {
		if (!wasLit && lit) return capacity;
		if (wasLit && !lit) return 0.0D;
		return Math.max(0.0D, Math.min(capacity, pooled));
	}

	/**
	 * A saved brazier's bowl: a lit one from before the bowl was tracked is full, and an unlit one cannot hold the
	 * full cost, since filling it lights it.
	 */
	public static double settleLoaded(boolean lit, double pooled, double capacity) {
		if (lit && isDry(pooled)) return capacity;
		if (!lit && isFull(pooled, capacity)) return 0.0D;
		return Math.max(0.0D, Math.min(capacity, pooled));
	}

	/** How full the bowl looks, 0 to 1, before the server has settled a fire that changed by other means. */
	public static float poolFraction(boolean lit, double pooled, double capacity) {
		if (capacity <= 0.0D) return 0.0F;
		if (lit && isDry(pooled)) return 1.0F;
		if (!lit && isFull(pooled, capacity)) return 0.0F;
		return (float) Math.max(0.0D, Math.min(1.0D, pooled / capacity));
	}
}
