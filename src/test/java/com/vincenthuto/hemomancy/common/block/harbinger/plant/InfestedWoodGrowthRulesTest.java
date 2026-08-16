package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InfestedWoodGrowthRulesTest {

	@Test
	void growthRequiresEmptyDarkSpace() {
		assertTrue(InfestedWoodGrowthRules.canGrow(true, 7));
		assertFalse(InfestedWoodGrowthRules.canGrow(false, 7));
		assertFalse(InfestedWoodGrowthRules.canGrow(true, 8));
	}

	@Test
	void rollSelectsTheDocumentedGrowthWithoutGaps() {
		assertEquals(InfestedWoodGrowthRules.Growth.INFECTED_FUNGUS, InfestedWoodGrowthRules.select(0));
		assertEquals(InfestedWoodGrowthRules.Growth.INFECTED_FUNGUS, InfestedWoodGrowthRules.select(4));
		assertEquals(InfestedWoodGrowthRules.Growth.HYPHAE, InfestedWoodGrowthRules.select(5));
		assertEquals(InfestedWoodGrowthRules.Growth.HYPHAE, InfestedWoodGrowthRules.select(7));
		assertEquals(InfestedWoodGrowthRules.Growth.STINKHORN_FUNGUS, InfestedWoodGrowthRules.select(8));
		assertEquals(InfestedWoodGrowthRules.Growth.STINKHORN_FUNGUS, InfestedWoodGrowthRules.select(9));
	}

	@Test
	void invalidRollIsRejected() {
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
				() -> InfestedWoodGrowthRules.select(10));
	}
}
