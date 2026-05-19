package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicIronDoorWaterloggingTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HematicIronDoorWaterloggingTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String waterloggedDoor = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/shared/WaterloggedDoorBlock.java"));
		assertContains("hematic iron door should use explicit waterlogged door behavior",
				blockInit, "new WaterloggedDoorBlock");
		assertContains("hematic iron door should be registered",
				blockInit, "hematic_iron_door");
		assertContains("waterlogged door should expose WATERLOGGED",
				waterloggedDoor, "BlockStateProperties.WATERLOGGED");
		assertContains("waterlogged door should support bucket waterlogging",
				waterloggedDoor, "implements SimpleWaterloggedBlock");
		assertContains("waterlogged door should preserve upper-half water independently",
				waterloggedDoor, "pos.above()");

		String blockstate = read(RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/hematic_iron_door.json"));
		assertContains("hematic iron door blockstate should ignore waterlogged states",
				blockstate, "\"multipart\"");
		assertDoesNotContain("hematic iron door blockstate should not enumerate waterlogged variants",
				blockstate, "waterlogged=");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
