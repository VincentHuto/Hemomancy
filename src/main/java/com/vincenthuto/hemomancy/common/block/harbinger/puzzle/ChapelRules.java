package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The four Chapels of a Saint's mausoleum, each a deed of the Saint's life told backwards. Beholding replays a memory
 * the player must walk again; Litany names offerings in Abocipher; Downfall is the fall, fought; Repose is stillness
 * under threat, where a Silentium has the edge.
 *
 * <p>Each trial plays across its wing (Track 3): the generator writes the wing's marks into the focus as offsets from
 * it, authored with the focus facing north, and {@link #place} turns them with the focus as it was placed.
 */
public final class ChapelRules {
	public enum Kind { BEHOLDING, LITANY, DOWNFALL, REPOSE }

	/**
	 * What a mark is for. WITNESS: a stele the Beholding's memory touches and the player walks back to. LITANY_STONE:
	 * where the Litany is read. OFFERING_STAND: holds one candidate (its index into the Litany's choices). START: the
	 * Downfall's gate. SPAWN: a seal a Downfall wave (its index) rises from. CIRCLE: the centre of Repose's still ring.
	 * PYLON: where a Repose pulse fires from.
	 */
	public enum MarkKind { WITNESS, LITANY_STONE, OFFERING_STAND, START, SPAWN, CIRCLE, PYLON }

	/** One mark: its offset from the focus as authored (focus facing north), its kind and its index. */
	public record Mark(BlockPos offset, MarkKind kind, int index) {}

	public static final int ATTEMPT_DEGREE = 8;
	public static final int BEHOLDING_STEPS = 4;
	public static final int BEHOLDING_FLASH_TICKS = 25;
	public static final int ANSWER_TICKS = 400;
	public static final int LITANY_CHOICES = 6;
	public static final int DOWNFALL_WAVES = 3;
	public static final int DOWNFALL_WAVE_SIZE = 3;
	public static final int REPOSE_TICKS = 600;
	public static final int REPOSE_PULSE_TICKS = 60;
	public static final float REPOSE_PULSE_DAMAGE = 2f;
	public static final double REPOSE_RADIUS = 3.5;
	public static final double CHAPEL_RADIUS = 6.0;
	/** A challenger this far beyond the wing's farthest mark has left the trial. */
	public static final double LEAVE_MARGIN = 8.0;
	public static final double STONE_REACH = 3.0;
	public static final double GATE_REACH = 3.0;
	/** A Downfall wave rises once its challenger comes this close to its seals. */
	public static final double WAVE_TRIGGER = 7.0;
	/** The fall ends when its challenger, every wave down, reaches the focus. */
	public static final double FINISH_REACH = 3.0;
	/** A Repose pulse reaches every player this close to the still ring's centre. */
	public static final double PULSE_REACH = 9.0;
	public static final int FAIL_COOLDOWN_TICKS = 100;
	private static final double STILL_SQR = 0.0025;

	private ChapelRules() {}

	public static boolean mayAttempt(int degree) {
		return degree >= ATTEMPT_DEGREE;
	}

	/** The witnesses a Beholding's memory touches among its four, in order: never the same one twice running. */
	public static List<Integer> beholdingSequence(long seed) {
		return beholdingSequence(seed, 4);
	}

	/** The same among {@code witnesses} witnesses. */
	public static List<Integer> beholdingSequence(long seed, int witnesses) {
		RandomSource random = RandomSource.create(seed);
		List<Integer> sequence = new ArrayList<>();
		int last = -1;
		while (sequence.size() < BEHOLDING_STEPS) {
			int witness = random.nextInt(witnesses);
			if (witness == last && witnesses > 1) continue;
			sequence.add(witness);
			last = witness;
		}
		return sequence;
	}

	/** Three different offerings, by index into the Litany's choices, in the order they must be given. */
	public static List<Integer> litanyOrder(long seed) {
		RandomSource random = RandomSource.create(seed);
		List<Integer> order = new ArrayList<>();
		while (order.size() < 3) {
			int choice = random.nextInt(LITANY_CHOICES);
			if (!order.contains(choice)) order.add(choice);
		}
		return order;
	}

	/** A Litany is read and answered at leisure: a minute, where a Beholding's answer has twenty seconds. */
	public static final int LITANY_ANSWER_TICKS = 1200;
	private static final double WITNESS_SQR = 2.56;

	/** Standing at a witness (offsets from its centre): pressed against the stele, not merely in the same room. */
	public static boolean atWitness(double dx, double dz) {
		return dx * dx + dz * dz < WITNESS_SQR;
	}

	/** The rotation that turned a focus authored facing north to {@code facing}. */
	public static Rotation rotation(Direction facing) {
		return switch (facing) {
			case EAST -> Rotation.CLOCKWISE_90;
			case SOUTH -> Rotation.CLOCKWISE_180;
			case WEST -> Rotation.COUNTERCLOCKWISE_90;
			default -> Rotation.NONE;
		};
	}

	/** A mark's position in the world, for a focus at {@code focus} facing {@code facing}. */
	public static BlockPos place(BlockPos focus, BlockPos offset, Direction facing) {
		return focus.offset(offset.rotate(rotation(facing)));
	}

	/** How far a challenger may stray from the focus: past the wing's farthest mark by a margin, never under 12. */
	public static double reach(List<Mark> marks) {
		double farthest = 0;
		for (Mark mark : marks) farthest = Math.max(farthest, Math.sqrt(mark.offset().distSqr(BlockPos.ZERO)));
		return Math.max(CHAPEL_RADIUS * 2, farthest + LEAVE_MARGIN);
	}

	/** The marks of one kind, ordered by index. */
	public static List<Mark> marksOf(List<Mark> marks, MarkKind kind) {
		return marks.stream().filter(mark -> mark.kind() == kind)
				.sorted(Comparator.comparingInt(Mark::index)).toList();
	}

	/** The seals a Downfall wave rises from: those carrying its index, or every seal when none does. */
	public static List<Mark> seals(List<Mark> marks, int wave) {
		List<Mark> all = marksOf(marks, MarkKind.SPAWN);
		List<Mark> own = all.stream().filter(mark -> mark.index() == wave).toList();
		return own.isEmpty() ? all : own;
	}

	/** A Repose pulse never takes the last of a player's life: the trial is stillness, not attrition. */
	public static float pulseDamage(float health) {
		return health <= REPOSE_PULSE_DAMAGE + 1f ? 0f : REPOSE_PULSE_DAMAGE;
	}

	/** A Litany's words are carved into its Chapel: the same order every attempt. */
	public static long litanySeed(long posKey, int slot) {
		return posKey * 31L + slot * 0x9E3779B97F4A7C15L;
	}

	public static boolean still(double moveSqr) {
		return moveSqr < STILL_SQR;
	}

	/** Ticks before a pulse over which the pylons wind their charge up, one step a third of the way each. */
	public static final int PYLON_WIND_TICKS = 30;

	/** A pylon's charge (0-3) on tick {@code timer} of a Repose: rising over the wind-up, spent as the pulse fires. */
	public static int pylonCharge(int timer) {
		int phase = Math.floorMod(timer, REPOSE_PULSE_TICKS);
		int windStart = REPOSE_PULSE_TICKS - PYLON_WIND_TICKS;
		if (phase < windStart) return 0;
		return 1 + (phase - windStart) * 3 / PYLON_WIND_TICKS;
	}

	/** Repose progress for one tick: nothing for a moving body; a Silentium's stillness counts double. */
	public static int reposeGain(boolean silentium, boolean still) {
		if (!still) return 0;
		return silentium ? 2 : 1;
	}
}
