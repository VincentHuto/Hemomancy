package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeMapNavigationTest {
	@Test
	void searchIsCaseInsensitiveAndExcludesUndiscoveredEntries() {
		List<RecipeMapEntry> entries = List.of(
				entry("sanguine_initiation", "Rite of Sanguine Initiation", 0, "Order", true, true),
				entry("apotheos_rite", "Rite of Apotheos", 7, "Order", false, false),
				entry("vascular_mending", "Rite of Vascular Mending", 1, "Body/Will", true, false));

		assertEquals(List.of("sanguine_initiation"), paths(RecipeMapQuery.match(entries, "SANGUINE", null, null)));
		assertEquals(List.of(), paths(RecipeMapQuery.match(entries, "apotheos", null, null)));
		assertEquals(List.of("vascular_mending"), paths(RecipeMapQuery.match(entries, "", 1, "Body/Will")));
	}

	@Test
	void recentHistoryIsUniqueMostRecentFirstAndCapped() {
		RecipeMapRecentHistory history = new RecipeMapRecentHistory(3);
		RecipeMapKey a = key("a");
		RecipeMapKey b = key("b");
		RecipeMapKey c = key("c");
		RecipeMapKey d = key("d");
		history.touch(a);
		history.touch(b);
		history.touch(c);
		history.touch(a);
		history.touch(d);

		assertEquals(List.of(d, a, c), history.entries());
	}

	@Test
	void inspectorUsesACompactPaneAndOverlaysNarrowScreens() {
		RecipeMapInspectorLayout wide = RecipeMapInspectorLayout.calculate(16, 16, 900, 500, true);
		assertEquals(false, wide.overlay());
		assertEquals(198, wide.panel().width());
		assertEquals(694, wide.mapViewport().width());
		assertEquals(wide.panel().top(), wide.preview().top());
		assertEquals(wide.preview().bottom(), wide.info().top());

		RecipeMapInspectorLayout narrow = RecipeMapInspectorLayout.calculate(16, 16, 600, 400, true);
		assertEquals(true, narrow.overlay());
		assertEquals(220, narrow.panel().width());
		assertEquals(600, narrow.mapViewport().width());

		RecipeMapInspectorLayout collapsed = RecipeMapInspectorLayout.calculate(16, 16, 900, 500, false);
		assertEquals(18, collapsed.panel().width());
		assertEquals(874, collapsed.mapViewport().width());
	}

	@Test
	void inspectorOwnsSharedPreviewAndControlGeometry() {
		RecipeMapInspectorLayout layout = RecipeMapInspectorLayout.calculate(16, 16, 900, 500, true);
		RecipeMapInspectorLayout.IntRect preview = layout.previewContent();

		assertEquals(layout.preview().left() + 8, preview.left());
		assertEquals(layout.preview().top() + 22, preview.top());
		assertEquals(layout.preview().right() - 8, preview.right());
		assertEquals(layout.preview().bottom() - 6, preview.bottom());
		assertTrue(layout.isOverToggle(layout.panel().left() + 9, layout.panel().top() + 10));
		assertFalse(layout.isOverToggle(layout.panel().left() + 19, layout.panel().top() + 10));
		assertEquals(1, layout.layerButtonAt(preview.right() - 10,
				preview.top() + preview.height() / 2 - 22, 4));
		assertEquals(-1, layout.layerButtonAt(preview.right() - 10,
				preview.top() + preview.height() / 2 + 22, 4));
		assertEquals(0, layout.layerButtonAt(preview.right() - 10,
				preview.top() + preview.height() / 2 - 22, 0));
	}

	@Test
	void selectingANodePreservesThePlayersCurrentView() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/RecipeMapCanvas.java"));
		int selectStart = source.indexOf("public ClickResult select(");
		int nextMethod = source.indexOf("\n\tprivate ", selectStart);
		String selectMethod = source.substring(selectStart, nextMethod);

		assertFalse(selectMethod.contains("centreOnNode"),
				"Selecting a recipe-map node must not rewrite the player's pan/zoom position");
	}

	private static List<String> paths(List<RecipeMapEntry> entries) {
		return entries.stream().map(entry -> entry.id().getPath().substring(entry.id().getPath().lastIndexOf('/') + 1)).toList();
	}

	private static RecipeMapEntry entry(String path, String name, int degree, String family,
			boolean visible, boolean unlocked) {
		return new RecipeMapEntry(key(path), name, degree, family, 0, visible, unlocked);
	}

	private static RecipeMapKey key(String path) {
		return new RecipeMapKey(RecipeMapEntry.Kind.RITE,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/" + path));
	}
}
