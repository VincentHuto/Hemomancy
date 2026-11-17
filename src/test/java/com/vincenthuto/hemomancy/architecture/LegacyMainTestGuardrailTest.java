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
 * UN-71: every test is a JUnit test that Gradle can filter by name. A {@code *Test.java} class declares no
 * {@code main()}, except the radial vein GPU review, whose {@code main} takes an output folder from its init script.
 */
class LegacyMainTestGuardrailTest {
	private static final Pattern MAIN = Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(");
	private static final List<String> TOOL_ENTRY_POINTS = List.of(
			"src/test/java/com/vincenthuto/hemomancy/client/screen/radial/RadialVeinBorderRenderTest.java");

	@Test
	void testClassesDeclareNoMain() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/test/java"))) {
			List<String> withMain = files.filter(path -> path.getFileName().toString().endsWith("Test.java"))
					.filter(path -> MAIN.matcher(SourceAssertions.read(path)).find())
					.map(RepoPaths::display)
					.sorted()
					.toList();
			assertEquals(TOOL_ENTRY_POINTS, withMain, "write checks as @Test methods, not a main()");
		}
	}
}
