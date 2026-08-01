package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RecipeMapControlsLayoutTest {
	@Test
	void filtersStackInTheUpperLeftGapWithoutTheRemovedSearchRow() {
		RecipeMapInspectorLayout.IntRect viewport =
				new RecipeMapInspectorLayout.IntRect(16, 16, 900, 500);

		RecipeMapControlsLayout.Result layout = RecipeMapControlsLayout.calculate(viewport);

		assertEquals(new RecipeMapInspectorLayout.IntRect(24, 21, 68, 13), layout.degree());
		assertEquals(new RecipeMapInspectorLayout.IntRect(24, 38, 68, 13), layout.family());
		assertEquals(new RecipeMapInspectorLayout.IntRect(24, 55, 68, 13), layout.layer());
	}

	@Test
	void recentsShareTheBottomLeftControlLineBesideHomeAndZoom() {
		RecipeMapInspectorLayout.IntRect viewport =
				new RecipeMapInspectorLayout.IntRect(16, 16, 900, 500);

		RecipeMapControlsLayout.Result layout = RecipeMapControlsLayout.calculate(viewport);

		assertEquals(74, layout.recentLabelX());
		assertEquals(500, layout.recentBaselineY());
		assertEquals(new RecipeMapInspectorLayout.IntRect(113, 498, 18, 14), layout.firstRecent());
	}
}
