package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RitualFloorIconTest {
	@Test
	void choosesMostUsedNonFocusSymbol() {
		assertEquals("A", RitualFloorIcon.dominantSymbol(new String[][] {
				{ "ABA" },
				{ "BFB" },
				{ "AAA" }
		}, "F", Set.of("A", "B", "F")));
	}

	@Test
	void tiesKeepTheFirstAuthoredBlock() {
		assertEquals("B", RitualFloorIcon.dominantSymbol(new String[][] {
				{ "BA" },
				{ "AB" }
		}, "F", Set.of("A", "B", "F")));
	}

	@Test
	void ignoresPatternCharactersWithoutRegisteredBlocks() {
		assertEquals("A", RitualFloorIcon.dominantSymbol(new String[][] {
				{ "A  " },
				{ " F " }
		}, "F", Set.of("A", "F", " ")));
	}
}
