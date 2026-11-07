package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import net.minecraft.util.Mth;

/**
 * The frost Velorum's bar frame and meter share, painted with {@link GuiCanvas}: the palette, the six-armed flake, the
 * crystalline front's teeth and the hematic-iron lantern cage (dark blue-grey with a pale bevel; no silver or brass).
 */
public final class VelorumFrostPaint {
	public static final int FROST_DARK = 0xFF0E1626;
	public static final int FROST = 0xFF2A3E5C;
	public static final int FROST_LIP = 0xFFB8DCF4;
	public static final int ICE = 0x9ACCF0;
	public static final int CORE = 0xE8F4FF;
	public static final int TENEBRIS = 0x140A1E;
	public static final int IRON = 0xFF24262F;
	public static final int IRON_BEVEL = 0xFF5A6078;

	private VelorumFrostPaint() {
	}

	/** A six-armed frost crystal. */
	public static void flake(GuiCanvas g, float x, float y, float size, float turn, int color) {
		for (int arm = 0; arm < 3; arm++) {
			float a = turn + arm * Mth.PI / 3.0F;
			float dx = Mth.cos(a) * size, dy = Mth.sin(a) * size;
			g.segment(x - dx, y - dy, x + dx, y + dy, 0.45F, 0.45F, color);
		}
	}

	public static void flake(GuiCanvas g, float x, float y, float size, float turn) {
		flake(g, x, y, size, turn, 0xE0FFFFFF);
	}

	/** A crystalline front: small alternating teeth along a vertical edge at {@code x}, pointing {@code dir} (+1 or -1). */
	public static void teeth(GuiCanvas g, float x, float top, float bottom, float dir, int color, int seed) {
		float height = bottom - top;
		int count = 3;
		for (int i = 0; i < count; i++) {
			float y0 = top + height * i / count, y1 = top + height * (i + 1) / count;
			float reach = (1.2F + GuiCanvas.hash(seed, i, 0) * 1.4F) * dir;
			g.tri(x, y0, color, x, y1, color, x + reach, (y0 + y1) * 0.5F, GuiCanvas.fade(color, 0.4F));
		}
	}

	/** The small lantern cage capping each end of the meter's vessel: two bars round a crystal dot. */
	public static void cage(GuiCanvas g, float cx, float top, float bottom, float light) {
		g.rect(cx - 2.0F, top - 1.0F, cx + 2.0F, top, IRON_BEVEL, IRON);
		g.rect(cx - 2.0F, bottom, cx + 2.0F, bottom + 1.0F, IRON, IRON_BEVEL);
		g.rect(cx - 2.0F, top, cx - 1.4F, bottom, IRON, IRON);
		g.rect(cx + 1.4F, top, cx + 2.0F, bottom, IRON, IRON);
		g.disc(cx, (top + bottom) * 0.5F, 0.9F, 1.2F, 6, GuiCanvas.argb(0.6F + 0.4F * light, CORE), GuiCanvas.argb(0.3F, ICE));
	}
}
