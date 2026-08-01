package com.vincenthuto.hemomancy.client.screen.skilltree.util;

/** Shared center and degree-ring geometry for concentric progress trees. */
public final class ConcentricTreeGeometry {
	public static final int CENTER_X = 480;
	public static final int CENTER_Y = 480;
	public static final int CONTENT_PADDING = 80;
	private static final int[] DEGREE_RING_RADII = {72, 120, 170, 220, 270, 320, 370, 420, 470};

	private ConcentricTreeGeometry() {}

	public static int ringCount() {
		return DEGREE_RING_RADII.length;
	}

	public static int radiusForDegree(int degree) {
		return DEGREE_RING_RADII[Math.max(0, Math.min(DEGREE_RING_RADII.length - 1, degree))];
	}

	public static int[] degreeRingRadii() {
		return DEGREE_RING_RADII.clone();
	}

	public static Point pointOnDegreeRing(int degree, double angleRadians) {
		int radius = radiusForDegree(degree);
		return new Point(CENTER_X + (int) Math.round(Math.cos(angleRadians) * radius),
				CENTER_Y + (int) Math.round(Math.sin(angleRadians) * radius));
	}

	public record Point(int x, int y) {}
}
