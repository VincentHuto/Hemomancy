package com.vincenthuto.hemomancy.common.block;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CraftingVesselShapeTest {
	@Test
	void ghastlyAlembicUsesModelClippedParts() throws IOException {
		String source = Files.readString(Path.of("src/main/java",
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/GhastlyAlembicBlock.java"));
		assertTrue(source.contains("AlembicGeometry.shape(StationTierProperty.stage(state), state.getValue(FACING), offset)"));
		assertTrue(source.contains("getShape") && source.contains("getCollisionShape"));
		assertTrue(source.indexOf("return partShape(state, BlockPos.ZERO);")
				!= source.lastIndexOf("return partShape(state, BlockPos.ZERO);"),
				"Selection and collision must both use the controller's clipped model part");
	}

	@Test
	void pallidRetortHasTwoBlockTallShape() throws IOException {
		assertTwoBlockTallShape("com/vincenthuto/hemomancy/common/block/unstained/crafting/PallidRetortBlock.java");
	}

	private static void assertTwoBlockTallShape(String sourcePath) throws IOException {
		String source = Files.readString(Path.of("src/main/java", sourcePath));
		assertTrue(source.contains("Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D)"));
		assertTrue(source.contains("getCollisionShape"));
	}
}
