package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingMetabolismRules {
	public static final int PROTECTED_FOOD_LEVEL = 6;
	private MorphlingMetabolismRules() {}

	public static Upkeep splitUpkeep(boolean enabled, boolean starving, int foodLevel,
			int maxFoodLevel, double upkeep) {
		double safe = Math.max(0.0D, upkeep);
		if (!enabled || starving || foodLevel <= PROTECTED_FOOD_LEVEL || maxFoodLevel <= 0) {
			return new Upkeep(safe, 0.0D);
		}
		return new Upkeep(safe * 0.5D, safe * 0.5D);
	}

	public static boolean suspendUpkeep(boolean dormantEnabled, boolean inCombat,
			boolean needsBondingBlood) {
		return dormantEnabled && !inCombat && !needsBondingBlood;
	}

	public record Upkeep(double blood, double hungerEquivalent) {}
}
