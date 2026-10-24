package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** How the Structure Spawner names and orders whole rite floors. */
final class CardinalRiteFloorListingTest {
	@Test
	void floorsAreNamedByStyleAndTier() {
		assertEquals("Working Floor (Greater)",
				EnglishText.render(CardinalRiteFloorDefinition.displayName("working", CardinalRiteType.GREATER)));
		assertEquals("Threshold Floor (Minor)",
				EnglishText.render(CardinalRiteFloorDefinition.displayName("threshold", CardinalRiteType.MINOR)));
		assertEquals("Pale Floor (Grand)",
				EnglishText.render(CardinalRiteFloorDefinition.displayName("pale", CardinalRiteType.GRAND)),
				"a datapack style without a key shows its id capitalised");
		assertEquals("Greater (7x7)", EnglishText.render(CardinalRiteFloorDefinition.tierWithSize(CardinalRiteType.GREATER)));
	}

	@Test
	void floorsListInPurposeOrderSmallestTierFirst() {
		int thresholdGrand = CardinalRiteFloorDefinition.listOrder("threshold", CardinalRiteType.GRAND);
		int dominionMinor = CardinalRiteFloorDefinition.listOrder("dominion", CardinalRiteType.MINOR);
		int workingMinor = CardinalRiteFloorDefinition.listOrder("working", CardinalRiteType.MINOR);
		int workingGrand = CardinalRiteFloorDefinition.listOrder("working", CardinalRiteType.GRAND);
		int communionMinor = CardinalRiteFloorDefinition.listOrder("communion", CardinalRiteType.MINOR);
		int unknown = CardinalRiteFloorDefinition.listOrder("pale", CardinalRiteType.MINOR);
		assertTrue(thresholdGrand < dominionMinor && dominionMinor < workingMinor);
		assertTrue(workingMinor < CardinalRiteFloorDefinition.listOrder("working", CardinalRiteType.LESSER)
				&& CardinalRiteFloorDefinition.listOrder("working", CardinalRiteType.GREATER) < workingGrand);
		assertTrue(workingGrand < communionMinor && communionMinor < unknown, "styles outside the purposes list last");
	}
}
