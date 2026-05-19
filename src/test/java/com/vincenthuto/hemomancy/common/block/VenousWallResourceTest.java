package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VenousWallResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String[] WALL_IDS = {
			"venous_stone_wall",
			"polished_venous_stone_brick_wall"
	};

	private VenousWallResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String hemoWallBlock = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/shared/HemoWallBlock.java"));
		assertContains("venous walls should use tag-resilient wall behavior",
				blockInit, "new HemoWallBlock");
		assertContains("custom wall behavior should connect to wall blocks directly",
				hemoWallBlock, "block instanceof WallBlock");

		String wallTag = readResource("data/minecraft/tags/blocks/walls.json");
		for (String id : WALL_IDS) {
			assertContains(id + " should be in minecraft:walls for WallBlock neighbor connections",
					wallTag, "\"hemomancy:" + id + "\"");

			String blockstate = readResource("assets/hemomancy/blockstates/" + id + ".json");
			assertContains(id + " should use multipart wall states", blockstate, "\"multipart\"");
			assertContains(id + " should cover low sides", blockstate, "\"low\"");
			assertContains(id + " should cover tall sides", blockstate, "\"tall\"");
			assertDoesNotContain(id + " should let vanilla WallBlock waterlogging be model-agnostic",
					blockstate, "waterlogged=");
		}
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
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
