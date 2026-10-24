package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.support.EnglishText;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RitesTabUnstainedListTest {
	@Test
	void gatewayRitesLeadTheirStageThenNamesReadAlphabetically() {
		List<String[]> purified = new ArrayList<>(List.of(
				new String[] {"cardinal_rite/silthmeres_remembrance", "Rite of Silthmere's Remembrance"},
				new String[] {"cardinal_rite/clarity_ascension", "Rite of Clarity Ascension"},
				new String[] {"cardinal_rite/closed_vein", "Rite of the Closed Vein"}));
		List<String[]> vigilant = new ArrayList<>(List.of(
				new String[] {"cardinal_rite/pale_vigil", "Rite of the Pale Vigil"},
				new String[] {"cardinal_rite/moon_washed_copper", "Rite of Moon-Washed Copper"},
				new String[] {"cardinal_rite/glass_lungs", "rite of glass lungs"}));

		purified.sort(RitesTabState.<String[]>sidebarOrder(row -> row[0], row -> row[1]));
		vigilant.sort(RitesTabState.<String[]>sidebarOrder(row -> row[0], row -> row[1]));

		assertEquals(List.of("cardinal_rite/closed_vein", "cardinal_rite/clarity_ascension",
				"cardinal_rite/silthmeres_remembrance"), purified.stream().map(row -> row[0]).toList(),
				"path order: the Closed Vein opens Purified, Clarity Ascension closes it");
		assertEquals(List.of("cardinal_rite/glass_lungs", "cardinal_rite/moon_washed_copper",
				"cardinal_rite/pale_vigil"), vigilant.stream().map(row -> row[0]).toList(),
				"names compare case-insensitively, never by blood cost or registry order");
	}

	@Test
	void unstainedTiersAreHeadedByStageNames() {
		RitesTabState rites = new RitesTabState();
		CraftingTabState crafting = new CraftingTabState();
		assertEquals("Degree 5", EnglishText.render(rites.tierLabel(5)));
		assertEquals("No Degree", EnglishText.render(crafting.tierLabel(0)));

		rites.unstainedStages = true;
		crafting.unstainedStages = true;

		assertEquals("Purified", EnglishText.render(rites.tierLabel(5)));
		assertEquals("Unbegun", EnglishText.render(crafting.tierLabel(0)));
		assertEquals("Rites by Stage", EnglishText.render(rites.sidebarTitle()));
		assertEquals("Crafting by Stage", EnglishText.render(crafting.sidebarTitle()));
	}

	@Test
	void emptyCraftingStagesCanBeHidden() {
		CraftingTabState crafting = new CraftingTabState();
		crafting.rebuildTierMap();
		crafting.hideEmptyTiers = true;

		assertTrue(crafting.isTierHidden(CraftingTabState.TIER_NAMES[3]));
		assertEquals(0, crafting.sidebarContentH());
	}

	@Test
	void floorsAndSigilsRingAtTheirFirstUse() {
		ResourceLocation communionMinor = id("communion_minor");
		ResourceLocation threshold = id("threshold_minor");
		ResourceLocation workingGrand = id("working_grand");
		ResourceLocation dominionGrand = id("dominion_grand");
		ResourceLocation lonely = id("lonely_tier");
		Map<ResourceLocation, String> tiers = new LinkedHashMap<>();
		tiers.put(communionMinor, "minor");
		tiers.put(threshold, "minor");
		tiers.put(workingGrand, "grand");
		tiers.put(dominionGrand, "grand");
		tiers.put(lonely, "unused");

		Map<ResourceLocation, Integer> degrees = RitesAndCraftsTabController.firstUseDegrees(tiers,
				Map.of(communionMinor, 2, dominionGrand, 5),
				Map.of(communionMinor, 0, threshold, 0, workingGrand, 3, dominionGrand, 3, lonely, 4));

		assertEquals(2, (int) degrees.get(communionMinor));
		assertEquals(2, (int) degrees.get(threshold), "an unused floor follows its tier's earliest-used sibling");
		assertEquals(5, (int) degrees.get(workingGrand));
		assertEquals(5, (int) degrees.get(dominionGrand));
		assertEquals(4, (int) degrees.get(lonely), "a tier nobody uses keeps its own fallback");
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
