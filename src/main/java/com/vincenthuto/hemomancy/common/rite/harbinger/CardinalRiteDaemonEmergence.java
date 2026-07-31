package com.vincenthuto.hemomancy.common.rite.harbinger;

/** Geometry and timing for a daemon forming from the planted Living Staff. */
public final class CardinalRiteDaemonEmergence {
	public static final int EMERGENCE_TICKS = 24;
	public static final int SPIRAL_POINTS = 8;
	private static final double STAFF_TOP_Y_OFFSET = 3.65D;

	private CardinalRiteDaemonEmergence() {
	}

	public static double progress(int elapsedTicks) {
		return Math.max(0.0D, Math.min(1.0D, elapsedTicks / (double) EMERGENCE_TICKS));
	}

	public static double daemonY(double focusY, double matureSourceY) {
		return Math.max(focusY + STAFF_TOP_Y_OFFSET, matureSourceY);
	}

	public static SpiralPoint spiralPoint(double centerX, double centerZ, int elapsedTicks,
			int pointIndex, int strand) {
		double progress = progress(elapsedTicks);
		double angle = elapsedTicks * 0.55D
				+ pointIndex * Math.PI * 2.0D / SPIRAL_POINTS
				+ strand * Math.PI;
		double radius = 0.28D + (1.0D - progress) * 0.16D;
		double y = 0.25D + pointIndex / (double) (SPIRAL_POINTS - 1) * 1.75D;
		return new SpiralPoint(centerX + Math.cos(angle) * radius, y,
				centerZ + Math.sin(angle) * radius);
	}

	public record SpiralPoint(double x, double y, double z) {
	}
}
