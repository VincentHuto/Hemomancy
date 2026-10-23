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
 * V-8 (step 8), O-6 (step 13): nothing under {@code common/} names a {@code net.minecraft.client} or
 * {@code com.vincenthuto.hemomancy.client} class, so a dedicated server never loads one. Item renderers and armour
 * models are registered from {@code client/item/ItemClientExtensions}; client-bound payload handlers live in
 * {@code client/network} behind {@code ClientPayloadHandlers}; other client actions (screens, the local player,
 * tooltip keys) go through {@code common/util/ClientBridge}.
 */
class CommonClientIsolationTest {
	private static final Pattern CLIENT_NAME = Pattern.compile(
			"\\b(?:net\\.minecraft\\.client|com\\.vincenthuto\\.hemomancy\\.client)\\.[\\w.]*");

	@Test
	void commonCodeNamesNoClientClass() throws Exception {
		List<String> violations = new ArrayList<>();
		for (Path file : commonFiles()) {
			Matcher client = CLIENT_NAME.matcher(stripCommentsAndStrings(SourceAssertions.read(file)));
			if (client.find()) violations.add(RepoPaths.display(file) + ": " + client.group());
		}
		assertEquals(List.of(), violations);
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
