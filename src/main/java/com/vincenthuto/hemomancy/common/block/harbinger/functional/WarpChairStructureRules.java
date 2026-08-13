package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class WarpChairStructureRules {
	private WarpChairStructureRules() {
	}

	public static BlockPos fillerPos(BlockPos lower, Direction facing) {
		return lower.above();
	}

	public static BlockPos pairedChairPos(BlockPos arborFloor) {
		return arborFloor.above().north();
	}

	public static BlockPos legacyPairedChairPos(BlockPos arborFloor) {
		return arborFloor.above().north(2);
	}

	public static Direction facingAwayFrom(BlockPos anchor, BlockPos chair) {
		int dx = chair.getX() - anchor.getX();
		int dz = chair.getZ() - anchor.getZ();
		return Math.abs(dx) > Math.abs(dz)
				? (dx >= 0 ? Direction.EAST : Direction.WEST)
				: (dz >= 0 ? Direction.SOUTH : Direction.NORTH);
	}
}
