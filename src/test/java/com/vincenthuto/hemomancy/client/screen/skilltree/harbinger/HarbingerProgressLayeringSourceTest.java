package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerProgressLayeringSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private HarbingerProgressLayeringSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String progressScreen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java");
		String manipTab = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationsTabController.java");
		String materialsView = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsTabView.java");
		String screenDrawUtils = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/util/ScreenDrawUtils.java");

		assertContains("materials detail panel keeps high-z reference pattern",
				materialsView, "INFO_PANEL_Z = 400.0F");
		assertContains("materials detail panel uses its named high-z layer",
				materialsView, "pose.translate(0.0F, 0.0F, INFO_PANEL_Z)");
		assertContains("materials tooltip defines a higher layer than raised recipe item stacks",
				materialsView, "MATERIAL_TOOLTIP_Z = INFO_PANEL_Z + 500.0F");
		assertContains("materials tooltip renders above selected material details",
				materialsView, "pose.translate(0.0F, 0.0F, MATERIAL_TOOLTIP_Z)");
		assertContains("harbinger screen defines a high-z chrome layer",
				progressScreen, "SCREEN_CHROME_Z = 400.0F");
		assertContains("harbinger tabs render above node item stacks",
				progressScreen, "drawTabsAboveCanvas(gfx, mouseX, mouseY)");
		assertContains("harbinger tab chrome pushes above item depth",
				progressScreen, "pose.translate(0.0F, 0.0F, SCREEN_CHROME_Z)");
		assertNotContains("harbinger skill points no longer render as persistent screen chrome",
				progressScreen, "drawSkillPointsAboveCanvas(gfx)");
		assertNotContains("harbinger skill point badge is removed",
				progressScreen, "Component.literal(\"SP:\"");
		assertNotContains("harbinger skill point badge no longer uses long spaced label",
				progressScreen, "\"Skill Points: \"");
		assertNotContains("harbinger skill point badge is no longer bold",
				progressScreen, ".withBold(true)");
		assertContains("harbinger home button anchors to left screen chrome",
				progressScreen, "private int homeButtonX()");
		assertContains("harbinger home button anchors to bottom screen chrome",
				progressScreen, "return guiTop + guiHeight - HOME_BTN_PAD - HOME_BTN_SIZE");
		assertContains("harbinger home button hitbox uses shared bottom-left anchor",
				progressScreen, "int bx = homeButtonX(), by = homeButtonY()");
		assertContains("harbinger home button draw uses shared bottom-left anchor",
				progressScreen, "ScreenDrawUtils.drawHomeButton(gfx, font, homeButtonX(), homeButtonY(), HOME_BTN_SIZE");
		assertNotContains("harbinger skill points are no longer tied to the bottom-left home button",
				progressScreen, "int badgeLeft = homeButtonX() + HOME_BTN_SIZE + SKILL_POINT_BADGE_GAP");
		assertNotContains("harbinger skill points are no longer aligned with the bottom-left home button",
				progressScreen, "int badgeTop = homeButtonY()");
		assertNotContains("harbinger skill points are no longer anchored to the top chrome beside old home button",
				progressScreen, "int badgeLeft = guiLeft + HOME_BTN_PAD + HOME_BTN_SIZE + SKILL_POINT_BADGE_GAP");
		assertNotContains("harbinger skill points are no longer anchored to the framed screen right edge",
				progressScreen, "int badgeRight = guiLeft + guiWidth - SKILL_POINT_BADGE_FRAME_INSET");
		assertNotContains("harbinger skill points are no longer anchored to the framed screen bottom edge",
				progressScreen, "int badgeBottom = guiTop + guiHeight - SKILL_POINT_BADGE_FRAME_INSET");
		assertNotContains("harbinger screen no longer duplicates rank from the overlay",
				progressScreen, "\"Rank: \"");
		assertContains("manipulation detail panel defines a high-z layer",
				manipTab, "INFO_PANEL_Z = 400.0F");
		assertContains("manipulation detail panel uses opaque backing",
				manipTab, "int solidBg = 0xFF000000 | (0x1A0505)");
		assertContains("manipulation detail panel pushes above canvas items",
				manipTab, "pose.translate(panelX, panelY, INFO_PANEL_Z)");
		assertContains("manipulation detail panel renders its recipe while elevated",
				manipTab, "MiniRecipeRenderer.draw(gfx, ctx.font(), foundRecipe, tx, ty, maxW, tendCol, MiniRecipeRenderer.BLOOD)");
		assertContains("tab helper uses opaque active backgrounds",
				screenDrawUtils, "tab.active() ? 0xFF1A0505");
		assertContains("tab helper uses opaque hovered backgrounds",
				screenDrawUtils, "hovered ? 0xFF180404");
		assertContains("tab helper uses opaque idle backgrounds",
				screenDrawUtils, "0xFF120303");
		assertContains("tab helper computes fitted tab geometry for narrow screens",
				screenDrawUtils, "fittedTabLayout(font, tabs, guiWidth, tabPad)");
		assertContains("tab helper truncates labels when fitted tabs shrink",
				screenDrawUtils, "truncateText(font, tab.label(), Math.max(1, tw - 10))");
		assertContains("tab hit testing uses the same fitted geometry as rendering",
				screenDrawUtils, "List<TabLayout> layout = fittedTabLayout(font, tabs, guiWidth, tabPad)");
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
