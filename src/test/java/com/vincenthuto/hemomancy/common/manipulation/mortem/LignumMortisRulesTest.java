package com.vincenthuto.hemomancy.common.manipulation.mortem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LignumMortisRulesTest {
	@Test
	void reachUsesMasteryAlignmentsAndSanguineReach() {
		assertEquals(6, LignumMortisRules.maxRadius(0, 0.0D, 0.0D, 1.0D));
		assertEquals(17, LignumMortisRules.maxRadius(2, 80.0D, 40.0D, 1.15D));
		assertEquals(24, LignumMortisRules.maxRadius(4, 100.0D, 100.0D, 1.45D));
		assertEquals(24, LignumMortisRules.maxRadius(99, 500.0D, 500.0D, 10.0D));
	}

	@Test
	void crawlStartsAtTwoAndCapsAtEightBlocksPerSecond() {
		assertEquals(2.0D, LignumMortisRules.blocksPerSecond(0.0D));
		assertEquals(5.0D, LignumMortisRules.blocksPerSecond(12.0D));
		assertEquals(8.0D, LignumMortisRules.blocksPerSecond(24.0D));
		assertEquals(8.0D, LignumMortisRules.blocksPerSecond(100.0D));
	}

	@Test
	void overlayDarkensAcrossFourDistanceBands() {
		assertEquals(0, LignumMortisRules.overlayBand(0.0D, 24.0D));
		assertEquals(1, LignumMortisRules.overlayBand(6.0D, 24.0D));
		assertEquals(2, LignumMortisRules.overlayBand(12.0D, 24.0D));
		assertEquals(3, LignumMortisRules.overlayBand(23.9D, 24.0D));
	}
}
