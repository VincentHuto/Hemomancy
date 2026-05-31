package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManipulationsDetailPanelSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ManipulationsDetailPanelSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String controller = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationsTabController.java");
		String language = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("detail panel should define two stat columns",
				controller, "MANIP_STAT_COLUMNS = 2");
		assertContains("detail panel should define three stat rows",
				controller, "MANIP_STAT_ROWS = 3");
		assertContains("detail panel should draw manipulation descriptions under the name",
				controller, "drawManipDescription(gfx, ctx");
		assertContains("detail panel should route stat rendering through a grid helper",
				controller, "drawManipStatGrid(gfx, ctx");
		assertContains("detail panel should use stat cell records for label/value/color data",
				controller, "private record ManipStatCell");
		assertContains("detail panel should include cooldown even when the value is zero",
				controller, "cooldownText(manip.getCooldownTicks())");
		assertContains("detail panel should use translatable manipulation descriptions",
				controller, "Component.translatable(manipDescriptionKey(manip))");
		assertContains("detail panel should keep descriptions compact",
				controller, "MANIP_DESCRIPTION_MAX_LINES = 2");
		assertContains("detail panel stat cells should be tall enough for descenders",
				controller, "MANIP_STAT_CELL_H = 20");
		assertContains("detail panel should compute a bottom limit from the GUI frame",
				controller, "int panelBottomLimit = ctx.guiTop() + ctx.guiHeight() - INFO_PANEL_MARGIN");
		assertDoesNotContain("detail panel should keep the recipe section visible when compacting",
				controller, "foundRecipe = null");
		assertContains("detail panel should size against the clamped panel area",
				controller, "int availablePanelH = panelBottomLimit - minPanelY");
		assertContains("detail panel should clamp its y position after content sizing",
				controller, "Mth.clamp(panelY");
		assertContains("language should include a description for Blood Shot",
				language, "\"hemomancy.manipulation.blood_shot.desc\"");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + " (still contains '" + forbidden + "')");
		}
	}
}
