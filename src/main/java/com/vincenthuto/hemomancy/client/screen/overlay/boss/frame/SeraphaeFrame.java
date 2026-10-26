package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.util.Mth;

/**
 * Saint Seraphae, the Bound Radiance: a radiant core at each end held by chains of dark hematic iron along both rails.
 * Her bar is the containment's integrity; as it falls the chains sag and links part, and the cores burn brighter.
 */
public final class SeraphaeFrame implements BossBarFrame {
	private static final int LUX = 0xFFF0B0, CHAIN = 0xFF2E2628, CHAIN_LIP = 0xFF5A4E50;

	@Override
	public int fillTop(int phase) {
		return 0xFFFFE8A0;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFFC08A30;
	}

	@Override
	public int nameColor(int phase) {
		return 0xFFF4D8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		c.canvas().rect(c.x() - 3, c.y() + 1, c.right() + 3, c.y() + 12, 0xFF120E0E, 0xFF0A0808);
		float loose = 1.0F - c.event().getProgress();
		chain(c, c.y() + 1.0F, loose, 0);
		chain(c, c.y() + 12.0F, loose, 1);
		core(c, c.x() - 10.0F, loose);
		core(c, c.right() + 10.0F, loose);
	}

	/** Oval links along a rail; looser containment sags them and opens gaps. */
	private static void chain(BossBarFrameContext c, float railY, float loose, int row) {
		GuiCanvas g = c.canvas();
		int links = 26;
		float span = BossBarFrame.WIDTH + 8.0F;
		for (int i = 0; i < links; i++) {
			if (GuiCanvas.hash(59, i, row) < loose * 0.45F) continue;
			float t = (i + 0.5F) / links;
			float lx = c.x() - 4 + span * t;
			float sag = Mth.sin(t * Mth.PI) * loose * 4.0F * (row == 0 ? -1.0F : 1.0F);
			float sway = Mth.sin(c.time() * 0.05F + i * 0.7F) * loose * 0.8F;
			float ly = railY + sag + sway;
			boolean flat = i % 2 == 0;
			float rx = flat ? 3.4F : 1.6F, ry = flat ? 1.6F : 1.2F;
			g.disc(lx, ly, rx + 0.6F, ry + 0.6F, 12, CHAIN_LIP, CHAIN);
			g.disc(lx, ly, rx - 0.7F, Math.max(0.3F, ry - 0.7F), 10, 0xFF0A0808, 0xFF0A0808);
		}
	}

	/** A bound core of light with soft rays, brighter as it slips its bindings. */
	private static void core(BossBarFrameContext c, float cx, float loose) {
		GuiCanvas g = c.canvas();
		float cy = c.midY();
		float burn = 0.55F + loose * 0.45F + Mth.sin(c.time() * 0.11F + cx) * 0.08F;
		g.disc(cx, cy, 9.0F, 9.0F, 20, GuiCanvas.argb(burn * 0.35F, LUX), GuiCanvas.argb(0.0F, LUX));
		for (int r = 0; r < 8; r++) {
			float a = Mth.TWO_PI * r / 8 + c.time() * 0.01F;
			float len = 6.0F + (r % 2) * 3.0F + loose * 3.0F;
			g.segment(cx, cy, cx + Mth.cos(a) * len, cy + Mth.sin(a) * len, 1.0F, 0.1F, GuiCanvas.argb(burn * 0.8F, LUX),
					GuiCanvas.argb(0.0F, LUX));
		}
		g.disc(cx, cy, 3.2F, 3.2F, 14, GuiCanvas.argb(1.0F, 0xFFFFFF), GuiCanvas.argb(burn, LUX));
		g.segment(cx - 5.0F, cy - 4.0F, cx + 5.0F, cy + 4.0F, 1.2F, 1.2F, CHAIN);
		g.segment(cx - 5.0F, cy + 4.0F, cx + 5.0F, cy - 4.0F, 1.2F, 1.2F, CHAIN);
	}
}
