package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Where a guy stake's line runs: from the knot at the top of the stake to the top of the first tent wall ahead of it
 * (the way the stake faces), up to {@link #REACH} blocks out and a couple of columns to either side so a corner stake
 * still finds its wall. A tent block is anything in {@code hemomancy:guy_line_anchors}.
 */
public final class GuyLineRules {
	public static final int REACH = 10;
	private static final int SIDEWAYS = 2;
	private static final int RISE = 16;
	/** The rope's knot in the stake model, authored facing north: a little forward of centre, near the top. */
	private static final double KNOT_FORWARD = 0.3D, KNOT_HEIGHT = 0.82D;

	private GuyLineRules() {
	}

	/** The world point the line ties off at on the stake. */
	public static Vec3 knot(BlockPos stake, Direction facing) {
		return Vec3.atBottomCenterOf(stake).add(facing.getStepX() * KNOT_FORWARD, KNOT_HEIGHT,
				facing.getStepZ() * KNOT_FORWARD);
	}

	/**
	 * The top of the nearest wall ahead, on its face toward the stake: the highest tent block in that column, no more
	 * than {@link #RISE} above the stake. Empty when no tent stands within reach.
	 */
	public static Optional<Vec3> anchor(BlockPos stake, Direction facing, Predicate<BlockPos> isTent) {
		Direction side = facing.getClockWise();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int ahead = 1; ahead <= REACH; ahead++) {
			for (int step = 0; step <= SIDEWAYS * 2; step++) {
				int lateral = (step + 1) / 2 * (step % 2 == 0 ? -1 : 1);
				int x = stake.getX() + facing.getStepX() * ahead + side.getStepX() * lateral;
				int z = stake.getZ() + facing.getStepZ() * ahead + side.getStepZ() * lateral;
				int top = Integer.MIN_VALUE;
				for (int rise = 0; rise <= RISE; rise++)
					if (isTent.test(cursor.set(x, stake.getY() + rise, z))) top = stake.getY() + rise;
				if (top == Integer.MIN_VALUE) continue;
				return Optional.of(new Vec3(x + 0.5D - facing.getStepX() * 0.5D, top + 1.0D,
						z + 0.5D - facing.getStepZ() * 0.5D));
			}
		}
		return Optional.empty();
	}
}
