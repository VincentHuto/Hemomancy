package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;

/**
 * Who may absorb the Ringmaster at his kneel (2026-11-18), as pure rules: the finale's owner, once the kneel has settled,
 * with active blood and Succession's requirements (full acclimation, the attention act and the three Succession acts,
 * not Unstained). Anyone who brought him down may still kill him; this is only the other way out.
 */
public final class RingmasterAbsorptionRules {
	/** The progress a channel must fill to take his place. */
	public static final int ABSORPTION_REQUIRED = 100;

	private RingmasterAbsorptionRules() {
	}

	public static boolean canAbsorb(boolean kneeling, int kneelTicks, boolean owner, boolean bloodActive, int acclimation,
			int challenges, boolean unstained) {
		return kneeling && kneelTicks >= FinaleRules.KNEEL_SETTLE_TICKS && owner && bloodActive
				&& CircusRouteRules.mayAbsorb(acclimation, challenges, unstained);
	}

	/** Whether the player is fit to hold the whip at all, whatever the kneel's state: what his refusal answers. */
	public static boolean fit(boolean bloodActive, int acclimation, int challenges, boolean unstained) {
		return bloodActive && CircusRouteRules.mayAbsorb(acclimation, challenges, unstained);
	}

	public static float advance(float progress, float add) {
		return Math.min(ABSORPTION_REQUIRED, progress + Math.max(0.0F, add));
	}

	public static boolean complete(float progress) {
		return progress >= ABSORPTION_REQUIRED;
	}
}
