package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 10 (roadmap Phase 8, V-5): noise that two shaders share word for word lives in {@code shaders/include/noise}
 * and is pulled in with {@code #moj_import <hemomancy:noise/...>}. A noise function counts as shared when its text and
 * the text of everything it calls match another shader's, whitespace aside. Seeded variants, whose noise reads one of
 * their shader's own uniforms, stay local.
 */
class ShaderNoiseIncludeGuardrailTest {
	private static final Path SHADERS = RepoPaths.ASSETS.resolve("shaders");
	private static final Path NOISE = SHADERS.resolve("include/noise");
	private static final Pattern FUNCTION = Pattern.compile(
			"(?m)^[ \\t]*(?:float|vec[234]|int|bool|mat[234])\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
	private static final Pattern NOISE_NAME = Pattern.compile("(?i)hash|noise|fbm");
	private static final Pattern UNIFORM = Pattern.compile("(?m)^\\s*uniform\\s+\\w+\\s+(\\w+)");
	private static final Pattern IMPORT = Pattern.compile("#moj_import <hemomancy:noise/([\\w/]+\\.glsl)>");

	@Test
	void sharedNoiseLivesInAnInclude() throws IOException {
		Map<String, List<String>> owners = new TreeMap<>();
		for (Path file : shaders()) {
			String text = SourceAssertions.read(file);
			Map<String, String> bodies = functions(text);
			Set<String> uniforms = new HashSet<>();
			Matcher uniform = UNIFORM.matcher(text);
			while (uniform.find()) uniforms.add(uniform.group(1));
			for (String name : bodies.keySet()) {
				if (!NOISE_NAME.matcher(name).find()) continue;
				String closure = closure(name, bodies, new HashSet<>(Set.of(name)));
				if (uniforms.stream().anyMatch(u -> Pattern.compile("\\b" + u + "\\b").matcher(closure).find())) continue;
				owners.computeIfAbsent(closure, key -> new ArrayList<>()).add(RepoPaths.display(file) + " " + name);
			}
		}
		List<List<String>> shared = owners.values().stream().filter(list -> list.size() > 1).toList();
		assertEquals(List.of(), shared, "move noise that shaders share into shaders/include/noise and #moj_import it");
	}

	@Test
	void everyNoiseIncludeIsImportedAndSelfContained() throws IOException {
		Set<String> includes = new TreeSet<>();
		try (Stream<Path> files = Files.list(NOISE)) {
			files.forEach(path -> includes.add(path.getFileName().toString()));
		}
		assertFalse(includes.isEmpty());
		Set<String> imported = new TreeSet<>();
		for (Path file : shaders()) {
			Matcher matcher = IMPORT.matcher(SourceAssertions.read(file));
			while (matcher.find()) imported.add(matcher.group(1));
		}
		assertEquals(includes, imported, "every noise include is imported, and every import names an include");
		for (String include : includes) {
			String text = SourceAssertions.read(NOISE.resolve(include));
			assertFalse(text.contains("uniform"), include + " reads no uniform; seeded noise stays in its shader");
			assertFalse(text.contains("#moj_import"), include + " imports nothing; shaders list each include they need");
			assertTrue(text.startsWith("// "), include + " says what it is for");
		}
	}

	/** Function bodies by name, whitespace-normalised, comments removed. */
	private static Map<String, String> functions(String text) {
		Map<String, String> bodies = new LinkedHashMap<>();
		Matcher matcher = FUNCTION.matcher(text);
		while (matcher.find()) {
			int depth = 0;
			int end = matcher.end() - 1;
			for (; end < text.length(); end++) {
				char c = text.charAt(end);
				if (c == '{') depth++;
				else if (c == '}' && --depth == 0) break;
			}
			bodies.merge(matcher.group(1), normalise(text.substring(matcher.start(), end + 1)), (a, b) -> a + b);
		}
		return bodies;
	}

	private static String closure(String name, Map<String, String> bodies, Set<String> seen) {
		StringBuilder out = new StringBuilder(bodies.get(name));
		for (String callee : bodies.keySet()) {
			if (seen.contains(callee) || !Pattern.compile("\\b" + callee + "\\(").matcher(bodies.get(name)).find()) continue;
			seen.add(callee);
			out.append('\n').append(closure(callee, bodies, seen));
		}
		return out.toString();
	}

	private static String normalise(String code) {
		String text = code.replaceAll("//[^\\n]*", "").replaceAll("\\s+", " ").trim();
		return text.replaceAll(" ?([(){};,=+\\-*/<>?:\\[\\]]) ?", "$1");
	}

	private static List<Path> shaders() throws IOException {
		try (Stream<Path> files = Files.walk(SHADERS)) {
			return files.filter(path -> path.toString().endsWith(".fsh") || path.toString().endsWith(".vsh"))
					.sorted().toList();
		}
	}
}
