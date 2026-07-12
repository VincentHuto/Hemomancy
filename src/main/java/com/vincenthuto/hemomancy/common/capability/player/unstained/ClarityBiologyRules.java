package com.vincenthuto.hemomancy.common.capability.player.unstained;

/** Pure scaling rules for the physiological effects that begin with Clarity. */
public final class ClarityBiologyRules {
	private static final float MAX_POTION_AND_POISON_REDUCTION = 0.75F;
	private static final float MAX_HUNGER_REDUCTION = 0.65F;

	private ClarityBiologyRules() {
	}

	public static float potionResponseMultiplier(boolean clarityUnlocked, float clarity) {
		return diminishingMultiplier(clarityUnlocked, clarity, MAX_POTION_AND_POISON_REDUCTION);
	}

	public static float poisonDamageMultiplier(boolean clarityUnlocked, float clarity) {
		return diminishingMultiplier(clarityUnlocked, clarity, MAX_POTION_AND_POISON_REDUCTION);
	}

	public static float hungerExhaustionMultiplier(boolean clarityUnlocked, float clarity) {
		return diminishingMultiplier(clarityUnlocked, clarity, MAX_HUNGER_REDUCTION);
	}

	public static float scalePotionResponse(float amount, boolean clarityUnlocked, float clarity) {
		return amount * potionResponseMultiplier(clarityUnlocked, clarity);
	}

	public static float scalePoisonDamage(float amount, boolean clarityUnlocked, float clarity) {
		return amount * poisonDamageMultiplier(clarityUnlocked, clarity);
	}

	public static float scaleHungerExhaustion(float amount, boolean clarityUnlocked, float clarity) {
		return amount * hungerExhaustionMultiplier(clarityUnlocked, clarity);
	}

	private static float diminishingMultiplier(boolean clarityUnlocked, float clarity, float maximumReduction) {
		if (!clarityUnlocked) {
			return 1.0F;
		}
		float progress = Math.clamp(clarity, 0.0F, 100.0F) / 100.0F;
		return 1.0F - maximumReduction * progress;
	}
}
