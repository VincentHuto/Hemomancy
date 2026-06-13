package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SummonsSidebarHitTestSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SummonsSidebarHitTestSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");
		String state = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabState.java");

		assertContains("degree hit testing should account for the full expanded selected group height",
				view, "sy += degreeEntryHeight(entry, state);");
		assertContains("selected degree height should include the trailing gap rendered after summon rows",
				view, "height += entry.getValue().size() * 18 + rowH + 2;");
		assertContains("sidebar content height should match the rendered selected degree height",
				state, "total += rowH + 2;");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
