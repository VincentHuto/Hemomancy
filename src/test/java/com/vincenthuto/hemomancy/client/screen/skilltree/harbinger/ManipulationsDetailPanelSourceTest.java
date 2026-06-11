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
		assertDoesNotContain("hover tooltip should not repeat the detail panel type",
				controller, "\"Type: \"");
		assertDoesNotContain("hover tooltip should not repeat the detail panel blood cost",
				controller, "\"Blood Cost: \"");
		assertDoesNotContain("hover tooltip should not repeat the detail panel vein section",
				controller, "\"Vein Section: \"");
		assertContains("hover tooltip should render above item icons from the detail panel",
				controller, "MANIP_TOOLTIP_Z");
		assertContains("hover tooltip should raise the pose before drawing",
				controller, "pose.translate(0, 0, MANIP_TOOLTIP_Z)");
		assertContains("detail panel should display secondary tendency when present",
				controller, "secondaryTendencyText(manip)");
		assertContains("detail panel should split mixed tendencies across compact lines",
				controller, "drawMixedTendencyValue(gfx, ctx");
		assertContains("detail panel should know which stat cell is the tendency cell",
				controller, "cell.mixedTendency()");
		assertContains("detail panel should color the secondary tendency independently",
				controller, "cell.secondaryValueColor()");
		assertContains("detail panel should compute the secondary tendency color",
				controller, "tendencyColor(secondaryTend)");
		assertContains("detail panel should reserve less dead space below compact recipes",
				controller, "recipeH + 4");
		assertContains("detail panel should use a compact final bottom pad",
				controller, "MANIP_PANEL_BOTTOM_PAD = 3");
		assertContains("detail panel should keep its top border below the tab strip",
				controller, "MANIP_PANEL_TOP_CLEARANCE = 24");
		assertContains("detail panel should use the tab-aware top clearance as its minimum y",
				controller, "int minPanelY = ctx.guiTop() + MANIP_PANEL_TOP_CLEARANCE");
		assertContains("manipulations should serialize secondary tendency when present",
				read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java"),
				"secondary_tendency");
		assertContains("detail panel should use translatable manipulation descriptions",
				controller, "Component.translatable(manipDescriptionKey(manip))");
		assertContains("detail panel should keep descriptions compact",
				controller, "MANIP_DESCRIPTION_MAX_LINES = 2");
		assertContains("detail panel stat cells should fit stacked mixed-tendency text",
				controller, "MANIP_STAT_CELL_H = 28");
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
