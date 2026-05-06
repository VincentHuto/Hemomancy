package com.vincenthuto.hemomancy.common.recipe.serializer;

public final class PatternKeyEntryTest {
	private PatternKeyEntryTest() {
	}

	public static void main(String[] args) {
		PatternKeyEntry blockEntry = PatternKeyEntry.fromJsonLiteral("B", "{\"block\":\"minecraft:polished_basalt\"}");
		assertFalse("legacy block entry is not a tag", blockEntry.isTag());
		assertEquals("legacy block id", "minecraft:polished_basalt", blockEntry.blockIdString());
		assertEquals("legacy fallback id", "minecraft:polished_basalt", blockEntry.fallbackIdString());

		PatternKeyEntry tagEntry = PatternKeyEntry.fromJsonLiteral("N",
				"{\"tag\":\"hemomancy:progression/nether_ritual_stone\",\"fallback\":\"minecraft:polished_basalt\"}");
		assertTrue("tag entry is a tag", tagEntry.isTag());
		assertEquals("tag id", "hemomancy:progression/nether_ritual_stone", tagEntry.tagIdString());
		assertEquals("tag fallback id", "minecraft:polished_basalt", tagEntry.fallbackIdString());
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
}
