package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.event.VelorumGloamFog;
import com.vincenthuto.hemomancy.client.render.world.VelorumDarkSight;
import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterStyle;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.Mth;

/**
 * The stillness meter under Velorum's bar (design section 4): a slender rime-glass vessel capped at each end by a small
 * hematic-iron lantern cage, frost (not liquid) creeping in from both ends toward the frame's dark orb, each end reaching
 * the value's share of half the vessel. A pale tick at 40 and two at 70. Halted, the flakes stop turning and a dark seam
 * holds each front; preserved, the vessel frosts white over the orb and a hairline crack runs across, thawing back from
 * the centre. A kneeling glyph lights while an ally shelters the viewer, an eye opens in a watching gaze. No number and
 * no clock face: nothing here reads as time. At Cold and above a faint frost creeps in at the screen's edges.
 * <p>
 * Revision 2026-11-06: the fronts' teeth are cruor tipped with frost; a small cruor heart left of the vessel beats,
 * ever slower, while an Arrested Pulse marks the viewer, and cracks when it bursts; in his liquid dark (or gloamed) a
 * torn-smoke vignette closes from the screen's edges, its inner edge 55% of the way out (35% in the Eclipse), never
 * inside the crosshair's middle third, and every sound but his thins to 0.55.
 */
public final class VelorumMeterStyle implements SaintMeterStyle {
	public static final int HEIGHT = 9;
	public static final float HALF = 60.0F;
	public static final float VESSEL = 7.0F;
	/** The screen's frost edge: 0.10 alpha at Cold rising to 0.25 just under full. */
	public static final float EDGE_MIN = 0.10F;
	public static final float EDGE_MAX = 0.25F;
	private static final float EDGE_BAND = 22.0F;
	/** The darkness muffles: inside his presence, and more in the Endless Hour. */
	public static final float MUFFLE_PRESENCE = 0.75F;
	public static final float MUFFLE_HOUR = 0.5F;
	/** In his liquid dark, or gloamed: every sound but his thins to this (R.3). */
	public static final float MUFFLE_DARK = 0.55F;
	/** The gloam's vignette: its inner edge this far from the centre to the edge (R.3), 0.7 alpha at the screen's edge. */
	public static final float GLOAM_INNER = 0.55F, GLOAM_INNER_ECLIPSE = 0.35F, GLOAM_EDGE = 0.7F;
	/** The cruor of the meter's teeth and the heart glyph (R.2 {@code CRUOR_LIT} and {@code CRUOR}). */
	private static final int CRUOR_LIT = 0x6A1C26, CRUOR = 0x3A0E14, LIQUID = 0x07040C, SMOKE = 0x140A1E;

	@Override
	public int height() {
		return HEIGHT;
	}

	/** How far one front has crept, in pixels: the value's share of half the vessel. */
	public static float reach(float value) {
		return HALF * HemoEasing.clamp01(value / VelorumStillnessRules.MAX);
	}

	/** Pale ticks from each end: one at Cold, two at Stilling. */
	public static int ticks(float value) {
		return value >= VelorumStillnessRules.STILLING ? 2 : value >= VelorumStillnessRules.COLD ? 1 : 0;
	}

	/** The screen edge's alpha for a value, 0 under Cold. */
	public static float edgeAlpha(float value) {
		if (value < VelorumStillnessRules.COLD) return 0.0F;
		float span = VelorumStillnessRules.MAX - 1.0F - VelorumStillnessRules.COLD;
		return HemoEasing.lerp(EDGE_MIN, EDGE_MAX, HemoEasing.clamp01((value - VelorumStillnessRules.COLD) / span));
	}

	@Override
	public void paint(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		float open = HemoEasing.smoothstep(HemoEasing.clamp01(c.reveal() / 0.6F));
		float cx = c.centerX();
		float half = HALF * open;
		float top = c.y() - 2.5F, bottom = top + VESSEL;
		float x0 = cx - half, x1 = cx + half;
		boolean halted = c.flag(VelorumStillnessRules.FLAG_HALTED);
		boolean preserved = c.flag(VelorumStillnessRules.FLAG_PRESERVED);
		float time = halted ? 0.0F : c.time();
		// the vessel: a dark rail with a pale lip along its top
		g.rect(x0, top, x1, bottom, GuiCanvas.fade(VelorumFrostPaint.FROST_DARK, 0.92F),
				GuiCanvas.fade(VelorumFrostPaint.FROST, 0.92F));
		g.rect(x0, top, x1, top + 1.0F, GuiCanvas.fade(VelorumFrostPaint.FROST_LIP, 0.55F),
				GuiCanvas.fade(VelorumFrostPaint.FROST_LIP, 0.2F));
		float value = c.ownValue();
		float reach = reach(value) * open;
		int frost = GuiCanvas.mix(GuiCanvas.argb(0.85F, VelorumFrostPaint.ICE), GuiCanvas.argb(0.95F, VelorumFrostPaint.CORE),
				HemoEasing.clamp01(value / VelorumStillnessRules.MAX));
		if (reach > 0.05F) {
			g.hrect(x0, top + 1.0F, x0 + reach, bottom, frost, GuiCanvas.fade(frost, 0.55F));
			g.hrect(x1 - reach, top + 1.0F, x1, bottom, GuiCanvas.fade(frost, 0.55F), frost);
			// the fronts' teeth: cruor tipped with frost
			int cruor = GuiCanvas.argb(0.95F, CRUOR_LIT);
			VelorumFrostPaint.teeth(g, x0 + reach, top + 1.0F, bottom, 1.0F, cruor, 61);
			VelorumFrostPaint.teeth(g, x1 - reach, top + 1.0F, bottom, -1.0F, cruor, 67);
			g.rect(x0 + reach, top + 1.0F, x0 + reach + 0.5F, bottom, frost, GuiCanvas.fade(frost, 0.4F));
			g.rect(x1 - reach - 0.5F, top + 1.0F, x1 - reach, bottom, frost, GuiCanvas.fade(frost, 0.4F));
			for (int k = 0; k < 4; k++) {
				boolean left = k % 2 == 0;
				float along = GuiCanvas.hash(71, k, 0) * reach;
				VelorumFrostPaint.flake(g, left ? x0 + along : x1 - along, top + 2.0F + GuiCanvas.hash(71, k, 1) * 3.0F,
						0.9F + GuiCanvas.hash(71, k, 2) * 0.6F, time * 0.012F + k);
			}
			if (halted) {
				// the cold is held: a hairline dark seam on each front
				int seam = GuiCanvas.argb(0.85F, VelorumFrostPaint.TENEBRIS);
				g.rect(x0 + reach, top + 1.0F, x0 + reach + 0.5F, bottom, seam, seam);
				g.rect(x1 - reach - 0.5F, top + 1.0F, x1 - reach, bottom, seam, seam);
			}
		}
		int tick = GuiCanvas.argb(0.55F, VelorumFrostPaint.CORE);
		for (int i = 0; i < 2; i++) {
			float at = (i == 0 ? VelorumStillnessRules.COLD : VelorumStillnessRules.STILLING) / VelorumStillnessRules.MAX * half;
			for (int n = 0; n <= i; n++) {
				float dx = n * 1.2F;
				g.rect(x0 + at + dx, bottom - 2.5F, x0 + at + dx + 0.4F, bottom, tick, tick);
				g.rect(x1 - at - dx - 0.4F, bottom - 2.5F, x1 - at - dx, bottom, tick, tick);
			}
		}
		if (preserved) {
			// the fronts meet over the orb: white, a hairline crack, thawing back from the centre as the hold runs out
			float hold = HemoEasing.clamp01(c.stateTicks() / (float) VelorumStillnessRules.PRESERVED_TICKS);
			float white = half * hold;
			g.hrect(cx - white, top + 1.0F, cx, bottom, GuiCanvas.argb(0.3F, 0xFFFFFF), GuiCanvas.argb(0.9F, 0xFFFFFF));
			g.hrect(cx, top + 1.0F, cx + white, bottom, GuiCanvas.argb(0.9F, 0xFFFFFF), GuiCanvas.argb(0.3F, 0xFFFFFF));
			int crack = GuiCanvas.argb(0.8F, VelorumFrostPaint.TENEBRIS);
			g.segment(cx - white, top + 3.5F, cx - white * 0.3F, top + 2.5F, 0.35F, 0.35F, crack);
			g.segment(cx - white * 0.3F, top + 2.5F, cx + white, top + 4.5F, 0.35F, 0.35F, crack);
		}
		VelorumFrostPaint.cage(g, x0 - 2.0F, top, bottom, halted ? 0.2F : 1.0F);
		VelorumFrostPaint.cage(g, x1 + 2.0F, top, bottom, halted ? 0.2F : 1.0F);
		if (c.flag(VelorumStillnessRules.FLAG_SHELTERED)) kneeling(g, x0 - 9.0F, top - 0.5F);
		if (c.flag(VelorumStillnessRules.FLAG_WATCHED)) eye(g, x1 + 8.0F, (top + bottom) * 0.5F, c.time());
		float marked = VelorumDarkSight.marked(c.viewer(), c.partialTick());
		if (marked >= 0.0F) heart(g, x0 - (c.flag(VelorumStillnessRules.FLAG_SHELTERED) ? 16.0F : 9.0F), (top + bottom) * 0.5F, marked,
				VelorumDarkSight.burst(c.viewer(), c.partialTick()));
	}

	/**
	 * A small cruor heart while an Arrested Pulse marks the viewer: it beats, each beat slower than the last (its blood
	 * slowing), frost crusting its edge as the next beat nears; at a burst it cracks.
	 */
	private static void heart(GuiCanvas g, float x, float y, float sinceMark, float sinceBurst) {
		int beat = VelorumVisualRules.beat(sinceMark);
		float period = 12.0F + 8.0F * Math.max(0, beat < 0 ? 3 : beat);
		float throb = (float) Math.pow(Math.max(0.0F, Mth.sin(sinceMark / period * Mth.TWO_PI)), 6.0F);
		float size = 2.2F + 0.6F * throb;
		int body = GuiCanvas.argb(0.95F, CRUOR_LIT), deep = GuiCanvas.argb(0.95F, CRUOR);
		g.disc(x - size * 0.45F, y - size * 0.25F, size * 0.55F, size * 0.55F, 8, body, deep);
		g.disc(x + size * 0.45F, y - size * 0.25F, size * 0.55F, size * 0.55F, 8, body, deep);
		g.tri(x - size, y - size * 0.1F, deep, x + size, y - size * 0.1F, deep, x, y + size, deep);
		float tell = VelorumVisualRules.tell(sinceMark);
		if (tell > 0.0F) {
			int rime = GuiCanvas.argb(0.85F * tell, VelorumFrostPaint.CORE);
			g.segment(x - size, y - size * 0.1F, x, y + size, 0.35F, 0.35F, rime);
			g.segment(x + size, y - size * 0.1F, x, y + size, 0.35F, 0.35F, rime);
		}
		if (sinceBurst >= 0.0F && sinceBurst < 30.0F) {
			int crack = GuiCanvas.argb(0.9F * (1.0F - sinceBurst / 30.0F), VelorumFrostPaint.CORE);
			g.segment(x - 0.2F, y - size * 0.6F, x + 0.5F, y, 0.3F, 0.3F, crack);
			g.segment(x + 0.5F, y, x - 0.4F, y + size * 0.7F, 0.3F, 0.3F, crack);
		}
	}

	/** An 8-pixel kneeling silhouette, lit pale while an ally devotee shelters the viewer. */
	private static void kneeling(GuiCanvas g, float x, float y) {
		int pale = GuiCanvas.argb(0.85F, VelorumFrostPaint.CORE);
		g.disc(x, y + 1.2F, 1.1F, 1.1F, 8, pale, pale);
		g.quad(x - 1.4F, y + 2.6F, pale, x + 1.4F, y + 2.6F, pale, x + 1.8F, y + 6.0F, pale, x - 1.0F, y + 6.0F, pale);
		g.rect(x - 1.0F, y + 6.0F, x + 3.0F, y + 7.2F, pale, pale);
	}

	/** A small eye that opens while the viewer stands in a watching figure's gaze (the Beholding deed). */
	private static void eye(GuiCanvas g, float x, float y, float time) {
		float open = 0.6F + 0.4F * Mth.sin(time * 0.08F);
		int pale = GuiCanvas.argb(0.85F, VelorumFrostPaint.CORE);
		int dark = GuiCanvas.argb(1.0F, VelorumFrostPaint.TENEBRIS);
		g.disc(x, y, 3.0F, 1.6F * open, 12, GuiCanvas.argb(0.25F, VelorumFrostPaint.ICE), pale);
		g.disc(x, y, 0.9F, 0.9F * open, 6, dark, dark);
	}

	/** At Cold and above, a faint frost at the screen's edges, never over the crosshair's middle third. */
	@Override
	public void paintScreen(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		gloam(c, g);
		float alpha = edgeAlpha(c.ownValue()) * HemoEasing.clamp01(c.reveal());
		if (alpha <= 0.0F) return;
		int frost = GuiCanvas.argb(alpha, VelorumFrostPaint.CORE);
		int clear = GuiCanvas.argb(0.0F, VelorumFrostPaint.CORE);
		float w = c.screenWidth(), h = c.screenHeight();
		float band = Math.min(EDGE_BAND, Math.min(w, h) / 6.0F);
		g.rect(0.0F, 0.0F, w, band, frost, clear);
		g.rect(0.0F, h - band, w, h, clear, frost);
		g.hrect(0.0F, band, band, h - band, frost, clear);
		g.hrect(w - band, band, w, h - band, clear, frost);
	}

	/** How far the gloam's vignette reaches in from each edge, as a share of half the screen. */
	public static float gloamBand(boolean eclipse) {
		return 1.0F - (eclipse ? GLOAM_INNER_ECLIPSE : GLOAM_INNER);
	}

	/**
	 * The gloam on screen: a vignette of torn smoke closing from the edges, 0.7 alpha at the edge thinning to nothing at
	 * its inner edge, broad tongues of smoke torn along that edge drifting slowly. Subtle, never black, and never inside
	 * the crosshair's middle third.
	 */
	private static void gloam(SaintMeterContext c, GuiCanvas g) {
		float strength = VelorumGloamFog.strength(c.viewer(), c.partialTick());
		if (strength <= 0.01F) return;
		boolean eclipse = VelorumDarkSight.eclipsed(c.viewer(), c.partialTick());
		float w = c.screenWidth(), h = c.screenHeight();
		float bx = w * 0.5F * gloamBand(eclipse), by = h * 0.5F * gloamBand(eclipse);
		int edge = GuiCanvas.argb(GLOAM_EDGE * strength, LIQUID);
		int clear = GuiCanvas.argb(0.0F, LIQUID);
		g.rect(0.0F, 0.0F, w, by, edge, clear);
		g.rect(0.0F, h - by, w, h, clear, edge);
		g.hrect(0.0F, by, bx, h - by, edge, clear);
		g.hrect(w - bx, by, w, h - by, clear, edge);
		// torn tongues of smoke along the inner edge, each its own length, drifting. Each tongue thins to nothing on every
		// side (polish round 1: a triangle based on the edge drew a hard line across the screen where the band's own
		// gradient had already reached zero)
		int smoke = GuiCanvas.argb(0.3F * strength, SMOKE);
		int thin = GuiCanvas.argb(0.0F, SMOKE);
		float drift = c.time() * 0.15F;
		int across = 14;
		for (int i = 0; i < across; i++) {
			float span = w / across;
			float x = i * span + (drift + GuiCanvas.hash(83, i, 0) * span) % span - span * 0.5F;
			float len = by * (0.25F + 0.45F * GuiCanvas.hash(83, i, 1)) * (0.8F + 0.2F * Mth.sin(c.time() * 0.03F + i));
			float root = by * (0.35F + 0.25F * GuiCanvas.hash(83, i, 2));
			float cx = x + span * 0.45F, mid = by - root * 0.3F, low = h - mid;
			tongue(g, cx, mid, x, x + span * 0.9F, mid - root, mid + len, smoke, thin, true);
			tongue(g, cx, low, x, x + span * 0.9F, low + root, low - len, smoke, thin, true);
		}
		int down = 8;
		for (int i = 0; i < down; i++) {
			float span = (h - 2.0F * by) / down;
			float y = by + i * span;
			float len = bx * (0.25F + 0.45F * GuiCanvas.hash(89, i, 1)) * (0.8F + 0.2F * Mth.sin(c.time() * 0.03F + i * 1.7F));
			float root = bx * (0.35F + 0.25F * GuiCanvas.hash(89, i, 2));
			float cy = y + span * 0.45F, mid = bx - root * 0.3F, far = w - mid;
			tongue(g, mid, cy, y, y + span * 0.9F, mid - root, mid + len, smoke, thin, false);
			tongue(g, far, cy, y, y + span * 0.9F, far + root, far - len, smoke, thin, false);
		}
	}

	/**
	 * One torn tongue of the gloam: densest at its centre on the band's edge, thinning to nothing at its two corners along
	 * that edge ({@code a0}, {@code a1}), back into the band ({@code back}) and in to its tip; across a horizontal edge when
	 * {@code across}, else down a vertical one. The centre {@code (u, v)} is on screen.
	 */
	private static void tongue(GuiCanvas g, float u, float v, float a0, float a1, float back, float tip, int dense, int thin,
			boolean across) {
		float cx = u, cy = v;
		float lx = across ? a0 : u, ly = across ? v : a0;
		float rx = across ? a1 : u, ry = across ? v : a1;
		float bx = across ? u : back, by = across ? back : v;
		float tx = across ? u : tip, ty = across ? tip : v;
		g.tri(lx, ly, thin, bx, by, thin, cx, cy, dense);
		g.tri(rx, ry, thin, bx, by, thin, cx, cy, dense);
		g.tri(lx, ly, thin, tx, ty, thin, cx, cy, dense);
		g.tri(rx, ry, thin, tx, ty, thin, cx, cy, dense);
	}

	/** The sensory darkness: other sounds thin inside his presence, more in the Endless Hour and in his dark. Never silent. */
	@Override
	public float muffle(SaintMeterContext c, SoundInstance sound) {
		if (c.saint() == null) return 1.0F;
		float muffle = c.viewer().distanceTo(c.saint()) <= VelorumPresence.RADIUS ? MUFFLE_PRESENCE : 1.0F;
		if (c.saint().view().moveId() == VelorumMove.ENDLESS_HOUR.id()) muffle = Math.min(muffle, MUFFLE_HOUR);
		float dark = VelorumGloamFog.last();
		if (dark > 0.0F) muffle = Math.min(muffle, HemoEasing.lerp(1.0F, MUFFLE_DARK, dark));
		return muffle;
	}
}
