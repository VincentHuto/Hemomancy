package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MnemonicBlueprintPlacementTest {
	private static final MnemonicBlueprintPlacement.Bounds BOUNDS =
			new MnemonicBlueprintPlacement.Bounds(0, 2, 0, 4);
	private static final BlockPos CENTER = new BlockPos(10, 64, 20);

	@Test
	void canonicalNorthCentersThePatternFootprint() {
		assertEquals(new BlockPos(9, 64, 18),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(0, 0, 0), BOUNDS, Direction.NORTH));
		assertEquals(new BlockPos(11, 65, 22),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(2, 1, 4), BOUNDS, Direction.NORTH));
	}

	@Test
	void eastFacingRotatesClockwiseAroundTheCenteredFootprint() {
		assertEquals(new BlockPos(12, 64, 19),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(0, 0, 0), BOUNDS, Direction.EAST));
		assertEquals(new BlockPos(8, 65, 21),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(2, 1, 4), BOUNDS, Direction.EAST));
	}

	@Test
	void southAndWestFacingCompleteTheFourCardinalRotations() {
		assertEquals(new BlockPos(11, 64, 22),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(0, 0, 0), BOUNDS, Direction.SOUTH));
		assertEquals(new BlockPos(8, 64, 21),
				MnemonicBlueprintPlacement.worldPosition(CENTER, new BlockPos(0, 0, 0), BOUNDS, Direction.WEST));
	}
}
