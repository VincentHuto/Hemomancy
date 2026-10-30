package com.vincenthuto.hemomancy.client.screen.inventory;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.init.CreativeTabSection;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The section banners of the main creative tab. Every banner is the same plate: hematic iron caps at both ends, a
 * bevelled body and a centred title plate. Each section paints its own colours and one motif, drawn outward from
 * the title on both sides so the two halves mirror exactly.
 */
final class CreativeTabBanners {
	private static final int OUTLINE = 0xFF050102;
	private static final int CAP = 0xFF4A171C;
	private static final int CAP_LIGHT = 0xFF8A3A40;
	private static final int RIVET = 0xFFC9B894;
	private static final int CAP_WIDTH = 4;

	/** One section's colours: body, its bevel light and shade, the title plate, the motif, its highlight, the title. */
	private record Palette(int body, int light, int shade, int plate, int motif, int glint, int text) {
	}

	/**
	 * One half of the banner, measured outward from the title: offset 0 sits next to the plate and {@code length}
	 * reaches the end cap.
	 */
	private record Side(GuiGraphics g, int inner, int dir, int length) {
		int x(int offset) {
			return inner + dir * offset;
		}

		/** A run of {@code width} pixels starting {@code offset} out from the title, rows {@code top} to {@code bottom}. */
		void fill(int offset, int width, int top, int bottom, int color) {
			if (offset >= length || width <= 0) {
				return;
			}
			int end = Math.min(offset + width, length) - 1;
			int a = x(offset);
			int b = x(end);
			g.fill(Math.min(a, b), top, Math.max(a, b) + 1, bottom, color);
		}

		void dot(int offset, int y, int color) {
			fill(offset, 1, y, y + 1, color);
		}
	}

	private CreativeTabBanners() {
	}

	private static Palette palette(CreativeTabSection section) {
		return switch (section) {
			case HEMATIC -> new Palette(0xFF5A0E14, 0xFF84202A, 0xFF34060B, 0xFF240407, 0xFFB3262F, 0xFFE8646C, 0xFFF0DCC0);
			case UNSTAINED -> new Palette(0xFF9EA4A6, 0xFFD8DCDA, 0xFF6A7274, 0xFFC4C8C4, 0xFF5C7884, 0xFFEAF2F2, 0xFF202A30);
			case CIRCUS -> new Palette(0xFF7A1018, 0xFFA82830, 0xFF3A060A, 0xFF2A0609, 0xFFE8DCC4, 0xFFFFF4E0, 0xFFF4E6CC);
			case BRINED_COURT -> new Palette(0xFF0E3034, 0xFF24504F, 0xFF061A1C, 0xFF071618, 0xFF5AA0A0, 0xFFD8EEE8, 0xFFDCEEE6);
			case SAINTS -> new Palette(0xFF4A4640, 0xFF6E685E, 0xFF26231F, 0xFF1E1C19, 0xFFD8CCAE, 0xFFFFF6DC, 0xFFEDE4CE);
			case GARDENS -> new Palette(0xFF2A1C26, 0xFF46323F, 0xFF140C12, 0xFF120A10, 0xFF9CAE86, 0xFFD6E4B8, 0xFFE2E8CC);
			case NETHER -> new Palette(0xFF2A0C08, 0xFF4E1A0E, 0xFF140402, 0xFF120403, 0xFFE0702A, 0xFFFFC060, 0xFFFFE2C0);
			case MISC -> new Palette(0xFF2E2A28, 0xFF4A4440, 0xFF161412, 0xFF141210, 0xFF9A8E7C, 0xFFD8CCB4, 0xFFE4DAC8);
			case END -> new Palette(0xFF1C102E, 0xFF34234E, 0xFF0C0618, 0xFF0A0514, 0xFFA884E0, 0xFFF0E6FF, 0xFFEEE4FF);
		};
	}

	static void draw(GuiGraphics g, Font font, CreativeTabSection section, int x, int y, int w, int h, float time) {
		Palette p = palette(section);
		int mid = y + h / 2;

		// Body: outline, fill and a one-pixel bevel.
		g.fill(x, y, x + w, y + h, OUTLINE);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, p.body);
		ScreenDrawUtils.drawBevelBorder(g, x + 1, y + 1, w - 2, h - 2, p.light, p.shade);

		Component title = section.title();
		int textWidth = font.width(title);
		int cx = x + w / 2;
		int plateLeft = cx - (textWidth + 1) / 2 - 4;
		int plateRight = cx + (textWidth + 1) / 2 + 4;

		int capInnerLeft = x + CAP_WIDTH;
		int capInnerRight = x + w - CAP_WIDTH;
		int gap = 3;
		Side left = new Side(g, plateLeft - 1 - gap, -1, plateLeft - gap - capInnerLeft - gap);
		Side right = new Side(g, plateRight + gap, 1, capInnerRight - gap - (plateRight + gap));
		for (Side side : new Side[] { left, right }) {
			drawMotif(section, p, side, y, h, mid, time);
		}

		// Title plate, with the section's colour as an underline.
		g.fill(plateLeft, y + 1, plateRight, y + h - 1, OUTLINE);
		g.fill(plateLeft + 1, y + 2, plateRight - 1, y + h - 2, p.plate);
		g.fill(plateLeft + 2, y + h - 4, plateRight - 2, y + h - 3, p.motif);
		g.drawString(font, title, cx - textWidth / 2, y + 5, p.text, section != CreativeTabSection.UNSTAINED);

		drawCap(g, x, y, h, true);
		drawCap(g, x + w - CAP_WIDTH, y, h, false);
	}

	private static void drawCap(GuiGraphics g, int x, int y, int h, boolean leftEnd) {
		g.fill(x, y, x + CAP_WIDTH, y + h, OUTLINE);
		g.fill(x + 1, y + 1, x + CAP_WIDTH - 1, y + h - 1, CAP);
		int lightX = leftEnd ? x + 1 : x + CAP_WIDTH - 2;
		g.fill(lightX, y + 1, lightX + 1, y + h - 1, CAP_LIGHT);
		int rivetX = leftEnd ? x + 2 : x + 1;
		g.fill(rivetX, y + 3, rivetX + 1, y + 4, RIVET);
		g.fill(rivetX, y + h - 4, rivetX + 1, y + h - 3, RIVET);
	}

	private static void drawMotif(CreativeTabSection section, Palette p, Side s, int y, int h, int mid, float time) {
		if (s.length() < 8) {
			return;
		}
		switch (section) {
			case HEMATIC -> drawVein(p, s, mid);
			case UNSTAINED -> drawStillWater(p, s, mid);
			case CIRCUS -> drawTroupe(p, s, y, h, mid);
			case BRINED_COURT -> drawTide(p, s, mid, time);
			case SAINTS -> drawHalls(p, s, mid);
			case GARDENS -> drawHyphae(p, s, mid, time);
			case NETHER -> drawEmbers(p, s, y, h, time);
			case END -> drawSynapses(p, s, mid, time);
			case MISC -> drawRivets(p, s, mid);
		}
	}

	/** A blood drop by the title, and a vein running out to the cap with paired capillaries swept outward. */
	private static void drawVein(Palette p, Side s, int mid) {
		s.fill(1, 1, mid - 3, mid - 1, p.motif);
		s.fill(0, 3, mid - 1, mid + 2, p.motif);
		s.fill(1, 1, mid + 2, mid + 3, p.motif);
		s.dot(1, mid - 1, p.glint);
		s.fill(5, s.length() - 5, mid, mid + 1, p.motif);
		for (int off = 9; off + 2 < s.length(); off += 8) {
			s.dot(off, mid - 1, p.motif);
			s.dot(off + 1, mid - 2, p.motif);
			s.dot(off, mid + 1, p.motif);
			s.dot(off + 1, mid + 2, p.motif);
		}
	}

	/** A still ring by the title, and ripples spreading from it. */
	private static void drawStillWater(Palette p, Side s, int mid) {
		ring(s, 0, mid - 2, 5, p.motif);
		for (int off = 8; off + 4 < s.length(); off += 8) {
			s.fill(off, 5, mid, mid + 1, p.motif);
			s.fill(off + 1, 3, mid - 2, mid - 1, p.light);
			s.fill(off + 1, 3, mid + 2, mid + 3, p.shade);
		}
	}

	/** A harlequin diamond by the title, then canvas stripes. */
	private static void drawTroupe(Palette p, Side s, int y, int h, int mid) {
		s.fill(2, 1, mid - 2, mid + 3, p.glint);
		s.fill(1, 3, mid - 1, mid + 2, p.glint);
		s.fill(0, 5, mid, mid + 1, p.glint);
		s.dot(2, mid, p.shade);
		for (int off = 8; off + 2 < s.length(); off += 6) {
			s.fill(off, 3, y + 4, y + h - 4, p.motif);
		}
	}

	/** Rolling wave crests. */
	private static void drawTide(Palette p, Side s, int mid, float time) {
		int phase = (int) (time / 8f);
		int[] crest = { 1, 0, -1, -2, -2, -1, 0, 1 };
		for (int off = 0; off < s.length(); off++) {
			int o = crest[Math.floorMod(off - phase, crest.length)];
			s.dot(off, mid + o, p.motif);
			if (o == -2) {
				s.dot(off, mid + o - 1, p.glint);
			}
		}
		for (int off = 2; off < s.length(); off += 8) {
			s.dot(off, mid + 4, p.light);
		}
	}

	/** A halo by the title, and a colonnade of mausoleum arches. */
	private static void drawHalls(Palette p, Side s, int mid) {
		ring(s, 0, mid - 3, 5, p.glint);
		s.dot(2, mid + 3, p.motif);
		s.fill(8, s.length() - 8, mid + 4, mid + 5, p.motif);
		for (int off = 8; off + 5 <= s.length(); off += 7) {
			s.fill(off, 1, mid - 1, mid + 4, p.motif);
			s.fill(off + 4, 1, mid - 1, mid + 4, p.motif);
			s.fill(off + 1, 3, mid - 2, mid - 1, p.motif);
		}
	}

	/** A hypha winding out from the title, with nodes and drifting spores. */
	private static void drawHyphae(Palette p, Side s, int mid, float time) {
		for (int off = 0; off < s.length(); off++) {
			int o = Math.round(Mth.sin(off * 0.42f) * 2f);
			s.dot(off, mid + o, p.motif);
			if (off % 12 == 6) {
				s.fill(off, 2, mid + o - 1, mid + o + 1, p.glint);
			}
		}
		int drift = (int) (time / 10f);
		for (int off = 3; off < s.length(); off += 6) {
			s.dot(off, mid - 5 + Math.floorMod(off * 5 - drift, 4), p.light);
		}
	}

	/** Flame tongues licking up from the lower edge. */
	private static void drawEmbers(Palette p, Side s, int y, int h, float time) {
		int flicker = (int) (time / 3f);
		int base = y + h - 3;
		for (int off = 0; off < s.length(); off++) {
			int height = 2 + Math.floorMod((off * 73_856_093) ^ ((flicker + off / 3) * 19_349_663), 6);
			s.fill(off, 1, base - height, base, p.motif);
			s.dot(off, base - height, p.glint);
		}
	}

	/** Nodes strung on a nerve, a pulse running out from the title. */
	private static void drawSynapses(Palette p, Side s, int mid, float time) {
		s.fill(0, s.length(), mid, mid + 1, p.shade);
		int pulse = (int) (time / 4f);
		for (int off = 2, i = 0; off + 1 < s.length(); off += 8, i++) {
			boolean lit = Math.floorMod(pulse - i, 6) == 0;
			s.fill(off - 1, 3, mid - 1, mid + 2, lit ? p.glint : p.motif);
			s.dot(off, mid, p.glint);
		}
		for (int off = 5; off < s.length(); off += 8) {
			s.dot(off, mid - 4 - (off / 8) % 2, p.light);
		}
	}

	/** A plain iron strap with rivets: the odds and ends. */
	private static void drawRivets(Palette p, Side s, int mid) {
		s.fill(0, s.length(), mid - 2, mid - 1, p.shade);
		s.fill(0, s.length(), mid + 2, mid + 3, p.shade);
		for (int off = 2; off < s.length(); off += 7) {
			s.dot(off, mid - 1, p.glint);
			s.dot(off, mid, p.motif);
		}
	}

	/** A 5 px pixel ring, {@code offset} out from the title. */
	private static void ring(Side s, int offset, int top, int size, int color) {
		s.fill(offset + 1, size - 2, top, top + 1, color);
		s.fill(offset + 1, size - 2, top + size - 1, top + size, color);
		s.fill(offset, 1, top + 1, top + size - 1, color);
		s.fill(offset + size - 1, 1, top + 1, top + size - 1, color);
	}
}
