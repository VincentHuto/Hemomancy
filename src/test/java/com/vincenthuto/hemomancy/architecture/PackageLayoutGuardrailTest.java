package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code common/} and {@code client/} are organised by function. A feature (the circus, the antecedent recordings,
 * succession, Earthen Vein travel) gets a named sub-package inside the function packages, never a top-level folder
 * that holds its blocks, items, payloads and state together. Add a top-level package here only for a new function.
 */
class PackageLayoutGuardrailTest {
	private static final List<String> COMMON = List.of("armor", "block", "brewing", "capability", "command", "damage",
			"data", "effect", "enchanting", "encounter", "entity", "event", "init", "item", "loot", "manipulation", "menu",
			"mission", "network", "particle", "recipe", "rite", "testing", "tile", "util", "world", "worldgen");
	private static final List<String> CLIENT = List.of("data", "event", "item", "model", "network", "particle", "player",
			"registry", "render", "screen", "sound");

	@Test
	void commonTopLevelPackagesAreFunctions() throws Exception {
		assertEquals(COMMON, packages("common"));
	}

	@Test
	void clientTopLevelPackagesAreFunctions() throws Exception {
		assertEquals(CLIENT, packages("client"));
	}

	private static List<String> packages(String side) throws Exception {
		try (Stream<Path> dirs = Files.list(RepoPaths.mainJava(side))) {
			return dirs.filter(Files::isDirectory)
					.filter(PackageLayoutGuardrailTest::holdsJava)
					.map(dir -> dir.getFileName().toString())
					.sorted()
					.toList();
		}
	}

	private static boolean holdsJava(Path dir) {
		try (Stream<Path> files = Files.walk(dir)) {
			return files.anyMatch(path -> path.toString().endsWith(".java"));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
