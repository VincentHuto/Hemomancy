package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * D-2 / D-8: a Hemomancy resource location comes from {@code Hemomancy.rloc("path")}, never from
 * {@code ResourceLocation.fromNamespaceAndPath("hemomancy", ...)} or {@code ResourceLocation.parse("hemomancy:...")}.
 * {@code Hemomancy.java}, which defines {@code rloc}, is the one exception.
 */
class ResourceLocationStyleGuardrailTest {
	private static final Pattern HAND_BUILT = Pattern.compile(
			"fromNamespaceAndPath\\(\\s*(?:\"hemomancy\"|Hemomancy\\.MOD_ID|MOD_ID)\\s*,|ResourceLocation\\.parse\\(\\s*\"hemomancy:");

	@Test
	void hemomancyLocationsComeFromRloc() throws Exception {
		List<String> handBuilt = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (file.getFileName().toString().equals("Hemomancy.java")) continue;
				Matcher matcher = HAND_BUILT.matcher(SourceAssertions.read(file));
				while (matcher.find()) handBuilt.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of(), handBuilt, "use Hemomancy.rloc");
	}
}
