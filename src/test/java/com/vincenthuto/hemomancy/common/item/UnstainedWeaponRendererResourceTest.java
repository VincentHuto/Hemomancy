package com.vincenthuto.hemomancy.common.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class UnstainedWeaponRendererResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String WEAPON_TEXTURES = "textures/entity/unstained_weapons/";

	@Test
	void unstainedWeaponRenderersAreWired() throws IOException {
		assertCustomRenderedItemModel("unstained_warhammer");
		assertCustomRenderedItemModel("silthmere_glaive");
		assertCustomRenderedItemModel("absolution_dagger");
		assertCustomRenderedItemModel("annettas_absolution_dagger");
		assertBbModelExport("UnstainedWarhammerModel", WEAPON_TEXTURES + "unstained_warhammer.png", true);
		assertBbModelExport("SilthmereGlaiveModel", WEAPON_TEXTURES + "silthmere_glaive.png", true);
		assertBbModelExport("AbsolutionDaggerModel", WEAPON_TEXTURES + "absolution_dagger.png", true);
		assertContains("src/main/resources/assets/hemomancy/models/item/bbmodel/SilthmereGlaiveModel.bbmodel",
				"-11.997736",
				"Glaive collar ribbon must invert Java Z rotation for Blockbench's flipped Y axis");
		for (String texture : new String[] { "unstained_warhammer", "silthmere_glaive", "absolution_dagger",
				"annettas_absolution_dagger" }) {
			assertPng("src/main/resources/assets/hemomancy/" + WEAPON_TEXTURES + texture + ".png");
			assertPng("src/main/resources/assets/hemomancy/" + WEAPON_TEXTURES + texture + "_glow.png");
		}
		assertPng("src/main/resources/assets/hemomancy/textures/item/unstained_shield.png");
		assertContains("src/main/resources/assets/hemomancy/models/item/unstained_shield.json", "lunar boss",
				"Unstained shield should keep its raised still-water moon boss");
		assertContains("src/main/resources/assets/hemomancy/models/item/unstained_shield.json", "verdigris boss ring",
				"Unstained shield should carry its patinated copper boss ring");

		assertContains("src/main/java/com/vincenthuto/hemomancy/client/item/ItemClientExtensions.java",
				"Map.entry(UnstainedWarhammerItem.class",
				"UnstainedWarhammerItem must expose client item extensions for its 3D renderer");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/item/unstained/tool/UnstainedWarhammerItem.java",
				"DiggerItem.createAttributes(tier, attackDamage, attackSpeed)",
				"UnstainedWarhammerItem must apply its configured damage and attack speed attributes");
		assertContains("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java",
				"new UnstainedWarhammerItem(8f, -3.4f",
				"Unstained warhammer should have a slower heavy-weapon attack cooldown");
		assertContains("src/main/resources/assets/hemomancy/lang/en_us.json",
				"On hit: cripples enemies with Slowness and Weakness.",
				"Unstained warhammer should display weapon info like the glaive and dagger");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/item/ItemClientExtensions.java",
				"Map.entry(SilthmereGlaiveItem.class",
				"SilthmereGlaiveItem must expose client item extensions for its 3D renderer");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/item/ItemClientExtensions.java",
				"Map.entry(AbsolutionDaggerItem.class",
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
		for (String renderer : new String[] { "UnstainedWarhammerItemRenderer", "SilthmereGlaiveItemRenderer",
				"AbsolutionDaggerItemRenderer" }) {
			String path = "src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/" + renderer + ".java";
			assertContains(path, WEAPON_TEXTURES, renderer + " should use its authored weapon atlas");
			assertNotContains(path, "textures/block/", renderer + " must not borrow a 16x16 block texture");
			assertContains(path, "RenderType.eyes(", renderer + " should render its still-water glow layer");
		}
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"ANNETTA_TEXTURE",
				"Annetta's dagger should render with its own hair, tooth and nail atlas");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"setAnnettaVariant(annetta)",
				"Only Annetta's dagger should show her braid");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/AbsolutionDaggerModel.java",
				"\"annetta_braid\"",
				"Dagger model should define Annetta's braid part");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/UnstainedWarhammerModel.java",
				"\"crescent_finial\"",
				"Warhammer should crown its bell with a crescent finial");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/SilthmereGlaiveItemRenderer.java",
				"GLAIVE_TEXTURE",
				"Glaive renderer should own its texture");
		assertContains("src/main/java/com/vincenthuto/hemomancy/client/render/item/unstained/AbsolutionDaggerItemRenderer.java",
				"DAGGER_TEXTURE",
				"Dagger renderer should own its texture");
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

	private static void assertPng(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(ROOT.resolve(path));
		assertTrue(bytes.length > 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G',
				path + " must be a PNG");
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
