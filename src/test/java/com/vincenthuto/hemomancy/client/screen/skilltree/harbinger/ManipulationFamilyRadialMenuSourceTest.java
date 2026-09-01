package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManipulationFamilyRadialMenuSourceTest {
	@Test
	void familyFormsRenderAsAnOpaqueLateOverlay() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationsTabController.java"));
		String screen = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java"));

		assertTrue(source.contains("drawFamilyRadialMenu(gfx, ctx, familyEntry, mouseX, mouseY)"));
		assertTrue(source.contains("FAMILY_MENU_Z = INFO_PANEL_Z - 100.0F"));
		assertTrue(source.contains("pose.translate(0, 0, FAMILY_MENU_Z)"));
		assertTrue(source.contains("EnumNodeShape.CIRCLE, centerX, centerY, menuRadius, FAMILY_MENU_BG"));
		String nodePass = source.substring(source.indexOf("private void drawManipNodes"),
				source.indexOf("private int masteryLevel"));
		assertFalse(nodePass.contains("drawFamilySatellites"));
		assertFalse(nodePass.contains("drawCenteredString(ctx.font(), labelText"));
		String overlay = source.substring(source.indexOf("public void renderOverlay"),
				source.indexOf("public void renderTooltip"));
		assertTrue(overlay.indexOf("drawFamilyRadialMenu") < overlay.indexOf("drawManipInfoPanel"));
		assertTrue(screen.contains("SCREEN_CHROME_Z = 200.0F"));
	}
}
