package com.vincenthuto.hemomancy.common.block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModelFirstTileRendererResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private ModelFirstTileRendererResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertModelFirstBlock("mycelial_lantern",
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/MycelialLanternBlock.java");
		assertModelFirstBlock("somatic_loom",
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomBlock.java");
		assertModelFirstBlock("scar_station",
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/ScarStationBlock.java");
		assertConvertedBlockModel("mycelial_lantern", "MycelialLanternModel", 14, "glass_chamber",
				"lantern_core_1");
		assertContains("mycelial lantern block model should expose GUI transforms for Blockbench editing",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/mycelial_lantern.json")),
				"\"display\"");
		assertContains("mycelial lantern block model should light inventory preview from the front",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/mycelial_lantern.json")),
				"\"gui_light\": \"front\"");
		assertContains("mycelial lantern Blockbench source should expose display transforms",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/mycelial_lantern.bbmodel")),
				"\"display\"");
		assertBlockAndBlockbenchDisplayTransforms("ghastly_alembic");
		assertBlockAndBlockbenchDisplayTransforms("pallid_retort");
		assertBlockAndBlockbenchDisplayTransforms("somatic_loom");
		assertBlockAndBlockbenchDisplayTransforms("altar_of_cleansing");
		assertBlockAndBlockbenchDisplayTransforms("morphling_incubator");
		assertConvertedBlockModel("somatic_loom", "SomaticLoomModel", 43, "crank_left_0",
				"weave_bed_0");
		assertConvertedBlockModel("scar_station", "ScarStationModel", 27, "shelf_books",
				"desk_scroll_1");
		assertContains("scar station should disable baked-model AO to match the old entity renderer lighting",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
				"\"ambientocclusion\": false");
		assertDoesNotContain("scar station should keep normal directional face shading",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
				"\"shade\": false");
		assertContains("scar station block model should expose GUI transforms for Blockbench editing",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
				"\"display\"");
		assertContains("scar station block model should light inventory preview from the front",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
				"\"gui_light\": \"front\"");
		assertContains("scar station should keep the angled chiseling surface instead of a flattened AABB",
				elementBlock(read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
						"desk_scroll_1"),
				"\"axis\": \"x\"");
		assertContains("scar station should approximate the old 42.5 degree desk-surface pitch",
				elementBlock(read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/scar_station.json")),
						"desk_scroll_1"),
				"\"angle\": 45");
		assertDoesNotContain("somatic loom baked model should not keep transparent zero-thickness sheet artifacts",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/somatic_loom.json")),
				"\"name\": \"weave_bed_flat\"");
		assertDoesNotContain("somatic loom baked model should not keep rotated transparent sheet artifacts",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/somatic_loom.json")),
				"\"name\": \"weave_bed_r1_aabb\"");
		assertModelFirstBlock("ghastly_alembic",
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/GhastlyAlembicBlock.java");
		assertModelFirstBlock("pallid_retort",
				"com/vincenthuto/hemomancy/common/block/unstained/crafting/PallidRetortBlock.java");
		assertModelFirstBlock("morphling_incubator",
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/MorphlingIncubatorBlock.java");
		assertModelFirstBlock("altar_of_cleansing",
				"com/vincenthuto/hemomancy/common/block/unstained/functional/AltarOfCleansingBlock.java");
		assertModelFirstBlock("morphling_cradle",
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/MorphlingCradleBlock.java");
		assertModelFirstBlock("specimen_jar",
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java");
		assertConvertedBlockModel("ghastly_alembic", "GhastlyAlembicModel", 24, "flask_0",
				"pipe_rod_3");
		assertConvertedBlockModel("pallid_retort", "PallidRetortModel", 24, "flask_0",
				"pipe_rod_3");
		assertFaceHasVisibleAlpha("ghastly_alembic", "flask_0", "up");
		assertFaceHasVisibleAlpha("pallid_retort", "flask_0", "up");
		assertConvertedBlockModel("morphling_incubator", "MorphlingIncubatorModel", 18, "tank_4",
				"floor_vials_3");
		assertConvertedBlockModel("altar_of_cleansing", "CleansingAltarModel", 60, "pale_lady_14",
				"gothic_arch_17");
		assertConvertedBlockModel("morphling_cradle", "MorphlingCradleModel", 7, "arms_2",
				"retainer_1");
		assertConvertedBlockModel("specimen_jar", "SpecimenJarModel", 8, "frame_6",
				"glass_0");
		assertBlockAndBlockbenchDisplayTransforms("morphling_cradle");
		assertBlockAndBlockbenchDisplayTransforms("specimen_jar");
		assertDoesNotExist("somatic loom should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/SomaticLoomModel.bbmodel"));
		assertDoesNotExist("scar station should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/ChiselStationModel.bbmodel"));
		assertDoesNotExist("scar station should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/ScarStationBlockItem.java"));
		assertDoesNotExist("ghastly alembic should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/GhastlyAlembicModel.bbmodel"));
		assertDoesNotExist("pallid retort should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/ModelPallidRetort.bbmodel"));
		assertDoesNotExist("morphling incubator should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/MorphlingIncubatorModel.bbmodel"));
		assertDoesNotExist("morphling incubator should not keep a stale separate item Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/bbmodel/morphling_incubator.bbmodel"));
		assertDoesNotExist("altar of cleansing should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/AltarOfCleansingModel.bbmodel"));
		assertDoesNotExist("morphling cradle should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/MorphlingCradleModel.bbmodel"));
		assertDoesNotExist("specimen jar should not keep the stale entity-format Blockbench source",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/SpecimenJarModel.bbmodel"));
		assertDoesNotExist("ghastly alembic should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/GhastlyAlembicBlockItem.java"));
		assertDoesNotExist("pallid retort should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/unstained/tile/PallidRetortBlockItem.java"));
		assertDoesNotExist("morphling incubator should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/MorphlingIncubatorBlockItem.java"));
		assertDoesNotExist("altar of cleansing should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/unstained/tile/AltarOfCleansingBlockItem.java"));
		assertDoesNotExist("morphling cradle should not keep a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/MorphlingCradleBlockItem.java"));

		assertDoesNotExist("mycelial lantern no longer needs a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/MycelialLanternBlockItem.java"));
		assertDoesNotExist("somatic loom no longer needs a custom block item",
				SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/tile/SomaticLoomBlockItem.java"));

		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		assertDoesNotContain("mycelial lantern should use generic BlockItem registration", blockInit,
				"new MycelialLanternBlockItem");
		assertDoesNotContain("somatic loom should use generic BlockItem registration", blockInit,
				"new SomaticLoomBlockItem");
		assertDoesNotContain("scar station should use generic BlockItem registration", blockInit,
				"new ScarStationBlockItem");
		assertDoesNotContain("ghastly alembic should use generic BlockItem registration", blockInit,
				"new GhastlyAlembicBlockItem");
		assertDoesNotContain("pallid retort should use generic BlockItem registration", blockInit,
				"new PallidRetortBlockItem");
		assertDoesNotContain("morphling incubator should use generic BlockItem registration", blockInit,
				"new MorphlingIncubatorBlockItem");
		assertDoesNotContain("altar of cleansing should use generic BlockItem registration", blockInit,
				"new AltarOfCleansingBlockItem");
		assertDoesNotContain("morphling cradle should use generic BlockItem registration", blockInit,
				"new MorphlingCradleBlockItem");

		String specimenJarBlockItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tile/SpecimenJarBlockItem.java"));
		assertContains("specimen jar item keeps capture behavior", specimenJarBlockItem,
				"interactLivingEntity");
		assertContains("specimen jar item keeps specimen tooltip behavior", specimenJarBlockItem,
				"SpecimenJarData.getSpecimenName");
		assertContains("specimen jar item should request the dynamic specimen item renderer",
				specimenJarBlockItem, "SpecimenJarItemRenderer");
		assertContains("specimen jar item should expose a custom item renderer",
				specimenJarBlockItem, "getCustomRenderer");

		String lanternRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/MycelialLanternRenderer.java"));
		assertContains("mycelial lantern keeps dynamic displayed stack rendering", lanternRenderer,
				"renderDisplayItem(te, display, partialTick, poseStack, buffers, itemLight, overlay);");
		assertDoesNotContain("mycelial lantern renderer should not render the static model body", lanternRenderer,
				"MycelialLanternModel");

		String loomRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/SomaticLoomRenderer.java"));
		assertContains("somatic loom keeps floating item rendering", loomRenderer, "renderItems(te, partialTicks");
		assertContains("somatic loom keeps tendency star rendering", loomRenderer, "drawFractalStar(vc, mat, te");
		assertContains("somatic loom keeps enzyme ring rendering", loomRenderer, "drawEnzymeRings(vc, mat, te");
		assertDoesNotContain("somatic loom renderer should not render the static model body", loomRenderer,
				"SomaticLoomModel");

		String scarStationRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/ScarStationRenderer.java"));
		assertContains("scar station keeps dynamic displayed slot rendering", scarStationRenderer,
				"renderStatic(stack, ItemDisplayContext.GROUND");
		assertDoesNotContain("scar station renderer must not render the baked block model twice", scarStationRenderer,
				"renderStationModel");
		assertDoesNotContain("scar station renderer should not render the static model body", scarStationRenderer,
				"ScarStationModel");

		String ghastlyRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/GhastlyAlembicRenderer.java"));
		assertContains("ghastly alembic keeps dynamic fluid rendering", ghastlyRenderer, "renderFluidLevel");
		assertDoesNotContain("ghastly alembic renderer should not render the static model body", ghastlyRenderer,
				"GhastlyAlembicModel");

		String pallidRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/PallidRetortRenderer.java"));
		assertContains("pallid retort keeps dynamic fluid rendering", pallidRenderer, "renderFluidLevel");
		assertDoesNotContain("pallid retort renderer should not render the static model body", pallidRenderer,
				"PallidRetortModel");

		String incubatorRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/MorphlingIncubatorRenderer.java"));
		assertContains("morphling incubator keeps dynamic fluid rendering", incubatorRenderer, "renderFluidLevel");
		assertContains("morphling incubator keeps dynamic creature rendering", incubatorRenderer, "renderCreature");
		assertDoesNotContain("morphling incubator renderer should not render the static model body", incubatorRenderer,
				"renderIncubatorModel");

		String altarRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/AltarOfCleansingRenderer.java"));
		assertDoesNotContain("altar of cleansing renderer should not render the static model body", altarRenderer,
				"CleansingAltarModel");

		String cradleRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/MorphlingCradleRenderer.java"));
		assertContains("morphling cradle keeps dynamic hosted stack rendering", cradleRenderer,
				"getHostedMorphling");
		assertContains("morphling cradle keeps dynamic item rendering", cradleRenderer,
				"renderStatic(null, hosted");
		assertDoesNotContain("morphling cradle renderer should not render the static model body", cradleRenderer,
				"MorphlingCradleModel");

		String specimenJarRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/SpecimenJarRenderer.java"));
		assertContains("specimen jar keeps dynamic captured specimen rendering", specimenJarRenderer,
				"renderSpecimen");
		assertDoesNotContain("specimen jar renderer should not render the static model body", specimenJarRenderer,
				"SpecimenJarModel");

		String specimenJarItemModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/specimen_jar.json"));
		assertContains("specimen jar item model should allow the custom item renderer",
				specimenJarItemModel, "\"parent\": \"builtin/entity\"");

		String altarBlockstate = read(RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/altar_of_cleansing.json"));
		String compactAltarBlockstate = altarBlockstate.replaceAll("\\s+", "");
		assertContains("altar model should rotate 180 degrees for north-facing placement", compactAltarBlockstate,
				"\"apply\":{\"model\":\"hemomancy:block/altar_of_cleansing\",\"y\":180},\"when\":{\"facing\":\"north\"}");
		assertContains("altar model should leave south-facing placement unrotated", compactAltarBlockstate,
				"\"apply\":{\"model\":\"hemomancy:block/altar_of_cleansing\"},\"when\":{\"facing\":\"south\"}");

		String scarBlockstate = read(RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/scar_station.json"));
		String compactScarBlockstate = scarBlockstate.replaceAll("\\s+", "");
		assertContains("scar station model should rotate 180 degrees for north-facing placement", compactScarBlockstate,
				"\"apply\":{\"model\":\"hemomancy:block/scar_station\",\"y\":180},\"when\":{\"facing\":\"north\"}");
		assertContains("scar station model should leave south-facing placement unrotated", compactScarBlockstate,
				"\"apply\":{\"model\":\"hemomancy:block/scar_station\"},\"when\":{\"facing\":\"south\"}");
		assertContains("scar station west-facing placement should rotate like the corrected baked stations", compactScarBlockstate,
				"\"y\":90},\"when\":{\"facing\":\"west\"}");
		assertContains("scar station east-facing placement should rotate like the corrected baked stations", compactScarBlockstate,
				"\"y\":270},\"when\":{\"facing\":\"east\"}");
	}

	private static void assertModelFirstBlock(String id, String blockSourcePath) throws IOException {
		String blockModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/" + id + ".json"));
		String itemModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + id + ".json"));
		String blockSource = read(SOURCE_ROOT.resolve(blockSourcePath));

		assertContains(id + " block model has explicit geometry", blockModel, "\"elements\"");
		assertContains(id + " block model renders through the translucent block path", blockModel,
				"\"render_type\": \"translucent\"");
		if (id.equals("specimen_jar")) {
			assertContains(id + " item keeps its contents-aware custom renderer", itemModel,
					"\"parent\": \"builtin/entity\"");
		} else {
			assertContains(id + " item model parents the baked block model", itemModel,
					"\"parent\": \"hemomancy:block/" + id + "\"");
			assertDoesNotContain(id + " item model should not request a custom item renderer", itemModel,
					"\"builtin/entity\"");
		}
		assertContains(id + " block should render its baked model", blockSource,
				"return RenderShape.MODEL;");
		assertDoesNotContain(id + " block should not suppress baked block rendering", blockSource,
				"return RenderShape.ENTITYBLOCK_ANIMATED;");
	}

	private static void assertBlockAndBlockbenchDisplayTransforms(String id) throws IOException {
		String blockModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/" + id + ".json"));
		String bbModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/" + id + ".bbmodel"));

		assertContains(id + " block model should expose GUI transforms for Blockbench editing", blockModel,
				"\"display\"");
		assertContains(id + " block model should light inventory preview from the front", blockModel,
				"\"gui_light\": \"front\"");
		assertContains(id + " block model should include editable scale data", blockModel,
				"\"scale\"");
		assertContains(id + " Blockbench source should expose display transforms", bbModel,
				"\"display\"");
	}

	private static void assertConvertedBlockModel(String id, String sourceModelName, int elementCount, String... names)
			throws IOException {
		String blockModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/" + id + ".json"));

		assertContains(id + " block model should retain source attribution", blockModel, "\"credit\"");
		assertContains(id + " block model should use a block-atlas texture", blockModel,
				"\"model\": \"hemomancy:block/model_" + id + "\"");
		assertContains(id + " block model should use a block-atlas particle texture", blockModel,
				"\"particle\": \"hemomancy:block/model_" + id + "\"");
		assertDoesNotContain(id + " baked model should not reference entity textures", blockModel,
				"hemomancy:entity/");
		assertDoesExist(id + " block-atlas texture should exist",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/block/model_" + id + ".png"));
		assertTextureHasPartialAlpha(id,
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/block/model_" + id + ".png"));
		assertEditableBlockbenchModel(id, sourceModelName);
		assertElementBounds(id, blockModel);
		assertEquals(id + " block model should preserve converted Java model boxes", elementCount,
				countOccurrences(blockModel, "\"name\""));
		for (String name : names) {
			assertContains(id + " block model should include converted part " + name, blockModel,
					"\"name\": \"" + name + "\"");
		}
	}

	private static void assertFaceHasVisibleAlpha(String id, String elementName, String faceName) throws IOException {
		String blockModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/" + id + ".json"));
		String element = elementBlock(blockModel, elementName);
		Matcher uvMatcher = Pattern.compile("\"" + faceName + "\"\\s*:\\s*\\{\\s*\"uv\"\\s*:\\s*\\[([^\\]]+)\\]")
				.matcher(element);
		if (!uvMatcher.find()) {
			throw new AssertionError(id + " element " + elementName + " should define " + faceName + " face UVs");
		}
		String[] values = uvMatcher.group(1).split(",");
		if (values.length != 4) {
			throw new AssertionError(id + " element " + elementName + " " + faceName + " UV should have four values");
		}

		BufferedImage image = ImageIO.read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/block/model_" + id + ".png").toFile());
		int x0 = uvToPixel(Math.min(Double.parseDouble(values[0].trim()), Double.parseDouble(values[2].trim())),
				image.getWidth());
		int x1 = uvToPixelCeil(Math.max(Double.parseDouble(values[0].trim()), Double.parseDouble(values[2].trim())),
				image.getWidth());
		int y0 = uvToPixel(Math.min(Double.parseDouble(values[1].trim()), Double.parseDouble(values[3].trim())),
				image.getHeight());
		int y1 = uvToPixelCeil(Math.max(Double.parseDouble(values[1].trim()), Double.parseDouble(values[3].trim())),
				image.getHeight());
		int visiblePixels = 0;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				if (((image.getRGB(x, y) >>> 24) & 0xFF) > 0) {
					visiblePixels++;
				}
			}
		}
		if (visiblePixels == 0) {
			throw new AssertionError(id + " element " + elementName + " " + faceName
					+ " face should not sample a fully transparent texture region");
		}
	}

	private static String elementBlock(String blockModel, String elementName) {
		int start = blockModel.indexOf("\"name\": \"" + elementName + "\"");
		if (start < 0) {
			throw new AssertionError("missing model element " + elementName);
		}
		int next = blockModel.indexOf("\"name\": \"", start + elementName.length() + 9);
		return next < 0 ? blockModel.substring(start) : blockModel.substring(start, next);
	}

	private static String blockstateVariant(String blockstate, String variantName) {
		int start = blockstate.indexOf("\"" + variantName + "\"");
		if (start < 0) {
			throw new AssertionError("missing blockstate variant " + variantName);
		}
		int next = blockstate.indexOf("\"facing=", start + variantName.length() + 2);
		return next < 0 ? blockstate.substring(start) : blockstate.substring(start, next);
	}

	private static int uvToPixel(double uv, int textureSize) {
		return Math.max(0, Math.min(textureSize, (int) Math.floor(uv / 16.0D * textureSize)));
	}

	private static int uvToPixelCeil(double uv, int textureSize) {
		return Math.max(0, Math.min(textureSize, (int) Math.ceil(uv / 16.0D * textureSize)));
	}

	private static void assertEditableBlockbenchModel(String id, String sourceModelName) throws IOException {
		String bbModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/bbmodel/" + id + ".bbmodel"));
		String compactBbModel = bbModel.replace(" ", "");

		assertContains(id + " should have an identified Blockbench source", compactBbModel,
				"\"name\":\"" + id + "\"");
		assertContains(id + " Blockbench source should use a block-editable model format", compactBbModel,
				"\"model_format\":\"java_block\"");
		assertDoesNotContain(id + " Blockbench source should not use the entity model format", bbModel,
				"\"model_format\":\"modded_entity\"");
		assertContains(id + " Blockbench source should reference the baked block texture", compactBbModel,
				"\"relative_path\":\"../../../textures/block/model_" + id + ".png\"");
	}

	private static String read(Path path) throws IOException {
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

	private static void assertDoesNotExist(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": found " + path);
		}
	}

	private static void assertDoesExist(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertElementBounds(String id, String blockModel) {
		Pattern coordinateArray = Pattern.compile("\"(?:from|to)\"\\s*:\\s*\\[([^\\]]+)\\]");
		Matcher matcher = coordinateArray.matcher(blockModel);
		int arrays = 0;
		while (matcher.find()) {
			arrays++;
			String[] values = matcher.group(1).split(",");
			assertEquals(id + " coordinate arrays should have three values", 3, values.length);
			for (String value : values) {
				double coordinate = Double.parseDouble(value.trim());
				if (coordinate < -16.0D || coordinate > 32.0D) {
					throw new AssertionError(id + " block model coordinate should fit vanilla element bounds: "
							+ coordinate);
				}
			}
		}
		if (arrays == 0) {
			throw new AssertionError(id + " block model should define element coordinate arrays");
		}
	}

	private static void assertTextureHasPartialAlpha(String id, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(id + " block-atlas texture should be readable as a PNG: " + path);
		}
		int partialAlphaPixels = 0;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
				if (alpha > 0 && alpha < 255) {
					partialAlphaPixels++;
				}
			}
		}
		if (partialAlphaPixels == 0) {
			throw new AssertionError(id + " block-atlas texture should include partial alpha for translucent rendering");
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but found " + actual);
		}
	}

	private static int countOccurrences(String text, String needle) {
		int count = 0;
		int index = text.indexOf(needle);
		while (index >= 0) {
			count++;
			index = text.indexOf(needle, index + needle.length());
		}
		return count;
	}
}
