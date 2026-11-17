package com.vincenthuto.hemomancy.common.manipulation.animus;

/**
 * The Tendon Line's pure maths: how far the line is thrown, when it snaps, when the haul arrives, and the pull itself.
 * The anchor reaches three times the verb's reach (so reach investment pays), and the line snaps once the player is a
 * third further than that from it.
 */
public final class TendonLineRules {
	/** The anchor is thrown this many times the verb's reach: 16.5 blocks by hand. */
	public static final double REACH_SCALE = 3.0D;
	/** The line snaps past this multiple of the anchor reach. */
	public static final double DETACH_FACTOR = 4.0D / 3.0D;
	public static final double CLOSE_DISTANCE = 1.35D;
	public static final double PULL_SPEED = 0.34D;
	public static final double EXISTING_MOTION_BLEND = 0.35D;
	/** A throw: 10 mL, at most once a second while Projection is held. */
	public static final double THROW_BLOOD = 10.0D;
	public static final int THROW_CADENCE = 20;
	/** The haul pulls every tick for 0.6 mL a tick, 12 mL a second. */
	public static final double HAUL_BLOOD_PER_TICK = 0.6D;
	public static final int HAUL_CADENCE = 1;

	private TendonLineRules() {
	}

	public static boolean canAnchor(double distance, double anchorReach) {
		return distance <= anchorReach;
	}

	public static double detachRange(double anchorReach) {
		return anchorReach * DETACH_FACTOR;
	}

	public static boolean shouldDetach(double distance, double anchorReach) {
		return distance > detachRange(anchorReach);
	}

	public static boolean hasArrived(double distance) {
		return distance <= CLOSE_DISTANCE;
	}

	public static Motion pullMotion(double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
		double dx = toX - fromX;
		double dy = toY - fromY;
		double dz = toZ - fromZ;
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length < 1.0E-5D) {
			return new Motion(0.0D, 0.0D, 0.0D);
		}
		double scale = PULL_SPEED / length;
		return new Motion(dx * scale, dy * scale, dz * scale);
	}

	public record Motion(double x, double y, double z) {
	}
}
