package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class HeartManipulationWiringSourceTest {
	private static final Path ROOT = Path.of("src/main");

	@Test
	void heartManipulationsAreWiredThroughCastingDamageHudAndMemoryWeaving() throws IOException {
		String manipulations = java("common/init/ManipulationInit.java");
		String items = java("common/init/ItemInit.java");
		String client = java("client/event/ManipulationChargeInput.java");
		String packet = java("common/network/capa/harbinger/manips/UseManipKeyPacket.java");

		assertTrue(manipulations.contains("MANIPS.register(\"ironhearted\""));
		assertTrue(manipulations.contains("MANIPS.register(\"blackhearted\""));
		assertTrue(items.contains("registerBloodMemoryItem(\"memory_ironhearted\", ManipulationInit.ironhearted)"));
		assertTrue(items.contains("registerBloodMemoryItem(\"memory_blackhearted\", ManipulationInit.blackhearted)"));
		assertTrue(client.contains("type == EnumManipulationType.CHARGED"));
		assertTrue(packet.contains("player.blockPosition(), pTic"));
		assertTrue(java("common/manipulation/mortem/BlackheartedEvents.java")
				.contains("source.is(DamageTypes.WITHER)"));
		for (String id : new String[] {"ironhearted", "blackhearted"}) {
			assertTrue(Files.isRegularFile(ROOT.resolve("resources/assets/hemomancy/models/item/memory_" + id + ".json")));
			assertTrue(Files.isRegularFile(ROOT.resolve("resources/data/hemomancy/recipe/memory_weaving/memory_" + id + ".json")));
		}
	}

	private static String java(String relative) throws IOException {
		return Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy").resolve(relative));
	}
}
