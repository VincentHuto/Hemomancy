package com.vincenthuto.hemomancy.common.entity.summon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MorphlingPolypResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOC_ROOT = Path.of("docs");

	private MorphlingPolypResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertExists("layer enum", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypLayer.java"));
		assertExists("layer rules", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypLayerRules.java"));
		assertExists("appendage model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingPolypLayerModel.java"));
		assertExists("appendage render layer", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/mob/MorphlingPolypAppendageLayer.java"));
		assertExists("appendage texture", RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/entity/morphling_polyp/model_morphling_polyp_layers.png"));
		assertExists("polyp spawn biome modifier", RESOURCE_ROOT.resolve(
				"data/hemomancy/neoforge/biome_modifier/add_morphling_polyp.json"));
		assertExists("polyp spawn biome tag", RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/morphling_polyp_spawnlist.json"));

		String entityInit = readSource("com/vincenthuto/hemomancy/common/init/EntityInit.java");
		assertContains("spawn placement registered", entityInit, "MorphlingPolypEntity::canSpawnHere");
		assertContains("attributes still registered", entityInit, "MorphlingPolypEntity.setAttributes().build()");

		String entity = readSource("com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypEntity.java");
		assertContains("layer data accessor", entity, "DATA_LAYER_MASK");
		assertContains("layer nbt key", entity, "MorphlingLayers");
		assertContains("finalize spawn assigns natural layers", entity, "assignNaturalLayers");
		assertContains("jumpy movement goal", entity, "MorphlingPolypHopGoal");
		assertContains("hint drop hook", entity, "dropLayerHint");

		String renderer = readSource("com/vincenthuto/hemomancy/client/render/entity/summon/MorphlingPolypRenderer.java");
		assertContains("renderer adds appendage layer", renderer, "new MorphlingPolypAppendageLayer");

		String layerModel = readSource("com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingPolypLayerModel.java");
		assertContains("layer model has fungal appendage", layerModel, "\"fungal\"");
		assertContains("layer model has cuttlefish appendage", layerModel, "\"cuttlefish\"");
		assertContains("layer model has mole appendage", layerModel, "\"mole\"");

		String spawnModifier = readResource("data/hemomancy/neoforge/biome_modifier/add_morphling_polyp.json");
		assertContains("spawn modifier uses polyp", spawnModifier, "\"type\": \"hemomancy:morphling_polyp\"");
		assertContains("spawn modifier is rare", spawnModifier, "\"weight\": 3");

		String spawnTag = readResource("data/hemomancy/tags/worldgen/biome/morphling_polyp_spawnlist.json");
		assertContains("spawn tag includes plains", spawnTag, "\"minecraft:plains\"");
		assertContains("spawn tag includes desert", spawnTag, "\"minecraft:desert\"");
		assertContains("spawn tag includes ocean", spawnTag, "\"minecraft:ocean\"");
		assertContains("spawn tag includes lush caves", spawnTag, "\"minecraft:lush_caves\"");

		String lootTable = readResource("data/hemomancy/loot_table/entities/morphling_polyp.json");
		assertContains("loot table drops polyp item", lootTable, "\"name\": \"hemomancy:morphling_polyp\"");
		assertNotContains("loot table no longer uses spore sac as main drop", lootTable, "hemomancy:spore_sac");

		String language = readResource("assets/hemomancy/lang/en_us.json");
		assertContains("entity translation remains", language, "\"entity.hemomancy.morphling_polyp\": \"Morphling Polyp\"");

		String reference = read(DOC_ROOT.resolve("HEMOMANCY_REFERENCE.md"));
		assertContains("reference documents layered wild polyps", reference, "layered wild polyps");
		assertContains("reference documents first polyp source", reference, "first Morphling Polyp");
	}

	private static String readSource(String relativePath) throws IOException {
		return read(SOURCE_ROOT.resolve(relativePath));
	}

	private static String readResource(String relativePath) throws IOException {
		return read(RESOURCE_ROOT.resolve(relativePath));
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

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": contained " + unexpected);
		}
	}
}
