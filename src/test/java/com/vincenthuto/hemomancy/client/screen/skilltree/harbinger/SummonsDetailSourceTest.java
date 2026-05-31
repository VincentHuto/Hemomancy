package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SummonsDetailSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SummonsDetailSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");

		assertContains("summons details should use stacked wrapped detail rows",
				view, "drawStackedDetailLine(gfx, ctx");
		assertContains("summons details should indent values without a fixed right column",
				view, "DETAIL_VALUE_INDENT");
		assertContains("summons details should give values the remaining full panel width",
				view, "int valueW = Math.max(24, totalW - DETAIL_VALUE_INDENT)");
		assertContains("summons details should wrap long values below their labels",
				view, "drawWrappedValue(gfx, ctx, valueText, x + DETAIL_VALUE_INDENT");
		assertNotContains("summons details should not draw values at the old fixed offset",
				view, "x + 78, y");
		assertNotContains("summons details should not reserve the old label column",
				view, "DETAIL_LABEL_W");
		assertNotContains("summons details should not shift values into the panel edge",
				view, "x + DETAIL_LABEL_W + DETAIL_COLUMN_GAP");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
