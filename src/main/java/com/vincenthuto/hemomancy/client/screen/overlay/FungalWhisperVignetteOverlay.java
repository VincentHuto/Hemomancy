package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Full-screen fungal vignette overlay. Ordinary whispers use a short orange
 * flash; the first Fungal Gardens projection uses an accelerating red pulse
 * that warns the player their forced return is approaching.
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
	private static int projectionRemainingTicks = 0;
	private static int projectionTotalTicks = 1;
	private static boolean projectionActive = false;

	public static void setProjectionState(boolean active, int remainingTicks, int totalTicks) {
		projectionActive = active;
		projectionRemainingTicks = Math.max(0, remainingTicks);
		projectionTotalTicks = Math.max(1, totalTicks);
	}

	public static boolean isProjectionActive() {
		return projectionActive;
	}

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
		if (projectionActive && projectionRemainingTicks > 0) projectionRemainingTicks--;
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		if (projectionActive) {
			renderProjectionVignette(gfx, screenWidth, screenHeight, partialTicks);
			return;
		}
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

		// Warm fungal orange
		int ri = 210;
		int gi = 120;
		int bi = 10;

		int a = (int) (alpha * 255);
		int opaqueColor = (a << 24) | (ri << 16) | (gi << 8) | bi;
		int transparentColor = (0 << 24) | (ri << 16) | (gi << 8) | bi;

		int edgeSize = (int) (Math.min(screenWidth, screenHeight) * 0.45f);

		// Top edge — opaque at top, fading to transparent
		gfx.fillGradient(0, 0, screenWidth, edgeSize, opaqueColor, transparentColor);

		// Bottom edge — transparent at top, fading to opaque
		gfx.fillGradient(0, screenHeight - edgeSize, screenWidth, screenHeight, transparentColor, opaqueColor);

		// Left edge — use banded uniform strips to approximate left-to-right fade
		int bands = 8;
		int bandWidth = edgeSize / bands;
		for (int b = 0; b < bands; b++) {
			int x0 = b * bandWidth;
			int x1 = (b + 1) * bandWidth;
			float t = 1.0f - ((float) b + 0.5f) / bands;
			int ba = (int) (a * t);
			int color = (ba << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(x0, 0, x1, screenHeight, color);
		}

		// Right edge — banded uniform strips (last band clamped to screenWidth)
		for (int b = 0; b < bands; b++) {
			int x0 = screenWidth - edgeSize + b * bandWidth;
			int x1 = (b == bands - 1) ? screenWidth : screenWidth - edgeSize + (b + 1) * bandWidth;
			float t = ((float) b + 0.5f) / bands;
			int ba = (int) (a * t);
			int color = (ba << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(x0, 0, x1, screenHeight, color);
		}

		// Corner intensifiers — use a small number of overlapping gradient
		// rectangles to approximate the radial fade, instead of per-pixel fills.
		int cornerSize = (int) (edgeSize * 0.6f);
		int cornerAlpha = (int) (a * 0.5f);
		int cornerBands = 6;
		int cBandSize = cornerSize / cornerBands;

		for (int b = 0; b < cornerBands; b++) {
			int size = cornerSize - b * cBandSize;
			float fade = 1.0f - (float) b / cornerBands;
			int ca = (int) (cornerAlpha * fade * fade);
			if (ca <= 0) continue;
			int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;

			// Top-left
			gfx.fill(0, 0, size, size, color);
			// Top-right
			gfx.fill(screenWidth - size, 0, screenWidth, size, color);
			// Bottom-left
			gfx.fill(0, screenHeight - size, size, screenHeight, color);
			// Bottom-right
			gfx.fill(screenWidth - size, screenHeight - size, screenWidth, screenHeight, color);
		}
	}

	private static void renderProjectionVignette(GuiGraphics gfx, int width, int height, float partialTicks) {
		float progress = 1.0F - Mth.clamp((projectionRemainingTicks - partialTicks) / projectionTotalTicks, 0.0F, 1.0F);
		float period = Mth.lerp(progress, 40.0F, 5.0F);
		float phase = ((projectionTotalTicks - projectionRemainingTicks) + partialTicks) % period / period;
		float pulse = Mth.sin(phase * (float) Math.PI);
		float alpha = Mth.clamp(0.08F + progress * 0.30F + pulse * (0.10F + progress * 0.42F), 0.0F, 0.88F);
		int a = (int) (alpha * 255.0F);
		int red = (a << 24) | (230 << 16) | (20 << 8) | 20;
		int clearRed = (230 << 16) | (20 << 8) | 20;
		int edge = (int) (Math.min(width, height) * (0.24F + progress * 0.18F));
		gfx.fillGradient(0, 0, width, edge, red, clearRed);
		gfx.fillGradient(0, height - edge, width, height, clearRed, red);
		int bands = 10;
		for (int band = 0; band < bands; band++) {
			float strength = 1.0F - (band + 0.5F) / bands;
			int color = ((int) (a * strength) << 24) | (230 << 16) | (20 << 8) | 20;
			int x0 = band * edge / bands;
			int x1 = (band + 1) * edge / bands;
			gfx.fill(x0, 0, x1, height, color);
			gfx.fill(width - x1, 0, width - x0, height, color);
		}
	}
}
