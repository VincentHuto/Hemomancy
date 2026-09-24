package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

/**
 * Pure movement constants and mood timing for the Myelin Borer.
 */
public final class MyelinBorerMovementRules {
	/** Lets a gripping Borer step over bundle knots and anchor blobs. */
	public static final float CLIMB_STEP_HEIGHT = 1.0F;
	/** Thirty seconds at twenty ticks per second. */
	public static final int AGITATION_TICKS = 600;
	private MyelinBorerMovementRules() {
	}

	public static int decayAgitation(int agitation) {
		return agitation <= 0 ? 0 : agitation - 1;
	}

	public static boolean isAgitated(int agitation) {
		return agitation > 0;
	}

	/** A fresh provocation restarts the full timer rather than stacking. */
	public static int raiseAgitation(int current) {
		return AGITATION_TICKS;
	}
}
