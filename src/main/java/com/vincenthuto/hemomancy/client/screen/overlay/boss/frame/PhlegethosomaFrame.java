package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * The arterial fluke: the bar is a vessel wall ringed with cartilage bands, an oral sucker at the left end and the
 * ventral sucker at the right. A heartbeat swell runs along the vessel and ichor beads off its underside. The beat
 * quickens as the fluke swells (pink) and turns raw and stuttering once its armour is stripped (white).
 */
public final class PhlegethosomaFrame implements BossBarFrame {
	private static final int[] FLOW = { 0xE54A32, 0xF06A7A, 0xFFD8C8 };
	private static final int[] FILL_TOP = { 0xFFE54A32, 0xFFE8586E, 0xFFF2A890 };
	private static final int[] FILL_BOTTOM = { 0xFF661226, 0xFF7A1A3E, 0xFF8A2A22 };
	private static final int[] BEAT = { 34, 26, 18 };
	private static final int BANDS = 11;
	private static final int DRIPS = 5;

	public static int phaseOf(BossEvent.BossBarColor color) {
		return switch (color) {
			case PINK -> 2;
			case WHITE -> 3;
			default -> 1;
		};
	}

	@Override
	public int phase(LerpingBossEvent event) {
		return phaseOf(event.getColor());
	}

	@Override
	public float[] notches() {
		return new float[] { 0.25F, 0.6F };
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
		return 0xFFFFE6DC;
	}

	@Override
	public int nameColor(int phase) {
		return phase == 3 ? 0xFFD8C8 : 0xE4B4A5;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		BossBarFramePainter.rails(c, 0, 0xFF180D13, 0xFF4C1721, 0xFF9C4233);
		float swell = swell(c);
		for (int i = 0; i < BANDS; i++) {
			float bx = c.x() + BossBarFrame.WIDTH * (i + 0.5F) / BANDS;
			float lift = bandLift(c, bx) * 1.2F;
			g.rect(bx - 1.0F, c.y() - 1.5F - lift, bx + 1.0F, c.y() + 0.5F, 0xFF6C2630, 0xFF37121B);
			g.rect(bx - 1.0F, c.y() + 12.5F, bx + 1.0F, c.y() + 14.5F + lift, 0xFF37121B, 0xFF6C2630);
		}
		sucker(c, c.x() - 8.0F, 4.6F + swell * 0.6F, false);
		sucker(c, c.right() + 8.0F, 5.4F + swell * 0.8F, true);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		int flow = FLOW[c.phase() - 1];
		float pos = pulsePos(c);
		if (pos >= 0.0F) {
			float px = c.x() + BossBarFrame.WIDTH * pos;
			g.hrect(px - 14.0F, c.y() + 2.0F, px, c.y() + 11.0F, GuiCanvas.argb(0.0F, flow), GuiCanvas.argb(0.35F, flow));
			g.hrect(px, c.y() + 2.0F, px + 4.0F, c.y() + 11.0F, GuiCanvas.argb(0.35F, flow), GuiCanvas.argb(0.0F, flow));
		}
		for (int d = 0; d < DRIPS; d++) {
			float period = 50.0F + GuiCanvas.hash(41, d, 0) * 40.0F;
			float life = ((c.time() + GuiCanvas.hash(41, d, 1) * period) % period) / period;
			float dx = c.x() + 10.0F + GuiCanvas.hash(41, d, 2) * (BossBarFrame.WIDTH - 20.0F);
			float fall = life < 0.6F ? 0.0F : (life - 0.6F) / 0.4F;
			float bead = Math.min(1.0F, life / 0.6F);
			g.disc(dx, c.y() + 14.0F + fall * fall * 7.0F, 0.7F + bead * 0.8F, 0.8F + bead * 1.0F, 8,
					GuiCanvas.argb(0.9F * (1.0F - fall), flow), GuiCanvas.argb(0.6F * (1.0F - fall), 0x661226));
		}
	}

	/** 0..1 swell of the current heartbeat; stripped flukes skip and double-beat. */
	private static float swell(BossBarFrameContext c) {
		int beat = BEAT[c.phase() - 1];
		float t = (c.time() % beat) / beat;
		float s = Math.max(0.0F, 1.0F - t * 5.0F);
		if (c.phase() == 3)
			s = Math.max(s, Math.max(0.0F, 1.0F - Math.abs(t - 0.3F) * 8.0F) * 0.7F);
		return s;
	}

	/** Where along the bar the heartbeat swell is, or -1 between beats. */
	private static float pulsePos(BossBarFrameContext c) {
		int beat = BEAT[c.phase() - 1];
		float t = (c.time() % beat) / beat;
		return t < 0.7F ? t / 0.7F : -1.0F;
	}

	/** How much a cartilage band at {@code x} is pushed outward by the passing swell. */
	private static float bandLift(BossBarFrameContext c, float x) {
		float pos = pulsePos(c);
		if (pos < 0.0F)
			return 0.0F;
		float px = c.x() + BossBarFrame.WIDTH * pos;
		return Math.max(0.0F, 1.0F - Math.abs(x - px) / 10.0F);
	}

	/** A muscular sucker ring with a dark throat, flexing with the beat. */
	private static void sucker(BossBarFrameContext c, float cx, float r, boolean ventral) {
		GuiCanvas g = c.canvas();
		int flow = FLOW[c.phase() - 1];
		float cy = c.midY();
		for (int i = 0; i < 8; i++) {
			float a = i * Mth.TWO_PI / 8.0F + (ventral ? 0.2F : 0.0F);
			float len = r + 1.6F + Mth.sin(c.time() * 0.1F + i) * 0.4F;
			g.segment(cx + Mth.cos(a) * r * 0.7F, cy + Mth.sin(a) * r * 0.7F, cx + Mth.cos(a) * len, cy + Mth.sin(a) * len,
					1.4F, 0.4F, 0xFF4C1721);
		}
		g.disc(cx, cy, r + 2.0F, r + 2.0F, 18, GuiCanvas.argb(0.35F * swell(c), flow), GuiCanvas.argb(0.0F, flow));
		g.disc(cx, cy, r, r, 18, 0xFF9C4233, 0xFF4C1721);
		g.disc(cx, cy, r * 0.5F, r * 0.5F, 14, 0xFF0C0508, GuiCanvas.argb(1.0F, 0x37121B));
	}
}
