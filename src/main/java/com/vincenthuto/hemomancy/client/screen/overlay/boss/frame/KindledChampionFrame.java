package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules;
import net.minecraft.util.Mth;

/**
 * The Kindled Champion: a pale polyp head at each end of the bar, crowned in flame-coloured tentacles that run amber at
 * the root through orange to crimson at the tips, with more tentacles licking up along the top rail like a fire's edge.
 * The notches are the thresholds where it buds; past each one the crowns carry another ring of buds and burn higher.
 */
public final class KindledChampionFrame implements BossBarFrame {
	private static final int AMBER = 0xFFE4982C, FLAME = 0xFFEE621E, CRIMSON = 0xFFB01A1E, POLYP = 0xFFE8D6CE, VESSEL = 0xFF8A1C22;

	@Override
	public float[] notches() {
		float[] thresholds = KindledChampionRules.BUD_THRESHOLDS;
		return new float[] {thresholds[1], thresholds[0]};
	}

	@Override
	public int fillTop(int phase) {
		return 0xFFFFA048;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFFB02418;
	}

	@Override
	public int nameColor(int phase) {
		return 0xFFD8B0;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 3, c.y() + 1, c.right() + 3, c.y() + 12, 0xFF2A1612, 0xFF140A08);
		g.rect(c.x() - 3, c.y() + 11, c.right() + 3, c.y() + 12.5F, VESSEL, VESSEL);
		int stage = KindledChampionRules.budStage(c.progress());
		int count = 18 + stage * 6;
		float span = BossBarFrame.WIDTH + 4.0F;
		for (int i = 0; i < count; i++) {
			float t = (i + 0.5F) / count;
			float x = c.x() - 2 + span * t;
			float flicker = Mth.sin(c.time() * 0.16F + i * 1.7F);
			float height = 3.0F + stage * 1.2F + GuiCanvas.hash(83, i, stage) * 3.0F + flicker * 0.8F;
			tentacle(g, x, c.y() + 1.0F, x + flicker * 1.2F, c.y() + 1.0F - height, 1.3F);
		}
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		int stage = KindledChampionRules.budStage(c.progress());
		head(c, c.x() - 10.0F, stage);
		head(c, c.right() + 10.0F, stage);
	}

	private static void tentacle(GuiCanvas g, float x0, float y0, float x1, float y1, float width) {
		float mx = Mth.lerp(0.55F, x0, x1), my = Mth.lerp(0.55F, y0, y1);
		g.segment(x0, y0, mx, my, width, width * 0.7F, AMBER, FLAME);
		g.segment(mx, my, x1, y1, width * 0.7F, 0.2F, FLAME, CRIMSON);
	}

	/** A polyp head ringed by its crown; each bud stage adds four tentacles and lengthens them all. */
	private static void head(BossBarFrameContext c, float cx, int stage) {
		GuiCanvas g = c.canvas();
		float cy = c.midY();
		int crown = KindledChampionRules.crownCount(stage);
		float glow = 0.35F + stage * 0.2F + Mth.sin(c.time() * 0.12F + cx) * 0.06F;
		g.disc(cx, cy, 10.0F, 10.0F, 20, GuiCanvas.argb(glow * 0.4F, FLAME), GuiCanvas.argb(0.0F, FLAME));
		for (int i = 0; i < crown; i++) {
			float a = Mth.TWO_PI * i / crown + Mth.sin(c.time() * 0.05F + i) * 0.08F;
			float len = 6.0F + stage * 1.2F + (i % 2) * 1.5F;
			tentacle(g, cx + Mth.cos(a) * 2.4F, cy + Mth.sin(a) * 2.4F, cx + Mth.cos(a) * len, cy + Mth.sin(a) * len, 1.2F);
		}
		g.disc(cx, cy, 3.2F, 3.2F, 14, POLYP, 0xFFC8B0A8);
		g.disc(cx, cy, 1.2F, 1.2F, 10, VESSEL, VESSEL);
	}
}
