package com.vincenthuto.hemomancy.common.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class CardinalRiteFoundationRulesTest {

	@Test
	void ordinaryFoundationRoundsOutwardToCoverEverySigilPoint() {
		var foundation = CardinalRiteFoundationRules.squareBelow(
				new BlockPos(10, 64, -4), 11.75F, false);

		assertEquals(625, foundation.size());
		assertTrue(foundation.contains(new BlockPos(-2, 63, -16)));
		assertTrue(foundation.contains(new BlockPos(22, 63, 8)));
	}

	@Test
	void foundingFaneFoundationCoversItsFullHeartBoundary() {
		var foundation = CardinalRiteFoundationRules.squareBelow(
				new BlockPos(10, 64, -4), 11.75F, true);

		assertEquals(6561, foundation.size());
		assertTrue(foundation.contains(new BlockPos(-30, 63, -44)));
		assertTrue(foundation.contains(new BlockPos(50, 63, 36)));
	}
}
