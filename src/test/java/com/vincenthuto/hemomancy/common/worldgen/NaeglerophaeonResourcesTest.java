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
		Path weaving = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving/memory_axonal_transduction.json");
		assertTrue(Files.exists(weaving), "The ganglion must weave Axonal Transduction in the Somatic Loom");
		JsonObject recipe = json(ROOT.relativize(weaving).toString());
		assertEquals("hemomancy:memory_weaving", recipe.get("type").getAsString());
		assertEquals("hemomancy:naeglerophaeon_ganglion", recipe.getAsJsonArray("catalysts")
				.get(0).getAsJsonObject().get("item").getAsString());
		assertEquals(4, recipe.getAsJsonObject("enzymes").get("ductilis").getAsInt());
		assertEquals(600, recipe.get("blood").getAsInt());
		assertEquals("hemomancy:memory_axonal_transduction", recipe.get("result").getAsString());
		assertFalse(Files.exists(ROOT.resolve("src/main/resources/data/hemomancy/recipe/synaptic_step.json")));
		JsonObject salvage = json("src/main/resources/data/hemomancy/recipe/synaptic_step_salvage.json");
		assertEquals("hemomancy:synaptic_step", salvage.getAsJsonArray("ingredients")
				.get(0).getAsJsonObject().get("item").getAsString());
		assertEquals("hemomancy:naeglerophaeon_ganglion", salvage.getAsJsonObject("result").get("id").getAsString());
		assertFalse(Files.readString(ROOT.resolve(
				"src/main/resources/data/hemomancy/worldgen/biome/cortical_drift.json"))
				.contains("naeglerophaeon"));
		assertFalse(json("src/main/resources/data/hemomancy/blood_profiles/naeglerophaeon.json")
				.get("requires_living_syringe").getAsBoolean());
	}

	@Test
	void authoredEntityGeometryIsNotScannedAsABlockModel() throws IOException {
		Path geometry = ROOT.resolve("src/main/resources/assets/hemomancy/entity_geometry/naeglerophaeon_cube_geometry.json");
		assertTrue(Files.exists(geometry), "Custom entity geometry must live outside the block-model scan");
		assertFalse(Files.exists(ROOT.resolve("src/main/resources/assets/hemomancy/models/entity/naeglerophaeon_cube_geometry.json")));
		JsonObject model = JsonParser.parseString(Files.readString(geometry)).getAsJsonObject();
		assertFalse(model.getAsJsonArray("elements").isEmpty());
		assertFalse(model.getAsJsonArray("outliner").isEmpty());
		String renderer = Files.readString(ROOT.resolve("src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/NaeglerophaeonBlockModel.java"));
		assertTrue(renderer.contains("/assets/hemomancy/entity_geometry/naeglerophaeon_cube_geometry.json"));
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
