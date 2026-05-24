package com.vincenthuto.hemomancy.common.entity.mob.animal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VerdigrisMothResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private VerdigrisMothResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entityInit = readSource("com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String entity = readSource("com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java");
		String itemInit = readSource("com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String layerEvents = readSource("com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		String spawnModifier = readResource("data/hemomancy/neoforge/biome_modifier/add_verdigris_moth.json");
		String spawnTag = readResource("data/hemomancy/tags/worldgen/biome/verdigris_moth_spawnlist.json");
		String lootTable = readResource("data/hemomancy/loot_table/entities/verdigris_moth.json");
		String lang = readResource("assets/hemomancy/lang/en_us.json");

		assertContains("entity registry holder", entityInit, "verdigris_moth = ENTITY_TYPES.register(");
		assertContains("entity registry id", entityInit, "\"verdigris_moth\"");
		assertContains("spawn placement", entityInit, "VerdigrisMothEntity::canSpawnHere");
		assertContains("attribute registration", entityInit, "VerdigrisMothEntity.setAttributes().build()");
		assertContains("night-adjusted spawn brightness", entity, "level.getMaxLocalRawBrightness(pos)");
		assertDoesNotContain("raw sky brightness blocks open night spawns", entity, "level.getRawBrightness(pos, 0)");
		assertContains("spawn egg", itemInit, "spawn_egg_verdigris_moth");
		assertContains("renderer registration", clientEvents, "VerdigrisMothRenderer::new");
		assertContains("model layer registration", layerEvents, "VerdigrisMothModel.LAYER_LOCATION");

		assertContains("spawn modifier type", spawnModifier, "\"type\": \"neoforge:add_spawns\"");
		assertContains("spawn modifier entity", spawnModifier, "\"type\": \"hemomancy:verdigris_moth\"");
		assertContains("forest spawn biome", spawnTag, "\"minecraft:forest\"");
		assertContains("dark forest spawn biome", spawnTag, "\"minecraft:dark_forest\"");
		assertContains("cold spawn biome", spawnTag, "\"minecraft:snowy_taiga\"");
		assertContains("mountain cold spawn biome", spawnTag, "\"minecraft:grove\"");

		assertContains("empty entity loot table", lootTable, "\"pools\": []");
		assertDoesNotContain("death loot should not be a good Virid Salis source", lootTable,
				"hemomancy:virid_salis_trail");
		assertContains("entity translation", lang, "\"entity.hemomancy.verdigris_moth\": \"Verdigris Moth\"");
		assertContains("spawn egg translation", lang,
				"\"item.hemomancy.spawn_egg_verdigris_moth\": \"Verdigris Moth Spawn Egg\"");
		assertExists("verdigris moth texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/entity/verdigris_moth/model_verdigris_moth.png"));
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
