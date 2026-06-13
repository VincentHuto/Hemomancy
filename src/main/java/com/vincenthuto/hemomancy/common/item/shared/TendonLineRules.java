package com.vincenthuto.hemomancy.common.item.shared;

public final class TendonLineRules {
	public static final double ANCHOR_RANGE = 18.0D;
	public static final double DETACH_RANGE = 24.0D;
	public static final double CLOSE_DISTANCE = 1.35D;
	public static final double PULL_SPEED = 0.34D;
	public static final double EXISTING_MOTION_BLEND = 0.35D;
	public static final int USE_DURATION_TICKS = 72000;
	public static final int USE_COOLDOWN_TICKS = 10;

	private TendonLineRules() {
	}

	public static boolean canAnchor(double distance) {
		return distance <= ANCHOR_RANGE;
	}

	public static boolean shouldDetach(double distance) {
		return distance > DETACH_RANGE;
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
