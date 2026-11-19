package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

class FinaleStageMappingTest {
	private static final BlockPos REAL = new BlockPos(-120, 70, 340), COPY = new BlockPos(8192, 2, 512);

	@Test
	void aPointMapsByTheSameOffsetUnderEveryRotation() {
		Vec3 offset = new Vec3(3.25D, 1.0D, -2.5D);
		for (Rotation rotation : Rotation.values()) {
			Vec3 copy = FinaleStage.toCopy(Vec3.atLowerCornerOf(REAL).add(offset), REAL, COPY, rotation);
			assertEquals(offset, copy.subtract(Vec3.atLowerCornerOf(COPY)), rotation.name());
		}
		assertEquals(37.5F, FinaleStage.yawToCopy(37.5F), 1.0E-6F);
	}

	@Test
	void perchesTurnWithThePavilion() {
		BlockPos cell = FinaleStage.HIGH_PERCHES.getFirst();
		BlockPos relative = cell.subtract(FinaleStage.CAROUSEL_CELL);
		for (Rotation rotation : Rotation.values()) {
			Vec3 perch = FinaleStage.perch(COPY, rotation, cell);
			assertEquals(Vec3.atBottomCenterOf(COPY.offset(relative.rotate(rotation))), perch, rotation.name());
		}
	}

	@Test
	void thereAreSixHighPerchesAndFourLowOnesInsideTheTemplate() {
		assertEquals(6, FinaleStage.HIGH_PERCHES.size());
		assertEquals(4, FinaleStage.LOW_PERCHES.size());
		for (BlockPos cell : FinaleStage.HIGH_PERCHES)
			assertTrue(inside(cell), cell.toString());
		for (BlockPos cell : FinaleStage.LOW_PERCHES) {
			assertTrue(inside(cell), cell.toString());
			assertTrue(cell.getY() <= 3, "a low perch is within reach of the ring's floor: " + cell);
		}
	}

	private static boolean inside(BlockPos cell) {
		return cell.getX() >= 0 && cell.getX() < 81 && cell.getY() >= 0 && cell.getY() < 23 && cell.getZ() >= 0
				&& cell.getZ() < 65;
	}
}
