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
 * D-2 / D-8: a Hemomancy resource location comes from {@code Hemomancy.rloc("path")}. The hand-built ones left (payload
 * types, effect modifier ids, and model layers owned by their {@code tools/model_export} generators) may only fall.
 */
class ResourceLocationStyleRatchetTest {
	private static final int CEILING = 90;
	private static final Pattern HAND_BUILT = Pattern.compile(
			"fromNamespaceAndPath\\(\\s*(?:\"hemomancy\"|Hemomancy\\.MOD_ID|MOD_ID)\\s*,|ResourceLocation\\.parse\\(\\s*\"hemomancy:");

	@Test
	void handBuiltHemomancyLocationsOnlyFall() throws Exception {
		int count = 0;
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (file.getFileName().toString().equals("Hemomancy.java")) continue;
				Matcher matcher = HAND_BUILT.matcher(SourceAssertions.read(file));
				while (matcher.find()) count++;
			}
		}
		assertTrue(count <= CEILING, count + " hand-built hemomancy resource locations; use Hemomancy.rloc");
	}
}
