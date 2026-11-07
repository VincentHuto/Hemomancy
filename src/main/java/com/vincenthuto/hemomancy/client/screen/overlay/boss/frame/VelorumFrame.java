package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;

/**
 * Saint Velorum's bar: frost-rimed rails with icicles of frozen cruor hanging from the lower edge and a tenebrous dark orb under the
 * middle. Three phases with notches at his floors (60%, 25%) and his last tenth: in the second the rail's icicles grow
 * by a third; in the third frost closes over the fill from both of its ends; under the last tenth the rime closes the
 * rails to a hairline round what is left; at nothing the orb's halo goes out.
 */
public final class VelorumFrame implements BossBarFrame {
	public static final float CONGREGATION_AT = 0.60F;
	public static final float HOUR_AT = 0.25F;
	public static final float LAST_TENTH = 0.10F;
	private static final float ICICLE_GROWTH = 4.0F / 3.0F;
	/** The rail's icicles are Frozen Cruor (revision 2026-11-06): R.2's {@code CRUOR_LIT} and {@code CRUOR_DEEP}. */
	private static final int CRUOR_LIT = 0x6A1C26, CRUOR_DEEP = 0x1A0508;

	public static int phaseOf(float progress) {
		if (progress <= HOUR_AT) return 3;
		return progress <= CONGREGATION_AT ? 2 : 1;
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getProgress());
	}

	@Override
	public float[] notches() {
		return new float[] { LAST_TENTH, HOUR_AT, CONGREGATION_AT };
	}

	@Override
	public int fillTop(int phase) {
		return phase >= 3 ? 0xFF9AC4F0 : 0xFF7AB8F0;
	}

	@Override
	public int fillBottom(int phase) {
		return phase >= 3 ? 0xFF2A3E70 : 0xFF22447A;
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
		BossBarFramePainter.rails(c, 0, VelorumFrostPaint.FROST_DARK, VelorumFrostPaint.FROST, VelorumFrostPaint.FROST_LIP);
		float growth = c.phase() >= 2 ? ICICLE_GROWTH : 1.0F;
		for (int i = 0; i < 11; i++) {
			float ix = c.x() + 6 + GuiCanvas.hash(47, i, 0) * (BossBarFrame.WIDTH - 12);
			float len = (2.5F + GuiCanvas.hash(47, i, 1) * 4.0F) * growth;
			// an icicle of frozen cruor, black-red to its point, a crust of frost where it hangs from the rail
			g.tri(ix - 1.3F, c.y() + 12.5F, GuiCanvas.argb(0.95F, CRUOR_LIT), ix + 1.3F, c.y() + 12.5F,
					GuiCanvas.argb(0.95F, CRUOR_LIT), ix, c.y() + 12.5F + len, GuiCanvas.argb(0.75F, CRUOR_DEEP));
			g.tri(ix - 1.3F, c.y() + 12.5F, GuiCanvas.argb(0.9F, 0xDCE8F4), ix + 1.3F, c.y() + 12.5F,
					GuiCanvas.argb(0.9F, 0xDCE8F4), ix, c.y() + 12.5F + Math.min(1.4F, len * 0.3F), GuiCanvas.argb(0.2F, 0xDCE8F4));
		}
		orb(c);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		if (c.progress() <= LAST_TENTH && c.progress() > 0.0F) {
			// the rime closes the rails to a hairline gap round what is left of the fill
			g.rect(c.x(), c.y() + 2, c.right(), c.y() + 4, 0xC0E8F4FF, 0x60E8F4FF);
			g.rect(c.x(), c.y() + 9, c.right(), c.y() + 11, 0x60E8F4FF, 0xC0E8F4FF);
		}
		if (c.phase() < 3 || c.fillEnd() <= c.x() + 1.0F) return;
		// in the Endless Hour frost closes over what is left of the fill, from both of its ends
		float reach = Math.min((c.fillEnd() - c.x()) * 0.5F, 14.0F + Mth.sin(c.time() * 0.02F) * 2.0F);
		g.hrect(c.x(), c.y() + 3, c.x() + reach, c.y() + 10, 0xA0E8F4FF, 0x00E8F4FF);
		g.hrect(c.fillEnd() - reach, c.y() + 3, c.fillEnd(), c.y() + 10, 0x00E8F4FF, 0xA0E8F4FF);
		for (int k = 0; k < 6; k++) {
			boolean left = k % 2 == 0;
			float along = GuiCanvas.hash(53, k, 0) * reach;
			VelorumFrostPaint.flake(g, left ? c.x() + along : c.fillEnd() - along,
					c.y() + 4.0F + GuiCanvas.hash(53, k, 1) * 5.0F, 1.2F + GuiCanvas.hash(53, k, 2), c.time() * 0.01F + k);
		}
	}

	/** A dark orb under the middle, haloed in frost, drinking the light; the halo goes out when the bar is empty. */
	private static void orb(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		float cx = c.centerX(), cy = c.y() + 15.0F;
		float pulse = c.progress() <= 0.0F ? 0.0F : 0.5F + Mth.sin(c.time() * 0.05F) * 0.2F;
		g.disc(cx, cy, 6.0F, 5.0F, 16, GuiCanvas.argb(pulse, VelorumFrostPaint.ICE), GuiCanvas.argb(0.0F, VelorumFrostPaint.ICE));
		g.disc(cx, cy, 3.4F, 3.4F, 14, GuiCanvas.argb(1.0F, VelorumFrostPaint.TENEBRIS), GuiCanvas.argb(1.0F, 0x2A1A3A));
		g.disc(cx - 1.0F, cy - 1.0F, 0.8F, 0.8F, 6, 0x60DCEEFA, 0x00DCEEFA);
	}
}
