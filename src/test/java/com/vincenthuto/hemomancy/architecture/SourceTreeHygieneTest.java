package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Phase 1 removed empty classes, empty subscribed handlers and an empty mixin file; keep them from coming back. */
class SourceTreeHygieneTest {
	/** A subscribed handler whose body holds nothing but whitespace and line comments. */
	private static final Pattern EMPTY_HANDLER = Pattern.compile(
			"@SubscribeEvent[^{;]*?\\)\\s*(?:throws [\\w., ]+)?\\{\\s*(?://[^\\n]*\\s*)*}");
	private static final Pattern HANDLER_NAME = Pattern.compile("void\\s+(\\w+)\\s*\\(");

	@Test
	void noEmptyJavaFiles() throws Exception {
		List<String> empty = new ArrayList<>();
		for (Path file : mainJavaFiles()) {
			if (SourceAssertions.read(file).isBlank()) {
				empty.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of(), empty);
	}

	@Test
	void noEmptySubscribedHandlers() throws Exception {
		List<String> empty = new ArrayList<>();
		for (Path file : mainJavaFiles()) {
			Matcher matcher = EMPTY_HANDLER.matcher(SourceAssertions.read(file));
			while (matcher.find()) {
				Matcher name = HANDLER_NAME.matcher(matcher.group());
				empty.add(RepoPaths.display(file) + "#" + (name.find() ? name.group(1) : "?"));
			}
		}
		assertEquals(List.of(), empty);
	}

	@Test
	void mixinConfigListsExactlyTheMixinClasses() throws Exception {
		JsonObject config = ResourceReader.jsonObject("hemomancy.mixins.json");
		String pkg = config.get("package").getAsString();
		Set<String> listed = new TreeSet<>();
		for (String side : List.of("mixins", "client", "server")) {
			JsonArray entries = config.getAsJsonArray(side);
			if (entries != null) {
				entries.forEach(entry -> listed.add(entry.getAsString()));
			}
		}
		Path base = RepoPaths.ROOT.resolve("src/main/java").resolve(pkg.replace('.', '/'));
		Set<String> annotated = new TreeSet<>();
		try (Stream<Path> files = Files.walk(base)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (SourceAssertions.read(file).contains("@Mixin(")) {
					String relative = base.relativize(file).toString().replace('\\', '/');
					annotated.add(relative.substring(0, relative.length() - ".java".length()).replace('/', '.'));
				}
			}
		}
		assertEquals(annotated, listed);
	}

	private static List<Path> mainJavaFiles() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java")).toList();
		}
	}
}
