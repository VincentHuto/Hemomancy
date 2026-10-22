package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T-2: tests share {@code support/} helpers instead of private copies. Existing copies are removed as tests are
 * touched, so the count may only fall. When it falls, lower {@link #CEILING} to the new count.
 */
class PrivateTestHelperRatchetTest {
	/** Private read/assert helper declarations outside {@code support/} at the end of consolidation step 2. */
	private static final int CEILING = 593;
	private static final Pattern PRIVATE_HELPER = Pattern.compile(
			"private\\s+static\\s+[\\w<>\\[\\],.? ]+?\\s+"
					+ "(read|readResource|assertContains|assertNotContains|assertDoesNotContain)\\s*\\(");

	@Test
	void privateHelperCopiesDoNotGrow() throws Exception {
		Path support = RepoPaths.TEST_JAVA.resolve("support");
		int count = 0;
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/test/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (file.startsWith(support)) {
					continue;
				}
				Matcher matcher = PRIVATE_HELPER.matcher(SourceAssertions.read(file));
				while (matcher.find()) {
					count++;
				}
			}
		}
		assertTrue(count <= CEILING, count + " private read/assert helpers exceed the ceiling of " + CEILING
				+ "; use support/SourceAssertions, support/ResourceReader and support/RepoPaths instead");
	}
}
