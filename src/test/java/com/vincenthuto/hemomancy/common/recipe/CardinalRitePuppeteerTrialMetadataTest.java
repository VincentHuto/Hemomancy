package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePuppeteerTrialMetadataTest {
	@Test
	void ordinaryRitesConsumeMediaButPuppeteerTrialsCanPreserveThem() {
		assertTrue(CardinalRiteMediumRules.consumeOnSuccessFromNullable(null));
		assertTrue(CardinalRiteMediumRules.consumeOnSuccessFromNullable(true));
		assertFalse(CardinalRiteMediumRules.consumeOnSuccessFromNullable(false));
		assertEquals("veinwing_vulture",
				new CardinalRiteRecipe.PuppeteerTrial("veinwing_vulture").summonName());
	}
}
