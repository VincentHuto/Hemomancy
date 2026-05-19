package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class CardinalRiteRecipeDataTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources/data");

	private CardinalRiteRecipeDataTest() {
	}

	public static void main(String[] args) throws IOException {
		sanguineInitiationHasNoDegreeRequirement();
		votaryRiteUsesApprovedObsidianLayout();
		sanguineInitiationLayoutDoesNotAlsoMatchVotaryRite();
	}

	private static void sanguineInitiationHasNoDegreeRequirement() throws IOException {
		String initiation = read("hemomancy/recipe/cardinal_rite/sanguine_initiation.json");
		assertContains("sanguine initiation required degree", initiation, "\"required_degree\": 0");
	}

	private static void sanguineInitiationLayoutDoesNotAlsoMatchVotaryRite() throws IOException {
		String initiation = read("hemomancy/recipe/cardinal_rite/sanguine_initiation.json");
		String votary = read("hemomancy/recipe/cardinal_rite/votary_rite.json");
		assertContains("sanguine initiation corner block", initiation, "\"block\": \"minecraft:stone_bricks\"");
		assertContains("sanguine initiation 3x3 shape", initiation, "\"CEC\"");
		assertFalse(votary.contains("\"PEP\""), "votary_rite should no longer use the old 3x3 shape");
		assertFalse(votary.contains("\"tag\""), "votary_rite should use exact blocks, not a broad ritual-stone tag");
	}

	private static void votaryRiteUsesApprovedObsidianLayout() throws IOException {
		String votary = read("hemomancy/recipe/cardinal_rite/votary_rite.json");
		assertContains("votary rite tier", votary, "\"riteType\": \"lesser\"");
		assertContains("votary obsidian key", votary, "\"block\": \"minecraft:obsidian\"");
		assertContains("votary top row", votary, "\"OEOEO\"");
		assertContains("votary shoulder row", votary, "\"E H E\"");
		assertContains("votary center row", votary, "\"OHBHO\"");
		assertMatches("votary top row should be its own ground-depth aisle",
				votary, "\"OEOEO\"\\s*]\\s*,\\s*\\[\\s*\"E H E\"");
		assertMatches("votary center row should be its own ground-depth aisle",
				votary, "\"E H E\"\\s*]\\s*,\\s*\\[\\s*\"OHBHO\"");
		assertMatches("votary lower shoulder row should be its own ground-depth aisle",
				votary, "\"OHBHO\"\\s*]\\s*,\\s*\\[\\s*\"E H E\"");
	}

	private static String read(String path) throws IOException {
		return Files.readString(RESOURCE_ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertMatches(String label, String text, String regex) {
		if (!Pattern.compile(regex).matcher(text).find()) {
			throw new AssertionError(label + ": missing pattern " + regex);
		}
	}

	private static void assertFalse(boolean value, String message) {
		if (value) {
			throw new AssertionError(message);
		}
	}
}
