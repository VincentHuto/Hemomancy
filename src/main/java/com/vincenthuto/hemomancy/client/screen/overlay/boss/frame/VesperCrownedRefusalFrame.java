package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;

/**
 * Vesper, the Crowned Refusal: rails of the mount's umber-maroon scutes, a thorned crown rising either side of his
 * name, and ice glinting on the plate edges. Each wound the fight opens (72%, 42%, 12%) cracks the scute over its
 * notch, ember showing through.
 */
public final class VesperCrownedRefusalFrame implements BossBarFrame {
	private static final float[] WOUNDS = { 0.12F, 0.42F, 0.72F };
	private static final int SCUTE = 0xFF5A2A22, SCUTE_DARK = 0xFF2A100C, SCUTE_LIP = 0xFF8A4A3A;
	private static final int ICE = 0x5CCAF5, EMBER = 0xFF3E12, BLACK = 0xFF030004;
	private static final float PLATE = 13.0F;

	/** 1, plus one for each wound opened. */
	public static int phaseOf(float progress) {
		int open = 0;
		for (float wound : WOUNDS) if (progress <= wound) open++;
		return 1 + open;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getProgress());
	}

	@Override
	public float[] notches() {
		return WOUNDS.clone();
	}

	@Override
	public int fillTop(int phase) {
		return 0xFFE00018;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF580012;
	}

	@Override
	public int nameColor(int phase) {
		return 0xF2F7FF;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		g.rect(c.x() - 6, c.y(), c.right() + 6, c.y() + 13, BLACK, BLACK);
		for (int row = 0; row < 2; row++) {
			float top = row == 0 ? c.y() : c.y() + 10.5F, bottom = row == 0 ? c.y() + 2.5F : c.y() + 13.0F;
			for (float px = c.x() - 6; px < c.right() + 6; px += PLATE) {
				float x1 = Math.min(px + PLATE + 1.0F, c.right() + 6);
				g.quad(px, top, SCUTE_LIP, px + 1.0F, bottom, SCUTE_DARK, x1, bottom, SCUTE_DARK, x1 - 1.0F, top, SCUTE);
				g.rect(px, top, px + 0.6F, bottom, SCUTE_DARK, SCUTE_DARK);
			}
		}
		crown(c);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		for (float wound : WOUNDS) {
			if (c.progress() > wound) continue;
			float wx = c.x() + BossBarFrame.WIDTH * wound;
			float glow = 0.6F + Mth.sin(c.time() * 0.1F + wound * 20.0F) * 0.25F;
			g.disc(wx, c.y() + 1.5F, 5.0F, 3.0F, 14, GuiCanvas.argb(glow * 0.5F, EMBER), GuiCanvas.argb(0.0F, EMBER));
			g.disc(wx, c.y() + 11.5F, 5.0F, 3.0F, 14, GuiCanvas.argb(glow * 0.5F, EMBER), GuiCanvas.argb(0.0F, EMBER));
			crack(g, wx, c.y(), c.y() + 2.5F, glow);
			crack(g, wx, c.y() + 10.5F, c.y() + 13.0F, glow);
		}
		for (int i = 0; i < 6; i++) {
			float cycle = (c.time() * 0.015F + GuiCanvas.hash(29, i, 0)) % 1.0F;
			if (cycle > 0.25F) continue;
			float sx = c.x() - 6 + Mth.floor(GuiCanvas.hash(29, i, 1) * (BossBarFrame.WIDTH + 12) / PLATE) * PLATE + 1.0F;
			float sy = GuiCanvas.hash(29, i, 2) > 0.5F ? c.y() + 0.5F : c.y() + 11.0F;
			glint(g, sx, sy, Mth.sin(cycle / 0.25F * Mth.PI));
		}
	}

	/** A thorned crown band under the name, thorns rising either side of it. */
	private static void crown(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		float half = Minecraft.getInstance().font.width(c.event().getName()) * 0.5F + 3.0F;
		float cx = c.centerX(), base = c.y() + 0.5F;
		for (int side = -1; side <= 1; side += 2) {
			for (int t = 0; t < 4; t++) {
				float tx = cx + side * (half + t * 4.0F);
				float height = 7.0F - t * 1.4F;
				float lean = side * (1.0F + t * 0.6F);
				g.tri(tx - 1.6F, base, BLACK, tx + 1.6F, base, BLACK, tx + lean, base - height, 0xFF3A1A20);
				g.segment(tx, base - 0.5F, tx + lean * 0.9F, base - height + 0.8F, 0.6F, 0.2F, GuiCanvas.argb(0.7F, ICE));
			}
		}
	}

	private static void crack(GuiCanvas g, float x, float top, float bottom, float glow) {
		int ember = GuiCanvas.argb(0.95F, EMBER);
		g.segment(x - 1.5F, top, x + 0.5F, (top + bottom) * 0.5F, 0.9F, 0.7F, ember);
		g.segment(x + 0.5F, (top + bottom) * 0.5F, x - 0.5F, bottom, 0.7F, 0.4F, GuiCanvas.argb(glow, 0xFFC060));
	}

	/** A four-point ice glint. */
	private static void glint(GuiCanvas g, float x, float y, float alpha) {
		int ice = GuiCanvas.argb(alpha, 0xF2F7FF);
		g.segment(x - 2.2F, y, x + 2.2F, y, 0.6F, 0.6F, ice);
		g.segment(x, y - 2.2F, x, y + 2.2F, 0.6F, 0.6F, ice);
		g.disc(x, y, 1.6F, 1.6F, 8, GuiCanvas.argb(alpha * 0.6F, ICE), GuiCanvas.argb(0.0F, ICE));
	}
}
