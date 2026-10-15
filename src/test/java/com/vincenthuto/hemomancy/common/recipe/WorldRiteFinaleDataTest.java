package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** World rites end with the daemon striking the sky or slamming the ground; the sky rites are pinned here. */
final class WorldRiteFinaleDataTest {
	private static final Path RITE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	@Test
	void onlyTheEclipseAndPallidShadowStrikeTheSky() throws IOException {
		Set<String> sky = new HashSet<>();
		try (var paths = Files.walk(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				if (!recipe.has("world_finale")) continue;
				String name = path.getFileName().toString().replace(".json", "");
				assertEquals("world", recipe.get("purpose").getAsString(), name + " names a world finale but is not a world rite");
				String finale = recipe.get("world_finale").getAsString();
				assertTrue(finale.equals("sky") || finale.equals("ground"), name + ": " + finale);
				if (finale.equals("sky")) sky.add(name);
			}
		}
		assertEquals(Set.of("sanguine_eclipse", "pallid_shadow"), sky);
	}
}
