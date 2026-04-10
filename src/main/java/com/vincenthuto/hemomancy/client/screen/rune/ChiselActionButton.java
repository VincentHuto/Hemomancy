package com.vincenthuto.hemomancy.client.screen.rune;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Programmatically rendered action button for the Chisel Station GUI.
 * Matches the Hemomancy blood theme with dark reddish-brown backgrounds,
 * pulsing blood-red borders, and themed hover/press states.
 */
public class ChiselActionButton extends Button {

	// ── Theme colors ──
	private static final int BG_NORMAL = 0xFF1A0808;
	private static final int BG_HOVERED = 0xFF2A0C0C;

	private static final int BORDER_NORMAL = 0xFF440E0E;
	private static final int BORDER_HOVERED = 0xFF882020;
	private static final int BORDER_INNER = 0xFF0D0303;

	/** The icon type to render inside the button. */
	public enum IconType {
		CLEAR, CHISEL, LOAD
	}

	private final IconType iconType;

	public ChiselActionButton(int x, int y, int width, int height, IconType iconType, OnPress action) {
		super(x, y, width, height, Component.empty(), action, DEFAULT_NARRATION);
		this.iconType = iconType;
	}

	@Override
	public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		boolean hovered = this.isHovered();

		int x = this.getX();
		int y = this.getY();
		int w = this.getWidth();
		int h = this.getHeight();

		// Background
		int bg = hovered ? BG_HOVERED : BG_NORMAL;
		gfx.fill(x, y, x + w, y + h, bg);

		// Outer border
		int border = hovered ? BORDER_HOVERED : BORDER_NORMAL;
		gfx.fill(x, y, x + w, y + 1, border);
		gfx.fill(x, y + h - 1, x + w, y + h, border);
		gfx.fill(x, y, x + 1, y + h, border);
		gfx.fill(x + w - 1, y, x + w, y + h, border);

		// Inner border
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);

		// Pulsing accent on hover
		if (hovered) {
			float time = System.nanoTime() / 1_000_000_000f;
			float pulse = 0.4f + 0.6f * ((float) Math.sin(time * 3.0f) * 0.5f + 0.5f);
			int accentAlpha = (int) (40 * pulse);
			int accent = (accentAlpha << 24) | 0xCC2020;
			gfx.fill(x + 2, y + 2, x + w - 2, y + h - 2, accent);
		}

		// Icon
		int iconColor = hovered ? 0xFFDD4444 : 0xFFAA3333;
		int cx = x + w / 2;
		int cy = y + h / 2;

		switch (iconType) {
			case CLEAR -> renderClearIcon(gfx, cx, cy, iconColor);
			case CHISEL -> renderChiselIcon(gfx, cx, cy, iconColor);
			case LOAD -> renderLoadIcon(gfx, cx, cy, iconColor);
		}
	}

	/** Renders an X icon for the clear button. */
	private void renderClearIcon(GuiGraphics gfx, int cx, int cy, int color) {
		// Diagonal X shape, 7x7 area
		for (int i = -3; i <= 3; i++) {
			gfx.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, color);
			gfx.fill(cx - i, cy + i, cx - i + 1, cy + i + 1, color);
		}
		// Thicken center
		gfx.fill(cx - 1, cy, cx + 2, cy + 1, color);
		gfx.fill(cx, cy - 1, cx + 1, cy + 2, color);
	}

	/** Renders a hammer/chisel icon for the craft button. */
	private void renderChiselIcon(GuiGraphics gfx, int cx, int cy, int color) {
		// Chisel blade (angled line from top-left to center)
		for (int i = -3; i <= 1; i++) {
			gfx.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, color);
			gfx.fill(cx + i + 1, cy + i, cx + i + 2, cy + i + 1, color);
		}
		// Handle (vertical line from center down)
		gfx.fill(cx + 1, cy + 1, cx + 3, cy + 5, color);
		// Blade head highlight
		gfx.fill(cx - 4, cy - 3, cx - 2, cy - 1, color);
		int highlight = (color & 0xFF000000) | Math.min(((color >> 16) & 0xFF) + 40, 255) << 16
				| ((color >> 8) & 0xFF) << 8 | (color & 0xFF);
		gfx.fill(cx - 3, cy - 2, cx - 2, cy - 1, highlight);
	}

	/** Renders a downward arrow icon for the load pattern button. */
	private void renderLoadIcon(GuiGraphics gfx, int cx, int cy, int color) {
		// Vertical shaft
		gfx.fill(cx - 1, cy - 4, cx + 2, cy + 1, color);
		// Arrow head (triangle pointing down)
		gfx.fill(cx - 3, cy + 0, cx + 4, cy + 1, color);
		gfx.fill(cx - 2, cy + 1, cx + 3, cy + 2, color);
		gfx.fill(cx - 1, cy + 2, cx + 2, cy + 3, color);
		gfx.fill(cx, cy + 3, cx + 1, cy + 4, color);
	}
}
