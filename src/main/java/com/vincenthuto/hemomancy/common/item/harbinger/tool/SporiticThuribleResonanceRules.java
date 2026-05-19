package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public final class SporiticThuribleResonanceRules {
	public static final double MATCHING_COST_MULTIPLIER = 0.85;
	public static final double MATCHING_COOLDOWN_MULTIPLIER = 0.9;

	private SporiticThuribleResonanceRules() {
	}

	public static double costMultiplier(EnumBloodTendency resonanceTendency, EnumBloodTendency manipulationTendency) {
		return matches(resonanceTendency, manipulationTendency) ? MATCHING_COST_MULTIPLIER : 1.0;
	}

	public static double cooldownMultiplier(EnumBloodTendency resonanceTendency, EnumBloodTendency manipulationTendency) {
		return matches(resonanceTendency, manipulationTendency) ? MATCHING_COOLDOWN_MULTIPLIER : 1.0;
	}

	private static boolean matches(EnumBloodTendency resonanceTendency, EnumBloodTendency manipulationTendency) {
		return resonanceTendency != null && resonanceTendency == manipulationTendency;
	}
}
