package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * O-7: player-facing English lives in {@code en_us.json}. No {@code Component.literal(...)} shows English text, whether
 * as its string, a concatenated piece, a ternary branch or a {@code String.format} pattern; a string passed into another
 * call (such as {@code tag.getString("Name")}) is not shown text. Text with arguments uses
 * {@code Component.translatable(key, args)}, or {@code HemoText.translatable} when an argument is not a component, number,
 * boolean or string.
 */
class HardCodedEnglishGuardrailTest {
	private static final Pattern LITERAL = Pattern.compile("(?<![\\w.])(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.literal\\(");
	private static final Pattern STRING = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
	private static final Pattern ENGLISH = Pattern.compile("[A-Za-z]{2}");
	private static final Pattern CALL_BEFORE = Pattern.compile("[\\w>]\\s*$");
	private static final Pattern TEXT_CALL_BEFORE = Pattern.compile("\\b(?:String\\.format|switch|if)\\s*$");
	/**
	 * A whole key string (not a prefix with something appended), wherever it appears: a call, a ternary, a switch arm, a
	 * data table. Any family counts ({@code screen.hemomancy.}, {@code material.hemomancy.}, {@code item.hemomancy.}...);
	 * keys that start with {@code hemomancy.} share their shape with NBT keys and are not checked.
	 */
	private static final Pattern KEY = Pattern.compile(
			"\"((?!com\\.)[a-z_]+(?:\\.[a-z0-9_]+)*?\\.hemomancy\\.[a-z0-9_.]*[a-z0-9_])\"(?!\\s*\\+)");
	/** A key handed straight to a translatable call, whatever its family, including the {@code hemomancy.} families. */
	private static final Pattern TRANSLATE_CALL = Pattern.compile(
			"(?:Component|HemoText)\\.translatable(?:WithFallback)?\\(\\s*\"(hemomancy\\.[a-z0-9_]+(?:\\.[a-z0-9_]+)+)\"\\s*[,)]");
	/** Orphaned content with no registry entry whose keys were never written; empty since the 2026-11 cull. */
	private static final List<String> UNREGISTERED = List.of();
	private static final List<String> DORMANT = List.<String>of();

	@Test
	void noLiteralShowsEnglish() throws Exception {
		List<String> english = new ArrayList<>();
		for (Path file : mainSources()) {
			String source = SourceAssertions.read(file);
			Matcher literal = LITERAL.matcher(source);
			while (literal.find()) {
				int close = closingParen(source, literal.end());
				String shown = shownEnglish(source.substring(literal.end(), close));
				if (shown != null) {
					int line = (int) source.substring(0, literal.start()).chars().filter(c -> c == '\n').count() + 1;
					english.add(RepoPaths.display(file) + ":" + line + " " + shown);
				}
			}
		}
		assertEquals(List.of(), english, "move this English to en_us.json and use a translation key");
	}

	@Test
	void everyKeyTheCodeNamesIsInEnglish() throws Exception {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> missing = new ArrayList<>();
		for (Path file : mainSources()) {
			if (UNREGISTERED.stream().anyMatch(orphan -> RepoPaths.display(file).endsWith(orphan))) continue;
			String text = SourceAssertions.read(file);
			Matcher key = KEY.matcher(text);
			while (key.find()) {
				if (!lang.has(key.group(1))) missing.add(RepoPaths.display(file) + ": " + key.group(1));
			}
			Matcher call = TRANSLATE_CALL.matcher(text);
			while (call.find()) {
				if (!lang.has(call.group(1))) missing.add(RepoPaths.display(file) + ": " + call.group(1));
			}
		}
		assertEquals(List.of(), missing);
	}

	/** The first English string literal shown as text in a literal's argument, or null. */
	private static String shownEnglish(String argument) {
		Deque<Boolean> callStack = new ArrayDeque<>();
		int i = 0;
		while (i < argument.length()) {
			char c = argument.charAt(i);
			if (c == '"') {
				Matcher string = STRING.matcher(argument);
				string.find(i);
				if (ENGLISH.matcher(string.group()).find() && !callStack.contains(Boolean.TRUE)) return string.group();
				i = string.end();
				continue;
			}
			if (c == '\'') {
				i = argument.indexOf('\'', argument.charAt(i + 1) == '\\' ? i + 2 : i + 1) + 1;
				continue;
			}
			if (c == '(') {
				String before = argument.substring(0, i);
				callStack.push(CALL_BEFORE.matcher(before).find() && !TEXT_CALL_BEFORE.matcher(before).find());
			} else if (c == ')') {
				callStack.pop();
			}
			i++;
		}
		return null;
	}

	private static int closingParen(String source, int from) {
		int depth = 1;
		int i = from;
		while (i < source.length()) {
			char c = source.charAt(i);
			if (c == '"') {
				Matcher string = STRING.matcher(source);
				string.find(i);
				i = string.end();
				continue;
			}
			if (c == '\'') {
				i = source.indexOf('\'', source.charAt(i + 1) == '\\' ? i + 2 : i + 1) + 1;
				continue;
			}
			if (c == '(') depth++;
			else if (c == ')' && --depth == 0) return i;
			i++;
		}
		return source.length();
	}

	private static List<Path> mainSources() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> DORMANT.stream().noneMatch(dormant ->
							RepoPaths.MAIN_JAVA.relativize(path).toString().replace('\\', '/').startsWith(dormant)))
					.toList();
		}
	}
}
