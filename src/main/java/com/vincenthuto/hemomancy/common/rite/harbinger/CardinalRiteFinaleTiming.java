package com.vincenthuto.hemomancy.common.rite.harbinger;

public final class CardinalRiteFinaleTiming {
	public static final int GROWTH_TICKS = 40;
	public static final int MERGE_TICKS = 40;
	public static final int TOTAL_TICKS = GROWTH_TICKS + MERGE_TICKS;
	public static final int OFFERING_DWELL_TICKS = 10;
	public static final double DAEMON_TRAVEL_BLOCKS_PER_TICK = 0.24D;
	public static final float PROCESSION_SCALE = 0.35F;
	public static final double PROCESSION_HEIGHT =
			CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * PROCESSION_SCALE;

	private CardinalRiteFinaleTiming() {
	}

	public static double growthProgress(int phaseTicks) {
		return clamp(phaseTicks / (double) GROWTH_TICKS);
	}

	public static double mergeProgress(int phaseTicks) {
		return clamp((phaseTicks - GROWTH_TICKS) / (double) MERGE_TICKS);
	}

	public static double offeringParticleStrength(int phaseTicks) {
		return 1.0D - growthProgress(phaseTicks);
	}

	public static boolean isImpactTick(int phaseTicks) {
		return phaseTicks == TOTAL_TICKS;
	}

	public static double preProcessionHeight(double requestedHeight) {
		return Math.min(Math.max(0.0D, requestedHeight), PROCESSION_HEIGHT);
	}

	private static double clamp(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}
}
