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
		assertContains("gourd model has compact cocoa-pod body", blockModel, "\"from\": [3, 0, 4]");
		assertContains("gourd model body stays low and pod-sized", blockModel, "\"to\": [13, 7, 12]");
		assertContains("gourd model has a small stem nub", blockModel, "\"from\": [7, 7, 6]");
		assertContains("gourd model reaches the west attached-stem edge", blockModel, "\"from\": [0, 2, 6]");
		assertContains("gourd model reaches the east attached-stem edge", blockModel, "\"from\": [13, 2, 6]");
		assertContains("gourd model reaches the north attached-stem edge", blockModel, "\"from\": [6, 2, 0]");
		assertContains("gourd model reaches the south attached-stem edge", blockModel, "\"from\": [6, 2, 12]");

		assertContains("gourd block exposes compact selection/collision shape", gourdBlock,
				"private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 4.0D, 13.0D, 7.0D, 12.0D);");
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
