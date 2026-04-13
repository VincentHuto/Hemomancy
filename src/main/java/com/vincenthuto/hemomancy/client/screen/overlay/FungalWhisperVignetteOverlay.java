package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Full-screen blood vignette overlay that flashes briefly when a fungal
 * whisper dialogue is triggered. The effect rapidly flashes in over a few
 * ticks, holds briefly, then fades out smoothly — giving the impression
 * of blood rushing to the edges of the player's vision.
 */
public class FungalWhisperVignetteOverlay {

	public static FungalWhisperVignetteOverlay instance;

	/** Total duration of the vignette effect in ticks (flash in + hold + fade out). */
	private static final int TOTAL_DURATION = 50; // ~2.5 seconds
	/** Ticks for the initial flash-in phase. */
	private static final int FLASH_IN_TICKS = 5;
	/** Ticks to hold at peak intensity after flash-in. */
	private static final int HOLD_TICKS = 10;

	/** Peak alpha at full intensity (0.0–1.0). */
	private static final float PEAK_ALPHA = 0.7f;

	private int remainingTicks = 0;

	/**
	 * Triggers the vignette flash. Can be called multiple times —
	 * resets the timer each time.
	 */
	public void trigger() {
		remainingTicks = TOTAL_DURATION;
	}

	/** Returns true if the vignette is currently active. */
	public boolean isActive() {
		return remainingTicks > 0;
	}

	/** Called once per client tick to count down the effect. */
	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		if (remainingTicks <= 0) {
			return;
		}

		// Calculate which phase we're in and derive alpha
		float elapsed = (TOTAL_DURATION - remainingTicks) + (1.0f - partialTicks);
		float alpha;

		if (elapsed < FLASH_IN_TICKS) {
			// Phase 1: rapid flash in
			float t = elapsed / FLASH_IN_TICKS;
			alpha = PEAK_ALPHA * (1.0f - (1.0f - t) * (1.0f - t));
		} else if (elapsed < FLASH_IN_TICKS + HOLD_TICKS) {
			// Phase 2: hold at peak with slight pulsing
			float holdProgress = (elapsed - FLASH_IN_TICKS) / HOLD_TICKS;
			float pulse = 1.0f - 0.1f * Mth.sin(holdProgress * (float) Math.PI * 2.0f);
			alpha = PEAK_ALPHA * pulse;
		} else {
			// Phase 3: smooth fade out
			float fadeElapsed = elapsed - FLASH_IN_TICKS - HOLD_TICKS;
			float fadeDuration = TOTAL_DURATION - FLASH_IN_TICKS - HOLD_TICKS;
			float t = Math.min(1.0f, fadeElapsed / fadeDuration);
			alpha = PEAK_ALPHA * (1.0f - t * t);
		}

		alpha = Mth.clamp(alpha, 0.0f, 1.0f);
		if (alpha <= 0.001f) return;

		// Deep blood red with a slight dark tint
		int ri = 115; // ~0.45 * 255
		int gi = 0;
		int bi = 5;   // ~0.02 * 255

		int a = (int) (alpha * 255);
		int opaqueColor = (a << 24) | (ri << 16) | (gi << 8) | bi;
		int transparentColor = (0 << 24) | (ri << 16) | (gi << 8) | bi;

		int edgeSize = (int) (Math.min(screenWidth, screenHeight) * 0.45f);

		// Top edge — opaque at top, fading to transparent
		gfx.fillGradient(0, 0, screenWidth, edgeSize, opaqueColor, transparentColor);

		// Bottom edge — transparent at top, fading to opaque
		gfx.fillGradient(0, screenHeight - edgeSize, screenWidth, screenHeight, transparentColor, opaqueColor);

		// Left edge — per-column fill, opaque at left fading right
		for (int col = 0; col < edgeSize; col++) {
			float t = 1.0f - (float) col / edgeSize;
			int ca = (int) (a * t);
			int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(col, 0, col + 1, screenHeight, color);
		}

		// Right edge — per-column fill, opaque at right fading left
		for (int col = 0; col < edgeSize; col++) {
			float t = (float) col / edgeSize;
			int ca = (int) (a * t);
			int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(screenWidth - edgeSize + col, 0, screenWidth - edgeSize + col + 1, screenHeight, color);
		}

		// Corner intensifiers — extra darkening in the four corners
		int cornerSize = (int) (edgeSize * 0.6f);
		int cornerAlpha = (int) (a * 0.5f);

		// Top-left corner
		for (int row = 0; row < cornerSize; row++) {
			float rowFade = 1.0f - (float) row / cornerSize;
			for (int col = 0; col < cornerSize - row; col++) {
				float colFade = 1.0f - (float) col / cornerSize;
				int ca = (int) (cornerAlpha * rowFade * colFade);
				if (ca > 0) {
					int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
					gfx.fill(col, row, col + 1, row + 1, color);
				}
			}
		}

		// Top-right corner
		for (int row = 0; row < cornerSize; row++) {
			float rowFade = 1.0f - (float) row / cornerSize;
			for (int col = 0; col < cornerSize - row; col++) {
				float colFade = 1.0f - (float) col / cornerSize;
				int ca = (int) (cornerAlpha * rowFade * colFade);
				if (ca > 0) {
					int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
					gfx.fill(screenWidth - 1 - col, row, screenWidth - col, row + 1, color);
				}
			}
		}

		// Bottom-left corner
		for (int row = 0; row < cornerSize; row++) {
			float rowFade = 1.0f - (float) row / cornerSize;
			for (int col = 0; col < cornerSize - row; col++) {
				float colFade = 1.0f - (float) col / cornerSize;
				int ca = (int) (cornerAlpha * rowFade * colFade);
				if (ca > 0) {
					int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
					gfx.fill(col, screenHeight - 1 - row, col + 1, screenHeight - row, color);
				}
			}
		}

		// Bottom-right corner
		for (int row = 0; row < cornerSize; row++) {
			float rowFade = 1.0f - (float) row / cornerSize;
			for (int col = 0; col < cornerSize - row; col++) {
				float colFade = 1.0f - (float) col / cornerSize;
				int ca = (int) (cornerAlpha * rowFade * colFade);
				if (ca > 0) {
					int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
					gfx.fill(screenWidth - 1 - col, screenHeight - 1 - row, screenWidth - col, screenHeight - row, color);
				}
			}
		}
	}
}
