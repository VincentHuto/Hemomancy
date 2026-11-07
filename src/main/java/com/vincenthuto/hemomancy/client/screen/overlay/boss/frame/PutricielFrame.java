package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * Saint Putriciel, the Befouled Saint: mossed rails sagging with rot, and five fungal nodules on the lower rail at 20,
 * 40, 60, 80 and 100% that light as the absolutions are taken (design section 12.6). The fill follows the phase (olive,
 * mould green, ochre; crimson with a pink-white top in the fire). In the cleansing interval the rails char and the lit
 * nodules and the end blooms' hearts burn crimson with pink-white centres; the ember colour is used nowhere else, so
 * the rot shows no red. The phase is read off the bar's colour and name.
 */
public final class PutricielFrame implements BossBarFrame {
	private static final int MOSS_DARK = 0xFF1A2210, MOSS = 0xFF3A4A22, MOSS_LIP = 0xFF6A7A3A;
	private static final int CHAR_DARK = 0xFF141210, CHAR = 0xFF2A2622, CHAR_LIP = 0xFF4A443C;
	private static final int ROT = 0x8A9A3A, PETAL = 0xB8A86A, PETAL_DARK = 0x5A4A2A, PETAL_TIP = 0xE8D8A0;
	private static final int NODULE_BASE = 0x6A6A3A, NODULE_CAP = 0xE3CC8B, ABSOLVED = 0xF4ECCB;
	private static final int FLAME = 0xD61E2E, HEART = 0xFFA8BE, CREAM_HEART = 0xE8D8A0;
	private static final float[] NOTCHES = {0.2F, 0.4F, 0.6F, 0.8F};

	@Override
	public float[] notches() {
		return NOTCHES;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		if (event.getColor() == BossEvent.BossBarColor.PINK) return PutricielPhase.INTERVAL.number();
		if (event.getColor() == BossEvent.BossBarColor.YELLOW) return PutricielPhase.ROT_III.number();
		if (event.getName().getContents() instanceof TranslatableContents name
				&& PutricielPhase.ROT_II.nameKey().equals(name.getKey())) return PutricielPhase.ROT_II.number();
		return PutricielPhase.ROT_I.number();
	}

	@Override
	public int fillTop(int phase) {
		return switch (PutricielPhase.byNumber(phase)) {
			case INTERVAL -> 0xFFFFA8BE;
			case ROT_II -> 0xFF9AC44A;
			case ROT_III -> 0xFFC8B45A;
			default -> 0xFF8A9A3A;
		};
	}

	@Override
	public int fillBottom(int phase) {
		return switch (PutricielPhase.byNumber(phase)) {
			case INTERVAL -> 0xFFD61E2E;
			case ROT_II -> 0xFF3E5A1A;
			case ROT_III -> 0xFF6A5A22;
			default -> 0xFF3E4A1A;
		};
	}

	@Override
	public int nameColor(int phase) {
		return 0xD8D8A8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		boolean fire = c.phase() == PutricielPhase.INTERVAL.number();
		if (fire) BossBarFramePainter.rails(c, 0, CHAR_DARK, CHAR, CHAR_LIP);
		else BossBarFramePainter.rails(c, 0, MOSS_DARK, MOSS, MOSS_LIP);
		for (int i = 0; i < 16; i++) {
			float mx = c.x() - 4 + GuiCanvas.hash(43, i, 0) * (BossBarFrame.WIDTH + 8);
			float my = GuiCanvas.hash(43, i, 1) > 0.5F ? c.y() + 0.5F : c.y() + 12.5F;
			int clump = fire ? 0x2A2622 : ROT, under = fire ? 0x141210 : 0x4A5A22;
			g.disc(mx, my, 2.0F + GuiCanvas.hash(43, i, 2) * 1.5F, 1.2F, 10, GuiCanvas.argb(0.9F, clump), GuiCanvas.argb(0.6F, under));
		}
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		boolean fire = c.phase() == PutricielPhase.INTERVAL.number();
		int lit = Math.round(c.event().getProgress() * PutricielAbsolutionRules.NEEDED);
		for (int i = 0; i < PutricielAbsolutionRules.NEEDED; i++) {
			float x = c.x() + BossBarFrame.WIDTH * (i + 1) / (float) PutricielAbsolutionRules.NEEDED;
			nodule(c, Math.min(x, c.right() - 2.5F), c.y() + 13.0F, i < lit, fire);
		}
		float open = c.event().getProgress();
		bloom(c, c.x() - 10.0F, c.midY(), 6.5F, open, fire);
		bloom(c, c.right() + 10.0F, c.midY(), 6.5F, open, fire);
	}

	/** One of the five nodules: olive and cream unlit; absolved, a cream-white glow, crimson only in the fire. */
	private static void nodule(BossBarFrameContext c, float x, float y, boolean lit, boolean fire) {
		GuiCanvas g = c.canvas();
		g.disc(x, y, 2.6F, 2.2F, 10, GuiCanvas.argb(1.0F, NODULE_BASE), GuiCanvas.argb(1.0F, 0x2A2A14));
		if (!lit) {
			g.disc(x, y - .6F, 1.6F, 1.1F, 8, GuiCanvas.argb(.9F, NODULE_CAP), GuiCanvas.argb(.6F, NODULE_BASE));
			return;
		}
		float glow = .8F + .2F * Mth.sin(c.time() * .12F + x);
		if (fire) {
			g.disc(x, y, 3.4F, 3.0F, 12, GuiCanvas.argb(glow, HEART), GuiCanvas.argb(0.0F, FLAME));
			g.disc(x, y, 1.8F, 1.6F, 10, GuiCanvas.argb(1.0F, HEART), GuiCanvas.argb(.9F, FLAME));
		} else {
			g.disc(x, y, 3.4F, 3.0F, 12, GuiCanvas.argb(glow * .7F, ABSOLVED), GuiCanvas.argb(0.0F, ABSOLVED));
			g.disc(x, y, 1.8F, 1.6F, 10, GuiCanvas.argb(1.0F, ABSOLVED), GuiCanvas.argb(.8F, NODULE_CAP));
		}
	}

	/** A rot bloom whose petals spread with the absolutions; its heart cream in the rot, crimson only in the fire. */
	private static void bloom(BossBarFrameContext c, float cx, float cy, float size, float open, boolean fire) {
		GuiCanvas g = c.canvas();
		int petals = 6;
		float spread = 0.25F + open * 0.75F;
		float sway = Mth.sin(c.time() * 0.03F + cx) * 0.08F;
		int edgeColor = fire ? 0x4A443C : PETAL, baseColor = fire ? 0x1A1612 : PETAL_DARK, tipColor = fire ? 0x6A6058 : PETAL_TIP;
		for (int p = 0; p < petals; p++) {
			float a = Mth.TWO_PI * p / petals + sway - Mth.HALF_PI;
			float len = size * (0.55F + spread * 0.45F);
			float side = 0.55F * spread + 0.15F;
			float tx = cx + Mth.cos(a) * len, ty = cy + Mth.sin(a) * len;
			float lx = cx + Mth.cos(a - side) * len * 0.55F, ly = cy + Mth.sin(a - side) * len * 0.55F;
			float rx = cx + Mth.cos(a + side) * len * 0.55F, ry = cy + Mth.sin(a + side) * len * 0.55F;
			int edge = GuiCanvas.argb(1.0F, edgeColor), base = GuiCanvas.argb(1.0F, baseColor);
			g.quad(cx, cy, base, lx, ly, edge, tx, ty, GuiCanvas.argb(1.0F, tipColor), rx, ry, edge);
		}
		float heart = open >= 0.999F ? 0.8F + Mth.sin(c.time() * 0.15F) * 0.2F : 0.25F + open * 0.4F;
		if (fire) {
			g.disc(cx, cy, size * 0.32F, size * 0.32F, 10, GuiCanvas.argb(heart, HEART), GuiCanvas.argb(heart * 0.6F, FLAME));
		} else {
			g.disc(cx, cy, size * 0.32F, size * 0.32F, 10, GuiCanvas.argb(heart, CREAM_HEART), GuiCanvas.argb(heart * 0.5F, 0x6A6A3A));
		}
	}
}
