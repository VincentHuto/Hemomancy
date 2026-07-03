package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class WillPresenceOverlay {
	public static WillPresenceOverlay instance;

	private static final int TOTAL_DURATION = 46;
	private static final int FLASH_TICKS = 6;
	private static final float PEAK_ALPHA = 0.58F;

	private int remainingTicks;

	public void trigger() {
		remainingTicks = TOTAL_DURATION;
	}

	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
	}

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		if (remainingTicks <= 0 || screenWidth <= 0 || screenHeight <= 0) return;
		float elapsed = (TOTAL_DURATION - remainingTicks) + (1.0F - partialTicks);
		float alpha;
		if (elapsed < FLASH_TICKS) {
			alpha = PEAK_ALPHA * (elapsed / FLASH_TICKS);
		} else {
			float t = Mth.clamp((elapsed - FLASH_TICKS) / (TOTAL_DURATION - FLASH_TICKS), 0.0F, 1.0F);
			alpha = PEAK_ALPHA * (1.0F - t * t);
		}
		alpha = Mth.clamp(alpha, 0.0F, PEAK_ALPHA);
		if (alpha <= 0.001F) return;

		int a = (int) (alpha * 255.0F);
		int edgeColor = (a << 24) | (0x5C << 16) | (0x16 << 8) | 0x33;
		int clearColor = (0x00 << 24) | (0x5C << 16) | (0x16 << 8) | 0x33;
		int edge = Math.max(24, (int) (Math.min(screenWidth, screenHeight) * 0.34F));
		graphics.fillGradient(0, 0, screenWidth, edge, edgeColor, clearColor);
		graphics.fillGradient(0, screenHeight - edge, screenWidth, screenHeight, clearColor, edgeColor);
	}
}
