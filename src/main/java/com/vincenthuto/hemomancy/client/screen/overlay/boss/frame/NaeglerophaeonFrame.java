package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * The Vagrant Mind's predator: a membrane rail strung with synaptic nodes, a neuron at each end, and nerve-lightning
 * jumping node to node. The arcs take the bar's phase colour (magenta, violet after the transition, mending gold in the
 * overload, ash once drained) and jump faster as the fight climbs.
 */
public final class NaeglerophaeonFrame implements BossBarFrame {
	private static final int[] ARC = { 0xE02870, 0xAA35FF, 0xFFE470, 0x8A8280 };
	private static final int[] FILL_TOP = { 0xFFE02870, 0xFFAA35FF, 0xFFFFE470, 0xFFB8B0AE };
	private static final int[] FILL_BOTTOM = { 0xFF7A1040, 0xFF4A1080, 0xFFB07A20, 0xFF5C5452 };
	private static final int NODES = 9;

	public static int phaseOf(BossEvent.BossBarColor color) {
		return switch (color) {
			case PURPLE -> 2;
			case YELLOW -> 3;
			case WHITE -> 4;
			default -> 1;
		};
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getColor());
	}

	@Override
	public float[] notches() {
		return new float[] { 0.1F, 0.5F };
	}

	@Override
	public int fillTop(int phase) {
		return FILL_TOP[phase - 1];
	}

	@Override
	public int fillBottom(int phase) {
		return FILL_BOTTOM[phase - 1];
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFFFF0F6;
	}

	@Override
	public int nameColor(int phase) {
		return phase == 4 ? 0xB8B0AE : 0xF4D0E2;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		BossBarFramePainter.rails(c, 0, 0xFF140A12, 0xFF2A1426, 0xFF4A2A44);
		neuron(c, c.x() - 9.0F, -1);
		neuron(c, c.right() + 9.0F, 1);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		int arc = ARC[c.phase() - 1];
		boolean drained = c.phase() == 4;
		int rate = drained ? 9 : 5 - c.phase();
		int beat = Mth.floor(c.time() / rate);
		for (int row = 0; row < 2; row++) {
			float y = row == 0 ? c.y() + 1.0F : c.y() + 12.0F;
			for (int n = 0; n < NODES; n++) {
				float nx = c.x() + BossBarFrame.WIDTH * n / (float) (NODES - 1);
				g.disc(nx, y, 1.6F, 1.6F, 8, GuiCanvas.argb(0.95F, arc), GuiCanvas.argb(0.5F, 0x2A1426));
				if (n == NODES - 1 || GuiCanvas.hash(beat, n, row) > (drained ? 0.15F : 0.35F + c.phase() * 0.12F)) continue;
				lightning(g, nx, y, c.x() + BossBarFrame.WIDTH * (n + 1) / (float) (NODES - 1), y, beat * 31 + n * 7 + row, arc,
						drained ? 0.35F : 0.9F);
			}
		}
	}

	/** A jagged bolt with a soft halo, re-drawn each beat. */
	private static void lightning(GuiCanvas g, float x0, float y0, float x1, float y1, int seed, int rgb, float alpha) {
		int steps = 6;
		float[] xs = new float[steps + 1], ys = new float[steps + 1];
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			float jag = i == 0 || i == steps ? 0.0F : (GuiCanvas.hash(seed, i, 5) - 0.5F) * 4.0F;
			xs[i] = Mth.lerp(t, x0, x1);
			ys[i] = Mth.lerp(t, y0, y1) + jag;
		}
		BossBarFramePainter.stroke(g, xs, ys, 2.4F, 2.4F, GuiCanvas.argb(alpha * 0.25F, rgb), GuiCanvas.argb(alpha * 0.25F, rgb));
		BossBarFramePainter.stroke(g, xs, ys, 0.7F, 0.7F, GuiCanvas.argb(alpha, 0xFFF0F8), GuiCanvas.argb(alpha, rgb));
	}

	/** A neuron body at an end of the bar, its dendrites reaching outward. */
	private static void neuron(BossBarFrameContext c, float cx, int dir) {
		GuiCanvas g = c.canvas();
		int arc = ARC[c.phase() - 1];
		float cy = c.midY();
		for (int d = 0; d < 5; d++) {
			float a = (dir < 0 ? Mth.PI : 0.0F) + (d - 2) * 0.55F + Mth.sin(c.time() * 0.05F + d) * 0.08F;
			float len = 7.0F + GuiCanvas.hash(23, d, dir) * 5.0F;
			float mx = cx + Mth.cos(a) * len * 0.6F, my = cy + Mth.sin(a) * len * 0.6F;
			g.segment(cx, cy, mx, my, 1.6F, 0.9F, 0xFF4A2A44);
			g.segment(mx, my, cx + Mth.cos(a + 0.3F) * len, cy + Mth.sin(a + 0.3F) * len, 0.9F, 0.2F, 0xFF4A2A44);
		}
		float pulse = 0.7F + Mth.sin(c.time() * 0.12F) * 0.3F;
		g.disc(cx, cy, 6.5F, 6.5F, 18, GuiCanvas.argb(0.4F * pulse, arc), GuiCanvas.argb(0.0F, arc));
		g.disc(cx, cy, 4.0F, 4.0F, 16, 0xFF3A1A36, 0xFF1A0A18);
		g.disc(cx, cy, 1.8F, 1.8F, 10, GuiCanvas.argb(pulse, 0xFFF0F8), GuiCanvas.argb(pulse * 0.8F, arc));
	}
}
