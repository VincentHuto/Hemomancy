package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnstainedChurchEntryResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void churchTemplateUsesStarterLootWithGuaranteedEntryItems() throws Exception {
		String template = readCompressed(RESOURCES.resolve("data/hemomancy/structure/unstained_church.nbt"));
		String loot = Files.readString(RESOURCES.resolve(
				"data/hemomancy/loot_table/chests/unstained_church.json")).replaceAll("\\s+", "");

		assertTrue(template.contains("minecraft:chest"));
		assertTrue(template.contains("hemomancy:chests/unstained_church"));
		assertTrue(loot.contains("\"rolls\":1,\"bonus_rolls\":0,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"hemomancy:hemolytic_solution\""));
		assertTrue(loot.contains("\"min\":2,\"max\":2"));
		assertTrue(loot.contains("\"name\":\"hemomancy:liber_immaculatus\""));
		assertFalse(loot.contains("tome_of_the_unstained"));
	}

	@Test
	void canonicalGuideStatesTheExactEntrySequenceWithoutFalseHemolysisClaim() throws Exception {
		String firstSteps = Files.readString(RESOURCES.resolve(
				"data/hemomancy/books/liberimmaculatus/intro/pages/first_steps.json"));
		String solution = Files.readString(RESOURCES.resolve(
				"data/hemomancy/books/liberimmaculatus/sacred_tools/pages/hemolytic_solution.json"));

		for (String instruction : new String[] { "two Hemolytic Solutions", "Unstained Podium",
				"Cleansed Stone", "Lethean Baptism", "right-click" }) {
			assertTrue(firstSteps.contains(instruction), "missing entry instruction: " + instruction);
		}
		assertFalse(solution.contains("Drinking it applies the Hemolysis effect"));
	}

	private static String readCompressed(Path path) throws Exception {
		try (var input = new GZIPInputStream(new ByteArrayInputStream(Files.readAllBytes(path)))) {
			return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
		}
	}
}
