package com.vincenthuto.hemomancy.client.render.item.hematic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingArmamentRenderPlacementSourceTest {
	public static void main(String[] args) throws IOException {
		Path root = Path.of("").toAbsolutePath();
		String torchModel = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingTorchModel.java"));
		String flailModel = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingFlailModel.java"));
		String flailHelper = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingFlailRenderHelper.java"));
		String flailRenderer = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingFlailItemRenderer.java"));
		String flailLayer = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/LivingFlailLayer.java"));
		String torchRenderer = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingTorchItemRenderer.java"));
		String torchPlacement = Files.readString(root.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingTorchRenderPlacement.java"));

		assertContains("torch has positive upright flame placement", torchModel, "TORCH_FLAME_MIN_Y = 20.5F");
		assertContains("torch shaft overlaps crown instead of separating", torchModel, "TORCH_SHAFT_TOP_Y = 18.0F");
		assertContains("torch renderer shares placement with the visual emitter", torchRenderer,
				"LivingTorchRenderPlacement.applyCustomModelTransform");
		assertNotContains("torch item placement does not double-rotate the mouth pose", torchPlacement,
				"THIRD_PERSON_MOUTH_PITCH_DEGREES");
		assertNotContains("torch item placement leaves positive Z mouth roll to the arm", torchPlacement,
				"THIRD_PERSON_MOUTH_ROLL_DEGREES");
		assertContains("torch third-person render lifts to mouth height", torchPlacement,
				"THIRD_PERSON_TORCH_LIFT = 0.14D");
		assertContains("torch third-person render points outward from the torso", torchPlacement,
				"THIRD_PERSON_TORCH_OUTWARD_OFFSET = 0.18D");
		assertContains("flail handle places chain end below the grip", flailModel, "CHAIN_COLLAR_MIN_Y = 8.5F");
		assertContains("flail physics anchor is below handle grip", flailHelper, "CHAIN_ANCHOR = new Vec3(0.0, 0.58, 0.0)");
		assertContains("flail static bob hangs downward", flailHelper, "new Vec3(0.0, 0.62, 0.0)");
		assertContains("flail physics returns positive chain slack", flailHelper, "return new Vec3(x, Math.sqrt(slack), z)");
		assertContains("flail zero-length chain fallback points downward", flailHelper,
				"tangent = new Vec3(0.0, -1.0, 0.0)");
		assertContains("flail helper rotates only chain and head into the droop frame", flailHelper,
				"private static void renderDroopingChainAndHead");
		assertContains("flail droop frame has a deliberate roll", flailHelper,
				"CHAIN_DROOP_ROLL_DEGREES = 180.0F");
		assertContains("flail droop frame has a deliberate pitch", flailHelper,
				"CHAIN_DROOP_PITCH_DEGREES = 80.0F");
		assertContains("flail droop frame faces the head away without turning the pole", flailHelper,
				"CHAIN_FACE_AWAY_YAW_DEGREES = 180.0F");
		assertContains("flail renders pole before chain droop frame", flailHelper,
				"model.renderHandle(poseStack, base, packedLight, packedOverlay, -1);");
		assertNotContains("flail no longer uses negative anchor", flailHelper,
				"ANCHOR = new Vec3(0.0, -1.0, 0.0)");
		assertContains("flail gui render is larger", flailRenderer, "GUI_MODEL_SCALE = 0.76F");
		assertContains("flail third-person render keeps gravity down in local model frame", flailLayer,
				"THIRD_PERSON_MODEL_PITCH_DEGREES = 0.0F");
		assertContains("flail third-person layer does not turn the whole pole to face the head", flailLayer,
				"THIRD_PERSON_MODEL_YAW_DEGREES = 0.0F");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + " still contains: " + forbidden);
		}
	}
}
