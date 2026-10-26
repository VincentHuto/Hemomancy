package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * The screen side of the Mycophant taking the player: a soft mycelial edge, hyphae laced across the view (tapered
 * strands with side branches and amber beads, creeping in during the capture, pulling taut and snapping one by one at
 * the waking), heavy eyelids, and the Pulp Bloom's drifting spores. Everything is smooth per-vertex-colour geometry in
 * the GUI render type, drawn inside one managed flush; timing comes from {@link MycophantIntroClientState}. The capture
 * and waking art belongs to their boss scenes ({@link #renderScene}); the hunt and the bloom draw from the HUD layer
 * ({@link #renderAmbient}).
 */
public final class MycophantMyceliumOverlay {
	private static final int HYPHA_R = 228, HYPHA_G = 216, HYPHA_B = 186;
	private static final int HALO_R = 146, HALO_G = 156, HALO_B = 96;
	private static final int AMBER_R = 238, AMBER_G = 172, AMBER_B = 62;
	private static final int STRAND_STEPS = 26;

	private MycophantMyceliumOverlay() {
	}

	/** The capture's and the waking's own art, drawn by their boss scenes: the murk, the edge, the fibers, the lids. */
	public static void renderScene(GuiGraphics graphics, int width, int height, float partial, float time,
			MycophantIntroClientState state) {
		GuiCanvas canvas = GuiCanvas.of(graphics);
		float dim = state.dimAlpha(partial);
		if (dim > 0.002F) canvas.rect(0, 0, width, height, argb(dim, 4, 6, 3), argb(dim, 4, 6, 3));
		float edge = state.sceneVignette(partial);
		if (edge > 0.002F) vignette(canvas, width, height, edge, time, state.seed(), 10, 15, 7);
		float fibers = state.fiberAlpha(partial);
		if (fibers > 0.002F) {
			for (int f = 0; f < MycophantIntroClientState.FIBERS; f++)
				fiber(canvas, width, height, f, fibers, state.fiberGrowth(f, partial), state.fiberTension(partial),
						state.fiberBreak(f, partial), time, state.seed());
		}
		float open = state.lidOpen(partial);
		if (open < 0.998F) lids(canvas, width, height, open);
	}

	/** What plays outside any scene: the hunt's watching creep and the Pulp Bloom veil. */
	public static void renderAmbient(GuiGraphics graphics, int width, int height, float partial, float time,
			MycophantIntroClientState state) {
		GuiCanvas canvas = GuiCanvas.of(graphics);
		float watch = state.watch(partial);
		if (watch > 0.002F) {
			vignette(canvas, width, height, watch, time, state.seed(), 10, 15, 7);
			if (state.fiberAlpha(partial) <= 0.002F) creep(canvas, width, height, watch, time, state.seed());
		}
		float veil = state.bloomVeil(partial);
		if (veil > 0.002F) {
			canvas.rect(0, 0, width, height, argb(veil * 0.22F, 126, 112, 34), argb(veil * 0.22F, 126, 112, 34));
			vignette(canvas, width, height, veil, time, 3, 92, 82, 22);
			spores(canvas, width, height, veil, time);
		}
	}

	// ------------------------------------------------------------------ edge, lids, spores

	/** A radial fade from a lumpy inner ellipse (clear) to past the corners (dark), in two smooth bands. */
	private static void vignette(GuiCanvas canvas, int width, int height, float strength, float time, int seed, int r, int g,
			int b) {
		int segments = 56;
		float cx = width * 0.5F, cy = height * 0.5F;
		float inner = Mth.lerp(strength, 1.08F, 0.46F);
		float alpha = Math.min(1.0F, strength * 0.95F);
		int clear = argb(0.0F, r, g, b), mid = argb(alpha * 0.55F, r, g, b), dark = argb(alpha, r, g, b);
		for (int i = 0; i < segments; i++) {
			float a0 = Mth.TWO_PI * i / segments, a1 = Mth.TWO_PI * (i + 1) / segments;
			float in0 = inner + lump(a0, time, seed) * 0.07F * strength, in1 = inner + lump(a1, time, seed) * 0.07F * strength;
			float mid0 = in0 + (1.55F - in0) * 0.42F, mid1 = in1 + (1.55F - in1) * 0.42F;
			float c0 = Mth.cos(a0) * cx, s0 = Mth.sin(a0) * cy, c1 = Mth.cos(a1) * cx, s1 = Mth.sin(a1) * cy;
			canvas.quad(cx + c0 * in0, cy + s0 * in0, clear, cx + c1 * in1, cy + s1 * in1, clear,
					cx + c1 * mid1, cy + s1 * mid1, mid, cx + c0 * mid0, cy + s0 * mid0, mid);
			canvas.quad(cx + c0 * mid0, cy + s0 * mid0, mid, cx + c1 * mid1, cy + s1 * mid1, mid,
					cx + c1 * 1.55F, cy + s1 * 1.55F, dark, cx + c0 * 1.55F, cy + s0 * 1.55F, dark);
		}
	}

	private static float lump(float angle, float time, int seed) {
		return Mth.sin(angle * 3.0F + time * 0.04F + seed) * 0.6F + Mth.sin(angle * 7.0F - time * 0.07F + seed * 0.3F) * 0.4F;
	}

	/**
	 * Heavy eyelids, curved like an eye opening, with a soft edge. Light through them reads blood-dark, not black.
	 * {@code open} 1 = fully open, 0 = shut.
	 */
	private static void lids(GuiCanvas canvas, int width, int height, float open) {
		int columns = 40;
		float feather = height * 0.16F;
		float base = Mth.lerp(open, height * 0.5F + feather, -feather - 4.0F);
		float bulge = height * 0.12F * (1.0F - open);
		int solid = argb(1.0F, 20, 4, 4), soft = argb(0.0F, 20, 4, 4);
		for (int i = 0; i < columns; i++) {
			float x0 = width * (float) i / columns, x1 = width * (float) (i + 1) / columns;
			float u0 = 2.0F * i / columns - 1.0F, u1 = 2.0F * (i + 1) / columns - 1.0F;
			float e0 = base + bulge * (1.0F - u0 * u0), e1 = base + bulge * (1.0F - u1 * u1);
			// upper lid, then its soft edge
			canvas.quad(x0, -1.0F, solid, x0, e0, solid, x1, e1, solid, x1, -1.0F, solid);
			canvas.quad(x0, e0, solid, x0, e0 + feather, soft, x1, e1 + feather, soft, x1, e1, solid);
			// lower lid, mirrored
			float b0 = height - e0, b1 = height - e1;
			canvas.quad(x0, b0, solid, x0, height + 1.0F, solid, x1, height + 1.0F, solid, x1, b1, solid);
			canvas.quad(x0, b0 - feather, soft, x0, b0, solid, x1, b1, solid, x1, b1 - feather, soft);
		}
	}

	/** The Pulp Bloom's spores drifting down through the view and twinkling. */
	private static void spores(GuiCanvas canvas, int width, int height, float veil, float time) {
		for (int i = 0; i < 70; i++) {
			float x = hash(11, i, 0) * width + Mth.sin(time * 0.05F + i) * 10.0F;
			float y = Mth.positiveModulo(hash(11, i, 1) * height + time * (0.25F + hash(11, i, 2) * 0.55F), height);
			float size = 0.8F + (i % 3) * 0.6F;
			float twinkle = 0.55F + 0.45F * Mth.sin(time * 0.22F + i * 1.3F);
			canvas.mote(x, y, size, argb(veil * twinkle * 0.85F, 214, 192, 84));
		}
	}

	// ------------------------------------------------------------------ the hyphae

	/**
	 * One binding strand between two screen edges. It creeps in from both anchors ({@code growth}), straightens and
	 * trembles under {@code tension}, and when it breaks its halves recoil toward their anchors, curl and fade.
	 */
	private static void fiber(GuiCanvas canvas, int width, int height, int f, float alpha, float growth, float tension,
			float snap, float time, int seed) {
		if (growth <= 0.001F) return;
		float min = Math.min(width, height);
		float[] p0 = edgePoint(width, height, f % 4, 0.12F + hash(seed, f, 0) * 0.76F);
		int otherSide = (f % 4 + 1 + (int) (hash(seed, f, 1) * 2.99F)) % 4;
		float[] p2 = edgePoint(width, height, otherSide, 0.12F + hash(seed, f, 2) * 0.76F);
		float mx = (p0[0] + p2[0]) * 0.5F, my = (p0[1] + p2[1]) * 0.5F;
		float bend = (hash(seed, f, 3) - 0.5F) * 0.3F * min;
		float dx = p2[0] - p0[0], dy = p2[1] - p0[1];
		float len = Math.max(1.0F, Mth.sqrt(dx * dx + dy * dy));
		float[] c = {
				Mth.lerp(0.28F, mx, width * 0.5F) - dy / len * bend,
				Mth.lerp(0.28F, my, height * 0.5F) + dx / len * bend + height * 0.1F * (1.0F - tension) };
		Curve curve = new Curve(p0, c, p2, tension, time, f);
		float width0 = (1.6F + hash(seed, f, 4) * 1.3F) * Math.max(1.0F, min / 260.0F);
		float breakAt = 0.36F + hash(seed, f, 5) * 0.28F;
		float recoil = 1.0F - (1.0F - snap) * (1.0F - snap);
		float curl = (hash(seed, f, 6) < 0.5F ? -1.0F : 1.0F) * recoil * 0.12F * min;
		float fade = alpha * (1.0F - snap * snap);
		if (fade <= 0.002F) return;
		float reachA = Math.min(breakAt * growth, breakAt * (1.0F - 0.82F * recoil));
		float reachB = Math.min((1.0F - breakAt) * growth, (1.0F - breakAt) * (1.0F - 0.82F * recoil));
		boolean whole = snap <= 0.0F && growth >= 1.0F;
		if (whole) {
			strand(canvas, curve, 0.0F, 1.0F, width0, fade, false, false, 0.0F, 0.0F);
		} else {
			strand(canvas, curve, 0.0F, reachA, width0, fade, false, true, curl, 0.0F);
			strand(canvas, curve, 1.0F - reachB, 1.0F, width0, fade, true, false, -curl, 1.0F);
		}
		// side hyphae and amber beads ride on whichever half carries them
		for (int k = 0; k < 2; k++) {
			float at = k == 0 ? breakAt * 0.45F : 1.0F - (1.0F - breakAt) * 0.4F;
			boolean first = at < breakAt;
			if (first ? at > reachA : at < 1.0F - reachB) continue;
			float bendHere = whole ? 0.0F : (first ? curl : -curl);
			float[] at0 = curve.point(at, whole ? 0.0F : (first ? 0.0F : 1.0F), first ? reachA : reachB, bendHere);
			float[] tangent = curve.tangent(at);
			float turn = (hash(seed, f * 2 + k, 7) < 0.5F ? -1.0F : 1.0F) * (0.8F + hash(seed, f * 2 + k, 8) * 0.4F);
			float branch = (0.05F + hash(seed, f * 2 + k, 9) * 0.06F) * min * Math.min(1.0F, growth * 1.4F);
			branch(canvas, at0, tangent, turn, branch, width0 * 0.6F, fade, time + f);
			float bead = width0 * (1.1F + 0.25F * Mth.sin(time * 0.15F + f + k));
			canvas.mote(at0[0], at0[1], bead * 1.9F, argb(fade * 0.25F, AMBER_R, AMBER_G, AMBER_B));
			canvas.mote(at0[0], at0[1], bead, argb(fade * 0.9F, AMBER_R, AMBER_G, AMBER_B));
		}
		// a puff of spores where it parts
		if (snap > 0.0F && snap < 1.0F) {
			float[] at = curve.point(breakAt, 0.0F, 1.0F, 0.0F);
			for (int m = 0; m < 7; m++) {
				float angle = hash(seed, f * 7 + m, 10) * Mth.TWO_PI;
				float reach = recoil * (0.04F + hash(seed, f * 7 + m, 11) * 0.06F) * min;
				canvas.mote(at[0] + Mth.cos(angle) * reach, at[1] + Mth.sin(angle) * reach + snap * snap * 6.0F,
						1.6F * (1.0F - snap) + 0.4F, argb(alpha * (1.0F - snap), AMBER_R, AMBER_G, AMBER_B));
			}
		}
	}

	/** The hunt's watching: short hyphae creep in from the corners and mid-edges of the view. */
	private static void creep(GuiCanvas canvas, int width, int height, float watch, float time, int seed) {
		float min = Math.min(width, height);
		for (int i = 0; i < 6; i++) {
			float[] from = switch (i) {
				case 0 -> new float[] { -2.0F, -2.0F };
				case 1 -> new float[] { width + 2.0F, -2.0F };
				case 2 -> new float[] { width + 2.0F, height + 2.0F };
				case 3 -> new float[] { -2.0F, height + 2.0F };
				case 4 -> new float[] { -2.0F, height * (0.35F + hash(seed, i, 0) * 0.3F) };
				default -> new float[] { width + 2.0F, height * (0.35F + hash(seed, i, 0) * 0.3F) };
			};
			float tx = width * 0.5F - from[0], ty = height * 0.5F - from[1];
			float len = Mth.sqrt(tx * tx + ty * ty);
			float turn = (hash(seed, i, 1) - 0.5F) * 0.9F;
			branch(canvas, from, new float[] { tx / len, ty / len }, turn, min * (0.12F + hash(seed, i, 2) * 0.1F) * watch,
					1.8F * Math.max(1.0F, min / 260.0F), watch, time + i);
		}
	}

	/** A tapering side hypha that leaves {@code from} at {@code turn} radians off {@code dir} and bends as it goes. */
	private static void branch(GuiCanvas canvas, float[] from, float[] dir, float turn, float length, float width0, float alpha,
			float time) {
		if (length < 1.0F) return;
		int steps = 10;
		float angle = (float) Math.atan2(dir[1], dir[0]) + turn;
		float x = from[0], y = from[1];
		float[] xs = new float[steps + 1], ys = new float[steps + 1];
		xs[0] = x;
		ys[0] = y;
		for (int i = 1; i <= steps; i++) {
			angle -= turn * 0.08F + Mth.sin(time * 0.06F + i) * 0.03F;
			x += Mth.cos(angle) * length / steps;
			y += Mth.sin(angle) * length / steps;
			xs[i] = x;
			ys[i] = y;
		}
		for (int i = 0; i < steps; i++) {
			float w0 = width0 * (1.0F - (float) i / steps), w1 = width0 * (1.0F - (float) (i + 1) / steps);
			canvas.segment(xs[i], ys[i], xs[i + 1], ys[i + 1], w0 * 2.4F, w1 * 2.4F, argb(alpha * 0.2F, HALO_R, HALO_G, HALO_B));
			canvas.segment(xs[i], ys[i], xs[i + 1], ys[i + 1], w0, w1, argb(alpha * 0.85F, HYPHA_R, HYPHA_G, HYPHA_B));
		}
	}

	/**
	 * Draws the curve from {@code t0} to {@code t1} as a tapered ribbon: thicker at the screen anchors, thinning toward
	 * the middle, and pinched to a point at a free (grown or snapped) end. The free end bends by {@code curl}.
	 */
	private static void strand(GuiCanvas canvas, Curve curve, float t0, float t1, float width0, float alpha, boolean freeStart,
			boolean freeEnd, float curl, float anchor) {
		if (t1 - t0 < 0.004F) return;
		float reach = Math.abs(t1 - t0);
		float[] prev = null;
		float prevW = 0.0F;
		for (int i = 0; i <= STRAND_STEPS; i++) {
			float t = Mth.lerp((float) i / STRAND_STEPS, t0, t1);
			float[] p = curve.point(t, anchor, reach, curl);
			float w = width0 * (0.5F + 0.5F * Math.abs(2.0F * t - 1.0F));
			float local = (float) i / STRAND_STEPS;
			if (freeEnd) w *= Math.min(1.0F, (1.0F - local) / 0.22F);
			if (freeStart) w *= Math.min(1.0F, local / 0.22F);
			if (prev != null) {
				canvas.segment(prev[0], prev[1], p[0], p[1], prevW * 2.6F, w * 2.6F, argb(alpha * 0.22F, HALO_R, HALO_G, HALO_B));
				canvas.segment(prev[0], prev[1], p[0], p[1], prevW, w, argb(alpha * 0.9F, HYPHA_R, HYPHA_G, HYPHA_B));
			}
			prev = p;
			prevW = w;
		}
	}

	private static float[] edgePoint(int width, int height, int side, float u) {
		return switch (side) {
			case 0 -> new float[] { u * width, -3.0F };
			case 1 -> new float[] { width + 3.0F, u * height };
			case 2 -> new float[] { u * width, height + 3.0F };
			default -> new float[] { -3.0F, u * height };
		};
	}

	/** A quadratic strand that trembles under tension; recoiling halves bend away from their anchor. */
	private record Curve(float[] p0, float[] c, float[] p2, float tension, float time, int index) {
		float[] point(float t, float anchor, float reach, float curl) {
			float u = 1.0F - t;
			float x = u * u * p0[0] + 2.0F * u * t * c[0] + t * t * p2[0];
			float y = u * u * p0[1] + 2.0F * u * t * c[1] + t * t * p2[1];
			float[] tan = tangent(t);
			float tremble = tension * tension * 1.8F * Mth.sin(time * 1.7F + index * 2.1F + t * 9.0F) * Mth.sin(t * Mth.PI);
			float away = reach <= 0.0F ? 0.0F : Math.abs(t - anchor) / reach;
			float bend = curl * away * away + tremble;
			return new float[] { x - tan[1] * bend, y + tan[0] * bend };
		}

		float[] tangent(float t) {
			float dx = 2.0F * (1.0F - t) * (c[0] - p0[0]) + 2.0F * t * (p2[0] - c[0]);
			float dy = 2.0F * (1.0F - t) * (c[1] - p0[1]) + 2.0F * t * (p2[1] - c[1]);
			float len = Math.max(1.0E-4F, Mth.sqrt(dx * dx + dy * dy));
			return new float[] { dx / len, dy / len };
		}
	}

	private static int argb(float alpha, int r, int g, int b) {
		return GuiCanvas.argb(alpha, r, g, b);
	}

	private static float hash(int seed, int a, int b) {
		return GuiCanvas.hash(seed, a, b);
	}
}
