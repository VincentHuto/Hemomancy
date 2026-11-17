package com.vincenthuto.hemomancy.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Settings shared by the GameTest isolation mixins, which keep each test's chunks ticking and its neighbours out of
 * reach. See docs/TESTING.md, "Deterministic GameTests".
 */
public final class GameTestIsolation {
	/** Blocks of open ground between two test structures, in x and in z. Vanilla leaves 5 and 6. */
	public static final int GRID_GAP = 64;
	/** Chunks around each structure that are forced and must be entity-ticking before its test starts. */
	public static final int MARGIN_CHUNKS = Integer.getInteger("hemomancy.gametest.marginChunks", 2);
	/**
	 * How far from 0 the random corner may fall, in x and in z. Vanilla picks up to 15,000,000; past about 8,000,000 a
	 * float holds a block coordinate only to the nearest block, and floats in aim and geometry code then miss.
	 */
	public static final int ORIGIN_RANGE = 1_000_000;

	private GameTestIsolation() {
	}

	/**
	 * The run's first structure corner. Vanilla picks a random block; this keeps the random place (the test world is
	 * reused between runs, so a fixed place would meet the last run's leftovers) but snaps it to a chunk corner, so
	 * every run lays its tests out on the same chunk alignment, and folds it into {@link #ORIGIN_RANGE}.
	 * {@code -Dhemomancy.gametest.origin=x,z} sets the corner exactly, unsnapped, to try a different alignment.
	 */
	public static BlockPos origin(BlockPos random) {
		String fixed = System.getProperty("hemomancy.gametest.origin", "").trim();
		if (!fixed.isEmpty()) {
			String[] parts = fixed.split(",");
			return new BlockPos(Integer.parseInt(parts[0].trim()), random.getY(), Integer.parseInt(parts[1].trim()));
		}
		return new BlockPos(near(random.getX()) & ~15, random.getY(), near(random.getZ()) & ~15);
	}

	private static int near(int coordinate) {
		return Math.floorMod(coordinate, 2 * ORIGIN_RANGE) - ORIGIN_RANGE;
	}

	public static BoundingBox withMargin(BoundingBox box) {
		int blocks = MARGIN_CHUNKS * 16;
		return box.inflatedBy(blocks, 0, blocks);
	}
}
