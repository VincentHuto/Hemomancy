package com.vincenthuto.hemomancy.common.manipulation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ActiveManipulationMemorySourceTest {
	private static final Path RECIPES = Path.of("src/main/resources/data/hemomancy/recipe/memory_weaving");
	private static final Map<String, String> REQUIRED_SOURCES = Map.of(
			"vital_effusion", "animus",
			"hemolymphal_pulse", "ductilis",
			"vascular_dowsing", "ferric");

	private ActiveManipulationMemorySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		for (Map.Entry<String, String> entry : REQUIRED_SOURCES.entrySet()) {
			assertReachableMemory(entry.getKey(), entry.getValue());
		}
	}

	private static void assertReachableMemory(String manipulation, String primaryTendency) throws IOException {
		Path path = RECIPES.resolve("memory_" + manipulation + ".json");
		if (!Files.isRegularFile(path)) {
			throw new AssertionError("active manipulation has no memory source: " + manipulation);
		}
		JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		assertEquals(manipulation + " output", "hemomancy:memory_" + manipulation,
				recipe.get("result").getAsString());
		int tendencyAmount = recipe.getAsJsonObject("enzymes").get(primaryTendency).getAsInt();
		if (tendencyAmount <= 0) {
			throw new AssertionError(manipulation + " must require its " + primaryTendency + " enzyme identity");
		}
		if (recipe.getAsJsonArray("catalysts").isEmpty()) {
			throw new AssertionError(manipulation + " must have an authored catalyst");
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
