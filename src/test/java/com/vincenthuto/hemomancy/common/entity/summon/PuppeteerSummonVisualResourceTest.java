package com.vincenthuto.hemomancy.common.entity.summon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PuppeteerSummonVisualResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private PuppeteerSummonVisualResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertSummonVisuals("veinwing_vulture", "VeinwingVulture", "VexRenderer");
		assertSummonVisuals("marrow_spitter", "MarrowSpitter", "SkeletonRenderer");
		assertSummonVisuals("gorebound_hulk", "GoreboundHulk", "ZombieRenderer");

		String layerEvents = readSource("com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		assertContains("veinwing layer registration", layerEvents, "VeinwingVultureModel.LAYER_LOCATION");
		assertContains("marrow layer registration", layerEvents, "MarrowSpitterModel.LAYER_LOCATION");
		assertContains("gorebound layer registration", layerEvents, "GoreboundHulkModel.LAYER_LOCATION");

		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		assertContains("veinwing renderer registration", clientEvents, "VeinwingVultureRenderer::new");
		assertContains("marrow renderer registration", clientEvents, "MarrowSpitterRenderer::new");
		assertContains("gorebound renderer registration", clientEvents, "GoreboundHulkRenderer::new");
	}

	private static void assertSummonVisuals(String id, String className, String forbiddenRenderer) throws IOException {
		Path modelPath = SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/entity/summon/" + className + "Model.java");
		Path rendererPath = SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/summon/" + className + "Renderer.java");
		Path texturePath = RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/entity/puppeteer_summon/" + id + ".png");

		assertExists(className + " model source", modelPath);
		assertExists(className + " renderer source", rendererPath);
		assertPng(className + " texture", texturePath);

		String model = read(modelPath);
		String renderer = read(rendererPath);

		assertContains(className + " model exposes layer", model, "public static final ModelLayerLocation LAYER_LOCATION");
		assertContains(className + " model uses entity id", model, "Hemomancy.rloc(\"" + id + "\")");
		assertContains(className + " renderer uses MobRenderer", renderer, "extends MobRenderer<");
		assertContains(className + " renderer uses custom texture path", renderer,
				"textures/entity/puppeteer_summon/" + id + ".png");
		assertContains(className + " renderer preserves dismissal skip", renderer,
				"PuppeteerSummonRenderHelper.shouldSkipRender(entity)");
		assertContains(className + " renderer preserves dismissal scale", renderer,
				"PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack)");
		assertDoesNotContain(className + " renderer no longer imports vanilla renderer", renderer, forbiddenRenderer);
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertPng(String label, Path path) throws IOException {
		assertExists(label, path);
		byte[] bytes = Files.readAllBytes(path);
		byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		for (int i = 0; i < pngHeader.length; i++) {
			if (bytes.length <= i || bytes[i] != pngHeader[i]) {
				throw new AssertionError(label + ": not a PNG file " + path);
			}
		}
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
