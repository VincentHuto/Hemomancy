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
		assertBbModelExport("UnstainedWarhammerModel", "textures/block/pale_silver_bell_body.png", true);
		assertBbModelExport("SilthmereGlaiveModel", "textures/block/pale_silver_block.png", true);
		assertBbModelExport("AbsolutionDaggerModel", "textures/block/pale_silver_block.png", false);
		assertContains("src/main/resources/assets/hemomancy/models/item/bbmodel/SilthmereGlaiveModel.bbmodel",
				"-10.31324",
				"Glaive main blade must invert Java Z rotation for Blockbench's flipped Y axis");
		assertContains("src/main/resources/assets/hemomancy/models/item/bbmodel/SilthmereGlaiveModel.bbmodel",
				"31.512679",
				"Glaive back hook must invert Java Z rotation for Blockbench's flipped Y axis");

		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"implements HemoClientItemExtensionsProvider",
				"UnstainedWarhammerItem must expose client item extensions for its 3D renderer");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"applyForgeHandTransform",
				"UnstainedWarhammerItem should leave first-person swing timing to the shared event handler");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"DiggerItem.createAttributes(tier, attackDamage, attackSpeed)",
				"UnstainedWarhammerItem must apply its configured damage and attack speed attributes");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java",
				"new UnstainedWarhammerItem(8f, -3.4f",
				"Unstained warhammer should have a slower heavy-weapon attack cooldown");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"On hit: cripples enemies with Slowness and Weakness.",
				"Unstained warhammer should display weapon info like the glaive and dagger");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/SilthmereGlaiveItem.java",
				"implements HemoClientItemExtensionsProvider",
				"SilthmereGlaiveItem must expose client item extensions for its 3D renderer");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/SilthmereGlaiveItem.java",
				"applyForgeHandTransform",
				"SilthmereGlaiveItem should leave first-person swing timing to the shared event handler");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/AbsolutionDaggerItem.java",
				"implements HemoClientItemExtensionsProvider",
				"AbsolutionDaggerItem must expose client item extensions for its 3D renderer");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/AbsolutionDaggerItem.java",
				"applyForgeHandTransform",
				"AbsolutionDaggerItem should keep the previously working event-handler thrust animation");

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
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"@EventBusSubscriber",
				"Unstained weapon swing adjustments should run through the old shared RenderHandEvent path");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"RenderHandEvent",
				"Unstained weapon swing adjustments should run through the old shared RenderHandEvent path");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"public static void onRenderHand(RenderHandEvent event)",
				"Unstained weapon swing adjustments should use the previously working render event hook");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"float sqrtSwing = Mth.sqrt(swing);",
				"Silthmere glaive should use the older event-path arc instead of the failed replacement sweep");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"float raisePhase = Mth.clamp(1.0F - swing * 2.0F, 0.0F, 1.0F);",
				"Unstained warhammer should use the older raise/slam event-path swing");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"Axis.XP.rotationDegrees(48.0F * thrust + 18.0F * lift)",
				"Absolution dagger special swing should drive upward as a thrust");
		assertNotContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"public static boolean applyForgeHandTransform",
				"Unstained weapon swings should not replace vanilla hand transforms");
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

	private static void assertBbModelExport(String modelName, String texturePath, boolean expectsRotation) throws IOException {
		String json = read("src/main/resources/assets/hemomancy/models/item/bbmodel/" + modelName + ".bbmodel");
		assertTrue(json.contains("\"model_format\": \"modded_entity\""),
				modelName + " must export as a Blockbench modded entity model");
		assertTrue(json.contains("\"box_uv\": true"),
				modelName + " must preserve box UVs for Java model parity");
		assertTrue(json.contains("\"elements\": ["),
				modelName + " must include exported cube elements");
		assertTrue(json.contains("\"relative_path\": \"../../../" + texturePath + "\""),
				modelName + " must point at its renderer texture");
		assertTrue(!expectsRotation || json.contains("\"rotation\": ["),
				modelName + " must preserve rotated Java PartPose groups");
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
