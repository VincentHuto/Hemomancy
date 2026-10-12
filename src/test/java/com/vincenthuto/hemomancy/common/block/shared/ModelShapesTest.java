package com.vincenthuto.hemomancy.common.block.shared;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ModelShapesTest {
	@Test
	void northShapeTurnsLikeTheBlockstateRotations() {
		VoxelShape north = ModelShapes.of(new double[] { 2, 0, -3, 6, 8, 4 });
		Map<Direction, VoxelShape> turned = ModelShapes.horizontal(north);
		assertEquals(box(2, 0, -3, 6, 8, 4), turned.get(Direction.NORTH).bounds());
		assertEquals(box(12, 0, 2, 19, 8, 6), turned.get(Direction.EAST).bounds());
		assertEquals(box(10, 0, 12, 14, 8, 19), turned.get(Direction.SOUTH).bounds());
		assertEquals(box(-3, 0, 10, 4, 8, 14), turned.get(Direction.WEST).bounds());
	}

	@Test
	void multiblockBoxesSplitIntoTheCellTheyStandIn() {
		Map<Direction, Map<BlockPos, VoxelShape>> cells = ModelShapes.cells(new double[] { -8, 0, 2, 16, 20, 14 });
		Map<BlockPos, VoxelShape> north = cells.get(Direction.NORTH);
		assertEquals(4, north.size());
		assertEquals(box(8, 0, 2, 16, 16, 14), north.get(new BlockPos(-1, 0, 0)).bounds());
		assertEquals(box(0, 0, 2, 16, 4, 14), north.get(new BlockPos(0, 1, 0)).bounds());
		Map<BlockPos, VoxelShape> east = cells.get(Direction.EAST);
		assertEquals(box(2, 0, 8, 14, 16, 16), east.get(new BlockPos(0, 0, -1)).bounds());
	}

	@Test
	void overhangOutsideTheFootprintHangsOffTheNeighbouringCell() {
		Map<Direction, Map<BlockPos, VoxelShape>> cells = ModelShapes.cells(Set.of(BlockPos.ZERO, new BlockPos(0, 1, 0)),
				new double[] { -4, 0, 2, 20, 24, 14 });
		Map<BlockPos, VoxelShape> north = cells.get(Direction.NORTH);
		assertEquals(Set.of(BlockPos.ZERO, new BlockPos(0, 1, 0)), north.keySet());
		assertEquals(box(-4, 0, 2, 20, 16, 14), north.get(BlockPos.ZERO).bounds());
		assertEquals(box(-4, 0, 2, 20, 8, 14), north.get(new BlockPos(0, 1, 0)).bounds());
		assertEquals(box(2, 0, -4, 14, 16, 20), cells.get(Direction.EAST).get(BlockPos.ZERO).bounds());
	}

	private static AABB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new AABB(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
	}
}
