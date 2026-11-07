package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterStyle;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeGrowthRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraintState;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;

/**
 * Seraphae's meter under her bar (design section 4). Her bar already shows integrity, so the meter shows what integrity
 * is made of: the restraints row (her four irons in body order on a hematic iron chain, held, slipped or being re-bound,
 * and her two reserves faint until woken), a gold impulse running the chain while an opening is live with the links
 * lighting white as its time runs out, and the challenger's own growth as a thin white vessel line filling toward a
 * centre knot. Painted in her frame's colours; growth is Lux and never shows gold.
 */
public final class SeraphaeMeterStyle implements SaintMeterStyle {
	public static final int HEIGHT = 16;
	private static final int CHAIN = 0xFF2E2628, CHAIN_LIP = 0xFF5A4E50, DARK = 0xFF0A0808;
	private static final int WHITE = 0xFFFFF4E8, GOLD = 0xFFFFE3A8, BONE = 0xFFD6CCB4;
	private static final float RESERVE_LEFT = 0.08F, RESERVE_RIGHT = 0.92F;
	private static final float SHACKLE = 3.2F, RESERVE_SHACKLE = 2.4F;
	private static final float GROWTH_WIDTH = 120.0F;
	private static final int LINKS = 34;
	private static final float IMPULSE_TICKS = 10.0F;

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public void paint(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		float reveal = HemoEasing.smoothstep(HemoEasing.clamp01(c.reveal()));
		int restraints = c.saint() instanceof SeraphaeEntity seraphae ? seraphae.syncedRestraints() : 0;
		int progress = c.saint() instanceof SeraphaeEntity seraphae ? seraphae.syncedRestraintProgress() : 0;
		float rowY = c.y() + 5.0F;
		chainLine(g, c, rowY, restraints, reveal);
		if (c.openingId() != 0) openingImpulse(g, c, rowY, reveal);
		if (c.saint() != null) {
			for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
				int slot = restraint.ordinal();
				shackle(g, c, xAt(c, restraint.hudX()), rowY, SHACKLE, SeraphaeRestraintState.state(restraints, slot),
						SeraphaeRestraintState.progress(progress, slot), reveal);
			}
			for (int i = 0; i < 2; i++) {
				int slot = SeraphaeRestraintState.RESERVE_SLOT + i;
				shackle(g, c, xAt(c, i == 0 ? RESERVE_LEFT : RESERVE_RIGHT), rowY, RESERVE_SHACKLE,
						SeraphaeRestraintState.state(restraints, slot), SeraphaeRestraintState.progress(progress, slot), reveal);
			}
		}
		growth(g, c, c.y() + 12.5F, reveal);
	}

	private static float xAt(SaintMeterContext c, float fraction) {
		return c.x() + BossBarFrame.WIDTH * fraction;
	}

	/** Alternating flat and edge links across the row; beside a slipped iron the chain sags and parts. */
	private static void chainLine(GuiCanvas g, SaintMeterContext c, float y, int restraints, float reveal) {
		float span = BossBarFrame.WIDTH * 0.9F * reveal;
		float x0 = c.centerX() - span / 2.0F;
		for (int i = 0; i < LINKS; i++) {
			float t = (i + 0.5F) / LINKS;
			float lx = x0 + span * t;
			float fraction = (lx - c.x()) / BossBarFrame.WIDTH;
			float sag = 0.0F;
			boolean parted = false;
			for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
				int state = SeraphaeRestraintState.state(restraints, restraint.ordinal());
				if (state != SeraphaeRestraintState.SLIPPED && state != SeraphaeRestraintState.BINDING) continue;
				float d = Math.abs(fraction - restraint.hudX());
				if (d < 0.07F) {
					sag = Math.max(sag, (0.07F - d) / 0.07F * 2.5F);
					parted |= state == SeraphaeRestraintState.SLIPPED && d > 0.025F && d < 0.04F;
				}
			}
			if (parted) continue;
			boolean flat = i % 2 == 0;
			float rx = flat ? 2.0F : 1.0F, ry = flat ? 1.0F : 0.8F;
			float ly = y + sag + Mth.sin(c.time() * 0.05F + i * 0.7F) * sag * 0.2F;
			int lit = linkLight(c, fraction);
			g.disc(lx, ly, rx + 0.5F, ry + 0.5F, 10, lit != 0 ? GuiCanvas.mix(CHAIN_LIP, WHITE, 0.6F) : CHAIN_LIP, CHAIN);
			g.disc(lx, ly, Math.max(0.3F, rx - 0.6F), Math.max(0.3F, ry - 0.6F), 8, DARK, DARK);
		}
	}

	/** While an opening runs, the links light white from both ends inward as its time runs out. */
	private static int linkLight(SaintMeterContext c, float fraction) {
		if (c.openingId() == 0) return 0;
		float fromEnd = Math.min(fraction, 1.0F - fraction) * 2.0F;
		return fromEnd < c.openingFraction() ? 1 : 0;
	}

	/** A gold impulse running the chain from the centre outward every 10 ticks while an opening is live. */
	private static void openingImpulse(GuiCanvas g, SaintMeterContext c, float y, float reveal) {
		float t = (c.time() % IMPULSE_TICKS) / IMPULSE_TICKS;
		float reach = BossBarFrame.WIDTH * 0.45F * t * reveal;
		float alpha = 1.0F - t;
		for (int s = -1; s <= 1; s += 2) {
			float x = c.centerX() + s * reach;
			g.disc(x, y, 2.4F, 1.4F, 10, GuiCanvas.fade(GOLD, alpha), GuiCanvas.fade(GOLD, 0.0F));
			g.mote(x, y, 0.8F, GuiCanvas.fade(0xFFFFFFFF, alpha));
		}
	}

	/** One iron: held, a ring round a breathing white core; slipped, a broken ring and a dark core; re-binding, its core
	 * filling white clockwise with a gold bead at the leading edge; a dormant reserve, a faint outline. */
	private static void shackle(GuiCanvas g, SaintMeterContext c, float x, float y, float r, int state, float progress,
			float reveal) {
		if (state == SeraphaeRestraintState.ABSENT || reveal <= 0.01F) return;
		float breath = 0.75F + 0.25F * Mth.sin(c.time() * 0.12F + x);
		switch (state) {
			case SeraphaeRestraintState.HELD -> {
				g.disc(x, y, r + 0.8F, r + 0.8F, 16, CHAIN_LIP, CHAIN);
				g.disc(x, y, r - 0.6F, r - 0.6F, 14, DARK, DARK);
				g.disc(x, y, r * 0.65F, r * 0.65F, 14, GuiCanvas.fade(WHITE, breath), GuiCanvas.fade(WHITE, breath * 0.4F));
			}
			case SeraphaeRestraintState.SLIPPED -> {
				g.arc(x, y, r - 0.6F, r + 0.8F, 0.6F, Mth.TWO_PI - 0.6F, 14, CHAIN, CHAIN_LIP);
				g.disc(x, y, r * 0.55F, r * 0.55F, 12, 0xFF1A1414, DARK);
			}
			case SeraphaeRestraintState.BINDING -> {
				g.disc(x, y, r + 0.8F, r + 0.8F, 16, CHAIN_LIP, CHAIN);
				g.disc(x, y, r - 0.6F, r - 0.6F, 14, DARK, DARK);
				float sweep = Mth.TWO_PI * Mth.clamp(progress, 0.0F, 1.0F);
				float start = -Mth.HALF_PI;
				if (sweep > 0.05F) g.arc(x, y, 0.0F, r * 0.65F, start, start + sweep, 16, WHITE, GuiCanvas.fade(WHITE, 0.6F));
				float bx = x + Mth.cos(start + sweep) * r * 0.65F, by = y + Mth.sin(start + sweep) * r * 0.65F;
				g.mote(bx, by, 0.9F, GOLD);
			}
			case SeraphaeRestraintState.DORMANT -> {
				g.arc(x, y, r - 0.4F, r + 0.4F, 0.0F, Mth.TWO_PI, 14, GuiCanvas.fade(CHAIN_LIP, 0.35F),
						GuiCanvas.fade(CHAIN_LIP, 0.35F));
			}
			default -> {
			}
		}
	}

	/** The challenger's growth: a thin vessel line filling white from both ends toward a centre knot. */
	private static void growth(GuiCanvas g, SaintMeterContext c, float y, float reveal) {
		float half = GROWTH_WIDTH / 2.0F * reveal;
		float cx = c.centerX();
		g.rect(cx - half, y - 0.5F, cx + half, y + 0.5F, GuiCanvas.fade(0xFF3A3032, 0.8F), GuiCanvas.fade(0xFF3A3032, 0.8F));
		for (float notch : new float[]{SeraphaeGrowthRules.SWELLING, SeraphaeGrowthRules.KNOTTING}) {
			float offset = half * notch / SeraphaeGrowthRules.MAX;
			for (int s = -1; s <= 1; s += 2) {
				float nx = cx + s * (half - offset);
				g.rect(nx - 0.5F, y - 1.5F, nx + 0.5F, y + 1.5F, BONE, BONE);
			}
		}
		float fill = c.secondaryFraction();
		if (fill > 0.0F) {
			float reach = half * fill;
			g.rect(cx - half, y - 0.5F, cx - half + reach, y + 0.5F, WHITE, GuiCanvas.fade(WHITE, 0.7F));
			g.rect(cx + half - reach, y - 0.5F, cx + half, y + 0.5F, WHITE, GuiCanvas.fade(WHITE, 0.7F));
		}
		boolean knotting = c.flag(SeraphaeGrowthRules.FLAG_KNOTTING);
		float swell = 1.4F + fill * 1.2F + (knotting ? 0.3F * Mth.sin(c.time() * 0.3F) : 0.0F);
		float flare = fill >= 0.95F ? 1.0F : 0.0F;
		g.disc(cx, y, swell + flare * 1.5F, swell + flare * 1.5F, 12, GuiCanvas.fade(WHITE, 0.4F + fill * 0.6F),
				GuiCanvas.fade(WHITE, 0.1F));
		if (c.flag(SeraphaeGrowthRules.FLAG_HELD)) {
			g.disc(cx, y, swell + 2.5F, swell + 2.5F, 14, GuiCanvas.fade(WHITE, 0.0F), GuiCanvas.fade(WHITE, 0.35F));
		}
	}
}
