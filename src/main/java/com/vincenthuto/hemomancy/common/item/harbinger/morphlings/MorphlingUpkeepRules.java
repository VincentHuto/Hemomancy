package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingUpkeepRules {
	private MorphlingUpkeepRules() {
	}

	public static double upkeepAmount(boolean passiveDrainEnabled, double baseDrainRate, int bloodCost) {
		if (!passiveDrainEnabled || baseDrainRate <= 0.0D) {
			return 0.0D;
		}
		return baseDrainRate * (1.0D + Math.max(0, bloodCost) / 100.0D);
	}

	public static boolean shouldRunEquippedTick(boolean hasMorphling, boolean intervalElapsed) {
		return hasMorphling && intervalElapsed;
	}
}
