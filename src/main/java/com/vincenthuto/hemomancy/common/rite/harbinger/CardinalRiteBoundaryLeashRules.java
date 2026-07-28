package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Keeps caster movement freedom proportional to the rite's full rendered
 * footprint without changing the area in which the rite affects entities.
 */
public final class CardinalRiteBoundaryLeashRules {
	private static final double RING_SPACING = 2.0D;
	private static final double CASTER_LEASH_MULTIPLIER = 3.0D;

	private CardinalRiteBoundaryLeashRules() {
	}

	public static double ritualRadius(int riteSize) {
		int ringCount = Math.max(1, (riteSize - 1) / 2);
		return riteSize / 2.0D + 1.0D + (ringCount - 1) * RING_SPACING;
	}

	public static double casterLeashRadius(int riteSize) {
		return ritualRadius(riteSize) * CASTER_LEASH_MULTIPLIER;
	}
}
