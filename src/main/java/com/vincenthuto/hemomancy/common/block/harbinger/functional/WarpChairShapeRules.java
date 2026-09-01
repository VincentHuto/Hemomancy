package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.stream.Stream;

public final class WarpChairShapeRules {
	private static final VoxelShape SHAPE = Stream.of(
			box(1, 0, 2, 4, 7, 5),
			box(12, 0, 2, 15, 7, 5),
			box(12, 0, 11, 15, 7, 14),
			box(1, 0, 11, 4, 7, 14),
			box(1, 7, 1, 15, 10, 15),
			box(12.25, 10, 1, 14.75, 15, 15),
			box(1.25, 10, 1, 3.75, 15, 15),
			box(1, 10, 11.75, 15, 32, 15)
	).reduce((first, second) -> Shapes.join(first, second, BooleanOp.OR)).orElseThrow();

	private static final Map<Direction, VoxelShape> ROTATED_SHAPES = Map.of(
			Direction.NORTH, SHAPE,
			Direction.SOUTH, rotate(SHAPE, Direction.SOUTH),
			Direction.EAST, rotate(SHAPE, Direction.EAST),
			Direction.WEST, rotate(SHAPE, Direction.WEST));
	private static final Map<Direction, VoxelShape> UPPER_SHAPES = Map.of(
			Direction.NORTH, upperSlice(Direction.NORTH),
			Direction.SOUTH, upperSlice(Direction.SOUTH),
			Direction.EAST, upperSlice(Direction.EAST),
			Direction.WEST, upperSlice(Direction.WEST));

	private WarpChairShapeRules() {
	}

	public static VoxelShape shape(Direction facing) {
		return ROTATED_SHAPES.getOrDefault(facing, SHAPE);
	}

	public static VoxelShape upperShape(Direction facing) {
		return UPPER_SHAPES.getOrDefault(facing, UPPER_SHAPES.get(Direction.NORTH));
	}

	private static VoxelShape upperSlice(Direction facing) {
		return Shapes.join(shape(facing).move(0.0D, -1.0D, 0.0D), Shapes.block(), BooleanOp.AND);
	}

	private static VoxelShape rotate(VoxelShape source, Direction facing) {
		VoxelShape[] result = { Shapes.empty() };
		source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
			VoxelShape box = switch (facing) {
				case SOUTH -> Shapes.box(1.0D - maxX, minY, 1.0D - maxZ,
						1.0D - minX, maxY, 1.0D - minZ);
				case EAST -> Shapes.box(1.0D - maxZ, minY, minX,
						1.0D - minZ, maxY, maxX);
				case WEST -> Shapes.box(minZ, minY, 1.0D - maxX,
						maxZ, maxY, 1.0D - minX);
				default -> Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
			};
			result[0] = Shapes.or(result[0], box);
		});
		return result[0].optimize();
	}

	private static VoxelShape box(double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ) {
		return Shapes.box(minX / 16.0D, minY / 16.0D, minZ / 16.0D,
				maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
	}
}
