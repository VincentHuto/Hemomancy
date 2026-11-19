package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

/** Snow the freeze pass laid on a troupe yard comes off; snow on the grass round it stays (2026-11-17). */
class TroupeSnowClearingTest {
	@Test
	void onlySnowOnTheYardFloorIsCleared() {
		// Column (0, 0): snow on sawdust. (1, 0): snow on grass. (2, 0): sawdust, no snow. (3, 0): snow on a curb top.
		Map<BlockPos, String> world = Map.of(
				new BlockPos(0, 64, 0), "snow", new BlockPos(0, 63, 0), "yard",
				new BlockPos(1, 64, 0), "snow", new BlockPos(1, 63, 0), "grass",
				new BlockPos(2, 63, 0), "yard",
				new BlockPos(3, 65, 0), "snow", new BlockPos(3, 64, 0), "yard");
		var cleared = TroupeSnowClearing.snowOnYard(0, 0, 3, 0, 60, 70,
				pos -> "snow".equals(world.get(pos)), pos -> "yard".equals(world.get(pos)));
		assertEquals(Set.of(new BlockPos(0, 64, 0), new BlockPos(3, 65, 0)), Set.copyOf(cleared));
	}
}
