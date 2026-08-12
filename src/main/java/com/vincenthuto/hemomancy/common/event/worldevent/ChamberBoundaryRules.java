package com.vincenthuto.hemomancy.common.event.worldevent;

/** One source of truth for Chamber placement, clamp, cell, and rescue bounds. */
public final class ChamberBoundaryRules {
	private static final int ORB_DEPTH = 3;
	private static final int RESCUE_DEPTH = 8;

	private ChamberBoundaryRules() {
	}

	public static boolean insidePlatform(double x, double z, int centerX, int centerZ, int radius) {
		return x >= centerX - radius && x <= centerX + radius
				&& z >= centerZ - radius && z <= centerZ + radius;
	}

	public static double clampCoordinate(double coordinate, int center, int radius) {
		return Math.max(center - radius + 0.5D, Math.min(center + radius + 0.5D, coordinate));
	}

	public static boolean belowOrbPlane(double y, int floorY) {
		return y < floorY - ORB_DEPTH;
	}

	public static boolean belowRescuePlane(double y, int floorY) {
		return y < floorY - RESCUE_DEPTH;
	}

	public static boolean insideAllocatedCell(double x, double z, int centerX, int centerZ, int spacing) {
		double half = spacing / 2.0D;
		return x >= centerX - half && x < centerX + half
				&& z >= centerZ - half && z < centerZ + half;
	}
}
