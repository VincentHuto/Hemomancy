package com.vincenthuto.hemomancy.common.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

/** The decisions taken on the 2026-11-02 audit's section E that show up as data and text. */
class AuditDecisionsResourceTest {
	private static String lang() {
		return ResourceReader.text("assets/hemomancy/lang/en_us.json");
	}

	@Test
	void annettasChurchGeneratesOutsideTheOptionalGardens() {
		String tag = ResourceReader.text("data/hemomancy/tags/worldgen/biome/has_structure/broken_church.json");
		assertTrue(tag.contains("minecraft:swamp") && tag.contains("minecraft:dark_forest"));
	}

	@Test
	void hemorathGivesTheRibReliquary() {
		assertTrue(ResourceReader.text("data/hemomancy/loot_table/entities/hemorath.json").contains("hemomancy:hemorath_rib"));
	}

	@Test
	void theGuardianGivesTheGlaiveFromPurityFifty() {
		String card = ResourceReader.text("data/hemomancy/npc_trade/guardian/silthmere_glaive.json");
		assertTrue(card.contains("hemomancy:silthmere_glaive") && card.contains("\"stage_at_least\": 3"), card);
	}

	@Test
	void bloodSideRecipesTakeNoCopper() {
		for (String recipe : new String[] { "hematic_microscope", "humoral_barometer", "gourdvine_tap" }) {
			assertFalse(ResourceReader.text("data/hemomancy/recipe/" + recipe + ".json").contains("copper"), recipe);
		}
	}

	@Test
	void cutContentLeavesNoData() {
		assertFalse(ResourceReader.exists("data/hemomancy/worldgen/structure_set/crimson_lodge_annex.json"));
		assertFalse(ResourceReader.exists("data/hemomancy/recipe/cardinal_rite/working/semi_sentient_construct.json"));
		assertFalse(lang().contains("marked_by_canon"));
	}

	@Test
	void onlyArchonsHearTheEighthDegreeNamed() {
		String lang = lang();
		for (String key : new String[] { "message.hemomancy.mausoleum_ward.refused", "message.hemomancy.chapel.refused",
				"hemomancy.rite.archon_rite.desc", "hemomancy.vicar.initiate.saints_lore" }) {
			int at = lang.indexOf('"' + key + '"');
			assertTrue(at >= 0, key);
			String line = lang.substring(at, lang.indexOf('\n', at)).toLowerCase();
			assertFalse(line.contains("eighth"), line);
		}
	}

	@Test
	void marrowHasOneName() {
		String lang = lang();
		for (String key : new String[] { "hemomancy.rite.puppeteer_trial_marrow_spitter.name", "item.hemomancy.marrow_spitter_carriage",
				"item.hemomancy.spawn_egg_marrow_spitter" }) {
			int at = lang.indexOf('"' + key + '"');
			assertFalse(lang.substring(at, lang.indexOf('\n', at)).contains("Spitter\","), key);
		}
		assertFalse(lang.contains("Marrow Spitter Carriage") || lang.contains("Marrow Spitter Ordeal"));
	}

	@Test
	void dynastyKeepsHoldLoot() throws IOException {
		assertTrue(ResourceReader.exists("data/hemomancy/loot_table/chests/dynasty_castle.json"));
		for (String piece : new String[] { "keep_grand", "keep_squat", "keep_ruined" }) {
			try (InputStream in = new GZIPInputStream(Files.newInputStream(RepoPaths.DATA.resolve("structure/dynasty_castle/" + piece + ".nbt")))) {
				assertTrue(new String(in.readAllBytes(), StandardCharsets.ISO_8859_1).contains("hemomancy:chests/dynasty_castle"), piece);
			}
		}
	}
}
