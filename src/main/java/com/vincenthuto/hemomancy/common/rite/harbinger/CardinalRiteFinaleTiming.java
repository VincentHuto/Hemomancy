package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

public final class CardinalRiteFinaleTiming {
	public static final int GROWTH_TICKS = 40;
	public static final int MERGE_TICKS = 40;
	public static final int TOTAL_TICKS = GROWTH_TICKS + MERGE_TICKS;
	public static final int OFFERING_DWELL_TICKS = 10;
	public static final double DAEMON_TRAVEL_BLOCKS_PER_TICK = 0.24D;
	public static final float PROCESSION_SCALE = 0.35F;
	/** Ticks between successive offerings being drawn out of their braziers. */
	public static final int OFFERING_DRAW_INTERVAL_TICKS = 12;
	/** Ticks an offering takes to travel along the floor into the daemon. */
	public static final int OFFERING_DRAW_TRAVEL_TICKS = 24;
	/** How much each arrived offering swells the daemon during the procession. */
	public static final float OFFERING_GROWTH_PER_ARRIVAL = 0.12F;
	public static final double PROCESSION_HEIGHT =
			CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * PROCESSION_SCALE;

	private CardinalRiteFinaleTiming() {
	}

	/** Offerings whose draw should have begun by this procession tick (draws start at tick 1). */
	public static int offeringsDue(int phaseTicks, int offerings) {
		if (phaseTicks < 1 || offerings <= 0) return 0;
		return Math.min(offerings, (phaseTicks - 1) / OFFERING_DRAW_INTERVAL_TICKS + 1);
	}

	/** Offerings that have reached the daemon by this procession tick. */
	public static int offeringsArrived(int phaseTicks, int offerings) {
		return offeringsDue(phaseTicks - OFFERING_DRAW_TRAVEL_TICKS, offerings);
	}

	/** The procession ends once the last drawn offering has arrived. */
	public static boolean processionFinished(int phaseTicks, int offerings) {
		if (offerings <= 0) return true;
		return phaseTicks >= 1 + (offerings - 1) * OFFERING_DRAW_INTERVAL_TICKS + OFFERING_DRAW_TRAVEL_TICKS;
	}

	public static double growthProgress(int phaseTicks) {
		return HemoEasing.clamp01(phaseTicks / (double) GROWTH_TICKS);
	}

	public static double mergeProgress(int phaseTicks) {
		return HemoEasing.clamp01((phaseTicks - GROWTH_TICKS) / (double) MERGE_TICKS);
	}

	public static double offeringParticleStrength(int phaseTicks) {
		return 1.0D - growthProgress(phaseTicks);
	}

	public static boolean isImpactTick(int phaseTicks) {
		return phaseTicks == TOTAL_TICKS;
	}

	/**
	 * The daemon's size: one size from the moment it forms over the staff, swelling only as each offering arrives,
	 * and kept through the culmination until it merges into the caster.
	 */
	public static float daemonScale(int arrivedOfferings) {
		return PROCESSION_SCALE * (1.0F + OFFERING_GROWTH_PER_ARRIVAL * Math.max(0, arrivedOfferings));
	}
}
