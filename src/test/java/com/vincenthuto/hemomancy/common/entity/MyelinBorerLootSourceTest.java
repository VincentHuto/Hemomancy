package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerLootSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void itemIsRegistered() throws IOException {
		assertTrue(Files.readString(
				ROOT.resolve("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"))
				.contains("ganglion_cluster"), "the Borer's drop must exist as an item");
	}

	@Test
	void lootTableDropsTheCluster() throws IOException {
		Path loot = ROOT.resolve("src/main/resources/data/hemomancy/loot_table/entities/myelin_borer.json");
		assertTrue(Files.exists(loot), "without a loot table the Borer drops nothing at all");
		JsonObject json;
		try (var reader = Files.newBufferedReader(loot)) {
			json = JsonParser.parseReader(reader).getAsJsonObject();
		}
		assertTrue(json.toString().contains("hemomancy:ganglion_cluster"), "it must drop the cluster");
		assertTrue("minecraft:entity".equals(json.get("type").getAsString()), "it is an entity loot table");
	}

	@Test
	void clustersCraftIntoSynapticNodes() throws IOException {
		Path recipe = ROOT.resolve(
				"src/main/resources/data/hemomancy/recipe/ganglion_cluster_to_synaptic_node.json");
		assertTrue(Files.exists(recipe),
				"the payoff is letting players extend the conduction network themselves");
		String json = Files.readString(recipe);
		assertTrue(json.contains("hemomancy:ganglion_cluster") && json.contains("hemomancy:synaptic_node"),
				"the recipe must turn clusters into placeable nodes");
	}
}
