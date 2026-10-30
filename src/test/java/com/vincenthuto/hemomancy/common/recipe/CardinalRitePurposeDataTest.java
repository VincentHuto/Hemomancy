package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePurposeDataTest {
	private static final Path RITE_ROOT = Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite");

	private static final Map<String, String> PURPOSES = Map.ofEntries(
			Map.entry("initiate_rite", "self"), Map.entry("sanguine_brotherhood", "self"),
			Map.entry("illuminatus_rite", "self"), Map.entry("sanctified_rite", "self"),
			Map.entry("archon_rite", "self"), Map.entry("apotheos_rite", "self"),
			Map.entry("vascular_mending", "self"), Map.entry("hematic_fortification", "self"),
			Map.entry("crimson_beacon", "self"), Map.entry("eternal_covenant", "self"),
			Map.entry("sanguine_attunement", "self"), Map.entry("chamber_of_will", "self"),
			Map.entry("rooted_vein", "world"), Map.entry("hungering_earth", "world"),
			Map.entry("sanguine_fervor", "world"), Map.entry("sanguine_eclipse", "world"),
			Map.entry("pallid_shadow", "world"), Map.entry("sanguine_dominion", "world"),
			Map.entry("founding_fane", "world"), Map.entry("hematic_succession", "world"),
			Map.entry("mnemonic_restoration", "world"), Map.entry("bloom_of_qliphoth", "world"),
			Map.entry("puppeteer_trial_veinwing_vulture", "world"), Map.entry("puppeteer_trial_marrow_spitter", "world"),
			Map.entry("puppeteer_trial_gorebound_hulk", "world"), Map.entry("puppeteer_trial_scarlet_mummer", "world"),
			Map.entry("puppeteer_trial_sanguine_hound", "world"), Map.entry("puppeteer_trial_cinder_bellows", "world"),
			Map.entry("puppeteer_trial_mnemonist_puppet", "world"),
			Map.entry("pallid_vessel_rite", "making"), Map.entry("crimson_vessel_rite", "making"),
			Map.entry("ashen_vessel_rite", "making"), Map.entry("horn_of_culmination_rite", "making"),
			Map.entry("exsanguination", "making"), Map.entry("first_condensation", "making"),
			Map.entry("sanguine_athanor", "making"), Map.entry("steady_separation", "making"),
			Map.entry("second_fraction", "making"), Map.entry("true_groove", "making"),
			Map.entry("enduring_pattern", "making"), Map.entry("armature_consecration", "making"),
			Map.entry("monolithic_armature", "making"), Map.entry("eightfold_script", "making"),
			Map.entry("palimpsest", "making"), Map.entry("ganglionic_weave", "making"),
			Map.entry("cortical_weave", "making"), Map.entry("first_clotting", "making"),
			Map.entry("druse_crystallization", "making"),
			Map.entry("bloodline_founding", "bond"), Map.entry("bloodline_recall", "bond"),
			Map.entry("hematic_unbinding", "bond"), Map.entry("scarlet_summons", "bond"),
			Map.entry("covenant_vigil", "bond"), Map.entry("ancestral_communion", "bond"),
			Map.entry("possession_rite", "bond"), Map.entry("visitation_rite", "bond"));
	private static final Set<String> SPECIAL = Set.of("apotheos_rite", "bloom_of_qliphoth", "ancestral_communion",
			"possession_rite");

	@Test
	void everyHarbingerRiteDeclaresItsPinnedPurposeOnAMatchingFloor() throws IOException {
		Set<String> seen = new HashSet<>();
		try (var paths = Files.walk(RITE_ROOT)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String name = path.getFileName().toString().replace(".json", "");
				boolean working = path.getParent().getFileName().toString().equals("working");
				assertTrue(json.has("purpose"), path + " must declare a purpose");
				String purpose = json.get("purpose").getAsString();
				if (working) {
					assertEquals("making", purpose, path.toString());
				} else {
					assertEquals(PURPOSES.get(name), purpose, path + " purpose");
					seen.add(name);
				}
				boolean special = json.has("special") && json.get("special").getAsBoolean();
				assertEquals(SPECIAL.contains(name), special, path + " special");
				String floor = json.get("floor").getAsString();
				assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.byId(purpose), special,
						floor.substring(floor.indexOf(':') + 1)), path.toString());
			}
		}
		assertEquals(PURPOSES.keySet(), seen);
	}
}
