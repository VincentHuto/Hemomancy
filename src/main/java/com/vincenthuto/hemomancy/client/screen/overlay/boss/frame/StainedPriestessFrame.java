package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;

/**
 * The Stained Priestess: clotted rails beaded with blood, tendril wings fanning from both ends, and the lancea's blue
 * point under the middle. Below half (her javelin fan) the wings flare wide and the lancea burns.
 */
public final class StainedPriestessFrame implements BossBarFrame {
	private static final int CLOT_DARK = 0xFF3A0408, CLOT = 0xFF60081A, CLOT_LIP = 0xFF96202A;
	private static final int BLOOD = 0x960E1A, GLOW = 0xDC2834, LANCEA = 0x96BEFF, SPORE = 0xC4A050;

	public static int phaseOf(float progress) {
		return progress <= 0.5F ? 2 : 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getProgress());
	}

	@Override
	public float[] notches() {
		return new float[] { 0.5F };
	}

	@Override
	public int fillTop(int phase) {
		return 0xFFDC283A;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF780A18;
	}

	@Override
	public int nameColor(int phase) {
		return phase >= 2 ? 0xF4D6DA : 0xE6C2C2;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		wings(c, c.x() - 4.0F, -1);
		wings(c, c.right() + 4.0F, 1);
		BossBarFramePainter.rails(c, 0, CLOT_DARK, CLOT, CLOT_LIP);
		GuiCanvas g = c.canvas();
		for (int i = 0; i < 14; i++) {
			float bx = c.x() - 2 + GuiCanvas.hash(5, i, 0) * (BossBarFrame.WIDTH + 4);
			float by = GuiCanvas.hash(5, i, 1) > 0.5F ? c.y() + 0.5F : c.y() + 12.5F;
			float r = 1.0F + GuiCanvas.hash(5, i, 2) * 1.1F;
			g.disc(bx, by, r * 1.3F, r, 10, 0xFF7A0C18, 0xFF3A0408);
		}
		lancea(c);
	}

	/** Five tendrils fanning out from an end of the bar, swaying, their tips lit. */
	private static void wings(BossBarFrameContext c, float fromX, int dir) {
		boolean flared = c.phase() >= 2;
		for (int t = 0; t < 5; t++) {
			float spread = flared ? 1.25F : 1.0F;
			float angle = (-1.05F + t * 0.42F) * spread;
			float length = (14.0F + (2 - Math.abs(t - 2)) * 4.0F) * (flared ? 1.3F : 1.0F);
			int steps = 14;
			float[] xs = new float[steps + 1], ys = new float[steps + 1];
			for (int i = 0; i <= steps; i++) {
				float s = i / (float) steps;
				float sway = Mth.sin(c.time() * 0.06F + t * 1.3F + s * 3.0F) * 2.2F * s;
				float a = angle + s * 0.35F * (t - 2) * 0.3F;
				xs[i] = fromX + dir * Mth.cos(a) * length * s;
				ys[i] = c.midY() + Mth.sin(a) * length * s + sway;
			}
			int tip = GuiCanvas.argb(flared ? 1.0F : 0.75F, flared ? GLOW : SPORE);
			BossBarFramePainter.stroke(c.canvas(), xs, ys, 2.2F, 0.35F, GuiCanvas.argb(1.0F, BLOOD), tip);
		}
	}

	/** The lancea's point under the middle, burning blue once she is below half. */
	private static void lancea(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		float cx = c.centerX(), top = c.y() + 12.0F;
		if (c.phase() >= 2) {
			float pulse = 0.35F + Mth.sin(c.time() * 0.15F) * 0.15F;
			g.disc(cx, top + 4.0F, 6.0F, 4.0F, 16, GuiCanvas.argb(pulse, LANCEA), GuiCanvas.argb(0.0F, LANCEA));
		}
		g.rect(cx - 0.5F, top - 2.0F, cx + 0.5F, top + 3.0F, 0xFFB8C8E0, 0xFF8090B0);
		int head = GuiCanvas.argb(1.0F, LANCEA);
		g.quad(cx, top + 2.0F, head, cx - 2.2F, top + 4.0F, head, cx, top + 7.0F, 0xFFFFFFFF, cx + 2.2F, top + 4.0F, head);
	}
}
