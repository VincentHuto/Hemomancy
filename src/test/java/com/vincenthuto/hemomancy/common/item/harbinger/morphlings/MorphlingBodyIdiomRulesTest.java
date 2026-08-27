package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingBodyIdiomRulesTest {
	@Test
	void coldBloodedProgressesFromSlowerFreezingToEnvironmentalImmunity() {
		assertEquals(0, WinterShroudResilienceRules.coldBloodedTier(1));
		assertEquals(1, WinterShroudResilienceRules.coldBloodedTier(2));
		assertEquals(2, WinterShroudResilienceRules.coldBloodedTier(3));
		assertEquals(3, WinterShroudResilienceRules.coldBloodedTier(4));

		assertEquals(20, WinterShroudResilienceRules.retainedEnvironmentalFreezeTicks(1, 20, 2));
		assertEquals(19, WinterShroudResilienceRules.retainedEnvironmentalFreezeTicks(2, 20, 2));
		assertEquals(0, WinterShroudResilienceRules.retainedEnvironmentalFreezeTicks(3, 20, 1));
		assertEquals(0, WinterShroudResilienceRules.retainedEnvironmentalFreezeTicks(4, 20, 1));
		assertFalse(WinterShroudResilienceRules.canTraversePowderSnow(2));
		assertTrue(WinterShroudResilienceRules.canTraversePowderSnow(3));
	}

	@Test
	void coldBloodedNeverErasesMagicalCold() {
		assertEquals(0.75F, WinterShroudResilienceRules.coldDamageMultiplier(2, true, true));
		assertEquals(0.50F, WinterShroudResilienceRules.coldDamageMultiplier(3, true, true));
		assertEquals(0.0F, WinterShroudResilienceRules.coldDamageMultiplier(4, true, true));
		assertEquals(0.25F, WinterShroudResilienceRules.coldDamageMultiplier(4, false, true));
		assertEquals(1.0F, WinterShroudResilienceRules.coldDamageMultiplier(4, false, false));
	}

	@Test
	void hotheadedTradesHeatForPowerAndExhaustion() {
		assertEquals(EmberfangHeatRules.TEMPERATE, EmberfangHeatRules.environmentLevel(0.9F, false, false, false));
		assertEquals(EmberfangHeatRules.WARM, EmberfangHeatRules.environmentLevel(1.0F, false, false, false));
		assertEquals(EmberfangHeatRules.HOT, EmberfangHeatRules.environmentLevel(1.5F, false, false, false));
		assertEquals(EmberfangHeatRules.EXTREME, EmberfangHeatRules.environmentLevel(2.0F, false, false, false));
		assertEquals(EmberfangHeatRules.EXTREME, EmberfangHeatRules.environmentLevel(0.8F, true, false, false));

		assertEquals(0.0D, EmberfangHeatRules.benefit(EmberfangHeatRules.TEMPERATE));
		assertEquals(0.05D, EmberfangHeatRules.benefit(EmberfangHeatRules.WARM));
		assertEquals(0.10D, EmberfangHeatRules.benefit(EmberfangHeatRules.HOT));
		assertEquals(0.15D, EmberfangHeatRules.benefit(EmberfangHeatRules.EXTREME));
		assertEquals(1.10F, EmberfangHeatRules.exhaustionMultiplier(EmberfangHeatRules.WARM));
		assertEquals(1.25F, EmberfangHeatRules.exhaustionMultiplier(EmberfangHeatRules.HOT));
		assertEquals(1.50F, EmberfangHeatRules.exhaustionMultiplier(EmberfangHeatRules.EXTREME));
		assertEquals(1.0F, EmberfangHeatRules.incomingDamageMultiplier(EmberfangHeatRules.WARM));
		assertEquals(1.05F, EmberfangHeatRules.incomingDamageMultiplier(EmberfangHeatRules.HOT));
		assertEquals(1.10F, EmberfangHeatRules.incomingDamageMultiplier(EmberfangHeatRules.EXTREME));
	}
}
