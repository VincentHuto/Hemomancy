package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class ClarityBiologyRulesTest {
	private ClarityBiologyRulesTest() {
	}

	public static void main(String[] args) {
		assertClose("Purity alone must not alter biology", 1.0F,
				ClarityBiologyRules.potionResponseMultiplier(false, 100.0F));
		assertClose("Clarity starts without an immediate penalty", 1.0F,
				ClarityBiologyRules.potionResponseMultiplier(true, 0.0F));
		assertClose("Potion response diminishes gradually", 0.625F,
				ClarityBiologyRules.potionResponseMultiplier(true, 50.0F));
		assertClose("Full Clarity retains a quarter potion response", 0.25F,
				ClarityBiologyRules.potionResponseMultiplier(true, 100.0F));
		assertClose("Poison uses the same diminished bodily response", 0.25F,
				ClarityBiologyRules.poisonDamageMultiplier(true, 100.0F));
		assertClose("Full Clarity consumes hunger at thirty-five percent", 0.35F,
				ClarityBiologyRules.hungerExhaustionMultiplier(true, 100.0F));
		assertClose("Clarity values clamp above their canonical maximum", 0.35F,
				ClarityBiologyRules.hungerExhaustionMultiplier(true, 150.0F));
		assertClose("Scaled values retain their original units", 2.0F,
				ClarityBiologyRules.scalePotionResponse(8.0F, true, 100.0F));
	}

	private static void assertClose(String message, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(message + ": expected " + expected + " but was " + actual);
		}
	}
}
