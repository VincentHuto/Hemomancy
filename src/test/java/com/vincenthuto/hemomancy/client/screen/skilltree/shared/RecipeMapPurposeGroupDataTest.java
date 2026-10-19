package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Pins every shipped Harbinger rite and craft to the purpose group its family sits in on the Liber map. */
final class RecipeMapPurposeGroupDataTest {
	private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe");

	@Test
	void everyRiteSitsInItsPurposeGroup() throws IOException {
		try (Stream<Path> files = Files.walk(RECIPES.resolve("cardinal_rite"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String path = recipePath(file);
				CardinalRitePurpose purpose = CardinalRitePurpose.byId(json.get("purpose").getAsString());
				assertNotNull(purpose, path + " declares no purpose");
				String family = path.startsWith("cardinal_rite/working/")
						? HarbingerRecipeMapDefinitions.craftingFamily(path)
						: HarbingerRecipeMapDefinitions.riteFamily(path);
				assertEquals(RecipeMapPurposeGroup.of(purpose), HarbingerRecipeMapDefinitions.group(family),
						path + " sits in " + family);
			}
		}
	}

	@Test
	void floorlessCraftsSitOnlyInTheFloorlessGroup() throws IOException {
		try (Stream<Path> files = Files.list(RECIPES.resolve("blood_structure"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String path = recipePath(file);
				assertEquals("Floorless Crafts", HarbingerRecipeMapDefinitions.craftingFamily(path), path);
			}
		}
	}

	private static String recipePath(Path file) {
		String relative = RECIPES.relativize(file).toString().replace('\\', '/');
		return relative.substring(0, relative.length() - ".json".length());
	}
}
