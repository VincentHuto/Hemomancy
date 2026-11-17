package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodDrunkPuppeteerPolishTest {
	@Test
	void puppeteerSummonsFourSpacedDollsThatDropNothing() {
		assertEquals(4, BloodDrunkPuppeteerTuning.DOLL_COUNT, "puppeteer summons four dolls");
		assertTrue(BloodDrunkPuppeteerTuning.DOLL_OWNER_SEARCH_RANGE >= 48.0,
				"doll owner search supports combat spacing");
		assertFalse(BloodDrunkPuppeteerTuning.SUMMONED_DOLLS_DROP_LOOT, "summoned dolls are not a loot farm");

		Set<String> offsets = new HashSet<>();
		for (int i = 0; i < BloodDrunkPuppeteerTuning.DOLL_COUNT; i++) {
			double[] offset = BloodDrunkPuppeteerTuning.dollSpawnOffset(i);
			offsets.add(offset[0] + "," + offset[2]);
		}
		assertEquals(BloodDrunkPuppeteerTuning.DOLL_COUNT, offsets.size(), "dolls spawn with distinct offsets");
	}

	@Test
	void puppeteerHasAFindableWildSpawnAndItsLoot() {
		String modifier = ResourceReader.text("data/hemomancy/neoforge/biome_modifier/add_blood_drunk_puppeteer.json");
		SourceAssertions.assertContains("biome modifier", modifier, "hemomancy:blood_drunk_puppeteer");
		SourceAssertions.assertContains("biome modifier gives puppeteers a findable wild spawn weight", modifier,
				"\"weight\": 24");
		String biomes = ResourceReader.text("data/hemomancy/tags/worldgen/biome/blood_drunk_puppeteer_spawnlist.json");
		SourceAssertions.assertContains("spawn biome tag", biomes, "minecraft:dark_forest");
		SourceAssertions.assertContains("spawn biome tag includes mangroves for wild testing", biomes,
				"minecraft:mangrove_swamp");
		SourceAssertions.assertContains("puppeteer loot",
				ResourceReader.text("data/hemomancy/loot_table/entities/blood_drunk_puppeteer.json"),
				"hemomancy:enthralling_filament");
	}

	@Test
	void puppeteersSpawnAtNightUnderTheMonsterLightRule() {
		String puppeteerEntity = SourceAssertions.readMainJava("common/entity/mob/monster/BloodDrunkPuppeteerEntity.java");
		SourceAssertions.assertContains("puppeteers spawn at night under the monster light rule (CR-3)",
				puppeteerEntity, "return checkMonsterSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);");
		SourceAssertions.assertNotContains("puppeteers no longer skip the light check (CR-3)",
				puppeteerEntity, "checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom)");
	}
}
