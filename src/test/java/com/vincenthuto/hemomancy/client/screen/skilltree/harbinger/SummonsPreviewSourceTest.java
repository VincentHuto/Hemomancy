package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SummonsPreviewSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SummonsPreviewSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");
		String controller = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabController.java");

		assertNotContains("summons preview should not use limited follow-mouse entity renderer",
				view, "renderEntityInInventoryFollowsMouse");
		assertNotContains("summons preview should not synthesize a cosine mouse position",
				view, "Mth.cos(state.previewRotationAngle)");
		assertContains("summons preview should render from explicit yaw",
				view, "float yaw = 180.0F + (float) Math.toDegrees(state.previewRotationAngle)");
		assertContains("summons preview should use direct entity inventory renderer",
				view, "InventoryScreen.renderEntityInInventory(gfx");
		assertContains("summons preview should restore entity rotations after preview rendering",
				view, "restorePreviewRotations(entity, rotations)");
		assertContains("summons preview should track preview scissor state",
				view, "boolean scissorEnabled = false");
		assertContains("summons preview should always release preview scissor before fallback drawing",
				view, "if (scissorEnabled) {");
		assertContains("summons preview should use height-aware render scale",
				view, "int scale = previewEntityScale(entity, bottom - top)");
		assertContains("summons preview should raise entity center to keep feet visible",
				view, "int centerY = top + Math.round((bottom - top) * 0.56F)");
		assertContains("summons preview should leave bottom room for tall mobs",
				view, "private static int previewEntityScale(LivingEntity entity, int availableH)");
		assertContains("scarlet mummer should have a preview entity",
				view, "case PuppeteerSummonDefinitions.SCARLET_MUMMER -> EntityInit.scarlet_mummer.get().create(mc.level)");
		assertContains("sanguine hound should have a preview entity",
				view, "case PuppeteerSummonDefinitions.SANGUINE_HOUND -> EntityInit.sanguine_hound.get().create(mc.level)");
		assertContains("summons auto spin should be half the prior speed",
				controller, "PREVIEW_AUTO_ROTATION_SPEED = 0.175f");
		assertContains("summons drag spin should be half the prior sensitivity",
				controller, "PREVIEW_DRAG_ROTATION_SPEED = 0.02f");
		assertContains("summons auto spin should use the named speed",
				controller, "state.previewRotationAngle += partial * PREVIEW_AUTO_ROTATION_SPEED");
		assertContains("summons drag spin should use the named sensitivity",
				controller, "* PREVIEW_DRAG_ROTATION_SPEED");
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
