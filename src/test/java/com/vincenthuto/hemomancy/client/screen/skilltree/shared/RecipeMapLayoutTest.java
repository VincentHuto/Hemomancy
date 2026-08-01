package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeMapLayoutTest {
	private static final ResourceLocation FIRST = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/first");
	private static final ResourceLocation HIDDEN = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/hidden");
	private static final ResourceLocation LAST = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/last");

	@Test
	void degreesUseConcentricRingsAndHiddenNodesKeepTheirAuthoredSlot() {
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(
				entry(FIRST, "First", 0, "Order", 0, true),
				entry(HIDDEN, "Hidden", 1, "Order", 0, false),
				entry(LAST, "Last", 1, "Order", 1, true)), List.of("Order"));

		RecipeMapLayout.NodeBounds first = layout.node(FIRST);
		RecipeMapLayout.NodeBounds hidden = layout.node(HIDDEN);
		RecipeMapLayout.NodeBounds last = layout.node(LAST);
		assertEquals(72.0, distanceFromCenter(layout, first), 1.0);
		assertEquals(120.0, distanceFromCenter(layout, hidden), 1.0);
		assertEquals(120.0, distanceFromCenter(layout, last), 1.0);
		assertTrue(hidden.centerX() != last.centerX() || hidden.centerY() != last.centerY(),
				"authored slots on the same family ring must remain distinct");
		assertEquals(LAST, layout.nodeAt(last.centerX(), last.centerY()).entry().id());
		assertNull(layout.visibleNodeAt(hidden.centerX(), hidden.centerY()));
	}

	@Test
	void familiesOccupyDistinctAngularSectorsOnTheSameDegreeRing() {
		ResourceLocation vessel = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/vessel");
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(
				entry(FIRST, "Order", 3, "Order", 0, true),
				entry(vessel, "Vessel", 3, "Vessel", 0, true)), List.of("Order", "Vessel"));

		RecipeMapLayout.NodeBounds orderNode = layout.node(FIRST);
		RecipeMapLayout.NodeBounds vesselNode = layout.node(vessel);
		assertEquals(220.0, distanceFromCenter(layout, orderNode), 1.0);
		assertEquals(220.0, distanceFromCenter(layout, vesselNode), 1.0);
		assertTrue(Math.hypot(orderNode.centerX() - vesselNode.centerX(),
				orderNode.centerY() - vesselNode.centerY()) > RecipeMapLayout.NODE_SIZE * 2.0);
	}

	@Test
	void deepDegreesReuseTheCompactRingFootprint() {
		ResourceLocation degreeFive = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/degree_five");
		ResourceLocation degreeEight = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/degree_eight");
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(
				entry(degreeFive, "Degree Five", 5, "Order", 0, true),
				entry(degreeEight, "Degree Eight", 8, "Order", 0, true)), List.of("Order"));

		assertEquals(120.0, distanceFromCenter(layout, layout.node(degreeFive)), 1.0);
		assertEquals(270.0, distanceFromCenter(layout, layout.node(degreeEight)), 1.0);
	}

	@Test
	void unknownFamiliesFallBackToMiscellaneousWithoutLosingTheEntry() {
		RecipeMapEntry unknown = entry(FIRST, "Addon Rite", 2, "Addon Family", 0, true);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(unknown), List.of("Order", RecipeMapLayout.MISC_FAMILY));

		assertEquals(RecipeMapLayout.MISC_FAMILY, layout.node(FIRST).entry().family());
		assertTrue(layout.contentWidth() > 0);
		assertTrue(layout.contentHeight() > 0);
	}

	private static RecipeMapEntry entry(ResourceLocation id, String name, int degree,
			String family, int order, boolean visible) {
		return new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, id), name,
				degree, family, order, visible, true);
	}

	private static double distanceFromCenter(RecipeMapLayout.Result layout, RecipeMapLayout.NodeBounds node) {
		return Math.hypot(node.centerX() - layout.centerX(), node.centerY() - layout.centerY());
	}
}
