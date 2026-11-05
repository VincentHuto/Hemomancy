package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.common.encounter.ChamberRoomShape;

/** Chamber placement, clamp, cell and rescue bounds, in world coordinates over {@link ChamberRoomShape}. */
public final class ChamberBoundaryRules {
	private static final int ORB_DEPTH = 3;
	private static final int RESCUE_DEPTH = 8;

	private ChamberBoundaryRules() {
	}

	/** Whether this point stands over the room's floor, the block containing it being the test. */
	public static boolean insidePlatform(double x, double z, int centerX, int centerZ, int radius) {
		return ChamberRoomShape.contains((int) Math.floor(x) - centerX, (int) Math.floor(z) - centerZ, radius);
	}

	/** The nearest world position to (x, z) a body may stand in the room; (x, z) itself when it is already inside. */
	public static ChamberRoomShape.Point clamp(double x, double z, int centerX, int centerZ, int radius) {
		double localX = x - (centerX + 0.5D);
		double localZ = z - (centerZ + 0.5D);
		ChamberRoomShape.Point local = ChamberRoomShape.clamp(localX, localZ, radius);
		// An axis left alone goes back exactly as it came: the round trip through local space can round, and the caller
		// would read that as a move and teleport the player every tick.
		return new ChamberRoomShape.Point(local.dx() == localX ? x : local.dx() + centerX + 0.5D,
				local.dz() == localZ ? z : local.dz() + centerZ + 0.5D);
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
