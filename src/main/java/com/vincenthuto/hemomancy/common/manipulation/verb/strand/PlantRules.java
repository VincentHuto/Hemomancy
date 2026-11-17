package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The plant: the player aims at the ground and holds, and blood sinks into the block a pulse at a time. A telegraph
 * ring grows with what has sunk, up to {@link #CAP_PULSES}. Release, a snap, or the cap sets off an eruption after a
 * short beat, its radius and damage scaled by the blood sunk. Walking away from the planted block sets it off too.
 *
 * <p>A plant sunk into a wet stain (core-loop Track 6) does not ring: it runs along the stain instead, one small
 * eruption a cell, {@link #STAIN_STEP_TICKS} apart, over as many wet cells as the blood sunk reaches
 * ({@link #stainCells}), and the run spends the stain's wet blood.
 */
public final class PlantRules {
	public static final int CAP_PULSES = 8;
	/** Ticks between the trigger and the eruption: the beat a creature in the ring has to step out. */
	public static final int ERUPTION_DELAY = 10;
	public static final double MIN_RADIUS = 1.5D, MAX_RADIUS = 4.0D;
	public static final float MIN_DAMAGE = 4.0F, MAX_DAMAGE = 12.0F;
	/** Past this many times the strand's length from the planted block, the plant goes off on its own. */
	public static final double LEASH = 1.5D;
	/** A stain run: wet cells at the first pulse, more a pulse, and the most it ever runs. */
	public static final int STAIN_BASE_CELLS = 4, STAIN_CELLS_PER_PULSE = 2, STAIN_MAX_CELLS = 20;
	/** How far from the planted cell a stain run may reach, in blocks. */
	public static final int STAIN_REACH = 8;
	/** Each cell of a run erupts this much after the last, and this wide. */
	public static final int STAIN_STEP_TICKS = 2;
	public static final double STAIN_CELL_RADIUS = 0.75D;

	private PlantRules() {
	}

	/** How full the plant is, from 0 to 1, after {@code sunkPulses} paid pulses. */
	public static double fraction(int sunkPulses) {
		return Math.max(0.0D, Math.min(1.0D, sunkPulses / (double) CAP_PULSES));
	}

	public static boolean full(int sunkPulses) {
		return sunkPulses >= CAP_PULSES;
	}

	/** Whether anything has sunk: an eruption needs at least one paid pulse, or it fizzles. */
	public static boolean primed(int sunkPulses) {
		return sunkPulses > 0;
	}

	public static double radius(int sunkPulses) {
		return MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * fraction(sunkPulses);
	}

	public static float damage(int sunkPulses) {
		return (float) (MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * fraction(sunkPulses));
	}

	/** How many wet cells a run along a stain reaches after {@code sunkPulses} paid pulses. */
	public static int stainCells(int sunkPulses) {
		if (sunkPulses <= 0) return 0;
		return Math.min(STAIN_MAX_CELLS, STAIN_BASE_CELLS + STAIN_CELLS_PER_PULSE * sunkPulses);
	}

	/**
	 * The wet cells a run starting at {@code start} reaches, in the order it reaches them: a flood through the six
	 * neighbours of each wet cell, no further than {@link #STAIN_REACH} blocks from the start in any axis, at most
	 * {@code limit} cells. Empty when {@code start} itself is not wet.
	 */
	public static List<BlockPos> stainRun(BlockPos start, Predicate<BlockPos> wet, int limit) {
		List<BlockPos> run = new ArrayList<>();
		if (limit <= 0 || !wet.test(start)) return run;
		Set<BlockPos> seen = new HashSet<>();
		Deque<BlockPos> open = new ArrayDeque<>();
		open.add(start.immutable());
		seen.add(start.immutable());
		while (!open.isEmpty() && run.size() < limit) {
			BlockPos cell = open.poll();
			run.add(cell);
			for (Direction side : Direction.values()) {
				BlockPos next = cell.relative(side);
				if (Math.abs(next.getX() - start.getX()) > STAIN_REACH || Math.abs(next.getY() - start.getY()) > STAIN_REACH
						|| Math.abs(next.getZ() - start.getZ()) > STAIN_REACH) continue;
				if (seen.add(next) && wet.test(next)) open.add(next);
			}
		}
		return run;
	}
}
