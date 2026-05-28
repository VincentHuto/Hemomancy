package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicArmatureEntityModelResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HematicArmatureEntityModelResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/HematicArmatureBlock.java"));
		String blockInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String layers = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/HematicArmatureRenderer.java"));
		String blockItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tile/HematicArmatureBlockItem.java"));
		String itemRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/tile/crafting/HematicArmatureItemRenderer.java"));
		String blockModel = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/models/block/hematic_armature.json"));
		String itemModel = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/models/item/hematic_armature.json"));

		assertContains("armature block should suppress the baked JSON block model",
				block, "return RenderShape.ENTITYBLOCK_ANIMATED;");
		assertContains("armature layer should be registered for block and item renderers",
				layers, "HematicArmatureModel.LAYER_LOCATION, HematicArmatureModel::createBodyLayer");
		assertContains("block renderer should bake the complex Armature model",
				renderer, "new HematicArmatureModel(context.bakeLayer(HematicArmatureModel.LAYER_LOCATION))");
		assertContains("block renderer should use the entity texture exported with the Java model",
				renderer, "textures/entity/model_hematic_armature.png");
		assertContains("block renderer should draw the Armature model body",
				renderer, "model.renderToBuffer");
		assertContains("block renderer should provide model-sized frustum bounds",
				renderer, "public AABB getRenderBoundingBox(HematicArmatureBlockEntity armature)");
		assertContains("block renderer bounds should cover the bowl wings and ritual frame",
				renderer, "BOWL_RENDER_BOUNDS");
		assertContains("blood reservoir should render up by the top heart",
				renderer, "RESERVOIR_BOTTOM_Y = 4.05F");
		assertContains("block renderer bounds should include the raised reservoir",
				renderer, "FRAME_RENDER_HEIGHT = 5.5D");
		assertBefore("blood reservoir should render before translucent model",
				renderer, "renderReservoir(armature, poseStack, buffer);",
				"renderModel(armature, partialTicks, poseStack, buffer, combinedLight, yRot);");
		assertBefore("blood reservoir batch should flush before translucent model",
				renderer, "source.endBatch(RenderTypeInit.FLASK_FLUID);",
				"renderModel(armature, partialTicks, poseStack, buffer, combinedLight, yRot);");
		assertContains("bowl item overlay should rotate with the same facing transform as the model",
				renderer, "Axis.YP.rotationDegrees(yRot)");
		assertDoesNotContain("bowl item overlay should not mirror the block facing transform",
				renderer, "Axis.YP.rotationDegrees(-yRot)");
		assertContains("inner right bowl item anchor should mirror the exported inner-left model offset",
				renderer, "{ 1.8125F, 1.26F, 1.0625F }");
		assertContains("armature auto block item should use its custom renderer provider",
				blockInit, "new HematicArmatureBlockItem");
		assertContains("armature block item should provide a custom renderer",
				blockItem, "new HematicArmatureItemRenderer(null, null)");
		assertContains("armature item renderer should bake the same Java model",
				itemRenderer, "new HematicArmatureModel(");
		assertContains("armature item renderer should use the block renderer texture",
				itemRenderer, "HematicArmatureRenderer.TEXTURE");
		assertContains("armature GUI item renderer should use a pitched view",
				itemRenderer, "GUI_MODEL_PITCH_DEGREES = 198.0F");
		assertContains("armature GUI item renderer should yaw toward an angled block view",
				itemRenderer, "GUI_MODEL_YAW_DEGREES = -42.0F");
		assertContains("armature GUI item renderer should roll down to the right",
				itemRenderer, "GUI_MODEL_ROLL_DEGREES = -8.0F");
		assertContains("armature GUI item renderer should apply the pitched view",
				itemRenderer, "Axis.XP.rotationDegrees(GUI_MODEL_PITCH_DEGREES)");
		assertContains("armature GUI item renderer should apply the angled yaw",
				itemRenderer, "Axis.YP.rotationDegrees(GUI_MODEL_YAW_DEGREES)");
		assertContains("armature GUI item renderer should apply the down-right roll",
				itemRenderer, "Axis.ZP.rotationDegrees(GUI_MODEL_ROLL_DEGREES)");
		assertDoesNotContain("armature GUI item renderer should no longer use the nearly face-on yaw",
				itemRenderer, "Axis.YP.rotationDegrees(30.0F)");
		assertContains("armature block model should delegate rendering to the block entity renderer",
				blockModel, "\"parent\": \"builtin/entity\"");
		assertContains("armature item model should delegate rendering to the custom item renderer",
				itemModel, "\"parent\": \"builtin/entity\"");
		assertDoesExist("armature entity texture should exist",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/entity/model_hematic_armature.png"));
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

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + ": missing " + first);
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + ": missing " + second);
		}
		if (firstIndex >= secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}

	private static void assertDoesExist(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}
}
