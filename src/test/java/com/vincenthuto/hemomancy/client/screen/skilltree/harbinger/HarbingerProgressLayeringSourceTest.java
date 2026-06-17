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
				materialsView, "pose.translate(0.0F, 0.0F, 400.0F)");
		assertContains("harbinger screen defines a high-z chrome layer",
				progressScreen, "SCREEN_CHROME_Z = 400.0F");
		assertContains("harbinger tabs render above node item stacks",
				progressScreen, "drawTabsAboveCanvas(gfx, mouseX, mouseY)");
		assertContains("harbinger tab chrome pushes above item depth",
				progressScreen, "pose.translate(0.0F, 0.0F, SCREEN_CHROME_Z)");
		assertContains("harbinger skill points render in screen chrome",
				progressScreen, "drawSkillPointsAboveCanvas(gfx)");
		assertContains("harbinger skill points use styled component width",
				progressScreen, "int textW = font.width(spComponent)");
		assertContains("harbinger skill point badge uses compact horizontal padding",
				progressScreen, "SKILL_POINT_BADGE_X_PAD = 2");
		assertContains("harbinger skill point badge uses compact vertical padding",
				progressScreen, "SKILL_POINT_BADGE_Y_PAD = 2");
		assertContains("harbinger skill point badge uses compact label text",
				progressScreen, "Component.literal(\"SP:\" + SkillProgressClientCache.current().getSkillPoints())");
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
		assertContains("harbinger skill points sit in the freed top-left chrome",
				progressScreen, "int badgeLeft = guiLeft + HOME_BTN_PAD");
		assertContains("harbinger skill points align with the top-left chrome padding",
				progressScreen, "int badgeTop = guiTop + HOME_BTN_PAD");
		assertContains("harbinger skill point text reserves badge padding from the left edge",
				progressScreen, "int textX = badgeLeft + SKILL_POINT_BADGE_X_PAD");
		assertContains("harbinger skill point text reserves badge padding from the top edge",
				progressScreen, "int textY = badgeTop + SKILL_POINT_BADGE_Y_PAD");
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
