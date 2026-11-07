package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterStyle;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathIronRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMeterRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathTitheRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;

/**
 * Hemorath's meter under his bar (design section 4, "The HUD meter"): a riveted hematic-iron vial the bar's width, the
 * blood he holds filling it from the left with a meniscus that throbs with his heartbeat, drips falling from its
 * underside as fast as he leaks, studs at the 35% and 70% thresholds and a cracked cap where he bursts, nine iron studs
 * that rust as his patches erupt, a pulse along the glass while Gorged or Strained, and the challenger's own Tithe debt
 * as dark links over the lower rail. Painted with the frame's palette; the rails tarnish violet in phase 2.
 */
public final class HemorathMeterStyle implements SaintMeterStyle {
	private static final int IRON_DARK = 0xFF141012, IRON = 0xFF3A3236, IRON_LIP = 0xFF6A5A5E;
	private static final int TARNISH_DARK = 0xFF140A1C, TARNISH = 0xFF3A2448, TARNISH_LIP = 0xFF6A4A80;
	private static final int WELL = 0xF04A4442;
	private static final int BLOOD_TOP = 0xFFB0202A, BLOOD_BOTTOM = 0xFF4A0A12, BLOOD_BOTTOM_EMPTIED = 0xFF2A0C3A;
	private static final int CORE = 0xFFD2C4, MENISCUS = 0xB3FFD2C4;
	private static final int RUSTED = 0xFF70281C, STUD = 0xFF8A7A7E;
	/** Rows: two of rail, seven of glass, two of rail, then room for the drips. */
	private static final int RAIL = 2, GLASS = 7, DRIP_FALL = 6, HEIGHT = RAIL + GLASS + RAIL + DRIP_FALL + 1;
	private static final float RIVET_EVERY = 16.0F;
	/** One drip every 40 ticks hollow, every 6 full. */
	private static final float DRIP_EMPTY = 40.0F, DRIP_FULL = 6.0F;
	private static final int DRIPS = 4;
	private static final float LINK_WIDTH = 4.0F;

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public void paint(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		float open = HemoEasing.smoothstep(HemoEasing.clamp01(c.reveal() / 0.6F));
		float rise = HemoEasing.smoothstep(HemoEasing.clamp01((c.reveal() - 0.4F) / 0.6F));
		float half = BossBarFrame.WIDTH * 0.5F * open;
		float x0 = c.centerX() - half, x1 = c.centerX() + half;
		boolean emptied = c.phase() >= 2;
		HemorathEntity saint = c.saint() instanceof HemorathEntity h ? h : null;
		boolean thirst = saint != null && saint.thirsting();
		int dark = emptied && !thirst ? TARNISH_DARK : IRON_DARK, body = emptied && !thirst ? TARNISH : IRON,
				lip = emptied && !thirst ? TARNISH_LIP : IRON_LIP;
		float top = c.y(), glassTop = top + RAIL, glassBottom = glassTop + GLASS, bottom = glassBottom + RAIL;
		// the rails and the glass well
		g.rect(x0 - 1, top, x1 + 1, glassTop, lip, body);
		g.rect(x0 - 1, glassBottom, x1 + 1, bottom, body, dark);
		g.rect(x0, glassTop, x1, glassBottom, WELL, GuiCanvas.fade(WELL, 0.9F));
		g.rect(x0, glassTop, x1, glassTop + 1, 0x30FFFFFF, 0x30FFFFFF);
		for (float rx = c.x() + 4; rx < c.right() - 2; rx += RIVET_EVERY) {
			if (rx < x0 || rx > x1) continue;
			g.disc(rx, top + 1.0F, 0.8F, 0.8F, 6, STUD, dark);
			g.disc(rx, bottom - 1.0F, 0.8F, 0.8F, 6, STUD, dark);
		}
		// the blood, with a meniscus that throbs with his heartbeat
		float f = c.saintFraction() * rise;
		float end = c.x() + BossBarFrame.WIDTH * f;
		float beat = HemorathVisualRules.heartbeat(c.time(), c.saintFraction());
		if (end > c.x() + 0.5F) {
			g.rect(Math.max(x0, c.x()), glassTop + 1, Math.min(x1, end), glassBottom, BLOOD_TOP,
					emptied ? BLOOD_BOTTOM_EMPTIED : BLOOD_BOTTOM);
			float wobble = beat > 0.5F ? 1.0F : 0.0F;
			g.rect(Math.min(x1, end) - 1, glassTop + 1 - wobble, Math.min(x1, end), glassBottom, MENISCUS, MENISCUS);
		}
		// thresholds and the cracked cap
		for (float at : new float[]{HemorathMeterRules.FILLING_FROM, HemorathMeterRules.BRIMMING_FROM}) {
			float sx = c.x() + BossBarFrame.WIDTH * at;
			if (sx > x0 && sx < x1) g.rect(sx - 1, glassBottom, sx + 1, bottom + 1, lip, dark);
		}
		if (open > 0.98F) {
			float cx = c.right() + 2.0F, cy = glassTop + GLASS * 0.5F;
			g.disc(cx, cy, 3.0F, 4.5F, 10, body, dark);
			boolean venting = c.saintFraction() >= HemorathMeterRules.VENT_FROM;
			int crack = venting ? GuiCanvas.argb(0.6F + 0.4F * beat, CORE) : GuiCanvas.fade(dark, 0.9F);
			g.segment(cx - 1.0F, cy - 3.5F, cx + 0.5F, cy, 0.6F, 0.4F, crack);
			g.segment(cx + 0.5F, cy, cx - 0.5F, cy + 3.5F, 0.4F, 0.2F, crack);
		}
		// nine iron studs on the upper rail's left end, one rusting per erupted patch
		int patches = saint == null ? 0 : saint.syncedPatches();
		for (int i = 0; i < HemorathIronRules.MAX_PATCHES; i++) {
			float sx = c.x() + 3 + i * 3.0F;
			if (sx < x0 || sx > x1) continue;
			g.rect(sx, top, sx + 2, top + 1.5F, i < patches ? RUSTED : lip, i < patches ? RUSTED : body);
		}
		// an opening that feeds pulses along the glass
		if (HemorathOpenings.feeds(c.openingId())) {
			float pulse = 0.5F + 0.5F * Mth.sin(c.time() * 0.6F);
			float width = (x1 - x0) * (1.0F - c.openingFraction());
			g.rect(c.centerX() - width * 0.5F, glassTop + 2, c.centerX() + width * 0.5F, glassTop + 4,
					GuiCanvas.argb(0.35F + 0.4F * pulse, CORE), GuiCanvas.argb(0.1F, CORE));
		}
		// the leak, visible: drips under the blood's leading third; none while he digests or in the Last Thirst
		boolean digesting = thirst || c.openingId() == HemorathOpenings.GORGED.id();
		if (!digesting && f > 0.02F) {
			float interval = HemoEasing.lerp(DRIP_EMPTY, DRIP_FULL, c.saintFraction());
			for (int i = 0; i < DRIPS; i++) {
				float phase = ((c.time() + i * interval * 0.37F) / interval) % 1.0F;
				int cycle = (int) Math.floor((c.time() + i * interval * 0.37F) / interval);
				float lead = BossBarFrame.WIDTH * f;
				float dx = c.x() + lead * (0.67F + 0.33F * GuiCanvas.hash(77, cycle, i));
				if (dx < x0 || dx > x1) continue;
				float dy = bottom + phase * DRIP_FALL;
				g.rect(dx, dy, dx + 1, dy + 2, GuiCanvas.argb(1.0F - phase, 0x8E1424), GuiCanvas.argb(0.6F * (1.0F - phase), 0x4A0A12));
			}
		}
		// the challenger's own Tithe debt: dark iron links over the left of the lower rail
		int links = HemorathTitheRules.links(c.ownSecondary());
		for (int i = 0; i < links; i++) {
			float lx = c.x() + 2 + i * LINK_WIDTH;
			if (lx < x0 || lx + LINK_WIDTH > x1) continue;
			g.rect(lx, glassBottom, lx + LINK_WIDTH - 1, glassBottom + 1, IRON_DARK, IRON_DARK);
			g.rect(lx, bottom - 1, lx + LINK_WIDTH - 1, bottom, IRON_DARK, IRON_DARK);
			g.rect(lx, glassBottom, lx + 1, bottom, IRON_LIP, IRON_DARK);
		}
	}
}
