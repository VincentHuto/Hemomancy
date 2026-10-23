package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O-7: player-facing English lives in {@code en_us.json}. Item tooltips, effect names and the Saint encounters use
 * translation keys; the hard-coded English that remains (mostly operator command output, screens and rite messages)
 * may only shrink. Lower the ceilings when you translate more.
 */
class HardCodedEnglishRatchetTest {
	private static final int LITERAL_CEILING = 865;
	private static final int TOOLTIP_CEILING = 28;
	private static final Pattern LITERAL = Pattern.compile("Component\\.literal\\(\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
	private static final Pattern ENGLISH = Pattern.compile("[A-Za-z]{2}");
	private static final Pattern STEP_KEY = Pattern.compile(
			"Component\\.translatable\\(\\s*\"((?:tooltip|message)\\.hemomancy\\.[a-z0-9_.]*[a-z0-9_])\"\\s*[,)]");

	@Test
	void hardCodedEnglishOnlyShrinks() throws Exception {
		int literals = 0;
		int tooltips = 0;
		for (Path file : mainSources()) {
			String source = SourceAssertions.read(file);
			literals += english(source);
			Matcher hover = Pattern.compile("\\bappendHoverText\\s*\\([^)]*\\)\\s*\\{").matcher(source);
			while (hover.find()) {
				tooltips += english(source.substring(hover.end() - 1, closing(source, hover.end() - 1)));
			}
		}
		assertTrue(literals <= LITERAL_CEILING, "English literals rose to " + literals + "; use a translation key");
		assertTrue(tooltips <= TOOLTIP_CEILING, "English tooltip literals rose to " + tooltips + "; use a translation key");
	}

	@Test
	void everyTooltipAndMessageKeyIsInEnglish() throws Exception {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> missing = new ArrayList<>();
		for (Path file : mainSources()) {
			Matcher key = STEP_KEY.matcher(SourceAssertions.read(file));
			while (key.find()) {
				if (!lang.has(key.group(1))) missing.add(RepoPaths.display(file) + ": " + key.group(1));
			}
		}
		assertEquals(List.of(), missing);
	}

	private static int english(String source) {
		int count = 0;
		Matcher literal = LITERAL.matcher(source);
		while (literal.find()) {
			if (ENGLISH.matcher(literal.group(1)).find()) count++;
		}
		return count;
	}

	private static int closing(String source, int open) {
		int depth = 0;
		for (int i = open; i < source.length(); i++) {
			if (source.charAt(i) == '{') depth++;
			else if (source.charAt(i) == '}' && --depth == 0) return i;
		}
		return source.length();
	}

	private static List<Path> mainSources() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java")).toList();
		}
	}
}
