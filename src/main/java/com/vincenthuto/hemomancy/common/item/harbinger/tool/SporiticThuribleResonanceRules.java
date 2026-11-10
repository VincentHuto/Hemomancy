package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

public final class SporiticThuribleResonanceRules {
	public static final double MATCHING_COST_MULTIPLIER = 0.85;
	public static final double MATCHING_COOLDOWN_MULTIPLIER = 0.9;

	private SporiticThuribleResonanceRules() {
	}

	public static double costMultiplier(EnumBloodTemper resonanceTemper, EnumBloodTemper manipulationTemper) {
		return matches(resonanceTemper, manipulationTemper) ? MATCHING_COST_MULTIPLIER : 1.0;
	}

	public static double cooldownMultiplier(EnumBloodTemper resonanceTemper, EnumBloodTemper manipulationTemper) {
		return matches(resonanceTemper, manipulationTemper) ? MATCHING_COOLDOWN_MULTIPLIER : 1.0;
	}

	private static boolean matches(EnumBloodTemper resonanceTemper, EnumBloodTemper manipulationTemper) {
		return resonanceTemper != null && resonanceTemper == manipulationTemper;
	}
}
