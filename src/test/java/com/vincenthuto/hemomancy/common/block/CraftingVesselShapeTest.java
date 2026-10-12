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
	void pallidRetortTracesItsModelAcrossBothCells() throws IOException {
		String source = Files.readString(Path.of("src/main/java",
				"com/vincenthuto/hemomancy/common/block/unstained/crafting/PallidRetortBlock.java"));
		assertTrue(source.contains("ModelShapes.cells(") && source.contains("ModelShapes.footprint(FILLER_OFFSETS)"),
				"The retort's shape must be split over its controller and filler cells");
		assertTrue(source.contains("public VoxelShape fillerShape(BlockState controller, BlockPos offset)"),
				"The filler above the retort must take its traced part instead of a full cube");
		assertTrue(!source.contains("Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D)"),
				"The two-block slab no longer fits the stepped retort model");
	}
}
