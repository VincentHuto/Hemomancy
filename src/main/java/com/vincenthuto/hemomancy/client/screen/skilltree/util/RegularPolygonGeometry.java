package com.vincenthuto.hemomancy.client.screen.skilltree.util;

/** Geometry shared by the rasterizer and hit test for regular skill-tree nodes. */
public final class RegularPolygonGeometry {
	private RegularPolygonGeometry() {}

	public static int[] horizontalSpan(int row, int radius, int sides) {
		if (radius <= 0 || sides < 3 || Math.abs(row) > radius) return new int[] { 0, -1 };
		double scanY = row;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < sides; i++) {
			double a1 = -Math.PI / 2.0D + 2.0D * Math.PI * i / sides;
			double a2 = -Math.PI / 2.0D + 2.0D * Math.PI * (i + 1) / sides;
			double x1 = Math.cos(a1) * radius;
			double y1 = Math.sin(a1) * radius;
			double x2 = Math.cos(a2) * radius;
			double y2 = Math.sin(a2) * radius;
			if (scanY < Math.min(y1, y2) - 0.000001D || scanY > Math.max(y1, y2) + 0.000001D) continue;
			if (Math.abs(y2 - y1) < 0.000001D) {
				min = Math.min(min, Math.min(x1, x2));
				max = Math.max(max, Math.max(x1, x2));
			} else {
				double x = x1 + (scanY - y1) * (x2 - x1) / (y2 - y1);
				min = Math.min(min, x);
				max = Math.max(max, x);
			}
		}
		if (!Double.isFinite(min)) return new int[] { 0, -1 };
		return new int[] { (int) Math.ceil(min), (int) Math.floor(max) };
	}

	public static boolean isInside(double x, double y, int cx, int cy, int radius, int sides) {
		if (radius <= 0 || sides < 3) return false;
		double localX = x - cx;
		double localY = y - cy;
		boolean inside = false;
		for (int i = 0, j = sides - 1; i < sides; j = i++) {
			double ai = -Math.PI / 2.0D + 2.0D * Math.PI * i / sides;
			double aj = -Math.PI / 2.0D + 2.0D * Math.PI * j / sides;
			double xi = Math.cos(ai) * radius;
			double yi = Math.sin(ai) * radius;
			double xj = Math.cos(aj) * radius;
			double yj = Math.sin(aj) * radius;
			double cross = (localX - xi) * (yj - yi) - (localY - yi) * (xj - xi);
			double dot = (localX - xi) * (localX - xj) + (localY - yi) * (localY - yj);
			if (Math.abs(cross) <= 0.00001D && dot <= 0.00001D) return true;
			if ((yi > localY) != (yj > localY)
					&& localX < (xj - xi) * (localY - yi) / (yj - yi) + xi) inside = !inside;
		}
		return inside;
	}
}
