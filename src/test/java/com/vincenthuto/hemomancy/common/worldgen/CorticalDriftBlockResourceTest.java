package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class CorticalDriftBlockResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");
	private static final Map<String, String> BLOCKS = Map.ofEntries(
			Map.entry("neural_tissue", "Neural Tissue"),
			Map.entry("axonal_slate", "Axonal Slate"),
			Map.entry("myelin_sheath", "Myelin Sheath"),
			Map.entry("ganglion_matter", "Ganglion Matter"),
			Map.entry("cortical_folds", "Cortical Folds"),
			Map.entry("nerve_fiber", "Nerve Fiber"),
			Map.entry("nerve_bundle", "Nerve Bundle"),
			Map.entry("synaptic_node", "Synaptic Node"),
			Map.entry("dura_membrane", "Dura Membrane"),
			Map.entry("dendrite_growth", "Dendrite Growth"));

	@Test
	void everyCorticalBlockHasRuntimeEnglishLocalization() throws IOException {
		JsonObject language = json("assets/hemomancy/lang/en_us.json");
		for (Map.Entry<String, String> block : BLOCKS.entrySet()) {
			assertTrue(language.has("block.hemomancy." + block.getKey()),
					() -> "missing localization for " + block.getKey());
			assertEquals(block.getValue(), language.get("block.hemomancy." + block.getKey()).getAsString());
		}
	}

	@Test
	void everyCorticalBlockDropsItself() throws IOException {
		for (String block : BLOCKS.keySet()) {
			JsonObject loot = json("data/hemomancy/loot_table/blocks/" + block + ".json");
			JsonObject pool = loot.getAsJsonArray("pools").get(0).getAsJsonObject();
			JsonObject entry = pool.getAsJsonArray("entries").get(0).getAsJsonObject();
			assertEquals("minecraft:block", loot.get("type").getAsString());
			assertEquals("minecraft:item", entry.get("type").getAsString());
			assertEquals("hemomancy:" + block, entry.get("name").getAsString());
			assertEquals("minecraft:survives_explosion",
					pool.getAsJsonArray("conditions").get(0).getAsJsonObject().get("condition").getAsString());
		}
	}

	@Test
	void corticalBlocksUseToolsThatMatchTheirMaterial() throws IOException {
		assertTagContains("pickaxe", "neural_tissue", "axonal_slate", "ganglion_matter", "cortical_folds",
				"synaptic_node");
		assertTagContains("shovel", "myelin_sheath");
		assertTagContains("axe", "nerve_fiber", "nerve_bundle");
		assertTagContains("hoe", "dura_membrane", "dendrite_growth");
	}

	private static void assertTagContains(String tool, String... blocks) throws IOException {
		JsonArray values = json("data/minecraft/tags/block/mineable/" + tool + ".json").getAsJsonArray("values");
		Set<String> entries = StreamSupport.stream(values.spliterator(), false)
				.map(element -> element.getAsString())
				.collect(Collectors.toSet());
		for (String block : blocks) {
			assertTrue(entries.contains("hemomancy:" + block),
					() -> block + " must be efficiently broken with a " + tool);
		}
	}

	private static JsonObject json(String relativePath) throws IOException {
		return JsonParser.parseString(Files.readString(RESOURCES.resolve(relativePath))).getAsJsonObject();
	}
}
