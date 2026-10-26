package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.util.Mth;

/**
 * The Latent Infection torn out of Annetta: a bare thin rail with two pale tendrils coiling around it, passing behind
 * and in front of the bar, and spores lifting off it.
 */
public final class LatentInfectionFrame implements BossBarFrame {
	private static final int PALE = 0xD6EAF6, ICHOR = 0xD69630, SPORE = 0xC8AA60;
	private static final int STEPS = 72;

	@Override
	public int fillTop(int phase) {
		return 0xFFE8D2A0;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFFA06A28;
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFFFFFFF;
	}

	@Override
	public int nameColor(int phase) {
		return PALE;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 3, c.y() + 1, c.right() + 3, c.y() + 12, 0xFF1A0E10, 0xFF120A0C);
		g.rect(c.x() - 2, c.y() + 2, c.right() + 2, c.y() + 3, 0xFF3A2A28, 0xFF3A2A28);
		coils(c, false);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		coils(c, true);
		spores(c);
	}

	/** Two tendrils wound round the bar; the halves facing the viewer draw over it, thicker and brighter. */
	private static void coils(BossBarFrameContext c, boolean front) {
		for (int k = 0; k < 2; k++) {
			float phase = k * Mth.PI + c.time() * 0.035F;
			for (int i = 0; i < STEPS; i++) {
				float x0 = c.x() - 10 + (BossBarFrame.WIDTH + 20) * i / (float) STEPS;
				float x1 = c.x() - 10 + (BossBarFrame.WIDTH + 20) * (i + 1) / (float) STEPS;
				float a0 = x0 * 0.085F + phase, a1 = x1 * 0.085F + phase;
				float depth = Mth.cos((a0 + a1) * 0.5F);
				if (depth > 0.0F != front) continue;
				float taper = edgeTaper(c, x0);
				float w = (1.0F + Math.abs(depth) * 0.9F) * taper;
				int color = front ? GuiCanvas.argb(0.95F, GuiCanvas.mix(0xFF000000 | PALE, 0xFF000000 | ICHOR, 0.2F))
						: GuiCanvas.argb(0.55F, 0x8A7A70);
				c.canvas().segment(x0, c.midY() + Mth.sin(a0) * 7.0F, x1, c.midY() + Mth.sin(a1) * 7.0F, w, w, color);
			}
		}
	}

	/** Tendril ends thin out past the bar's ends. */
	private static float edgeTaper(BossBarFrameContext c, float x) {
		float from = Math.min(x - (c.x() - 10), c.right() + 10 - x);
		return Mth.clamp(from / 12.0F, 0.15F, 1.0F);
	}

	private static void spores(BossBarFrameContext c) {
		for (int i = 0; i < 10; i++) {
			float cycle = (c.time() * 0.01F + GuiCanvas.hash(17, i, 0)) % 1.0F;
			float sx = c.x() + GuiCanvas.hash(17, i, 1) * BossBarFrame.WIDTH + Mth.sin(cycle * 6.0F + i) * 2.0F;
			float sy = c.y() + 4.0F - cycle * 14.0F;
			float alpha = Mth.sin(cycle * Mth.PI) * 0.8F;
			c.canvas().mote(sx, sy, 0.8F, GuiCanvas.argb(alpha, SPORE));
		}
	}
}
