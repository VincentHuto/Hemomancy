package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Biome selection and terrain must read one set of climate functions, or biomes drift off their land. */
class FungalGardensTerrainCoherenceTest {
	private static final Path DENSITY = Path.of("src/main/resources/data/hemomancy/worldgen/density_function/fungal");
	private static final Set<String> CLIMATE_NOISES = Set.of("minecraft:continentalness", "minecraft:erosion",
			"minecraft:ridge", "minecraft:vegetation", "minecraft:temperature");
	private static final Set<String> CLIMATE_FILES = Set.of("continents.json", "erosion.json", "ridges.json",
			"humidity.json", "temperature.json");

	@Test
	void routerClimateAndTerrainShareTheSameFunctions() throws IOException {
		JsonObject router = resourceJson("data/hemomancy/worldgen/noise_settings/fungal_noise_settings.json")
				.getAsJsonObject("noise_router");

		assertEquals("hemomancy:fungal/continents", router.get("continents").getAsString());
		assertEquals("hemomancy:fungal/erosion", router.get("erosion").getAsString());
		assertEquals("hemomancy:fungal/ridges", router.get("ridges").getAsString());
		assertEquals("hemomancy:fungal/humidity", router.get("vegetation").getAsString());
		assertEquals("hemomancy:fungal/depth", router.get("depth").getAsString());
		assertTrue(router.get("final_density").toString().contains("hemomancy:fungal/terrain"),
				"final density must be the shared fungal terrain");
	}

	@Test
	void climateNoiseIsSampledOnlyByTheClimateFunctions() throws IOException {
		try (Stream<Path> files = Files.list(DENSITY)) {
			for (Path file : files.toList()) {
				if (CLIMATE_FILES.contains(file.getFileName().toString())) continue;
				String text = Files.readString(file);
				for (String noise : CLIMATE_NOISES) {
					assertFalse(text.contains("\"" + noise + "\""),
							file.getFileName() + " samples " + noise + " directly instead of the shared climate function");
				}
			}
		}
	}

	@Test
	void biomeBoxesTileWithoutOverlap() throws IOException {
		List<double[]> boxes = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (JsonElement entry : dimensionBiomes()) {
			JsonObject parameters = entry.getAsJsonObject().getAsJsonObject("parameters");
			boxes.add(new double[] {
					min(parameters, "continentalness"), max(parameters, "continentalness"),
					min(parameters, "weirdness"), max(parameters, "weirdness"),
					min(parameters, "humidity"), max(parameters, "humidity") });
			names.add(entry.getAsJsonObject().get("biome").getAsString());
		}

		double volume = 0;
		for (int a = 0; a < boxes.size(); a++) {
			double[] box = boxes.get(a);
			volume += (box[1] - box[0]) * (box[3] - box[2]) * (box[5] - box[4]);
			for (int b = a + 1; b < boxes.size(); b++) {
				assertFalse(overlaps(box, boxes.get(b)), names.get(a) + " overlaps " + names.get(b));
			}
		}
		assertEquals(8.0, volume, 1.0E-9, "biome boxes must cover the whole climate cube");
	}

	@Test
	void dimensionUsesItsOwnLowlandsAndNamesEveryBiome() throws IOException {
		Set<String> biomes = new LinkedHashSet<>();
		dimensionBiomes().forEach(entry -> biomes.add(entry.getAsJsonObject().get("biome").getAsString()));
		String lang = resourceText("assets/hemomancy/lang/en_us.json");

		assertFalse(biomes.contains("hemomancy:fungal_gardens"),
				"fungal_gardens is the Overworld region biome; sharing it leaks structures between worlds");
		assertTrue(biomes.contains("hemomancy:mycelial_meadows"));
		assertEquals(7, biomes.size());
		for (String biome : biomes) {
			assertTrue(lang.contains("\"biome." + biome.replace(':', '.') + "\""), "missing biome name for " + biome);
		}
	}

	private static boolean overlaps(double[] a, double[] b) {
		for (int axis = 0; axis < 6; axis += 2) {
			if (Math.min(a[axis + 1], b[axis + 1]) - Math.max(a[axis], b[axis]) <= 1.0E-9) return false;
		}
		return true;
	}

	private static double min(JsonObject parameters, String key) {
		return parameters.getAsJsonArray(key).get(0).getAsDouble();
	}

	private static double max(JsonObject parameters, String key) {
		return parameters.getAsJsonArray(key).get(1).getAsDouble();
	}

	private static JsonArray dimensionBiomes() throws IOException {
		return resourceJson("data/hemomancy/dimension/fungal_gardens.json").getAsJsonObject("generator")
				.getAsJsonObject("biome_source").getAsJsonArray("biomes");
	}

	private static JsonObject resourceJson(String path) throws IOException {
		return JsonParser.parseString(resourceText(path)).getAsJsonObject();
	}

	private static String resourceText(String path) throws IOException {
		try (InputStream input = FungalGardensTerrainCoherenceTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(input, "Missing resource " + path);
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
