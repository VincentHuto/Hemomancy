package com.vincenthuto.hemomancy.common.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnstainedWeaponRendererResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	public static void main(String[] args) throws IOException {
		assertCustomRenderedItemModel("unstained_warhammer");
		assertCustomRenderedItemModel("silthmere_glaive");
		assertCustomRenderedItemModel("absolution_dagger");
		assertCustomRenderedItemModel("annettas_absolution_dagger");

		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"implements HemoClientItemExtensionsProvider",
				"UnstainedWarhammerItem must expose client item extensions for its 3D renderer");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/SilthmereGlaiveItem.java",
				"implements HemoClientItemExtensionsProvider",
				"SilthmereGlaiveItem must expose client item extensions for its 3D renderer");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/AbsolutionDaggerItem.java",
				"implements HemoClientItemExtensionsProvider",
				"AbsolutionDaggerItem must expose client item extensions for its 3D renderer");

		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java",
				"UnstainedWarhammerModel.LAYER_LOCATION",
				"Warhammer model layer must be registered");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java",
				"SilthmereGlaiveModel.LAYER_LOCATION",
				"Glaive model layer must be registered");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java",
				"AbsolutionDaggerModel.LAYER_LOCATION",
				"Dagger model layer must be registered");

		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/UnstainedWarhammerModel.java",
				"bell",
				"Warhammer model should keep a bell-inspired striking head");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"blade",
				"Glaive model should define a polearm blade");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/AbsolutionDaggerModel.java",
				"stiletto",
				"Dagger model should define its stiletto blade");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case WARHAMMER -> 0.52F",
				"Warhammer GUI scale should be large enough for an inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case GLAIVE -> 0.48F",
				"Glaive GUI scale should be large enough for an inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case DAGGER -> 0.7F",
				"Dagger GUI scale should be large enough for an inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"applyHandAnchor(displayContext, poseStack);",
				"Unstained weapons should translate their model origin into the player's hand");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));",
				"Right-hand contexts should use the tuned side-on weapon orientation");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));",
				"Left-hand contexts should mirror the tuned side-on weapon orientation");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case THIRD_PERSON_RIGHT_HAND -> poseStack.translate(-0.5F, -0.5F, -0.5F);",
				"Right-hand third-person transform should preserve the manually tuned anchor");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case THIRD_PERSON_LEFT_HAND -> poseStack.translate(0.5F, -0.5F, -0.5F);",
				"Left-hand third-person transform should mirror the manually tuned anchor");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case FIRST_PERSON_RIGHT_HAND -> poseStack.translate(-0.42F, -0.38F, -0.42F);",
				"Right-hand first-person transform should follow the tuned third-person anchor at a closer offset");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWeaponItemRenderer.java",
				"case FIRST_PERSON_LEFT_HAND -> poseStack.translate(0.42F, -0.38F, -0.42F);",
				"Left-hand first-person transform should mirror the tuned first-person anchor");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/UnstainedWarhammerModel.java",
				"bell_flared_face",
				"Warhammer bell head should rotate the flared mouth into the striking face");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/UnstainedWarhammerModel.java",
				"PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, 0.0F, 0.0F, -1.5708F)",
				"Warhammer bell head should be rotated sideways across the haft");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"main_cutting_blade",
				"Glaive should read as a single broad cutting blade");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"back_hook",
				"Glaive may keep a small back hook without becoming a trident");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"blade_socket",
				"Glaive should include a socket that closes the gap between shaft and blade");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"blade_left_sweep",
				"Glaive should not use the old fork-like left sweep");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/SilthmereGlaiveModel.java",
				"blade_right_sweep",
				"Glaive should not use the old fork-like right sweep");
	}

	private static void assertCustomRenderedItemModel(String itemId) throws IOException {
		String json = read("src/main/resources/assets/hemomancy/models/item/" + itemId + ".json");
		assertTrue(json.contains("\"parent\"") && json.contains("\"builtin/entity\""),
				itemId + " must use builtin/entity so the custom item renderer is used");
		assertTrue(!json.contains("\"minecraft:item/handheld\"") && !json.contains("\"item/handheld\""),
				itemId + " must not regress to a flat handheld sprite model");
	}

	private static void assertContains(String path, String expected, String message) throws IOException {
		assertTrue(read(path).contains(expected), message + " (missing '" + expected + "')");
	}

	private static void assertNotContains(String path, String unexpected, String message) throws IOException {
		assertTrue(!read(path).contains(unexpected), message + " (still contains '" + unexpected + "')");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
