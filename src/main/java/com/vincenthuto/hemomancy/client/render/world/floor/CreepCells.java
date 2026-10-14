package com.vincenthuto.hemomancy.client.render.world.floor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Where the world stage's creep can lie around one floor: grounded cells beyond the trench, each at its own ground
 * height. The ground is re-read every {@link #RECHECK_TICKS}, so the creep follows blocks placed or broken during
 * the rite and chunks that load after the rite was first seen.
 */
public final class CreepCells {
	public static final int NO_GROUND = Integer.MIN_VALUE;
	/** How often the ground under the creep is read again. */
	public static final int RECHECK_TICKS = 40;

	/** Whether the block at this offset from the Focus, {@code dy} levels up or down, can carry the creep. */
	@FunctionalInterface
	public interface Ground {
		boolean carries(int x, int dy, int z);
	}

	/** Whether the block at this offset from the Focus lies beyond the floor's trench line. */
	@FunctionalInterface
	public interface Outside {
		boolean beyond(int x, int z);
	}

	/** The trench-distance encoding a ground-layer vertex carries at this offset. */
	@FunctionalInterface
	public interface Blue {
		float at(float x, float z);
	}

	private final int half;
	private final int margin;
	private final int drop;
	private List<float[]> cells;
	private long checkedAt;

	public CreepCells(int half, int margin, int drop) {
		this.half = half;
		this.margin = margin;
		this.drop = drop;
	}

	/**
	 * The cells as {x, z, radius, blue at the four corners, surface y}, out to the creep's furthest reach. They are
	 * read again from the ground when {@link #RECHECK_TICKS} have passed, or when time runs backwards.
	 */
	public List<float[]> cells(long tick, Ground ground, Outside outside, Blue blue) {
		if (cells == null || tick < checkedAt || tick - checkedAt >= RECHECK_TICKS) {
			cells = build(ground, outside, blue);
			checkedAt = tick;
		}
		return cells;
	}

	private List<float[]> build(Ground ground, Outside outside, Blue blue) {
		List<float[]> found = new ArrayList<>();
		float limit = half + 0.5F + margin;
		int reach = (int) Math.ceil(limit);
		for (int x = -reach; x <= reach; x++) {
			for (int z = -reach; z <= reach; z++) {
				float radius = (float) Math.hypot(x + 0.5F, z + 0.5F);
				if (radius > limit || !outside.beyond(x, z)) continue;
				int cellX = x;
				int cellZ = z;
				int level = groundOffset(dy -> ground.carries(cellX, dy, cellZ), drop);
				if (level == NO_GROUND) continue;
				found.add(new float[] {x, z, radius, blue.at(x, z), blue.at(x, z + 1), blue.at(x + 1, z + 1),
						blue.at(x + 1, z), level + FloorMesh.SURFACE_Y});
			}
		}
		return found;
	}

	/**
	 * The nearest level to the Focus's own whose block can carry the creep, searching 0, +1, -1, +2, -2 ... out to
	 * {@code drop}, so the membrane follows gentle terrain but stops at cliffs and caves.
	 */
	public static int groundOffset(IntPredicate carries, int drop) {
		if (carries.test(0)) return 0;
		for (int step = 1; step <= drop; step++) {
			if (carries.test(step)) return step;
			if (carries.test(-step)) return -step;
		}
		return NO_GROUND;
	}

	/** An upper bound on the creep cells around a floor of this half-width: the disc out to its furthest reach. */
	public static int maxCells(int half, int margin) {
		double limit = half + 0.5D + margin + 1.0D;
		return (int) Math.ceil(Math.PI * limit * limit);
	}
}
