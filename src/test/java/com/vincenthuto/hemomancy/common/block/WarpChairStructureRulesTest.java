package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairShapeRules;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairStructureRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WarpChairStructureRulesTest {
	@Test
	void fillerAlwaysOccupiesTheBlockDirectlyAbove() {
		BlockPos lower = new BlockPos(7, 40, -3);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			assertEquals(lower.above(), WarpChairStructureRules.fillerPos(lower, direction));
		}
	}

	@Test
	void pairedChairFacesAwayFromTheArbor() {
		BlockPos arbor = BlockPos.ZERO;
		assertEquals(new BlockPos(0, 1, -1), WarpChairStructureRules.pairedChairPos(arbor));
		assertEquals(new BlockPos(0, 1, -2), WarpChairStructureRules.legacyPairedChairPos(arbor));
		assertEquals(Direction.SOUTH,
				WarpChairStructureRules.facingAwayFrom(arbor, new BlockPos(0, 0, 2)));
		assertEquals(Direction.WEST,
				WarpChairStructureRules.facingAwayFrom(arbor, new BlockPos(-2, 0, 0)));
	}

	@Test
	void tallBackrestRotatesWithThePlacedFacing() {
		assertTallBackrest(WarpChairShapeRules.shape(Direction.NORTH), 1, 11.75, 15, 15);
		assertTallBackrest(WarpChairShapeRules.shape(Direction.SOUTH), 1, 1, 15, 4.25);
		assertTallBackrest(WarpChairShapeRules.shape(Direction.EAST), 1, 1, 4.25, 15);
		assertTallBackrest(WarpChairShapeRules.shape(Direction.WEST), 11.75, 1, 15, 15);
	}

	private static void assertTallBackrest(VoxelShape shape, double minX, double minZ,
			double maxX, double maxZ) {
		AABB actual = shape.toAabbs().stream()
				.filter(box -> box.maxY > 1.5D)
				.reduce((first, second) -> new AABB(
						Math.min(first.minX, second.minX), Math.min(first.minY, second.minY),
						Math.min(first.minZ, second.minZ), Math.max(first.maxX, second.maxX),
						Math.max(first.maxY, second.maxY), Math.max(first.maxZ, second.maxZ)))
				.orElseThrow(() -> new AssertionError("chair has no two-block-tall backrest"));
		assertEquals(minX / 16.0D, actual.minX, 1.0E-7D);
		assertEquals(minZ / 16.0D, actual.minZ, 1.0E-7D);
		assertEquals(maxX / 16.0D, actual.maxX, 1.0E-7D);
		assertEquals(maxZ / 16.0D, actual.maxZ, 1.0E-7D);
		assertEquals(2.0D, actual.maxY, 1.0E-7D);
	}
}
