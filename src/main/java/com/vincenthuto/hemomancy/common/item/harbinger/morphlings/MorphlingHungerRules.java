package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingHungerRules {
	public enum HungerState {
		DISABLED,
		EXEMPT,
		FED,
		HUNGRY,
		STARVING
	}

	private MorphlingHungerRules() {
	}

	public static HungerState state(boolean hungerEnabled, int maturity, boolean wildBound, long now,
			long lastFedGameTime, int fedDurationTicks) {
		if (!hungerEnabled) {
			return HungerState.DISABLED;
		}
		if (maturity < 3 || wildBound) {
			return HungerState.EXEMPT;
		}
		if (lastFedGameTime <= 0L) {
			return HungerState.HUNGRY;
		}
		int safeFedDuration = Math.max(1, fedDurationTicks);
		long elapsed = Math.max(0L, now - lastFedGameTime);
		if (elapsed <= safeFedDuration) {
			return HungerState.FED;
		}
		return elapsed > safeFedDuration * 3L ? HungerState.STARVING : HungerState.HUNGRY;
	}

	public static int adjustedAmplifier(int baseAmplifier, HungerState state) {
		return Math.max(0, baseAmplifier);
	}

	public static int capMaturityByHusbandry(int enzymeMaturity, int experienceProgress, int stageQuota) {
		if (stageQuota <= 0 || enzymeMaturity <= 1) {
			return enzymeMaturity;
		}
		int maxByExperience = 1 + Math.max(0, experienceProgress) / stageQuota;
		return Math.min(enzymeMaturity, Math.min(maxByExperience, PrimalMorphlingRules.PRIMAL_LEVEL));
	}

	public static boolean shouldApplyStarvingDrain(HungerState state) {
		return state == HungerState.STARVING;
	}
}
