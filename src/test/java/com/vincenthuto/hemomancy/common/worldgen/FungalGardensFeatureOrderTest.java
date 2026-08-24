package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class FungalGardensFeatureOrderTest {
	private static final String BLEEDING_HEARTS = "hemomancy:bleeding_hearts";
	private static final String FUNGAL_FLOOR = "hemomancy:fungal_floor";
	private static final String PATCH_HYPHAE = "hemomancy:patch_hyphae";
	private static final String SMALL_FUNGUS = "hemomancy:small_infected_fungus";

	@Test
	void everyDestinationBiomeContributesAnAcyclicFeatureOrder() throws IOException {
		JsonObject dimension = resourceJson("data/hemomancy/dimension/fungal_gardens.json");
		Set<String> biomes = new LinkedHashSet<>();
		for (JsonElement entry : dimension.getAsJsonObject("generator")
				.getAsJsonObject("biome_source").getAsJsonArray("biomes")) {
			biomes.add(entry.getAsJsonObject().get("biome").getAsString());
		}

		Map<Integer, Map<String, Set<String>>> edgesByStep = new HashMap<>();
		for (String biome : biomes) {
			String path = "data/" + biome.replace(':', '/') + ".json";
			path = path.replace("/hemomancy/", "/hemomancy/worldgen/biome/");
			JsonArray steps = resourceJson(path).getAsJsonArray("features");
			boolean hasFungalFloor = false;
			for (int step = 0; step < steps.size(); step++) {
				JsonArray features = steps.get(step).getAsJsonArray();
				hasFungalFloor |= indexOf(features, FUNGAL_FLOOR) >= 0;
				assertCanonicalRelativeOrder(biome, features);
				Map<String, Set<String>> edges = edgesByStep.computeIfAbsent(step, ignored -> new HashMap<>());
				for (int before = 0; before < features.size(); before++) {
					String first = features.get(before).getAsString();
					edges.computeIfAbsent(first, ignored -> new HashSet<>());
					for (int after = before + 1; after < features.size(); after++) {
						edges.get(first).add(features.get(after).getAsString());
					}
				}
			}
			assertTrue(hasFungalFloor, biome + " is missing the continuous fungal floor feature");
		}

		for (var entry : edgesByStep.entrySet()) {
			assertAcyclic(entry.getKey(), entry.getValue());
		}
	}

	@Test
	void javaBiomeBootstrapUsesTheSameCanonicalOrder() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java"));

		assertSourceOrder(methodBody(source, "fungalIsles"), "PATCH_HYPHAE", "SMALL_INFECTED_FUNGUS");
		assertSourceOrder(methodBody(source, "fungalGardens"),
				"BLEEDING_HEARTS", "PATCH_HYPHAE", "SMALL_INFECTED_FUNGUS");
		assertSourceOrder(methodBody(source, "sporecrownThicket"),
				"BLEEDING_HEARTS", "PATCH_HYPHAE", "SMALL_INFECTED_FUNGUS");
	}

	private static String methodBody(String source, String methodName) {
		int start = source.indexOf("Biome " + methodName);
		int end = source.indexOf("\n\t}", start);
		return source.substring(start, end);
	}

	private static void assertSourceOrder(String source, String... features) {
		int previous = -1;
		for (String feature : features) {
			int current = source.indexOf(feature);
			assertTrue(current > previous, feature + " is out of canonical order");
			previous = current;
		}
	}

	private static void assertCanonicalRelativeOrder(String biome, JsonArray features) {
		int bleeding = indexOf(features, BLEEDING_HEARTS);
		int patch = indexOf(features, PATCH_HYPHAE);
		int small = indexOf(features, SMALL_FUNGUS);
		if (bleeding >= 0 && patch >= 0) assertTrue(bleeding < patch, biome + ": bleeding hearts after hyphae");
		if (bleeding >= 0 && small >= 0) assertTrue(bleeding < small, biome + ": bleeding hearts after fungus");
		if (patch >= 0 && small >= 0) assertTrue(patch < small, biome + ": hyphae after fungus");
	}

	private static int indexOf(JsonArray features, String feature) {
		for (int index = 0; index < features.size(); index++) {
			if (feature.equals(features.get(index).getAsString())) return index;
		}
		return -1;
	}

	private static void assertAcyclic(int step, Map<String, Set<String>> edges) {
		Set<String> nodes = new HashSet<>(edges.keySet());
		edges.values().forEach(nodes::addAll);
		Map<String, Integer> indegree = new HashMap<>();
		nodes.forEach(node -> indegree.put(node, 0));
		edges.values().forEach(targets -> targets.forEach(target -> indegree.merge(target, 1, Integer::sum)));
		ArrayDeque<String> ready = new ArrayDeque<>();
		indegree.forEach((node, count) -> { if (count == 0) ready.add(node); });
		int visited = 0;
		while (!ready.isEmpty()) {
			String node = ready.remove();
			visited++;
			for (String target : edges.getOrDefault(node, Set.of())) {
				if (indegree.merge(target, -1, Integer::sum) == 0) ready.add(target);
			}
		}
		assertEquals(nodes.size(), visited, "feature order cycle in decoration step " + step);
	}

	private static JsonObject resourceJson(String path) throws IOException {
		try (InputStream input = FungalGardensFeatureOrderTest.class.getClassLoader().getResourceAsStream(path)) {
			assertNotNull(input, "Missing resource " + path);
			return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}
}
