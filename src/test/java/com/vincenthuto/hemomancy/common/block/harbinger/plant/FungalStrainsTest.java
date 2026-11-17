package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop round two, P7: the fungal strain table. Eight rows, one per spore and one per school, three substrate
 * groups; each strain has its block's resources, needs blood to grow, and its group's tag lists the substrates the
 * Liber names (Ravenous Heartwood, Salt-Stained Timber, podzol and mycelium).
 */
class FungalStrainsTest {
	@Test
	void eachSporeAndEachSchoolHasOneStrain() {
		Set<String> spores = new HashSet<>();
		Set<String> blocks = new HashSet<>();
		EnumSet<EnumBloodTemper> tempers = EnumSet.noneOf(EnumBloodTemper.class);
		for (FungalStrains strain : FungalStrains.values()) {
			assertTrue(spores.add(strain.sporeId()), "two strains share " + strain.sporeId());
			assertTrue(blocks.add(strain.blockId()), "two strains share " + strain.blockId());
			assertTrue(tempers.add(strain.temper()), "two strains share " + strain.temper());
			assertSame(strain, FungalStrains.bySpore(strain.sporeId()));
			assertEquals(strain.strainName(), strain.getSerializedName());
		}
		assertEquals(8, FungalStrains.values().length);
		assertEquals(EnumSet.allOf(EnumBloodTemper.class), tempers, "every school has a strain");
		assertNull(FungalStrains.bySpore("gourd_seeds"));
	}

	@Test
	void theSubstrateGroupsAreTheOnesTheLiberNames() {
		Map<FungalStrains.Substrate, List<FungalStrains>> byGroup = new java.util.EnumMap<>(FungalStrains.Substrate.class);
		for (FungalStrains strain : FungalStrains.values()) byGroup.computeIfAbsent(strain.substrate(), group -> new ArrayList<>()).add(strain);
		assertEquals(List.of(FungalStrains.VIVACIOUS, FungalStrains.FERVENT, FungalStrains.RUINOUS), byGroup.get(FungalStrains.Substrate.FLESH));
		assertEquals(List.of(FungalStrains.NEUROTIC, FungalStrains.FRIGID, FungalStrains.FERRIC), byGroup.get(FungalStrains.Substrate.BRINED));
		assertEquals(List.of(FungalStrains.INCANDESCENT, FungalStrains.UMBRAL), byGroup.get(FungalStrains.Substrate.COMMON));
		assertEquals(List.of("hemomancy:ravenous_heartwood"), values("tags/block/strain_substrates/flesh.json"));
		assertEquals(List.of("hemomancy:salt_stained_timber"), values("tags/block/strain_substrates/brined.json"));
		List<String> common = values("tags/block/strain_substrates/common.json");
		assertTrue(common.contains("minecraft:podzol") && common.contains("minecraft:mycelium"), common.toString());
		for (FungalStrains.Substrate group : FungalStrains.Substrate.values()) {
			assertEquals("hemomancy:strain_substrates/" + group.id(), group.tag().location().toString());
			assertTrue(values("tags/block/strain_substrates.json").contains("#" + group.tag().location()), group + " is not in the umbrella tag");
			assertTrue(ResourceReader.asset("lang/en_us.json").has(group.tooltipKey()), "no tooltip line for " + group);
		}
	}

	@Test
	void everyStrainNeedsBloodAndHasItsResources() {
		List<String> needsBlood = values("tags/block/needs_blood_to_grow.json");
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		for (FungalStrains strain : FungalStrains.values()) {
			String block = strain.blockId();
			assertTrue(needsBlood.contains("hemomancy:" + block), block + " grows without blood");
			JsonObject variants = ResourceReader.asset("blockstates/" + block + ".json").getAsJsonObject("variants");
			for (int age = 0; age <= FungalStrainRules.MAX_AGE; age++) assertTrue(variants.has("age=" + age), block + " age " + age);
			assertTrue(lang.has("block.hemomancy." + block), "no name for " + block);
			String loot = ResourceReader.text("data/hemomancy/loot_table/blocks/" + block + ".json");
			assertTrue(loot.contains("hemomancy:" + strain.sporeId()), block + " does not return its spore");
		}
	}

	@Test
	void aStrainGrowsAStageAtATimeAndReturnsMoreRipe() {
		assertTrue(FungalStrainRules.ownRollGrows(0));
		assertFalse(FungalStrainRules.ownRollGrows(1));
		assertEquals(1, FungalStrainRules.advance(0, 1));
		assertEquals(FungalStrainRules.MAX_AGE, FungalStrainRules.advance(2, 5), "never past ripe");
		assertTrue(FungalStrainRules.ripe(FungalStrainRules.MAX_AGE));
		assertFalse(FungalStrainRules.ripe(2));
		assertEquals(1.0D, FungalStrainRules.expectedSpores(1), "an unripe strain gives back what was planted");
		assertTrue(FungalStrainRules.expectedSpores(FungalStrainRules.MAX_AGE) > 1.0D, "a ripe one gives more");
	}

	private static List<String> values(String dataPath) {
		List<String> values = new ArrayList<>();
		for (JsonElement element : ResourceReader.data(dataPath).getAsJsonArray("values")) values.add(element.getAsString());
		return values;
	}
}
