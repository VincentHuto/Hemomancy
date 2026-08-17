package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingUpkeepRules {
	public static final double PUPPET_INTERFERENCE_MULTIPLIER = 1.25D;

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

	public static double withPuppetInterference(double upkeep, boolean activeOwnedTether) {
		return activeOwnedTether ? Math.max(0.0D, upkeep) * PUPPET_INTERFERENCE_MULTIPLIER : upkeep;
	}

	public static double bondingCredit(double actualDrain, double ordinaryUpkeep) {
		return Math.min(Math.max(0.0D, actualDrain), Math.max(0.0D, ordinaryUpkeep));
	}
}
