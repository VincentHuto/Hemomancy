package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.util.Mth;

/**
 * Saint Putriciel, the Rotting Saint: mossed rails sagging with rot, and the Bloom of Rot at each end and under the
 * middle. Their bar counts absolutions, and the blooms open as it fills, an ember heart showing at full bloom.
 */
public final class PutricielFrame implements BossBarFrame {
	private static final int MOSS_DARK = 0xFF1A2210, MOSS = 0xFF3A4A22, MOSS_LIP = 0xFF6A7A3A;
	private static final int ROT = 0x8A9A3A, PETAL = 0xB8A86A, PETAL_DARK = 0x5A4A2A, EMBER = 0xFF6A1A;

	@Override
	public int fillTop(int phase) {
		return 0xFF9AC44A;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF3E5A1A;
	}

	@Override
	public int nameColor(int phase) {
		return 0xD8D8A8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		BossBarFramePainter.rails(c, 0, MOSS_DARK, MOSS, MOSS_LIP);
		for (int i = 0; i < 16; i++) {
			float mx = c.x() - 4 + GuiCanvas.hash(43, i, 0) * (BossBarFrame.WIDTH + 8);
			float my = GuiCanvas.hash(43, i, 1) > 0.5F ? c.y() + 0.5F : c.y() + 12.5F;
			g.disc(mx, my, 2.0F + GuiCanvas.hash(43, i, 2) * 1.5F, 1.2F, 10, GuiCanvas.argb(0.9F, ROT),
					GuiCanvas.argb(0.6F, 0x4A5A22));
		}
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		float open = c.event().getProgress();
		bloom(c, c.x() - 10.0F, c.midY(), 6.5F, open);
		bloom(c, c.right() + 10.0F, c.midY(), 6.5F, open);
		bloom(c, c.centerX(), c.y() + 15.0F, 4.0F, open);
	}

	/** A rot bloom whose petals spread from closed (0) to open (1). */
	private static void bloom(BossBarFrameContext c, float cx, float cy, float size, float open) {
		GuiCanvas g = c.canvas();
		int petals = 6;
		float spread = 0.25F + open * 0.75F;
		float sway = Mth.sin(c.time() * 0.03F + cx) * 0.08F;
		for (int p = 0; p < petals; p++) {
			float a = Mth.TWO_PI * p / petals + sway - Mth.HALF_PI;
			float len = size * (0.55F + spread * 0.45F);
			float side = 0.55F * spread + 0.15F;
			float tx = cx + Mth.cos(a) * len, ty = cy + Mth.sin(a) * len;
			float lx = cx + Mth.cos(a - side) * len * 0.55F, ly = cy + Mth.sin(a - side) * len * 0.55F;
			float rx = cx + Mth.cos(a + side) * len * 0.55F, ry = cy + Mth.sin(a + side) * len * 0.55F;
			int edge = GuiCanvas.argb(1.0F, PETAL), base = GuiCanvas.argb(1.0F, PETAL_DARK);
			g.quad(cx, cy, base, lx, ly, edge, tx, ty, GuiCanvas.argb(1.0F, 0xE8D8A0), rx, ry, edge);
		}
		float heart = open >= 0.999F ? 0.8F + Mth.sin(c.time() * 0.15F) * 0.2F : 0.25F + open * 0.4F;
		g.disc(cx, cy, size * 0.32F, size * 0.32F, 10, GuiCanvas.argb(heart, EMBER), GuiCanvas.argb(heart * 0.5F, 0x5A1A0A));
	}
}
