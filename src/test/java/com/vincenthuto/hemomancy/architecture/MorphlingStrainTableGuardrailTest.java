package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Brined Court Track 1: a Morphling strain is one row of {@code MorphlingStrains}. Code reads the table instead of
 * naming a strain's {@code morphling_<id>} item (only {@code ItemInit}, which registers the items, may), and the
 * eight first-attempt strains, deleted without migration, leave no trace under {@code src/}.
 */
class MorphlingStrainTableGuardrailTest {
	private static final Pattern STRAIN_ID = Pattern.compile(
			"morphling_(?:sunburst|holdfast|mermaids_purse|dead_mans_rope|ink_cap|hushbell|marrowplume|kinbud)\\b");
	private static final Pattern OLD_STRAIN = Pattern.compile(
			"lumenlace|irontooth|deadman'?s[ _]?purse|witch'?s[ _]?ear|bootlace|winter[ _]?shroud|gravecap|emberfang",
			Pattern.CASE_INSENSITIVE);
	private static final List<String> REGISTRATIONS = List.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
	private static final String THIS_FILE = "MorphlingStrainTableGuardrailTest.java";

	@Test
	void strainIdsAreNamedOnlyByTheTableAndItemRegistration() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String display = RepoPaths.display(file);
				if (REGISTRATIONS.contains(display)) continue;
				int count = count(STRAIN_ID.matcher(SourceAssertions.read(file)));
				if (count > 0) found.put(display, count);
			}
		}
		assertEquals(Map.of(), found, "read MorphlingStrains (a row's item(), itemPath() or passiveEffect()) instead");
	}

	@Test
	void theFirstAttemptStrainsLeaveNoTrace() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src"))) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				if (file.getFileName().toString().equals(THIS_FILE)) continue;
				String text = file.getFileName() + "\n"
						+ new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
				int count = count(OLD_STRAIN.matcher(text));
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		assertEquals(Map.of(), found, "the old Morphling strains were deleted outright; docs may keep them as history");
	}

	private static int count(Matcher matcher) {
		int count = 0;
		while (matcher.find()) count++;
		return count;
	}
}
