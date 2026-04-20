package com.vincenthuto.hemomancy.client.screen.skilltree;

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
		switch (shape) {
			case DIAMOND  -> drawDiamondFill(gfx, cx, cy, hs, color);
			case SQUARE   -> gfx.fill(cx - hs, cy - hs, cx + hs, cy + hs, color);
			case CIRCLE   -> drawCircleFill(gfx, cx, cy, hs, color);
			case TRIANGLE -> drawTriangleFill(gfx, cx, cy, hs, color);
			case HEXAGON  -> drawHexagonFill(gfx, cx, cy, hs, color);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Outline (1-px border)
	// ────────────────────────────────────────────────────────────

	public static void drawOutline(GuiGraphics gfx, EnumNodeShape shape,
								   int cx, int cy, int hs, int color) {
		switch (shape) {
			case DIAMOND  -> drawDiamondOutline(gfx, cx, cy, hs, color);
			case SQUARE   -> drawSquareOutline(gfx, cx, cy, hs, color);
			case CIRCLE   -> drawCircleOutline(gfx, cx, cy, hs, color);
			case TRIANGLE -> drawTriangleOutline(gfx, cx, cy, hs, color);
			case HEXAGON  -> drawHexagonOutline(gfx, cx, cy, hs, color);
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
		};
	}

	// ================================================================
	//  Diamond
	// ================================================================

	private static void drawDiamondFill(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int w = hs - Math.abs(row);
			if (w <= 0) continue;
			gfx.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
		}
	}

	private static void drawDiamondOutline(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int w = hs - Math.abs(row);
			if (w <= 0) {
				gfx.fill(cx, cy + row, cx + 1, cy + row + 1, color);
				continue;
			}
			gfx.fill(cx - w, cy + row, cx - w + 1, cy + row + 1, color);
			gfx.fill(cx + w - 1, cy + row, cx + w, cy + row + 1, color);
			if (Math.abs(row) >= hs - 1) {
				gfx.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
			}
		}
	}

	private static boolean isInsideDiamond(double mx, double my, int cx, int cy, int hs) {
		return Math.abs(mx - cx) + Math.abs(my - cy) <= hs;
	}

	// ================================================================
	//  Square
	// ================================================================

	private static void drawSquareOutline(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		gfx.fill(cx - hs, cy - hs, cx + hs, cy - hs + 1, color); // top
		gfx.fill(cx - hs, cy + hs - 1, cx + hs, cy + hs, color); // bottom
		gfx.fill(cx - hs, cy - hs, cx - hs + 1, cy + hs, color); // left
		gfx.fill(cx + hs - 1, cy - hs, cx + hs, cy + hs, color); // right
	}

	private static boolean isInsideSquare(double mx, double my, int cx, int cy, int hs) {
		return mx >= cx - hs && mx <= cx + hs && my >= cy - hs && my <= cy + hs;
	}

	// ================================================================
	//  Circle (pixel-approximated)
	// ================================================================

	private static void drawCircleFill(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		int r2 = hs * hs;
		for (int row = -hs; row <= hs; row++) {
			int maxW = (int) Math.sqrt(r2 - row * row);
			if (maxW <= 0) continue;
			gfx.fill(cx - maxW, cy + row, cx + maxW, cy + row + 1, color);
		}
	}

	private static void drawCircleOutline(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		int r2 = hs * hs;
		int innerR2 = (hs - 1) * (hs - 1);
		for (int row = -hs; row <= hs; row++) {
			int outerW = (int) Math.sqrt(Math.max(0, r2 - row * row));
			int innerW = (int) Math.sqrt(Math.max(0, innerR2 - row * row));
			if (outerW <= 0) continue;
			// left edge
			gfx.fill(cx - outerW, cy + row, cx - innerW, cy + row + 1, color);
			// right edge
			gfx.fill(cx + innerW, cy + row, cx + outerW, cy + row + 1, color);
		}
	}

	private static boolean isInsideCircle(double mx, double my, int cx, int cy, int hs) {
		double dx = mx - cx;
		double dy = my - cy;
		return dx * dx + dy * dy <= (double) hs * hs;
	}

	// ================================================================
	//  Triangle (equilateral, pointing up)
	// ================================================================

	private static void drawTriangleFill(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		// Apex at (cx, cy - hs), base from (cx - hs, cy + hs) to (cx + hs, cy + hs).
		int h = hs * 2; // total height
		for (int row = 0; row <= h; row++) {
			int y = cy - hs + row;
			float progress = (float) row / h;
			int halfW = (int) (hs * progress);
			if (halfW <= 0) {
				halfW = 1;
			}
			gfx.fill(cx - halfW, y, cx + halfW, y + 1, color);
		}
	}

	private static void drawTriangleOutline(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		int h = hs * 2;
		for (int row = 0; row <= h; row++) {
			int y = cy - hs + row;
			float progress = (float) row / h;
			int halfW = Math.max(1, (int) (hs * progress));
			if (row == h) {
				// Bottom edge — full row
				gfx.fill(cx - halfW, y, cx + halfW, y + 1, color);
			} else {
				// Left and right edges
				gfx.fill(cx - halfW, y, cx - halfW + 1, y + 1, color);
				gfx.fill(cx + halfW - 1, y, cx + halfW, y + 1, color);
			}
		}
		// Apex pixel
		gfx.fill(cx, cy - hs, cx + 1, cy - hs + 1, color);
	}

	private static boolean isInsideTriangle(double mx, double my, int cx, int cy, int hs) {
		// Apex (cx, cy-hs), base at cy+hs, half-width = hs at base.
		if (my < cy - hs || my > cy + hs) return false;
		double progress = (my - (cy - hs)) / (double)(hs * 2);
		double halfW = hs * progress;
		return Math.abs(mx - cx) <= halfW;
	}

	// ================================================================
	//  Hexagon (flat-topped)
	// ================================================================

	private static void drawHexagonFill(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		// Flat-topped hex: half-height = hs, half-width at equator = hs,
		// top/bottom flat edges at hs/2 wide.
		for (int row = -hs; row <= hs; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			gfx.fill(cx - halfW, cy + row, cx + halfW, cy + row + 1, color);
		}
	}

	private static void drawHexagonOutline(GuiGraphics gfx, int cx, int cy, int hs, int color) {
		for (int row = -hs; row <= hs; row++) {
			int halfW = hexHalfWidth(row, hs);
			if (halfW <= 0) continue;
			int nextHalfW = (row < hs) ? hexHalfWidth(row + 1, hs) : 0;
			// Edge pixels
			gfx.fill(cx - halfW, cy + row, cx - halfW + 1, cy + row + 1, color);
			gfx.fill(cx + halfW - 1, cy + row, cx + halfW, cy + row + 1, color);
			// Top & bottom flat edges
			if (row == -hs || row == hs) {
				gfx.fill(cx - halfW, cy + row, cx + halfW, cy + row + 1, color);
			}
			// If width changes, fill connecting pixels
			if (halfW != nextHalfW && row != hs) {
				int min = Math.min(halfW, nextHalfW);
				int max = Math.max(halfW, nextHalfW);
				gfx.fill(cx - max, cy + row, cx - min, cy + row + 1, color);
				gfx.fill(cx + min, cy + row, cx + max, cy + row + 1, color);
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
}
