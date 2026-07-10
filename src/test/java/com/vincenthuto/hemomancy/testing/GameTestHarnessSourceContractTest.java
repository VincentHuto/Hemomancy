package com.vincenthuto.hemomancy.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class GameTestHarnessSourceContractTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void devOnlyHarnessProvidesCommandsAndEarlyHarbingerScenarios() throws IOException {
		String build = read("build.gradle");
		String catalog = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestScenarioCatalog.java");
		String commands = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestCommands.java");
		String gameTests = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerPilotGameTests.java");
		String guide = read("docs/TESTING.md");

		assertTrue(build.contains("gameTest"), "build must declare the isolated gameTest source set");
		assertTrue(build.contains("tasks.register('alphaCheck')"));
		assertTrue(commands.contains("literal(\"setup\")"));
		assertTrue(commands.contains("literal(\"verify\")"));
		assertTrue(commands.contains("literal(\"run\")"));
		assertTrue(commands.contains("literal(\"run_all\")"));
		assertTrue(commands.contains("runAll("));
		assertTrue(commands.contains("literal(\"status\")"));
		assertTrue(commands.contains("literal(\"clear\")"));
		assertTrue(gameTests.contains("@GameTestHolder(Hemomancy.MOD_ID)"));

		var ids = Pattern.compile("new HemoTestScenario\\(\\s*\"([^\"]+)\"")
				.matcher(catalog).results().map(match -> match.group(1)).toList();
		assertEquals(7, ids.size());
		assertTrue(ids.contains("blood_structure_locked"));
		assertTrue(ids.contains("blood_structure_unlocked"));
		assertTrue(ids.contains("artificer_assignment_ready"));
		assertTrue(ids.contains("artificer_reward_claimed"));
		assertTrue(ids.contains("uninitiated_cannot_pass_bloodcraft_degree_gate"));
		assertTrue(ids.contains("sanguine_initiation_recipe_loaded"));
		assertTrue(ids.contains("sanguine_initiation_degree_mapping"));
		assertTrue(guide.contains("./gradlew.bat alphaCheck"));
		assertTrue(guide.contains("/hemo test setup"));
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}
}
