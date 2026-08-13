package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class ToggleableAbsorptionRules {
	private static final double SHARED_FRACTION = 0.25D;
	private ToggleableAbsorptionRules() {}

	public static int targetCount(boolean distributed, int cap, int available) {
		return Math.max(0, Math.min(distributed ? Math.max(1, cap) : 1, available));
	}

	public static double damagePerTarget(boolean distributed, double focusedDamage, int targets) {
		return distributed ? focusedDamage / Math.max(1, targets) : focusedDamage;
	}

	public static double clampDamageForMercy(boolean mercy, boolean authoredSpecialTarget,
			double health, double requested) {
		if (!mercy || authoredSpecialTarget) return Math.max(0.0D, requested);
		return Math.max(0.0D, Math.min(requested, health - 1.0D));
	}

	public static double personalBlood(boolean sharedSiphonActive, double absorbed) {
		double safeAmount = Math.max(0.0D, absorbed);
		return sharedSiphonActive ? safeAmount * (1.0D - SHARED_FRACTION) : safeAmount;
	}

	public static double sharedBlood(boolean sharedSiphonActive, double absorbed) {
		return sharedSiphonActive ? Math.max(0.0D, absorbed) * SHARED_FRACTION : 0.0D;
	}
}
