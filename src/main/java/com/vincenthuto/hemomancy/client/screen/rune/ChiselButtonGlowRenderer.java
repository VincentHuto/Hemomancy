package com.vincenthuto.hemomancy.client.screen.rune;

import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Renders a pulsing red glow border around active ChiselButtons.
 * Uses layered translucent fills via GuiGraphics so positioning is
 * always correct within the GUI coordinate system.
 *
 * The glow is built from multiple concentric rectangles, each one pixel
 * larger and progressively more transparent, creating a soft bloom falloff.
 */
public class ChiselButtonGlowRenderer {

	/** Number of glow layers radiating outward from the button edge. */
	private static final int GLOW_LAYERS = 4;

	/** Base glow color components (deep blood red). */
	private static final int RED = 220;
	private static final int GREEN = 25;
	private static final int BLUE = 20;

	/** Maximum alpha for the innermost glow layer (0–255). */
	private static final int BASE_ALPHA = 160;

	/** Speed of the pulsing animation — higher = faster. */
	private static final float PULSE_SPEED = 3.0f;

	/** Minimum brightness multiplier at the dimmest part of the pulse. */
	private static final float PULSE_MIN = 0.55f;

	/** Preview glow color components (purple). */
	private static final int PREVIEW_RED = 160;
	private static final int PREVIEW_GREEN = 40;
	private static final int PREVIEW_BLUE = 220;
	/**
	 * Renders the glow effect for both confirmed (red) and previewed (purple) runes.
	 *
	 * @param graphics        the GuiGraphics instance
	 * @param runeButtonArray the 8x8 grid of chisel buttons
	 * @param pattern         the 8x8 byte pattern (1 = confirmed / active)
	 * @param preview         the 8x8 byte preview pattern (1 = previewed but not yet confirmed)
	 */
	public static void renderGlowGrid(GuiGraphics graphics, ChiselButton[][] runeButtonArray, byte[][] pattern, byte[][] preview) {
		// Render purple glow for preview-only cells (previewed but not yet confirmed)
		renderGlowGridWithColor(graphics, runeButtonArray, preview, pattern, PREVIEW_RED, PREVIEW_GREEN, PREVIEW_BLUE);
		// Render red glow for confirmed cells
		renderGlowGridWithColor(graphics, runeButtonArray, pattern, null, RED, GREEN, BLUE);
	}

	/**
	 * Renders the glow effect behind all active buttons in the grid.
	 * Call this from renderBg, passing the GuiGraphics directly.
	 *
	 * @param graphics        the GuiGraphics instance from renderBg
	 * @param runeButtonArray the 8x8 grid of chisel buttons
	 * @param pattern         the 8x8 byte pattern (1 = active)
	 */
	public static void renderGlowGrid(GuiGraphics graphics, ChiselButton[][] runeButtonArray, byte[][] pattern) {
		// Pulsing animation driven by game time
		float gameTime = (float) (Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.getGameTime() : 0)
				+ Minecraft.getInstance().getFrameTime();
		float pulse = PULSE_MIN + (1.0f - PULSE_MIN) * ((float) Math.sin(gameTime * PULSE_SPEED * 0.1f) * 0.5f + 0.5f);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (pattern[i][j] != 0) {
					// Check which neighbors are also active (cardinal + diagonal)
					boolean adjUp    = (i > 0) && pattern[i - 1][j] != 0;
					boolean adjDown  = (i < 7) && pattern[i + 1][j] != 0;
					boolean adjLeft  = (j > 0) && pattern[i][j - 1] != 0;
					boolean adjRight = (j < 7) && pattern[i][j + 1] != 0;
					boolean adjUpLeft    = (i > 0 && j > 0) && pattern[i - 1][j - 1] != 0;
					boolean adjUpRight   = (i > 0 && j < 7) && pattern[i - 1][j + 1] != 0;
					boolean adjDownLeft  = (i < 7 && j > 0) && pattern[i + 1][j - 1] != 0;
					boolean adjDownRight = (i < 7 && j < 7) && pattern[i + 1][j + 1] != 0;

					ChiselButton btn = runeButtonArray[i][j];
					renderGlowAroundButton(graphics, btn.getX(), btn.getY(),
							btn.getWidth(), btn.getHeight(), pulse,
							adjUp, adjDown, adjLeft, adjRight,
							adjUpLeft, adjUpRight, adjDownLeft, adjDownRight,
							RED, GREEN, BLUE);
				}
			}
		}
	}

	/**
	 * Renders glow for all active cells in {@code source}, using custom RGB colors.
	 * Cells that are also active in {@code exclude} (if non-null) are skipped.
	 */
	private static void renderGlowGridWithColor(GuiGraphics graphics, ChiselButton[][] runeButtonArray,
			byte[][] source, byte[][] exclude, int r, int g, int b) {
		float gameTime = (float) (Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.getGameTime() : 0)
				+ Minecraft.getInstance().getFrameTime();
		float pulse = PULSE_MIN + (1.0f - PULSE_MIN) * ((float) Math.sin(gameTime * PULSE_SPEED * 0.1f) * 0.5f + 0.5f);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (source[i][j] != 0 && (exclude == null || exclude[i][j] == 0)) {
					boolean adjUp    = (i > 0) && source[i - 1][j] != 0 && (exclude == null || exclude[i - 1][j] == 0);
					boolean adjDown  = (i < 7) && source[i + 1][j] != 0 && (exclude == null || exclude[i + 1][j] == 0);
					boolean adjLeft  = (j > 0) && source[i][j - 1] != 0 && (exclude == null || exclude[i][j - 1] == 0);
					boolean adjRight = (j < 7) && source[i][j + 1] != 0 && (exclude == null || exclude[i][j + 1] == 0);
					boolean adjUpLeft    = (i > 0 && j > 0) && source[i - 1][j - 1] != 0 && (exclude == null || exclude[i - 1][j - 1] == 0);
					boolean adjUpRight   = (i > 0 && j < 7) && source[i - 1][j + 1] != 0 && (exclude == null || exclude[i - 1][j + 1] == 0);
					boolean adjDownLeft  = (i < 7 && j > 0) && source[i + 1][j - 1] != 0 && (exclude == null || exclude[i + 1][j - 1] == 0);
					boolean adjDownRight = (i < 7 && j < 7) && source[i + 1][j + 1] != 0 && (exclude == null || exclude[i + 1][j + 1] == 0);

					ChiselButton btn = runeButtonArray[i][j];
					renderGlowAroundButton(graphics, btn.getX(), btn.getY(),
							btn.getWidth(), btn.getHeight(), pulse,
							adjUp, adjDown, adjLeft, adjRight,
							adjUpLeft, adjUpRight, adjDownLeft, adjDownRight,
							r, g, b);
				}
			}
		}
	}

	/**
	 * Renders concentric glow layers around a single button.
	 * Skips edges where a cardinal neighbor is active, and clamps corners
	 * where a diagonal neighbor is active to prevent overlapping spikes.
	 */
	private static void renderGlowAroundButton(GuiGraphics graphics, int x, int y,
			int width, int height, float pulse,
			boolean adjUp, boolean adjDown, boolean adjLeft, boolean adjRight,
			boolean adjUpLeft, boolean adjUpRight, boolean adjDownLeft, boolean adjDownRight,
			int glowR, int glowG, int glowB) {
		for (int layer = GLOW_LAYERS; layer >= 1; layer--) {
			// Alpha falls off with distance: innermost layer = strongest
			float layerFraction = (float) layer / GLOW_LAYERS;
			int alpha = Mth.clamp((int) (BASE_ALPHA * (1.0f - layerFraction + 0.25f) * pulse), 0, 255);

			int color = (alpha << 24) | (glowR << 16) | (glowG << 8) | glowB;

			int x1 = x - layer;
			int y1 = y - layer;
			int x2 = x + width + layer;
			int y2 = y + height + layer;

			// Top edge: clamp left end if adjLeft OR adjUpLeft, right end if adjRight OR adjUpRight
			int topLeft  = (adjLeft  || adjUpLeft)  ? x : x1;
			int topRight = (adjRight || adjUpRight) ? (x + width) : x2;
			// Bottom edge: clamp similarly with down diagonals
			int botLeft  = (adjLeft  || adjDownLeft)  ? x : x1;
			int botRight = (adjRight || adjDownRight) ? (x + width) : x2;
			// Left edge: clamp top if adjUp OR adjUpLeft, bottom if adjDown OR adjDownLeft
			int leftTop    = (adjUp   || adjUpLeft)   ? y : y1;
			int leftBottom = (adjDown || adjDownLeft)  ? (y + height) : y2;
			// Right edge: clamp with right diagonals
			int rightTop    = (adjUp   || adjUpRight)   ? y : y1;
			int rightBottom = (adjDown || adjDownRight)  ? (y + height) : y2;

			// Top edge (skip if neighbor above)
			if (!adjUp) {
				graphics.fill(topLeft, y1, topRight, y1 + 1, color);
			}
			// Bottom edge (skip if neighbor below)
			if (!adjDown) {
				graphics.fill(botLeft, y2 - 1, botRight, y2, color);
			}
			// Left edge (skip if neighbor to the left)
			if (!adjLeft) {
				graphics.fill(x1, leftTop, x1 + 1, leftBottom, color);
			}
			// Right edge (skip if neighbor to the right)
			if (!adjRight) {
				graphics.fill(x2 - 1, rightTop, x2, rightBottom, color);
			}
		}

		// Inner highlight — a slightly brighter 1px border right against the button
		int innerAlpha = Mth.clamp((int) (BASE_ALPHA * 1.1f * pulse), 0, 255);
		int innerColor = (innerAlpha << 24) | (Math.min(glowR + 35, 255) << 16) | (glowG << 8) | glowB;
		if (!adjUp) {
			graphics.fill(x, y - 1, x + width, y, innerColor);
		}
		if (!adjDown) {
			graphics.fill(x, y + height, x + width, y + height + 1, innerColor);
		}
		if (!adjLeft) {
			graphics.fill(x - 1, y, x, y + height, innerColor);
		}
		if (!adjRight) {
			graphics.fill(x + width, y, x + width + 1, y + height, innerColor);
		}
	}
}

