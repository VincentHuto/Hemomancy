package com.vincenthuto.hemomancy.common.tile.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class PallidRetortOutputRulesTest {
	@Test
	void recipeOutputAndHumorBottlingUseIndependentSlots() {
		assertEquals(PallidRetortOutputRules.Destination.RECIPE_RESULT,
				PallidRetortOutputRules.recipeDestination(true));
		assertEquals(PallidRetortOutputRules.Destination.FLASK_OUTPUT,
				PallidRetortOutputRules.bottlingDestination());
	}
}
