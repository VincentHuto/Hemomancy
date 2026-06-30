package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LowtideRuinObjAssetResourceTest {
	private static final Path MODEL_ROOT = Path.of("src/main/resources/assets/hemomancy/models/world/lowtide_ruin");
	private static final Path OBJ_ROOT = MODEL_ROOT.resolve("obj");
	private static final Path TEXTURE_ROOT = Path.of("src/main/resources/assets/hemomancy/textures/world/lowtide_ruin");
	private static final Path BLOCK_ATLAS = Path.of("src/main/resources/assets/minecraft/atlases/blocks.json");
	private static final Path LOWTIDE_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java");
	private static final Path MODEL_CACHE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/LowtideRuinObjModels.java");
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
	private static final Path CLIENT_CONFIG = Path.of("src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");
	private static final String[] PIECES = {
			"foundation_rubble",
			"tower_short",
			"arch_fragment",
			"dome_chapel",
			"spire_fragment"
	};
	private static final int[] PIECE_FACE_LIMITS = {
			1500,
			1250,
			900,
			1600,
			1000
	};

	private LowtideRuinObjAssetResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		for (int index = 0; index < PIECES.length; index++) {
			String piece = PIECES[index];
			assertModelJson(piece);
			assertObjSet(piece, PIECE_FACE_LIMITS[index]);
			assertTexture(piece);
		}
		assertTexture("tower_short_e");

		String towerMtl = read(OBJ_ROOT.resolve("tower_short.mtl"));
		assertContains("tower_short mat0 uses main texture", towerMtl, "map_Kd hemomancy:world/lowtide_ruin/tower_short");
		assertContains("tower_short mat1 uses emissive-source texture as normal diffuse art", towerMtl,
				"map_Kd hemomancy:world/lowtide_ruin/tower_short_e");
		assertNotContains("tower_short mtl should not keep converter placeholder diffuse", towerMtl, "Diffuse.png");
		assertNotContains("tower_short mtl should not keep converter placeholder entity diffuse", towerMtl,
				"EntityDiffuse.png");

		String effects = read(LOWTIDE_EFFECTS);
		String modelCache = read(MODEL_CACHE);
		String clientEvents = read(CLIENT_EVENTS);
		String clientConfig = read(CLIENT_CONFIG);
		String reference = read(REFERENCE);
		String blockAtlas = read(BLOCK_ATLAS);

		assertContains("client config exposes Lowtide ruin quality enum", clientConfig,
				"public enum LowtideRuinStructureQuality");
		assertContains("client config defines Lowtide ruin quality option", clientConfig,
				"LOWTIDE_RUIN_STRUCTURE_QUALITY");
		assertContains("client config defaults Lowtide ruin quality to high", clientConfig,
				"LowtideRuinStructureQuality.HIGH");

		assertNotContains("renderer should not draw procedural ruin structures anymore", effects,
				"renderLowtideProceduralRuinCluster(");
		assertContains("renderer replaces ruin silhouettes with OBJ far and distant field", effects,
				"renderLowtideObjRuinField(poseStack, time, skyDistance, quality);");
		assertContains("renderer draws no oversized close-to-player OBJ ring", effects,
				"for (int cluster = 0; cluster < farClusterCount; cluster++)");
		assertContains("renderer draws distant OBJ ruins", effects,
				"for (int cluster = 0; cluster < distantClusterCount; cluster++)");
		assertContains("renderer has a high-poly far OBJ ruin cluster count", effects,
				"private static final int LOWTIDE_RUIN_HIGH_FAR_CLUSTER_COUNT = 127;");
		assertContains("renderer has a high-poly distant OBJ ruin cluster count", effects,
				"private static final int LOWTIDE_RUIN_HIGH_DISTANT_CLUSTER_COUNT = 12;");
		assertContains("renderer has a low-poly far OBJ ruin cluster count", effects,
				"private static final int LOWTIDE_RUIN_LOW_FAR_CLUSTER_COUNT = 27;");
		assertContains("renderer has a low-poly distant OBJ ruin cluster count", effects,
				"private static final int LOWTIDE_RUIN_LOW_DISTANT_CLUSTER_COUNT = 6;");
		assertContains("renderer reads the Lowtide ruin quality config", effects,
				"HemoClientConfig.lowtideRuinStructureQuality()");
		assertContains("renderer can skip OBJ ruins from quality config", effects,
				"LowtideRuinStructureQuality.OFF");
		assertContains("renderer makes OBJ arches rare", effects,
				"if (cluster % 5 == 0) {");
		assertContains("renderer keeps Lowtide OBJ draw count bounded to one model per cluster", effects,
				"LowtideRuinObjModels.Piece piece = lowtideRuinObjPieceForCluster(cluster);");
		assertContains("renderer picks per-piece model scale once per cluster", effects,
				"float modelScale = scale * lowtideRuinObjScale(piece);");
		assertContains("renderer caches OBJ ruin geometry in a static vertex buffer", effects,
				"private static VertexBuffer lowtideRuinObjVertexBuffer;");
		assertContains("renderer marks OBJ ruin cache dirty for model reloads", effects,
				"private static boolean lowtideRuinObjVertexBufferDirty = true;");
		assertContains("renderer tracks cached OBJ ruin quality", effects,
				"private static LowtideRuinStructureQuality lowtideRuinObjVertexBufferQuality");
		assertContains("renderer uploads OBJ ruin geometry once to a static GPU buffer", effects,
				"new VertexBuffer(VertexBuffer.Usage.STATIC);");
		assertContains("renderer binds the OBJ vertex buffer before upload so VAO state is valid", effects,
				"lowtideRuinObjVertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);\n"
						+ "\t\tlowtideRuinObjVertexBuffer.bind();\n"
						+ "\t\tlowtideRuinObjVertexBuffer.upload(meshData);");
		assertContains("renderer draws cached OBJ ruin geometry instead of rebuilding every frame", effects,
				"lowtideRuinObjVertexBuffer.drawWithShader(");
		assertContains("renderer rebuilds cached OBJ ruin geometry when quality changes", effects,
				"lowtideRuinObjVertexBufferQuality == quality");
		assertNotContains("OBJ ruin active render should not use per-frame block model renderer", effects,
				"getBlockRenderer().getModelRenderer().renderModel");
		assertContains("model cache invalidates cached Lowtide OBJ geometry", modelCache,
				"MnemonicLowtideChamberEffects.invalidateLowtideObjRuinCache();");
		assertNotContains("renderer should not build multi-model OBJ cities per cluster", effects,
				"renderLowtideObjPiece(poseStack, minecraft, consumer, state, LowtideRuinObjModels.Piece.FOUNDATION_RUBBLE,");
		assertNotContains("renderer should not always add a tower to every OBJ cluster", effects,
				"renderLowtideObjPiece(poseStack, minecraft, consumer, state, LowtideRuinObjModels.Piece.TOWER_SHORT,");
		assertNotContains("renderer should not use the previous close OBJ distance band", effects,
				"0.42F, 0.68F");
		assertContains("far OBJ ruins use distance-faded scale bands", effects,
				"float scale = skyDistance * Mth.lerp(distanceFade, Mth.lerp(random.nextFloat(), 0.026F, 0.040F),");
		assertContains("distant OBJ ruins are smallest", effects,
				"float scale = skyDistance * Mth.lerp(random.nextFloat(), 0.007F, 0.014F);");
		assertContains("far OBJ ruins are darker than foreground hero values", effects,
				"LOWTIDE_RUIN_FAR_LIGHT");
		assertContains("distant OBJ ruins are darkest", effects,
				"LOWTIDE_RUIN_DISTANT_LIGHT");
		assertContains("renderer draws lowtide ruins before the glossy lake overlay", effects,
				"renderLowtideRuinedStructures(context.poseStack(), context.time(), context.skyDistance());\n"
						+ "\t\trenderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());");
		assertContains("OBJ ruins render with culling enabled to avoid inside-out backsides", effects,
				"private static void renderLowtideObjRuinField(PoseStack poseStack, float time, float skyDistance,\n"
						+ "\t\t\tLowtideRuinStructureQuality quality) {\n\t\tRenderSystem.enableCull();");
		assertNotContains("OBJ ruins should not disable culling for solid model rendering", effects,
				"private static void renderLowtideObjRuinField(PoseStack poseStack, float time, float skyDistance,\n"
						+ "\t\t\tLowtideRuinStructureQuality quality) {\n\t\tRenderSystem.disableCull();");
		assertContains("OBJ ruins get depth-tested before the lake overlay", effects,
				"RenderSystem.enableDepthTest();\n\t\tRenderSystem.depthMask(true);\n\t\tLowtideRuinStructureQuality quality = HemoClientConfig.lowtideRuinStructureQuality();");
		assertContains("lowtide lake ignores ruin depth so it still overlays the structures", effects,
				"static void renderLowtideSkyLake(PoseStack poseStack, float time, float skyDistance) {\n"
						+ "\t\tRenderSystem.enableBlend();\n\t\tRenderSystem.disableCull();\n\t\tRenderSystem.disableDepthTest();");
		assertContains("model cache registers foundation OBJ model", modelCache,
				"world/lowtide_ruin/foundation_rubble");
		assertContains("client events register lowtide OBJ models", clientEvents,
				"LowtideRuinObjModels.register(event);");
		assertContains("client events cache lowtide OBJ models", clientEvents,
				"LowtideRuinObjModels.cache(evt);");
		assertContains("reference documents OBJ lowtide ruin field", reference,
				"OBJ-only flooded ruin field");
		assertContains("block atlas stitches lowtide OBJ texture directory", blockAtlas,
				"\"source\": \"world/lowtide_ruin\"");
		assertFileDoesNotExist("old unused tower broken OBJ should not stay in active asset set",
				OBJ_ROOT.resolve("tower_broken.obj"));
		assertFileDoesNotExist("old unused tower broken material should not stay in active asset set",
				OBJ_ROOT.resolve("tower_broken.mtl"));
	}

	private static void assertModelJson(String piece) throws IOException {
		Path path = MODEL_ROOT.resolve(piece + ".json");
		assertFileExists(piece + " model json", path);
		String json = read(path);
		assertContains(piece + " model json uses NeoForge default item parent", json,
				"\"parent\": \"neoforge:item/default\"");
		assertNotContains(piece + " model json should not use obsolete Forge default item parent", json,
				"\"parent\": \"forge:item/default\"");
		assertContains(piece + " model json uses NeoForge composite loader", json,
				"\"loader\": \"neoforge:composite\"");
		assertContains(piece + " model json uses NeoForge obj loader", json, "\"loader\": \"neoforge:obj\"");
		assertNotContains(piece + " model json should not use obsolete Forge loader namespace", json,
				"\"loader\": \"forge:");
		assertContains(piece + " model json disables automatic culling", json, "\"automatic_culling\": false");
		assertContains(piece + " model json flips v", json, "\"flip_v\": true");
		assertContains(piece + " model json is non-emissive", json, "\"emissive_ambient\": false");
		assertContains(piece + " model json points to obj", json,
				"\"model\": \"hemomancy:models/world/lowtide_ruin/obj/" + piece + ".obj\"");
	}

	private static void assertObjSet(String piece, int faceLimit) throws IOException {
		Path obj = OBJ_ROOT.resolve(piece + ".obj");
		Path mtl = OBJ_ROOT.resolve(piece + ".mtl");
		assertFileExists(piece + " obj", obj);
		assertFileExists(piece + " mtl", mtl);
		String objText = read(obj);
		String mtlText = read(mtl);
		assertContains(piece + " obj references normalized mtl", objText, "mtllib " + piece + ".mtl");
		assertAtMost(piece + " low-poly obj face count", countObjFaces(objText), faceLimit);
		assertContains(piece + " mtl uses Hemomancy texture path", mtlText,
				"map_Kd hemomancy:world/lowtide_ruin/" + piece);
		assertNotContains(piece + " mtl should not reference desktop/source file names", mtlText,
				"lowtide_ruin_" + piece + "_new.png");
		assertNotContains(piece + " mtl should not reference raw png paths", mtlText, ".png");
	}

	private static void assertTexture(String name) {
		assertFileExists(name + " texture", TEXTURE_ROOT.resolve(name + ".png"));
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertFileDoesNotExist(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}

	private static int countObjFaces(String objText) {
		int count = 0;
		for (String line : objText.split("\n")) {
			if (line.startsWith("f ")) {
				count++;
			}
		}
		return count;
	}

	private static void assertAtMost(String label, int actual, int limit) {
		if (actual > limit) {
			throw new AssertionError(label + ": expected at most " + limit + " but got " + actual);
		}
	}
}
