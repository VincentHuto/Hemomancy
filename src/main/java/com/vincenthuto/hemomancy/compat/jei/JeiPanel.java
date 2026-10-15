package com.vincenthuto.hemomancy.compat.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared look for Hemomancy's JEI pages: a bevelled panel with an optional footer strip, recessed slots,
 * a pulsing result frame, small caption labels, pixel icons and a footer stat row with hover tooltips.
 * Every category draws through one {@link Theme} so blood, still-water, fungal and lantern pages read as
 * one family.
 */
final class JeiPanel {
	private JeiPanel() {
	}

	/** Palette for one family of pages. */
	record Theme(int bg, int inner, int frameLight, int frameDark, int slotBg, int slotDark, int slotLight,
			int ring, int accent, int title, int text, int muted, int arrowLit) {
	}

	static final Theme BLOOD = new Theme(0xFF0B0305, 0xFF120507, 0xFF4A1414, 0xFF1C0606, 0xFF1A0709, 0xFF070102,
			0xFF3E1214, 0xFF3A0E12, 0xFFD01C1C, 0xFFE8D0C8, 0xFF9A7A7C, 0xFF6E4A4C, 0xFFE0303A);
	static final Theme STILL = new Theme(0xFF060A10, 0xFF0A1119, 0xFF4C6478, 0xFF141E28, 0xFF0D1720, 0xFF03070B,
			0xFF39505F, 0xFF20364A, 0xFF9FD0EE, 0xFFE4F4FF, 0xFF8EA6B8, 0xFF56697A, 0xFFBFE6FF);
	static final Theme FUNGAL = new Theme(0xFF060904, 0xFF0B1006, 0xFF3A5414, 0xFF141E06, 0xFF101807, 0xFF040702,
			0xFF2F4D12, 0xFF22340C, 0xFFB6D85A, 0xFFE2F2B8, 0xFF8E9E70, 0xFF5E7040, 0xFFC7F03A);
	static final Theme LANTERN = new Theme(0xFF0D0604, 0xFF150A05, 0xFF6A3A10, 0xFF241206, 0xFF201007, 0xFF080403,
			0xFF5B3210, 0xFF3A200A, 0xFFE0A452, 0xFFF6DDB4, 0xFFA48A6A, 0xFF7A5A3A, 0xFFFFC060);

	static final int BLOOD_RED = 0xFFD01C1C;

	static float time() {
		return (Util.getMillis() % 100_000L) / 1000f;
	}

	static float pulse(float time, float speed) {
		return 0.5f + 0.5f * Mth.sin(time * speed);
	}

	// ── Panel ──

	/** Fills the page; a footer strip below {@code footerY} gets its own darker band and divider. */
	static void panel(GuiGraphics gfx, Theme t, int w, int h, int footerY) {
		gfx.fill(0, 0, w, h, t.bg());
		gfx.fill(1, 1, w - 1, footerY > 0 ? footerY : h - 1, t.inner());
		bevel(gfx, 0, 0, w, h, t.frameLight(), t.frameDark());
		if (footerY > 0) gfx.fill(1, footerY, w - 1, footerY + 1, t.frameLight());
	}

	/** Title text with a hairline divider under it. */
	static void title(GuiGraphics gfx, Font font, Theme t, Component text, int x, int y, int maxWidth, int w) {
		trimmed(gfx, font, text, x, y, maxWidth, t.title());
		gfx.fill(4, y + 10, w - 4, y + 11, t.ring());
	}

	static void bevel(GuiGraphics gfx, int x, int y, int w, int h, int light, int dark) {
		gfx.fill(x, y, x + w, y + 1, light);
		gfx.fill(x, y, x + 1, y + h, light);
		gfx.fill(x, y + h - 1, x + w, y + h, dark);
		gfx.fill(x + w - 1, y, x + w, y + h, dark);
	}

	// ── Slots (x, y are the 16x16 JEI slot position) ──

	static void slot(GuiGraphics gfx, Theme t, int x, int y) {
		gfx.fill(x - 1, y - 1, x + 17, y + 17, t.slotBg());
		bevel(gfx, x - 1, y - 1, 18, 18, t.slotDark(), t.slotLight());
	}

	static void tintedSlot(GuiGraphics gfx, Theme t, int x, int y, int color) {
		gfx.fill(x - 2, y - 2, x + 18, y + 18, withAlpha(color, 60));
		gfx.fill(x - 1, y - 1, x + 17, y + 17, t.slotBg());
		bevel(gfx, x - 1, y - 1, 18, 18, darken(color, 0.35f), color);
	}

	/** An unused optional socket: a faint outline with a centre pip. */
	static void ghostSlot(GuiGraphics gfx, Theme t, int x, int y) {
		bevel(gfx, x - 1, y - 1, 18, 18, withAlpha(t.slotLight(), 90), withAlpha(t.slotLight(), 90));
		diamond(gfx, x + 8, y + 8, 1, withAlpha(t.slotLight(), 140));
	}

	/** The result socket: a pulsing coloured halo and bevel around a recessed slot. */
	static void resultSlot(GuiGraphics gfx, Theme t, int x, int y, int color, float time) {
		int glow = withAlpha(color, (int) (40 + 50 * pulse(time, 2.2f)));
		gfx.fill(x - 4, y - 4, x + 20, y + 20, glow);
		bevel(gfx, x - 4, y - 4, 24, 24, color, darken(color, 0.45f));
		slot(gfx, t, x, y);
	}

	// ── Text ──

	static final float SMALL = 0.75f;

	/** Small centred caption; shrinks further if it would exceed {@code maxWidth}. */
	static void label(GuiGraphics gfx, Font font, Component text, int cx, int y, int color, int maxWidth) {
		float s = Math.min(SMALL, maxWidth / (float) Math.max(1, font.width(text)));
		gfx.pose().pushPose();
		gfx.pose().translate(cx, y + (SMALL - s) * 4.5f, 0);
		gfx.pose().scale(s, s, 1f);
		gfx.drawString(font, text, -font.width(text) / 2, 0, color, false);
		gfx.pose().popPose();
	}

	static void label(GuiGraphics gfx, Font font, Component text, int cx, int y, int color) {
		label(gfx, font, text, cx, y, color, 1000);
	}

	static void labelLeft(GuiGraphics gfx, Font font, Component text, int x, int y, int color) {
		gfx.pose().pushPose();
		gfx.pose().translate(x, y, 0);
		gfx.pose().scale(SMALL, SMALL, 1f);
		gfx.drawString(font, text, 0, 0, color, false);
		gfx.pose().popPose();
	}

	/** Word-wrapped small text; returns the height used. */
	static int wrapped(GuiGraphics gfx, Font font, Component text, int x, int y, int width, int color) {
		var lines = font.split(text, (int) (width / SMALL));
		gfx.pose().pushPose();
		gfx.pose().translate(x, y, 0);
		gfx.pose().scale(SMALL, SMALL, 1f);
		for (int i = 0; i < lines.size(); i++) gfx.drawString(font, lines.get(i), 0, i * 10, color, false);
		gfx.pose().popPose();
		return (int) Math.ceil(lines.size() * 10 * SMALL);
	}

	static void trimmed(GuiGraphics gfx, Font font, Component text, int x, int y, int maxWidth, int color) {
		if (font.width(text) <= maxWidth) {
			gfx.drawString(font, text, x, y, color, false);
			return;
		}
		String cut = font.plainSubstrByWidth(text.getString(), maxWidth - font.width("..")) + "..";
		gfx.drawString(font, cut, x, y, color, false);
	}

	// ── Footer stats ──

	enum Icon { BLOOD, TIME, DEGREE, FRAGMENT, XP, HUMOR, NONE }

	record Stat(Icon icon, Component text, int color, Component tooltip) {
		static Stat of(Icon icon, Component text, int color) {
			return new Stat(icon, text, color, null);
		}

		static Stat of(Icon icon, Component text, int color, Component tooltip) {
			return new Stat(icon, text, color, tooltip);
		}
	}

	private record Placed(Stat stat, int x, int w) {
	}

	/** First stat hugs the left, last hugs the right, any middle ones share the gap between. */
	private static List<Placed> layout(Font font, List<Stat> stats, int w) {
		List<Placed> placed = new ArrayList<>();
		if (stats.isEmpty()) return placed;
		int[] widths = stats.stream().mapToInt(s -> font.width(s.text()) + (s.icon() == Icon.NONE ? 0 : 8)).toArray();
		if (stats.size() == 1) {
			placed.add(new Placed(stats.get(0), (w - widths[0]) / 2, widths[0]));
			return placed;
		}
		int total = 0;
		for (int width : widths) total += width;
		int minGap = 10, room = w - 12 - minGap * (stats.size() - 1);
		if (total > room) {
			for (int i = 0; i < widths.length; i++) widths[i] = widths[i] * room / total;
			total = 0;
			for (int width : widths) total += width;
		}
		float gap = (w - 12 - total) / (float) (stats.size() - 1);
		float x = 6;
		for (int i = 0; i < stats.size(); i++) {
			placed.add(new Placed(stats.get(i), Math.round(x), widths[i]));
			x += widths[i] + gap;
		}
		return placed;
	}

	static void footer(GuiGraphics gfx, Font font, List<Stat> stats, int w, int footerY) {
		int y = footerY + 5;
		for (Placed p : layout(font, stats, w)) {
			int x = p.x();
			if (p.stat().icon() != Icon.NONE) {
				icon(gfx, p.stat().icon(), x, y, p.stat().color());
				x += 8;
			}
			int textW = font.width(p.stat().text());
			int room = p.w() - (p.stat().icon() == Icon.NONE ? 0 : 8);
			if (textW <= room) {
				gfx.drawString(font, p.stat().text(), x, y, p.stat().color(), false);
			} else {
				float s = room / (float) textW;
				gfx.pose().pushPose();
				gfx.pose().translate(x, y + (1 - s) * 4f, 0);
				gfx.pose().scale(s, s, 1f);
				gfx.drawString(font, p.stat().text(), 0, 0, p.stat().color(), false);
				gfx.pose().popPose();
			}
		}
	}

	/** Adds the hovered footer stat's tooltip; returns true if the mouse was over the footer. */
	static boolean footerTooltip(ITooltipBuilder tooltip, Font font, List<Stat> stats, int w, int footerY, int h,
			double mx, double my) {
		if (my < footerY || my > h) return false;
		for (Placed p : layout(font, stats, w)) {
			if (mx >= p.x() - 2 && mx <= p.x() + p.w() + 2 && p.stat().tooltip() != null) {
				tooltip.add(p.stat().tooltip().copy().withStyle(ChatFormatting.GRAY));
			}
		}
		return true;
	}

	static Component secondsText(int ticks) {
		float seconds = ticks / 20f;
		return Component.translatable("hemomancy.jei.seconds",
				seconds == Math.floor(seconds) ? String.valueOf((int) seconds) : String.format("%.1f", seconds));
	}

	// ── Pixel icons (8 px tall, drawn at x, y) ──

	static void icon(GuiGraphics gfx, Icon icon, int x, int y, int color) {
		switch (icon) {
			case BLOOD -> drop(gfx, x, y, BLOOD_RED, 0xFFFF9090);
			case HUMOR -> drop(gfx, x, y, 0xFFDDEEF6, 0xFFFFFFFF);
			case TIME -> hourglass(gfx, x, y, color);
			case DEGREE -> {
				diamond(gfx, x + 2, y + 3, 3, darken(color, 0.5f));
				diamond(gfx, x + 2, y + 3, 1, color);
			}
			case FRAGMENT -> diamond(gfx, x + 2, y + 3, 2, color);
			case XP -> {
				diamond(gfx, x + 2, y + 3, 3, 0xFF4A8A1A);
				diamond(gfx, x + 2, y + 3, 1, 0xFFC8FF5A);
			}
			case NONE -> {
			}
		}
	}

	static void drop(GuiGraphics gfx, int x, int y, int color, int shine) {
		gfx.fill(x + 2, y, x + 3, y + 2, color);
		gfx.fill(x + 1, y + 2, x + 4, y + 4, color);
		gfx.fill(x, y + 4, x + 5, y + 7, color);
		gfx.fill(x + 1, y + 7, x + 4, y + 8, color);
		gfx.fill(x + 1, y + 4, x + 2, y + 6, shine);
	}

	static void hourglass(GuiGraphics gfx, int x, int y, int color) {
		gfx.fill(x, y, x + 5, y + 1, color);
		gfx.fill(x, y + 7, x + 5, y + 8, color);
		gfx.fill(x + 1, y + 1, x + 4, y + 3, darken(color, 0.6f));
		gfx.fill(x + 2, y + 3, x + 3, y + 5, color);
		gfx.fill(x + 1, y + 5, x + 4, y + 7, color);
	}

	static void plus(GuiGraphics gfx, int cx, int cy, int color) {
		gfx.fill(cx - 2, cy, cx + 3, cy + 1, color);
		gfx.fill(cx, cy - 2, cx + 1, cy + 3, color);
	}

	/** A small flame of three flickering tongues rising from a 7 px bed at (cx, baseY). */
	static void flame(GuiGraphics gfx, int cx, int baseY, float time, int outer, int mid, int core) {
		gfx.fill(cx - 3, baseY - 2, cx + 4, baseY, outer);
		int[] offsets = { -2, 0, 2 };
		for (int i = 0; i < 3; i++) {
			int h = (i == 1 ? 7 : 4) + Math.round(2 * pulse(time + i * 0.9f, 6f + i));
			int x = cx + offsets[i];
			gfx.fill(x, baseY - h, x + 1, baseY - 1, outer);
			if (i == 1) {
				gfx.fill(x - 1, baseY - h + 3, x + 2, baseY - 1, mid);
				gfx.fill(x, baseY - h + 2, x + 1, baseY - 1, mid);
				gfx.fill(x, baseY - 3, x + 1, baseY - 1, core);
			} else {
				gfx.fill(x, baseY - h + 2, x + 1, baseY - 1, mid);
			}
		}
	}

	/** Heat shimmer: wavering columns of faint motes rising from (cx, baseY) by {@code height}. */
	static void shimmer(GuiGraphics gfx, int cx, int baseY, int height, float time, int color) {
		for (int col = -1; col <= 1; col++) {
			for (int i = 0; i < 3; i++) {
				float p = (time * 0.7f + i / 3f + col * 0.17f) % 1f;
				int y = baseY - Math.round(p * height);
				int x = cx + col * 4 + Math.round(Mth.sin(time * 4f + p * 6f + col) * 1.5f);
				gfx.fill(x, y, x + 1, y + 2, withAlpha(color, (int) (200 * (1f - p))));
			}
		}
	}

	// ── Geometry ──

	/** Pixel-stepped line; crisp at GUI scale and matches the item art. */
	static void line(GuiGraphics gfx, int x0, int y0, int x1, int y1, int color) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		for (int i = 0; i <= steps; i++) {
			float t = steps == 0 ? 0 : (float) i / steps;
			int x = Math.round(Mth.lerp(t, x0, x1));
			int y = Math.round(Mth.lerp(t, y0, y1));
			gfx.fill(x, y, x + 1, y + 1, color);
		}
	}

	static void diamond(GuiGraphics gfx, int cx, int cy, int r, int color) {
		for (int dy = -r; dy <= r; dy++) {
			int half = r - Math.abs(dy);
			gfx.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
		}
	}

	/** A soft elliptical glow: nested translucent ellipses filled row by row, brightest at the centre. */
	static void glow(GuiGraphics gfx, int cx, int cy, int rx, int ry, int color, float strength) {
		for (int layer = 5; layer > 0; layer--) {
			float f = layer / 5f;
			int lrx = Math.max(1, (int) (rx * f)), lry = Math.max(1, (int) (ry * f));
			int alpha = (int) (10 * strength * (1.2f - f) + 2);
			for (int dy = -lry; dy <= lry; dy++) {
				float n = dy / (float) lry;
				int half = (int) (lrx * Math.sqrt(Math.max(0, 1 - n * n)));
				if (half > 0) gfx.fill(cx - half, cy + dy, cx + half, cy + dy + 1, withAlpha(color, alpha));
			}
		}
	}

	static boolean within(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	// ── Colour ──

	/** Lifts dark colours (Mortem, Ferric, Tenebris) so they read on a near-black panel. */
	static int brighten(int rgb) {
		int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
		int max = Math.max(r, Math.max(g, b));
		if (max >= 160) return 0xFF000000 | rgb;
		float lift = max == 0 ? 0 : 160f / max;
		r = Math.min(255, (int) (r * lift) + 40);
		g = Math.min(255, (int) (g * lift) + 40);
		b = Math.min(255, (int) (b * lift) + 40);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	static int darken(int argb, float f) {
		int r = (int) (((argb >> 16) & 0xFF) * f), g = (int) (((argb >> 8) & 0xFF) * f), b = (int) ((argb & 0xFF) * f);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	static int withAlpha(int argb, int alpha) {
		return (Mth.clamp(alpha, 0, 255) << 24) | (argb & 0xFFFFFF);
	}

	static int lerpColor(int a, int b, float t) {
		int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
		int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}
}
