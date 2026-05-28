package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CustomBlockItemGuiPoseSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final String ITEM_RENDERER_PACKAGE =
			"com/vincenthuto/hemomancy/client/render/item/tile/functional/";

	private CustomBlockItemGuiPoseSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertDownRightGuiPose("earthen vein item renderer",
				"EarthenVeinItemRenderer.java");
		assertDownRightGuiPose("puppeteers spindle item renderer",
				"PuppeteersSpindleItemRenderer.java");
		assertMirroredGuiPose("visceral mirror item renderer",
				"VisceralMirrorItemRenderer.java");
		String mirror = read(SOURCE_ROOT.resolve(ITEM_RENDERER_PACKAGE + "VisceralMirrorItemRenderer.java"));
		assertContains("visceral mirror item renderer should preserve its GUI scale as a named constant",
				mirror, "GUI_MODEL_SCALE = 0.45F");
		assertContains("visceral mirror item renderer should flip local X so the authored front face is visible",
				mirror, "poseStack.scale(-GUI_MODEL_SCALE, GUI_MODEL_SCALE, GUI_MODEL_SCALE)");
		String spindle = read(SOURCE_ROOT.resolve(ITEM_RENDERER_PACKAGE + "PuppeteersSpindleItemRenderer.java"));
		assertContains("puppeteers spindle item renderer should lower its GUI model",
				spindle, "GUI_MODEL_TRANSLATE_Y = 0.26D");
		assertContains("puppeteers spindle item renderer should use the lowered GUI model translation",
				spindle, "poseStack.translate(0.5D, GUI_MODEL_TRANSLATE_Y, 0.5D)");
	}

	private static void assertDownRightGuiPose(String label, String fileName) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(ITEM_RENDERER_PACKAGE + fileName));
		assertContains(label + " should define the shared GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES = 198.0F");
		assertContains(label + " should define the shared GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES = -42.0F");
		assertContains(label + " should define the shared GUI roll",
				renderer, "GUI_MODEL_ROLL_DEGREES = -8.0F");
		assertContains(label + " should apply the GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES");
		assertContains(label + " should apply the GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES");
		assertContains(label + " should apply the GUI down-right roll",
				renderer, "GUI_MODEL_ROLL_DEGREES");
		assertDoesNotContain(label + " should not keep the old face-on inventory yaw",
				renderer, "Vector3.YP, 90");
		assertDoesNotContain(label + " should not keep the old near-isometric GUI yaw",
				renderer, "Vector3.YN, 45");
	}

	private static void assertMirroredGuiPose(String label, String fileName) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(ITEM_RENDERER_PACKAGE + fileName));
		assertContains(label + " should define the shared GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES = 198.0F");
		assertContains(label + " should define the right-facing front GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES = 42.0F");
		assertContains(label + " should define the shared GUI roll",
				renderer, "GUI_MODEL_ROLL_DEGREES = -8.0F");
		assertContains(label + " should apply the GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES");
		assertContains(label + " should apply the reversed GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES");
		assertContains(label + " should apply the GUI down-right roll",
				renderer, "GUI_MODEL_ROLL_DEGREES");
		assertDoesNotContain(label + " should not keep the old face-on inventory yaw",
				renderer, "Vector3.YP, 90");
		assertDoesNotContain(label + " should not keep the old near-isometric GUI yaw",
				renderer, "Vector3.YN, 45");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
