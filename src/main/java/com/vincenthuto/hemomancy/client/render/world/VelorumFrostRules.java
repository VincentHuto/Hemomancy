package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Velorum's pure visual rules (visual pass 2, 2026-11-06), JVM-tested: the life of a telegraph from its reveal to
 * its last frost, the seam networks his floor freezes along, the sunken trench as the floor plane shows it from a
 * camera, the shards and plates of ice that stand out of it with their punch, hold and crumble, the chips they throw,
 * and the timing of his slashes, folds, icicles and shells. Lengths are blocks and times ticks; local points are
 * {along, right} in a footprint's frame.
 */
final class VelorumFrostRules {
	/** The commit's flash. */
	static final float FLASH = 4.0F;
	/** The frost holds and then sublimes over this many ticks after the active span. */
	static final float LINGER = 16.0F;
	/** Ice punches past its height over PUNCH ticks and settles back over SETTLE. */
	static final float PUNCH = 2.5F, SETTLE = 3.0F;
	static final double OVERSHOOT = 1.12D;
	/** Ice crumbles and sublimes from its point down over this many ticks. */
	static final float CRUMBLE = 10.0F;
	/** A chip flies for CHIP_FLIGHT ticks, skitters, and is gone by CHIP_LIFE. */
	static final float CHIP_FLIGHT = 6.0F, CHIP_LIFE = 16.0F;
	/** A Black-Ice trench sinks this deep through the wind-up; the black ice fills it to this far under the floor. */
	static final double TRENCH_DEPTH = 0.42D, TRENCH_FILLED = 0.12D;
	/** Behind a black-ice front the trench fills over this many blocks. */
	static final double WELL = 2.5D;
	/** A frost shock runs out over this many ticks. */
	static final float SHOCK = 8.0F;
	/** A slash shows from this many ticks before the commit, and frays over this many after its sweep ends. */
	static final float SLASH_LEAD = 3.0F, SLASH_HOLD = 8.0F;
	/** The Splinter Crown's icicles hang this high and fall over the wind-up's last ICICLE_DROP ticks. */
	static final double ICICLE_HEIGHT = 6.5D;
	static final float ICICLE_DROP = 4.0F;
	/** The Long Night's folds sink over FOLD_SINK ticks after the commit while the dark floods out over DARK_WAVE. */
	static final float FOLD_SINK = 10.0F, DARK_WAVE = 10.0F;
	/** A preserving shell grows over SHELL_GROW ticks and breaks over SHELL_BREAK. */
	static final float SHELL_GROW = 3.0F, SHELL_BREAK = 12.0F;
	/** Seam gap widths: a main seam, a lip or edge, a branch. A seam's strip is FEATHER times its width each side. */
	static final double MAIN = 0.10D, EDGE = 0.075D, BRANCH = 0.045D, FEATHER = 3.4D;

	private VelorumFrostRules() {
	}

	// ── the life of a telegraph ──────────────────────────────────────

	/**
	 * How a telegraph looks at one moment: how far its seams have run ({@code grow}), the frost, the black ice, the cold
	 * glint, the width of its seams and its strength.
	 */
	record Look(float grow, float frost, float ice, float glint, float width, float alpha) {
		static final Look NONE = new Look(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

		boolean visible() {
			return alpha > 0.004F;
		}

		Look scaled(float strength) {
			return new Look(grow, frost, ice, glint, width, alpha * strength);
		}

		Look withGrow(float value) {
			return new Look(value, frost, ice, glint, width, alpha);
		}

		Look withIce(float value) {
			return new Look(grow, frost, value, glint, width, alpha);
		}

		Look withGlint(float value) {
			return new Look(grow, frost, ice, value, width, alpha);
		}

		Look withWidth(float value) {
			return new Look(grow, frost, ice, glint, value, alpha);
		}
	}

	/**
	 * The same life for every move. Build (reveal to commit): the seams run their whole length by 65% of the reveal, the
	 * frost thickens from 0.2, black ice starts filming over the second half, the cold glint climbs. Flash (FLASH ticks):
	 * the glint runs white, the seams yawn 30% wider. Active: full frost and ice, the glint settling. Linger (LINGER ticks
	 * after the active span, or the flash if longer): the seams freeze shut, the ice sublimes and the whole fades over
	 * the last 65%.
	 */
	static Look look(float moveTime, int reveal, int windup, int active) {
		return look(moveTime, reveal, windup, active, Integer.MAX_VALUE / 4);
	}

	/** The same, with the linger shortened to fit a short recovery (the Repose deed's), so the frost never cuts off. */
	static Look look(float moveTime, int reveal, int windup, int active, int recovery) {
		if (moveTime < reveal) return Look.NONE;
		float age = moveTime - windup;
		if (age < 0.0F) {
			float b = windup <= reveal ? 1.0F : HemoEasing.clamp01((moveTime - reveal) / (float) (windup - reveal));
			float late = HemoEasing.clamp01((b - 0.5F) / 0.5F);
			return new Look(HemoEasing.clamp01(b / 0.65F), 0.2F + 0.75F * b, 0.55F * late * late, 0.12F + 0.38F * b * b,
					0.55F + 0.45F * b, HemoEasing.smoothstep(HemoEasing.clamp01(b / 0.12F)));
		}
		if (age < FLASH) {
			float f = age / FLASH;
			return new Look(1.0F, 1.0F, 0.75F + 0.25F * f, 1.0F - 0.45F * f, 1.3F - 0.3F * f, 1.0F);
		}
		float held = Math.max(active, FLASH);
		if (age < held) return new Look(1.0F, 1.0F, 1.0F, 0.55F, 1.0F, 1.0F);
		float linger = Math.max(4.0F, Math.min(LINGER, active + (float) recovery - held));
		float t = (age - held) / linger;
		if (t >= 1.0F) return Look.NONE;
		float fade = 1.0F - HemoEasing.smoothstep(HemoEasing.clamp01((t - 0.35F) / 0.65F));
		return new Look(1.0F, 1.0F - 0.3F * t, 1.0F - 0.6F * t, 0.55F * (1.0F - t) * (1.0F - t), 1.0F - 0.45F * t, fade);
	}

	/** How much of a rime front at {@code front} covers a point {@code distance} out, softened over {@code soft}. */
	static float reach(double distance, double front, double soft) {
		return (float) HemoEasing.clamp01((front - distance) / Math.max(1.0E-3D, soft) + 0.5D);
	}

	// ── seams ────────────────────────────────────────────────────────

	/** One frozen seam: local points, its gap width, and when it starts running and over how much of the grow. */
	record Seam(double[] x, double[] y, double width, double start, double span) {
		int size() {
			return x.length;
		}

		double length() {
			double total = 0.0D;
			for (int i = 1; i < x.length; i++) total += Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
			return total;
		}
	}

	/** How much of a seam shows at {@code grow}. */
	static double shown(float grow, double start, double span) {
		if (span <= 0.0D) return grow >= start ? 1.0D : 0.0D;
		return HemoEasing.clamp01((grow - start) / span);
	}

	/** {@code count} seams running out from {@code inner} to {@code outer}, every other one forking once on its way. */
	static List<Seam> radial(double inner, double outer, int count, double phaseDegrees, int seed) {
		List<Seam> out = new ArrayList<>(count * 2);
		for (int i = 0; i < count; i++) {
			double a = Math.toRadians(phaseDegrees + i * 360.0D / count + (unit(seed, i) - 0.5D) * 8.0D);
			out.add(jagged(inner, outer, a, 7, MAIN, 0.0D, 1.0D, seed + i * 13));
			if (i % 2 == 0) {
				double fraction = 0.35D + 0.25D * unit(seed, 40 + i);
				double split = inner + (outer - inner) * fraction;
				double turn = (unit(seed, 60 + i) < 0.5D ? -1.0D : 1.0D) * Math.toRadians(14.0D + 10.0D * unit(seed, 80 + i));
				out.add(branch(split, a, turn, (outer - split) * 0.55D, fraction, seed + i * 17));
			}
		}
		return out;
	}

	/** The same seams running in from the rim toward the centre, so they grow inward. */
	static List<Seam> inward(double inner, double outer, int count, double phaseDegrees, int seed) {
		List<Seam> out = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			double a = Math.toRadians(phaseDegrees + i * 360.0D / count + (unit(seed, i) - 0.5D) * 8.0D);
			out.add(reversed(jagged(inner, outer, a, 7, MAIN, 0.0D, 1.0D, seed + i * 13)));
		}
		return out;
	}

	/** Seams fanning across a cone from his heel: radials at its edges and between, and a broken outer arc. */
	static List<Seam> cone(double inner, double radius, double fromDegrees, double toDegrees, int seed) {
		List<Seam> out = new ArrayList<>();
		int radials = Math.max(3, (int) Math.ceil((toDegrees - fromDegrees) / 14.0D)) + 1;
		double r0 = Math.max(0.4D, inner);
		for (int i = 0; i < radials; i++) {
			double bearing = fromDegrees + (toDegrees - fromDegrees) * i / (radials - 1);
			boolean edge = i == 0 || i == radials - 1;
			out.add(jagged(r0, radius, Math.toRadians(bearing), 6, edge ? EDGE : MAIN, 0.0D, 1.0D, seed + i * 11));
		}
		double step = 26.0D;
		for (double from = fromDegrees; from < toDegrees - 4.0D; from += step) {
			out.addAll(arc(radius, from, Math.min(toDegrees, from + step - 4.0D), EDGE, 0.55D, 0.45D, seed + (int) from));
		}
		return out;
	}

	/** One seam along an arc of {@code radius}, a point every six degrees with a little radial wander. */
	static List<Seam> arc(double radius, double fromDegrees, double toDegrees, double width, double start, double span, int seed) {
		int points = Math.max(2, (int) Math.ceil((toDegrees - fromDegrees) / 6.0D) + 1);
		double[] x = new double[points], y = new double[points];
		for (int k = 0; k < points; k++) {
			double a = Math.toRadians(fromDegrees + (toDegrees - fromDegrees) * k / (points - 1));
			double r = radius + (unit(seed, 200 + k) - 0.5D) * 0.1D;
			x[k] = Math.cos(a) * r;
			y[k] = Math.sin(a) * r;
		}
		return List.of(new Seam(x, y, width, start, span));
	}

	/** A seam all round a circle, growing round from the aim. */
	static List<Seam> ring(double radius, int seed) {
		return arc(radius, -180.0D, 180.0D, EDGE, 0.0D, 1.0D, seed);
	}

	/** A small star of seams, no forks. */
	static List<Seam> star(double radius, int count, int seed) {
		List<Seam> out = new ArrayList<>(count);
		double phase = unit(seed, 900) * 360.0D;
		for (int i = 0; i < count; i++) {
			double a = Math.toRadians(phase + i * 360.0D / count + (unit(seed, 910 + i) - 0.5D) * 20.0D);
			double r = radius * (0.65D + 0.35D * unit(seed, 930 + i));
			out.add(jagged(0.12D, r, a, 4, EDGE, 0.0D, 1.0D, seed + i * 7));
		}
		return out;
	}

	/**
	 * The lips of a trench from {@code a0} to {@code length} at {@code half} either side, freezing outward as they run,
	 * with short fractures leaving them every block and a third.
	 */
	static List<Seam> lips(double a0, double length, double half, int seed) {
		List<Seam> out = new ArrayList<>();
		int points = Math.max(2, (int) Math.ceil(length - a0) + 1);
		for (int side = -1; side <= 1; side += 2) {
			double[] x = new double[points], y = new double[points];
			for (int k = 0; k < points; k++) {
				x[k] = a0 + (length - a0) * k / (points - 1);
				y[k] = side * half + (k == 0 || k == points - 1 ? 0.0D : (unit(seed, 300 + k + side * 50) - 0.5D) * 0.06D);
			}
			out.add(new Seam(x, y, EDGE, 0.0D, 1.0D));
			for (double along = a0 + 0.6D; along < length - 0.5D; along += 1.3D) {
				double lean = 0.15D + 0.25D * unit(seed, (int) (along * 10.0D) + side * 7);
				double start = Math.min(0.85D, (along - a0) / Math.max(0.01D, length - a0) * 0.85D);
				out.add(new Seam(new double[]{along, along + lean * 0.5D, along + lean},
						new double[]{side * half, side * (half + 0.22D), side * (half + 0.48D)}, BRANCH, start, 0.15D));
			}
		}
		return out;
	}

	/** A seam from radius r0 to r1 on bearing {@code a} (radians from the aim, positive right), wandering across its line. */
	private static Seam jagged(double r0, double r1, double a, int points, double width, double start, double span, int seed) {
		double[] x = new double[points], y = new double[points];
		double ca = Math.cos(a), sa = Math.sin(a);
		for (int k = 0; k < points; k++) {
			double t = k / (double) (points - 1);
			double r = r0 + (r1 - r0) * t;
			double off = k == 0 || k == points - 1 ? 0.0D : (unit(seed, k) - 0.5D) * 0.22D;
			x[k] = ca * r - sa * off;
			y[k] = sa * r + ca * off;
		}
		return new Seam(x, y, width, start, span);
	}

	/** A fork leaving a seam at radius {@code from} on bearing {@code a}, turned by {@code turn}. */
	private static Seam branch(double from, double a, double turn, double length, double start, int seed) {
		double sx = Math.cos(a) * from, sy = Math.sin(a) * from;
		double h = a + turn;
		int n = 4;
		double[] x = new double[n], y = new double[n];
		for (int k = 0; k < n; k++) {
			double t = k / (double) (n - 1);
			double off = k == 0 ? 0.0D : (unit(seed, k) - 0.5D) * 0.12D;
			x[k] = sx + Math.cos(h) * length * t - Math.sin(h) * off;
			y[k] = sy + Math.sin(h) * length * t + Math.cos(h) * off;
		}
		double begin = Math.min(0.9D, start);
		return new Seam(x, y, BRANCH, begin, 1.0D - begin);
	}

	private static Seam reversed(Seam seam) {
		int n = seam.size();
		double[] x = new double[n], y = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = seam.x()[n - 1 - i];
			y[i] = seam.y()[n - 1 - i];
		}
		return new Seam(x, y, seam.width(), seam.start(), seam.span());
	}

	// ── the trench ───────────────────────────────────────────────────

	/** A point of a trench face where the floor plane shows it: {along, right} in the lane's frame, and V on the face. */
	record TrenchPoint(double a, double r, double v) {
	}

	/** A trench face: a wall (V from the lip down) or the bed (V 1), as a convex polygon on the floor plane. */
	record Face(boolean wall, List<TrenchPoint> points) {
	}

	/**
	 * The faces of a trench {@code depth} deep, cut from {@code a0} to {@code a1} and {@code -half} to {@code half} in a
	 * lane's frame, as the floor plane shows them to a camera at {cAlong, cRight}, {@code height} above the floor. A
	 * point under the floor shows where the view ray to it crosses the floor plane (a scaling about the camera's foot by
	 * height / (height + depth)), and every face is clipped to the trench's mouth, so the floor hides what it would: the
	 * near walls and the bed under the near lip. A wall shows only when its inner face is turned to the camera; an end
	 * wall only when {@code nearEnd} or {@code farEnd} asks for it (two trenches of different depth meet without one).
	 * The bed comes first, then the walls, which never overlap it.
	 */
	static List<Face> trench(double a0, double a1, double half, double depth, double cAlong, double cRight, double height,
			boolean nearEnd, boolean farEnd) {
		List<Face> faces = new ArrayList<>(5);
		if (a1 - a0 < 1.0E-3D || half <= 0.0D || height <= 0.05D) return faces;
		if (depth <= 1.0E-4D) {
			faces.add(new Face(false, List.of(new TrenchPoint(a0, -half, 1.0D), new TrenchPoint(a1, -half, 1.0D),
					new TrenchPoint(a1, half, 1.0D), new TrenchPoint(a0, half, 1.0D))));
			return faces;
		}
		double k = height / (height + depth);
		List<TrenchPoint> bed = clip(List.of(below(a0, -half, cAlong, cRight, k), below(a1, -half, cAlong, cRight, k),
				below(a1, half, cAlong, cRight, k), below(a0, half, cAlong, cRight, k)), a0, a1, -half, half);
		if (bed.size() >= 3) faces.add(new Face(false, bed));
		if (cRight < half) wall(faces, a0, half, a1, half, cAlong, cRight, k, a0, a1, half);
		if (cRight > -half) wall(faces, a1, -half, a0, -half, cAlong, cRight, k, a0, a1, half);
		if (farEnd && cAlong < a1) wall(faces, a1, half, a1, -half, cAlong, cRight, k, a0, a1, half);
		if (nearEnd && cAlong > a0) wall(faces, a0, -half, a0, half, cAlong, cRight, k, a0, a1, half);
		return faces;
	}

	private static TrenchPoint below(double a, double r, double cAlong, double cRight, double k) {
		return new TrenchPoint(cAlong + (a - cAlong) * k, cRight + (r - cRight) * k, 1.0D);
	}

	private static void wall(List<Face> faces, double pa, double pr, double qa, double qr, double cAlong, double cRight,
			double k, double a0, double a1, double half) {
		List<TrenchPoint> quad = List.of(new TrenchPoint(pa, pr, 0.0D), new TrenchPoint(qa, qr, 0.0D),
				below(qa, qr, cAlong, cRight, k), below(pa, pr, cAlong, cRight, k));
		List<TrenchPoint> clipped = clip(quad, a0, a1, -half, half);
		if (clipped.size() >= 3) faces.add(new Face(true, clipped));
	}

	/** A convex polygon clipped to the box a0..a1 by b0..b1 (Sutherland-Hodgman), V carried along the cut edges. */
	static List<TrenchPoint> clip(List<TrenchPoint> polygon, double a0, double a1, double b0, double b1) {
		List<TrenchPoint> out = clipEdge(polygon, true, a0, true);
		out = clipEdge(out, true, a1, false);
		out = clipEdge(out, false, b0, true);
		return clipEdge(out, false, b1, false);
	}

	private static List<TrenchPoint> clipEdge(List<TrenchPoint> in, boolean along, double bound, boolean keepAbove) {
		List<TrenchPoint> out = new ArrayList<>(in.size() + 2);
		int n = in.size();
		for (int i = 0; i < n; i++) {
			TrenchPoint current = in.get(i), previous = in.get((i + n - 1) % n);
			boolean currentIn = inside(current, along, bound, keepAbove), previousIn = inside(previous, along, bound, keepAbove);
			if (currentIn) {
				if (!previousIn) out.add(cross(previous, current, along, bound));
				out.add(current);
			} else if (previousIn) {
				out.add(cross(previous, current, along, bound));
			}
		}
		return out;
	}

	private static boolean inside(TrenchPoint point, boolean along, double bound, boolean keepAbove) {
		double c = along ? point.a() : point.r();
		return keepAbove ? c >= bound - 1.0E-9D : c <= bound + 1.0E-9D;
	}

	private static TrenchPoint cross(TrenchPoint p, TrenchPoint q, boolean along, double bound) {
		double pc = along ? p.a() : p.r(), qc = along ? q.a() : q.r();
		double t = Math.abs(qc - pc) < 1.0E-12D ? 0.0D : (bound - pc) / (qc - pc);
		return new TrenchPoint(p.a() + (q.a() - p.a()) * t, p.r() + (q.r() - p.r()) * t, p.v() + (q.v() - p.v()) * t);
	}

	// ── ice ──────────────────────────────────────────────────────────

	/**
	 * One shard standing in a cluster: its foot's offset, height, girth, how flat its cross-section is (1 round, a small
	 * value a blade), the turn of its blade, its lean per block of height and its facets.
	 */
	record Shard(double dx, double dz, double height, double radius, double flat, double yaw, double leanX, double leanZ,
			int sides) {
	}

	/**
	 * A cluster of ice: a tall central shard and {@code count - 1} shorter ones round it, splaying out, each with its own
	 * girth, lean and blade. Girth goes with the square root of the scale, so a big shard has mass.
	 */
	static List<Shard> cluster(int seed, double scale, int count, double flat, int sides) {
		List<Shard> out = new ArrayList<>(count);
		double girth = 0.17D * Math.sqrt(scale);
		for (int i = 0; i < count; i++) {
			if (i == 0) {
				out.add(new Shard(0.0D, 0.0D, scale * (0.92D + 0.12D * unit(seed, 1)), girth, flat, unit(seed, 2) * Math.PI,
						(unit(seed, 3) - 0.5D) * 0.12D, (unit(seed, 4) - 0.5D) * 0.12D, sides));
				continue;
			}
			double a = i * 2.39996D + unit(seed, 10 + i) * 0.9D;
			double reach = scale * (0.12D + 0.14D * unit(seed, 20 + i));
			double splay = 0.18D + 0.22D * unit(seed, 30 + i);
			out.add(new Shard(Math.cos(a) * reach, Math.sin(a) * reach, scale * (0.42D + 0.32D * unit(seed, 40 + i)),
					girth * (0.55D + 0.25D * unit(seed, 50 + i)), flat, a + unit(seed, 60 + i) - 0.5D, Math.cos(a) * splay,
					Math.sin(a) * splay, sides));
		}
		return out;
	}

	/** The shards leaning further along the horizontal {x, z} by {@code amount} per block of height. */
	static List<Shard> leaned(List<Shard> shards, double x, double z, double amount) {
		List<Shard> out = new ArrayList<>(shards.size());
		for (Shard s : shards) {
			out.add(new Shard(s.dx(), s.dz(), s.height(), s.radius(), s.flat(), s.yaw(), s.leanX() + x * amount,
					s.leanZ() + z * amount, s.sides()));
		}
		return out;
	}

	/** Grown height (0 to the overshoot) of ice that broke out {@code age} ticks ago and crumbles from {@code crumbleAt}. */
	static double height(float age, float crumbleAt) {
		if (age <= 0.0F || age >= crumbleAt + CRUMBLE) return 0.0D;
		if (age < PUNCH) return OVERSHOOT * HemoEasing.easeOutCubic(age / PUNCH);
		if (age < PUNCH + SETTLE) return OVERSHOOT + (1.0D - OVERSHOOT) * HemoEasing.smoothstep((age - PUNCH) / SETTLE);
		return 1.0D;
	}

	/** How much of the ice is still there: whole until {@code crumbleAt}, then subliming from the point down. */
	static float formation(float age, float crumbleAt) {
		if (age <= 0.0F) return 0.0F;
		if (age < crumbleAt) return 1.0F;
		return HemoEasing.clamp01(1.0F - (age - crumbleAt) / CRUMBLE);
	}

	/** The cold light inside ice at its birth, dying away over a few ticks. */
	static float birthGlint(float age) {
		return age <= 0.0F ? 0.0F : (float) Math.exp(-age / 3.0D);
	}

	/**
	 * Chip {@code i} of a burst {@code age} ticks old: {x, y, z, tumble, scale} from the burst's foot, or null. It
	 * leaves on an arc, lands, skitters a little further across the ice and fades.
	 */
	static double[] chip(int seed, int i, float age, double reach) {
		float own = age - (float) unit(seed, 300 + i) * 1.5F;
		if (own <= 0.0F || own >= CHIP_LIFE) return null;
		double a = unit(seed, 310 + i) * Math.PI * 2.0D;
		double distance = reach * (0.45D + 0.55D * unit(seed, 320 + i));
		double up = 0.45D + 0.6D * unit(seed, 330 + i);
		double t = Math.min(1.0D, own / CHIP_FLIGHT);
		double slide = own > CHIP_FLIGHT ? Math.min(1.0D, (own - CHIP_FLIGHT) / 4.0D) : 0.0D;
		double r = distance * (t + 0.18D * HemoEasing.easeOutCubic(slide));
		double y = up * 4.0D * t * (1.0D - t);
		double tumble = own * (0.9D + unit(seed, 340 + i)) * (1.0D - 0.85D * slide);
		double scale = 1.0D - HemoEasing.smoothstep(HemoEasing.clamp01((own - (CHIP_LIFE - 4.0F)) / 4.0F));
		return new double[]{Math.cos(a) * r, y, Math.sin(a) * r, tumble, scale};
	}

	// ── timing ───────────────────────────────────────────────────────

	/** A frost shock's radius {@code age} ticks after it left, or -1 outside its run. */
	static double shockRadius(float age, double reach) {
		if (age < 0.0F || age >= SHOCK) return -1.0D;
		return reach * HemoEasing.easeOutCubic(age / SHOCK);
	}

	/** A slash's strength: whole until its sweep ends at {@code end}, then fading over SLASH_HOLD. */
	static float slashFade(float age, float end) {
		if (age <= end) return 1.0F;
		return HemoEasing.clamp01(1.0F - (age - end) / SLASH_HOLD);
	}

	/** How far a slash has frayed: 0 until its sweep ends, 1 by SLASH_HOLD after. */
	static float slashFray(float age, float end) {
		return HemoEasing.clamp01((age - end) / SLASH_HOLD);
	}

	/** An icicle's height over the floor: hanging until the drop, then falling under gravity to land at the commit. */
	static double icicleDrop(float moveTime, int windup) {
		float t = moveTime - (windup - ICICLE_DROP);
		if (t <= 0.0F) return ICICLE_HEIGHT;
		if (t >= ICICLE_DROP) return 0.0D;
		double f = t / ICICLE_DROP;
		return ICICLE_HEIGHT * (1.0D - f * f);
	}

	/** The Long Night's folds rising through the wind-up, full by its last fifth. */
	static float foldRise(float moveTime, int windup) {
		return HemoEasing.smoothstep(HemoEasing.clamp01(moveTime / Math.max(1.0F, windup * 0.8F)));
	}

	/** The folds sinking into the floor after the commit. */
	static float foldSink(float age) {
		return age <= 0.0F ? 0.0F : HemoEasing.easeInCubic(HemoEasing.clamp01(age / FOLD_SINK));
	}

	/** The dark flooding out across the floor after the commit, from two blocks to {@code reach}; -1 outside its run. */
	static double darkWave(float age, double reach) {
		if (age < 0.0F || age >= DARK_WAVE) return -1.0D;
		return 2.0D + (reach - 2.0D) * HemoEasing.easeOutCubic(age / DARK_WAVE);
	}

	/** How full a trench is {@code behind} blocks behind its black-ice front. */
	static float fill(double behind) {
		return (float) HemoEasing.smoothstep(HemoEasing.clamp01(behind / WELL));
	}

	/** A shell's growth from the feet up, {@code age} ticks after it closed. */
	static float shellGrow(float age) {
		return HemoEasing.smoothstep(HemoEasing.clamp01(age / SHELL_GROW));
	}

	/** A stable value in [0, 1) for a seed and a salt. */
	static double unit(int seed, int salt) {
		int h = seed * 0x27D4EB2D ^ salt * 0x165667B1;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 12;
		h *= 0x297A2D39;
		h ^= h >>> 15;
		return (h >>> 8) / 16777216.0D;
	}
}
