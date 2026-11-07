package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Hemorath's effects as pure numbers (visual pass 2, 2026-11-06): the life of a telegraph from the first crack
 * to the seal, the crack networks each footprint shape breaks into, the iron clusters that punch out of the floor and
 * the debris they throw. No Minecraft types, so JVM tests cover it; visual numbers never decide hits.
 * <p>
 * Crack points are local to a footprint's origin: {@code x} along its aim, {@code y} to its right, in blocks.
 */
public final class HemorathFissureRules {
	// ── the life of a telegraph ──────────────────────────────────────

	/** The commit's flash: the seam runs white-hot and the gap yawns for this many ticks. */
	public static final float FLASH = 4.0F;
	/** After the active span the cracks drain and seal over this many ticks. */
	public static final float LINGER = 14.0F;
	/** The cracks have run out their whole length by this much of the reveal. */
	public static final float CRACK_LEAD = 0.6F;
	/** How much wider the gap stands at the height of the flash. */
	public static final float FLASH_YAWN = 0.35F;
	/** Heat at and above this reads white-hot in the shader. */
	public static final float WHITE_HOT = 0.85F;
	/** A crack strip is this many times the gap's half-width: the rest is its lip and the scorched stone round it. */
	public static final double HALO = 3.3D;

	/**
	 * A telegraph's state at one moment: how far the cracks have run ({@code grow}), the heat of their seams, how far
	 * blood has welled up into them, the rust at their lips, the gap's width factor and the whole thing's strength.
	 */
	public record Look(float grow, float heat, float blood, float rust, float width, float alpha) {
		public static final Look NONE = new Look(0, 0, 0, 0, 0, 0);

		public boolean visible() {
			return alpha > 0.003F && width > 0.003F;
		}
	}

	/**
	 * The cracks at {@code moveTime} ticks into a move that reveals at {@code revealTick} and commits at the end of its
	 * {@code windup}: they run out and brighten through the wind-up while blood wells up into them, flash at the commit,
	 * stand full through the active span, then cool, drain and seal over {@link #LINGER}.
	 */
	public static Look look(float moveTime, int revealTick, int windup, int active) {
		if (moveTime < revealTick) return Look.NONE;
		if (moveTime < windup) {
			float r = windup <= revealTick ? 1.0F : HemoEasing.clamp01((moveTime - revealTick) / (float) (windup - revealTick));
			float grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(r / CRACK_LEAD));
			float heat = 0.12F + 0.58F * r * r;
			float blood = 0.75F * HemoEasing.ramp(r, 0.2F, 1.0F);
			float rust = HemoEasing.ramp(r, 0.0F, 0.6F);
			float width = 0.5F + 0.5F * HemoEasing.easeOutCubic(r);
			float alpha = HemoEasing.ramp(r, 0.0F, 0.12F);
			return new Look(grow, heat, blood, rust, width, alpha);
		}
		float since = moveTime - windup;
		if (since < FLASH) {
			float f = 1.0F - since / FLASH;
			return new Look(1.0F, 0.8F + 0.2F * f, 0.75F + 0.25F * (1.0F - f), 1.0F, 1.0F + FLASH_YAWN * f, 1.0F);
		}
		if (since < active) {
			float settle = HemoEasing.clamp01((since - FLASH) / Math.max(1.0F, active - FLASH));
			return new Look(1.0F, 0.8F - 0.2F * settle, 1.0F, 1.0F, 1.0F, 1.0F);
		}
		float l = (since - Math.max(active, FLASH)) / LINGER;
		if (l >= 1.0F) return Look.NONE;
		float cool = 1.0F - l;
		return new Look(1.0F, 0.6F * cool * cool, 1.0F - HemoEasing.ramp(l, 0.0F, 0.7F), 1.0F,
				1.0F - HemoEasing.ramp(l, 0.35F, 1.0F), 1.0F - HemoEasing.ramp(l, 0.6F, 1.0F));
	}

	/** The last tick a move's cracks show. */
	public static float lastTick(int windup, int active) {
		return windup + Math.max(active, FLASH) + LINGER;
	}

	/** How much of a crack starting at {@code start} of the run and taking {@code span} of it shows at {@code grow}. */
	public static double shown(double grow, double start, double span) {
		if (span <= 0.0D) return grow >= start ? 1.0D : 0.0D;
		return HemoEasing.clamp01((grow - start) / span);
	}

	// ── crack networks ───────────────────────────────────────────────

	/** One crack: its points, the gap's half-width in blocks, and when it starts and how long it takes to run. */
	public record Crack(double[] x, double[] y, double width, double start, double span) {
		public int size() {
			return x.length;
		}

		public double length() {
			double total = 0.0D;
			for (int i = 1; i < x.length; i++) total += Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
			return total;
		}
	}

	/** Gap half-widths: a main fault, a footprint's edge, an interior crack, a branch. */
	public static final double FAULT = 0.14D, EDGE = 0.09D, INNER = 0.075D, BRANCH = 0.05D;

	/** Parallel lane of {@code length} and half-width {@code half} from the origin: a main fault, its edges, side branches and an end cap. */
	public static List<Crack> lane(double length, double half, int seed) {
		List<Crack> out = new ArrayList<>();
		if (length <= 0.2D) return out;
		double fault = Math.min(FAULT, 0.07D + 0.09D * half);
		out.add(jag(0.0D, 0.0D, length, 0.0D, 0.7D, Math.min(0.3D, 0.35D * half + 0.05D), fault, 0.0D, 0.85D, seed, true));
		for (int side = -1; side <= 1; side += 2) {
			out.add(jag(Math.min(0.4D, length * 0.2D), side * half, length, side * half, 0.9D, 0.07D, EDGE * 0.8D, 0.08D, 0.8D,
					seed + 11 * side, true));
		}
		int k = 0;
		for (double x = 0.8D + 0.5D * unit(seed, 3); x < length - 0.5D; x += 1.2D + 0.6D * unit(seed, 40 + k)) {
			double side = (k % 2 == 0 ? 1 : -1) * (unit(seed, 60 + k) < 0.2D ? -1 : 1);
			double reach = half * (0.7D + 0.3D * unit(seed, 80 + k));
			double ahead = half * (0.4D + 0.6D * unit(seed, 100 + k));
			out.add(jag(x, 0.0D, Math.min(length, x + ahead), side * reach, 0.35D, 0.06D, BRANCH, 0.85D * x / length, 0.2D,
					seed + 200 + k, false));
			k++;
		}
		out.add(jag(length, -half, length, half, 0.4D, 0.08D, EDGE * 0.7D, 0.8D, 0.2D, seed + 7, true));
		return out;
	}

	/**
	 * A cone from {@code inner} to {@code radius} between bearings {@code fromDeg} and {@code toDeg} (positive to the
	 * right): its two sides, its outer arc, interior radials and the arcs that join them half way out.
	 */
	public static List<Crack> cone(double inner, double radius, double fromDeg, double toDeg, int seed) {
		List<Crack> out = new ArrayList<>();
		double near = Math.max(inner, 0.35D);
		if (radius <= near || toDeg <= fromDeg) return out;
		boolean full = toDeg - fromDeg >= 359.0D;
		if (!full) {
			out.add(radial(fromDeg, near, radius, EDGE, 0.0D, 0.7D, seed + 1));
			out.add(radial(toDeg, near, radius, EDGE, 0.0D, 0.7D, seed + 2));
		}
		out.add(arc(radius, fromDeg, toDeg, 0.12D, EDGE, full ? 0.45D : 0.55D, 0.4D, seed + 3));
		int radials = full ? (int) Math.max(6, Math.min(14, Math.round(radius * 1.6D)))
				: (int) Math.max(1, Math.round((toDeg - fromDeg) / 28.0D));
		double step = (toDeg - fromDeg) / (full ? radials : radials + 1);
		double[] bearings = new double[radials];
		for (int i = 0; i < radials; i++) {
			double at = fromDeg + step * (full ? i : i + 1) + (unit(seed, 300 + i) - 0.5D) * step * 0.35D;
			bearings[i] = at;
			double reach = radius * (0.72D + 0.28D * unit(seed, 320 + i));
			out.add(radial(at, near + 0.1D, reach, INNER, 0.05D + 0.1D * unit(seed, 340 + i), 0.6D, seed + 360 + i));
		}
		double mid = Math.max(near + 0.4D, radius * 0.55D);
		for (int i = 0; i + 1 < radials || full && i < radials; i++) {
			double a = bearings[i], b = i + 1 < radials ? bearings[i + 1] : bearings[0] + 360.0D;
			if (unit(seed, 400 + i) < 0.35D) continue;
			out.add(arc(mid * (0.9D + 0.2D * unit(seed, 420 + i)), a, b, 0.08D, BRANCH, 0.45D, 0.3D, seed + 440 + i));
		}
		return out;
	}

	/** A disc of {@code radius} round the origin: radials, an outer ring and broken arcs half way out. */
	public static List<Crack> disc(double radius, int seed) {
		return cone(0.0D, radius, -180.0D, 180.0D, seed);
	}

	/** A ring crack at {@code radius} with short cracks running across it, one every two and a half blocks or so. */
	public static List<Crack> ring(double radius, int seed) {
		List<Crack> out = new ArrayList<>();
		if (radius <= 0.2D) return out;
		out.add(arc(radius, -180.0D, 180.0D, 0.15D, EDGE * 1.1D, 0.0D, 0.6D, seed));
		int across = (int) Math.max(6, Math.round(Math.PI * 2.0D * radius / 2.5D));
		for (int i = 0; i < across; i++) {
			double at = -180.0D + 360.0D * (i + unit(seed, 500 + i) * 0.6D) / across;
			double from = radius - 0.5D - 0.6D * unit(seed, 520 + i), to = radius + 0.35D + 0.5D * unit(seed, 540 + i);
			out.add(radial(at, Math.max(0.1D, from), to, BRANCH, 0.3D + 0.3D * unit(seed, 560 + i), 0.3D, seed + 580 + i));
		}
		return out;
	}

	/** A small burst of cracks round a point: five to seven radials with a broken ring, for landings and marks. */
	public static List<Crack> star(double radius, int seed) {
		List<Crack> out = new ArrayList<>();
		if (radius <= 0.1D) return out;
		int arms = 5 + (int) (unit(seed, 1) * 3.0D);
		double turn = unit(seed, 2) * 360.0D;
		for (int i = 0; i < arms; i++) {
			double at = turn + i * 360.0D / arms + (unit(seed, 600 + i) - 0.5D) * 25.0D;
			double reach = radius * (0.75D + 0.4D * unit(seed, 620 + i));
			out.add(radial(at, 0.05D, reach, i % 2 == 0 ? INNER : BRANCH, 0.0D, 0.7D, seed + 640 + i));
		}
		out.add(arc(radius * 0.8D, turn, turn + 360.0D * (0.55D + 0.3D * unit(seed, 3)), 0.08D, BRANCH, 0.4D, 0.4D, seed + 9));
		return out;
	}

	/** The rim of a {@code halfX} by {@code halfY} rectangle round the origin, inset a little, with short cracks running in. */
	public static List<Crack> rim(double halfX, double halfY, int seed) {
		List<Crack> out = new ArrayList<>();
		double inset = 0.25D, x = halfX - inset, y = halfY - inset;
		if (x <= 0.2D || y <= 0.2D) return out;
		double[][] corners = {{-x, -y}, {x, -y}, {x, y}, {-x, y}};
		for (int i = 0; i < 4; i++) {
			double[] a = corners[i], b = corners[(i + 1) % 4];
			out.add(jag(a[0], a[1], b[0], b[1], 0.8D, 0.1D, EDGE, 0.0D, 0.7D, seed + i, true));
			double length = Math.hypot(b[0] - a[0], b[1] - a[1]);
			double nx = -(b[1] - a[1]) / length, ny = (b[0] - a[0]) / length;
			// inward is toward the centre
			if ((a[0] + b[0]) * nx + (a[1] + b[1]) * ny > 0.0D) {
				nx = -nx;
				ny = -ny;
			}
			int count = (int) Math.max(2, length / 2.0D);
			for (int k = 0; k < count; k++) {
				double t = (k + 0.3D + 0.4D * unit(seed, 700 + i * 31 + k)) / count;
				double px = a[0] + (b[0] - a[0]) * t, py = a[1] + (b[1] - a[1]) * t;
				double reach = 0.5D + 0.8D * unit(seed, 760 + i * 31 + k);
				out.add(jag(px, py, px + nx * reach, py + ny * reach, 0.3D, 0.06D, BRANCH, 0.3D, 0.3D, seed + 800 + i * 31 + k,
						false));
			}
		}
		return out;
	}

	/** A crack scored along a path of local points, {@code width} wide. */
	public static List<Crack> path(List<double[]> points, double width, int seed) {
		List<Crack> out = new ArrayList<>();
		for (int i = 0; i + 1 < points.size(); i++) {
			double[] a = points.get(i), b = points.get(i + 1);
			double t0 = i / (double) Math.max(1, points.size() - 1);
			out.add(jag(a[0], a[1], b[0], b[1], 0.6D, 0.06D, width, t0 * 0.8D, 0.25D, seed + i * 13, true));
		}
		return out;
	}

	/** Three deep claw gouges across a cone: arcs at 45, 70 and 95% of its radius, each inset from the cone's sides. */
	public static List<Crack> gouges(double radius, double fromDeg, double toDeg, int seed) {
		List<Crack> out = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			double r = radius * (0.45D + 0.25D * i);
			double inset = (toDeg - fromDeg) * 0.08D * (i + 1);
			out.add(arc(r, fromDeg + inset, toDeg - inset, 0.05D, 0.1D - 0.012D * i, 0.0D, 1.0D, seed + 900 + i));
		}
		return out;
	}

	// ── crack pieces ─────────────────────────────────────────────────

	/** A jagged crack from a to b, a point every {@code step} blocks, wandering up to {@code amp} to either side. */
	static Crack jag(double ax, double ay, double bx, double by, double step, double amp, double width, double start,
			double span, int seed, boolean pinnedEnd) {
		double length = Math.hypot(bx - ax, by - ay);
		int segments = (int) Math.max(2, Math.ceil(length / Math.max(0.1D, step)));
		double nx = length < 1e-6 ? 0.0D : -(by - ay) / length, ny = length < 1e-6 ? 0.0D : (bx - ax) / length;
		double[] x = new double[segments + 1], y = new double[segments + 1];
		double drift = 0.0D;
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments;
			drift = drift * 0.45D + (unit(seed, i) - 0.5D) * 2.0D;
			double envelope = pinnedEnd ? Math.sin(Math.PI * t) : Math.sin(Math.PI * 0.5D * t);
			double off = i == 0 ? 0.0D : HemoEasing.clamp01(Math.abs(drift) / 1.6D) * Math.signum(drift) * amp * envelope;
			x[i] = ax + (bx - ax) * t + nx * off;
			y[i] = ay + (by - ay) * t + ny * off;
		}
		return new Crack(x, y, width, start, span);
	}

	static Crack radial(double degrees, double from, double to, double width, double start, double span, int seed) {
		double r = Math.toRadians(degrees), c = Math.cos(r), s = Math.sin(r);
		return jag(c * from, s * from, c * to, s * to, 0.55D, Math.min(0.22D, 0.06D + 0.03D * (to - from)), width, start, span,
				seed, false);
	}

	static Crack arc(double radius, double fromDeg, double toDeg, double jitter, double width, double start, double span,
			int seed) {
		int segments = (int) Math.max(4, Math.ceil(Math.toRadians(toDeg - fromDeg) * radius / 0.7D));
		double[] x = new double[segments + 1], y = new double[segments + 1];
		double drift = 0.0D;
		for (int i = 0; i <= segments; i++) {
			double a = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
			drift = drift * 0.5D + (unit(seed, i) - 0.5D);
			double r = radius + drift * jitter * 2.0D;
			x[i] = Math.cos(a) * r;
			y[i] = Math.sin(a) * r;
		}
		return new Crack(x, y, width, start, span);
	}

	// ── trenches ─────────────────────────────────────────────────────

	/**
	 * A trench at full drop is this many blocks deep for each block of its width. The fissure shader's
	 * {@code TRENCH_DEPTH} must match.
	 */
	public static final double TRENCH_DEPTH = 1.0D;
	/** The split has run the trench's whole length by this much of the reveal. */
	public static final float TRENCH_SPLIT = 0.45F;
	/** Both ends of a trench shoal up over this many blocks, so the channel closes to a point at its ends. */
	public static final double TRENCH_TAPER = 0.9D;
	/** The blood's leading tongue rises over this many blocks of the channel. */
	public static final double FLOOD_EDGE = 1.5D;
	/** How high the blood stands, as a share of the depth: at the commit, and at the surge after it. Never brimming. */
	public static final float FLOOD_LEVEL = 0.45F, SURGE_LEVEL = 0.62F;
	/** After the active span a trench drains and closes over at most this many ticks (never past the recovery). */
	public static final float TRENCH_CLOSE = 20.0F;
	/** The seam flares this far behind the hit front as the front passes, in blocks. */
	public static final double FRONT_FLARE = 1.5D;

	/**
	 * A trench's state at one point along it: how far the floor there has dropped (0 flat to 1 the full depth), how
	 * high blood stands in the channel (a share of the depth), the heat of the seam at its bottom, how far the slabs
	 * along its lips are heaved (above 1 in the commit's jolt) and the strength.
	 */
	public record Trench(float depth, float blood, float heat, float heave, float alpha) {
		public static final Trench NONE = new Trench(0, 0, 0, 0, 0);

		public boolean visible() {
			return alpha > 0.003F;
		}
	}

	/**
	 * The trench at {@code along} blocks out of one running {@code from} to {@code to}, {@code moveTime} ticks into a
	 * move that reveals at {@code revealTick} and commits at the end of its {@code windup}. Through the wind-up the
	 * floor splits out from his feet and drops while blood floods the channel from the far end back toward him; at the
	 * commit the seam flares white-hot, the blood surges and the slabs jolt; through the active span the seam flares
	 * again just behind the hit front ({@code front} blocks out, negative before it runs); then over {@code close}
	 * ticks the blood drains back out the far end and the channel closes.
	 */
	public static Trench trench(float moveTime, int revealTick, int windup, int active, float close, double along,
			double from, double to, double front) {
		if (moveTime < revealTick || to <= from || along < from - 1e-6 || along > to + 1e-6) return Trench.NONE;
		double span = to - from;
		float f = (float) HemoEasing.clamp01((along - from) / span);
		double ends = Math.min(along - from, to - along) / Math.min(TRENCH_TAPER, span * 0.5D);
		float taper = 0.12F + 0.88F * HemoEasing.smoothstep((float) HemoEasing.clamp01(ends));
		if (moveTime < windup) {
			float r = windup <= revealTick ? 1.0F : HemoEasing.clamp01((moveTime - revealTick) / (float) (windup - revealTick));
			float split = HemoEasing.easeOutCubic(HemoEasing.clamp01(r / TRENCH_SPLIT));
			float reached = HemoEasing.clamp01((split * 1.15F - f) / 0.15F);
			if (reached <= 0.0F) return Trench.NONE;
			float drop = reached * (0.3F + 0.7F * HemoEasing.smoothstep(r)) * taper;
			float flood = HemoEasing.smoothstep(HemoEasing.ramp(r, 0.2F, 0.95F));
			double edge = to - (span + FLOOD_EDGE) * flood;
			float wet = (float) HemoEasing.clamp01((along - edge) / FLOOD_EDGE);
			float alpha = HemoEasing.ramp(r, 0.0F, 0.12F) * HemoEasing.clamp01(reached * 3.0F);
			return new Trench(drop, FLOOD_LEVEL * wet * reached, 0.12F + 0.5F * r * r, drop * 0.8F, alpha);
		}
		float since = moveTime - windup;
		if (since < Math.max(active, FLASH)) {
			float jolt = since < FLASH ? 1.0F - since / FLASH : 0.0F;
			float settle = HemoEasing.clamp01((since - FLASH) / Math.max(1.0F, active - FLASH));
			float heat = since < FLASH ? WHITE_HOT + (1.0F - WHITE_HOT) * jolt : 0.8F - 0.2F * settle;
			if (front >= 0.0D && along <= front) {
				heat = Math.min(1.0F, heat + 0.3F * (float) Math.exp(-(front - along) / FRONT_FLARE));
			}
			float blood = HemoEasing.lerp(FLOOD_LEVEL, SURGE_LEVEL, HemoEasing.clamp01(since / FLASH));
			return new Trench(taper, blood, heat, taper * (1.0F + 0.25F * jolt), 1.0F);
		}
		float l = (since - Math.max(active, FLASH)) / Math.max(1.0F, close);
		if (l >= 1.0F) return Trench.NONE;
		float cool = 1.0F - l;
		// the blood runs back out the way it came, the near end draining first
		double edge = from - FLOOD_EDGE + (span + FLOOD_EDGE) * HemoEasing.ramp(l, 0.0F, 0.55F);
		float wet = (float) HemoEasing.clamp01((along - edge) / FLOOD_EDGE);
		float shut = 1.0F - (float) HemoEasing.easeInOutCubic(HemoEasing.ramp(l, 0.35F, 1.0F));
		return new Trench(taper * shut, SURGE_LEVEL * wet, 0.6F * cool * cool, taper * shut, 1.0F - HemoEasing.ramp(l, 0.75F, 1.0F));
	}

	/** The last tick a move's trench shows. */
	public static float trenchLastTick(int windup, int active, float close) {
		return windup + Math.max(active, FLASH) + Math.max(1.0F, close);
	}

	/** Slabs rest this far under the floor (hidden), and a full heave lifts their hinge this much. */
	public static final double SLAB_REST = -0.03D, SLAB_HEAVE = 0.08D;
	/** The least gap between a slab and the trench's own edge, in blocks. */
	public static final double SLAB_GAP = 0.02D;
	/**
	 * A heaved slab reaches out past the trench's edge at most this many times the trench's half-width, its gap and
	 * the steepest tilt included (the widest slab is 0.74 of the half-width).
	 */
	public static final double SLAB_REACH = 0.9D;

	/**
	 * One slab of floor along a trench's lip: centred {@code along} the trench on {@code side} (-1 left, 1 right),
	 * {@code length} along it, {@code width} out from the lip, {@code thick}; it heaves to {@code tilt} radians with its
	 * trench-side edge up, one end higher by {@code roll}, starting once the trench has dropped past {@code lag}.
	 */
	public record Slab(double along, int side, double length, double width, double thick, double tilt, double roll,
			double lag) {
	}

	/**
	 * The slabs along both lips of a trench running {@code from} to {@code to}, {@code half} blocks to either side:
	 * broken pieces of varied length and width with gaps between them, a few missing. Their size follows the trench's
	 * width, the Iron Rake's lane (half a block) being the unit.
	 */
	public static List<Slab> slabs(double from, double to, double half, int seed) {
		List<Slab> out = new ArrayList<>();
		if (to - from < 0.4D || half <= 0.0D) return out;
		double scale = Math.max(0.5D, Math.min(1.5D, half / 0.5D));
		for (int side = -1; side <= 1; side += 2) {
			int base = 3000 + (side + 1) * 400;
			double at = from + 0.1D + 0.3D * unit(seed, base);
			for (int k = 0; at < to - 0.25D && k < 64; k++) {
				double length = Math.min(to - at, (0.4D + 0.42D * unit(seed, base + 1 + k * 7)) * scale);
				if (length < 0.2D) break;
				if (unit(seed, base + 2 + k * 7) > 0.2D) {
					double width = half * (0.3D + 0.44D * unit(seed, base + 3 + k * 7));
					double thick = (0.1D + 0.06D * unit(seed, base + 4 + k * 7)) * scale;
					double tilt = 0.4D + 0.35D * unit(seed, base + 5 + k * 7);
					double roll = (unit(seed, base + 6 + k * 7) - 0.5D) * 0.6D;
					double lag = 0.3D * unit(seed, base + 8 + k * 7);
					out.add(new Slab(at + length * 0.5D, side, length, width, thick, tilt, roll, lag));
				}
				at += length + (0.05D + 0.1D * unit(seed, base + 9 + k * 7)) * scale;
			}
		}
		return out;
	}

	/** How far a slab is heaved, 0 at rest to about 1.3 in the commit's jolt, for a trench heaved {@code heave}. */
	public static double slabLift(Slab slab, float heave) {
		if (heave <= slab.lag()) return 0.0D;
		return Math.min(1.3D, (heave - slab.lag()) / (1.0D - slab.lag()));
	}

	/**
	 * A slab's eight corners at {@code lift}, as {along, right, height above the floor}: per end (near, far) its top
	 * outer, top inner, bottom inner and bottom outer corners. Hinged on its outer edge, its trench-side edge rises;
	 * at rest it lies just under the floor. No corner ever comes nearer the trench's centre line than its edge
	 * ({@code half} plus {@link #SLAB_GAP}), so the slab frames the footprint and never covers it.
	 */
	public static double[][] slabCorners(Slab slab, double half, double lift) {
		double theta = slab.tilt() * Math.max(0.0D, lift);
		double pivot = SLAB_REST + SLAB_HEAVE * HemoEasing.clamp01(lift);
		double outer = half + SLAB_GAP + slab.width();
		double[][] c = new double[8][];
		double least = Double.MAX_VALUE;
		for (int e = 0; e < 2; e++) {
			double end = e == 0 ? -1.0D : 1.0D;
			double th = theta * (1.0D + slab.roll() * end);
			double outAlong = slab.along() + end * slab.length() * 0.5D, inAlong = slab.along() + end * slab.length() * 0.42D;
			double inward = slab.width() * Math.cos(th), rise = slab.width() * Math.sin(th);
			// down through the slab from its top face, as {inward, up}
			double dx = Math.sin(th) * slab.thick(), dy = -Math.cos(th) * slab.thick();
			double[] distance = {outer, outer - inward, outer - inward - dx, outer - dx};
			double[] height = {pivot, pivot + rise, pivot + rise + dy, pivot + dy};
			double[] alongs = {outAlong, inAlong, inAlong, outAlong};
			for (int k = 0; k < 4; k++) {
				c[e * 4 + k] = new double[]{alongs[k], distance[k], height[k]};
				least = Math.min(least, distance[k]);
			}
		}
		double push = Math.max(0.0D, half + SLAB_GAP - least);
		for (double[] corner : c) corner[1] = slab.side() * (corner[1] + push);
		return c;
	}

	// ── iron ─────────────────────────────────────────────────────────

	/** A spike rises to this over {@link #PUNCH} ticks, then settles to 1. */
	public static final float OVERSHOOT = 1.14F;
	public static final float PUNCH = 3.0F;
	public static final float SETTLE = 3.0F;
	/** A spike cracks and sinks back over this many ticks. */
	public static final float SINK = 10.0F;
	/** The shiver a standing spike keeps after its punch, in blocks, dying over SHIVER_DECAY ticks. */
	public static final double SHIVER = 0.035D;
	public static final float SHIVER_DECAY = 7.0F;

	/** One spike of a cluster: its foot offset, height, base radius, lean per block of height, facets and turn. */
	public record Spike(double dx, double dz, double height, double radius, double leanX, double leanZ, int sides,
			double turn) {
	}

	/**
	 * A cluster of {@code count} spikes about {@code scale} blocks tall: one tall central spike and shorter ones round
	 * it splaying outward, each with its own lean, girth and facets, so no two clusters stand alike.
	 */
	public static List<Spike> cluster(int seed, double scale, int count) {
		List<Spike> out = new ArrayList<>(count);
		// about a quarter of a block across at the foot for a one-block spike: iron with mass, not a needle
		double girth = 0.19D * Math.sqrt(Math.max(0.2D, scale));
		for (int i = 0; i < count; i++) {
			double u = unit(seed, 1000 + i), v = unit(seed, 1100 + i), w = unit(seed, 1200 + i);
			if (i == 0) {
				double tilt = Math.toRadians(u * 360.0D);
				out.add(new Spike(0.0D, 0.0D, scale * (0.95D + 0.1D * v), girth * 1.15D, Math.cos(tilt) * 0.08D,
						Math.sin(tilt) * 0.08D, 6, w * Math.PI));
				continue;
			}
			double a = i * 2.39996D + u * 0.6D, distance = scale * (0.16D + 0.22D * v);
			double lean = 0.15D + 0.28D * w;
			out.add(new Spike(Math.cos(a) * distance, Math.sin(a) * distance, scale * (0.38D + 0.4D * unit(seed, 1300 + i)),
					girth * (0.6D + 0.3D * u), Math.cos(a) * lean, Math.sin(a) * lean, v < 0.5D ? 5 : 6, w * Math.PI));
		}
		return out;
	}

	/** Height factor {@code age} ticks after the spike broke out: a fast punch past full height, then settling. */
	public static float rise(float age) {
		if (age <= 0.0F) return 0.0F;
		if (age < PUNCH) return OVERSHOOT * (float) Math.sin(age / PUNCH * Math.PI * 0.5D);
		return HemoEasing.lerp(OVERSHOOT, 1.0F, HemoEasing.ramp(age, PUNCH, PUNCH + SETTLE));
	}

	/** Height factor for a spike that broke out {@code age} ticks ago and starts sinking at {@code sinkAt}. */
	public static float height(float age, float sinkAt) {
		return rise(age) * (1.0F - HemoEasing.ramp(age, sinkAt, sinkAt + SINK));
	}

	/**
	 * The ferric material's formation for such a spike: the wet blood front climbs it as it punches out, and the iron
	 * dissolves back into blood from the tip down as it sinks.
	 */
	public static float formation(float age, float sinkAt) {
		if (age <= 0.0F) return 0.0F;
		float up = HemoEasing.lerp(0.3F, 1.0F, HemoEasing.ramp(age, 0.0F, PUNCH * 0.8F));
		return up * (1.0F - 0.5F * HemoEasing.ramp(age, sinkAt, sinkAt + SINK));
	}

	/** How far a standing spike shivers to the side, in blocks: nothing during the punch, then dying away. */
	public static double shiver(float age, double time, int seed) {
		if (age < PUNCH) return 0.0D;
		return SHIVER * Math.exp(-(age - PUNCH) / SHIVER_DECAY) * Math.sin(time * 2.7D + seed * 1.3D);
	}

	// ── debris ───────────────────────────────────────────────────────

	/** Chips of floor thrown up by a punch fly and settle over this many ticks. */
	public static final float CHIP_LIFE = 14.0F;
	public static final double CHIP_GRAVITY = 0.07D;

	/**
	 * Chip {@code i} of a burst, {@code age} ticks after it: {x, y, z} from the burst point, the chip's tumble angle and
	 * its fade (1 to 0), or null once it has settled and gone. {@code reach} scales how far chips fly.
	 */
	public static double[] chip(int seed, int i, float age, double reach) {
		if (age < 0.0F || age >= CHIP_LIFE) return null;
		double a = unit(seed, 1500 + i) * Math.PI * 2.0D;
		double speed = reach * (0.05D + 0.07D * unit(seed, 1520 + i));
		double up = 0.22D + 0.2D * unit(seed, 1540 + i);
		double flight = 2.0D * up / CHIP_GRAVITY;
		double t = Math.min(age, flight);
		double y = Math.max(0.0D, up * t - CHIP_GRAVITY * t * t * 0.5D);
		double out = speed * (t + Math.max(0.0D, age - flight) * 0.1D);
		double fade = 1.0D - HemoEasing.ramp(age, CHIP_LIFE * 0.6F, CHIP_LIFE);
		return new double[]{Math.cos(a) * out, y, Math.sin(a) * out, age * (0.6D + unit(seed, 1560 + i)), fade};
	}

	// ── claw slashes and shocks ──────────────────────────────────────

	/** A claw's slash starts this many ticks before the commit, sweeps over SLASH_SWEEP, and frays over SLASH_LINGER. */
	public static final float SLASH_LEAD = 2.0F, SLASH_SWEEP = 4.0F, SLASH_LINGER = 7.0F;

	/** How far a claw slash's head has swept across its arc, 0 to 1, {@code age} ticks after the commit; -1 if not drawn. */
	public static float slashHead(float age) {
		float t = age + SLASH_LEAD;
		if (t < 0.0F || t > SLASH_SWEEP + SLASH_LINGER) return -1.0F;
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(t / SLASH_SWEEP));
	}

	/** The slash's strength: full through the sweep, then falling away as it frays. */
	public static float slashFade(float age) {
		float t = age + SLASH_LEAD - SLASH_SWEEP;
		if (t <= 0.0F) return 1.0F;
		return 1.0F - HemoEasing.clamp01(t / SLASH_LINGER);
	}

	/** How far the slash has frayed: 0 through the sweep, 1 as it goes, widening and drifting up as it dissolves. */
	public static float slashFray(float age) {
		return 1.0F - slashFade(age);
	}

	/** A slam's shock of blood runs out over this many ticks. */
	public static final float SHOCK = 7.0F;

	/** The shock ring's radius {@code age} ticks after a slam, reaching {@code reach}; negative when not drawn. */
	public static double shockRadius(float age, double reach) {
		if (age < 0.0F || age > SHOCK) return -1.0D;
		return 0.35D + (reach - 0.35D) * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / (SHOCK * 0.8F)));
	}

	// ── blood ────────────────────────────────────────────────────────

	/**
	 * A swell travelling along a stream: 0 between boluses, 1 at one's heart. {@code s} is the distance along the
	 * stream, {@code speed} blocks a tick and {@code spacing} blocks between boluses.
	 */
	public static double bolus(double s, double time, double speed, double spacing) {
		double phase = (s - time * speed) / Math.max(0.05D, spacing);
		double frac = phase - Math.floor(phase);
		double d = (frac - 0.5D) * 3.2D;
		return Math.exp(-d * d);
	}

	// ── seeds ────────────────────────────────────────────────────────

	/** A stable value in [0, 1) for seed and index. */
	public static double unit(int seed, int index) {
		int h = seed * 0x9E3779B1 ^ index * 0x85EBCA77;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 12;
		h *= 0x297A2D39;
		h ^= h >>> 15;
		return (h >>> 8) / (double) (1 << 24);
	}

	private HemorathFissureRules() {
	}
}
