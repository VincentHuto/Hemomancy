package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerEscritoireShapeTest {
	@Test
	void authoredRectanglesRotateWithEveryHorizontalFacing() {
		assertShape(Direction.NORTH,
				box(-7.5, 0, -1.25, 23.25, 16, 16),
				box(-5.5, 16, 0, 21.25, 27, 16));
		assertShape(Direction.SOUTH,
				box(-7.25, 0, 0, 23.5, 16, 17.25),
				box(-5.25, 16, 0, 21.5, 27, 16));
		assertShape(Direction.EAST,
				box(0, 0, -7.5, 17.25, 16, 23.25),
				box(0, 16, -5.5, 16, 27, 21.25));
		assertShape(Direction.WEST,
				box(-1.25, 0, -7.25, 16, 16, 23.5),
				box(0, 16, -5.25, 16, 27, 21.5));
	}

	private static void assertShape(Direction facing, AABB lower, AABB upper) {
		VoxelShape shape = HarbingerEscritoireShapeRules.shape(facing);
		assertEquals(2, shape.toAabbs().size());
		assertTrue(shape.toAabbs().contains(lower), () -> facing + " missing lower " + lower);
		assertTrue(shape.toAabbs().contains(upper), () -> facing + " missing upper " + upper);
	}

	private static AABB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new AABB(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
	}
}
