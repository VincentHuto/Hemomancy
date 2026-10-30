package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.WorkingCraftBed;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Pins the Working-craft roster and the floorless Blood Structure tier. */
final class WorkingCraftRosterDataTest {
	private static final Path WORKING = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite/working");
	private static final Path STRUCTURES = Path.of("src/main/resources/data/hemomancy/recipe/blood_structure");

	private record Craft(int degree, String tier, String medium, int feed) {
	}

	private static final Map<String, Craft> ROSTER = Map.ofEntries(
			Map.entry("resonant_forge", new Craft(3, "minor", "hemomancy:ferric_binder", 500)),
			Map.entry("scarlet_vanity", new Craft(3, "minor", "hemomancy:sanguine_formation", 300)),
			Map.entry("somatic_loom", new Craft(3, "lesser", "hemomancy:sanguine_formation", 150)),
			Map.entry("mycelial_lantern", new Craft(3, "lesser", "hemomancy:ferric_binder", 2500)),
			Map.entry("fungal_implantation_pylon", new Craft(4, "lesser", "hemomancy:hyphal_substrate", 1200)),
			Map.entry("sporitic_thurible", new Craft(4, "lesser", "hemomancy:spore_sac", 1000)),
			Map.entry("enzymatic_scriptorium", new Craft(3, "greater", "hemomancy:ferric_binder", 500)),
			Map.entry("mnemonic_reliquary", new Craft(3, "greater", "hemomancy:sanguine_formation", 75)),
			Map.entry("warp_chair", new Craft(3, "greater", "hemomancy:sanguine_formation", 250)),
			Map.entry("semi_sentient_construct", new Craft(3, "greater", "hemomancy:befouling_ash_trail", 250)),
			Map.entry("mason_effigy", new Craft(4, "greater", "hemomancy:runic_motif_paper", 150)),
			Map.entry("mind_spike", new Craft(4, "greater", "hemomancy:sanguine_formation", 500)),
			Map.entry("runic_chisel_station", new Craft(4, "greater", "hemomancy:sanguine_formation", 100)),
			Map.entry("visceral_mirror", new Craft(4, "greater", "hemomancy:sanguine_formation", 200)),
			Map.entry("dendritic_distributor", new Craft(5, "greater", "hemomancy:sanguine_formation", 500)),
			Map.entry("morphling_incubator", new Craft(5, "greater", "hemomancy:morphling_polyp", 200)),
			Map.entry("vacant_effigy", new Craft(5, "greater", "hemomancy:sanguine_formation", 250)),
			Map.entry("sanguine_monolith", new Craft(6, "greater", "hemomancy:sanguine_formation", 175)),
			Map.entry("covenant_throne", new Craft(6, "greater", "hemomancy:sanguine_formation", 8000)),
			Map.entry("vascular_effigy", new Craft(6, "grand", "hemomancy:sanguine_formation", 6500)));

	// mycelial_synapse: a heart-node's envelope anchor, floorless like the stake it mirrors (the Gardens have no rite floor).
	private static final Set<String> FLOORLESS = Set.of("hematic_iron_block", "liber_sanguinum", "living_staff",
			"iron_brazier", "dried_gourd", "hematic_stake", "mycelial_synapse", "vial_centrifuge", "ghastly_alembic",
			"pallid_retort", "unstained_pillar");

	@Test
	void everyRosterCraftIsAWorkingRiteWithItsPinnedShape() throws IOException {
		assertEquals(new TreeSet<>(ROSTER.keySet()), names(WORKING));
		for (Map.Entry<String, Craft> entry : ROSTER.entrySet()) {
			JsonObject json = read(WORKING.resolve(entry.getKey() + ".json"));
			Craft craft = entry.getValue();
			String where = entry.getKey();
			assertEquals("hemomancy:cardinal_rite_recipe", json.get("type").getAsString(), where);
			assertEquals("hemomancy:cardinal_rite/working/" + where, json.get("id").getAsString(), where);
			assertEquals("making", json.get("purpose").getAsString(), where);
			assertEquals(0, json.get("bloodCost").getAsInt(), where);
			assertEquals(craft.degree(), json.get("required_degree").getAsInt(), where);
			assertEquals(craft.tier(), json.get("riteType").getAsString(), where);
			assertEquals("hemomancy:working_" + craft.tier(), json.get("floor").getAsString(), where);
			assertEquals(craft.medium(), json.getAsJsonObject("medium").get("item").getAsString(), where);
			JsonObject subject = json.getAsJsonObject("subject");
			assertEquals(craft.feed(), subject.get("feed").getAsInt(), where);
			assertTrue(json.getAsJsonArray("brazier_signature").size() >= 1, where + " needs an offering");
			assertTrue(json.has("result"), where);
			assertEquals(0, json.getAsJsonObject("ceremony").getAsJsonArray("anchors").size(), where);
			assertEquals("forging", json.getAsJsonObject("ceremony").get("culmination").getAsString(), where);
			assertEquals("safe_retry", json.getAsJsonObject("ceremony").get("failure").getAsString(), where);

			WorkingCraftBed.Footprint footprint = WorkingCraftBed.Footprint.of(pattern(subject.getAsJsonArray("pattern")));
			assertTrue(WorkingCraftBed.forTier(CardinalRiteType.byName(craft.tier())).fits(footprint),
					where + " subject " + footprint + " must fit its bed");
			for (Map.Entry<String, JsonElement> key : subject.getAsJsonObject("key").entrySet()) {
				assertFalse(key.getValue().toString().contains("copper"), where + " subject must not carry copper");
			}
		}
	}

	@Test
	void onlyTheFloorlessTierRemainsAsBloodStructures() throws IOException {
		assertEquals(new TreeSet<>(FLOORLESS), names(STRUCTURES));
		for (String name : FLOORLESS) {
			assertFalse(read(STRUCTURES.resolve(name + ".json")).has("offerings"),
					name + " is floorless and takes no brazier offerings");
		}
	}

	private static TreeSet<String> names(Path dir) throws IOException {
		try (var paths = Files.list(dir)) {
			return paths.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".json"))
					.map(n -> n.substring(0, n.length() - 5)).collect(Collectors.toCollection(TreeSet::new));
		}
	}

	private static String[][] pattern(JsonArray rows) {
		String[][] pattern = new String[rows.size()][];
		for (int z = 0; z < rows.size(); z++) {
			JsonArray layer = rows.get(z).getAsJsonArray();
			pattern[z] = new String[layer.size()];
			for (int y = 0; y < layer.size(); y++) pattern[z][y] = layer.get(y).getAsString();
		}
		return pattern;
	}

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
