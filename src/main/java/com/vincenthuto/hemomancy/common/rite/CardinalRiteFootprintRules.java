package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class CardinalRiteFootprintRules {
	public static final float MARKER_CLEARANCE = 0.75F;
	public static final float AWAKENED_SIGIL_CLEARANCE = 1.25F;
	public static final float MIN_AWAKENED_SIGIL_ORBIT_RADIUS = 1.5F;

	private CardinalRiteFootprintRules() {
	}

	public static float radius(List<BlockPos> boundaryPoints, List<BlockPos> sigilPoints) {
		double farthest = 0.0D;
		for (BlockPos point : boundaryPoints) farthest = Math.max(farthest, horizontalDistance(point));
		for (BlockPos point : sigilPoints) farthest = Math.max(farthest, horizontalDistance(point));
		return (float) farthest + MARKER_CLEARANCE;
	}

	public static float awakenedSigilOrbitRadius(float footprintRadius) {
		float safeFootprint = Float.isFinite(footprintRadius) ? Math.max(0.0F, footprintRadius) : 0.0F;
		return Math.max(MIN_AWAKENED_SIGIL_ORBIT_RADIUS, safeFootprint - AWAKENED_SIGIL_CLEARANCE);
	}

	private static double horizontalDistance(BlockPos point) {
		return Math.sqrt((double) point.getX() * point.getX() + (double) point.getZ() * point.getZ());
	}
}
