package com.vincenthuto.hemomancy.common.manipulation.animus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The numbers and geometry of Living Land (core-loop Track 9), with no level in them. A patch is the face the player
 * woke: its plane is the surface the strikes come out of. The struck face picks the mode (floor, wall or ceiling); a
 * strike takes a short column of real blocks straight back into the surface under the enemy's shadow on that plane,
 * carries it out to the enemy and back, and the blocks settle where they came from.
 */
public final class LivingLandTerrainRules {
	/** What the patch's face makes of a strike. */
	public enum Mode {
		/** Teeth of ground rise out of the floor under the enemy. */
		FLOOR_TEETH,
		/** Lances run out of a wall at the enemy. */
		WALL_LANCES,
		/** The ceiling comes down on the enemy. */
		CEILING_CRUSH;

		public static Mode forFace(Direction face) {
			return switch (face) {
				case UP -> FLOOR_TEETH;
				case DOWN -> CEILING_CRUSH;
				default -> WALL_LANCES;
			};
		}
	}

	/** The short charge between the first paid pulse and the first strike: the patch wakes. */
	public static final int CHARGE_TICKS = 20;
	/** Ticks between pulses; a strike can leave on any pulse after the charge. */
	public static final int CADENCE = 10;
	/** Blood a pulse costs before the cost ledger: 30 mL a second. */
	public static final double BLOOD_PER_PULSE = 15.0D;
	/** How far from the struck block, on its plane, the patch reaches for a source column. */
	public static final double PATCH_RADIUS = 7.0D;
	/** How far in front of the patch's plane an enemy may stand and still be struck. */
	public static final double MAX_FRONT = 9.0D;
	/** How far from the projected shadow of an enemy a source cell may be. */
	public static final int SHADOW_SEARCH = 2;
	public static final int MIN_LENGTH = 2;
	public static final int MAX_LENGTH = 5;
	/** A raise never lifts the patch more than this many blocks. */
	public static final int MAX_RAISE = 3;
	/** How deep behind the struck block a raise looks for ground to stack. */
	public static final int RAISE_SOURCE_DEPTH = 6;
	/** Blocks a tick the strike's head travels out, and back. */
	public static final double OUT_SPEED = 0.7D;
	public static final double BACK_SPEED = 0.6D;
	/** A strike that has not reached its enemy by now turns back. */
	public static final int REACH_TICKS = 50;
	/** A strike that has not settled by now settles where it is. */
	public static final int SETTLE_DEADLINE_TICKS = 140;
	/** Ticks a strike holds in its enemy before it goes home. */
	public static final int LINGER_TICKS = 8;
	/** How close the head must come to the enemy's box to land. */
	public static final double LAND_MARGIN = 0.35D;

	private LivingLandTerrainRules() {
	}

	/**
	 * Blocks in one strike's column: three, a fourth at mastery level 2 and a fifth at 4, times the pulse's work scale
	 * (a dry pulse, x0.75, takes fewer), never under two.
	 */
	public static int pillarLength(int manipLevel, double workScale) {
		int base = 3 + (manipLevel >= 2 ? 1 : 0) + (manipLevel >= 4 ? 1 : 0);
		return clamp((int) Math.round(base * workScale), MIN_LENGTH, MAX_LENGTH);
	}

	/** Strikes in flight from one patch at once: one, and two from mastery level 3 ("two at a later rank"). */
	public static int strikesInFlight(int manipLevel) {
		return manipLevel >= 3 ? 2 : 1;
	}

	/** What a strike deals on landing: 4 + 2 for every block past the second, so 6 for a three-block column. */
	public static float damage(int length) {
		return 4.0F + 2.0F * Math.max(0, length - MIN_LENGTH);
	}

	/** How high a raise may stand: three blocks, fewer when the pulse is dry. */
	public static int maxRaise(double workScale) {
		return clamp((int) Math.round(MAX_RAISE * workScale), 1, MAX_RAISE);
	}

	/**
	 * Whether a point stands in front of the patch: on the face's side of its plane, and close enough. {@code patch}
	 * is the struck block; the plane is its {@code face}.
	 */
	public static boolean inFront(BlockPos patch, Direction face, Vec3 point) {
		double front = frontDistance(patch, face, point);
		return front > 0.0D && front <= MAX_FRONT;
	}

	/** Distance of {@code point} in front of the face plane of {@code patch} (negative behind it). */
	public static double frontDistance(BlockPos patch, Direction face, Vec3 point) {
		double plane = face.getAxis().choose(patch.getX(), patch.getY(), patch.getZ())
				+ (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D : 0.0D);
		double along = face.getAxis().choose(point.x, point.y, point.z);
		return (along - plane) * face.getAxisDirection().getStep();
	}

	/** The cell on the patch's plane under {@code point}'s shadow (the point moved straight back onto the plane). */
	public static BlockPos shadow(BlockPos patch, Direction face, Vec3 point) {
		BlockPos cell = BlockPos.containing(point);
		return switch (face.getAxis()) {
			case X -> new BlockPos(patch.getX(), cell.getY(), cell.getZ());
			case Y -> new BlockPos(cell.getX(), patch.getY(), cell.getZ());
			case Z -> new BlockPos(cell.getX(), cell.getY(), patch.getZ());
		};
	}

	/**
	 * The source column for a strike at {@code target}: of the cells within {@link #SHADOW_SEARCH} of the target's
	 * shadow on the patch's plane and no further than {@link #PATCH_RADIUS} from the struck block, whose front is open
	 * and which can be lifted, the one about a column's length from the target (then the one nearest the shadow), and the
	 * cells straight behind it, as many as can be lifted up to {@code length}. Empty when no cell serves or the column
	 * would be shorter than {@link #MIN_LENGTH}. The first cell of the column is the one at the surface.
	 */
	public static Optional<List<BlockPos>> sourceColumn(BlockPos patch, Direction face, Vec3 target, int length,
			Predicate<BlockPos> liftable, Predicate<BlockPos> open) {
		if (!inFront(patch, face, target)) return Optional.empty();
		BlockPos shadow = shadow(patch, face, target);
		List<BlockPos> candidates = new ArrayList<>();
		for (int a = -SHADOW_SEARCH; a <= SHADOW_SEARCH; a++) {
			for (int b = -SHADOW_SEARCH; b <= SHADOW_SEARCH; b++) {
				BlockPos cell = switch (face.getAxis()) {
					case X -> shadow.offset(0, a, b);
					case Y -> shadow.offset(a, 0, b);
					case Z -> shadow.offset(a, b, 0);
				};
				if (cell.distSqr(patch) <= PATCH_RADIUS * PATCH_RADIUS) candidates.add(cell);
			}
		}
		// Prefer the cell from which the whole column has to come out to reach the target, so a strike at an enemy
		// standing on the patch erupts beside it and arcs in rather than poking a block up under its feet.
		double reach = length - 0.5D;
		candidates.sort(Comparator.<BlockPos>comparingDouble(cell -> Math.abs(Math.sqrt(Vec3.atCenterOf(cell).distanceToSqr(target)) - reach))
				.thenComparingDouble(cell -> cell.distSqr(shadow))
				.thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ));
		for (BlockPos cell : candidates) {
			if (!open.test(cell.relative(face)) || !liftable.test(cell)) continue;
			List<BlockPos> column = new ArrayList<>(length);
			for (int depth = 0; depth < length; depth++) {
				BlockPos next = cell.relative(face.getOpposite(), depth);
				if (depth > 0 && !liftable.test(next)) break;
				column.add(next);
			}
			if (column.size() >= MIN_LENGTH) return Optional.of(column);
		}
		return Optional.empty();
	}

	/**
	 * A raise of the patch to {@code height}: the cells it fills, stacked out of the struck face, and the ground it
	 * takes them from, found behind the struck block (so the surface the player sees stays whole and the holes are
	 * underground). Returns the pairs for the next {@code height - standing} steps only; empty when the next cell out is
	 * not open or no ground is left behind to take.
	 */
	public static List<RaiseStep> raiseSteps(BlockPos patch, Direction face, int standing, int height,
			Predicate<BlockPos> liftable, Predicate<BlockPos> open, Predicate<BlockPos> taken) {
		List<RaiseStep> steps = new ArrayList<>();
		int depth = 1;
		for (int level = standing + 1; level <= height; level++) {
			BlockPos into = patch.relative(face, level);
			if (!open.test(into)) break;
			BlockPos from = null;
			for (; depth <= RAISE_SOURCE_DEPTH; depth++) {
				BlockPos candidate = patch.relative(face.getOpposite(), depth);
				if (!taken.test(candidate) && liftable.test(candidate)) {
					from = candidate;
					depth++;
					break;
				}
			}
			if (from == null) break;
			steps.add(new RaiseStep(from, into));
		}
		return steps;
	}

	/** One block a raise moves: from the ground behind the patch to a cell stacked out of its face. */
	public record RaiseStep(BlockPos from, BlockPos into) {
	}

	/**
	 * Where a block goes when the cell it came from is taken: the nearest free cell within {@code radius}, nearest
	 * first and then by coordinates, so the choice never depends on iteration order. Empty when none is free.
	 */
	public static Optional<BlockPos> nearestFree(BlockPos origin, int radius, Predicate<BlockPos> free) {
		List<BlockPos> cells = new ArrayList<>();
		for (BlockPos cell : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
			if (cell.distSqr(origin) <= (double) radius * radius) cells.add(cell.immutable());
		}
		cells.sort(Comparator.<BlockPos>comparingDouble(cell -> cell.distSqr(origin))
				.thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
		for (BlockPos cell : cells) {
			if (free.test(cell)) return Optional.of(cell);
		}
		return Optional.empty();
	}

	/**
	 * Where block {@code index} of a strike stands (0 is the head): one block apart along a curve that leaves the
	 * surface square to it and bends to the head. A block the head has not yet drawn out sits back in its socket,
	 * behind the face, so the column slides out of the ground as the head advances.
	 */
	public static Vec3 blockCentre(Vec3 root, Direction face, Vec3 head, int index) {
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		double chord = head.distanceTo(root);
		Vec3 control = root.add(normal.scale(chord * 0.5D));
		double length = arcLength(root, control, head);
		double back = index;
		if (back >= length) return root.subtract(normal.scale(back - length + 0.5D));
		double t = length <= 1.0E-6D ? 0.0D : 1.0D - back / length;
		return bezier(root, control, head, t);
	}

	private static double arcLength(Vec3 a, Vec3 b, Vec3 c) {
		double length = 0.0D;
		Vec3 previous = a;
		for (int i = 1; i <= 12; i++) {
			Vec3 next = bezier(a, b, c, i / 12.0D);
			length += next.distanceTo(previous);
			previous = next;
		}
		return length;
	}

	static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double t) {
		double u = 1.0D - t;
		return a.scale(u * u).add(b.scale(2.0D * u * t)).add(c.scale(t * t));
	}

	/** The point a strike leaves from and returns to: the middle of the front face of its column's surface block. */
	public static Vec3 root(BlockPos surface, Direction face) {
		return Vec3.atCenterOf(surface).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5D));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
