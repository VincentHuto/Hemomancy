package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HematicCommandWiringSourceTest {
	private static final Path ROOT = Path.of("src/main");

	@Test
	void rebukeAndImpressmentAreWiredThroughProgressionAndMemoryWeaving() throws IOException {
		String manipulations = java("common/init/ManipulationInit.java");
		String items = java("common/init/ItemInit.java");
		String tree = java("common/init/ManipulationTreeInit.java");
		String manager = java("common/manipulation/HematicCommandManager.java");

		assertTrue(manipulations.contains("MANIPS.register(\"hematic_rebuke\""));
		assertTrue(manipulations.contains("MANIPS.register(\"hematic_impressment\""));
		assertTrue(items.contains("BASEITEMS.register(\"memory_hematic_rebuke\""));
		assertTrue(items.contains("BASEITEMS.register(\"memory_hematic_impressment\""));
		assertFalse(tree.contains("register(\"hematic_impressment\""));
		assertTrue(tree.contains("register(\"sovereign_instinct\",560,121, \"hematic_rebuke\")"));
		assertTrue(manager.contains("IMPRESSED_BY_CASTER"));
		assertTrue(manager.contains("HemoEntityPredicates.NOBLOOD.test(target)"));
		for (String id : new String[] {"hematic_rebuke", "hematic_impressment"}) {
			assertTrue(Files.isRegularFile(ROOT.resolve("resources/assets/hemomancy/models/item/memory_" + id + ".json")));
			assertTrue(Files.isRegularFile(ROOT.resolve("resources/data/hemomancy/recipe/memory_weaving/memory_" + id + ".json")));
		}
	}

	private static String java(String relative) throws IOException {
		return Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy").resolve(relative));
	}
}
