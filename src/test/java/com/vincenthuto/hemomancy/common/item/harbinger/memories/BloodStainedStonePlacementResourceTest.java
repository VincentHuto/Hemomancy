package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodStainedStonePlacementResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BloodStainedStonePlacementResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String item = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodStainedStoneItem.java"));
		String blockInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String placedBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/BloodStainedStoneMarkerBlock.java"));
		String blockstate = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/blockstates/placed_blood_stained_stone.json"));
		String model = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/models/block/placed_blood_stained_stone.json"));
		String lootTable = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/loot_table/blocks/placed_blood_stained_stone.json"));

		assertContains("blood stained stone places through useOn", item, "InteractionResult useOn(UseOnContext context)");
		assertContains("placement requires a top face", item, "context.getClickedFace() != Direction.UP");
		assertContains("placement uses the hidden marker block", item,
				"BlockInit.placed_blood_stained_stone.get().defaultBlockState()");
		assertContains("placement consumes the item in survival", item, "context.getItemInHand().shrink(1)");
		assertContains("placement writes the marker block", item, "level.setBlock(placePos, state");

		assertContains("marker block is registered", blockInit,
				"placed_blood_stained_stone = SPECIALBLOCKS.register(");
		assertContains("marker block uses the expected registry id", blockInit,
				"\"placed_blood_stained_stone\"");
		assertContains("marker block has no auto block item", blockInit,
				"block == BlockInit.placed_blood_stained_stone.get()");

		assertContains("marker block is a thin flat sprite", placedBlock,
				"Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D)");
		assertContains("marker block requires ground support", placedBlock,
				"isFaceSturdy(level, supportPos, Direction.UP)");
		assertContains("marker block disappears when unsupported", placedBlock,
				"Blocks.AIR.defaultBlockState()");
		assertContains("marker block uses a normal baked model", placedBlock,
				"RenderShape.MODEL");

		assertContains("blockstate points at marker model", blockstate,
				"\"model\": \"hemomancy:block/placed_blood_stained_stone\"");
		assertContains("model uses item texture", model,
				"\"stone\": \"hemomancy:item/blood_stained_stone\"");
		assertContains("model is cutout", model, "\"render_type\": \"cutout\"");
		assertContains("model starts inset on ground", model, "\"from\": [2, 0, 2]");
		assertContains("model has sprite thickness", model, "\"to\": [14, 2, 14]");
		assertContains("marker drops the original item", lootTable,
				"\"name\": \"hemomancy:blood_stained_stone\"");
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
}
