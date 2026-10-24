package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * O-7 follow-up: English that a player can read lives in {@code en_us.json}, including names and descriptions in data
 * tables and the reasons rule methods return, which {@link HardCodedEnglishGuardrailTest} cannot see because they never
 * reach a {@code Component.literal} directly. Any sentence-like string literal in main code counts, outside comments and
 * lines that log, throw, assert or build a command. The files that keep some (validation messages, debug tooling, config
 * comments, saved default names) are listed in {@code architecture/english-literal-allow-list.txt} with an exact count
 * and a reason: lower a count when English leaves, and never add a file without a reason that keeps it out of a
 * player's sight.
 */
class EnglishLiteralAllowListTest {
	private static final String ALLOW_LIST = "src/test/resources/architecture/english-literal-allow-list.txt";
	private static final Pattern STRING = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
	private static final Pattern SENTENCE = Pattern.compile("[A-Za-z]{2,}[ ,.;:!?'-]+[A-Za-z]{2,}");
	private static final Pattern RESOURCE_ID = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_/.:-]*");
	private static final Pattern SKIP_LINE = Pattern.compile("LOGGER|Logger|\\.(?:info|warn|error|debug|trace)\\("
			+ "|throw new|Exception\\(|AssertionError|@\\w+\\(|Commands\\.literal|Commands\\.argument|System\\.(?:out|err)"
			+ "|\\.comment\\(|\\.define|IllegalArgument|IllegalState|requireNonNull|DataResult\\.error|assert ");
	private static final List<String> DORMANT = List.of("compat/mna/", "compat/curios/");

	@Test
	void englishLiteralsStayInTheirListedFiles() throws Exception {
		Map<String, Integer> allowed = new TreeMap<>();
		for (String line : SourceAssertions.read(ALLOW_LIST).split("\n")) {
			line = line.strip();
			if (line.isEmpty() || line.startsWith("#")) continue;
			String[] parts = line.split("\t");
			allowed.put(parts[1], Integer.parseInt(parts[0]));
		}
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String relative = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
				if (DORMANT.stream().anyMatch(relative::startsWith)) continue;
				int count = countEnglish(SourceAssertions.read(file));
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		List<String> drift = new ArrayList<>();
		for (var entry : found.entrySet()) {
			Integer limit = allowed.get(entry.getKey());
			if (!entry.getValue().equals(limit)) {
				drift.add(entry.getKey() + ": " + entry.getValue() + " English literals, list says " + limit);
			}
		}
		for (String listed : allowed.keySet()) {
			if (!found.containsKey(listed)) drift.add(listed + ": listed but has none; remove the line");
		}
		assertEquals(List.of(), drift, "Move player-facing English to en_us.json; update " + ALLOW_LIST
				+ " only for text no player sees");
	}

	static int countEnglish(String source) {
		int count = 0;
		for (String line : source.split("\n")) {
			String stripped = line.strip();
			if (stripped.startsWith("//") || stripped.startsWith("*") || stripped.startsWith("/*")
					|| SKIP_LINE.matcher(line).find()) {
				continue;
			}
			Matcher string = STRING.matcher(withoutTrailingComment(line));
			while (string.find()) {
				String text = string.group(1);
				if (text.contains(".") && !text.contains(" ")) continue;
				if (RESOURCE_ID.matcher(text).matches()) continue;
				if (SENTENCE.matcher(text).find()) count++;
			}
		}
		return count;
	}

	/** The line up to a {@code //} comment that starts outside a string or char literal. */
	private static String withoutTrailingComment(String line) {
		char quote = 0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			} else if (c == '"' || c == '\'') {
				quote = c;
			} else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
				return line.substring(0, i);
			}
		}
		return line;
	}
}
