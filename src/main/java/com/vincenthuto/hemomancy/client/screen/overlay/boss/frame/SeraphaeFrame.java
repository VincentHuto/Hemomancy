package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaePhases;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;

/**
 * Saint Seraphae, the Chain Saint: a white core at each end held by chains of dark hematic iron along both rails. Her
 * bar is containment integrity, filling ivory white from the left; the chains sag looser while it is low. On a phase
 * change the cores flash and a single gold impulse runs the top rail (the one gold on her frame is a nerve firing).
 */
public final class SeraphaeFrame implements BossBarFrame {
	private static final int LUX = 0xFFF8F2, GOLD = 0xFFE3A8, CHAIN = 0xFF2E2628, CHAIN_LIP = 0xFF5A4E50;
	private static final float FLASH_TICKS = 24.0F;
	private int lastPhase = -1;
	private float changedAt = -1000.0F;

	@Override
	public int fillTop(int phase) {
		return 0xFFFFF8EE;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFFD8CFC0;
	}

	@Override
	public int nameColor(int phase) {
		return 0xFFF8EE;
	}

	/** Her phase, read from the bar's title: the kit renames the bar on each phase change. */
	@Override
	public int phase(LerpingBossEvent event) {
		if (event.getName().getContents() instanceof TranslatableContents translatable) {
			String key = translatable.getKey();
			if (SeraphaePhases.ESCAPING_LIGHT_KEY.equals(key)) return 3;
			if (SeraphaePhases.RESTRAINT_KEY.equals(key)) return 2;
		}
		return 1;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		if (c.phase() != lastPhase) {
			if (lastPhase > 0) changedAt = c.time();
			lastPhase = c.phase();
		}
		float flash = Mth.clamp(1.0F - (c.time() - changedAt) / FLASH_TICKS, 0.0F, 1.0F);
		c.canvas().rect(c.x() - 3, c.y() + 1, c.right() + 3, c.y() + 12, 0xFF120E0E, 0xFF0A0808);
		float loose = 1.0F - c.event().getProgress();
		chain(c, c.y() + 1.0F, loose, 0);
		chain(c, c.y() + 12.0F, loose, 1);
		core(c, c.x() - 10.0F, loose, flash);
		core(c, c.right() + 10.0F, loose, flash);
		if (flash > 0.0F) {
			float t = 1.0F - flash;
			float x = c.x() - 4 + (BossBarFrame.WIDTH + 8.0F) * t;
			c.canvas().disc(x, c.y() + 1.0F, 3.0F, 1.2F, 10, GuiCanvas.argb(flash, GOLD), GuiCanvas.argb(0.0F, GOLD));
		}
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

	/** A bound white core with soft rays, brighter as it slips its bindings, flashing on a phase change. */
	private static void core(BossBarFrameContext c, float cx, float loose, float flash) {
		GuiCanvas g = c.canvas();
		float cy = c.midY();
		float burn = Math.min(1.0F, 0.55F + loose * 0.45F + Mth.sin(c.time() * 0.11F + cx) * 0.08F + flash * 0.4F);
		g.disc(cx, cy, 9.0F + flash * 3.0F, 9.0F + flash * 3.0F, 20, GuiCanvas.argb(burn * 0.35F, LUX), GuiCanvas.argb(0.0F, LUX));
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
