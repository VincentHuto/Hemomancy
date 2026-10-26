package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * Where the quarry stands relative to the Spawn Mother: a distance band from her centre and a sector from her body
 * facing. The move picker answers each band and sector differently. Pure, so it is JVM-tested.
 */
public final class MycophantPositionRules {
	/** Horizontal distance from her centre: under her, at claw reach, at spit range, kiting. */
	public enum Band { HUG, NEAR, MID, FAR }

	/** Angle from her body facing: in front, beside, behind. */
	public enum Sector { FRONT, FLANK, REAR }

	private static final double HUG_MAX = 3.5D;
	private static final double NEAR_MAX = 7.0D;
	private static final double MID_MAX = 13.0D;
	private static final double FRONT_MAX = 60.0D;
	private static final double FLANK_MAX = 120.0D;

	private MycophantPositionRules() {
	}

	/** HUG under 3.5 blocks, NEAR to 7, MID to 13, FAR beyond; each lower bound belongs to the outer band. */
	public static Band band(double horizontalDistance) {
		if (horizontalDistance < HUG_MAX) return Band.HUG;
		if (horizontalDistance < NEAR_MAX) return Band.NEAR;
		if (horizontalDistance < MID_MAX) return Band.MID;
		return Band.FAR;
	}

	/** FRONT up to 60 degrees, FLANK up to 120, REAR beyond. */
	public static Sector sector(double degreesFromFacing) {
		if (degreesFromFacing <= FRONT_MAX) return Sector.FRONT;
		if (degreesFromFacing <= FLANK_MAX) return Sector.FLANK;
		return Sector.REAR;
	}

	/**
	 * Degrees (0..180) between her facing at {@code yawDegrees} and the point {@code (toX, toZ)} seen from
	 * {@code (fromX, fromZ)}. Uses the same forward vector as {@code MycophantEntity.forward()}: (-sin yaw, cos yaw).
	 * A point on her centre counts as straight ahead.
	 */
	public static double degreesFromFacing(double fromX, double fromZ, float yawDegrees, double toX, double toZ) {
		double dx = toX - fromX;
		double dz = toZ - fromZ;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-6D) return 0.0D;
		double yaw = Math.toRadians(yawDegrees);
		double dot = (dx * -Math.sin(yaw) + dz * Math.cos(yaw)) / length;
		return Math.toDegrees(Math.acos(Math.max(-1.0D, Math.min(1.0D, dot))));
	}
}
