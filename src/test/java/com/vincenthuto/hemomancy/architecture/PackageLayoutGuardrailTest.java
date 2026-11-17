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

	/** Worldgen is split by generation step; encounter rules and entity geometry live outside it. */
	@Test
	void worldgenIsSplitByGenerationStep() throws Exception {
		assertEquals(List.of("biome", "carver", "feature", "structure", "terrain"),
				packages("common/worldgen"));
		try (Stream<Path> files = Files.list(RepoPaths.mainJava("common/worldgen"))) {
			assertEquals(List.of(), files.filter(path -> path.toString().endsWith(".java"))
					.map(path -> path.getFileName().toString()).toList());
		}
	}

	/**
	 * The core loop overhaul (2026-11-09) was planned as tracks, but its code files by function like everything else:
	 * verbs under {@code manipulation/verb}, living blood under {@code tile}, the ground under {@code world/ground}.
	 * No folder anywhere in source or resources is named for the overhaul itself.
	 */
	@Test
	void noFolderIsNamedForTheCoreLoop() throws Exception {
		List<String> found = new java.util.ArrayList<>();
		for (Path root : List.of(RepoPaths.ROOT.resolve("src/main"), RepoPaths.ROOT.resolve("src/gameTest"),
				RepoPaths.ROOT.resolve("src/test"))) {
			if (!Files.isDirectory(root)) continue;
			try (Stream<Path> dirs = Files.walk(root)) {
				dirs.filter(Files::isDirectory)
						.map(dir -> dir.getFileName().toString().toLowerCase(java.util.Locale.ROOT).replace("_", ""))
						.filter(name -> name.equals("coreloop"))
						.forEach(found::add);
			}
		}
		assertEquals(List.of(), found);
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
