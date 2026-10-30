package com.vincenthuto.hemomancy.common.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The heart-node, the Mycelial Synapse and the fruiting-body cache are generator-authored set pieces
 * (tools/model_export): every model they use is a generated one with a Blockbench project, at block texel density
 * on world textures, in Harbinger materials.
 */
class HeartNodeSetPieceResourceTest {
	private static final Set<String> GENERATORS = Set.of("heart_node", "mycelial_synapse", "fruiting_body_cache");
	private static final Set<String> FORBIDDEN = Set.of("copper", "brass", "bronze", "gold", "silver");

	@Test
	void eachPieceHasAGeneratorAndABlockbenchProject() {
		for (String name : GENERATORS) {
			assertTrue(Files.isRegularFile(RepoPaths.TOOLS.resolve("model_export/" + name + ".py")), name + " generator");
			assertTrue(ResourceReader.exists("assets/hemomancy/models/block/bbmodel/" + name + ".bbmodel"),
					name + " Blockbench project");
		}
		assertFalse(ResourceReader.exists("assets/hemomancy/models/block/heart_node.json"),
				"the hand-written heart-node cube is gone; the blockstate picks a stage model");
		assertFalse(ResourceReader.exists("assets/hemomancy/textures/block/heart_node.png"),
				"and so is its single texture");
	}

	@Test
	void theHeartNodeShowsFourStagesAndItsHeartBeatsApart() {
		JsonObject variants = ResourceReader.asset("blockstates/heart_node.json").getAsJsonObject("variants");
		assertEquals(4, variants.size());
		for (int stage = 0; stage < 4; stage++) {
			String model = variants.getAsJsonObject("stage=" + stage).get("model").getAsString();
			assertEquals("hemomancy:block/heart_node_stage_" + stage, model);
			assertGenerated("heart_node_stage_" + stage);
			assertGenerated("heart_node_withered_stage_" + stage);
		}
		assertNamed("heart_node_stage_0", "seed_knot");
		assertNamed("heart_node_stage_1", "root_tip");
		assertNamed("heart_node_stage_2", "rib_tip");
		assertNamed("heart_node_stage_3", "cage_crown");
		assertNamed("heart_node_stage_3", "fruit_cap");
		assertGenerated("heart_node_heart");
		assertGenerated("heart_node_heart_withered");
		for (int stage = 0; stage < 4; stage++) {
			assertFalse(text("heart_node_stage_" + stage).contains("\"name\": \"ventricle\""),
					"the renderer draws the heart, so no stage body carries one");
		}
		assertTrue(ResourceReader.asset("models/item/heart_node.json").toString().contains("ventricle"),
				"the item shows the grown body with its heart");
	}

	@Test
	void theSynapseGripsItsFaceInAllSixFacingsAliveOrWithered() {
		JsonObject variants = ResourceReader.asset("blockstates/mycelial_synapse.json").getAsJsonObject("variants");
		assertEquals(12, variants.size());
		Map<String, int[]> turns = Map.of("up", new int[] { 0, 0 }, "down", new int[] { 180, 0 },
				"north", new int[] { 90, 0 }, "south", new int[] { 90, 180 }, "east", new int[] { 90, 90 },
				"west", new int[] { 90, 270 });
		for (Map.Entry<String, int[]> turn : turns.entrySet()) {
			for (boolean withered : new boolean[] { false, true }) {
				JsonObject variant = variants.getAsJsonObject("facing=" + turn.getKey() + ",withered=" + withered);
				assertEquals("hemomancy:block/mycelial_synapse" + (withered ? "_withered" : ""),
						variant.get("model").getAsString());
				assertEquals(turn.getValue()[0], variant.has("x") ? variant.get("x").getAsInt() : 0, turn.getKey());
				assertEquals(turn.getValue()[1], variant.has("y") ? variant.get("y").getAsInt() : 0, turn.getKey());
			}
		}
		assertGenerated("mycelial_synapse");
		assertGenerated("mycelial_synapse_withered");
		assertGenerated("mycelial_synapse_bulb");
		assertNamed("mycelial_synapse", "grip");
		assertNamed("mycelial_synapse", "flute");
		assertNamed("mycelial_synapse", "dendrite_tip");
		assertTrue(text("mycelial_synapse_withered").contains("mycelial_synapse_withered_crystal"),
				"the withered synapse wears its dulled crystal");
	}

	@Test
	void theCacheSplitsAlongASeamIntoTwoLids() {
		assertGenerated("fruiting_body_cache");
		assertGenerated("fruiting_body_cache_lid_west");
		assertGenerated("fruiting_body_cache_lid_east");
		assertNamed("fruiting_body_cache", "bracket");
		assertNamed("fruiting_body_cache_lid_west", "seam");
		assertNamed("fruiting_body_cache_lid_west", "held_hilt");
		assertNamed("fruiting_body_cache_lid_east", "held_helm");
		assertFalse(text("fruiting_body_cache").contains("\"name\": \"cap\""),
				"the cap halves are drawn by the renderer, so the body has none");
	}

	private static String text(String model) {
		return ResourceReader.text("assets/hemomancy/models/block/" + model + ".json");
	}

	private static void assertNamed(String model, String part) {
		assertTrue(text(model).contains("\"name\": \"" + part + "\""), model + " has a " + part);
	}

	/** A generated model: cutout, AO off, UVs within one texture tile, and no copper-family texture. */
	private static void assertGenerated(String model) {
		JsonObject json = ResourceReader.asset("models/block/" + model + ".json");
		assertEquals("minecraft:cutout", json.get("render_type").getAsString(), model);
		assertFalse(json.get("ambientocclusion").getAsBoolean(), model);
		for (Map.Entry<String, JsonElement> texture : json.getAsJsonObject("textures").entrySet()) {
			String location = texture.getValue().getAsString();
			for (String metal : FORBIDDEN) assertFalse(location.contains(metal), model + " uses " + location);
		}
		for (JsonElement element : json.getAsJsonArray("elements")) {
			for (Map.Entry<String, JsonElement> face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
				for (JsonElement uv : face.getValue().getAsJsonObject().getAsJsonArray("uv")) {
					double value = uv.getAsDouble();
					assertTrue(value >= 0 && value <= 16, model + " keeps 16 px per block UVs");
				}
			}
		}
	}
}
