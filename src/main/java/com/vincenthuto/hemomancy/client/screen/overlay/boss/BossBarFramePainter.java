package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;

/**
 * Draws a framed boss bar: the frame's back art, a recessed well, the chip, the gradient fill, notches, the front art,
 * then the name above. As a bar appears the frame unfurls from the middle out and the fill rises after it.
 */
public final class BossBarFramePainter {
	/** Rows a framed bar covers, from the name's top (origin - 8) to the lowest ornament (origin + 18). */
	public static final int SPAN = 26;
	/** Half the widest frame art, for the reveal's clip. */
	private static final int HALF_REACH = 116;

	private BossBarFramePainter() {
	}

	public static void paint(GuiGraphics graphics, BossBarFrame frame, LerpingBossEvent event, int x, int y, float progress,
			float chip, float reveal, float time) {
		int phase = frame.phase(event);
		float open = HemoEasing.smoothstep(HemoEasing.clamp01(reveal / 0.6F));
		float rise = HemoEasing.smoothstep(HemoEasing.clamp01((reveal - 0.45F) / 0.55F));
		int center = x + BossBarFrame.WIDTH / 2;
		int reach = Math.max(1, Math.round(HALF_REACH * open));
		graphics.drawManaged(() -> {
			graphics.enableScissor(center - reach, y - 12, center + reach, y + SPAN);
			GuiCanvas canvas = GuiCanvas.of(graphics);
			BossBarFrameContext c = new BossBarFrameContext(canvas, x, y, progress * rise, Math.max(progress, chip) * rise,
					reveal, time, phase, event);
			frame.paintBack(c);
			well(c, frame);
			fill(c, frame);
			notches(c, frame);
			frame.paintFront(c);
			graphics.disableScissor();
		});
		name(graphics, event.getName(), center, y - 8, frame.nameColor(phase), open);
	}

	private static void well(BossBarFrameContext c, BossBarFrame frame) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 1, c.y() + 2, c.right() + 1, c.y() + 11, frame.wellColor(), frame.wellColor());
		g.rect(c.x(), c.y() + 3, c.right(), c.y() + 4, 0x50000000, 0x00000000);
	}

	private static void fill(BossBarFrameContext c, BossBarFrame frame) {
		GuiCanvas g = c.canvas();
		float top = c.y() + 3, bottom = c.y() + 10;
		float end = c.fillEnd();
		float chipEnd = c.x() + BossBarFrame.WIDTH * c.chip();
		if (chipEnd > end + 0.01F) g.rect(end, top, chipEnd, bottom, frame.chipColor(c.phase()),
				GuiCanvas.mix(frame.chipColor(c.phase()), frame.fillBottom(c.phase()), 0.45F));
		if (end <= c.x() + 0.01F) return;
		int fillTop = frame.fillTop(c.phase()), fillBottom = frame.fillBottom(c.phase());
		g.rect(c.x(), top, end, bottom, fillTop, fillBottom);
		// a slow swell travels along the fill so a held bar still reads as alive
		float swell = c.x() + ((c.time() * 1.6F) % (BossBarFrame.WIDTH + 40.0F)) - 20.0F;
		float s0 = Math.max(c.x(), swell - 20.0F), s1 = Math.min(end, swell + 20.0F);
		if (s1 > s0) {
			float mid = Mth.clamp(swell, s0, s1);
			g.hrect(s0, top, mid, bottom, 0x00FFFFFF, 0x24FFFFFF);
			g.hrect(mid, top, s1, bottom, 0x24FFFFFF, 0x00FFFFFF);
		}
		g.rect(c.x(), top, end, top + 1, 0x38FFFFFF, 0x38FFFFFF);
		g.rect(c.x(), bottom - 1, end, bottom, 0x40000000, 0x40000000);
		g.rect(end - 1, top, end, bottom, 0x60FFFFFF, 0x30FFFFFF);
	}

	private static void notches(BossBarFrameContext c, BossBarFrame frame) {
		for (float notch : frame.notches()) {
			float nx = c.x() + BossBarFrame.WIDTH * notch;
			int color = frame.notchColor(c.phase());
			boolean passed = c.progress() < notch;
			c.canvas().rect(nx - 0.5F, c.y() + 1, nx + 0.5F, c.y() + 12, GuiCanvas.fade(color, passed ? 0.45F : 1.0F),
					GuiCanvas.fade(color, passed ? 0.2F : 0.6F));
		}
	}

	private static void name(GuiGraphics graphics, Component name, int center, int y, int rgb, float alpha) {
		Font font = Minecraft.getInstance().font;
		int a = Math.max(8, Math.round(255 * alpha));
		graphics.drawString(font, name, center - font.width(name) / 2, y, a << 24 | rgb & 0xFFFFFF, true);
	}

	// ------------------------------------------------------------------ shared pieces for frames

	/** A bevelled band around the well: {@code dark} edge, {@code body}, a {@code light} top lip. */
	public static void rails(BossBarFrameContext c, float inset, int dark, int body, int light) {
		GuiCanvas g = c.canvas();
		float x0 = c.x() - 4 - inset, x1 = c.right() + 4 + inset;
		float y0 = c.y(), y1 = c.y() + 13;
		g.rect(x0, y0, x1, y1, dark, dark);
		g.rect(x0 + 1, y0 + 1, x1 - 1, y1 - 1, body, GuiCanvas.mix(body, dark, 0.45F));
		g.rect(x0 + 1, y0 + 1, x1 - 1, y0 + 2, light, light);
	}

	/** A tapered polyline: width runs from {@code w0} to {@code w1} and colour from {@code c0} to {@code c1} along it. */
	public static void stroke(GuiCanvas g, float[] xs, float[] ys, float w0, float w1, int c0, int c1) {
		int n = xs.length - 1;
		for (int i = 0; i < n; i++) {
			float t0 = i / (float) n, t1 = (i + 1) / (float) n;
			g.segment(xs[i], ys[i], xs[i + 1], ys[i + 1], Mth.lerp(t0, w0, w1), Mth.lerp(t1, w0, w1),
					GuiCanvas.mix(c0, c1, t0), GuiCanvas.mix(c0, c1, t1));
		}
	}

	/** The bar name's translation key, or "" for a literal name. */
	public static String nameKey(Component name) {
		return name.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : "";
	}

	/** Mirrors an x about the bar's centre: frames draw one side and mirror the other. */
	public static float mirror(BossBarFrameContext c, float x) {
		return 2.0F * c.centerX() - x;
	}
}
