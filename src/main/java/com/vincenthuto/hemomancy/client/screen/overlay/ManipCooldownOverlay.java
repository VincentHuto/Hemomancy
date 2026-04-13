package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.client.gui.GuiGraphics;

/**
 * HUD overlay that renders a reddish vignette around the screen edges
 * while any blood manipulation cooldown is active. The vignette fades
 * out as the cooldown expires.
 */
public class ManipCooldownOverlay {

	public static ManipCooldownOverlay instance;

	private static volatile int cooldownDuration = 0;
	private static volatile int cooldownRemaining = 0;

	public static void startCooldown(int ticks) {
		cooldownDuration = ticks;
		cooldownRemaining = ticks;
	}

	public static boolean isOnCooldown() {
		return cooldownRemaining > 0;
	}

	public static void tick() {
		if (cooldownRemaining > 0) {
			cooldownRemaining--;
		}
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		if (cooldownRemaining <= 0 || cooldownDuration <= 0) {
			return;
		}

		float progress = (cooldownRemaining - partialTicks) / (float) cooldownDuration;
		progress = Math.max(0.0f, Math.min(1.0f, progress));

		float alpha = progress * 0.6f;
		int edgeSize = (int) (Math.min(screenWidth, screenHeight) * 0.35f);

		int ri = 153; // 0.6 * 255
		int gi = 0;
		int bi = 0;
		int a = (int) (alpha * 255);
		int opaqueColor = (a << 24) | (ri << 16) | (gi << 8) | bi;
		int transparentColor = (0 << 24) | (ri << 16) | (gi << 8) | bi;

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
	}
}
