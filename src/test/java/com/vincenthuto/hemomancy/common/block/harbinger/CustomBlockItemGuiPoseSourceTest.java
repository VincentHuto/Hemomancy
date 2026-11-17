package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class CustomBlockItemGuiPoseSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final String FUNCTIONAL_ITEM_RENDERER_PACKAGE =
			"com/vincenthuto/hemomancy/client/render/item/tile/harbinger/functional/";
	private static final String CRAFTING_ITEM_RENDERER_PACKAGE =
			"com/vincenthuto/hemomancy/client/render/item/tile/harbinger/crafting/";

	private CustomBlockItemGuiPoseSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		assertDownRightGuiPose("earthen vein item renderer",
				FUNCTIONAL_ITEM_RENDERER_PACKAGE, "EarthenVeinItemRenderer.java");
		assertDownRightGuiPose("puppeteers spindle item renderer",
				CRAFTING_ITEM_RENDERER_PACKAGE, "PuppeteersSpindleItemRenderer.java");

		String spindle = read(SOURCE_ROOT.resolve(CRAFTING_ITEM_RENDERER_PACKAGE + "PuppeteersSpindleItemRenderer.java"));
		assertContains("puppeteers spindle item renderer should name the mesh's visual centre",
				spindle, "MODEL_CENTRE_Y = 16.25F / 16.0F");
		assertContains("puppeteers spindle item renderer should place its GUI model at the slot centre",
				spindle, "poseStack.translate(0.5D, 0.5D, 0.5D)");
		assertContains("puppeteers spindle item renderer should rotate about the mesh's centre, not its feet",
				spindle, "poseStack.translate(0.0F, -MODEL_CENTRE_Y, -MODEL_CENTRE_Z)");
	}

	private static void assertDownRightGuiPose(String label, String packagePath, String fileName) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(packagePath + fileName));
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
		assertNotContains(label + " should not keep the old face-on inventory yaw",
				renderer, "Vector3.YP, 90");
		assertNotContains(label + " should not keep the old near-isometric GUI yaw",
				renderer, "Vector3.YN, 45");
	}

	private static void assertMirroredGuiPose(String label, String fileName) throws IOException {
		String renderer = read(SOURCE_ROOT.resolve(FUNCTIONAL_ITEM_RENDERER_PACKAGE + fileName));
		assertContains(label + " should define the shared GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES = 198.0F");
		assertContains(label + " should define the right-facing front GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES = -45.0F");
		assertContains(label + " should define the shared GUI roll",
				renderer, "GUI_MODEL_ROLL_DEGREES = 1.0F");
		assertContains(label + " should apply the GUI pitch",
				renderer, "GUI_MODEL_PITCH_DEGREES");
		assertContains(label + " should apply the reversed GUI yaw",
				renderer, "GUI_MODEL_YAW_DEGREES");
		assertContains(label + " should apply the GUI down-right roll",
				renderer, "GUI_MODEL_ROLL_DEGREES");
		assertNotContains(label + " should not keep the old face-on inventory yaw",
				renderer, "Vector3.YP, 90");
		assertNotContains(label + " should not keep the old near-isometric GUI yaw",
				renderer, "Vector3.YN, 45");
	}
}
