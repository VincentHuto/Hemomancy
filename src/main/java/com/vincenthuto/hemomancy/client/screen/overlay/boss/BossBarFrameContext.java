package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import net.minecraft.client.gui.components.LerpingBossEvent;

/**
 * One framed bar being drawn. {@code x}, {@code y} are vanilla's bar origin; the fill runs {@link BossBarFrame#WIDTH}
 * across from {@code x} at rows {@code y + 3} to {@code y + 10}. {@code reveal} runs 0 to 1 as the bar appears, and
 * {@code time} is client ticks plus the partial tick, for idle motion.
 */
public record BossBarFrameContext(GuiCanvas canvas, int x, int y, float progress, float chip, float reveal, float time,
		int phase, LerpingBossEvent event) {
	public float centerX() {
		return x + BossBarFrame.WIDTH * 0.5F;
	}

	public float right() {
		return x + BossBarFrame.WIDTH;
	}

	/** The fill's vertical middle. */
	public float midY() {
		return y + 6.5F;
	}

	/** Where the fill ends. */
	public float fillEnd() {
		return x + BossBarFrame.WIDTH * progress;
	}
}
