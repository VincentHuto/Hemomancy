package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class CorticalArcActivityTest {
	@Test
	void nearbyEndPlayersReceiveTwoArcPassesPerSecond() {
		int passes = 0;
		for (long tick = 0; tick < 200; tick++) {
			if (CorticalArcActivity.shouldScan(true, false, tick, 0)) {
				passes++;
			}
		}
		assertEquals(20, passes, "nearby nerve activity should be checked twice per second");
		assertFalse(CorticalArcActivity.shouldScan(false, false, 0, 0),
				"the ambient network belongs only in the End");
		assertFalse(CorticalArcActivity.shouldScan(true, true, 0, 0),
				"spectators must not cause ambient effects");
	}

	@Test
	void scanBoundsReachBothEndsOfAPlayerAdjacentBridgeSpan() {
		BlockPos center = new BlockPos(200, 64, -100);
		CorticalArcActivity.ScanBounds bounds = CorticalArcActivity.scanBounds(center, 0, 256);
		assertTrue(bounds.contains(new BlockPos(136, 64, -100)), "negative bridge endpoint was missed");
		assertTrue(bounds.contains(new BlockPos(264, 64, -100)), "positive bridge endpoint was missed");
		assertFalse(bounds.contains(new BlockPos(273, 64, -100)), "scan must retain a finite work radius");
	}

	@Test
	void bridgeScaleNodePairsCanArcWithoutLinkingUnrelatedNetworks() {
		List<BlockPos> nodes = List.of(BlockPos.ZERO, new BlockPos(96, 0, 0), new BlockPos(160, 0, 0));
		assertEquals(1, CorticalArcActivity.nearest(nodes, 0, new boolean[nodes.size()]),
				"a real bridge-scale span should be eligible to fire");

		boolean[] firstTwoConsumed = { true, true, false };
		assertEquals(-1, CorticalArcActivity.nearest(nodes, 2, firstTwoConsumed),
				"a lone distant network must not arc to itself or a consumed endpoint");
	}
}
