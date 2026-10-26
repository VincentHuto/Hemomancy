package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;

/**
 * Saint Velorum, the Frozen Martyr: frost-rimed rails with icicles hanging from the lower edge and a tenebrous dark
 * orb under the middle. In the silence (below a quarter) frost creeps in over the fill from both ends.
 */
public final class VelorumFrame implements BossBarFrame {
	private static final int FROST_DARK = 0xFF0E1626, FROST = 0xFF2A3E5C, FROST_LIP = 0xFFB8DCF4;
	private static final int ICE = 0x9ACCF0, TENEBRIS = 0x140A1E;

	public static int phaseOf(float progress) {
		return progress <= 0.25F ? 2 : 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getProgress());
	}

	@Override
	public float[] notches() {
		return new float[] { 0.25F };
	}

	@Override
	public int fillTop(int phase) {
		return 0xFF7AB8F0;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF22447A;
	}

	@Override
	public int chipColor(int phase) {
		return 0xFFF2FAFF;
	}

	@Override
	public int nameColor(int phase) {
		return 0xDCEEFA;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		BossBarFramePainter.rails(c, 0, FROST_DARK, FROST, FROST_LIP);
		for (int i = 0; i < 11; i++) {
			float ix = c.x() + 6 + GuiCanvas.hash(47, i, 0) * (BossBarFrame.WIDTH - 12);
			float len = 2.5F + GuiCanvas.hash(47, i, 1) * 4.0F;
			g.tri(ix - 1.3F, c.y() + 12.5F, GuiCanvas.argb(0.95F, 0xDCEEFA), ix + 1.3F, c.y() + 12.5F,
					GuiCanvas.argb(0.95F, 0xDCEEFA), ix, c.y() + 12.5F + len, GuiCanvas.argb(0.3F, ICE));
		}
		orb(c);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		if (c.phase() < 2 || c.fillEnd() <= c.x() + 1.0F) return;
		GuiCanvas g = c.canvas();
		// in the silence frost closes over what is left of the fill, from both of its ends
		float reach = Math.min((c.fillEnd() - c.x()) * 0.5F, 14.0F + Mth.sin(c.time() * 0.02F) * 2.0F);
		g.hrect(c.x(), c.y() + 3, c.x() + reach, c.y() + 10, 0xA0E8F4FF, 0x00E8F4FF);
		g.hrect(c.fillEnd() - reach, c.y() + 3, c.fillEnd(), c.y() + 10, 0x00E8F4FF, 0xA0E8F4FF);
		for (int k = 0; k < 6; k++) {
			boolean left = k % 2 == 0;
			float along = GuiCanvas.hash(53, k, 0) * reach;
			flake(g, left ? c.x() + along : c.fillEnd() - along, c.y() + 4.0F + GuiCanvas.hash(53, k, 1) * 5.0F,
					1.2F + GuiCanvas.hash(53, k, 2), c.time() * 0.01F + k);
		}
	}

	/** A six-armed frost crystal. */
	private static void flake(GuiCanvas g, float x, float y, float size, float turn) {
		for (int arm = 0; arm < 3; arm++) {
			float a = turn + arm * Mth.PI / 3.0F;
			float dx = Mth.cos(a) * size, dy = Mth.sin(a) * size;
			g.segment(x - dx, y - dy, x + dx, y + dy, 0.45F, 0.45F, 0xE0FFFFFF);
		}
	}

	/** A dark orb under the middle, haloed in frost, drinking the light. */
	private static void orb(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		float cx = c.centerX(), cy = c.y() + 15.0F;
		float pulse = 0.5F + Mth.sin(c.time() * 0.05F) * 0.2F;
		g.disc(cx, cy, 6.0F, 5.0F, 16, GuiCanvas.argb(pulse, ICE), GuiCanvas.argb(0.0F, ICE));
		g.disc(cx, cy, 3.4F, 3.4F, 14, GuiCanvas.argb(1.0F, TENEBRIS), GuiCanvas.argb(1.0F, 0x2A1A3A));
		g.disc(cx - 1.0F, cy - 1.0F, 0.8F, 0.8F, 6, 0x60DCEEFA, 0x00DCEEFA);
	}
}
