package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteExplicitCeremonyResourceTest {
	private static final Path ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");
	private static final Set<String> NON_HARBINGER = Set.of(
			"antiseptic_ground", "clarity_ascension", "closed_vein", "glass_lungs",
			"lethean_baptism", "lethean_font", "lethean_judgment", "lethean_tide",
			"lethe_covenant", "moon_washed_copper", "pale_consecration", "pale_vigil",
			"severed_covenant", "silthmeres_remembrance", "silver_dawn", "silver_veil",
			"still_waters");

	@Test
	void everyProductionHarbingerRiteExplicitlyAuthorsItsCeremony() throws IOException {
		for (Path path : productionHarbingerRecipes()) {
			JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
			assertTrue(recipe.has("ceremony"), path.getFileName() + " is missing ceremony");
			JsonObject ceremony = recipe.getAsJsonObject("ceremony");
			assertTrue(ceremony.has("profile"), path.getFileName() + " is missing profile");
			assertTrue(ceremony.has("anchors") || ceremony.has("layout"),
					path.getFileName() + " is missing anchors/layout");
			assertTrue(ceremony.has("support_sockets"), path.getFileName() + " is missing support_sockets");
			assertTrue(ceremony.has("waves"), path.getFileName() + " is missing waves");
			assertTrue(ceremony.has("signature"), path.getFileName() + " is missing signature");
			assertTrue(ceremony.has("fragile_offsets"), path.getFileName() + " is missing fragile_offsets");
			assertTrue(ceremony.has("target_duration_ticks"),
					path.getFileName() + " is missing target_duration_ticks");
			assertTrue(ceremony.has("focus"), path.getFileName() + " is missing focus");
			assertTrue(ceremony.has("required_helpers"),
					path.getFileName() + " is missing required_helpers");
			assertTrue(ceremony.has("still_interval_ticks"),
					path.getFileName() + " is missing still_interval_ticks");
			assertTrue(ceremony.has("atmosphere"), path.getFileName() + " is missing atmosphere");
			assertTrue(ceremony.has("failure"), path.getFileName() + " is missing failure");
			ceremony.getAsJsonArray("support_sockets").forEach(socket ->
					assertTrue(socket.getAsJsonObject().has("required"),
							path.getFileName() + " has a support socket without required"));
		}
	}

	private static List<Path> productionHarbingerRecipes() throws IOException {
		try (Stream<Path> files = Files.list(ROOT)) {
			return files.filter(path -> path.toString().endsWith(".json"))
					.filter(path -> !path.getFileName().toString().startsWith("sample_"))
					.filter(path -> !NON_HARBINGER.contains(stripExtension(path.getFileName().toString())))
					.toList();
		}
	}

	private static String stripExtension(String name) {
		return name.substring(0, name.length() - ".json".length());
	}
}
