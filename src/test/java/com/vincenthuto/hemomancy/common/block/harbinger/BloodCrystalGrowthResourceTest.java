package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodCrystalGrowthResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BloodCrystalGrowthResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockInit = readSource("com/vincenthuto/hemomancy/common/init/BlockInit.java");
		String crystalBlock = readSource("com/vincenthuto/hemomancy/common/block/harbinger/BloodCrystalBlock.java");
		String alembic = readSource("com/vincenthuto/hemomancy/common/tile/crafting/GhastlyAlembicBlockEntity.java");
		String crystalBlockstate = readResource("assets/hemomancy/blockstates/blood_crystal.json");
		String crystalLoot = readResource("data/hemomancy/loot_table/blocks/blood_crystal.json");

		assertDoesNotContain("blood crystal bud block should no longer be registered", blockInit,
				"blood_crystal_bud");
		assertDoesNotExist("blood crystal bud source should be removed",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/harbinger/BloodCrystalBudBlock.java"));
		assertDoesNotExist("blood crystal bud blockstate should be removed",
				RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/blood_crystal_bud.json"));
		assertDoesNotExist("blood crystal bud item model should be removed",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/blood_crystal_bud.json"));
		assertDoesNotExist("blood crystal bud loot table should be removed",
				RESOURCE_ROOT.resolve("data/hemomancy/loot_table/blocks/blood_crystal_bud.json"));

		assertContains("blood crystal should own age state", crystalBlock,
				"public static final IntegerProperty AGE = BlockStateProperties.AGE_3;");
		assertContains("blood crystal should expose max age", crystalBlock, "public static final int MAX_AGE = 3;");
		assertContains("blood crystal should register age in its state definition", crystalBlock,
				"pBuilder.add(FACING, AGE, WATERLOGGED);");
		assertContains("blood crystal should support alembic growth", crystalBlock,
				"public boolean tryGrow(Level level, BlockPos pos, BlockState state, RandomSource rand)");

		assertContains("alembic leak should place blood crystals", alembic, "BlockInit.blood_crystal.get()");
		assertDoesNotContain("alembic leak should not reference bud block", alembic, "blood_crystal_bud");
		assertDoesNotContain("alembic leak should not import bud block", alembic, "BloodCrystalBudBlock");

		for (int age = 0; age <= 3; age++) {
			assertContains("blood crystal blockstate should include age " + age, crystalBlockstate,
					"\"age\": \"" + age + "\"");
			assertContains("blood crystal blockstate should use staged blood crystal model " + age, crystalBlockstate,
					"\"model\": \"hemomancy:block/blood_crystal_stage" + age + "\"");
			assertExists("blood crystal stage model " + age,
					RESOURCE_ROOT.resolve("assets/hemomancy/models/block/blood_crystal_stage" + age + ".json"));
		}

		assertContains("blood crystal loot should condition immature shard drops on age 2", crystalLoot,
				"\"age\": \"2\"");
		assertContains("blood crystal loot should condition mature shard drops on age 3", crystalLoot,
				"\"age\": \"3\"");
		assertDoesNotContain("blood crystal loot should not mention bud ids", crystalLoot,
				"blood_crystal_bud");
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
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

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertDoesNotExist(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": found " + path);
		}
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
