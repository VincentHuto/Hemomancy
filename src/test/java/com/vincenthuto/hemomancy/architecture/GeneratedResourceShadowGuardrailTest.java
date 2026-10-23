package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * D-5: {@code processResources} lets {@code src/main/resources} win over {@code src/generated/resources}, so a
 * generated file with an authored twin never ships. Datagen writes only what nothing authors, and runs only the
 * providers listed here; language, recipes, tags and loot are authored by hand.
 */
class GeneratedResourceShadowGuardrailTest {
	private static final Path GENERATED = RepoPaths.resolve("src/generated/resources");

	@Test
	void noGeneratedFileIsShadowedByAnAuthoredOne() throws Exception {
		try (Stream<Path> files = Files.walk(GENERATED)) {
			List<String> shadowed = files.filter(Files::isRegularFile)
					.map(GENERATED::relativize)
					.filter(relative -> !relative.startsWith(".cache"))
					.filter(relative -> Files.exists(RepoPaths.MAIN_RESOURCES.resolve(relative)))
					.map(relative -> relative.toString().replace('\\', '/'))
					.sorted()
					.toList();
			assertEquals(List.of(), shadowed, "Delete the generated copy and skip it in its provider");
		}
	}

	@Test
	void dataGenerationRunsOnlyTheKeptProviders() {
		String generation = SourceAssertions.readMainJava("common/data/gen/DataGeneration.java");
		List<String> providers = Pattern.compile("addProvider\\([^,]+,\\s*new (\\w+)\\(")
				.matcher(generation).results().map(match -> match.group(1)).toList();
		assertEquals(List.of("HemoBlockStateProvider", "HemoItemModelProvider", "CircusPavilionStructureProvider"),
				providers);
	}
}
