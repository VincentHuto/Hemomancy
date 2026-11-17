package com.vincenthuto.hemomancy.common.data.book;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinition;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DS-9 (2026-11-16): every Working craft and both Armature rites have a Liber page, each craft page opens at its
 * craft's own Degree, and the four bosses and the Brined Court (NQ-16-3 A) have theirs.
 */
final class LiberCraftAndBossPagesDataTest {
	/** Crafts that had a page of their own before DS-9, which no craft rite opens. */
	private static final Map<String, ResourceLocation> OWN_HOMES = Map.of(
			"resonant_forge", LiberEntryDefinitions.FORGE_RECORDING,
			"enzymatic_scriptorium", LiberEntryDefinitions.SCRIPTORIUM,
			"somatic_loom", LiberEntryDefinitions.MEMORY_WEAVING,
			"morphling_cradle", LiberEntryDefinitions.MORPHLING_CRADLE_AND_NECTAR);

	@Test
	void everyWorkingCraftHasAPage() throws IOException {
		List<String> missing = new ArrayList<>();
		int crafts = 0;
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("recipe/cardinal_rite/working"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				crafts++;
				String craft = file.getFileName().toString().replace(".json", "");
				ResourceLocation home = OWN_HOMES.get(craft);
				boolean paged = home != null ? LiberEntryDefinitions.get(home).isPresent()
						: !LiberEntryDefinitions.forRite("cardinal_rite/working/" + craft).isEmpty();
				if (!paged) missing.add(craft);
			}
		}
		assertEquals(18, crafts, "the Working craft roster changed; give a new craft its page");
		assertEquals(List.of(), missing, "Working crafts with no Liber page");
	}

	@Test
	void eachCraftPageOpensAtItsCraftsDegree() {
		List<String> problems = new ArrayList<>();
		for (LiberEntryDefinitions.CraftPage craft : LiberEntryDefinitions.CRAFT_PAGES) {
			String file = "data/hemomancy/books/" + craft.page().getPath() + ".json";
			if (!ResourceReader.exists(file)) problems.add(craft.page() + " has no page file");
			int degree = craft.rite() != null
					? ResourceReader.data("recipe/" + craft.rite() + ".json").get("required_degree").getAsInt()
					: Math.max(1, ResourceReader.data("recipe/blood_structure/" + craft.item() + ".json").get("required_degree").getAsInt());
			if (degree != craft.degree()) problems.add(craft.item() + " opens at Degree " + craft.degree() + ", its craft at " + degree);
			if (craft.rite() != null && !ResourceReader.data("recipe/" + craft.rite() + ".json").getAsJsonObject("result")
					.get("id").getAsString().equals(Hemomancy.MOD_ID + ":" + craft.item())) {
				problems.add(craft.item() + " is not what " + craft.rite() + " makes");
			}
		}
		assertEquals(17, LiberEntryDefinitions.CRAFT_PAGES.size());
		assertEquals(List.of(), problems);
	}

	@Test
	void bothArmatureRitesOpenTheArmatureChapter() {
		for (String rite : List.of("armature_consecration", "monolithic_armature")) {
			var pages = LiberEntryDefinitions.forRite("cardinal_rite/" + rite);
			assertTrue(!pages.isEmpty() && pages.stream().allMatch(page -> page.getPath().startsWith("libersanguinium/armature/")),
					rite + " opens " + pages);
		}
		assertTrue(LiberEntryDefinitions.get(LiberEntryDefinitions.ARMATURE_RACK).isPresent());
		assertTrue(ResourceReader.exists("data/hemomancy/books/libersanguinium/armature/chapter.json"));
	}

	@Test
	void eachBossPageOpensWhenItsFightBegins() {
		for (ResourceLocation page : List.of(LiberEntryDefinitions.VESPER, LiberEntryDefinitions.MYCOPHANT,
				LiberEntryDefinitions.NAEGLEROPHAEON, LiberEntryDefinitions.PHLEGETHOSOMA, LiberEntryDefinitions.BLOOM_WARDEN,
				LiberEntryDefinitions.KINDLED_CHAMPION)) {
			LiberEntryDefinition definition = LiberEntryDefinitions.get(page).orElseThrow(() -> new AssertionError(page + " is not registered"));
			assertTrue(definition.sources().contains(HemomancyDiscoverySource.ENCOUNTER), page + " has no encounter opener");
			assertTrue(ResourceReader.exists("data/hemomancy/books/" + page.getPath() + ".json"), page + " has no file");
		}
	}

	@Test
	void theCourtChapterHoldsItsHousePageAndBothBosses() {
		assertTrue(ResourceReader.exists("data/hemomancy/books/libersanguinium/brined_court/chapter.json"));
		for (ResourceLocation page : List.of(LiberEntryDefinitions.COURT_HOUSES, LiberEntryDefinitions.BLOOM_WARDEN,
				LiberEntryDefinitions.KINDLED_CHAMPION)) {
			assertTrue(page.getPath().startsWith("libersanguinium/brined_court/pages/"), page.toString());
			assertTrue(LiberEntryDefinitions.get(page).isPresent(), page + " is not registered");
		}
	}
}
