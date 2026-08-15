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
			case DECAGON  -> drawDecagonFill(sink, cx, cy, hs, color);
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
			case DECAGON  -> drawDecagonOutline(sink, cx, cy, hs, color);
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

	private static void drawDecagonFill(RectSink sink, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int[] span = RegularPolygonGeometry.horizontalSpan(row, hs, 10);
			if (span[1] >= span[0]) sink.fill(cx + span[0], cy + row, cx + span[1] + 1, cy + row + 1, color);
		}
	}

	private static void drawDecagonOutline(RectSink sink, int cx, int cy, int hs, int color) {
		int[] previous = null;
		for (int row = -hs; row <= hs; row++) {
			int[] span = RegularPolygonGeometry.horizontalSpan(row, hs, 10);
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
		// Flat-topped hex: half-height = hs, half-width at equator = hs,
		// top/bottom flat edges at hs/2 wide.
		for (int row = -hs; row <= hs; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			sink.fill(cx - halfW, cy + row, cx + halfW, cy + row + 1, color);
		}
	}

	private static void drawHexagonOutline(RectSink sink, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			int nextHalfW = (row < hs) ? hexHalfWidth(row + 1, hs) : 0;
			// Edge pixels
			sink.fill(cx - halfW, cy + row, cx - halfW + 1, cy + row + 1, color);
			sink.fill(cx + halfW - 1, cy + row, cx + halfW, cy + row + 1, color);
			// Top & bottom flat edges
			if (row == -hs || row == hs) {
				sink.fill(cx - halfW, cy + row, cx + halfW, cy + row + 1, color);
			}
			// If width changes, fill connecting pixels
			if (halfW != nextHalfW && row != hs) {
				int min = Math.min(halfW, nextHalfW);
				int max = Math.max(halfW, nextHalfW);
				sink.fill(cx - max, cy + row, cx - min, cy + row + 1, color);
				sink.fill(cx + min, cy + row, cx + max, cy + row + 1, color);
			}
		}
	}

	private static boolean isInsideHexagon(double mx, double my, int cx, int cy, int hs) {
		int row = (int) Math.round(my - cy);
		if (Math.abs(row) > hs) return false;
		int halfW = hexHalfWidth(row, hs);
		return Math.abs(mx - cx) <= halfW;
	}

	/**
	 * Flat-topped hexagon half-width at a given row offset from centre.
	 * Full width ({@code hs}) at equator (|row| &lt;= hs/2), then linearly
	 * tapers to approximately {@code hs/2} at the top/bottom tips.
	 */
	private static int hexHalfWidth(int row, int hs) {
		int absRow = Math.abs(row);
		// The flat equator zone spans the middle third (absRow <= hs/2),
		// then tapers linearly to ~hs/2 at the top/bottom.
		if (absRow <= hs / 2) {
			return hs;
		}
		// Linear taper from hs → hs/2 over the remaining rows
		float t = (float)(absRow - hs / 2) / (float)(hs - hs / 2);
		return Math.max(1, (int)(hs * (1f - 0.5f * t)));
	}

	@FunctionalInterface
	private interface RectSink {
		void fill(int left, int top, int right, int bottom, int color);
	}
}
