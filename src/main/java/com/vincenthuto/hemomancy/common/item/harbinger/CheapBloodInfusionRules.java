package com.vincenthuto.hemomancy.common.item.harbinger;

public final class CheapBloodInfusionRules {
	public static final int DURATION_TICKS = 20 * 60 * 3;
	public static final int COOLDOWN_TICKS = 60;
	public static final int MAX_AMPLIFIER = 3;
	private static final double COST_PENALTY_PER_LEVEL = 0.15;
	private static final double MAX_DRUNKENNESS_COOLDOWN_MULTIPLIER = 1.25;
	/** NQ-01-9: the share a vessel gives at each drunkenness level the drinker already has. */
	private static final double[] YIELD_BY_LEVEL = {0.80D, 0.60D, 0.40D, 0.25D};

	private CheapBloodInfusionRules() {
	}

	public static int nextAmplifier(int currentAmplifier) {
		return Math.min(MAX_AMPLIFIER, currentAmplifier + 1);
	}

	public static double getManipulationCostMultiplier(int amplifier) {
		if (amplifier < 0) {
			return 1.0;
		}
		int clampedAmplifier = Math.min(MAX_AMPLIFIER, amplifier);
		return 1.0 + COST_PENALTY_PER_LEVEL * (clampedAmplifier + 1);
	}

	/**
	 * CL-6: a Blood Rock, Flask or Jug gives less when drunk in quick succession. Sober it gives all; each level of
	 * Blood Drunkenness already on the drinker cuts it to 80, 60, 40, then 25%. The effect lasts three minutes and each
	 * drink renews it, so three minutes without a drink resets the count.
	 */
	public static double yieldShare(int amplifier) {
		return amplifier < 0 ? 1.0D : YIELD_BY_LEVEL[Math.min(MAX_AMPLIFIER, amplifier)];
	}

	public static double getManipulationCooldownMultiplier(int amplifier) {
		return amplifier >= MAX_AMPLIFIER ? MAX_DRUNKENNESS_COOLDOWN_MULTIPLIER : 1.0;
	}
}
