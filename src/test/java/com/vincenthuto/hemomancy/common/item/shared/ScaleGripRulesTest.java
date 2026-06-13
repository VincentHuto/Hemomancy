package com.vincenthuto.hemomancy.common.item.shared;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ScaleGripRulesTest {
	private ScaleGripRulesTest() {
	}

	public static void main(String[] args) {
		matchesOneGripAndOneUnprotectedTarget();
		rejectsLooseGripCharmBehavior();
		rejectsAlreadyProtectedTarget();
		recipeSerializerDoesNotUseIdentityUnitCodec();
	}

	private static void matchesOneGripAndOneUnprotectedTarget() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true, false),
				new ScaleGripRules.Candidate(1, "minecraft:diamond_pickaxe", false, false, false));
		assertTrue(ScaleGripRules.matchesBindingCraft(candidates), "one grip and one unprotected item should bind");
	}

	private static void rejectsLooseGripCharmBehavior() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true, false));
		assertFalse(ScaleGripRules.matchesBindingCraft(candidates), "loose grip alone should not protect inventory");
	}

	private static void rejectsAlreadyProtectedTarget() {
		List<ScaleGripRules.Candidate> candidates = List.of(
				new ScaleGripRules.Candidate(0, "hemomancy:scale_grip", false, true, false),
				new ScaleGripRules.Candidate(1, "minecraft:diamond_pickaxe", false, false, true));
		assertFalse(ScaleGripRules.matchesBindingCraft(candidates), "already protected target should not bind twice");
	}

	private static void recipeSerializerDoesNotUseIdentityUnitCodec() {
		try {
			String source = Files.readString(Path.of(
					"src/main/java/com/vincenthuto/hemomancy/common/recipe/ScaleGripBindingRecipe.java"));
			assertFalse(source.contains("StreamCodec.unit(new ScaleGripBindingRecipe())"),
					"recipe sync must encode any scale grip recipe instance, not one unit instance");
			assertFalse(source.contains("MapCodec.unit(new ScaleGripBindingRecipe())"),
					"recipe json codec must encode any scale grip recipe instance, not one unit instance");
		} catch (Exception e) {
			throw new AssertionError("could not inspect scale grip recipe serializer source", e);
		}
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertFalse(boolean value, String label) {
		if (value) {
			throw new AssertionError(label);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
	}
}
