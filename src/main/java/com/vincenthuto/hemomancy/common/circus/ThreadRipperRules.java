package com.vincenthuto.hemomancy.common.circus;

public final class ThreadRipperRules {
	public static final float UNRAVEL_HEALTH_RATIO = 0.30F;

	private ThreadRipperRules() {
	}

	public static Outcome outcome(boolean captive, boolean tethered, boolean protectedBody, float healthRatio) {
		if (protectedBody || !tethered) return Outcome.NONE;
		if (captive) return Outcome.SEVER_CAPTIVE;
		return healthRatio <= UNRAVEL_HEALTH_RATIO ? Outcome.UNRAVEL : Outcome.DISRUPT;
	}

	public enum Outcome {
		NONE, DISRUPT, UNRAVEL, SEVER_CAPTIVE
	}
}
