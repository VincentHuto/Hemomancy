package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class CardinalRiteFootprintRules {
	public static final float MARKER_CLEARANCE = 0.75F;

	private CardinalRiteFootprintRules() {
	}

	public static float radius(List<BlockPos> boundaryPoints, List<BlockPos> sigilPoints) {
		double farthest = 0.0D;
		for (BlockPos point : boundaryPoints) farthest = Math.max(farthest, horizontalDistance(point));
		for (BlockPos point : sigilPoints) farthest = Math.max(farthest, horizontalDistance(point));
		return (float) farthest + MARKER_CLEARANCE;
	}

	public static float enclosingRadius(float fallbackRadius, float floorRadius,
			List<BlockPos> boundaryPoints, List<BlockPos> supportSocketPoints,
			List<BlockPos> sigilPoints) {
		double farthest = 0.0D;
		for (BlockPos point : boundaryPoints) farthest = Math.max(farthest, horizontalDistance(point));
		for (BlockPos point : supportSocketPoints) farthest = Math.max(farthest, horizontalDistance(point));
		for (BlockPos point : sigilPoints) farthest = Math.max(farthest, horizontalDistance(point));
		float contentRadius = farthest > 0.0D ? (float) farthest + MARKER_CLEARANCE : 0.0F;
		return Math.max(safeRadius(fallbackRadius), Math.max(safeRadius(floorRadius), contentRadius));
	}

	private static float safeRadius(float radius) {
		return Float.isFinite(radius) ? Math.max(0.0F, radius) : 0.0F;
	}

	private static double horizontalDistance(BlockPos point) {
		return Math.sqrt((double) point.getX() * point.getX() + (double) point.getZ() * point.getZ());
	}
}
