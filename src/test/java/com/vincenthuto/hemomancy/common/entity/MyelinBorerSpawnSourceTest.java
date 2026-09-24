package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class MyelinBorerSpawnSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void borersSpawnInTheDrift() throws IOException {
		Path biome = ROOT.resolve("src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json");
		JsonObject json;
		try (var reader = Files.newBufferedReader(biome)) {
			json = JsonParser.parseReader(reader).getAsJsonObject();
		}
		var monsters = json.getAsJsonObject("spawners").getAsJsonArray("monster");
		assertTrue(StreamSupport.stream(monsters.spliterator(), false)
				.anyMatch(e -> "hemomancy:myelin_borer".equals(e.getAsJsonObject().get("type").getAsString())),
				"bootstrapBiomes is dead code in this repo, so the JSON is what actually spawns the Borer");
	}

	@Test
	void spawnsOnlyNearTheNetwork() throws IOException {
		String entity = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java"));
		int hook = entity.indexOf("canSpawnHere");
		assertTrue(hook > 0, "the spawn hook must exist");
		assertTrue(entity.substring(hook).contains("CRAWLABLE"),
				"Borers ride the bridges; spawning them adrift in the void would drop them into nothing");
	}
}
