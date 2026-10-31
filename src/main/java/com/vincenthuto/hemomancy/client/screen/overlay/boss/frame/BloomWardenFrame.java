package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import net.minecraft.util.Mth;

/**
 * The Bloom Warden: gold Sunburst fronds grow out along both rails from a bloom at each end, over a bed of grey-green
 * moss. The bar is the Warden's hold on the tier; as it falls the fronds fold down and lose their gold, and the blooms
 * close, until it re-anchors.
 */
public final class BloomWardenFrame implements BossBarFrame {
	private static final int MOSS = 0xFF3C4A30, MOSS_LIT = 0xFF6E8A4A, FROND = 0xFF94B446, GOLD = 0xFFFCC448, WILT = 0xFF7A6A44;

	@Override
	public int fillTop(int phase) {
		return 0xFFFFE08A;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFFC88A2A;
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFF4F0C8;
	}

	@Override
	public int nameColor(int phase) {
		return 0xFCE6A8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 4, c.y(), c.right() + 4, c.y() + 13, MOSS_LIT, MOSS);
		float hold = c.progress();
		fronds(c, c.y() + 0.5F, -1.0F, hold);
		fronds(c, c.y() + 12.5F, 1.0F, hold);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		float hold = c.progress();
		bloom(c, c.x() - 9.0F, hold);
		bloom(c, c.right() + 9.0F, hold);
	}

	/** Fronds along a rail, leaning outward; a weaker hold folds them flat and greys them toward the wilted ochre. */
	private static void fronds(BossBarFrameContext c, float railY, float side, float hold) {
		GuiCanvas g = c.canvas();
		int count = 22;
		float span = BossBarFrame.WIDTH + 6.0F;
		for (int i = 0; i < count; i++) {
			float t = (i + 0.5F) / count;
			float fx = c.x() - 3 + span * t;
			float lean = (t - 0.5F) * 1.2F + Mth.sin(c.time() * 0.04F + i * 0.9F) * 0.15F;
			float lift = Mth.lerp(hold, 0.25F, 1.0F) * (3.0F + GuiCanvas.hash(71, i, side > 0 ? 1 : 0) * 3.5F);
			float tipX = fx + lean * lift * 0.8F;
			float tipY = railY + side * lift;
			int color = GuiCanvas.mix(WILT, FROND, hold);
			g.segment(fx, railY, tipX, tipY, 1.4F, 0.3F, color, GuiCanvas.mix(color, GOLD, hold));
			if (i % 3 == 0) g.mote(tipX, tipY, 0.9F, GuiCanvas.argb(0.35F + hold * 0.6F, GOLD));
		}
	}

	/** A Sunburst bloom: gold petals round a pale core, closing as the hold weakens. */
	private static void bloom(BossBarFrameContext c, float cx, float hold) {
		GuiCanvas g = c.canvas();
		float cy = c.midY();
		float open = Mth.lerp(hold, 0.35F, 1.0F);
		g.disc(cx, cy, 8.5F * open, 8.5F * open, 18, GuiCanvas.argb(0.3F * hold, GOLD), GuiCanvas.argb(0.0F, GOLD));
		for (int p = 0; p < 8; p++) {
			float a = Mth.TWO_PI * p / 8 + c.time() * 0.005F;
			float len = (4.0F + (p % 2) * 1.5F) * open;
			g.segment(cx, cy, cx + Mth.cos(a) * len, cy + Mth.sin(a) * len, 2.2F, 0.5F, GOLD, GuiCanvas.mix(WILT, 0xFFFFE8A0, hold));
		}
		g.disc(cx, cy, 2.0F, 2.0F, 12, 0xFFFFF4D0, GuiCanvas.mix(WILT, GOLD, hold));
	}
}
