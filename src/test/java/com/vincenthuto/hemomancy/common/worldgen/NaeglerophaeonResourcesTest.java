package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class NaeglerophaeonResourcesTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static JsonObject json(String relative) throws IOException {
		return JsonParser.parseString(Files.readString(ROOT.resolve(relative))).getAsJsonObject();
	}

	@Test
	void dropAndRecipeAreRegisteredButBiomeDoesNotSpawnBoss() throws IOException {
		String entity = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		assertTrue(entity.contains("register(\"naeglerophaeon\""));
		JsonObject loot = json("src/main/resources/data/hemomancy/loot_table/entities/naeglerophaeon.json");
		assertEquals("hemomancy:naeglerophaeon_ganglion", loot.getAsJsonArray("pools")
				.get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject()
				.get("name").getAsString());
		JsonObject recipe = json("src/main/resources/data/hemomancy/recipe/synaptic_step.json");
		assertEquals(4, recipe.getAsJsonArray("ingredients").size());
		assertEquals("hemomancy:synaptic_step", recipe.getAsJsonObject("result").get("id").getAsString());
		assertFalse(Files.readString(ROOT.resolve(
				"src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json"))
				.contains("naeglerophaeon"));
		assertTrue(json("src/main/resources/data/hemomancy/blood_profiles/naeglerophaeon.json")
				.get("requires_living_syringe").getAsBoolean());
	}

	@Test
	void itemArtUsesTheNativePixelGrid() throws IOException {
		for (String name : new String[] {"naeglerophaeon_ganglion", "synaptic_step"}) {
			Path path = ROOT.resolve("src/main/resources/assets/hemomancy/textures/item/" + name + ".png");
			var image = ImageIO.read(path.toFile());
			assertEquals(16, image.getWidth(), name);
			assertEquals(16, image.getHeight(), name);
		}
	}
}
