package com.vincenthuto.hemomancy.compat.jei;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

final class JeiProgressArrow {
	private JeiProgressArrow() {
	}

	/** Themed arrow: the track and resting head come from the panel's ring colour, the travelling glint from its arrow colour. */
	static void draw(GuiGraphics gfx, int startX, int y, int tipX, float time, JeiPanel.Theme theme) {
		int lit = theme.arrowLit();
		draw(gfx, startX, y, tipX, time, JeiPanel.withAlpha(theme.ring(), 0xCC), JeiPanel.withAlpha(theme.slotLight(), 0xDD),
				Math.max(0, ((lit >> 16) & 0xFF) - 65), Math.max(0, ((lit >> 8) & 0xFF) - 50), Math.max(0, (lit & 0xFF) - 66));
	}

	static void draw(GuiGraphics gfx, int startX, int y, int tipX, float time,
			int trackColor, int dimHeadColor, int redBase, int greenBase, int blueBase) {
		int shaftEnd = tipX - 5;
		gfx.fill(startX, y, shaftEnd, y + 2, trackColor);
		drawArrowHead(gfx, tipX, y, dimHeadColor);

		float progress = (time * 0.85f) % 1.0f;
		float trail = 0.34f;
		int totalW = Math.max(tipX - startX + 1, 1);
		for (int x = startX; x <= tipX; x++) {
			float xProgress = (float) (x - startX) / totalW;
			float dist = progress - xProgress;
			if (dist < 0f || dist > trail) {
				continue;
			}

			float intensity = 1f - dist / trail;
			int alpha = (int) (85 + 170 * intensity);
			int red = (int) Mth.clamp(redBase + 65 * intensity, 0, 255);
			int green = (int) Mth.clamp(greenBase + 50 * intensity, 0, 255);
			int blue = (int) Mth.clamp(blueBase + 66 * intensity, 0, 255);
			int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

			if (x < shaftEnd) {
				gfx.fill(x, y, x + 1, y + 2, color);
			} else {
				drawArrowHeadColumn(gfx, x, tipX, y, color);
			}
		}
	}

	private static void drawArrowHead(GuiGraphics gfx, int tipX, int y, int color) {
		for (int x = tipX - 5; x <= tipX; x++) {
			drawArrowHeadColumn(gfx, x, tipX, y, color);
		}
	}

	private static void drawArrowHeadColumn(GuiGraphics gfx, int x, int tipX, int y, int color) {
		int fromTip = tipX - x;
		if (fromTip < 0 || fromTip > 5) {
			return;
		}
		int halfHeight = Math.max(0, fromTip / 2);
		gfx.fill(x, y - halfHeight, x + 1, y + 2 + halfHeight, color);
	}
}
