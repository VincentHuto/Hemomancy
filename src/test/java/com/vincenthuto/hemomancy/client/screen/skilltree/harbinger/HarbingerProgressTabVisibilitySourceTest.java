package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.support.EnglishText;

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

		assertContains("the merged rites and crafts tab is visible by default",
				progressScreen, "RITES(\"screen.hemomancy.harbinger_progress.tab.rites\", 0xFF8844CC, 0, false)");
		assertEnglish("screen.hemomancy.harbinger_progress.tab.rites", "Rites & Crafts");
		assertNotContains("crafting no longer occupies a separate top-level tab",
				progressScreen, "CRAFTING(\"");
		assertContains("tendencies tab is hidden until degree 3",
				progressScreen, "TENDENCIES(\"screen.hemomancy.harbinger_progress.tab.tendencies\", 0xFFCC8833, 3, false)");
		assertEnglish("screen.hemomancy.harbinger_progress.tab.tendencies", "Tendencies");
		assertNotContains("scars no longer occupy a separate top-level tab",
				progressScreen, "SCARS(\"");
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

	private static void assertEnglish(String key, String expected) {
		if (!EnglishText.of(key).equals(expected)) {
			throw new AssertionError(key + " (expected '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
