package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Saint Hemorath, the Hollow Vessel: riveted hematic-iron rails around a well drained to bare ash, with an empty urn at
 * each end. When the vessel empties (phase 2) the iron tarnishes violet and the urns crack.
 */
public final class HemorathFrame implements BossBarFrame {
	private static final String EMPTIES_KEY = "message.hemomancy.hemorath.saint_hemorath_the_vessel_empties";
	private static final int IRON_DARK = 0xFF141012, IRON = 0xFF3A3236, IRON_LIP = 0xFF6A5A5E;
	private static final int TARNISH_DARK = 0xFF140A1C, TARNISH = 0xFF3A2448, TARNISH_LIP = 0xFF6A4A80;
	private static final int RUST = 0x7A2A1A;

	public static int phaseOf(Component name) {
		return BossBarFramePainter.nameKey(name).equals(EMPTIES_KEY) ? 2 : 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getName());
	}

	@Override
	public float[] notches() {
		return new float[] { 0.5F };
	}

	@Override
	public int fillTop(int phase) {
		return phase >= 2 ? 0xFF8A3AA8 : 0xFFB0202A;
	}

	@Override
	public int fillBottom(int phase) {
		return phase >= 2 ? 0xFF2A0C3A : 0xFF4A0A12;
	}

	@Override
	public int wellColor() {
		return 0xF04A4442;
	}

	@Override
	public int nameColor(int phase) {
		return phase >= 2 ? 0xC8A8E0 : 0xD8CCC8;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		boolean emptied = c.phase() >= 2;
		BossBarFramePainter.rails(c, 1, emptied ? TARNISH_DARK : IRON_DARK, emptied ? TARNISH : IRON,
				emptied ? TARNISH_LIP : IRON_LIP);
		for (float rx = c.x() + 4; rx < c.right() - 2; rx += 16.0F) {
			for (int row = 0; row < 2; row++) {
				float ry = row == 0 ? c.y() + 1.0F : c.y() + 12.0F;
				g.disc(rx, ry, 0.9F, 0.9F, 6, emptied ? 0xFF9A7AB8 : 0xFF8A7A7E, 0xFF201A1C);
			}
		}
		for (int i = 0; i < 5; i++) {
			float sx = c.x() + GuiCanvas.hash(41, i, 0) * BossBarFrame.WIDTH;
			g.rect(sx, c.y() + 12.0F, sx + 1.0F, c.y() + 13.0F + GuiCanvas.hash(41, i, 1) * 2.5F,
					GuiCanvas.argb(0.7F, RUST), GuiCanvas.argb(0.0F, RUST));
		}
		urn(c, c.x() - 10.0F, emptied);
		urn(c, c.right() + 10.0F, emptied);
	}

	/** An empty urn: a dark mouth, iron body, nothing inside. */
	private static void urn(BossBarFrameContext c, float cx, boolean cracked) {
		GuiCanvas g = c.canvas();
		float cy = c.midY() + 1.0F;
		int body = cracked ? TARNISH : IRON, dark = cracked ? TARNISH_DARK : IRON_DARK;
		g.disc(cx, cy, 6.2F, 6.8F, 18, body, dark);
		g.rect(cx - 3.0F, cy - 9.0F, cx + 3.0F, cy - 5.0F, body, dark);
		g.disc(cx, cy - 9.0F, 3.6F, 1.2F, 12, 0xFF050304, 0xFF050304);
		g.disc(cx - 2.0F, cy - 1.5F, 1.2F, 2.6F, 8, 0x40FFFFFF, 0x00FFFFFF);
		if (cracked) {
			float flicker = 0.6F + Mth.sin(c.time() * 0.2F + cx) * 0.2F;
			g.segment(cx + 1.0F, cy - 6.0F, cx - 1.5F, cy, 0.8F, 0.6F, GuiCanvas.argb(flicker, 0xB080E0));
			g.segment(cx - 1.5F, cy, cx + 1.5F, cy + 5.0F, 0.6F, 0.2F, GuiCanvas.argb(flicker, 0xB080E0));
		}
	}
}
