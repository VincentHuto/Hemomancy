package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.ArrayList;
import java.util.List;

public final class BloodCraftingPatternSearchRulesTest {
	private BloodCraftingPatternSearchRulesTest() {
	}

	public static void main(String[] args) {
		estimatesLargeRitesAsMuchMoreExpensiveThanSmallRites();
		sortsSmallPatternsBeforeLargePatterns();
		skipsPatternsThatDoNotContainTheClickedSymbol();
	}

	private static void estimatesLargeRitesAsMuchMoreExpensiveThanSmallRites() {
		long small = BloodCraftingPatternSearchRules.estimatedWorstCaseBlockChecks(3, 3, 1);
		long large = BloodCraftingPatternSearchRules.estimatedWorstCaseBlockChecks(9, 9, 9);

		assertTrue("9x9x9 search should be dramatically more expensive than 3x3x1",
				large > small * 100L);
	}

	private static void sortsSmallPatternsBeforeLargePatterns() {
		List<FakePattern> patterns = new ArrayList<>(List.of(
				new FakePattern("grand", 9, 9, 9),
				new FakePattern("minor", 3, 3, 1),
				new FakePattern("lesser", 5, 5, 5)));

		patterns.sort((left, right) -> BloodCraftingPatternSearchRules.compareDimensionsBySearchCost(
				left.width, left.height, left.depth,
				right.width, right.height, right.depth));

		assertEquals("first pattern", "minor", patterns.get(0).name);
		assertEquals("last pattern", "grand", patterns.get(2).name);
	}

	private static void skipsPatternsThatDoNotContainTheClickedSymbol() {
		String[][] pattern = {
				{"ABA", "BCB", "ABA"}
		};

		assertTrue("pattern should include catalyst symbol",
				BloodCraftingPatternSearchRules.patternUsesSymbol(pattern, "C"));
		assertFalse("pattern should skip unrelated symbol",
				BloodCraftingPatternSearchRules.patternUsesSymbol(pattern, "Z"));
	}

	private static void assertEquals(String label, String expected, String actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}

	private record FakePattern(String name, int width, int height, int depth) {
	}
}
