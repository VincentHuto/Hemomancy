package com.vincenthuto.hemomancy.common.manipulation.tenebris;

/**
 * Blood Eclipse's carried dome, as plain numbers. The dome opens small and grows while Projection is held; it Obscures
 * what the caster may harm inside it and hides what stands in it; the attack button ends it in a flash that hits
 * harder the wider the dome had grown.
 */
public final class EclipseDomeRules {
	/** The dome's radius as it opens and once it is full grown, in blocks. */
	public static final double MIN_RADIUS = 2.0D, MAX_RADIUS = 8.0D;
	/** Ticks of holding for a full-grown dome. */
	public static final int GROW_TICKS = 60;
	/** Blood a pulse costs before the cost ledger: 60 mL a second. */
	public static final double BLOOD_PER_PULSE = 30.0D;
	/** How long the dome's Obscured lasts on a body, refreshed each pulse while it stays inside. */
	public static final int OBSCURED_TICKS = 30;
	/** The flash's damage on a dome just opened and on one full grown, before Crimson Mastery. */
	public static final float MIN_FLASH = 3.0F, MAX_FLASH = 8.0F;
	/** How long the flash leaves a body Obscured. */
	public static final int FLASH_OBSCURED_TICKS = 100;
	/** Ticks after a flash before a new dome may open: the dark is spent. */
	public static final int SPENT_TICKS = 40;
	/** How fast the caster walks under the dome, as a share of their pace. */
	public static final double PACE = 0.8D;

	private EclipseDomeRules() {
	}

	/** How grown the dome is after {@code heldTicks} of holding, 0 to 1. */
	public static double growth(int heldTicks) {
		return Math.max(0.0D, Math.min(1.0D, heldTicks / (double) GROW_TICKS));
	}

	public static double radius(int heldTicks) {
		return MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * growth(heldTicks);
	}

	public static float flashDamage(int heldTicks) {
		return (float) (MIN_FLASH + (MAX_FLASH - MIN_FLASH) * growth(heldTicks));
	}

	/** Whether a body {@code distance} from the dome's centre is inside it. */
	public static boolean inside(double distance, int heldTicks) {
		return distance <= radius(heldTicks);
	}
}
