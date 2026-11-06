package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * The light on a dialogue's special action: a red halo breathing around the button, a hot face and rim, and a bright band
 * sweeping across. Bright on purpose, so the one action that can't be taken back stands apart from every other button.
 */
final class DialogueActionGlow {
	/** How far the halo reaches past the button; {@link DialogueHubLayout} leaves this much room around it. */
	static final int HALO = 6;
	private static final float BREATH = 0.16F;
	private static final int SWEEP_WIDTH = 14;
	private static final int SWEEP_TICKS = 70;
	private static final int HALO_RED = 0xFF2410;
	private static final int FACE_TOP = 0xB01A08;
	private static final int FACE_BOTTOM = 0x6E0A0C;
	private static final int RIM = 0xFF7A3C;
	private static final int RIM_INNER = 0xC42A14;
	private static final int SWEEP = 0xFFC890;

	private DialogueActionGlow() {
	}

	/** Draws the halo behind the button and its lit face, ready for the label. */
	static void draw(GuiGraphics gfx, int x, int y, int width, int height, float time, boolean hot) {
		float breath = 0.5F + 0.5F * Mth.sin(time * BREATH);
		float strength = hot ? 1.0F : 0.6F + 0.4F * breath;
		for (int reach = HALO; reach >= 1; reach--) {
			float falloff = 1.0F - (reach - 1) / (float) HALO;
			int color = argb(strength * falloff * falloff * 0.42F, HALO_RED);
			int corner = Math.min(reach, 2);
			gfx.fill(x - reach, y - reach + corner, x + width + reach, y + height + reach - corner, color);
			gfx.fill(x - reach + corner, y - reach, x + width + reach - corner, y - reach + corner, color);
			gfx.fill(x - reach + corner, y + height + reach - corner, x + width + reach - corner, y + height + reach, color);
		}
		gfx.fillGradient(x, y, x + width, y + height, argb(0.75F + 0.25F * strength, FACE_TOP), argb(0.75F + 0.25F * strength, FACE_BOTTOM));
		sweep(gfx, x + 2, y + 2, width - 4, height - 4, time);
		ScreenDrawUtils.drawRingBorder(gfx, x, y, width, height, argb(0.55F + 0.45F * strength, RIM), argb(0.8F, RIM_INNER));
	}

	/** A soft vertical band crossing the face, clipped to it by hand: a scissor here would cut the halo too. */
	private static void sweep(GuiGraphics gfx, int x, int y, int width, int height, float time) {
		float phase = (time % SWEEP_TICKS) / SWEEP_TICKS;
		int center = x - SWEEP_WIDTH + Math.round(phase * (width + SWEEP_WIDTH * 2));
		for (int step = -SWEEP_WIDTH / 2; step <= SWEEP_WIDTH / 2; step++) {
			int column = center + step;
			if (column < x || column >= x + width) continue;
			float falloff = 1.0F - Math.abs(step) / (SWEEP_WIDTH / 2.0F + 1.0F);
			gfx.fill(column, y, column + 1, y + height, argb(falloff * falloff * 0.38F, SWEEP));
		}
	}

	private static int argb(float alpha, int rgb) {
		return Mth.clamp(Math.round(alpha * 255.0F), 0, 255) << 24 | rgb;
	}
}
