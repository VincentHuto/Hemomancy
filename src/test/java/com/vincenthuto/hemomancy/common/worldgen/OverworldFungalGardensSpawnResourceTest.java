package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverworldFungalGardensSpawnResourceTest {
	private static final String GARDENS = "hemomancy:fungal_gardens";
	private static final List<String> STEPS = List.of("raw_generation", "lakes", "local_modifications",
			"underground_structures", "surface_structures", "strongholds", "underground_ores",
			"underground_decoration", "fluid_springs", "vegetal_decoration", "top_layer_modification");

	@Test
	void overworldGardensMonstersComeInPacksOfOneToFour() {
		var monsters = gardens().getAsJsonObject("spawners").getAsJsonArray("monster");
		assertFalse(monsters.isEmpty(), "the Overworld Gardens lost its monsters");
		for (JsonElement entry : monsters) {
			var spawn = entry.getAsJsonObject();
			String type = spawn.get("type").getAsString();
			int min = spawn.get("minCount").getAsInt();
			int max = spawn.get("maxCount").getAsInt();
			assertTrue(min >= 1 && max <= 4 && min <= max, type + " spawns in packs of " + min + " to " + max);
		}
	}

	@Test
	void theOverworldGardensIsAnOverworldBiomeWithVanillaOres() {
		JsonArray overworld = ResourceReader.jsonObject("data/minecraft/tags/worldgen/biome/is_overworld.json")
				.getAsJsonArray("values");
		assertTrue(strings(overworld).contains(GARDENS), "fungal_gardens is missing from #minecraft:is_overworld");
		List<String> ores = strings(gardens().getAsJsonArray("features").get(STEPS.indexOf("underground_ores"))
				.getAsJsonArray());
		for (String ore : List.of("minecraft:ore_coal_upper", "minecraft:ore_iron_middle", "minecraft:ore_copper",
				"minecraft:ore_gold", "minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond")) {
			assertTrue(ores.contains(ore), "the Overworld Gardens has no " + ore);
		}
		assertTrue(ores.indexOf("minecraft:ore_copper") < ores.indexOf("hemomancy:fungal_floor"),
				"vanilla ores come first, as in every vanilla biome, so the step keeps one order");
	}

	@Test
	void noMooshroomRowThatCanNeverSpawn() {
		for (JsonElement entry : gardens().getAsJsonObject("spawners").getAsJsonArray("creature")) {
			assertFalse("minecraft:mooshroom".equals(entry.getAsJsonObject().get("type").getAsString()),
					"mooshrooms cannot spawn on erythrocytic mycelium");
		}
	}

	@Test
	void noFeatureModifierAddsAFeatureTheGardensAlreadyLists() throws IOException {
		Set<String> gardensTags = new HashSet<>();
		gardensTags.add("#minecraft:is_overworld");
		gardensTags.add("#c:is_overworld");
		try (Stream<Path> tags = Files.list(ResourceReader.path("data/hemomancy/tags/worldgen/biome"))) {
			for (Path tag : tags.filter(path -> path.toString().endsWith(".json")).toList()) {
				String name = tag.getFileName().toString().replace(".json", "");
				if (strings(ResourceReader.jsonObject("data/hemomancy/tags/worldgen/biome/" + name + ".json")
						.getAsJsonArray("values")).contains(GARDENS)) {
					gardensTags.add("#hemomancy:" + name);
				}
			}
		}
		JsonArray steps = gardens().getAsJsonArray("features");
		List<String> problems = new ArrayList<>();
		try (Stream<Path> modifiers = Files.list(ResourceReader.path("data/hemomancy/neoforge/biome_modifier"))) {
			for (Path file : modifiers.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				JsonObject modifier = ResourceReader.jsonObject(
						"data/hemomancy/neoforge/biome_modifier/" + file.getFileName());
				if (!"neoforge:add_features".equals(modifier.get("type").getAsString())) continue;
				JsonElement biomes = modifier.get("biomes");
				boolean reaches = biomes.isJsonPrimitive() && (gardensTags.contains(biomes.getAsString())
						|| GARDENS.equals(biomes.getAsString()));
				if (!reaches) continue;
				List<String> own = strings(steps.get(STEPS.indexOf(modifier.get("step").getAsString())).getAsJsonArray());
				JsonElement features = modifier.get("features");
				List<String> added = features.isJsonArray() ? strings(features.getAsJsonArray())
						: List.of(features.getAsString());
				for (String feature : added) {
					if (own.contains(feature)) problems.add(file.getFileName() + " adds " + feature + " twice");
				}
			}
		}
		assertTrue(problems.isEmpty(), "a feature listed twice in one step is a feature order cycle: " + problems);
	}

	private static JsonObject gardens() {
		return ResourceReader.data("worldgen/biome/fungal_gardens.json");
	}

	private static List<String> strings(JsonArray array) {
		List<String> values = new ArrayList<>();
		array.forEach(element -> {
			if (element.isJsonPrimitive()) values.add(element.getAsString());
		});
		return values;
	}
}
