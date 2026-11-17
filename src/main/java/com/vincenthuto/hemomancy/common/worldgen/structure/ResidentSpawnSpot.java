package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.Nullable;

/**
 * Where a site's resident stands (PT-23): its authored marker, or when that is obstructed the nearest clear, standable
 * cell within {@link #REACH} blocks of it. A marker with none is logged, never skipped in silence.
 */
public final class ResidentSpawnSpot {
	public static final int REACH = 3;

	private ResidentSpawnSpot() {
	}

	@Nullable
	public static BlockPos find(WorldGenLevel level, BlockPos marker) {
		if (standable(level, marker)) return marker;
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos cell : BlockPos.betweenClosed(marker.offset(-REACH, -1, -REACH), marker.offset(REACH, 1, REACH))) {
			double distance = cell.distSqr(marker);
			if (distance < bestDistance && distance <= REACH * REACH && standable(level, cell)) {
				best = cell.immutable();
				bestDistance = distance;
			}
		}
		if (best == null) Hemomancy.LOGGER.warn("No clear cell within {} blocks of the resident marker at {}", REACH, marker);
		return best;
	}

	static boolean standable(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
				&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
	}
}
