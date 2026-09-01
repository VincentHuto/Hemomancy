package com.vincenthuto.hemomancy.common.tile.unstained.crafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PallidRetortOutputRulesTest {
	@Test
	void recipeOutputAndHumorBottlingUseIndependentSlots() {
		assertEquals(PallidRetortOutputRules.Destination.RECIPE_RESULT,
				PallidRetortOutputRules.recipeDestination(true));
		assertEquals(PallidRetortOutputRules.Destination.FLASK_OUTPUT,
				PallidRetortOutputRules.bottlingDestination());
	}
}
