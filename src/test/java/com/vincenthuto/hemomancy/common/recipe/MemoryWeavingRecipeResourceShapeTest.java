package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class MemoryWeavingRecipeResourceShapeTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path RECIPE_DIR = ROOT.resolve("src/main/resources/data/hemomancy/recipe/memory_weaving");
	private static final String[] LEGACY_TENDENCY_KEYS = {
			"\"animus\": true", "\"flammeus\": true", "\"ductilis\": true", "\"lux\": true",
			"\"mortem\": true", "\"congeatio\": true", "\"ferric\": true", "\"tenebris\": true"
	};

	private MemoryWeavingRecipeResourceShapeTest() {
	}

	public static void main(String[] args) throws IOException {
		assertMigratedMemoryRecipesUseNewShape();
		assertNoDuplicateCatalystAndEnzymeSignatures();
	}

	private static void assertMigratedMemoryRecipesUseNewShape() throws IOException {
		try (Stream<Path> paths = Files.list(RECIPE_DIR)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				String json = Files.readString(path);
				assertContains(path, json, "\"catalysts\"");
				assertCatalystsAreNotEmpty(path, json);
				assertContains(path, json, "\"enzymes\"");
				assertContains(path, json, "\"blood\"");
				assertDoesNotContain(path, json, "\"ingredient\"");
				for (String legacy : LEGACY_TENDENCY_KEYS) {
					assertDoesNotContain(path, json, legacy);
				}
			}
		}
	}

	private static void assertNoDuplicateCatalystAndEnzymeSignatures() throws IOException {
		Set<String> signatures = new HashSet<>();
		try (Stream<Path> paths = Files.list(RECIPE_DIR)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				String json = Files.readString(path).replace("\r\n", "\n");
				String signature = section(json, "\"catalysts\"", "\"result\"")
						.replaceAll("\\s+", "");
				if (!signatures.add(signature)) {
					throw new AssertionError("duplicate memory weaving catalyst/enzyme signature in " + path);
				}
			}
		}
	}

	private static String section(String text, String start, String end) {
		int startIndex = text.indexOf(start);
		int endIndex = text.indexOf(end, startIndex);
		if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex) {
			throw new AssertionError("unable to locate section from " + start + " to " + end);
		}
		return text.substring(startIndex, endIndex);
	}

	private static void assertCatalystsAreNotEmpty(Path path, String json) {
		String catalysts = section(json, "\"catalysts\"", "\"enzymes\"");
		if (!catalysts.contains("\"item\"") && !catalysts.contains("\"tag\"")) {
			throw new AssertionError(path + " must define at least one catalyst item or tag");
		}
	}

	private static void assertContains(Path path, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(path + " missing '" + expected + "'");
		}
	}

	private static void assertDoesNotContain(Path path, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(path + " still contains legacy '" + forbidden + "'");
		}
	}
}
