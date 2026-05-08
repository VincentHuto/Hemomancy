package com.vincenthuto.hemomancy.common.item.harbinger;

public final class CheapBloodInfusionRules {
	public static final int DURATION_TICKS = 20 * 60 * 3;
	public static final int COOLDOWN_TICKS = 60;
	public static final int MAX_AMPLIFIER = 3;
	private static final double COST_PENALTY_PER_LEVEL = 0.15;
	private static final double MAX_DRUNKENNESS_COOLDOWN_MULTIPLIER = 1.25;

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

	public static double getManipulationCooldownMultiplier(int amplifier) {
		return amplifier >= MAX_AMPLIFIER ? MAX_DRUNKENNESS_COOLDOWN_MULTIPLIER : 1.0;
	}
}
