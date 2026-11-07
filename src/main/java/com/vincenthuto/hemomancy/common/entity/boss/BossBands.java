package com.vincenthuto.hemomancy.common.entity.boss;

/**
 * Where the quarry stands relative to a boss: a distance band from its centre and a sector from its body facing. A
 * boss's picker answers each band and sector differently. Pure, so it is JVM-tested.
 */
public record BossBands(double hug, double near, double mid) {
	/** The one set every Saint uses (resolution B-1 of 2026-11-05): HUG under 3, NEAR under 6.5, MID under 12. */
	public static final BossBands STANDARD = new BossBands(3.0D, 6.5D, 12.0D);

	public enum Band { HUG, NEAR, MID, FAR }

	public enum Sector { FRONT, FLANK, REAR }

	private static final double FRONT_MAX = 60.0D;
	private static final double FLANK_MAX = 120.0D;

	/** Each lower bound belongs to the outer band. */
	public Band band(double horizontalDistance) {
		if (horizontalDistance < hug) return Band.HUG;
		if (horizontalDistance < near) return Band.NEAR;
		if (horizontalDistance < mid) return Band.MID;
		return Band.FAR;
	}

	/** FRONT up to 60 degrees, FLANK up to 120, REAR beyond. */
	public static Sector sector(double degreesFromFacing) {
		if (degreesFromFacing <= FRONT_MAX) return Sector.FRONT;
		if (degreesFromFacing <= FLANK_MAX) return Sector.FLANK;
		return Sector.REAR;
	}

	/**
	 * Degrees (0..180) between the facing at {@code yawDegrees} and the point {@code (toX, toZ)} seen from
	 * {@code (fromX, fromZ)}, with Minecraft's forward vector (-sin yaw, cos yaw). A point on the centre counts as ahead.
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
