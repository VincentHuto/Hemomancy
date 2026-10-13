package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class RecipeMapCanvasLabelTest {
	@Test
	void longFamilyNamesWrapAtTheirSlashOrMiddleSpace() {
		assertEquals(List.of("Order"), RecipeMapCanvas.familyLabelLines("Order", text -> text.length() * 7));
		assertEquals(List.of("Qliphoth/", "Forbidden"),
				RecipeMapCanvas.familyLabelLines("Qliphoth/Forbidden", text -> text.length() * 7));
		assertEquals(List.of("Ritual", "Infrastructure"),
				RecipeMapCanvas.familyLabelLines("Ritual Infrastructure", text -> text.length() * 7));
	}

	@Test
	void labelsGrowAwayFromTheHub() {
		assertArrayEquals(new int[] {300, 95}, RecipeMapCanvas.familyLabelOrigin(0.0, 300, 100, 60, 10),
				"east labels start at the anchor");
		assertArrayEquals(new int[] {240, 95}, RecipeMapCanvas.familyLabelOrigin(Math.PI, 300, 100, 60, 10),
				"west labels end at the anchor");
		assertArrayEquals(new int[] {270, 90}, RecipeMapCanvas.familyLabelOrigin(-Math.PI / 2, 300, 100, 60, 10),
				"north labels sit above the anchor");
		assertArrayEquals(new int[] {270, 100}, RecipeMapCanvas.familyLabelOrigin(Math.PI / 2, 300, 100, 60, 10),
				"south labels hang below the anchor");
	}

	@Test
	void openingViewFramesThePlayersNextDegreeOnTheLayer() {
		assertEquals(RecipeMapLayout.ringRadius(1), RecipeMapCanvas.focusRadius(0, SkillTreeLayer.SURFACE));
		assertEquals(RecipeMapLayout.ringRadius(4), RecipeMapCanvas.focusRadius(7, SkillTreeLayer.SURFACE));
		assertEquals(RecipeMapLayout.ringRadius(5), RecipeMapCanvas.focusRadius(2, SkillTreeLayer.DEEP));
		assertEquals(RecipeMapLayout.ringRadius(7), RecipeMapCanvas.focusRadius(6, SkillTreeLayer.DEEP));
		assertEquals(RecipeMapLayout.ringRadius(8), RecipeMapCanvas.focusRadius(8, SkillTreeLayer.DEEP));
	}
}
