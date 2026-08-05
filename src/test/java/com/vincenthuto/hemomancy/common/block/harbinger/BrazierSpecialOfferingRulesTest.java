package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BrazierSpecialOfferingRulesTest {
	@Test
	void successfulSpecialOfferingsInALitBrazierSelectTheirDistinctFeedback() {
		assertEquals(BrazierSpecialOfferingRules.Effect.SCAR_TENDRILS,
				BrazierSpecialOfferingRules.select(true, true, true, false, false));
		assertEquals(BrazierSpecialOfferingRules.Effect.GRAFT_LIGHTNING,
				BrazierSpecialOfferingRules.select(true, true, false, true, false));
		assertEquals(BrazierSpecialOfferingRules.Effect.MEMORY_GLOW,
				BrazierSpecialOfferingRules.select(true, true, false, false, true));
	}

	@Test
	void feedbackRequiresBothFireAndASuccessfulSpecialInteraction() {
		assertEquals(BrazierSpecialOfferingRules.Effect.NONE,
				BrazierSpecialOfferingRules.select(false, true, true, false, false));
		assertEquals(BrazierSpecialOfferingRules.Effect.NONE,
				BrazierSpecialOfferingRules.select(true, false, false, true, false));
		assertEquals(BrazierSpecialOfferingRules.Effect.NONE,
				BrazierSpecialOfferingRules.select(true, true, false, false, false));
	}

	@Test
	void graftDataWinsIfAnItemAlsoBelongsToABroaderSpecialCategory() {
		assertEquals(BrazierSpecialOfferingRules.Effect.GRAFT_LIGHTNING,
				BrazierSpecialOfferingRules.select(true, true, true, true, true));
	}

	@Test
	void encodedBloodMemoriesSelectMemoryFeedback() {
		assertTrue(BrazierSpecialOfferingEffects.isMemoryItemType(BloodMemoryItem.class));
	}

	@Test
	void ignitionTriggersFeedbackForAnOfferingAlreadyInTheBrazier() {
		assertTrue(BrazierSpecialOfferingRules.shouldEmitOnIgnition(false, true, true));
		assertFalse(BrazierSpecialOfferingRules.shouldEmitOnIgnition(false, true, false));
		assertFalse(BrazierSpecialOfferingRules.shouldEmitOnIgnition(true, true, true));
	}

	@Test
	void eachSpecialOfferingHasARecurringFeedbackCadence() {
		assertTrue(BrazierSpecialOfferingRules.shouldEmitPersistent(
				BrazierSpecialOfferingRules.Effect.MEMORY_GLOW, 12L));
		assertFalse(BrazierSpecialOfferingRules.shouldEmitPersistent(
				BrazierSpecialOfferingRules.Effect.MEMORY_GLOW, 13L));
		assertTrue(BrazierSpecialOfferingRules.shouldEmitPersistent(
				BrazierSpecialOfferingRules.Effect.GRAFT_LIGHTNING, 16L));
		assertTrue(BrazierSpecialOfferingRules.shouldEmitPersistent(
				BrazierSpecialOfferingRules.Effect.SCAR_TENDRILS, 20L));
		assertFalse(BrazierSpecialOfferingRules.shouldEmitPersistent(
				BrazierSpecialOfferingRules.Effect.NONE, 0L));
	}
}
