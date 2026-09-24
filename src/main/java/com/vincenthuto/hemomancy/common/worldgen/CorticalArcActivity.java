package com.vincenthuto.hemomancy.common.worldgen;

import java.util.List;
import net.minecraft.core.BlockPos;

/** Pure scheduling and distance policy for player-proximate Cortical Drift nerve activity. */
final class CorticalArcActivity {
	private static final int ARC_INTERVAL = 10;
	private static final int SCAN_RADIUS = 72;
	private static final double PAIR_RANGE_SQR = 128.0D * 128.0D;

	record ScanBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
		boolean contains(BlockPos pos) {
			return pos.getX() >= minX && pos.getX() <= maxX
					&& pos.getY() >= minY && pos.getY() <= maxY
					&& pos.getZ() >= minZ && pos.getZ() <= maxZ;
		}
	}

	private CorticalArcActivity() {
	}

	static boolean shouldScan(boolean inEnd, boolean spectator, long gameTime, int playerId) {
		return inEnd && !spectator && (gameTime + playerId) % ARC_INTERVAL == 0L;
	}

	static ScanBounds scanBounds(BlockPos center, int minBuildHeight, int maxBuildHeight) {
		return new ScanBounds(
				center.getX() - SCAN_RADIUS,
				center.getX() + SCAN_RADIUS,
				Math.max(minBuildHeight, center.getY() - SCAN_RADIUS),
				Math.min(maxBuildHeight - 1, center.getY() + SCAN_RADIUS),
				center.getZ() - SCAN_RADIUS,
				center.getZ() + SCAN_RADIUS);
	}

	/** Returns the nearest unused endpoint within one bridge-scale span. */
	static int nearest(List<BlockPos> nodes, int index, boolean[] consumed) {
		BlockPos from = nodes.get(index);
		int best = -1;
		double bestDistance = PAIR_RANGE_SQR;
		for (int i = 0; i < nodes.size(); i++) {
			if (i == index || consumed[i]) {
				continue;
			}
			double distance = from.distSqr(nodes.get(i));
			if (distance < bestDistance) {
				bestDistance = distance;
				best = i;
			}
		}
		return best;
	}
}
