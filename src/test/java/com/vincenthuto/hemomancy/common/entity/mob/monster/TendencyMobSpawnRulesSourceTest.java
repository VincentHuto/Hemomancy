package com.vincenthuto.hemomancy.common.entity.mob.monster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TendencyMobSpawnRulesSourceTest {
	private static final String[] DORMANT_ENTITY_IDS = {
			"cruor_fiend",
			"void_drinker",
			"frozen_clot",
			"abyssal_siphon",
			"synapse_hound",
			"myelin_borer"
	};
	private static final String[] ACTIVE_DESICCANT_SNIPPETS = {
			" desiccant = ENTITY_TYPES.register(",
			"\"desiccant\"",
			"DesiccantEntity::canSpawnHere",
			"event.put(EntityInit.desiccant.get(), DesiccantEntity.setAttributes().build())"
	};
	private static final String[] DORMANT_DROP_IDS = {
			"desiccated_membrane",
			"molten_scab",
			"void_ichor",
			"frozen_clot",
			"abyssal_ichor",
			"nerve_bundle"
	};

	private TendencyMobSpawnRulesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertDesiccantActive();
		String entityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		for (String id : DORMANT_ENTITY_IDS) {
			assertDoesNotContain("dormant mob should not be actively registered: " + id,
					stripLineComments(entityInit), "register(\"" + id + "\"");
			assertPathMissing("dormant mob should not have a biome modifier: " + id,
					Path.of("src/main/resources/data/hemomancy/neoforge/biome_modifier/add_" + id + ".json"));
			assertPathMissing("dormant mob should not have an entity loot table: " + id,
					Path.of("src/main/resources/data/hemomancy/loot_table/entities/" + id + ".json"));
		}
		for (String id : DORMANT_DROP_IDS) {
			assertDoesNotContain("dormant drop should not be actively registered: " + id,
					stripLineComments(itemInit), "register(\"" + id + "\"");
			assertDoesNotContain("recipes should not consume dormant drop: " + id,
					readTree(Path.of("src/main/resources/data/hemomancy/recipe")), "hemomancy:" + id);
		}
	}

	private static void assertDesiccantActive() throws IOException {
		String entityInit = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String itemInit = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String clientEvents = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String layerEvents = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		for (String snippet : ACTIVE_DESICCANT_SNIPPETS) {
			assertContains("active desiccant source " + snippet, entityInit, snippet);
		}
		assertContains("desiccant spawn egg holder", itemInit, " spawn_egg_desiccant = SPAWNEGGS.register(");
		assertContains("desiccant spawn egg id", itemInit, "\"spawn_egg_desiccant\"");
		assertContains("desiccant renderer", clientEvents, "EntityInit.desiccant.get(), DesiccantRenderer::new");
		assertContains("desiccant model layer", layerEvents, "DesiccantModel.LAYER_LOCATION");
		assertPathExists("desiccant biome modifier",
				Path.of("src/main/resources/data/hemomancy/neoforge/biome_modifier/add_desiccant.json"));
		assertPathExists("desiccant biome tag",
				Path.of("src/main/resources/data/hemomancy/tags/worldgen/biome/desiccant_spawnlist.json"));
		assertPathExists("desiccant loot table",
				Path.of("src/main/resources/data/hemomancy/loot_table/entities/desiccant.json"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	private static String readTree(Path root) throws IOException {
		StringBuilder content = new StringBuilder();
		try (var paths = Files.walk(root)) {
			for (Path path : paths.filter(Files::isRegularFile).toList()) {
				content.append(Files.readString(path).replace("\r\n", "\n")).append('\n');
			}
		}
		return content.toString();
	}

	private static String stripLineComments(String text) {
		StringBuilder stripped = new StringBuilder();
		for (String line : text.split("\n")) {
			String trimmed = line.stripLeading();
			if (!trimmed.startsWith("//")) {
				stripped.append(line).append('\n');
			}
		}
		return stripped.toString();
	}

	private static void assertPathMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists at " + path);
		}
	}

	private static void assertPathExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
