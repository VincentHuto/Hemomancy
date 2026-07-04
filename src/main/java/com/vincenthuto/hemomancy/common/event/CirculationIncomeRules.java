package com.vincenthuto.hemomancy.common.event;

/**
 * Pure math for the shared passive blood-income bandwidth ("one blood, one
 * circulation, finite flow"). All passive income channels — armor set regen,
 * mask trickles, morphling siphons, cradle redistribution — draw from a single
 * rolling window so stacking sources yields diminishing returns instead of
 * additive snowballing.
 *
 * <p>Deliberately exempt (the cap governs the world lending to the player,
 * not the player's own will): skill regen (Sanguine Surge, Last Wind),
 * manipulation-generated blood (Vital Reservoir, Exsanguinate), and
 * consumables.</p>
 *
 * <p>No Minecraft imports on purpose — see SilentArchonArmorRules for the
 * pure-rules + adapter pattern this follows. The Minecraft-facing adapter is
 * CirculationIncomeHelper.</p>
 */
public final class CirculationIncomeRules {

	public static final int DEFAULT_WINDOW_TICKS = 20;
	public static final double DEFAULT_BASE_BANDWIDTH = 6.0D;
	public static final double DEFAULT_BANDWIDTH_PER_DEGREE = 1.5D;
	public static final double DEFAULT_BANDWIDTH_PER_CAPACITY_POINT = 0.002D;

	private CirculationIncomeRules() {
	}

	/** Outcome of a grant request: what was allowed plus the new window state. */
	public record Grant(double granted, long windowStart, double usedInWindow) {
	}

	/**
	 * Bandwidth available per window. Generous enough that any single source
	 * never clips; only stacked sources contend.
	 */
	public static double bandwidthPerWindow(double base, double perDegree, int degree, double perCapacityPoint,
			double capacityBonus) {
		double bandwidth = base + perDegree * Math.max(0, degree) + perCapacityPoint * Math.max(0.0D, capacityBonus);
		return Math.max(0.0D, bandwidth);
	}

	/**
	 * Request income against the rolling window. A stale window resets; the
	 * grant is clamped to remaining bandwidth and never negative.
	 */
	public static Grant grant(double requested, long now, int windowTicks, long windowStart, double usedInWindow,
			double bandwidth) {
		long start = windowStart;
		double used = usedInWindow;
		if (windowTicks <= 0) {
			windowTicks = DEFAULT_WINDOW_TICKS;
		}
		if (now < start || now >= start + windowTicks) {
			start = now;
			used = 0.0D;
		}
		double remaining = Math.max(0.0D, bandwidth - used);
		double granted = Math.max(0.0D, Math.min(requested, remaining));
		return new Grant(granted, start, used + granted);
	}
}
