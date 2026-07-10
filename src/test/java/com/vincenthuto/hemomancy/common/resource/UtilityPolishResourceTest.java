package com.vincenthuto.hemomancy.common.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class UtilityPolishResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path SRC = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private UtilityPolishResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertObjectModel("ossuary clock", ASSETS.resolve("models/block/ossuary_clock.json"), 8);
		assertObjectModel("humoral barometer", ASSETS.resolve("models/block/humoral_barometer.json"), 5);
		assertObjectModel("mnemonic candle bowl", ASSETS.resolve("models/block/mnemonic_candle.json"), 3);
		assertObjectModel("lethean poppy wreath", ASSETS.resolve("models/block/lethean_poppy_wreath.json"), 12);
		assertOssuaryClockIsTwoBlocksTall();
		assertBarometerIsWallMounted();
		assertMycelialCrucibleAvoidsCrossBlockLightingSeams();
		assertMycelialCrucibleHasRotatingWideFootprint();
		assertMycelialCrucibleHasRotatingSpoutParticles();
		assertMycelialCrucibleHasBasinShader();
		assertLetheanPoppyWreathIsWallMountedDecoration();
		assertMnemonicCandleHasWickParticles();
		assertBloodChumIsThrownWaterAreaUtility();
		assertDicentraSapLegacyOnlyAcrossData();
	}

	private static void assertObjectModel(String label, Path path, int minElements) throws IOException {
		String model = read(path);
		assertNotContains(label + " should not use cube_all", model, "\"parent\": \"minecraft:block/cube_all\"");
		int elements = model.split("\"from\"").length - 1;
		if (elements < minElements) {
			throw new AssertionError(label + " needs a proper 3D model; found " + elements + " elements");
		}
	}

	private static void assertBarometerIsWallMounted() throws IOException {
		String block = read(SRC.resolve("common/block/shared/HumoralBarometerBlock.java"));
		String blockstate = read(ASSETS.resolve("blockstates/humoral_barometer.json"));
		assertContains("barometer has horizontal facing", block, "HorizontalDirectionalBlock.FACING");
		assertContains("barometer rejects floor placement", block, "clickedFace.getAxis().isVertical()");
		assertContains("barometer uses model-facing opposite clicked wall face", block, "clickedFace.getOpposite()");
		assertContains("barometer checks backing wall", block, "canSurvive");
		assertContains("north-facing barometer shape hugs north wall", block,
				"NORTH_SHAPE = Block.box(2.0D, 1.0D, 0.0D, 14.0D, 15.0D, 3.0D)");
		assertContains("south-facing barometer shape hugs south wall", block,
				"SOUTH_SHAPE = Block.box(2.0D, 1.0D, 13.0D, 14.0D, 15.0D, 16.0D)");
		assertContains("west-facing barometer shape hugs west wall", block,
				"WEST_SHAPE = Block.box(0.0D, 1.0D, 2.0D, 3.0D, 15.0D, 14.0D)");
		assertContains("east-facing barometer shape hugs east wall", block,
				"EAST_SHAPE = Block.box(13.0D, 1.0D, 2.0D, 16.0D, 15.0D, 14.0D)");
		assertContains("barometer blockstate has north variant", blockstate, "facing=north");
		assertContains("barometer blockstate has rotation", blockstate, "\"y\": 180");
	}

	private static void assertMycelialCrucibleAvoidsCrossBlockLightingSeams() throws IOException {
		String model = read(ASSETS.resolve("models/block/mycelial_crucible.json"));
		assertContains("mycelial crucible model disables AO across its two-block visual", model,
				"\"ambientocclusion\": false");
		assertNotContains("mycelial crucible should keep normal directional face shading", model, "\"shade\": false");
		assertContains("mycelial crucible still intentionally spans the upper filler space", model, "32");
	}

	private static void assertMycelialCrucibleHasRotatingWideFootprint() throws IOException {
		String block = read(SRC.resolve("common/block/harbinger/crafting/MycelialCrucibleBlock.java"));
		assertContains("mycelial crucible uses rotation helper for filler footprint", block,
				"HorizontalFacingRotationHelper.rotateSouthOffsets(FILLER_OFFSETS, facing)");
		assertContains("mycelial crucible fills left lower cell", block, "new BlockPos(-1, 0, 0)");
		assertContains("mycelial crucible fills right lower cell", block, "new BlockPos(1, 0, 0)");
		assertContains("mycelial crucible fills left upper cell", block, "new BlockPos(-1, 1, 0)");
		assertContains("mycelial crucible fills center upper cell", block, "new BlockPos(0, 1, 0)");
		assertContains("mycelial crucible fills right upper cell", block, "new BlockPos(1, 1, 0)");
		assertContains("mycelial crucible placement checks rotated footprint", block,
				"canPlaceMultiBlock(level, pos, facing)");
		assertContains("mycelial crucible places rotated fillers", block, "rotatedFillerOffsets(facing)");
		assertContains("mycelial crucible links fillers to main block", block, "filler.setMainBlockPos(mainPos)");
		assertContains("mycelial crucible removes by original facing", block, "removeFillers(level, pos, state.getValue(FACING))");
		assertContains("mycelial crucible cleans up linked stale fillers", block, "removeLinkedFillerBlocks(level, mainPos)");
	}

	private static void assertMycelialCrucibleHasRotatingSpoutParticles() throws IOException {
		String block = read(SRC.resolve("common/block/harbinger/crafting/MycelialCrucibleBlock.java"));
		assertContains("mycelial crucible documents the left model spout", block, "chain_l");
		assertContains("mycelial crucible documents the right model spout", block, "chain_r");
		assertContains("mycelial crucible emits green hutoslib embers", block,
				"EmberParticleFactory.createData(ParticleColor.GREEN");
		assertContains("mycelial crucible emits hemomancy absorbed blood cells", block,
				"AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD)");
		assertContains("mycelial crucible rotates stream anchors by facing", block,
				"rotateModelPointForFacing");
		assertContains("mycelial crucible spawns particle trails", block, "spawnSpoutTrail");
	}

	private static void assertMycelialCrucibleHasBasinShader() throws IOException {
		String shaderInit = read(SRC.resolve("common/init/ShaderInit.java"));
		String renderTypes = read(SRC.resolve("client/render/HemoRenderTypes.java"));
		String renderer = read(SRC.resolve("client/render/tile/crafting/MycelialCrucibleRenderer.java"));
		String shaderJson = read(ASSETS.resolve("shaders/core/world/mycelial_crucible_basin.json"));
		String fragment = read(ASSETS.resolve("shaders/core/world/mycelial_crucible_basin.fsh"));
		String vertex = read(ASSETS.resolve("shaders/core/world/mycelial_crucible_basin.vsh"));
		assertContains("mycelial crucible basin shader is registered", shaderInit, "MYCELIAL_CRUCIBLE_BASIN");
		assertContains("mycelial crucible basin shader uses texture/color vertices", shaderInit,
				"DefaultVertexFormat.POSITION_TEX_COLOR");
		assertContains("mycelial crucible basin render type exists", renderTypes, "mycelialCrucibleBasin");
		assertContains("mycelial crucible basin render type sets shader uniforms", renderTypes,
				"mycelial_crucible_basin_uniforms");
		assertContains("mycelial crucible renderer draws basin shader quad", renderer, "renderBasinShader");
		assertContains("mycelial crucible renderer uses basin render type", renderer, "HemoRenderTypes.mycelialCrucibleBasin");
		assertContains("mycelial crucible renderer emits basin uv quad", renderer, "emitBasinVertex");
		assertContains("mycelial crucible basin shader uses subdivided geometry for wave motion", renderer,
				"BASIN_SUBDIVISIONS = 8");
		assertContains("mycelial crucible basin shader waves the quad surface", renderer, "basinWaveY");
		assertContains("mycelial crucible basin shader damps waves at the clipped edges", renderer, "edgeDamping");
		assertContains("mycelial crucible basin shader fills basin opening without corner clipping", renderer,
				"BASIN_MIN = 2.375F / 16.0F");
		assertContains("mycelial crucible basin shader fills basin opening without corner clipping", renderer,
				"BASIN_MAX = 13.625F / 16.0F");
		assertContains("mycelial crucible shader json references vertex shader", shaderJson,
				"\"vertex\": \"hemomancy:world/mycelial_crucible_basin\"");
		assertContains("mycelial crucible shader json references fragment shader", shaderJson,
				"\"fragment\": \"hemomancy:world/mycelial_crucible_basin\"");
		assertContains("mycelial crucible shader json exposes time uniform", shaderJson, "\"HemoTime\"");
		assertContains("mycelial crucible shader has red swirl", fragment, "redStream");
		assertContains("mycelial crucible shader has green swirl", fragment, "greenStream");
		assertContains("mycelial crucible shader passes uv coordinates", vertex, "texCoord0 = UV0");
	}

	private static void assertLetheanPoppyWreathIsWallMountedDecoration() throws IOException {
		String block = read(SRC.resolve("common/block/unstained/decor/LetheanPoppyWreathBlock.java"));
		String blockInit = read(SRC.resolve("common/init/BlockInit.java"));
		String itemInit = read(SRC.resolve("common/init/ItemInit.java"));
		String hemomancy = read(SRC.resolve("Hemomancy.java"));
		String blockstate = read(ASSETS.resolve("blockstates/lethean_poppy_wreath.json"));
		String itemModel = read(ASSETS.resolve("models/item/lethean_poppy_wreath.json"));
		String lang = read(ASSETS.resolve("lang/en_us.json"));
		String loot = read(DATA.resolve("loot_table/blocks/lethean_poppy_wreath.json"));
		String particleInit = read(SRC.resolve("common/init/ParticleInit.java"));
		String dripParticle = read(SRC.resolve("client/particle/LetheanDripParticle.java"));
		String dripParticleJson = read(ASSETS.resolve("particles/lethean_drip.json"));
		assertContains("wreath has horizontal facing", block, "HorizontalDirectionalBlock.FACING");
		assertContains("wreath rejects floor placement", block, "clickedFace.getAxis().isVertical()");
		assertContains("wreath uses model-facing opposite clicked wall face", block, "clickedFace.getOpposite()");
		assertContains("wreath checks backing wall", block, "canSurvive");
		assertContains("wreath emits occasional client drips", block, "animateTick");
		assertContains("wreath uses white lethean drip particles", block, "ParticleInit.lethean_drip.get()");
		assertNotContains("wreath drips should not be dust motes", block, "DustParticleOptions");
		assertContains("wreath drip particles are common but intermittent", block, "random.nextInt(10) != 0");
		assertContains("wreath emits still particles so the drip owns its fall", block,
				"ParticleInit.lethean_drip.get(), x, y, z, 0.0D, 0.0D, 0.0D");
		assertContains("wreath drips spawn on the mounted face", block, "z += facing.getStepZ() * frontOffset");
		assertContains("wreath drips spawn on the mounted face", block, "x += facing.getStepX() * frontOffset");
		assertContains("lethean drip particle is registered", particleInit, "lethean_drip");
		assertContains("lethean drip particle factory is registered", particleInit, "LetheanDripParticle.Provider::new");
		assertContains("lethean drip particle is white", dripParticle, "this.setColor(0.96F, 0.96F, 1.0F)");
		assertContains("lethean drip particle ignores inherited horizontal launch", dripParticle,
				"super(level, x, y, z, 0.0D, 0.0D, 0.0D)");
		assertContains("lethean drip particle falls straight down", dripParticle, "this.yd = -0.012D");
		assertContains("lethean drip particle has no sideways drift", dripParticle, "this.xd = 0.0D");
		assertContains("lethean drip particle has no sideways drift", dripParticle, "this.zd = 0.0D");
		assertContains("lethean drip particle uses drip sprite", dripParticleJson, "minecraft:drip_fall");
		assertContains("north-facing wreath shape hugs north wall", block,
				"NORTH_SHAPE = Block.box(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 3.0D)");
		assertContains("wreath block registered", blockInit, "lethean_poppy_wreath = MODELEDBLOCKS.register");
		assertContains("wreath item remains existing id as BlockItem", itemInit,
				"new BlockItem(BlockInit.lethean_poppy_wreath.get(), new Item.Properties().stacksTo(16))");
		assertContains("wreath block is not added twice to creative tab", hemomancy,
				"block != BlockInit.lethean_poppy_wreath.get()");
		assertContains("wreath blockstate has north variant", blockstate, "facing=north");
		assertContains("wreath blockstate has rotation", blockstate, "\"y\": 180");
		assertContains("wreath item keeps explicit generated geometry", itemModel, "\"elements\"");
		assertContains("wreath item uses the block texture", itemModel,
				"\"0\": \"hemomancy:block/lethean_poppy_wreath\"");
		assertNotContains("wreath model element rotations must use vanilla axis format", read(ASSETS.resolve("models/block/lethean_poppy_wreath.json")),
				"\"rotation\": {\"x\"");
		assertContains("wreath block localization exists", lang,
				"\"block.hemomancy.lethean_poppy_wreath\": \"Lethean Poppy Wreath\"");
		assertContains("wreath drops itself", loot, "hemomancy:lethean_poppy_wreath");
	}

	private static void assertOssuaryClockIsTwoBlocksTall() throws IOException {
		String block = read(SRC.resolve("common/block/shared/OssuaryClockBlock.java"));
		String blockstate = read(ASSETS.resolve("blockstates/ossuary_clock.json"));
		String lowerModel = read(ASSETS.resolve("models/block/ossuary_clock.json"));
		String upperModel = read(ASSETS.resolve("models/block/ossuary_clock_upper.json"));
		assertContains("ossuary clock has upper/lower half state", block, "DoubleBlockHalf");
		assertContains("ossuary clock has horizontal facing state", block, "HorizontalDirectionalBlock.FACING");
		assertContains("ossuary clock faces the placing player", block, "context.getHorizontalDirection().getOpposite()");
		assertContains("ossuary clock copies facing into upper filler half", block, ".setValue(FACING, state.getValue(FACING))");
		assertContains("ossuary clock rotates facing", block, "rotation.rotate(state.getValue(FACING))");
		assertContains("ossuary clock places upper filler half", block, "pos.above()");
		assertContains("ossuary clock avoids floor face culling voids", block, "getOcclusionShape");
		assertContains("ossuary clock blockstate has lower half model", blockstate, "half=lower");
		assertContains("ossuary clock blockstate has upper half model", blockstate, "half=upper");
		assertContains("ossuary clock blockstate rotates north facing", blockstate, "facing=north");
		assertContains("ossuary clock blockstate rotates east facing", blockstate, "facing=east");
		assertContains("ossuary clock blockstate rotates south facing", blockstate, "facing=south");
		assertContains("ossuary clock blockstate rotates west facing", blockstate, "facing=west");
		assertNotContains("ossuary clock lower model must not render through upper filler space", lowerModel, "32");
		assertNotContains("ossuary clock lower model must stay inside lower block light volume", lowerModel, "29");
		assertContains("ossuary clock upper half renders its own geometry", upperModel, "\"from\"");
		assertContains("ossuary clock upper model has normalized top cap", upperModel, "        16,\n        16,\n        16");
		assertContains("ossuary clock lower model disables cross-half AO", lowerModel, "\"ambientocclusion\": false");
		assertContains("ossuary clock upper model disables cross-half AO", upperModel, "\"ambientocclusion\": false");
		assertJsonExists(ASSETS.resolve("models/block/ossuary_clock_upper.json"));
	}

	private static void assertMnemonicCandleHasWickParticles() throws IOException {
		String block = read(SRC.resolve("common/block/shared/MnemonicCandleBlock.java"));
		assertContains("mnemonic candle emits client wick particles", block, "animateTick");
		assertContains("mnemonic candle smoke particle", block, "ParticleTypes.SMOKE");
		assertContains("mnemonic candle flame particle", block, "ParticleTypes.SMALL_FLAME");
	}

	private static void assertBloodChumIsThrownWaterAreaUtility() throws IOException {
		String item = read(SRC.resolve("common/item/shared/BloodChumItem.java"));
		String entityInit = read(SRC.resolve("common/init/EntityInit.java"));
		String mixin = read(SRC.resolve("mixin/core/MixinFishingHook.java"));
		String clientEvents = read(SRC.resolve("client/event/ClientEvents.java"));
		String areaManager = read(SRC.resolve("common/effect/ChummedWatersAreaManager.java"));
		assertNotContains("blood chum should not add player mob effects", item, "addEffect");
		assertContains("blood chum throws projectile", item, "BloodChumEntity");
		assertContains("blood chum projectile registered", entityInit, "blood_chum_projectile");
		assertContains("blood chum projectile rendered", clientEvents, "EntityInit.blood_chum_projectile.get()");
		assertContains("fishing hook checks chummed water area", mixin, "ChummedWatersAreaManager.isChummed");
		assertContains("chummed waters emits ambient particles", areaManager, "ServerTickEvent.Post");
		assertContains("chummed waters uses visible dye-like particles", areaManager, "DustParticleOptions");
	}

	private static void assertJsonExists(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		read(path);
	}

	private static void assertDicentraSapLegacyOnlyAcrossData() throws IOException {
		try (Stream<Path> paths = Files.walk(DATA)) {
			for (Path path : paths.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".json")).toList()) {
				String normalized = path.toString().replace('\\', '/');
				String text = read(path);
				if (text.contains("hemomancy:dicentra_sap")
						&& !normalized.endsWith("recipe/distillation/dicentra_sap.json")) {
					throw new AssertionError("non-legacy dicentra sap data reference remains: " + path);
				}
			}
		}
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
