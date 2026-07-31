package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFloorCompatibilityTest {
	@Test
	void higherTierOfSameStyleSatisfiesLowerRequirementWithoutComparingGeometry() {
		CardinalRiteFloorRequirement lesserCommunion =
				new CardinalRiteFloorRequirement("communion", CardinalRiteType.LESSER);

		assertTrue(lesserCommunion.accepts("communion", CardinalRiteType.GRAND));
		assertTrue(lesserCommunion.accepts("communion", CardinalRiteType.LESSER));
		assertFalse(lesserCommunion.accepts("communion", CardinalRiteType.MINOR));
	}

	@Test
	void anotherStyleNeverSatisfiesRequirementAtAnyTier() {
		CardinalRiteFloorRequirement lesserCommunion =
				new CardinalRiteFloorRequirement("communion", CardinalRiteType.LESSER);

		assertFalse(lesserCommunion.accepts("dominion", CardinalRiteType.GRAND));
	}
}
