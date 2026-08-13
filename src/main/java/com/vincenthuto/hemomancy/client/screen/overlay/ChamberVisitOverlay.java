package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ChamberVisitOverlay {
	private static boolean active;
	private static ChamberVisitMode mode = ChamberVisitMode.ADMIN;
	private static int remainingTicks;
	private static int totalTicks;

	private ChamberVisitOverlay() {
	}

	public static void setState(boolean isActive, ChamberVisitMode visitMode, int remaining, int total) {
		active = isActive;
		mode = visitMode;
		remainingTicks = Math.max(0, remaining);
		totalTicks = Math.max(0, total);
	}

	public static void tick() {
		if (active && mode.timed() && remainingTicks > 0) remainingTicks--;
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		if (!active || !mode.timed() || totalTicks <= 0) return;
		Minecraft minecraft = Minecraft.getInstance();
		int seconds = Math.max(0, remainingTicks / 20);
		Component text = Component.translatable("overlay.hemomancy.chamber_visit",
				seconds / 60, String.format("%02d", seconds % 60));
		int x = (width - minecraft.font.width(text)) / 2;
		int y = height - 58;
		graphics.fill(x - 5, y - 3, x + minecraft.font.width(text) + 5, y + 11, 0x8809000D);
		graphics.drawString(minecraft.font, text, x, y, 0xFFD8A0B8, true);
	}
}
