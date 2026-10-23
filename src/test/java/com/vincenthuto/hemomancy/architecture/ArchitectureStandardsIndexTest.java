package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every architecture guardrail is listed in the guardrail index of {@code docs/architecture/ARCHITECTURE_STANDARDS.md},
 * so the standards stay the one place a contributor reads to learn what the build enforces.
 */
class ArchitectureStandardsIndexTest {
	private static final List<String> HELPERS = List.of("NetworkRegistrations");

	@Test
	void everyArchitectureTestIsInTheGuardrailIndex() throws Exception {
		String standards = SourceAssertions.read("docs/architecture/ARCHITECTURE_STANDARDS.md");
		String index = SourceAssertions.section(standards, "## Guardrail index", "Behaviour and data tests");
		try (Stream<Path> files = Files.list(RepoPaths.TEST_JAVA.resolve("architecture"))) {
			List<String> missing = files.map(path -> path.getFileName().toString())
					.filter(name -> name.endsWith(".java"))
					.map(name -> name.substring(0, name.length() - ".java".length()))
					.filter(name -> !HELPERS.contains(name))
					.filter(name -> !index.contains("`" + name + "`"))
					.sorted()
					.toList();
			assertEquals(List.of(), missing, "list these guardrails in ARCHITECTURE_STANDARDS.md");
		}
	}
}
