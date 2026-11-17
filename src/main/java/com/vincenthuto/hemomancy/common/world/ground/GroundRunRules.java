package com.vincenthuto.hemomancy.common.world.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The ground network's numbers and its pure rules (core-loop Track 6): how a run of gouges or veins hangs together,
 * how far a pulse travels and how fast it lights, how a stain ages, how much a vein may grow and carry, and how a
 * basin's level reads in its gutters. No level access: the callers hand in what they see.
 */
public final class GroundRunRules {
	// ── Runs ──

	/** The most cells one pulse lights, as the basin's search was bounded. */
	public static final int MAX_RUN = 64;
	/** A pulse lights this many cells of distance per tick, so a long run never switches its light in one tick. */
	public static final int STAGGER_CELLS_PER_TICK = 2;
	/** A lit cell does its work and checks its time this often. */
	public static final int EFFECT_INTERVAL = 10;
	/** A hot cell smelts one item lying on it every this many effect ticks (every one and a half seconds). */
	public static final int HOT_SMELT_EVERY = 3;
	/**
	 * The burn a hot cell leaves in a furnace, smoker or blast furnace beside it each effect tick: one effect tick and a
	 * margin, so the furnace burns on at coal speed while the run holds and goes out within a second of it going dark.
	 */
	public static final int HOT_FURNACE_BURN = EFFECT_INTERVAL + 5;
	/** One hot run burns at most this many furnaces, smokers and blast furnaces, the nearest its pulse first (CQ-43). */
	public static final int HOT_FURNACE_MAX = 4;
	/** A feeding run grows each crop beside it a stage every this many effect ticks (every thirty seconds, CB-24). */
	public static final int FEED_EVERY = 60;
	/** How long a run stays lit when no catalyst went into it. */
	public static final int BARE_LIT_TICKS = 600;
	/** A catalyst feeder tops a run up once it has this little time left. */
	public static final int FEEDER_MARGIN_TICKS = 60;
	/** The blood one pulse into a run costs. */
	public static final double PULSE_ML = 15.0D;
	/** The blood of the run's lighter a catalyst feeder spends each time it dissolves a catalyst (CQ-7, NQ-02-2). */
	public static final double FEEDER_ML = 15.0D;
	/** Ticks between two pulses one player sends into the ground. */
	public static final int PULSE_INTERVAL = 20;

	// ── Stains ──

	public static final int WET = 0;
	public static final int TACKY = 1;
	public static final int DRY = 2;
	/** The blood one new stain costs. */
	public static final double STAIN_ML = 4.0D;
	/** Wetting a tacky or dry stain again. */
	public static final double REWET_ML = 2.0D;
	/** What Absorption takes back up from a wet stain. */
	public static final double STAIN_REFUND_ML = 2.0D;
	/** Ticks between two stains one player lays, or two the Absorption takes up. */
	public static final int STAIN_INTERVAL = 4;
	/** The chance a random tick ages a wet or tacky stain one step. */
	public static final float AGE_CHANCE = 0.5F;
	/** The chance a random tick wears a dry stain away. */
	public static final float FADE_CHANCE = 0.25F;
	/** The chance a creature with blood leaves a stain where it died. */
	public static final float KILL_STAIN_CHANCE = 0.35F;
	/** The chance, every {@link #BLEED_CHECK_TICKS}, that a heavily bleeding creature leaves a stain. */
	public static final float BLEED_STAIN_CHANCE = 0.25F;
	public static final int BLEED_CHECK_TICKS = 40;

	// ── Living veins ──

	/** Blood a vein moves between its two nodes per second, at most. */
	public static final double VEIN_FLOW_PER_SECOND = 20.0D;
	/** The owner must stand this close to a node, in the same dimension, for its vein to move blood. */
	public static final double VEIN_OWNER_RANGE = 32.0D;
	/** A tank blood left within this many ticks is drawing, and a vein runs toward it (CL-14, five seconds). */
	public static final long VEIN_DRAW_WINDOW_TICKS = 100L;
	/** The draw stamp of a tank nothing has drawn from yet. */
	public static final long NEVER_DRAWN = Long.MIN_VALUE;
	/** The upkeep the owner pays each second a vein moves blood. */
	public static final double VEIN_UPKEEP_ML = 1.0D;
	/** A vein left without its owner this long withers back to a gouge. */
	public static final long VEIN_WITHER_TICKS = 24_000L;
	/** Growing tissue into one cell of gouge. */
	public static final double VEIN_GROWTH_ML = 20.0D;
	/** Cells one growth pulse reaches. */
	public static final int VEIN_GROWTH_PER_PULSE = 4;

	// ── Basins and gutters ──

	/** A player's venous basin holds this much. */
	public static final double BASIN_CAPACITY = 2_000.0D;
	/** What a basin's gutters deliver per second, shared between what they reach. */
	public static final double GUTTER_FLOW_PER_SECOND = 10.0D;
	/** Every this many gutters from a player's basin, the stream runs one fill level lower (the arena's basins drop every four). */
	public static final int GUTTER_FALLOFF = 8;
	/** Blood that keeps one stretch of farmland at a gutter's end wet for a second. */
	public static final double FARMLAND_ML = 0.5D;

	private static final List<BlockPos> NEIGHBOURS = neighbours();

	private GroundRunRules() {
	}

	/** One cell of a run: how many steps it lies from the pulse's origin, and the cell it was reached from. */
	public record Step(BlockPos pos, int distance, BlockPos parent) {
	}

	/**
	 * The run that holds {@code start}, breadth first, at most {@code limit} cells. Cells join their 18 neighbours
	 * (faces and edges, not corners), so a run follows a floor up a step and round a wall's corner. The origin is its own
	 * parent.
	 */
	public static List<Step> walk(BlockPos start, Predicate<BlockPos> isRun, int limit) {
		List<Step> run = new ArrayList<>();
		if (limit <= 0 || !isRun.test(start)) return run;
		ArrayDeque<Step> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		BlockPos origin = start.immutable();
		queue.add(new Step(origin, 0, origin));
		seen.add(origin);
		while (!queue.isEmpty() && run.size() < limit) {
			Step step = queue.poll();
			run.add(step);
			for (BlockPos offset : NEIGHBOURS) {
				BlockPos next = step.pos().offset(offset);
				if (seen.add(next) && isRun.test(next)) queue.add(new Step(next, step.distance() + 1, step.pos()));
			}
		}
		return run;
	}

	/** Whether two cells are joined as a run joins them: they share a face or an edge, not only a corner. */
	public static boolean joined(BlockPos a, BlockPos b) {
		int dx = Math.abs(a.getX() - b.getX()), dy = Math.abs(a.getY() - b.getY()), dz = Math.abs(a.getZ() - b.getZ());
		int moved = dx + dy + dz;
		return dx <= 1 && dy <= 1 && dz <= 1 && (moved == 1 || moved == 2);
	}

	/** Ticks after the pulse that a cell {@code distance} steps out lights. */
	public static long lightDelay(int distance) {
		return 1L + Math.max(0, distance) / STAGGER_CELLS_PER_TICK;
	}

	/** The cell of {@code run} nearest {@code target}: where a pointing run flares. */
	public static BlockPos farEnd(List<Step> run, BlockPos target) {
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (Step step : run) {
			double distance = step.pos().distSqr(target);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = step.pos();
			}
		}
		return best;
	}

	/**
	 * The furnaces a hot run may burn (CQ-43): walking {@code run} nearest the pulse first, each cell takes the furnaces
	 * beside it that no nearer cell took, until {@code max} are taken. Returns each cell's mask of sides
	 * ({@code 1 << Direction#get3DDataValue}); a cell with none is left out.
	 */
	public static Map<BlockPos, Integer> furnaceSides(List<Step> run, Predicate<BlockPos> isFurnace, int max) {
		Map<BlockPos, Integer> sides = new HashMap<>();
		Set<BlockPos> taken = new HashSet<>();
		for (Step step : run) {
			if (taken.size() >= max) break;
			int mask = 0;
			for (Direction side : Direction.values()) {
				if (taken.size() >= max) break;
				BlockPos next = step.pos().relative(side);
				if (isFurnace.test(next) && taken.add(next.immutable())) mask |= 1 << side.get3DDataValue();
			}
			if (mask != 0) sides.put(step.pos(), mask);
		}
		return sides;
	}

	/** Whether a hot cell with {@code mask} may burn the furnace on its {@code side}. */
	public static boolean burnsSide(int mask, Direction side) {
		return (mask & (1 << side.get3DDataValue())) != 0;
	}

	/** How long a run stays lit for a catalyst worth {@code ticks}; a bare pulse is short. */
	public static int litTicks(int catalystTicks) {
		return catalystTicks > 0 ? catalystTicks : BARE_LIT_TICKS;
	}

	/** A stain's age after one random tick: wet to tacky to dry, or -1 when a dry stain wears away. */
	public static int nextStainAge(int age, float roll) {
		if (age >= DRY) return roll < FADE_CHANCE ? -1 : DRY;
		return roll < AGE_CHANCE ? age + 1 : age;
	}

	/** What Absorption takes back from a stain of {@code age}: part of a wet stain, nothing from an older one. */
	public static double stainRefund(int age) {
		return age == WET ? STAIN_REFUND_ML : 0.0D;
	}

	/** The total vein length a player of Harbinger degree {@code degree} may keep grown. */
	public static int veinAllowance(int degree) {
		if (degree < 3) return 0;
		return switch (degree) {
			case 3 -> 16;
			case 4 -> 24;
			case 5 -> 32;
			case 6 -> 48;
			default -> 64;
		};
	}

	/**
	 * How much one end of a vein is drawing (CL-14, NQ-02-3): a tank blood left in the last
	 * {@link #VEIN_DRAW_WINDOW_TICKS} by anything but the vein or clotting draws to fill its room; a machine
	 * ({@code machine}, not a store) below {@code workingReserve} draws up to it, so a dry machine can start. A store
	 * never pulls by its level. 0 when the end is not drawing.
	 */
	public static double drawDemand(long now, long lastDrawn, double fresh, double room, boolean machine, double workingReserve) {
		if (room <= 0.0D) return 0.0D;
		if (lastDrawn != NEVER_DRAWN && now >= lastDrawn && now - lastDrawn <= VEIN_DRAW_WINDOW_TICKS) return room;
		if (machine && fresh < workingReserve) return Math.min(room, workingReserve - fresh);
		return 0.0D;
	}

	/**
	 * The blood a vein moves in one second from node A's reservoir to node B's (negative: from B to A), CL-14: only
	 * toward an end that is drawing ({@link #drawDemand}), at most {@code maxFlow}, never more than the source holds
	 * fresh, the target has room for or the target draws. With neither end drawing nothing moves. With both drawing the
	 * fuller tank, by share of its size, runs toward the emptier until the two stand level.
	 */
	public static double veinTransfer(double freshA, double roomA, double capacityA, double demandA, double freshB,
			double roomB, double capacityB, double demandB, double maxFlow) {
		if (capacityA <= 0.0D || capacityB <= 0.0D || maxFlow <= 0.0D) return 0.0D;
		boolean aDraws = demandA > 0.0D;
		boolean bDraws = demandB > 0.0D;
		if (!aDraws && !bDraws) return 0.0D;
		if (bDraws && !aDraws) return Math.max(0.0D, Math.min(Math.min(maxFlow, freshA), Math.min(roomB, demandB)));
		if (aDraws && !bDraws) return -Math.max(0.0D, Math.min(Math.min(maxFlow, freshB), Math.min(roomA, demandA)));
		return level(freshA, roomA, capacityA, freshB, roomB, capacityB, maxFlow);
	}

	/** Two drawing tanks: the fuller, by share of its size, runs toward the emptier until they stand level. */
	private static double level(double freshA, double roomA, double capacityA, double freshB, double roomB,
			double capacityB, double maxFlow) {
		double shareA = freshA / capacityA;
		double shareB = freshB / capacityB;
		if (Math.abs(shareA - shareB) < 1.0E-6D) return 0.0D;
		// Moving x from the fuller to the emptier changes the gap by x/capFrom + x/capTo; stop when they meet.
		boolean aToB = shareA > shareB;
		double fromCap = aToB ? capacityA : capacityB;
		double toCap = aToB ? capacityB : capacityA;
		double level = Math.abs(shareA - shareB) / (1.0D / fromCap + 1.0D / toCap);
		double amount = Math.min(Math.min(maxFlow, level), Math.min(aToB ? freshA : freshB, aToB ? roomB : roomA));
		amount = Math.max(0.0D, amount);
		return aToB ? amount : -amount;
	}

	/** The fill a basin shows (0 to 3) for the blood in it, fresh and clotted together, against its size. */
	public static int basinFill(double held, double capacity) {
		if (held <= 0.0D || capacity <= 0.0D) return 0;
		double share = held / capacity;
		if (share >= 0.66D) return 3;
		if (share >= 0.33D) return 2;
		return 1;
	}

	/** How full a gutter {@code distance} steps from a basin of fill {@code basinFill} runs while it pours. */
	public static int streamAt(int basinFill, int distance) {
		return Math.max(0, basinFill - Math.max(0, distance) / GUTTER_FALLOFF);
	}

	/**
	 * What a gutter on a network shared by several worked basins shows: the highest stream any of them gives it, so one
	 * basin's walk never lowers what another keeps wet.
	 */
	public static int sharedFill(int own, int... others) {
		int fill = Math.max(0, own);
		for (int other : others) fill = Math.max(fill, other);
		return fill;
	}

	/** Each of {@code targets} takes an even share of a second's flow, or what the basin has left. */
	public static double deliveryShare(double fresh, int targets) {
		if (targets <= 0 || fresh <= 0.0D) return 0.0D;
		return Math.min(GUTTER_FLOW_PER_SECOND, fresh) / targets;
	}

	private static List<BlockPos> neighbours() {
		List<BlockPos> offsets = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					int moved = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
					if (moved == 1 || moved == 2) offsets.add(new BlockPos(dx, dy, dz));
				}
			}
		}
		return List.copyOf(offsets);
	}
}
