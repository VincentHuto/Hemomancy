package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Static utility methods for drawing and hit-testing the five node shapes
 * (diamond, square, circle, triangle, hexagon) used by HarbingerProgressScreen
 * and UnstainedProgressScreen.
 * <p>
 * All methods are parameterised by centre coordinates and a half-size
 * (half the node's bounding dimension).
 */
public final class NodeShapeRenderer {

	private NodeShapeRenderer() {}

	// ────────────────────────────────────────────────────────────
	//  Filled shape
	// ────────────────────────────────────────────────────────────

	public static void drawFill(GuiGraphics gfx, EnumNodeShape shape,
								int cx, int cy, int hs, int color) {
		drawFill(gfx::fill, shape, cx, cy, hs, color);
	}

	public static void drawFill(ColoredRectBatch batch, EnumNodeShape shape,
			int cx, int cy, int hs, int color) {
		drawFill(batch::fill, shape, cx, cy, hs, color);
	}

	private static void drawFill(RectSink sink, EnumNodeShape shape, int cx, int cy, int hs, int color) {
		switch (shape) {
			case DIAMOND  -> drawDiamondFill(sink, cx, cy, hs, color);
			case SQUARE   -> sink.fill(cx - hs, cy - hs, cx + hs, cy + hs, color);
			case CIRCLE   -> drawCircleFill(sink, cx, cy, hs, color);
			case TRIANGLE -> drawTriangleFill(sink, cx, cy, hs, color);
			case HEXAGON  -> drawHexagonFill(sink, cx, cy, hs, color);
			case OCTAGON  -> drawRegularPolygonFill(sink, cx, cy, hs, 8, color);
			case DECAGON  -> drawRegularPolygonFill(sink, cx, cy, hs, 10, color);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Outline (1-px border)
	// ────────────────────────────────────────────────────────────

	public static void drawOutline(GuiGraphics gfx, EnumNodeShape shape,
								   int cx, int cy, int hs, int color) {
		drawOutline(gfx::fill, shape, cx, cy, hs, color);
	}

	public static void drawOutline(ColoredRectBatch batch, EnumNodeShape shape,
			int cx, int cy, int hs, int color) {
		drawOutline(batch::fill, shape, cx, cy, hs, color);
	}

	private static void drawOutline(RectSink sink, EnumNodeShape shape, int cx, int cy, int hs, int color) {
		switch (shape) {
			case DIAMOND  -> drawDiamondOutline(sink, cx, cy, hs, color);
			case SQUARE   -> drawSquareOutline(sink, cx, cy, hs, color);
			case CIRCLE   -> drawCircleOutline(sink, cx, cy, hs, color);
			case TRIANGLE -> drawTriangleOutline(sink, cx, cy, hs, color);
			case HEXAGON  -> drawHexagonOutline(sink, cx, cy, hs, color);
			case OCTAGON  -> drawRegularPolygonOutline(sink, cx, cy, hs, 8, color);
			case DECAGON  -> drawRegularPolygonOutline(sink, cx, cy, hs, 10, color);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Hit test
	// ────────────────────────────────────────────────────────────

	public static boolean isInside(EnumNodeShape shape,
								   double mx, double my, int cx, int cy, int hs) {
		return switch (shape) {
			case DIAMOND  -> isInsideDiamond(mx, my, cx, cy, hs);
			case SQUARE   -> isInsideSquare(mx, my, cx, cy, hs);
			case CIRCLE   -> isInsideCircle(mx, my, cx, cy, hs);
			case TRIANGLE -> isInsideTriangle(mx, my, cx, cy, hs);
			case HEXAGON  -> isInsideHexagon(mx, my, cx, cy, hs);
			case OCTAGON  -> RegularPolygonGeometry.isInside(mx, my, cx, cy, hs, 8);
			case DECAGON  -> RegularPolygonGeometry.isInside(mx, my, cx, cy, hs, 10);
		};
	}

	//  Diamond

	private static void drawDiamondFill(RectSink sink, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int w = hs - Math.abs(row);
			if (w <= 0) continue;
			sink.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
		}
	}

	private static void drawDiamondOutline(RectSink sink, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int w = hs - Math.abs(row);
			if (w <= 0) {
				sink.fill(cx, cy + row, cx + 1, cy + row + 1, color);
				continue;
			}
			sink.fill(cx - w, cy + row, cx - w + 1, cy + row + 1, color);
			sink.fill(cx + w - 1, cy + row, cx + w, cy + row + 1, color);
			if (Math.abs(row) >= hs - 1) {
				sink.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
			}
		}
	}

	private static boolean isInsideDiamond(double mx, double my, int cx, int cy, int hs) {
		return Math.abs(mx - cx) + Math.abs(my - cy) <= hs;
	}

	//  Square

	private static void drawSquareOutline(RectSink sink, int cx, int cy, int hs, int color) {
		sink.fill(cx - hs, cy - hs, cx + hs, cy - hs + 1, color); // top
		sink.fill(cx - hs, cy + hs - 1, cx + hs, cy + hs, color); // bottom
		sink.fill(cx - hs, cy - hs, cx - hs + 1, cy + hs, color); // left
		sink.fill(cx + hs - 1, cy - hs, cx + hs, cy + hs, color); // right
	}

	private static boolean isInsideSquare(double mx, double my, int cx, int cy, int hs) {
		return mx >= cx - hs && mx <= cx + hs && my >= cy - hs && my <= cy + hs;
	}

	//  Circle (pixel-approximated)

	private static void drawCircleFill(RectSink sink, int cx, int cy, int hs, int color) {
		int r2 = hs * hs;
		for (int row = -hs; row <= hs; row++) {
			int maxW = (int) Math.sqrt(r2 - row * row);
			if (maxW <= 0) continue;
			sink.fill(cx - maxW, cy + row, cx + maxW, cy + row + 1, color);
		}
	}

	private static void drawCircleOutline(RectSink sink, int cx, int cy, int hs, int color) {
		int r2 = hs * hs;
		int innerR2 = (hs - 1) * (hs - 1);
		for (int row = -hs; row <= hs; row++) {
			int outerW = (int) Math.sqrt(Math.max(0, r2 - row * row));
			int innerW = (int) Math.sqrt(Math.max(0, innerR2 - row * row));
			if (outerW <= 0) continue;
			// left edge
			sink.fill(cx - outerW, cy + row, cx - innerW, cy + row + 1, color);
			// right edge
			sink.fill(cx + innerW, cy + row, cx + outerW, cy + row + 1, color);
		}
	}

	private static boolean isInsideCircle(double mx, double my, int cx, int cy, int hs) {
		double dx = mx - cx;
		double dy = my - cy;
		return dx * dx + dy * dy <= (double) hs * hs;
	}

	private static void drawRegularPolygonFill(RectSink sink, int cx, int cy, int hs, int sides, int color) {
		for (int row = -hs; row <= hs; row++) {
			int[] span = RegularPolygonGeometry.horizontalSpan(row, hs, sides);
			if (span[1] >= span[0]) sink.fill(cx + span[0], cy + row, cx + span[1] + 1, cy + row + 1, color);
		}
	}

	private static void drawRegularPolygonOutline(RectSink sink, int cx, int cy, int hs, int sides, int color) {
		int[] previous = null;
		for (int row = -hs; row <= hs; row++) {
			int[] span = RegularPolygonGeometry.horizontalSpan(row, hs, sides);
			if (span[1] < span[0]) continue;
			if (previous == null || row == hs) {
				sink.fill(cx + span[0], cy + row, cx + span[1] + 1, cy + row + 1, color);
			} else {
				sink.fill(cx + span[0], cy + row, cx + span[0] + 1, cy + row + 1, color);
				sink.fill(cx + span[1], cy + row, cx + span[1] + 1, cy + row + 1, color);
				if (span[0] != previous[0]) sink.fill(cx + Math.min(span[0], previous[0]), cy + row,
						cx + Math.max(span[0], previous[0]) + 1, cy + row + 1, color);
				if (span[1] != previous[1]) sink.fill(cx + Math.min(span[1], previous[1]), cy + row,
						cx + Math.max(span[1], previous[1]) + 1, cy + row + 1, color);
			}
			previous = span;
		}
	}

	//  Triangle (equilateral, pointing up)

	private static void drawTriangleFill(RectSink sink, int cx, int cy, int hs, int color) {
		// Apex at (cx, cy - hs), base from (cx - hs, cy + hs) to (cx + hs, cy + hs).
		int h = hs * 2; // total height
		for (int row = 0; row <= h; row++) {
			int y = cy - hs + row;
			float progress = (float) row / h;
			int halfW = (int) (hs * progress);
			if (halfW <= 0) {
				halfW = 1;
			}
			sink.fill(cx - halfW, y, cx + halfW, y + 1, color);
		}
	}

	private static void drawTriangleOutline(RectSink sink, int cx, int cy, int hs, int color) {
		int h = hs * 2;
		for (int row = 0; row <= h; row++) {
			int y = cy - hs + row;
			float progress = (float) row / h;
			int halfW = Math.max(1, (int) (hs * progress));
			if (row == h) {
				// Bottom edge — full row
				sink.fill(cx - halfW, y, cx + halfW, y + 1, color);
			} else {
				// Left and right edges
				sink.fill(cx - halfW, y, cx - halfW + 1, y + 1, color);
				sink.fill(cx + halfW - 1, y, cx + halfW, y + 1, color);
			}
		}
		// Apex pixel
		sink.fill(cx, cy - hs, cx + 1, cy - hs + 1, color);
	}

	private static boolean isInsideTriangle(double mx, double my, int cx, int cy, int hs) {
		// Apex (cx, cy-hs), base at cy+hs, half-width = hs at base.
		if (my < cy - hs || my > cy + hs) return false;
		double progress = (my - (cy - hs)) / (double)(hs * 2);
		double halfW = hs * progress;
		return Math.abs(mx - cx) <= halfW;
	}

	//  Hexagon (flat-topped)

	private static void drawHexagonFill(RectSink sink, int cx, int cy, int hs, int color) {
		int halfHeight = hexHalfHeight(hs);
		for (int row = -halfHeight; row <= halfHeight; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			sink.fill(cx - halfW, cy + row, cx + halfW, cy + row + 1, color);
		}
	}

	private static void drawHexagonOutline(RectSink sink, int cx, int cy, int hs, int color) {
		int halfHeight = hexHalfHeight(hs);
		for (int row = -halfHeight; row <= halfHeight; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			int connectedHalfW = row == 0 ? halfW : hexHalfWidth(row - Integer.signum(row), hs);
			// Edge pixels
			sink.fill(cx - connectedHalfW, cy + row, cx - halfW + 1, cy + row + 1, color);
			sink.fill(cx + halfW - 1, cy + row, cx + connectedHalfW, cy + row + 1, color);
			// Top & bottom flat edges
			if (row == -halfHeight || row == halfHeight) {
				sink.fill(cx - connectedHalfW, cy + row, cx + connectedHalfW, cy + row + 1, color);
			}
		}
	}

	private static boolean isInsideHexagon(double mx, double my, int cx, int cy, int hs) {
		int row = (int) Math.round(my - cy);
		if (Math.abs(row) > hexHalfHeight(hs)) return false;
		int halfW = hexHalfWidth(row, hs);
		return Math.abs(mx - cx) <= halfW;
	}

	/**
	 * Flat-topped hexagon half-width at a given row offset from centre.
	 * Tapers continuously from {@code hs} at the equator to approximately
	 * {@code hs / 2} at the flat top and bottom edges.
	 */
	private static int hexHalfWidth(int row, int hs) {
		int halfHeight = hexHalfHeight(hs);
		if (Math.abs(row) > halfHeight) return 0;
		return Math.max(1, hs - Math.abs(row) * hs / (2 * halfHeight));
	}

	private static int hexHalfHeight(int hs) {
		return Math.max(1, Math.round(hs * 0.8660254F));
	}

	@FunctionalInterface
	private interface RectSink {
		void fill(int left, int top, int right, int bottom, int color);
	}
}
