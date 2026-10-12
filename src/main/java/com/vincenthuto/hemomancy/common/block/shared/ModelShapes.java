package com.vincenthuto.hemomancy.common.block.shared;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Outline and collision shapes traced from north-facing block models, in model pixels, and turned
 * the same way the blockstates turn the models (north y0, east y90, south y180, west y270).
 */
public final class ModelShapes {
	private static final double EPSILON = 1.0E-6;

	private ModelShapes() {
	}

	/** Union of pixel boxes, each {x0, y0, z0, x1, y1, z1}. */
	public static VoxelShape of(double[]... boxes) {
		VoxelShape shape = Shapes.empty();
		for (double[] b : boxes)
			shape = Shapes.or(shape, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return shape.optimize();
	}

	/** A north-facing shape turned about the block centre for each horizontal facing. */
	public static Map<Direction, VoxelShape> horizontal(VoxelShape north) {
		Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
		for (Direction facing : Direction.Plane.HORIZONTAL)
			shapes.put(facing, rotate(north, facing));
		return shapes;
	}

	public static VoxelShape rotate(VoxelShape north, Direction facing) {
		VoxelShape[] result = { Shapes.empty() };
		north.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
			for (int turn = 0; turn < turns(facing); turn++) {
				double nextX0 = 1 - z1, nextX1 = 1 - z0;
				z0 = x0;
				z1 = x1;
				x0 = nextX0;
				x1 = nextX1;
			}
			result[0] = Shapes.or(result[0], Shapes.box(x0, y0, z0, x1, y1, z1));
		});
		return result[0].optimize();
	}

	/** A multiblock's north-facing footprint: the controller plus its filler offsets. */
	public static Set<BlockPos> footprint(BlockPos... fillers) {
		Set<BlockPos> cells = new java.util.HashSet<>(Set.of(fillers));
		cells.add(BlockPos.ZERO);
		return Set.copyOf(cells);
	}

	/**
	 * Splits a multiblock's north-facing pixel boxes (relative to the controller cell) into the cell
	 * each part stands in, for every facing. Keys are world offsets from the controller.
	 */
	public static Map<Direction, Map<BlockPos, VoxelShape>> cells(double[]... boxes) {
		return cells(null, boxes);
	}

	/**
	 * As {@link #cells(double[]...)}, but parts falling in a cell outside {@code footprint} (north-facing
	 * offsets, controller included) hang off the nearest footprint cell, so a model overhanging its
	 * fillers still has an outline and collision there.
	 */
	public static Map<Direction, Map<BlockPos, VoxelShape>> cells(@Nullable Set<BlockPos> footprint, double[]... boxes) {
		Map<BlockPos, VoxelShape> north = new HashMap<>();
		for (double[] b : boxes) {
			VoxelShape box = Block.box(b[0], b[1], b[2], b[3], b[4], b[5]);
			for (int x = cell(b[0] / 16); x < b[3] / 16 - EPSILON; x++)
				for (int y = cell(b[1] / 16); y < b[4] / 16 - EPSILON; y++)
					for (int z = cell(b[2] / 16); z < b[5] / 16 - EPSILON; z++) {
						BlockPos cell = new BlockPos(x, y, z);
						VoxelShape clipped = Shapes.join(box.move(-x, -y, -z), Shapes.block(), BooleanOp.AND);
						BlockPos owner = footprint == null || footprint.contains(cell) ? cell : neighbour(footprint, cell);
						if (!clipped.isEmpty() && owner != null)
							north.merge(owner, clipped.move(x - owner.getX(), y - owner.getY(), z - owner.getZ()), Shapes::or);
					}
		}
		Map<Direction, Map<BlockPos, VoxelShape>> result = new EnumMap<>(Direction.class);
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			Map<BlockPos, VoxelShape> cells = new HashMap<>();
			north.forEach((offset, shape) -> {
				BlockPos turned = offset;
				for (int turn = 0; turn < turns(facing); turn++)
					turned = new BlockPos(-turned.getZ(), turned.getY(), turned.getX());
				cells.put(turned, rotate(shape, facing));
			});
			result.put(facing, Map.copyOf(cells));
		}
		return result;
	}

	/** The nearest footprint cell, preferring the same layer; parts more than two cells away are dropped. */
	@Nullable
	private static BlockPos neighbour(Set<BlockPos> footprint, BlockPos cell) {
		BlockPos best = null;
		int bestCost = 3;
		for (BlockPos candidate : footprint) {
			int cost = Math.abs(candidate.getX() - cell.getX()) + Math.abs(candidate.getZ() - cell.getZ())
					+ 2 * Math.abs(candidate.getY() - cell.getY());
			if (cost < bestCost || cost == bestCost && best != null && candidate.compareTo(best) < 0) {
				best = candidate;
				bestCost = cost;
			}
		}
		return best;
	}

	private static int cell(double v) {
		return (int) Math.floor(v + EPSILON);
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
