package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Vesper, the Evening Star: black lacquer rails with a travelling gloss, and a four-rayed white star over the middle
 * whose rays lengthen with each act (hood on, open, sickles). Shamed, the star greys out and the bar becomes the
 * absorption meter, drawn in ice.
 */
public final class VesperEveningStarFrame implements BossBarFrame {
	public static final int SHAMED = 4;
	private static final String SHAMED_KEY = "message.hemomancy.vesper_the_evening_star_entity.vesper_the_shamed_evening_star";
	private static final int ICE = 0x5CCAF5, WHITE = 0xF2F7FF;

	public static int phaseOf(Component name, float progress) {
		if (BossBarFramePainter.nameKey(name).equals(SHAMED_KEY)) return SHAMED;
		if (progress <= 1.0F / 3.0F + 1.0E-6F) return 3;
		if (progress <= 2.0F / 3.0F + 1.0E-6F) return 2;
		return 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getName(), event.getProgress());
	}

	@Override
	public float[] notches() {
		return new float[] { 1.0F / 3.0F, 2.0F / 3.0F };
	}

	@Override
	public int fillTop(int phase) {
		return phase == SHAMED ? 0xFF8ADCFA : 0xFFE00018;
	}

	@Override
	public int fillBottom(int phase) {
		return phase == SHAMED ? 0xFF1A4A70 : 0xFF580012;
	}

	@Override
	public int nameColor(int phase) {
		return phase == SHAMED ? 0xA8A8B8 : WHITE;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		BossBarFramePainter.rails(c, 2, 0xFF030004, 0xFF120A10, 0xFF3A3040);
		float gloss = c.x() - 30 + (c.time() * 0.9F) % (BossBarFrame.WIDTH + 60);
		for (int row = 0; row < 2; row++) {
			float y = row == 0 ? c.y() + 1.0F : c.y() + 11.5F;
			g.hrect(gloss - 18, y, gloss, y + 1.0F, 0x00FFFFFF, 0x40FFFFFF);
			g.hrect(gloss, y, gloss + 18, y + 1.0F, 0x40FFFFFF, 0x00FFFFFF);
		}
		star(c, c.x() - 11.0F, c.midY(), 0.45F, 1);
		star(c, c.right() + 11.0F, c.midY(), 0.45F, 1);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		int act = c.phase() == SHAMED ? 1 : c.phase();
		star(c, c.centerX(), c.midY(), 0.7F + act * 0.3F, c.phase() == SHAMED ? 0 : act);
	}

	/** A four-rayed star. {@code act} 0 is the shamed, greyed star. */
	private static void star(BossBarFrameContext c, float x, float y, float scale, int act) {
		GuiCanvas g = c.canvas();
		boolean shamed = act == 0;
		float twinkle = shamed ? 0.35F : 0.85F + Mth.sin(c.time() * 0.09F + x) * 0.15F;
		int core = GuiCanvas.argb(twinkle, shamed ? 0x8A8A98 : WHITE);
		int halo = GuiCanvas.argb(twinkle * 0.4F, shamed ? 0x606070 : ICE);
		float rayX = (8.0F + act * 6.0F) * scale, rayY = 6.5F * Math.min(scale, 1.1F);
		g.disc(x, y, 5.0F * scale, 5.0F * scale, 16, halo, GuiCanvas.argb(0.0F, ICE));
		g.segment(x - rayX, y, x, y, 0.1F, 1.6F * scale, GuiCanvas.fade(core, 0.0F), core);
		g.segment(x, y, x + rayX, y, 1.6F * scale, 0.1F, core, GuiCanvas.fade(core, 0.0F));
		g.segment(x, y - rayY, x, y, 0.1F, 1.4F * scale, GuiCanvas.fade(core, 0.0F), core);
		g.segment(x, y, x, y + rayY, 1.4F * scale, 0.1F, core, GuiCanvas.fade(core, 0.0F));
		g.disc(x, y, 1.4F * scale, 1.4F * scale, 10, core, GuiCanvas.fade(core, 0.6F));
	}
}
