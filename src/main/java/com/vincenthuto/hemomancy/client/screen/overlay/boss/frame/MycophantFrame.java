package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The Spawn Mother: flesh rails wrapped in hyphae, an amber gestation sac at each end with a curled shape turning over
 * inside, and her folded frill collar under the middle. Joining (phase 2) parts the collar on the pulp's eyes, and
 * wing-lattice fiddleheads uncurl from the sacs.
 */
public final class MycophantFrame implements BossBarFrame {
	private static final int AMBER = 0xFFA828, YOLK = 0xFFCE50, CRIMSON = 0xA8161E, BONE = 0xE8D8B0, OLIVE = 0x78803A;
	private static final int FLESH_DARK = 0xFF240608, FLESH = 0xFF5A1A1C, FLESH_LIP = 0xFF8A4A40;

	public static int phaseOf(Component name) {
		return BossBarFramePainter.nameKey(name).endsWith(".joining") ? 2 : 1;
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
		return phase >= 2 ? 0xFFE0602A : 0xFFD02A30;
	}

	@Override
	public int fillBottom(int phase) {
		return phase >= 2 ? 0xFF8A1E12 : 0xFF7A0C14;
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFFFD27A;
	}

	@Override
	public int nameColor(int phase) {
		return phase >= 2 ? 0xFFC060 : BONE;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		if (c.phase() >= 2) {
			fiddlehead(c, c.x() - 12, -1);
			fiddlehead(c, c.right() + 12, 1);
		}
		BossBarFramePainter.rails(c, 1, FLESH_DARK, FLESH, FLESH_LIP);
		collar(c);
		hyphae(c, c.y() + 1.0F, 0);
		hyphae(c, c.y() + 12.0F, 3);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		sac(c, c.x() - 9.0F, 0);
		sac(c, c.right() + 9.0F, 1);
	}

	/** Pale strands winding along a rail, crossing it every so often. */
	private static void hyphae(BossBarFrameContext c, float railY, int seed) {
		for (int k = 0; k < 2; k++) {
			int steps = 46;
			float[] xs = new float[steps + 1], ys = new float[steps + 1];
			for (int i = 0; i <= steps; i++) {
				float x = c.x() - 4 + (BossBarFrame.WIDTH + 8) * i / (float) steps;
				xs[i] = x;
				ys[i] = railY + Mth.sin(x * 0.11F + k * 2.4F + seed + c.time() * 0.02F) * 1.4F;
			}
			BossBarFramePainter.stroke(c.canvas(), xs, ys, 0.7F, 0.7F, GuiCanvas.argb(0.55F, BONE),
					GuiCanvas.argb(0.35F, BONE));
		}
	}

	/** A tall-oval amber sac with a dark curl that turns over inside it. */
	private static void sac(BossBarFrameContext c, float cx, int side) {
		GuiCanvas g = c.canvas();
		float cy = c.midY();
		float breathe = 1.0F + Mth.sin(c.time() * 0.07F + side * 1.7F) * 0.05F;
		float rx = 6.5F * breathe, ry = 8.5F * breathe;
		g.disc(cx, cy, rx + 1.2F, ry + 1.2F, 20, 0xFF3A0A0C, 0xFF3A0A0C);
		g.disc(cx, cy, rx, ry, 20, GuiCanvas.argb(1.0F, YOLK), GuiCanvas.argb(1.0F, 0xC0701C));
		float turn = c.time() * 0.03F + side * Mth.PI;
		g.disc(cx + Mth.cos(turn) * 1.6F, cy + Mth.sin(turn) * 2.2F, 2.4F, 3.0F, 12, 0x90401008, 0x10401008);
		for (int v = 0; v < 3; v++) {
			float a = -0.9F + v * 0.9F + side * Mth.PI;
			g.segment(cx + Mth.cos(a) * rx * 0.95F, cy + Mth.sin(a) * ry * 0.95F, cx + Mth.cos(a) * rx * 0.4F,
					cy + Mth.sin(a) * ry * 0.4F, 0.8F, 0.3F, GuiCanvas.argb(0.7F, CRIMSON));
		}
		g.disc(cx - rx * 0.35F, cy - ry * 0.45F, 1.4F, 2.0F, 8, 0xB0FFF4D0, 0x00FFF4D0);
	}

	/** The frill collar under the middle: folded petals; at joining the two halves part on the pulp's eyes. */
	private static void collar(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		float cx = c.centerX(), top = c.y() + 11.0F;
		float part = c.phase() >= 2 ? 7.0F : 0.0F;
		if (part > 0.0F) {
			g.disc(cx, top + 4.0F, part + 1.0F, 4.0F, 14, 0xFFC8604A, 0xFF5A1A1C);
			for (int e = 0; e < 4; e++) {
				float ex = cx - 4.5F + e * 3.0F, ey = top + 3.0F + (e % 2) * 2.0F;
				float blink = Mth.clamp(Mth.sin(c.time() * 0.05F + e * 1.9F) * 4.0F + 3.0F, 0.0F, 1.0F);
				g.disc(ex, ey, 1.1F, Math.max(0.15F, 1.1F * blink), 8, GuiCanvas.argb(1.0F, YOLK),
						GuiCanvas.argb(1.0F, AMBER));
			}
		}
		for (int side = -1; side <= 1; side += 2) {
			for (int p = 0; p < 4; p++) {
				float base = cx + side * (part + p * 3.2F + 1.0F);
				float tipX = base + side * (2.0F + p * 1.4F), tipY = top + 5.5F - p * 0.6F;
				int body = GuiCanvas.mix(0xFF000000 | OLIVE, 0xFF000000 | CRIMSON, p / 4.0F);
				g.tri(base - side * 1.6F, top, body, base + side * 1.6F, top, body, tipX, tipY, 0xFF000000 | BONE);
			}
		}
	}

	/** A wing-lattice fiddlehead uncurling outward from a sac. */
	private static void fiddlehead(BossBarFrameContext c, float fromX, int dir) {
		float unfurl = 0.75F + Mth.sin(c.time() * 0.04F + dir) * 0.08F;
		for (int f = 0; f < 2; f++) {
			int steps = 18;
			float[] xs = new float[steps + 1], ys = new float[steps + 1];
			float angle = f == 0 ? -0.5F : 0.6F, radius0 = 9.0F;
			for (int i = 0; i <= steps; i++) {
				float t = i / (float) steps;
				float a = angle - t * 4.2F * (1.2F - unfurl);
				float radius = radius0 * (1.0F - t * 0.7F);
				xs[i] = fromX + dir * (t * 10.0F + Mth.cos(a) * radius * t);
				ys[i] = c.midY() + Mth.sin(a) * radius * t - (f == 0 ? 3.0F : -3.0F) * t;
			}
			BossBarFramePainter.stroke(c.canvas(), xs, ys, 1.8F, 0.4F, GuiCanvas.argb(0.95F, 0xD8C890),
					GuiCanvas.argb(0.85F, OLIVE));
		}
	}
}
