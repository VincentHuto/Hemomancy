package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HarbingerEscritoireShapeRules {
	private static final VoxelShape NORTH = join(
			box(-7.5, 0, -1.25, 23.25, 16, 16), box(-5.5, 16, 0, 21.25, 27, 16));
	private static final VoxelShape SOUTH = join(
			box(-7.25, 0, 0, 23.5, 16, 17.25), box(-5.25, 16, 0, 21.5, 27, 16));
	private static final VoxelShape EAST = join(
			box(0, 0, -7.5, 17.25, 16, 23.25), box(0, 16, -5.5, 16, 27, 21.25));
	private static final VoxelShape WEST = join(
			box(-1.25, 0, -7.25, 16, 16, 23.5), box(0, 16, -5.25, 16, 27, 21.5));

	private HarbingerEscritoireShapeRules() {
	}

	public static VoxelShape shape(Direction facing) {
		return switch (facing) {
			case SOUTH -> SOUTH;
			case EAST -> EAST;
			case WEST -> WEST;
			default -> NORTH;
		};
	}

	private static VoxelShape box(double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ) {
		return Shapes.box(minX / 16.0, minY / 16.0, minZ / 16.0,
				maxX / 16.0, maxY / 16.0, maxZ / 16.0);
	}

	private static VoxelShape join(VoxelShape first, VoxelShape second) {
		return Shapes.or(first, second);
	}
}
