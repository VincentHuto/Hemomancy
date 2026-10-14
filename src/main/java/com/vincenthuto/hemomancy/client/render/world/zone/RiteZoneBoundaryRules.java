package com.vincenthuto.hemomancy.client.render.world.zone;

/**
 * Pure geometry and timing for rite zone boundaries. A zone is a square of
 * blocks; its drawn edge runs along the outer faces of the outermost blocks.
 */
final class RiteZoneBoundaryRules {
	/** Horizontal distance from the camera beyond which no boundary is drawn. */
	static final double VIEW_DISTANCE = 32.0;
	/** Vertical gap between the camera and the ground beyond which a sample is skipped. */
	static final double MAX_GROUND_GAP = 20.0;
	/** Spacing of boundary samples along an edge, in blocks. */
	static final double STEP = 1.0;

	private RiteZoneBoundaryRules() {}

	/** Horizontal distance from a point to the perimeter of the box [min, max], inside or out. */
	static double distanceToPerimeter(double x, double z, double minX, double minZ, double maxX, double maxZ) {
		boolean inside = x >= minX && x <= maxX && z >= minZ && z <= maxZ;
		if (inside) {
			return Math.min(Math.min(x - minX, maxX - x), Math.min(z - minZ, maxZ - z));
		}
		double dx = Math.max(Math.max(minX - x, 0.0), x - maxX);
		double dz = Math.max(Math.max(minZ - z, 0.0), z - maxZ);
		return Math.sqrt(dx * dx + dz * dz);
	}

	/**
	 * The part of an edge within {@code view} of the camera. The edge lies at
	 * {@code across = fixed} and runs from {@code min} to {@code max} along the
	 * other axis.
	 *
	 * @return {start, end} along the edge, or null when nothing is in range
	 */
	static double[] visibleSpan(double fixed, double min, double max, double camAlong, double camAcross, double view) {
		double gap = Math.abs(camAcross - fixed);
		if (gap >= view) return null;
		double half = Math.sqrt(view * view - gap * gap);
		double start = Math.max(min, Math.floor((camAlong - half) / STEP) * STEP);
		double end = Math.min(max, Math.ceil((camAlong + half) / STEP) * STEP);
		return end - start > 1.0e-3 ? new double[] { start, end } : null;
	}

	/** 1 at the camera, easing to 0 at {@code view}. */
	static float fade(double distance, double view) {
		double t = 1.0 - Math.min(1.0, Math.max(0.0, distance / view));
		return (float) (t * t * (3.0 - 2.0 * t));
	}

	/** A slow heartbeat that travels along the Dominion vein. Range 0.35 to 1. */
	static float dominionPulse(double time, double along) {
		double beat = Math.pow(Math.max(0.0, Math.sin(time * 0.04 - along * 0.12)), 6.0);
		return (float) (0.35 + 0.65 * beat);
	}

	/** Sideways wander of the Dominion vein, so it reads as a vessel rather than a ruled line. */
	static double veinMeander(double along, double seed) {
		return Math.sin(along * 0.55 + seed) * 0.14 + Math.sin(along * 1.7 + seed * 2.3) * 0.05;
	}

	/** The Fervor haze's breathing, with a per-sample flicker. Range 0.28 to 1. */
	static float fervorPulse(double time, double along) {
		double breath = 0.7 + 0.3 * Math.sin(time * 0.11);
		double flicker = 0.85 + 0.15 * Math.sin(time * 0.23 + along * 0.9);
		return (float) (breath * flicker);
	}
}
