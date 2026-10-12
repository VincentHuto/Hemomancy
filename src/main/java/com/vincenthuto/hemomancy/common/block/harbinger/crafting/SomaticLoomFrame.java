package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Model pixels of the north-facing loom to world positions, turned the way the blockstate turns the model. */
public final class SomaticLoomFrame {
	private SomaticLoomFrame() {
	}

	public static Vec3 toWorld(BlockPos pos, Direction facing, double x, double y, double z) {
		for (int turn = 0; turn < turns(facing); turn++) {
			double nextX = 16 - z;
			z = x;
			x = nextX;
		}
		return new Vec3(pos.getX() + x / 16, pos.getY() + y / 16, pos.getZ() + z / 16);
	}

	private static int turns(Direction facing) {
		return switch (facing) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
	}
}
