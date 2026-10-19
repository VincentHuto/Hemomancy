package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

final class RecipeMapLayoutInvariantTest {
	private static final double MIN_CENTRE_DISTANCE = RecipeMapLayout.NODE_SIZE + 10;

	@Test
	void sixEntriesInOneFamilyDegreeCellStayAtLeast38PxApart() {
		List<RecipeMapEntry> entries = new ArrayList<>();
		for (int index = 0; index < 6; index++) entries.add(entry("cell_" + index, 1, "Order", index, true));

		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, List.of("Order"));

		assertMinimumSpacing(layout, entries);
	}

	@Test
	void crowdedCellsSplitIntoTwoStaggeredRowsAndKeepTheirSpacing() {
		List<RecipeMapEntry> entries = new ArrayList<>();
		List<String> families = new ArrayList<>();
		for (int index = 0; index < 14; index++) entries.add(entry("crowded_" + index, 5, "Crowded", index, true));
		families.add("Crowded");
		for (int family = 0; family < 8; family++) {
			families.add("Family " + family);
			for (int index = 0; index < 3; index++) {
				entries.add(entry("f" + family + "_" + index, 5, "Family " + family, index, true));
			}
		}

		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, families);

		assertMinimumSpacing(layout, entries);
		int ring = RecipeMapLayout.ringRadius(5);
		Set<Integer> rows = new TreeSet<>();
		for (int index = 0; index < 14; index++) {
			double radius = distanceFromCenter(layout, layout.node(id("crowded_" + index)));
			int row = radius < ring ? -1 : 1;
			rows.add(row);
			assertEquals(ring + row * RecipeMapLayout.STAGGER_OFFSET, radius, 1.0,
					"staggered rows sit just inside and outside the degree ring");
		}
		assertEquals(Set.of(-1, 1), rows, "an overfull cell uses both rows");
	}

	@Test
	void hiddenAndLockedEntriesNeverShiftVisibleOnes() {
		List<RecipeMapEntry> revealed = new ArrayList<>();
		List<RecipeMapEntry> veiled = new ArrayList<>();
		String[] families = {"Order", "Vessel", "Ichorian Sigils"};
		for (int index = 0; index < 12; index++) {
			String family = families[index % families.length];
			int degree = index % 9;
			boolean hidden = index % 3 == 0;
			revealed.add(entry("entry_" + index, degree, family, index, true));
			veiled.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, id("entry_" + index)),
					"entry_" + index, degree, family, index, !hidden, index % 2 == 0));
		}

		RecipeMapLayout.Result full = RecipeMapLayout.build(revealed, List.of(families));
		RecipeMapLayout.Result playerView = RecipeMapLayout.build(veiled, List.of(families));

		for (RecipeMapEntry entry : veiled) {
			RecipeMapLayout.NodeBounds expected = full.node(entry.key());
			RecipeMapLayout.NodeBounds actual = playerView.node(entry.key());
			assertEquals(expected.centerX(), actual.centerX(), entry.id() + " x");
			assertEquals(expected.centerY(), actual.centerY(), entry.id() + " y");
		}
		assertEquals(full.sectors(), playerView.sectors());
		assertEquals(full.contentWidth(), playerView.contentWidth());
	}

	@Test
	void sectorWidthFollowsTheFamilysWidestCell() {
		List<RecipeMapEntry> entries = new ArrayList<>();
		for (int index = 0; index < 12; index++) entries.add(entry("busy_" + index, 3, "Busy", index, true));
		entries.add(entry("quiet_0", 3, "Quiet", 0, true));
		entries.add(entry("quiet_1", 3, "Quiet", 1, true));

		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, List.of("Quiet", "Busy"));

		RecipeMapLayout.Sector busy = sector(layout, SkillTreeLayer.SURFACE, "Busy");
		RecipeMapLayout.Sector quiet = sector(layout, SkillTreeLayer.SURFACE, "Quiet");
		assertTrue(busy.width() > quiet.width() * 4,
				"a 12-node family must get far more angle than a 2-node family");
		assertEquals(Math.PI * 2 - RecipeMapLayout.LABEL_GAP_RADIANS, busy.width() + quiet.width(), 1.0e-9,
				"sectors share the circle minus the reserved degree-label gap");
	}

	@Test
	void familiesRunClockwiseFromNorthByEarliestDegree() {
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(
				entry("late", 3, "Late", 0, true),
				entry("early", 1, "Early", 0, true),
				entry("middle", 2, "Middle", 0, true)), List.of("Late", "Middle", "Early"));

		List<String> order = layout.sectors(SkillTreeLayer.SURFACE).stream()
				.map(RecipeMapLayout.Sector::family).toList();
		assertEquals(List.of("Early", "Middle", "Late"), order);
		assertEquals(-Math.PI / 2 + RecipeMapLayout.LABEL_GAP_RADIANS / 2,
				layout.sectors(SkillTreeLayer.SURFACE).getFirst().startAngle(), 1.0e-9);
	}

	@Test
	void eachLayerWeighsOnlyItsOwnFamilies() {
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(
				entry("surface", 2, "Surface Only", 0, true),
				entry("deep", 6, "Deep Only", 0, true),
				entry("both_surface", 3, "Both", 0, true),
				entry("both_deep", 7, "Both", 1, true)), List.of("Surface Only", "Deep Only", "Both"));

		assertEquals(List.of("Surface Only", "Both"), layout.sectors(SkillTreeLayer.SURFACE).stream()
				.map(RecipeMapLayout.Sector::family).toList());
		assertEquals(List.of("Both", "Deep Only"), layout.sectors(SkillTreeLayer.DEEP).stream()
				.map(RecipeMapLayout.Sector::family).toList());
	}

	@Test
	void contentFitsTheOuterRimAndCentresTheHub() {
		RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(entry("only", 0, "Order", 0, true)),
				List.of("Order"));

		assertEquals(layout.contentWidth() / 2, layout.centerX());
		assertEquals(layout.contentHeight() / 2, layout.centerY());
		assertEquals(2 * (RecipeMapLayout.outerRingRadius(SkillTreeLayer.DEEP) + RecipeMapLayout.LABEL_MARGIN),
				layout.contentWidth());
		assertTrue(layout.contentWidth() < 1030, "the old 1030 px canvas left most of it empty");
	}

	@Test
	void nodesSitOnTheirDegreeRingInsideTheirSector() {
		List<RecipeMapEntry> entries = new ArrayList<>();
		for (int degree = 0; degree <= 8; degree++) {
			entries.add(entry("a_" + degree, degree, "A", degree, true));
			entries.add(entry("b_" + degree, degree, "B", degree, true));
		}

		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, List.of("A", "B"));

		for (RecipeMapEntry entry : entries) {
			RecipeMapLayout.NodeBounds node = layout.node(entry.key());
			assertEquals(RecipeMapLayout.ringRadius(entry.column()), distanceFromCenter(layout, node), 1.0);
			SkillTreeLayer layer = SkillTreeLayerRules.layerForDegree(entry.column());
			RecipeMapLayout.Sector sector = sector(layout, layer, entry.family());
			double angle = Math.atan2(node.centerY() - layout.centerY(), node.centerX() - layout.centerX());
			while (angle < sector.startAngle()) angle += Math.PI * 2;
			assertTrue(angle <= sector.endAngle(), entry.id() + " stays inside its family sector");
		}
	}

	/** Same fixture and coordinates as recipeMapGraph.test.ts, so the editor preview stays in step. */
	@Test
	void matchesTheEditorPreviewFixture() {
		List<RecipeMapEntry> entries = new ArrayList<>();
		entries.add(entry("order_0", 2, "Order", 0, true));
		entries.add(entry("order_1", 5, "Order", 1, true));
		for (int index = 0; index < 3; index++) entries.add(entry("vessel_" + index, 3, "Vessel", index, true));
		for (int index = 0; index < 8; index++) entries.add(entry("crowded_" + index, 5, "Crowded", index, true));
		List<String> families = new ArrayList<>(List.of("Order", "Vessel", "Crowded"));
		for (int family = 0; family < 5; family++) {
			families.add("Family " + family);
			for (int index = 0; index < 4; index++) entries.add(entry("f" + family + "_" + index, 5, "Family " + family, index, true));
		}
		families.add(RecipeMapLayout.MISC_FAMILY);
		entries.add(entry("stray", 1, "Addon", 0, true));

		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, families);

		assertEquals(470, layout.centerX());
		assertEquals(940, layout.contentWidth());
		int[][] expected = {{643, 333}, {669, 653}, {519, 256}, {154, 520}, {580, 280}, {613, 303}, {684, 520}, {412, 258}};
		String[] ids = {"stray", "order_0", "order_1", "vessel_1", "crowded_0", "crowded_1", "crowded_7", "f4_3"};
		for (int index = 0; index < ids.length; index++) {
			RecipeMapLayout.NodeBounds node = layout.node(id(ids[index]));
			assertEquals(expected[index][0], node.centerX(), ids[index] + " x");
			assertEquals(expected[index][1], node.centerY(), ids[index] + " y");
		}
	}

	private static void assertMinimumSpacing(RecipeMapLayout.Result layout, List<RecipeMapEntry> entries) {
		for (int first = 0; first < entries.size(); first++) {
			for (int second = first + 1; second < entries.size(); second++) {
				RecipeMapEntry a = entries.get(first);
				RecipeMapEntry b = entries.get(second);
				if (SkillTreeLayerRules.layerForDegree(a.column()) != SkillTreeLayerRules.layerForDegree(b.column())) continue;
				RecipeMapLayout.NodeBounds nodeA = layout.node(a.key());
				RecipeMapLayout.NodeBounds nodeB = layout.node(b.key());
				double distance = Math.hypot(nodeA.centerX() - nodeB.centerX(), nodeA.centerY() - nodeB.centerY());
				assertTrue(distance >= MIN_CENTRE_DISTANCE,
						a.id().getPath() + " / " + b.id().getPath() + " are " + distance + " px apart");
			}
		}
	}

	private static RecipeMapLayout.Sector sector(RecipeMapLayout.Result layout, SkillTreeLayer layer, String family) {
		return layout.sectors(layer).stream().filter(sector -> sector.family().equals(family)).findFirst().orElseThrow();
	}

	private static double distanceFromCenter(RecipeMapLayout.Result layout, RecipeMapLayout.NodeBounds node) {
		return Math.hypot(node.centerX() - layout.centerX(), node.centerY() - layout.centerY());
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/" + path);
	}

	private static RecipeMapEntry entry(String path, int degree, String family, int order, boolean visible) {
		return new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, id(path)), path,
				degree, family, order, visible, true);
	}
}
