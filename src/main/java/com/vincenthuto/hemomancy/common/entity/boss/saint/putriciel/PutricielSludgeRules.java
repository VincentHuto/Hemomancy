package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The rot sludge (design revision 2026-11-06, R2): the rot pools where some moves land and stays until the fire comes
 * for it. Which moves lay a pool and where, the placement rules (the boss floor, the pyre circles, this fight's ash,
 * the overlap with older pools), the cap with its oldest-first settle, and the catch schedule the Ignition computes:
 * when the fire reaches each pool and colony patch, and from where (the Ignition ring, fire runners from the relit
 * pyres and from burning pools, the fallback), plus the live sources that can only bring a catch forward. Pure, so
 * it is JVM-tested; the entity spawns, expires and edits through the kit and the arena.
 * <p>
 * Frame conventions the client reads: a pool's frame origin is its centre (a disc is a {@code Disc} at the origin; a
 * runnel is a whole-length {@code Path} through its two ends, with the origin at their midpoint and the aim at its far
 * end); a {@code SLUDGE_FIRE} carries the pool's shape with its aim at the catch point and its target at the source;
 * a {@code FIRE_RUNNER} is a {@code Lane} from the source (origin) to the catch point (aim). Fades tell the endings
 * apart: a pool that catches fades over {@link #BURN}, one that settles over {@link #SETTLE}; a colony patch that
 * burns fades over {@link #COLONY_BURN}, one a pool absorbs over {@link #ABSORB}; an ending's sludge fire lives
 * {@link #ENDING_BURN} ticks.
 */
public final class PutricielSludgeRules {
	// ── the burn's beats, ticks from the catch (contract 2) ──────────
	public static final int CATCH = 6, BOIL_END = 30, ASH_AT = 44, BURN = 50, COOL = 60;
	/** A pool settling into the moss (cap or safety life), and a new pool's spread from the splash. */
	public static final int SETTLE = 30, FORM = 8;
	/** A fire runner crawls this many blocks a tick. */
	public static final double RUNNER_SPEED = 0.5D;
	/** A colony patch's burn, and every sludge burn at the ending. */
	public static final int COLONY_BURN = 30, ENDING_BURN = 10;
	/** A colony patch a new pool lays over sinks into it over this many ticks. */
	public static final int ABSORB = 12;

	// ── the pool and its fire ────────────────────────────────────────
	/** A safety only: the interval always comes first. */
	public static final int POOL_LIFE = 2400, POOL_PULSE = 5;
	public static final float POOL_COLONISATION = 0.5F;
	/** Movement speed x0.65 while wading, released this many ticks after the last pool. */
	public static final double SLOW = -0.35D;
	public static final int SLOW_RELEASE = 6;
	public static final int WADE_SOUND_EVERY = 8;
	/** A burning pool: 50 ticks of burning, then 60 of cooling that touch nothing. */
	public static final int FIRE_LIFE = BURN + COOL, FIRE_PULSE = 10;
	public static final float FIRE_DAMAGE = 1.0F, FIRE_COLONISATION = -2.0F;
	/** A runner's scorched strip is 0.6 wide; it lives its travel plus this. */
	public static final double RUNNER_HALF_WIDTH = 0.3D;
	public static final int RUNNER_TAIL = 30;
	/** The ash a burned pool leaves: its disc less this, at most this many cells. */
	public static final double ASH_INSET = 0.25D;

	// ── what lays ────────────────────────────────────────────────────
	public static final double VOLLEY_RADIUS = 1.5D, DROP_RADIUS = 2.0D, SNARE_RADIUS = 1.75D, BLOOM_RADIUS = 1.75D;
	/** Offering Grasp's runnel: the lane's outer four blocks, from 2 to 6 out. */
	public static final double RUNNEL_FROM = 2.0D, RUNNEL_TO = 6.0D, RUNNEL_HALF_WIDTH = 0.55D;
	/** Shelf Volley's landings 0, 3 and 6 pool. */
	public static final int VOLLEY_EVERY = 3;
	/** The Bloom drains into two pools on cells at least 3 from safe ground and 2 from any challenger. */
	public static final int BLOOM_DRAINS = 2;
	public static final double BLOOM_SAFE_GAP = 3.0D, BLOOM_CHALLENGER_GAP = 2.0D;
	/** The five moves that lay. */
	public static final Set<PutricielMove> LAYERS = Collections.unmodifiableSet(EnumSet.of(PutricielMove.SHELF_VOLLEY,
			PutricielMove.CENSER_DROP, PutricielMove.OFFERING_GRASP, PutricielMove.ROOT_SNARE, PutricielMove.BLOOM_OF_ROT));

	// ── placement ────────────────────────────────────────────────────
	/** A disc clipped under this radius (or a runnel under this length) is not laid. */
	public static final double MIN_RADIUS = 1.0D, MIN_LENGTH = 1.0D;
	/** A pool's rim stays this far short of an ash cell's centre. */
	public static final double ASH_GAP = 0.5D;
	/** A new pool may overlap an older one by this much of its own radius. */
	public static final double OVERLAP = 0.4D;

	// ── the cap ──────────────────────────────────────────────────────
	public static final int POOLS = 6, POOLS_PER_EXTRA = 1, POOLS_MAX = 9;
	public static final double AREA = 42.0D, AREA_PER_EXTRA = 6.0D, AREA_MAX = 60.0D;

	// ── the catch ────────────────────────────────────────────────────
	/** The Ignition ring reaches pools whose nearest edge is within 6 of the body, 10 ticks for the 6 blocks. */
	public static final double RING_REACH = 6.0D;
	public static final int RING_TRAVEL = 10;
	/** A relit pyre's runner leaves 4 ticks after it relights, for the nearest pool within 8 blocks. */
	public static final int PYRE_RUNNER_DELAY = 4;
	public static final double PYRE_RUNNER_REACH = 8.0D;
	/** A burning pool's runner leaves 6 ticks after it caught, for any pool within 3 of its rim. */
	public static final int POOL_RUNNER_DELAY = 6;
	public static final double POOL_RUNNER_REACH = 3.0D;
	/** Anything still unlit this long after the commit catches at its edge nearest the body. */
	public static final int FALLBACK = 140;
	/** The burning body lights a pool within its radius plus this. */
	public static final double BODY_REACH = 0.6D;

	private PutricielSludgeRules() {
	}

	// ── which moves lay ──────────────────────────────────────────────

	/** Only the five rot moves lay, and only in the rot: no fire move and no Embering leaves sludge. */
	public static boolean lays(PutricielMove move, PutricielStage stage) {
		return stage == PutricielStage.ROT && LAYERS.contains(move);
	}

	/** Shelf Volley's landing {@code index} pools (0, 3, 6) instead of leaving a colony patch. */
	public static boolean volleyPool(int index) {
		return index >= 0 && index % VOLLEY_EVERY == 0;
	}

	/** Every other Offering Grasp overflows: the move's synced serial is even. */
	public static boolean graspPool(int serial) {
		return serial % 2 == 0;
	}

	/** A Root Snare pools only when it strikes no one. */
	public static boolean snarePool(boolean struck) {
		return !struck;
	}

	/** A pool absorbs a colony patch whose centre lies closer than the patch's radius plus half the pool's. */
	public static boolean absorbs(Patch pool, Vec3 colony, double colonyRadius) {
		return !pool.colony() && pool.axisDistance(colony) < colonyRadius + pool.radius() / 2.0D;
	}

	// ── a patch of rot on the floor ──────────────────────────────────

	/**
	 * A pool, runnel or colony patch on the floor: a disc when {@code from} and {@code to} coincide, else a runnel of
	 * half-width {@code radius} along the segment.
	 */
	public record Patch(Vec3 from, Vec3 to, double radius, boolean colony) {
		public static Patch disc(Vec3 centre, double radius) {
			return new Patch(centre, centre, radius, false);
		}

		public static Patch runnel(Vec3 from, Vec3 to, double halfWidth) {
			return new Patch(from, to, halfWidth, false);
		}

		public static Patch colonyAt(Vec3 centre, double radius) {
			return new Patch(centre, centre, radius, true);
		}

		public boolean runnel() {
			return length() > 1.0E-6D;
		}

		public double length() {
			return BossFootprint.Shapes.planar(from, to);
		}

		public Vec3 centre() {
			return from.lerp(to, 0.5D);
		}

		/** A disc's pi r squared; a runnel's length by its width (its ends are pinched). */
		public double area() {
			return runnel() ? length() * 2.0D * radius : Math.PI * radius * radius;
		}

		/** Planar distance from the patch's axis (its centre, or the runnel's segment). */
		public double axisDistance(Vec3 point) {
			return BossFootprint.Shapes.segmentDistance(from, to, point);
		}

		public boolean contains(Vec3 point) {
			return axisDistance(point) <= radius;
		}

		/** How far {@code point} lies outside the rim; 0 inside. */
		public double edgeDistance(Vec3 point) {
			return Math.max(0.0D, axisDistance(point) - radius);
		}

		/** The point of the rim nearest {@code point} (the point itself when inside), on the patch's floor. */
		public Vec3 nearestEdge(Vec3 point) {
			Vec3 axis = closestOnAxis(point);
			double dx = point.x - axis.x, dz = point.z - axis.z;
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance <= radius) return new Vec3(point.x, from.y, point.z);
			return new Vec3(axis.x + dx / distance * radius, from.y, axis.z + dz / distance * radius);
		}

		/** Rim to rim: 0 when the two touch or overlap. */
		public double gap(Patch other) {
			return Math.max(0.0D, axisGap(other) - radius - other.radius);
		}

		/** How far the two axes come to each other. */
		public double axisGap(Patch other) {
			return segmentGap(from, to, other.from, other.to);
		}

		/** The centre and points round the rim (and along a runnel), which a fire footprint is tested at. */
		public List<Vec3> probes() {
			List<Vec3> points = new ArrayList<>();
			double inset = radius * 0.9D;
			if (!runnel()) {
				points.add(from);
				for (int i = 0; i < 8; i++) {
					double angle = i * Math.PI / 4.0D;
					points.add(new Vec3(from.x + Math.cos(angle) * inset, from.y, from.z + Math.sin(angle) * inset));
				}
				return points;
			}
			double length = length();
			double sx = -(to.z - from.z) / length, sz = (to.x - from.x) / length;
			for (int i = 0; i <= 4; i++) {
				Vec3 axis = from.lerp(to, i / 4.0D);
				points.add(axis);
				points.add(new Vec3(axis.x + sx * inset, axis.y, axis.z + sz * inset));
				points.add(new Vec3(axis.x - sx * inset, axis.y, axis.z - sz * inset));
			}
			return points;
		}

		private Vec3 closestOnAxis(Vec3 point) {
			double dx = to.x - from.x, dz = to.z - from.z;
			double lengthSq = dx * dx + dz * dz;
			if (lengthSq < 1.0E-12D) return from;
			double t = Mth.clamp(((point.x - from.x) * dx + (point.z - from.z) * dz) / lengthSq, 0.0D, 1.0D);
			return new Vec3(from.x + dx * t, from.y, from.z + dz * t);
		}
	}

	/** Planar distance between two segments (either may be a point). */
	static double segmentGap(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
		if (crosses(a, b, c, d)) return 0.0D;
		return Math.min(Math.min(BossFootprint.Shapes.segmentDistance(a, b, c), BossFootprint.Shapes.segmentDistance(a, b, d)),
				Math.min(BossFootprint.Shapes.segmentDistance(c, d, a), BossFootprint.Shapes.segmentDistance(c, d, b)));
	}

	private static boolean crosses(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
		double d1 = side(c, d, a), d2 = side(c, d, b), d3 = side(a, b, c), d4 = side(a, b, d);
		return (d1 > 0.0D && d2 < 0.0D || d1 < 0.0D && d2 > 0.0D) && (d3 > 0.0D && d4 < 0.0D || d3 < 0.0D && d4 > 0.0D);
	}

	private static double side(Vec3 a, Vec3 b, Vec3 p) {
		return (b.x - a.x) * (p.z - a.z) - (b.z - a.z) * (p.x - a.x);
	}

	// ── the patch as the kit's hazard shape ──────────────────────────

	/** A pool's hazard shape: a disc at the origin, or a whole-length path through the runnel's two ends. */
	public static BossFootprint shape(Patch patch) {
		if (patch.runnel()) return new BossFootprint.Path(patch.radius(), 0.0D, 0.0D, 0, 0.0D, BossFootprint.Height.STANDING);
		return new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, patch.radius(), BossFootprint.Height.STANDING);
	}

	/** A pool's hazard frame: the origin at its centre, a runnel's ends as its points and its far end as the aim. */
	public static FootprintFrame frame(Patch patch, double floorY) {
		Vec3 centre = patch.centre();
		FootprintFrame frame = FootprintFrame.simple(centre, patch.runnel() ? patch.to() : centre, centre, floorY);
		return patch.runnel() ? frame.withPoints(List.of(patch.from(), patch.to())) : frame;
	}

	/** The patch a hazard's shape and frame describe, or null for a shape a pool never takes. */
	@Nullable
	public static Patch patchOf(BossFootprint shape, FootprintFrame frame, boolean colony) {
		if (shape instanceof BossFootprint.Disc disc) return new Patch(frame.anchor(disc.at()), frame.anchor(disc.at()), disc.radius(), colony);
		if (shape instanceof BossFootprint.Path path && frame.points().size() >= 2) {
			return new Patch(frame.points().get(0), frame.points().get(frame.points().size() - 1), path.halfWidth(), colony);
		}
		return null;
	}

	/** A fire runner's shape: a 0.6-wide lane from its source to the catch point, its front crawling at 0.5. */
	public static BossFootprint runnerShape(double length) {
		return new BossFootprint.Lane(length, RUNNER_HALF_WIDTH, 0, 0.0D, 0.0D, RUNNER_SPEED, 0, BossFootprint.Height.STANDING);
	}

	/** Ticks a runner takes to crawl {@code distance}. */
	public static int travel(double distance) {
		return (int) Math.ceil(Math.max(0.0D, distance) / RUNNER_SPEED);
	}

	/** A runner lives its travel and then 30 ticks more. */
	public static int runnerLife(double distance) {
		return travel(distance) + RUNNER_TAIL;
	}

	// ── placement ────────────────────────────────────────────────────

	public enum Outcome { LAY, SPLASH, SKIP }

	/** What a landing does: lays {@code patch} (clipped), splashes onto the older pool {@code onto}, or nothing. */
	public record Placement(Outcome outcome, @Nullable Patch patch, int onto) {
		static final Placement SKIP = new Placement(Outcome.SKIP, null, -1);
	}

	/**
	 * Where a wanted pool may lie: inside the boss floor (clipped to it), clear of every pyre's circle by
	 * {@link PutricielArenaRules#PYRE_SAFE}, never on this fight's ash (a disc whose centre cell is ash is not laid;
	 * otherwise its rim stays {@link #ASH_GAP} short of the nearest ash cell's centre; a runnel stops short of its
	 * first), and under {@link #MIN_RADIUS} not at all. A pool overlapping an older one by more than {@link #OVERLAP}
	 * of its radius splashes onto that pool instead. {@code pools} are the live pools, oldest first.
	 */
	public static Placement place(Patch wanted, AABB floor, List<Vec3> pyres, LongSet ash, List<Patch> pools) {
		Patch clipped = wanted.runnel() ? clipRunnel(wanted, floor, pyres, ash) : clipDisc(wanted, floor, pyres, ash);
		if (clipped == null) return Placement.SKIP;
		int onto = -1;
		double deepest = OVERLAP * clipped.radius();
		for (int i = 0; i < pools.size(); i++) {
			Patch older = pools.get(i);
			double depth = clipped.radius() + older.radius() - clipped.axisGap(older);
			if (depth > deepest) {
				deepest = depth;
				onto = i;
			}
		}
		if (onto >= 0) return new Placement(Outcome.SPLASH, null, onto);
		return new Placement(Outcome.LAY, clipped, -1);
	}

	@Nullable
	private static Patch clipDisc(Patch wanted, AABB floor, List<Vec3> pyres, LongSet ash) {
		Vec3 c = wanted.from();
		if (!BossFootprint.Shapes.planarInside(floor, c)) return null;
		if (ash.contains(BossFootprint.cellAt(c))) return null;
		double r = wanted.radius();
		r = Math.min(r, Math.min(Math.min(c.x - floor.minX, floor.maxX - c.x), Math.min(c.z - floor.minZ, floor.maxZ - c.z)));
		for (Vec3 pyre : pyres) r = Math.min(r, BossFootprint.Shapes.planar(c, pyre) - PutricielArenaRules.PYRE_SAFE);
		r = Math.min(r, nearestAsh(c, ash) - ASH_GAP);
		return r < MIN_RADIUS ? null : Patch.disc(c, r);
	}

	@Nullable
	private static Patch clipRunnel(Patch wanted, AABB floor, List<Vec3> pyres, LongSet ash) {
		double length = wanted.length();
		double hw = wanted.radius();
		double reach = -1.0D;
		int steps = Math.max(1, (int) Math.ceil(length / 0.25D));
		for (int i = 0; i <= steps; i++) {
			double along = length * i / steps;
			Vec3 p = wanted.from().lerp(wanted.to(), i / (double) steps);
			if (!runnelClear(p, hw, floor, pyres, ash)) break;
			reach = along;
		}
		if (reach < MIN_LENGTH) return null;
		Vec3 end = wanted.from().lerp(wanted.to(), reach / length);
		return Patch.runnel(wanted.from(), end, hw);
	}

	private static boolean runnelClear(Vec3 p, double hw, AABB floor, List<Vec3> pyres, LongSet ash) {
		if (p.x - hw < floor.minX || p.x + hw > floor.maxX || p.z - hw < floor.minZ || p.z + hw > floor.maxZ) return false;
		if (ash.contains(BossFootprint.cellAt(p))) return false;
		for (Vec3 pyre : pyres) if (BossFootprint.Shapes.planar(p, pyre) < PutricielArenaRules.PYRE_SAFE + hw) return false;
		return nearestAsh(p, ash) >= hw + ASH_GAP;
	}

	/** Planar distance from {@code point} to the nearest ash cell's centre, or a large number with none. */
	static double nearestAsh(Vec3 point, LongSet ash) {
		double best = Double.MAX_VALUE;
		for (long cell : ash) {
			double x = net.minecraft.core.BlockPos.getX(cell) + 0.5D, z = net.minecraft.core.BlockPos.getZ(cell) + 0.5D;
			best = Math.min(best, Math.hypot(point.x - x, point.z - z));
		}
		return best;
	}

	// ── the cap ──────────────────────────────────────────────────────

	/** At most 6 live pools with one challenger, one more for each extra, at most 9. */
	public static int poolCap(int challengers) {
		return Math.min(POOLS_MAX, POOLS + POOLS_PER_EXTRA * Math.max(0, challengers - 1));
	}

	/** At most 42 square blocks of sludge with one challenger, 6 more for each extra, at most 60. */
	public static double areaCap(int challengers) {
		return Math.min(AREA_MAX, AREA + AREA_PER_EXTRA * Math.max(0, challengers - 1));
	}

	/** How many of the live pools ({@code oldestFirst}) settle, oldest first, so that {@code adding} fits the cap. */
	public static int settleCount(List<Patch> oldestFirst, Patch adding, int challengers) {
		int cap = poolCap(challengers);
		double areaCap = areaCap(challengers);
		double area = adding.area();
		for (Patch pool : oldestFirst) area += pool.area();
		int settle = 0;
		while (settle < oldestFirst.size() && (oldestFirst.size() - settle + 1 > cap || area > areaCap + 1.0E-6D)) {
			area -= oldestFirst.get(settle).area();
			settle++;
		}
		return settle;
	}

	// ── the Bloom's drain ────────────────────────────────────────────

	/**
	 * Where the Bloom's field drains at its last pulse: up to two cells of the boss floor within {@code fieldRadius}
	 * of the body, at least {@link #BLOOM_SAFE_GAP} from every safe cell and {@link #BLOOM_CHALLENGER_GAP} from every
	 * challenger, picked by {@code seed}, the second clear of the first's pool.
	 */
	public static List<Vec3> bloomDrains(AABB floor, Vec3 body, double fieldRadius, LongSet safe, List<Vec3> challengers,
			double floorY, long seed) {
		List<Vec3> candidates = new ArrayList<>();
		for (int x = Mth.floor(floor.minX); x < Mth.ceil(floor.maxX); x++) {
			for (int z = Mth.floor(floor.minZ); z < Mth.ceil(floor.maxZ); z++) {
				Vec3 c = new Vec3(x + 0.5D, floorY, z + 0.5D);
				if (!BossFootprint.Shapes.planarInside(floor, c) || BossFootprint.Shapes.planar(c, body) > fieldRadius) continue;
				if (nearestAsh(c, safe) < BLOOM_SAFE_GAP) continue;
				boolean clear = true;
				for (Vec3 player : challengers) {
					if (BossFootprint.Shapes.planar(c, player) < BLOOM_CHALLENGER_GAP) {
						clear = false;
						break;
					}
				}
				if (clear) candidates.add(c);
			}
		}
		Collections.shuffle(candidates, new Random(seed));
		List<Vec3> picked = new ArrayList<>();
		for (Vec3 c : candidates) {
			if (picked.size() >= BLOOM_DRAINS) break;
			boolean apart = true;
			for (Vec3 other : picked) if (BossFootprint.Shapes.planar(c, other) < 2.0D * BLOOM_RADIUS) apart = false;
			if (apart) picked.add(c);
		}
		return picked;
	}

	// ── the ash a burned pool leaves ─────────────────────────────────

	/** The cells a burned pool turns to ash: its disc less {@link #ASH_INSET}, or the runnel's lane, at most {@code cap}. */
	public static LongList ashCells(Patch patch, int cap) {
		if (patch.runnel()) return PutricielArenaRules.lineCells(patch.from(), patch.to(), Math.max(0.0D, patch.radius() - ASH_INSET), cap);
		return PutricielArenaRules.discCells(patch.from(), Math.max(0.5D, patch.radius() - ASH_INSET), cap);
	}

	// ── the live sources ─────────────────────────────────────────────

	/** The first probe of {@code patch} a fire footprint strikes at {@code age}, or null. */
	@Nullable
	public static Vec3 touched(Patch patch, BossFootprint shape, FootprintFrame frame, double age) {
		if (shape instanceof BossFootprint.None) return null;
		for (Vec3 probe : patch.probes()) if (shape.hits(frame, probe, age)) return probe;
		return null;
	}

	/** The burning body lights a patch within its own radius plus {@link #BODY_REACH}. */
	public static boolean bodyTouches(Patch patch, Vec3 body, double bodyRadius) {
		return patch.edgeDistance(body) <= bodyRadius + BODY_REACH;
	}

	// ── the catch schedule ───────────────────────────────────────────

	/** Where a catch came from. */
	public enum Source { RING, PYRE, POOL, FIRE, BODY, PLAYER, FALLBACK }

	/**
	 * When and where one patch catches, ticks from the Ignition's commit: the catch point on its rim, where the fire
	 * came from, and the tick a runner leaves for it (-1 when no runner carries it).
	 */
	public record Catch(int tick, Vec3 point, Vec3 from, int depart, Source source) {
		public boolean runner() {
			return depart >= 0;
		}
	}

	/**
	 * The fire's schedule over the live patches: computed at the commit with the earliest source winning, then kept as
	 * the fight runs. A live source (a fire move, the body, a burning player) lights a patch at once, which can only
	 * bring its catch forward, and every pool that catches sends runners on to the pools near it.
	 */
	public static final class Schedule {
		private final List<Patch> patches;
		private final Catch[] best;
		private final boolean[] caught;

		private Schedule(List<Patch> patches, Catch[] best) {
			this.patches = List.copyOf(patches);
			this.best = best;
			this.caught = new boolean[patches.size()];
		}

		public int size() {
			return patches.size();
		}

		public Patch patch(int index) {
			return patches.get(index);
		}

		public Catch entry(int index) {
			return best[index];
		}

		public boolean caught(int index) {
			return caught[index];
		}

		public boolean allCaught() {
			for (boolean c : caught) if (!c) return false;
			return true;
		}

		/** The patches not yet lit whose catch is due by {@code now}. */
		public List<Integer> due(int now) {
			List<Integer> due = new ArrayList<>();
			for (int i = 0; i < best.length; i++) if (!caught[i] && best[i].tick() <= now) due.add(i);
			return due;
		}

		/** The patches a runner leaves for at {@code now}. */
		public List<Integer> departing(int now) {
			List<Integer> leaving = new ArrayList<>();
			for (int i = 0; i < best.length; i++) if (!caught[i] && best[i].runner() && best[i].depart() == now) leaving.add(i);
			return leaving;
		}

		/** Lights patch {@code index} at {@code now}; false when it already burned. Its runners set out for the pools near it. */
		public boolean light(int index, int now, Vec3 point, Vec3 from, Source source) {
			if (caught[index]) return false;
			caught[index] = true;
			best[index] = new Catch(now, point, from, best[index].runner() && best[index].tick() == now ? best[index].depart() : -1, source);
			relax(patches, best, caught, index, now);
			return true;
		}
	}

	/**
	 * The schedule at the commit (tick 0): the Ignition ring for patches within {@link #RING_REACH} of the body, a
	 * runner from each relit pyre ({@code pyres} in relight order, {@code relightGap} apart) to its nearest pool within
	 * {@link #PYRE_RUNNER_REACH}, runners on from every burning pool, and the fallback at {@link #FALLBACK}. The
	 * earliest wins. Runners carry fire to pools only; a colony patch catches from the ring, the body, a fire move or
	 * the fallback.
	 */
	public static Schedule catchSchedule(List<Patch> patches, Vec3 body, List<Vec3> pyres, int relightGap) {
		int n = patches.size();
		Catch[] best = new Catch[n];
		for (int i = 0; i < n; i++) {
			Patch patch = patches.get(i);
			Vec3 edge = patch.nearestEdge(body);
			best[i] = new Catch(FALLBACK, edge, body, -1, Source.FALLBACK);
			double distance = patch.edgeDistance(body);
			if (distance <= RING_REACH) {
				best[i] = new Catch((int) Math.ceil(RING_TRAVEL * distance / RING_REACH), edge, body, -1, Source.RING);
			}
		}
		for (int k = 0; k < pyres.size(); k++) {
			Vec3 pyre = pyres.get(k);
			int nearest = -1;
			double gap = Double.MAX_VALUE;
			for (int i = 0; i < n; i++) {
				Patch patch = patches.get(i);
				if (patch.colony()) continue;
				double g = patch.edgeDistance(pyre) - PutricielArenaRules.PYRE_SAFE;
				if (g <= PYRE_RUNNER_REACH && g < gap) {
					gap = g;
					nearest = i;
				}
			}
			if (nearest < 0) continue;
			Patch patch = patches.get(nearest);
			Vec3 from = toward(pyre, patch.nearestEdge(pyre), PutricielArenaRules.PYRE_SAFE, patch.from().y);
			Vec3 point = patch.nearestEdge(from);
			int depart = k * Math.max(0, relightGap) + PYRE_RUNNER_DELAY;
			int tick = depart + travel(BossFootprint.Shapes.planar(from, point));
			if (tick < best[nearest].tick()) best[nearest] = new Catch(tick, point, from, depart, Source.PYRE);
		}
		// the burning pools pass the fire on: Dijkstra over the catch ticks
		boolean[] done = new boolean[n];
		for (int round = 0; round < n; round++) {
			int next = -1;
			for (int i = 0; i < n; i++) if (!done[i] && (next < 0 || best[i].tick() < best[next].tick())) next = i;
			if (next < 0) break;
			done[next] = true;
			relax(patches, best, done, next, best[next].tick());
		}
		return new Schedule(patches, best);
	}

	/** A pool burning from {@code tick} sends a runner, 6 ticks later, to every unfinished pool within 3 of its rim. */
	private static void relax(List<Patch> patches, Catch[] best, boolean[] finished, int index, int tick) {
		Patch burning = patches.get(index);
		if (burning.colony()) return;
		for (int j = 0; j < patches.size(); j++) {
			if (j == index || finished[j]) continue;
			Patch other = patches.get(j);
			if (other.colony() || burning.gap(other) > POOL_RUNNER_REACH) continue;
			Vec3 from = burning.nearestEdge(other.nearestEdge(burning.centre()));
			Vec3 point = other.nearestEdge(from);
			int depart = tick + POOL_RUNNER_DELAY;
			int arrive = depart + travel(BossFootprint.Shapes.planar(from, point));
			if (arrive < best[j].tick()) best[j] = new Catch(arrive, point, from, depart, Source.POOL);
		}
	}

	/** The point {@code distance} from {@code centre} toward {@code toward}, on the floor {@code y}. */
	private static Vec3 toward(Vec3 centre, Vec3 toward, double distance, double y) {
		double dx = toward.x - centre.x, dz = toward.z - centre.z;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-6D) return new Vec3(centre.x, y, centre.z);
		double step = Math.min(distance, length);
		return new Vec3(centre.x + dx / length * step, y, centre.z + dz / length * step);
	}

	/** At the ending every unlit pool catches at once from its edge nearest the body, its burn scaled to 10 ticks. */
	public static Vec3 endingCatch(Patch patch, Vec3 body) {
		return patch.nearestEdge(body);
	}
}
