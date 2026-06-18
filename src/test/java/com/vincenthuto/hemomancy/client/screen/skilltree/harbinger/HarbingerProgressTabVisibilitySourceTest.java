package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerProgressTabVisibilitySourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private HarbingerProgressTabVisibilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String progressScreen = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java"));

		assertContains("crafting tab is hidden until degree 2",
				progressScreen, "CRAFTING(\"Crafting\", 0xFFAA2222, 2)");
		assertContains("manipulations tab is hidden until degree 3",
				progressScreen, "MANIPULATIONS(\"Manipulations\", 0xFFCC8833, 3)");
		assertContains("scars tab is hidden until degree 4",
				progressScreen, "SCARS(\"Scars\", 0xFF44AACC, 4)");
		assertContains("tab descriptors are built from visible tabs",
				progressScreen, "for (Tab tab : visibleTabs(playerDegree))");
		assertContains("hit testing maps visible index back to visible tab list",
				progressScreen, "return idx >= 0 ? visibleTabs.get(idx) : null");
		assertContains("direct tab switches refuse locked tabs",
				progressScreen, "if (!tab.visibleAtDegree(playerDegree)) return;");
		assertContains("active tab recovers if current degree cannot see it",
				progressScreen, "activeTab = firstVisibleTab(playerDegree)");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
