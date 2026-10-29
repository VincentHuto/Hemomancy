package com.vincenthuto.hemomancy.common.world;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderingPoolRulesTest {
	@Test
	void onlyAnApotheosOfferingIsRendered() {
		assertTrue(RenderingPoolRules.mayRender(true));
		assertFalse(RenderingPoolRules.mayRender(false), "anyone else's items float untouched");
	}

	@Test
	void eachItemRollsEachResult() {
		RandomSource random = RandomSource.create(7L);
		assertEquals(12, RenderingPoolRules.yield(2, 6, 1f, random), "a certain result is count x stack");
		assertEquals(0, RenderingPoolRules.yield(2, 6, 0f, random));
		int rolled = RenderingPoolRules.yield(1, 64, 0.5f, random);
		assertTrue(rolled > 10 && rolled < 54, "a half chance lands about half the time: " + rolled);
	}
}
