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
		votaryRiteUsesLayeredStation();
		sanguineInitiationAndVotaryRemainDistinct();
	}

	private static void sanguineInitiationHasNoDegreeRequirement() throws IOException {
		String initiation = read("hemomancy/recipe/cardinal_rite/sanguine_initiation.json");
		assertContains("sanguine initiation required degree", initiation, "\"required_degree\": 0");
	}

	private static void sanguineInitiationAndVotaryRemainDistinct() throws IOException {
		String initiation = read("hemomancy/recipe/cardinal_rite/sanguine_initiation.json");
		String votary = read("hemomancy/recipe/cardinal_rite/votary_rite.json");
		assertContains("sanguine initiation floor", initiation, "\"floor\": \"hemomancy:threshold_minor\"");
		assertContains("votary floor", votary, "\"floor\": \"hemomancy:threshold_lesser\"");
		assertContains("sanguine initiation uses temple medium", initiation, "\"focus\": \"temple_medium\"");
		assertContains("votary uses hematic medium", votary, "\"focus\": \"hematic_medium\"");
		assertContains("sanguine initiation has no offerings", initiation, "\"brazier_signature\": []");
		assertContains("votary has no offerings", votary, "\"brazier_signature\": []");
		assertFalse(initiation.contains("\"required_structure\""),
				"sanguine initiation should be structureless");
	}

	private static void votaryRiteUsesLayeredStation() throws IOException {
		String votary = read("hemomancy/recipe/cardinal_rite/votary_rite.json");
		assertContains("votary rite tier", votary, "\"riteType\": \"minor\"");
		assertContains("votary required structure", votary, "\"required_structure\"");
		assertContains("votary keyed pillar", votary,
				"\"block\": \"hemomancy:hematic_iron_pillar\"");
		assertFalse(votary.contains("\"consume_on_success\": true"),
				"votary upper structure should default to reusable");
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
