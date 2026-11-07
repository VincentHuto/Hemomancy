package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Seraphae's effects as pure numbers (visual pass 2, 2026-11-06): the life of a telegraph from the first cut to
 * the scar, the furrows, vessels and nerve fibres each footprint shape grows into, the bone that grows out of the
 * floor and out of struck bodies, the shards it throws, the lash and reach trails' timing, the waves of swelling and
 * light, and her iron chains' links and whip. Her language is healing that hurts: the floor is cut open and heals over
 * too fast, swelling, rooting and spurring; white light runs in the vessels; gold is a nerve firing, drawn only on the
 * Ductilis material. No Minecraft types, so JVM tests cover it; visual numbers never decide hits.
 * <p>
 * Network points are local to a footprint's origin: {@code x} along its aim, {@code y} to its right, in blocks.
 */
public final class SeraphaeOvergrowthRules {
	// ── the life of a telegraph ──────────────────────────────────────

	/** The commit's flash: the light runs white-hot and the swelling surges for this many ticks. */
	public static final float FLASH = 4.0F;
	/** After the active span the light drains and the swelling subsides into a pale scar over this many ticks. */
	public static final float LINGER = 16.0F;
	/** The incision has run its whole length by this much of the reveal. */
	public static final float RUN_LEAD = 0.55F;
	/** How far past full the swelling surges at the height of the flash. */
	public static final float SURGE = 0.3F;
	/** Light at and above this reads as the hot core in the shaders. */
	public static final float WHITE_HOT = 0.9F;
	/**
	 * A furrow strip is this many times the furrow's half-width. The cut and its swollen lips fill the half-width
	 * exactly (the lips' outer foot is the footprint's edge, so the drawn edge is the hit test's); the rest is the
	 * pallor fading round them.
	 */
	public static final double HALO = 1.35D;
	/** The scar a subsided swelling leaves: the swell it settles to before it fades. */
	public static final float SCAR = 0.35F;

	/**
	 * A telegraph's state at one moment: how far the cut has run ({@code grow}), how swollen the tissue stands, how
	 * much white light wells in it, how raw (torn, red) it is, the furrow's width factor and the whole thing's
	 * strength.
	 */
	public record Look(float grow, float swell, float light, float raw, float width, float alpha) {
		public static final Look NONE = new Look(0, 0, 0, 0, 0, 0);

		public boolean visible() {
			return alpha > 0.003F && width > 0.003F;
		}

		/** The same look with its strength scaled. */
		public Look faded(float factor) {
			return new Look(grow, swell, light, raw, width, alpha * factor);
		}
	}

	/**
	 * The floor at {@code moveTime} ticks into a move that reveals at {@code revealTick} and commits at the end of its
	 * {@code windup}. Emerge: the cut opens, faint. Build: the tissue swells up into it while light wells and the
	 * stretched flesh goes raw toward the commit. Flash: the light runs white-hot and the swelling surges. Strike: full
	 * through the active span. Linger: the light drains first, the swelling subsides into a pale scar, then it fades,
	 * over {@code linger} ticks.
	 */
	public static Look look(float moveTime, int revealTick, int windup, int active, float linger) {
		if (moveTime < revealTick) return Look.NONE;
		if (moveTime < windup) {
			float r = windup <= revealTick ? 1.0F : HemoEasing.clamp01((moveTime - revealTick) / (float) (windup - revealTick));
			float grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(r / RUN_LEAD));
			float swell = 0.15F + 0.65F * HemoEasing.easeOutCubic(r);
			float light = 0.08F + 0.72F * r * r;
			float raw = 0.6F * HemoEasing.ramp(r, 0.45F, 1.0F);
			float width = 0.45F + 0.55F * HemoEasing.easeOutCubic(r);
			float alpha = HemoEasing.ramp(r, 0.0F, 0.12F);
			return new Look(grow, swell, light, raw, width, alpha);
		}
		float since = moveTime - windup;
		if (since < FLASH) {
			float f = 1.0F - since / FLASH;
			return new Look(1.0F, 1.0F + SURGE * f, 1.0F, 0.75F + 0.25F * f, 1.0F + 0.3F * f, 1.0F);
		}
		if (since < active) {
			float settle = HemoEasing.clamp01((since - FLASH) / Math.max(1.0F, active - FLASH));
			return new Look(1.0F, 1.0F, 0.9F - 0.2F * settle, 0.75F, 1.0F, 1.0F);
		}
		float l = (since - Math.max(active, FLASH)) / Math.max(1.0F, linger);
		if (l >= 1.0F) return Look.NONE;
		return new Look(1.0F, 1.0F - (1.0F - SCAR) * HemoEasing.ramp(l, 0.0F, 0.8F), 0.7F * (1.0F - HemoEasing.ramp(l, 0.0F, 0.5F)),
				0.75F * (1.0F - HemoEasing.ramp(l, 0.0F, 0.6F)), 1.0F - 0.3F * HemoEasing.ramp(l, 0.3F, 1.0F),
				1.0F - HemoEasing.ramp(l, 0.55F, 1.0F));
	}

	/** The linger a move can use: never past the end of its recovery. */
	public static float linger(int recovery) {
		return Math.max(4.0F, Math.min(LINGER, recovery));
	}

	/** The last tick a move's floor shows. */
	public static float lastTick(int windup, int active, float linger) {
		return windup + Math.max(active, FLASH) + linger;
	}

	/** How much of a seam starting at {@code start} of the run and taking {@code span} of it shows at {@code grow}. */
	public static double shown(double grow, double start, double span) {
		if (span <= 0.0D) return grow >= start ? 1.0D : 0.0D;
		return HemoEasing.clamp01((grow - start) / span);
	}

	// ── networks ─────────────────────────────────────────────────────

	/** What a seam is drawn as: a filled furrow of swelling tissue, a vessel of light, or a nerve fibre (gold). */
	public enum Kind { FURROW, VESSEL, FIBRE }

	/** One seam: its points, its half-width in blocks, when it starts and how long it takes to run, and what it is. */
	public record Seam(double[] x, double[] y, double width, double start, double span, Kind kind) {
		public int size() {
			return x.length;
		}

		public double length() {
			double total = 0.0D;
			for (int i = 1; i < x.length; i++) total += Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
			return total;
		}
	}

	/** Half-widths: a vessel's root and a nerve fibre's. */
	public static final double VESSEL = 0.1D, NERVE_FIBRE = 0.08D;
	/** A lane's vessels branch off its lips about this far apart. */
	public static final double VESSEL_GAP = 1.7D;

	/**
	 * A lane of {@code length} and half-width {@code half} from the origin, as a trench: one furrow filling the lane's
	 * whole width (the floor opened along it and swelling shut, never a set of parallel lines), lens-ended, with
	 * vessels branching off its lips into the floor on alternating sides as the cut runs past them.
	 */
	public static List<Seam> trench(double length, double half, int seed) {
		List<Seam> out = new ArrayList<>();
		if (length <= 0.3D || half <= 0.0D) return out;
		out.add(jag(0.15D, 0.0D, length, 0.0D, 0.9D, Math.min(0.12D, half * 0.15D), half, 0.0D, 0.85D, seed, true, Kind.FURROW));
		int k = 0;
		for (double x = 0.7D + 0.4D * unit(seed, 3); x < length - 0.4D; x += VESSEL_GAP * (0.8D + 0.4D * unit(seed, 40 + k))) {
			int side = (k % 2 == 0 ? 1 : -1) * (unit(seed, 60 + k) < 0.18D ? -1 : 1);
			double heading = side * (40.0D + 25.0D * unit(seed, 80 + k));
			double reach = half * (0.9D + 0.9D * unit(seed, 100 + k)) + 0.4D;
			tree(out, x, side * half * 0.85D, heading, reach, VESSEL, 0.1D + 0.75D * x / length, 0.22D, 1, seed + 200 + k * 13);
			k++;
		}
		return out;
	}

	/**
	 * A cone's nerve fan from {@code inner} to {@code radius} between bearings {@code fromDeg} and {@code toDeg}
	 * (positive to the right): radial nerve fibres about every 13 degrees, each with a side branch, and a furrow along
	 * the outer arc where the lash's reach ends.
	 */
	public static List<Seam> fan(double inner, double radius, double fromDeg, double toDeg, int seed) {
		List<Seam> out = new ArrayList<>();
		double near = Math.max(inner, 0.4D);
		if (radius <= near || toDeg <= fromDeg) return out;
		int fibres = (int) Math.max(3, Math.round((toDeg - fromDeg) / 13.0D));
		for (int i = 0; i <= fibres; i++) {
			double at = fromDeg + (toDeg - fromDeg) * i / fibres + (i == 0 || i == fibres ? 0.0D : (unit(seed, 300 + i) - 0.5D) * 6.0D);
			double reach = radius * (0.82D + 0.18D * unit(seed, 320 + i));
			double r = Math.toRadians(at);
			out.add(jag(Math.cos(r) * near, Math.sin(r) * near, Math.cos(r) * reach, Math.sin(r) * reach, 0.6D, 0.09D,
					NERVE_FIBRE, 0.05D * unit(seed, 340 + i), 0.7D, seed + 360 + i, false, Kind.FIBRE));
			double fork = near + (reach - near) * (0.45D + 0.25D * unit(seed, 380 + i));
			double turn = Math.toRadians(at + (unit(seed, 400 + i) < 0.5D ? -9.0D : 9.0D));
			double twig = fork + Math.min(1.3D, (reach - fork) * 0.85D);
			out.add(jag(Math.cos(r) * fork, Math.sin(r) * fork, Math.cos(turn) * twig, Math.sin(turn) * twig, 0.4D,
					0.05D, NERVE_FIBRE * 0.6D, 0.35D, 0.4D, seed + 420 + i, false, Kind.FIBRE));
		}
		out.add(arc(radius, fromDeg, toDeg, 0.08D, 0.2D, 0.45D, 0.45D, seed + 3, Kind.FURROW));
		return out;
	}

	/**
	 * A disc of {@code radius} round the origin: vessel trees branching outward from near its centre, and the swollen
	 * rim as a broken ring of furrows.
	 */
	public static List<Seam> disc(double radius, int seed) {
		List<Seam> out = new ArrayList<>();
		if (radius <= 0.3D) return out;
		int trees = (int) Math.max(5, Math.min(11, Math.round(radius * 2.4D)));
		double turn = unit(seed, 1) * 360.0D;
		for (int i = 0; i < trees; i++) {
			double heading = turn + i * 360.0D / trees + (unit(seed, 500 + i) - 0.5D) * 18.0D;
			double h = Math.toRadians(heading);
			double from = Math.min(radius * 0.15D, 0.25D + 0.15D * unit(seed, 520 + i));
			// a trunk and two forks reach at most about 2.2 trunks out, so the tree stays inside the rim
			tree(out, Math.cos(h) * from, Math.sin(h) * from, heading, radius * (0.24D + 0.08D * unit(seed, 540 + i)), VESSEL * 1.2D,
					0.0D, 0.45D, 2, seed + 560 + i * 17);
		}
		int arcs = 3 + (int) (unit(seed, 2) * 2.0D);
		for (int i = 0; i < arcs; i++) {
			double from = turn + i * 360.0D / arcs + 8.0D * unit(seed, 600 + i);
			double to = from + 360.0D / arcs * (0.62D + 0.25D * unit(seed, 620 + i));
			out.add(arc(radius * (0.94D + 0.04D * unit(seed, 640 + i)), from, to, 0.06D, 0.17D, 0.35D, 0.5D, seed + 660 + i, Kind.FURROW));
		}
		return out;
	}

	/** A small root burst round a point: five to seven forked vessels and a broken furrow ring, for marks and pads. */
	public static List<Seam> star(double radius, int seed) {
		List<Seam> out = new ArrayList<>();
		if (radius <= 0.1D) return out;
		int arms = 5 + (int) (unit(seed, 1) * 3.0D);
		double turn = unit(seed, 2) * 360.0D;
		for (int i = 0; i < arms; i++) {
			double heading = turn + i * 360.0D / arms + (unit(seed, 700 + i) - 0.5D) * 25.0D;
			tree(out, 0.0D, 0.0D, heading, radius * (0.4D + 0.15D * unit(seed, 720 + i)), VESSEL, 0.0D, 0.6D, 1, seed + 740 + i * 11);
		}
		out.add(arc(radius * 0.85D, turn, turn + 360.0D * (0.55D + 0.3D * unit(seed, 3)), 0.06D, 0.13D, 0.4D, 0.4D, seed + 9,
				Kind.FURROW));
		return out;
	}

	/**
	 * Wing Grasp's closing fingers on a disc of {@code radius}: {@code count} furrows curling in from the rim toward the
	 * centre, the way the wing's tip fan closes over the target.
	 */
	public static List<Seam> fingers(double radius, int count, int seed) {
		List<Seam> out = new ArrayList<>();
		if (radius <= 0.3D || count <= 0) return out;
		double turn = unit(seed, 4) * 360.0D;
		for (int i = 0; i < count; i++) {
			double base = turn + i * 360.0D / count;
			int n = 9;
			double[] x = new double[n], y = new double[n];
			for (int k = 0; k < n; k++) {
				double t = k / (double) (n - 1);
				double r = radius * (1.0D - 0.85D * t);
				double a = Math.toRadians(base + 55.0D * t * t);
				x[k] = Math.cos(a) * r;
				y[k] = Math.sin(a) * r;
			}
			out.add(new Seam(x, y, 0.2D, 0.05D * i / count, 0.75D, Kind.FURROW));
		}
		return out;
	}

	/**
	 * One Convulsion spoke's fibres, about its centre line (bearing 0) from {@code inner} to {@code outer} and
	 * {@code arcDegrees} wide: four nerve fibres across it, a furrow on its outer rim and one on its inner rim, so the
	 * band reads as one nerve bundle and the gaps between bundles stay clean floor.
	 */
	public static List<Seam> spoke(double inner, double outer, double arcDegrees, int seed) {
		List<Seam> out = new ArrayList<>();
		if (outer <= inner || arcDegrees <= 0.0D) return out;
		double half = arcDegrees / 2.0D;
		for (int i = 0; i < 4; i++) {
			double at = -half * 0.78D + i * half * 0.52D + (unit(seed, 800 + i) - 0.5D) * 4.0D;
			double r = Math.toRadians(at);
			out.add(jag(Math.cos(r) * inner, Math.sin(r) * inner, Math.cos(r) * outer, Math.sin(r) * outer, 0.55D, 0.07D,
					NERVE_FIBRE * (i == 1 || i == 2 ? 1.25D : 0.9D), 0.0D, 0.8D, seed + 820 + i, false, Kind.FIBRE));
		}
		out.add(arc(outer, -half, half, 0.04D, 0.16D, 0.3D, 0.5D, seed + 840, Kind.FURROW));
		out.add(arc(inner, -half * 0.9D, half * 0.9D, 0.03D, 0.11D, 0.2D, 0.5D, seed + 841, Kind.FURROW));
		return out;
	}

	/** A ring furrow at {@code radius}, {@code width} its half-width, whole or nearly so. */
	public static List<Seam> ring(double radius, double width, int seed) {
		List<Seam> out = new ArrayList<>();
		if (radius <= 0.2D) return out;
		out.add(arc(radius, -180.0D, 180.0D, 0.05D, width, 0.0D, 0.7D, seed, Kind.FURROW));
		return out;
	}

	/**
	 * A vessel tree into {@code out}: a trunk from (x, y) along {@code heading} degrees for {@code length} blocks, and at
	 * its end two branches turned 25 to 40 degrees either way, shorter and finer, down to {@code depth} forks. Each
	 * part starts where its parent has run to.
	 */
	public static void tree(List<Seam> out, double x, double y, double heading, double length, double width, double start,
			double span, int depth, int seed) {
		if (length < 0.15D || width < 0.012D) return;
		double h = Math.toRadians(heading);
		double ex = x + Math.cos(h) * length, ey = y + Math.sin(h) * length;
		out.add(jag(x, y, ex, ey, 0.35D, Math.min(0.12D, 0.05D + 0.04D * length), width, start, span, seed, false, Kind.VESSEL));
		if (depth <= 0) return;
		double next = start + span * 0.85D;
		for (int side = -1; side <= 1; side += 2) {
			double turn = heading + side * (25.0D + 15.0D * unit(seed, 900 + side));
			tree(out, ex, ey, turn, length * (0.5D + 0.2D * unit(seed, 910 + side)), width * 0.62D, next, span * 0.8D, depth - 1,
					seed * 31 + side + 7);
		}
	}

	// ── seam pieces ──────────────────────────────────────────────────

	/** A wandering seam from a to b, a point every {@code step} blocks, wandering up to {@code amp} either side. */
	static Seam jag(double ax, double ay, double bx, double by, double step, double amp, double width, double start,
			double span, int seed, boolean pinnedEnd, Kind kind) {
		double length = Math.hypot(bx - ax, by - ay);
		int segments = (int) Math.max(2, Math.ceil(length / Math.max(0.1D, step)));
		double nx = length < 1e-6 ? 0.0D : -(by - ay) / length, ny = length < 1e-6 ? 0.0D : (bx - ax) / length;
		double[] x = new double[segments + 1], y = new double[segments + 1];
		double drift = 0.0D;
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments;
			drift = drift * 0.55D + (unit(seed, i) - 0.5D) * 1.6D;
			double envelope = pinnedEnd ? Math.sin(Math.PI * t) : Math.sin(Math.PI * 0.5D * t);
			double off = i == 0 ? 0.0D : HemoEasing.clamp01(Math.abs(drift) / 1.4D) * Math.signum(drift) * amp * envelope;
			x[i] = ax + (bx - ax) * t + nx * off;
			y[i] = ay + (by - ay) * t + ny * off;
		}
		return new Seam(x, y, width, start, span, kind);
	}

	static Seam arc(double radius, double fromDeg, double toDeg, double jitter, double width, double start, double span, int seed,
			Kind kind) {
		int segments = (int) Math.max(4, Math.ceil(Math.toRadians(toDeg - fromDeg) * radius / 0.6D));
		double[] x = new double[segments + 1], y = new double[segments + 1];
		double drift = 0.0D;
		for (int i = 0; i <= segments; i++) {
			double a = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
			drift = drift * 0.5D + (unit(seed, i) - 0.5D);
			double r = radius + drift * jitter * 2.0D;
			x[i] = Math.cos(a) * r;
			y[i] = Math.sin(a) * r;
		}
		return new Seam(x, y, width, start, span, kind);
	}

	// ── bone ─────────────────────────────────────────────────────────

	/** A spur grows to this over {@link #GROW} ticks, then settles to 1 over {@link #SETTLE}. */
	public static final float OVERGROW = 1.1F;
	public static final float GROW = 4.0F;
	public static final float SETTLE = 4.0F;
	/** A spur withers back into the floor over this many ticks, dissolving into light from the tip down. */
	public static final float WITHER = 12.0F;
	/** A standing growth throbs with this much girth on her heartbeat. */
	public static final double THROB = 0.07D;

	/**
	 * One bone spur of a cluster: its foot offset, height, base radius, lean per block of height, facets, turn, and
	 * {@code curl}: how far its upper half bends further along its lean, so the bone grows like something alive.
	 */
	public record Spur(double dx, double dz, double height, double radius, double leanX, double leanZ, int sides, double turn,
			double curl) {
		/** The same spur leaning {@code lean} more per block of height along {x, z} and curling by {@code curl} more. */
		public Spur toward(double x, double z, double lean, double more) {
			return new Spur(dx, dz, height, radius, leanX + x * lean, leanZ + z * lean, sides, turn, curl + more);
		}
	}

	/**
	 * A cluster of {@code count} spurs about {@code scale} blocks tall: a tall central spur and shorter ones round it
	 * splaying and curling outward, each with its own girth and facets, so no two clusters stand alike.
	 */
	public static List<Spur> cluster(int seed, double scale, int count) {
		List<Spur> out = new ArrayList<>(count);
		double girth = 0.21D * Math.sqrt(Math.max(0.2D, scale));
		for (int i = 0; i < count; i++) {
			double u = unit(seed, 1000 + i), v = unit(seed, 1100 + i), w = unit(seed, 1200 + i);
			if (i == 0) {
				double tilt = Math.toRadians(u * 360.0D);
				out.add(new Spur(0.0D, 0.0D, scale * (0.95D + 0.1D * v), girth * 1.15D, Math.cos(tilt) * 0.07D,
						Math.sin(tilt) * 0.07D, 6, w * Math.PI, 0.12D));
				continue;
			}
			double a = i * 2.39996D + u * 0.6D, distance = scale * (0.14D + 0.2D * v);
			double lean = 0.18D + 0.3D * w;
			out.add(new Spur(Math.cos(a) * distance, Math.sin(a) * distance, scale * (0.4D + 0.38D * unit(seed, 1300 + i)),
					girth * (0.55D + 0.3D * u), Math.cos(a) * lean, Math.sin(a) * lean, v < 0.5D ? 5 : 6, w * Math.PI,
					0.25D + 0.35D * unit(seed, 1400 + i)));
		}
		return out;
	}

	/** Height factor {@code age} ticks after the bone broke out: a fast swelling growth past full, then settling. */
	public static float rise(float age) {
		if (age <= 0.0F) return 0.0F;
		if (age < GROW) return OVERGROW * HemoEasing.easeOutCubic(age / GROW);
		return HemoEasing.lerp(OVERGROW, 1.0F, HemoEasing.ramp(age, GROW, GROW + SETTLE));
	}

	/** Height factor for bone that broke out {@code age} ticks ago and withers from {@code witherAt}. */
	public static float height(float age, float witherAt) {
		return rise(age) * (1.0F - 0.6F * HemoEasing.ramp(age, witherAt, witherAt + WITHER));
	}

	/**
	 * The bone material's formation: living tissue climbs the spur as it grows (the tip is still soft and pale), and
	 * the bone dissolves back into light from the tip down as it withers; 0 once gone.
	 */
	public static float formation(float age, float witherAt) {
		if (age <= 0.0F) return 0.0F;
		float up = HemoEasing.lerp(0.35F, 1.0F, HemoEasing.ramp(age, 0.0F, GROW));
		return up * (1.0F - HemoEasing.ramp(age, witherAt, witherAt + WITHER));
	}

	/** The girth factor of standing growth: a heartbeat throb that dies over its first 20 ticks, then a slow breath. */
	public static double throb(float age, double time, int seed) {
		if (age < GROW) return 1.0D;
		double beat = Math.pow(Math.max(0.0D, Math.sin(time * 0.31D + seed * 0.7D)), 6.0D);
		double fresh = Math.exp(-(age - GROW) / 20.0D);
		return 1.0D + THROB * beat * (0.35D + 0.65D * fresh);
	}

	/** The light still running in a growth's grooves {@code age} ticks after it broke out: bright at birth, then dim. */
	public static float glow(float age) {
		if (age <= 0.0F) return 0.0F;
		return 0.25F + 0.75F * (float) Math.exp(-age / 8.0D);
	}

	// ── shards ───────────────────────────────────────────────────────

	/** Shards of bone and light thrown by a growth or a burst fly and settle over this many ticks. */
	public static final float SHARD_LIFE = 16.0F;
	public static final double SHARD_GRAVITY = 0.05D;

	/**
	 * Shard {@code i} of a burst, {@code age} ticks after it: {x, y, z} from the burst point, its tumble angle and its
	 * fade (1 to 0), or null once it has settled and gone. {@code reach} scales how far shards fly.
	 */
	public static double[] shard(int seed, int i, float age, double reach) {
		if (age < 0.0F || age >= SHARD_LIFE) return null;
		double a = unit(seed, 1500 + i) * Math.PI * 2.0D;
		double speed = reach * (0.05D + 0.07D * unit(seed, 1520 + i));
		double up = 0.18D + 0.18D * unit(seed, 1540 + i);
		double flight = 2.0D * up / SHARD_GRAVITY;
		double t = Math.min(age, flight);
		double y = Math.max(0.0D, up * t - SHARD_GRAVITY * t * t * 0.5D);
		double out = speed * (t + Math.max(0.0D, age - flight) * 0.1D);
		double fade = 1.0D - HemoEasing.ramp(age, SHARD_LIFE * 0.55F, SHARD_LIFE);
		return new double[]{Math.cos(a) * out, y, Math.sin(a) * out, age * (0.5D + unit(seed, 1560 + i)), fade};
	}

	// ── strikes ──────────────────────────────────────────────────────

	/** A lash starts this many ticks before the commit, sweeps over LASH_SWEEP (the kit's 6) and frays over LASH_LINGER. */
	public static final float LASH_LEAD = 2.0F, LASH_SWEEP = 6.0F, LASH_LINGER = 8.0F;

	/** How far a lash's head has swept, 0 to 1, {@code age} ticks after the commit; -1 when it is not drawn. */
	public static float lashHead(float age) {
		float t = age + LASH_LEAD;
		if (t < 0.0F || t > LASH_SWEEP + LASH_LINGER) return -1.0F;
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(t / LASH_SWEEP));
	}

	/** The lash's strength: full through the sweep, then falling away as it frays. */
	public static float lashFade(float age) {
		float t = age + LASH_LEAD - LASH_SWEEP;
		if (t <= 0.0F) return 1.0F;
		return 1.0F - HemoEasing.clamp01(t / LASH_LINGER);
	}

	/** How far the lash has frayed: 0 through the sweep, 1 as it goes. */
	public static float lashFray(float age) {
		return 1.0F - lashFade(age);
	}

	/** A reach's hand trails show from the commit through the glide and fray over this many ticks after it. */
	public static final float REACH_LINGER = 8.0F;

	/** The reach's trail strength {@code age} ticks after the commit with an {@code active} glide; 0 when not drawn. */
	public static float reachFade(float age, int active) {
		if (age < -1.0F) return 0.0F;
		if (age < active) return HemoEasing.clamp01((age + 1.0F) / 2.0F);
		return 1.0F - HemoEasing.clamp01((age - active) / REACH_LINGER);
	}

	/** A wave of swelling and light runs out over this many ticks. */
	public static final float WAVE = 8.0F;

	/** The wave's radius {@code age} ticks after it broke, reaching {@code reach}; negative when not drawn. */
	public static double wave(float age, double reach) {
		if (age < 0.0F || age > WAVE) return -1.0D;
		return 0.3D + (reach - 0.3D) * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / (WAVE * 0.8F)));
	}

	/** The width factor of a beam's body {@code age} ticks after it fired: flared wide at the flash, then steady. */
	public static float beamFlare(float age) {
		if (age < 0.0F) return 0.0F;
		return 1.0F + 0.8F * (float) Math.exp(-age / 1.5D);
	}

	/** A struck body's impact shows for this many ticks: the hurt flash's own length. */
	public static final float IMPACT = 10.0F;

	/** How far bone has driven out of a struck body: biting out over 1.5 ticks, held, withdrawn from 3 to 8. */
	public static float bite(float age) {
		if (age <= 0.0F || age >= IMPACT) return 0.0F;
		return HemoEasing.clamp01(age / 1.5F) * (1.0F - HemoEasing.ramp(age, 3.0F, 8.0F));
	}

	// ── light with body ──────────────────────────────────────────────

	/**
	 * A swell travelling along a stream of light: 0 between knots, 1 at one's heart. {@code s} is the distance along it,
	 * {@code speed} blocks a tick and {@code spacing} blocks between knots.
	 */
	public static double knot(double s, double time, double speed, double spacing) {
		double phase = (s - time * speed) / Math.max(0.05D, spacing);
		double frac = phase - Math.floor(phase);
		double d = (frac - 0.5D) * 3.0D;
		return Math.exp(-d * d);
	}

	// ── chains ───────────────────────────────────────────────────────

	/** A chain link's length along the chain and the pitch between links, in blocks (6 and a bit under 5 texels). */
	public static final double LINK_LENGTH = 0.375D, LINK_PITCH = 0.3D;
	/** How far a slack chain sags, as the radiance braid sags. */
	public static final double SAG = 0.45D;

	/** How many links span a chain of {@code length} blocks. */
	public static int links(double length) {
		return (int) Math.max(1, Math.floor(Math.max(0.0D, length - LINK_LENGTH) / LINK_PITCH) + 1);
	}

	/** The sag {@code t} of the way along a chain {@code taut} (0 slack to 1 drawn tight). */
	public static double sag(double t, float taut) {
		return Math.sin(Math.PI * HemoEasing.clamp01(t)) * SAG * (1.0D - HemoEasing.clamp01(taut));
	}

	/**
	 * The Chain Wrench's whip: the lateral throw {@code t} of the way along the chain {@code age} ticks after the
	 * commit, a wave running from her hands to the anchor, pinned at both ends, dying away over 12 ticks.
	 */
	public static double whip(double t, float age, double amplitude) {
		if (age < 0.0F) return 0.0D;
		double travel = age / 5.0D;
		double envelope = Math.sin(Math.PI * HemoEasing.clamp01(t)) * Math.exp(-Math.max(0.0D, t - travel) * 6.0D);
		return amplitude * envelope * Math.sin((t - travel) * Math.PI * 3.0D) * Math.exp(-age / 12.0D);
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

	private SeraphaeOvergrowthRules() {
	}
}
