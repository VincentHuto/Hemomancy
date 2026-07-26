package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GourdPresentationResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private GourdPresentationResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/gourd.json"));
		String gourdBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/plant/GourdBlock.java"));
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));

		assertDoesNotContain("gourd model should not inherit a full cube column", blockModel,
				"\"parent\": \"minecraft:block/cube_column\"");
		assertContains("gourd model has explicit element geometry", blockModel, "\"elements\"");
		assertContains("gourd model has the lower pod body", blockModel, "\"from\": [4, 0, 4]");
		assertContains("gourd model has the upper pod body", blockModel, "\"from\": [4, 7, 4]");
		assertContains("gourd model reaches its full ripe height", blockModel, "\"to\": [8.5, 14, 9.5]");
		assertContains("gourd model has a narrow stem nub", blockModel, "\"from\": [7.5, 13, 6.5]");

		assertContains("gourd block selection/collision encloses the current tall pod model", gourdBlock,
				"private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 3.0D, 12.0D, 14.0D, 13.0D);");
		assertContains("gourd block overrides getShape", gourdBlock, "protected VoxelShape getShape(");
		assertContains("gourd block overrides getCollisionShape", gourdBlock,
				"protected VoxelShape getCollisionShape(");
		assertContains("gourd stem renders as cutout", clientEvents,
				"ItemBlockRenderTypes.setRenderLayer(BlockInit.gourd_stem.get(), RenderType.cutout());");
		assertContains("attached gourd stem renders as cutout", clientEvents,
				"ItemBlockRenderTypes.setRenderLayer(BlockInit.attached_gourd_stem.get(), RenderType.cutout());");
		assertContains("attached gourd stem restores the custom age-7 stem when fruit is removed", blockInit,
				"new AttachedStemBlock(GOURD_STEM_BLOCK_KEY, GOURD_BLOCK_KEY, GOURD_SEED_ITEM_KEY");
	}

	private static String read(Path path) throws IOException {
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
