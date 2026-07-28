package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;

public final class CardinalRiteBloodlettingRules {
	private static final int OFFERING_ML = 50;
	private static final float HEALTH_COST = 2.0F;
	private static final float MINIMUM_HEALTH = 2.0F;

	private CardinalRiteBloodlettingRules() {
	}

	public static boolean canTraceSigil(int degree, CardinalRitePhase phase) {
		return degree == 1 && (phase == CardinalRitePhase.INSCRIPTION
				|| phase == CardinalRitePhase.ORDEAL
				|| phase == CardinalRitePhase.STILL_INTERVAL);
	}

	public static boolean canRepairBoundaryDirectly(int degree) {
		return degree == 1;
	}

	public static int offeringMl() {
		return OFFERING_ML;
	}

	public static boolean canOffer(float currentHealth) {
		return currentHealth > MINIMUM_HEALTH;
	}

	public static float healthAfterStroke(float currentHealth) {
		return Math.max(MINIMUM_HEALTH, currentHealth - HEALTH_COST);
	}
}
