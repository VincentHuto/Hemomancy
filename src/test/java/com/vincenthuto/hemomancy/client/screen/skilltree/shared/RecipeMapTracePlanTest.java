package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeMapTracePlanTest {
	private static final int ACCENT = 0xFF8E244D;

	@Test
	void identicalInputsProduceTheSameCachePlan() {
		Fixture fixture = fixture();

		RecipeMapTracePlan first = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT, SkillTreeLayer.SURFACE);
		RecipeMapTracePlan second = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT, SkillTreeLayer.SURFACE);

		assertEquals(first, second);
		assertEquals(5, first.rings().size());
		assertEquals(3, first.spokes().size(), "one spoke per sector boundary, framing the degree-label gap");
		assertEquals(2, first.connections().size());
	}

	@Test
	void degreeRingsUseTheSameOpacityAsSkills() {
		Fixture fixture = fixture();

		RecipeMapTracePlan unfiltered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT, SkillTreeLayer.SURFACE);
		RecipeMapTracePlan filtered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), 1, null, ACCENT, SkillTreeLayer.SURFACE);

		assertEquals(List.of(0x18), unfiltered.rings().stream()
				.map(ring -> ring.color() >>> 24).distinct().toList());
		assertEquals(List.of(0x18), filtered.rings().stream()
				.map(ring -> ring.color() >>> 24).distinct().toList());
	}

	@Test
	void filtersInvalidateThePlanAndRemoveConnectionsWithFilteredEndpoints() {
		Fixture fixture = fixture();
		RecipeMapTracePlan unfiltered = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, null, ACCENT, SkillTreeLayer.SURFACE);
		RecipeMapTracePlan orderOnly = RecipeMapTracePlan.build(
				fixture.layout(), fixture.links(), null, "Order", ACCENT, SkillTreeLayer.SURFACE);

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

		RecipeMapTracePlan plan = RecipeMapTracePlan.build(layout, List.of(link), null, null, ACCENT,
				SkillTreeLayer.SURFACE);

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
		assertEquals(1, deep.connections().stream().filter(connection -> !connection.hubStub()).count());
		assertEquals(List.of(72, 120, 170, 220, 270),
				surface.rings().stream().map(RecipeMapTracePlan.Ring::radius).toList());
		assertEquals(List.of(170, 220, 270, 320),
				deep.rings().stream().map(RecipeMapTracePlan.Ring::radius).toList());
		assertEquals(270.0, spokeEndRadius(layout, surface.spokes().getFirst()), 1.0);
		assertEquals(320.0, spokeEndRadius(layout, deep.spokes().getFirst()), 1.0);
	}

	@Test
	void crossLayerParentsBecomeOneHubStubOnTheDeepLayer() {
		RecipeMapEntry surfaceParent = entry("illuminatus", 4, "Order", true);
		RecipeMapEntry conceptualParent = entry("initiate", 2, "Order", true);
		RecipeMapEntry deepChild = entry("sanctified", 5, "Order", true);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(
				List.of(surfaceParent, conceptualParent, deepChild), List.of("Order"));
		List<RecipeMapLink> links = List.of(
				new RecipeMapLink(conceptualParent.key(), deepChild.key(), RecipeMapLink.Kind.CONCEPTUAL),
				new RecipeMapLink(surfaceParent.key(), deepChild.key(), RecipeMapLink.Kind.PROGRESSION));

		RecipeMapTracePlan surface = RecipeMapTracePlan.build(layout, links, null, null, ACCENT, SkillTreeLayer.SURFACE);
		RecipeMapTracePlan deep = RecipeMapTracePlan.build(layout, links, null, null, ACCENT, SkillTreeLayer.DEEP);

		assertEquals(0, surface.connections().size());
		assertEquals(1, deep.connections().size(), "one stub per deep child, not one per surface parent");
		RecipeMapTracePlan.Connection stub = deep.connections().getFirst();
		RecipeMapLayout.NodeBounds child = layout.node(deepChild.key());
		assertTrue(stub.hubStub());
		assertEquals(RecipeMapLink.Kind.PROGRESSION, stub.kind());
		assertEquals(layout.centerX(), stub.x0());
		assertEquals(layout.centerY(), stub.y0());
		assertEquals(child.centerX(), stub.x1());
		assertEquals(child.centerY(), stub.y1());
	}

	@Test
	void deepChildrenWithAnInLayerParentOrABlockedPathGetNoHubStub() {
		RecipeMapEntry surfaceParent = entry("crimson", 4, "Vessel", true);
		RecipeMapEntry deepParent = entry("ashen", 5, "Vessel", true);
		RecipeMapEntry deepChild = entry("horn", 6, "Vessel", true);
		RecipeMapLayout.Result layout = RecipeMapLayout.build(
				List.of(surfaceParent, deepParent, deepChild), List.of("Vessel"));
		List<RecipeMapLink> links = List.of(
				new RecipeMapLink(surfaceParent.key(), deepParent.key(), RecipeMapLink.Kind.PROGRESSION),
				new RecipeMapLink(deepParent.key(), deepChild.key(), RecipeMapLink.Kind.PROGRESSION),
				new RecipeMapLink(surfaceParent.key(), deepChild.key(), RecipeMapLink.Kind.CONCEPTUAL));

		RecipeMapTracePlan deep = RecipeMapTracePlan.build(layout, links, null, null, ACCENT, SkillTreeLayer.DEEP);

		assertEquals(1, deep.connections().stream().filter(RecipeMapTracePlan.Connection::hubStub).count(),
				"horn already hangs off ashen, so only ashen reaches back to the hub");

		RecipeMapEntry blocked = entry("blocked", 6, "Vessel", true);
		RecipeMapLayout.Result blockedLayout = RecipeMapLayout.build(
				List.of(surfaceParent, deepParent, blocked), List.of("Vessel"));
		RecipeMapTracePlan blockedPlan = RecipeMapTracePlan.build(blockedLayout, List.of(
				new RecipeMapLink(surfaceParent.key(), blocked.key(), RecipeMapLink.Kind.CONCEPTUAL)),
				null, null, ACCENT, SkillTreeLayer.DEEP);

		assertEquals(0, blockedPlan.connections().size(),
				"a stub that would run straight through ashen is left out");
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
