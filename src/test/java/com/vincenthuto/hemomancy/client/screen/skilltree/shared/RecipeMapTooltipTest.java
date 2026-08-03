package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeMapTooltipTest {
	@Test
	void tooltipKeepsTheFullNameAndAuthoredDescriptionThroughLayout() {
		RecipeMapEntry source = new RecipeMapEntry(
				new RecipeMapKey(RecipeMapEntry.Kind.RITE,
						ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/marrow_spitter")),
				"Rite of the Marrow Spitter's Sanguine Attunement",
				"Bind the defeated Marrow Spitter shape to an attuned Marionette Crossbar.",
				2, "Addon Family", 0, true, true);

		RecipeMapEntry laidOut = RecipeMapLayout.build(
				List.of(source), List.of(RecipeMapLayout.MISC_FAMILY)).entries().getFirst();
		RecipeMapTooltip.Content tooltip = RecipeMapTooltip.content(laidOut);

		assertEquals("Rite of the Marrow Spitter's Sanguine Attunement", tooltip.title());
		assertEquals("Bind the defeated Marrow Spitter shape to an attuned Marionette Crossbar.",
				tooltip.description());
		assertEquals("Rite  |  Degree 2  |  Miscellaneous", tooltip.context());
	}

	@Test
	void tooltipWidthIsReadableWithoutSpillingAcrossTheScreen() {
		assertEquals(220, RecipeMapTooltip.maxWidth(900));
		assertEquals(140, RecipeMapTooltip.maxWidth(360));
	}

	@Test
	void craftingTooltipSummarizesTheSameActivationDetailsAsTheInspector() {
		assertEquals("Hold Ferric Binder and activate Hematic Iron Block. Blood cost: 150 mL.",
				CraftingTabController.tooltipDescription(150, "Ferric Binder", "Hematic Iron Block"));
	}

	@Test
	void floorTooltipNamesItsRitualTierInsteadOfCallingItADegree() {
		RecipeMapEntry floor = new RecipeMapEntry(
				new RecipeMapKey(RecipeMapEntry.Kind.FLOOR,
						ResourceLocation.fromNamespaceAndPath("hemomancy", "communion_greater")),
				"Greater Communion Floor", "Focused floor-only construction view.",
				2, "Ritual Floors", 0, true, true);

		assertEquals("Ritual Floor  |  Greater  |  Ritual Floors",
				RecipeMapTooltip.content(floor).context());
	}

	@Test
	void riteExplanationIsHiddenUntilShiftIsHeld() {
		RecipeMapEntry rite = new RecipeMapEntry(
				new RecipeMapKey(RecipeMapEntry.Kind.RITE,
						ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/test")),
				"Test Rite", "A long rite explanation.", 3, "Order", 0, true, true);
		RecipeMapEntry crafting = new RecipeMapEntry(
				new RecipeMapKey(RecipeMapEntry.Kind.CRAFTING,
						ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_structure/test")),
				"Test Craft", "Crafting directions.", 3, "Apparatus", 0, true, true);

		assertEquals("", RecipeMapTooltip.visibleDescription(rite, false));
		assertEquals("A long rite explanation.", RecipeMapTooltip.visibleDescription(rite, true));
		assertEquals("Crafting directions.", RecipeMapTooltip.visibleDescription(crafting, false));
	}

	@Test
	void blueprintCueUsesTheExactTooltipRectangleOnEitherSideOfThePointer() {
		RecipeMapTooltipPositioner right = new RecipeMapTooltipPositioner();
		right.positionTooltip(320, 180, 20, 20, 100, 40);
		assertEquals(new RecipeMapTooltipPositioner.CuePosition(116, 28), right.cuePosition());

		RecipeMapTooltipPositioner left = new RecipeMapTooltipPositioner();
		left.positionTooltip(320, 180, 300, 20, 100, 40);
		assertEquals(new RecipeMapTooltipPositioner.CuePosition(272, 28), left.cuePosition());
	}

	@Test
	void blueprintCueSharesTheInstructionRowWithoutCoveringItsText() {
		int precedingTextHeight = 60;
		int instructionWidth = 100;
		int tooltipWidth = instructionWidth + RecipeMapTooltip.blueprintCueReservedWidth();
		int tooltipHeight = precedingTextHeight + 10 + RecipeMapTooltip.blueprintCueSpacerLines() * 10;
		RecipeMapTooltipPositioner positioner = new RecipeMapTooltipPositioner();
		positioner.positionTooltip(320, 180, 20, 20, tooltipWidth, tooltipHeight);

		assertEquals(8 + precedingTextHeight, positioner.cuePosition().y(),
				"The icon must start on the imprint instruction row");
		assertTrue(positioner.cuePosition().x() >= 32 + instructionWidth,
				"The imprint instruction must reserve horizontal room for the icon");
	}
}
