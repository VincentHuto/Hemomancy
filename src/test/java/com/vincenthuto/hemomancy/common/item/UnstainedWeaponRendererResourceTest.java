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
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWarhammerItemRenderer.java",
				"GUI_MODEL_SCALE = 0.46F",
				"Warhammer GUI scale should be tuned independently for its broad bell head");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWarhammerItemRenderer.java",
				"GUI_MODEL_TRANSLATE_X = 0.42D",
				"Warhammer GUI pose should move right within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWarhammerItemRenderer.java",
				"GUI_MODEL_TRANSLATE_Y = -0.44D",
				"Warhammer GUI pose should move up within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"GUI_MODEL_SCALE = 0.32F",
				"Glaive GUI scale should be tuned independently for its long polearm silhouette");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"GUI_MODEL_TRANSLATE_X = 0.42D",
				"Glaive GUI pose should move right within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"GUI_MODEL_TRANSLATE_Y = -0.4D",
				"Glaive GUI pose should move up within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"GUI_MODEL_SCALE = 0.62F",
				"Dagger GUI scale should be tuned independently for its short blade silhouette");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"GUI_MODEL_TRANSLATE_X = 0.2D",
				"Dagger GUI pose should move right within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"GUI_MODEL_TRANSLATE_Y = -0.2D",
				"Dagger GUI pose should move up within the inventory slot");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWarhammerItemRenderer.java",
				"applyFirstPersonTransform",
				"Warhammer should own its first-person hand transform");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"applyFirstPersonTransform",
				"Glaive should own its first-person hand transform");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"applyFirstPersonTransform",
				"Dagger should own its first-person hand transform");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/UnstainedWarhammerItemRenderer.java",
				"WARHAMMER_TEXTURE",
				"Warhammer renderer should own its texture");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"GLAIVE_TEXTURE",
				"Glaive renderer should own its texture");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"DAGGER_TEXTURE",
				"Dagger renderer should own its texture");
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
				"float windup = 1.0F - smoothStep(Mth.clamp(swing / 0.68F, 0.0F, 1.0F));",
				"Unstained warhammer should hold the windup longer before its downward slam");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"float slamPhase = smoothStep(Mth.clamp((swing - 0.58F) / 0.42F, 0.0F, 1.0F));",
				"Unstained warhammer should delay its slam for a heavier swing");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/event/UnstainedWeaponSwingAnimationHandler.java",
				"poseStack.translate(side * 0.14F * sweep, 0.10F * arc, -0.06F * sweep);",
				"Silthmere glaive should sweep up and right in first person");
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
