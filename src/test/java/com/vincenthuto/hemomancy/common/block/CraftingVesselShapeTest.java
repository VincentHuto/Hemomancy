package com.vincenthuto.hemomancy.common.block;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class CraftingVesselShapeTest {
	@Test
	void ghastlyAlembicHasTwoBlockTallShape() throws IOException {
		assertTwoBlockTallShape("com/vincenthuto/hemomancy/common/block/harbinger/crafting/GhastlyAlembicBlock.java");
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
