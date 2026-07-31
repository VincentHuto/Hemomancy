package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared world-space aim points for rendered Cardinal Rite targets.
 */
public final class CardinalRiteTargetGeometry {
	public static final double SIGIL_MARKER_Y_OFFSET = 0.08D;

	private CardinalRiteTargetGeometry() {
	}

	public static Vec3 anchorAimPoint(BlockPos riteCenter, BlockPos anchorOffset) {
		return new Vec3(
				riteCenter.getX() + anchorOffset.getX() + 0.5D,
				CardinalRiteAnchorVisualRules.ritePlaneY(riteCenter.getY()),
				riteCenter.getZ() + anchorOffset.getZ() + 0.5D);
	}

	public static Vec3 sigilAimPoint(BlockPos riteCenter, BlockPos surface,
			int placementX, int placementZ, double nodeX, double nodeZ) {
		return new Vec3(
				riteCenter.getX() + 0.5D + placementX + nodeX,
				surface.getY() + SIGIL_MARKER_Y_OFFSET,
				riteCenter.getZ() + 0.5D + placementZ + nodeZ);
	}

	public static int nearestAnchorIndex(BlockPos riteSurface, List<BlockPos> anchorOffsets,
			BlockPos target, double radius) {
		if (riteSurface == null || anchorOffsets == null || target == null || radius < 0.0D) return -1;
		int nearest = -1;
		double nearestDistanceSqr = radius * radius;
		for (int index = 0; index < anchorOffsets.size(); index++) {
			BlockPos offset = anchorOffsets.get(index);
			if (offset == null) continue;
			BlockPos anchor = riteSurface.offset(offset.getX(), 0, offset.getZ());
			double distanceSqr = Math.min(anchor.distSqr(target), anchor.above().distSqr(target));
			if (distanceSqr > nearestDistanceSqr) continue;
			if (nearest >= 0 && distanceSqr == nearestDistanceSqr) continue;
			nearest = index;
			nearestDistanceSqr = distanceSqr;
		}
		return nearest;
	}
}
