package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 11 (roadmap Phase 10): {@code /hemo} is one root with one permission check, and each subcommand is its own
 * {@code <Name>Subcommand} class whose {@code build()} returns its branch. The syntax itself is pinned by
 * {@code HemoCommandTreeTest}; this keeps the structure from growing back into one class.
 */
class HemoCommandStructureGuardrailTest {
	private static final Path COMMANDS = RepoPaths.mainJava("common/command");
	private static final int MAX_SUBCOMMAND_LINES = 300;

	@Test
	void theRootOnlyWiresSubcommands() throws IOException {
		String root = SourceAssertions.read(COMMANDS.resolve("HemoCommand.java"));
		Set<String> unwired = new TreeSet<>();
		try (Stream<Path> files = Files.list(COMMANDS)) {
			for (Path file : files.filter(path -> path.toString().endsWith("Subcommand.java")).toList()) {
				String name = file.getFileName().toString().replace(".java", "");
				String source = SourceAssertions.read(file);
				SourceAssertions.assertContains(name + " builds one branch", source,
						"static LiteralArgumentBuilder<CommandSourceStack> build()");
				SourceAssertions.assertNotContains(name + " registers nothing itself", source, "dispatcher.register(");
				assertTrue(source.lines().count() <= MAX_SUBCOMMAND_LINES,
						name + " has grown past " + MAX_SUBCOMMAND_LINES + " lines; split its handlers out");
				if (root.split(java.util.regex.Pattern.quote(".then(" + name + ".build())"), -1).length != 2) unwired.add(name);
			}
		}
		assertEquals(Set.of(), unwired, "wire each subcommand into HemoCommand.register exactly once");
		SourceAssertions.assertNotContains("the root holds no handlers", root, "ctx ->");
	}

	@Test
	void onlyHemoCommandRegistersTheHemoRootInTheMod() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String source = SourceAssertions.read(file).replaceAll("\\s+", "");
				int count = source.split("register\\(Commands\\.literal\\(\"hemo\"\\)", -1).length - 1;
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		assertEquals(Map.of(RepoPaths.display(COMMANDS.resolve("HemoCommand.java")), 1), found,
				"add a /hemo branch as a Subcommand class wired into HemoCommand");
	}
}
