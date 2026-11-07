package com.vincenthuto.hemomancy.common.effect;

/**
 * What Mind Broken takes from a Silentium (D-3, ruled 2026-11-06): manipulations cost 15% more, passive blood regen
 * runs 15% slower, and the Silent Archon vestments go quiet (incorporeality, Silent Slipping, the Severance and the
 * death refusal).
 */
public final class MindBrokenRules {
	public static final double COST_MULTIPLIER = 1.15D;
	public static final double PASSIVE_REGEN_MULTIPLIER = 0.85D;

	private MindBrokenRules() {
	}

	public static double manipulationCostMultiplier(boolean mindBroken) {
		return mindBroken ? COST_MULTIPLIER : 1.0D;
	}

	public static double passiveRegenMultiplier(boolean mindBroken) {
		return mindBroken ? PASSIVE_REGEN_MULTIPLIER : 1.0D;
	}

	/** The vestments answer a Silentium whose mind is whole. */
	public static boolean vestmentsAnswer(boolean silentiumWithFullSet, boolean mindBroken) {
		return silentiumWithFullSet && !mindBroken;
	}
}
