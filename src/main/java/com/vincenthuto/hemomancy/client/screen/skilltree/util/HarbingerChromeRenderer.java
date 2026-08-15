package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import net.minecraft.client.gui.GuiGraphics;

/** Accent-aware Harbinger frame renderer inspired by the NPC dialogue nine-slice artwork. */
public final class HarbingerChromeRenderer {
	private HarbingerChromeRenderer() {}

	public enum State { IDLE, HOVERED, ACTIVE, DISABLED }

	public static void drawFrame(GuiGraphics gfx, int x, int y, int width, int height,
			int accent, State state) {
		ColoredRectBatch batch = new ColoredRectBatch(gfx);
		drawFrame(batch, x, y, width, height, accent, state);
		batch.flush();
	}

	public static void drawFrame(ColoredRectBatch batch, int x, int y, int width, int height,
			int accent, State state) {
		Palette palette = palette(accent, state);
		HarbingerChromeGeometry.emitFrameSegments(x, y, width, height, (left, top, right, bottom, tone) -> {
			int color = switch (tone) {
				case OUTER -> palette.outer();
				case INNER -> palette.inner();
				case HIGHLIGHT -> palette.highlight();
				case SHADOW -> palette.shadow();
				case STITCH -> palette.stitch();
			};
			batch.fill(left, top, right, bottom, color);
		});
	}

	private static Palette palette(int accent, State state) {
		int base = state == State.DISABLED ? 0xFF555055 : accent;
		float strength = switch (state) {
			case ACTIVE -> 1.0F;
			case HOVERED -> 0.88F;
			case IDLE -> 0.58F;
			case DISABLED -> 0.42F;
		};
		int outer = scale(base, strength);
		int inner = scale(base, strength * 0.45F);
		int highlight = mix(outer, 0xFFFFD69A, state == State.ACTIVE ? 0.42F : 0.22F);
		int shadow = scale(base, strength * 0.28F);
		int stitch = state == State.ACTIVE || state == State.HOVERED ? highlight : outer;
		return new Palette(outer, inner, highlight, shadow, stitch);
	}

	private static int scale(int color, float amount) {
		int alpha = (color >>> 24) & 0xFF;
		int red = Math.round(((color >>> 16) & 0xFF) * amount);
		int green = Math.round(((color >>> 8) & 0xFF) * amount);
		int blue = Math.round((color & 0xFF) * amount);
		return alpha << 24 | clamp(red) << 16 | clamp(green) << 8 | clamp(blue);
	}

	private static int mix(int from, int to, float amount) {
		int alpha = (from >>> 24) & 0xFF;
		int red = mixChannel((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, amount);
		int green = mixChannel((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, amount);
		int blue = mixChannel(from & 0xFF, to & 0xFF, amount);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int mixChannel(int from, int to, float amount) {
		return clamp(Math.round(from + (to - from) * amount));
	}

	private static int clamp(int value) {
		return Math.max(0, Math.min(255, value));
	}

	private record Palette(int outer, int inner, int highlight, int shadow, int stitch) {}
}
