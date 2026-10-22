package com.vincenthuto.hemomancy.testing;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** The reflective adapter must run every legacy {@code main()} test that exists in source, and nothing else. */
class LegacyMainTestSuiteContractTest {
	private static final Path TEST_SOURCE_ROOT = RepoPaths.resolve("src/test/java");
	private static final Pattern MAIN = Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(");

	@Test
	void discoversEveryExistingLegacyMainTest() throws Exception {
		Set<String> inSource;
		try (Stream<Path> files = Files.walk(TEST_SOURCE_ROOT)) {
			inSource = files.filter(path -> path.getFileName().toString().endsWith("Test.java"))
					.filter(path -> MAIN.matcher(SourceAssertions.read(path)).find())
					.map(LegacyMainTestSuiteContractTest::classNameFor)
					.collect(Collectors.toCollection(TreeSet::new));
		}
		Set<String> discovered = LegacyMainTestAdapterTest.discoverLegacyMainClasses().stream()
				.map(Class::getName)
				.collect(Collectors.toCollection(TreeSet::new));

		assertFalse(inSource.isEmpty(), "no legacy main tests found in source");
		assertEquals(inSource, discovered);
	}

	private static String classNameFor(Path sourceFile) {
		String relative = TEST_SOURCE_ROOT.relativize(sourceFile).toString().replace('\\', '/');
		return relative.substring(0, relative.length() - ".java".length()).replace('/', '.');
	}
}
