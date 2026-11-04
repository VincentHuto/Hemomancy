package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import net.minecraft.util.Mth;

/**
 * The Ringmaster: the big top's striped canvas runs along both rails in crimson lacquer and bone, pegged down with
 * iron stakes, and a thrown knife hangs point-down at each end. While he keeps to the rafters the bar is a full
 * crimson swag; once he descends it reads his blood, the stripes fray as it falls, and the knives swing.
 */
public final class CircusRingmasterFrame implements BossBarFrame {
	// The pavilion palette: tent shadow, crimson and bone stripes, the iron rail. Rebuilt 2026-11-03 after a cull
	// dropped the multi-declarator line; values follow the sibling frames.
	private static final int SHADOW = 0xFF2A0A0E;
	private static final int CRIMSON_LIT = 0xFFD8323A;
	private static final int BONE = 0xFFE8D8B0;
	private static final int BONE_SHADE = 0xFFB8A478;
	private static final int IRON = 0xFF3A3236;
	private static final int IRON_LIT = 0xFF8A7A7E;

	@Override
	public int fillTop(int phase) {
		return 0xFFE04A4A;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF7A0C18;
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFF4E6C8;
	}

	@Override
	public int nameColor(int phase) {
		return 0xF0DCC0;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 5, c.y(), c.right() + 5, c.y() + 13, SHADOW, 0xFF160608);
		float hold = c.progress();
		canvas(c, c.y() + 0.5F, -1.0F, hold);
		canvas(c, c.y() + 12.5F, 1.0F, hold);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		float hold = c.progress();
		knife(c, c.x() - 8.0F, hold, 0.0F);
		knife(c, c.right() + 8.0F, hold, 1.9F);
	}

	/** Striped canvas scallops along a rail, pegged by stakes; a falling bar frays the scallops and dulls the red. */
	private static void canvas(BossBarFrameContext c, float railY, float side, float hold) {
		GuiCanvas g = c.canvas();
		int count = 16;
		float span = BossBarFrame.WIDTH + 8.0F;
		for (int i = 0; i < count; i++) {
			float t0 = (float) i / count;
			float t1 = (float) (i + 1) / count;
			float x0 = c.x() - 4 + span * t0;
			float x1 = c.x() - 4 + span * t1;
			float sag = Mth.lerp(hold, 1.2F, 3.2F) + Mth.sin(c.time() * 0.03F + i * 1.1F) * 0.3F * (1.0F - hold);
			int stripe = (i & 1) == 0 ? GuiCanvas.mix(0xFF5A1018, CRIMSON_LIT, hold) : GuiCanvas.mix(BONE_SHADE, BONE, hold);
			g.segment(x0, railY, (x0 + x1) * 0.5F, railY + side * sag, 1.6F, 1.0F, stripe, stripe);
			g.segment((x0 + x1) * 0.5F, railY + side * sag, x1, railY, 1.0F, 1.6F, stripe, stripe);
			if (i % 4 == 0) g.segment(x0, railY, x0, railY + side * 2.4F, 1.2F, 0.6F, IRON_LIT, IRON);
		}
	}

	/** A thrown knife hanging point-down at the rail's end; it swings more the lower the bar falls. */
	private static void knife(BossBarFrameContext c, float cx, float hold, float offset) {
		GuiCanvas g = c.canvas();
		float cy = c.midY() - 7.0F;
		float swing = Mth.sin(c.time() * 0.05F + offset) * (0.08F + 0.35F * (1.0F - hold));
		float length = 13.0F;
		float tipX = cx + Mth.sin(swing) * length;
		float tipY = cy + Mth.cos(swing) * length;
		g.segment(cx, cy, cx + Mth.sin(swing) * 4.5F, cy + Mth.cos(swing) * 4.5F, 2.4F, 2.0F, IRON, IRON_LIT);
		g.segment(cx + Mth.sin(swing) * 4.5F, cy + Mth.cos(swing) * 4.5F, tipX, tipY, 2.0F, 0.2F, BONE, BONE_SHADE);
		g.mote(tipX, tipY, 1.1F, GuiCanvas.argb(0.4F + 0.5F * (1.0F - hold), CRIMSON_LIT));
	}
}
