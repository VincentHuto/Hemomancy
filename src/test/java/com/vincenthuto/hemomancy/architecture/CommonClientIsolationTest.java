package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * V-8 (step 8): nothing under {@code common/} names a {@code net.minecraft.client} or
 * {@code com.vincenthuto.hemomancy.client} class, so a dedicated server never loads one. The one allowed place is an
 * item client extension: a {@code HemoClientItemExtensionsProvider} may build its renderer or pose inside
 * {@code hemomancy$getClientItemExtensions()} or inside an {@code IClientItemExtensions} class or anonymous class in the
 * same file, which only the client's {@code ClientExtensionsRegistrar} ever calls.
 *
 * <p>Client-bound payload handlers live in {@code client/network} behind {@code ClientPayloadHandlers}; other client
 * actions (screens, the local player, tooltip keys) go through {@code common/util/ClientBridge}.
 */
class CommonClientIsolationTest {
	private static final Pattern CLIENT_NAME = Pattern.compile(
			"\\b(?:net\\.minecraft\\.client|com\\.vincenthuto\\.hemomancy\\.client)\\.[\\w.]*");
	private static final Pattern CLIENT_IMPORT = Pattern.compile(
			"(?m)^import\\s+(?:static\\s+)?((?:net\\.minecraft\\.client|com\\.vincenthuto\\.hemomancy\\.client)\\.[\\w.]+)\\s*;");
	private static final String EXTENSION_PROVIDER = "HemoClientItemExtensionsProvider";

	@Test
	void commonCodeNamesClientClassesOnlyInsideItemClientExtensions() throws Exception {
		List<String> violations = new ArrayList<>();
		Set<String> extensionFiles = new TreeSet<>();
		for (Path file : commonFiles()) {
			String text = SourceAssertions.read(file);
			String code = stripCommentsAndStrings(text);
			if (!CLIENT_NAME.matcher(code).find()) {
				continue;
			}
			String name = RepoPaths.display(file).replace('\\', '/');
			if (Pattern.compile("(?m)^import\\s+(?:net\\.minecraft\\.client|com\\.vincenthuto\\.hemomancy\\.client)[\\w.]*\\.\\*")
					.matcher(code).find()) {
				violations.add(name + ": wildcard client import (name each class so its uses can be checked)");
				continue;
			}
			if (!code.contains(EXTENSION_PROVIDER)) {
				violations.add(name + ": not an item client extension");
				continue;
			}
			extensionFiles.add(name);
			List<int[]> spans = extensionSpans(code);
			Set<String> simpleNames = new TreeSet<>();
			Matcher imports = CLIENT_IMPORT.matcher(code);
			while (imports.find()) {
				String imported = imports.group(1);
				simpleNames.add(imported.substring(imported.lastIndexOf('.') + 1));
			}
			// Blank the imports in place, so offsets still line up with the spans.
			String body = Pattern.compile("(?m)^import\\s.*$").matcher(code)
					.replaceAll(match -> " ".repeat(match.group().length()));
			for (String simple : simpleNames) {
				Matcher use = Pattern.compile("\\b" + Pattern.quote(simple) + "\\b").matcher(body);
				while (use.find()) {
					if (!inside(spans, use.start())) {
						violations.add(name + ": " + simple + " outside the client extension");
						break;
					}
				}
			}
			Matcher qualified = CLIENT_NAME.matcher(body);
			while (qualified.find()) {
				if (!inside(spans, qualified.start())) {
					violations.add(name + ": " + qualified.group() + " outside the client extension");
				}
			}
		}
		assertEquals(List.of(), violations);
		assertTrue(extensionFiles.size() > 40, "expected the item client extensions to be found: " + extensionFiles.size());
	}

	/** The client half of a client-bound payload is registered once on the client. */
	@Test
	void everyDispatchedPayloadHasOneClientHandler() throws Exception {
		String handler = SourceAssertions.readMainJava("common/network/PacketHandler.java");
		Set<String> dispatched = new TreeSet<>();
		Matcher registration = Pattern.compile(
				"net\\.play\\w+\\(\\s*(?:[\\w.]+\\.)?(\\w+)\\.TYPE\\s*,[^;]*?ClientPayloadHandlers::dispatch", Pattern.DOTALL)
				.matcher(handler);
		while (registration.find()) {
			dispatched.add(registration.group(1));
		}
		List<String> registered = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve("client"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher client = Pattern.compile("ClientPayloadHandlers\\.register\\(\\s*(\\w+)\\.TYPE")
						.matcher(SourceAssertions.read(file));
				while (client.find()) {
					registered.add(client.group(1));
				}
			}
		}
		// The bidirectional Antecedent effect dispatches its client half from its own handler.
		Set<String> expected = new TreeSet<>(dispatched);
		expected.add("AntecedentEffectPacket");
		assertEquals(new ArrayList<>(expected), registered.stream().sorted().toList());
		assertTrue(dispatched.size() > 60, "dispatched payloads: " + dispatched.size());
	}

	private static boolean inside(List<int[]> spans, int at) {
		for (int[] span : spans) {
			if (at >= span[0] && at < span[1]) {
				return true;
			}
		}
		return false;
	}

	/** Bodies of the extension method and of every IClientItemExtensions class or anonymous class. */
	private static List<int[]> extensionSpans(String code) {
		List<int[]> spans = new ArrayList<>();
		Matcher start = Pattern.compile(
				"hemomancy\\$getClientItemExtensions\\s*\\([^)]*\\)\\s*\\{|implements\\s+IClientItemExtensions\\b[^{]*\\{"
						+ "|new\\s+IClientItemExtensions\\s*\\(\\s*\\)\\s*\\{")
				.matcher(code);
		while (start.find()) {
			int open = start.end() - 1;
			int depth = 0;
			for (int i = open; i < code.length(); i++) {
				char c = code.charAt(i);
				if (c == '{') {
					depth++;
				} else if (c == '}' && --depth == 0) {
					spans.add(new int[] { open, i + 1 });
					break;
				}
			}
		}
		return spans;
	}

	/** Blanks comments and string literals, keeping offsets, so javadoc links and text are not references. */
	private static String stripCommentsAndStrings(String text) {
		StringBuilder out = new StringBuilder(text);
		Matcher matcher = Pattern.compile("/\\*.*?\\*/|//[^\\n]*|\"(?:\\\\.|[^\"\\\\\\n])*\"", Pattern.DOTALL)
				.matcher(text);
		while (matcher.find()) {
			for (int i = matcher.start(); i < matcher.end(); i++) {
				if (out.charAt(i) != '\n') {
					out.setCharAt(i, ' ');
				}
			}
		}
		return out.toString();
	}

	private static List<Path> commonFiles() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve("common"))) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
		}
	}
}
