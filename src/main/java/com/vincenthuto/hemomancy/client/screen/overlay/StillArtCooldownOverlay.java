package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * HUD overlay that renders a pale, low-intensity vignette while Still Arts are
 * settling. Unlike manipulation cooldowns, this represents recovering focus
 * rather than bloodshot strain.
 */
public class StillArtCooldownOverlay {

	public static StillArtCooldownOverlay instance;

	private static volatile int cooldownDuration = 0;
	private static volatile int cooldownRemaining = 0;

	public static void startCooldown(int ticks) {
		cooldownDuration = ticks;
		cooldownRemaining = ticks;
	}

	public static boolean isOnCooldown() {
		return cooldownRemaining > 0;
	}

	public static int getCooldownRemaining() {
		return cooldownRemaining;
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
		if (!useCooldownVignette()) {
			renderNumericCooldown(gfx, screenWidth);
			return;
		}

		float progress = (cooldownRemaining - partialTicks) / (float) cooldownDuration;
		progress = Math.max(0.0f, Math.min(1.0f, progress));

		float alpha = progress * 0.28f;
		int edgeSize = (int) (Math.min(screenWidth, screenHeight) * 0.30f);

		int ri = 245;
		int gi = 250;
		int bi = 255;
		int a = (int) (alpha * 255);
		int opaqueColor = (a << 24) | (ri << 16) | (gi << 8) | bi;
		int transparentColor = (ri << 16) | (gi << 8) | bi;

		gfx.fillGradient(0, 0, screenWidth, edgeSize, opaqueColor, transparentColor);
		gfx.fillGradient(0, screenHeight - edgeSize, screenWidth, screenHeight, transparentColor, opaqueColor);

		for (int col = 0; col < edgeSize; col++) {
			float t = 1.0f - (float) col / edgeSize;
			int ca = (int) (a * t);
			int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(col, 0, col + 1, screenHeight, color);
		}

		for (int col = 0; col < edgeSize; col++) {
			float t = (float) col / edgeSize;
			int ca = (int) (a * t);
			int color = (ca << 24) | (ri << 16) | (gi << 8) | bi;
			gfx.fill(screenWidth - edgeSize + col, 0, screenWidth - edgeSize + col + 1, screenHeight, color);
		}
	}

	private static boolean useCooldownVignette() {
		return HemoClientConfig.USE_COOLDOWN_VIGNETTE == null || HemoClientConfig.USE_COOLDOWN_VIGNETTE.get();
	}

	private void renderNumericCooldown(GuiGraphics gfx, int screenWidth) {
		Minecraft mc = Minecraft.getInstance();
		int seconds = Math.max(1, (int) Math.ceil(cooldownRemaining / 20.0));
		Component text = Component.literal(seconds + "s");
		int textWidth = mc.font.width(text);

		boolean gaugeOnLeft = UnstainedGaugeOverlay.isConfiguredOnLeftSide();
		int centerX = UnstainedGaugeOverlay.getGaugeCenterX(screenWidth);
		int centerY = UnstainedGaugeOverlay.getGaugeCenterY();
		int radius = UnstainedGaugeOverlay.getGaugeRadius();
		int x = gaugeOnLeft
				? centerX + radius + 8
				: centerX - radius - textWidth - 8;
		int y = centerY - 4;

		gfx.drawString(mc.font, text, x, y, 0xFFEAF6FF, true);
	}
}
