package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;

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
			Direction.SOUTH, ModelShapes.rotate(SHAPE, Direction.SOUTH),
			Direction.EAST, ModelShapes.rotate(SHAPE, Direction.EAST),
			Direction.WEST, ModelShapes.rotate(SHAPE, Direction.WEST));
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

	private static VoxelShape box(double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ) {
		return Shapes.box(minX / 16.0D, minY / 16.0D, minZ / 16.0D,
				maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
	}
}
