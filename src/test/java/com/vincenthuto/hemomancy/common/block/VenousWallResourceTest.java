package com.vincenthuto.hemomancy.common.block;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class VenousWallResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String[] WALL_IDS = {
			"venous_stone_wall",
			"polished_venous_stone_brick_wall",
			"bone_brick_wall",
			"hematic_iron_plating_wall",
			"polished_venous_stone_wall"
	};

	private VenousWallResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String hemoWallBlock = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/shared/HemoWallBlock.java"));
		assertContains("venous walls should use tag-resilient wall behavior",
				blockInit, "new HemoWallBlock");
		assertContains("custom wall behavior should connect to wall blocks directly",
				hemoWallBlock, "block instanceof WallBlock");

		String wallTag = readResource("data/minecraft/tags/block/walls.json");
		for (String id : WALL_IDS) {
			assertContains(id + " should be in minecraft:walls for WallBlock neighbor connections",
					wallTag, "\"hemomancy:" + id + "\"");

			String blockstate = readResource("assets/hemomancy/blockstates/" + id + ".json");
			assertContains(id + " should use multipart wall states", blockstate, "\"multipart\"");
			assertContains(id + " should cover low sides", blockstate, "\"low\"");
			assertContains(id + " should cover tall sides", blockstate, "\"tall\"");
			assertNotContains(id + " should let vanilla WallBlock waterlogging be model-agnostic",
					blockstate, "waterlogged=");
		}
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
	}
}
