package com.vincenthuto.hemomancy.common.manipulation.ductilis;

public final class ThreadRipperRules {
	public static final float UNRAVEL_HEALTH_RATIO = 0.30F;

	private ThreadRipperRules() {
	}

	public static Outcome outcome(boolean captive, boolean tethered, boolean protectedBody, float healthRatio) {
		return outcome(captive, tethered, false, protectedBody, healthRatio);
	}

	/**
	 * @param foreignSummon a hostile summoned or owned body that is not a puppet: a Vex, or a creature owned by
	 *                      someone who is neither the caster nor an ally. Its tie unravels outright.
	 */
	public static Outcome outcome(boolean captive, boolean tethered, boolean foreignSummon, boolean protectedBody,
			float healthRatio) {
		if (protectedBody) return Outcome.NONE;
		if (!tethered) return foreignSummon ? Outcome.UNRAVEL : Outcome.NONE;
		if (captive) return Outcome.SEVER_CAPTIVE;
		return healthRatio <= UNRAVEL_HEALTH_RATIO ? Outcome.UNRAVEL : Outcome.DISRUPT;
	}

	public enum Outcome {
		NONE, DISRUPT, UNRAVEL, SEVER_CAPTIVE
	}
}
