package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class RecipeMapTracePlanTest {
	private static final int ACCENT = 0xFF8E244D;

	@Test
	void identicalInputsProduceTheSameCachePlan() {
		Fixture fixture = fixture();

		RecipeMapTracePlan first = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT);
		RecipeMapTracePlan second = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT);

		assertEquals(first, second);
		assertEquals(9, first.rings().size());
		assertEquals(2, first.spokes().size());
		assertEquals(2, first.connections().size());
	}

	@Test
	void degreeRingsUseTheSameOpacityAsSkills() {
		Fixture fixture = fixture();

		RecipeMapTracePlan unfiltered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT);
		RecipeMapTracePlan filtered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), 1, null, ACCENT);

		assertEquals(List.of(0x18), unfiltered.rings().stream()
				.map(ring -> ring.color() >>> 24).distinct().toList());
		assertEquals(List.of(0x18), filtered.rings().stream()
				.map(ring -> ring.color() >>> 24).distinct().toList());
	}

	@Test
	void filtersInvalidateThePlanAndRemoveConnectionsWithFilteredEndpoints() {
		Fixture fixture = fixture();
		RecipeMapTracePlan unfiltered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT);
		RecipeMapTracePlan orderOnly = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, "Order", ACCENT);

		assertNotEquals(unfiltered, orderOnly);
		assertEquals(1, orderOnly.connections().size());
		assertEquals(RecipeMapLink.Kind.PROGRESSION, orderOnly.connections().getFirst().kind());
	}

	@Test
	void hiddenEndpointsAreNeverBakedIntoTheConnectionLayer() {
		RecipeMapEntry visible = entry("visible", 0, "Order", true);
		RecipeMapEntry hidden = entry("hidden", 1, "Order", false);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(visible, hidden), List.of("Order"));
		RecipeMapLink link = new RecipeMapLink(visible.key(), hidden.key(), RecipeMapLink.Kind.PROGRESSION);

		RecipeMapTracePlan plan = RecipeMapTracePlan.build(layout, List.of(link), null, null, ACCENT);

		assertEquals(0, plan.connections().size());
	}

	@Test
	void eachCachedLayerContainsOnlyItsOwnDegreeRingsAndConnections() {
		RecipeMapEntry surfaceRoot = entry("surface_root", 0, "Order", true);
		RecipeMapEntry surfaceEdge = entry("surface_edge", 4, "Order", true);
		RecipeMapEntry deepRoot = entry("deep_root", 5, "Order", true);
		RecipeMapEntry deepEdge = entry("deep_edge", 8, "Order", true);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(
				List.of(surfaceRoot, surfaceEdge, deepRoot, deepEdge), List.of("Order"));
		List<RecipeMapLink> links = List.of(
				new RecipeMapLink(surfaceRoot.key(), surfaceEdge.key(), RecipeMapLink.Kind.PROGRESSION),
				new RecipeMapLink(surfaceEdge.key(), deepRoot.key(), RecipeMapLink.Kind.PROGRESSION),
				new RecipeMapLink(deepRoot.key(), deepEdge.key(), RecipeMapLink.Kind.PROGRESSION));

		RecipeMapTracePlan surface = RecipeMapTracePlan.build(
				layout, links, null, null, ACCENT, SkillTreeLayer.SURFACE);
		RecipeMapTracePlan deep = RecipeMapTracePlan.build(
				layout, links, null, null, ACCENT, SkillTreeLayer.DEEP);

		assertEquals(5, surface.rings().size());
		assertEquals(4, deep.rings().size());
		assertEquals(1, surface.connections().size());
		assertEquals(1, deep.connections().size());
		assertEquals(List.of(72, 120, 170, 220, 270),
				surface.rings().stream().map(RecipeMapTracePlan.Ring::radius).toList());
		assertEquals(List.of(120, 170, 220, 270),
				deep.rings().stream().map(RecipeMapTracePlan.Ring::radius).toList());
		assertEquals(270.0, spokeEndRadius(layout, surface.spokes().getFirst()), 1.0);
		assertEquals(270.0, spokeEndRadius(layout, deep.spokes().getFirst()), 1.0);
	}

	private static double spokeEndRadius(RecipeMapLayout.Result layout, RecipeMapTracePlan.Line spoke) {
		return Math.hypot(spoke.x1() - layout.centerX(), spoke.y1() - layout.centerY());
	}

	private static Fixture fixture() {
		RecipeMapEntry orderRoot = entry("order_root", 0, "Order", true);
		RecipeMapEntry orderChild = entry("order_child", 1, "Order", true);
		RecipeMapEntry vessel = entry("vessel", 1, "Vessel", true);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(
				List.of(orderRoot, orderChild, vessel), List.of("Order", "Vessel"));
		return new Fixture(layout, List.of(
				new RecipeMapLink(orderRoot.key(), orderChild.key(), RecipeMapLink.Kind.PROGRESSION),
				new RecipeMapLink(orderRoot.key(), vessel.key(), RecipeMapLink.Kind.CONCEPTUAL)));
	}

	private static RecipeMapEntry entry(String path, int degree, String family, boolean visible) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/" + path);
		return new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, id), path,
				degree, family, 0, visible, true);
	}

	private record Fixture(RecipeMapLayout.Result layout, List<RecipeMapLink> links) {}
}
