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

	public static boolean shouldApplyStarvingDrain(HungerState state) {
		return state == HungerState.STARVING;
	}
}
