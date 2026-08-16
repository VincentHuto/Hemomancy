package com.vincenthuto.hemomancy.common.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ReleaseResourceIntegrityTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final Path BUILD = Path.of("build.gradle");

	private static final List<String> RETIRED_ITEM_IDS = List.of(
			"hemomancy:field_notes",
			"hemomancy:hematic_field_ink",
			"hemomancy:pale_field_ink",
			"hemomancy:roasted_gourd_seeds",
			"hemomancy:dicentra_sap",
			"hemomancy:blood_tendency_gauge",
			"hemomancy:scar_binder",
			"hemomancy:scar_binder_upgraded");

	private ReleaseResourceIntegrityTest() {
	}

	public static void main(String[] args) throws IOException {
		assertNoRetiredIdsInRuntimeData();
		assertModernCompressionRecipes();
		assertValidEffigyStackSizes();
		assertModernTagsAndBannerPatterns();
		assertAuthoredOrphansHaveSources();
		assertSpecialPlantLootCoverage();
		assertIronWallIsDormantAndSinglyRendered();
		assertBannerPatternsAreNotDeferredRegistrations();
		assertNoInvalidExampleRecipe();
		assertNoRetiredDiscoveryHooks();
		assertUnstainedGuideCoversNewSystems();
		assertAlphaCheckScansRuntimeResourceErrors();
	}

	private static void assertNoRetiredIdsInRuntimeData() throws IOException {
		for (Path file : jsonFiles(DATA)) {
			String text = Files.readString(file);
			for (String retiredId : RETIRED_ITEM_IDS) {
				if (text.contains("\"" + retiredId + "\"")) {
					throw new AssertionError(file + " references retired item " + retiredId);
				}
			}
		}
	}

	private static void assertModernCompressionRecipes() throws IOException {
		assertRecipeUses("mnemonic_ambergris_block.json",
				"\"id\": \"hemomancy:mnemonic_ambergris_block\"", "\"item\": \"hemomancy:mnemonic_ambergris_block\"");
		assertRecipeUses("chitinite_block.json",
				"\"item\": \"hemomancy:chitinous_husk\"", "\"item\": \"hemomancy:chitinite\"");
		assertRecipeUses("chitinite_block.json",
				"\"id\": \"hemomancy:chitinite_block\"", "\"item\": \"hemomancy:chitinite_block\"");
		assertRecipeUses("sclerite_block.json",
				"\"item\": \"hemomancy:chalybeate_sclerite\"", "\"item\": \"hemomancy:sclerite\"");
		assertRecipeUses("sclerite_block.json",
				"\"id\": \"hemomancy:sclerite_block\"", "\"item\": \"hemomancy:sclerite_block\"");
		if (Files.exists(DATA.resolve("recipe/organic_prism_block.json"))) {
			throw new AssertionError("redundant organic_prism_block recipe remains in the runtime datapack");
		}
	}

	private static void assertValidEffigyStackSizes() throws IOException {
		for (String file : List.of("zombie_husk_effigy.json", "desert_husk_effigy.json", "spider_husk_effigy.json")) {
			String text = Files.readString(DATA.resolve("recipe").resolve(file));
			if (!text.contains("\"count\": 1")) {
				throw new AssertionError(file + " must output one non-stackable effigy");
			}
		}
	}

	private static void assertModernTagsAndBannerPatterns() throws IOException {
		String voidVessels = Files.readString(DATA.resolve("tags/item/progression/void_vessels.json"));
		assertContains(voidVessels, "\"minecraft:ender_eye\"", "void vessel tag uses the current Ender Eye id");
		assertNotContains(voidVessels, "minecraft:eye_of_ender", "void vessel tag excludes the retired Ender Eye id");

		for (String file : List.of("hemolymphopoda_spawnlist.json", "stink_horn_spawnlist.json")) {
			String text = Files.readString(DATA.resolve("tags/worldgen/biome").resolve(file));
			assertNotContains(text, "#forge:", file + " excludes legacy Forge biome tags");
		}
		String hemojelly = Files.readString(DATA.resolve("tags/worldgen/biome/hemojelly_spawnlist.json"));
		assertNotContains(hemojelly, "minecraft:deep_warm_ocean", "hemojelly tag excludes nonexistent biome");

		for (String id : List.of("hemomancy_heart", "hemomancy_veins")) {
			Path definition = DATA.resolve("banner_pattern").resolve(id + ".json");
			if (!Files.isRegularFile(definition)) {
				throw new AssertionError("missing data-driven banner pattern " + definition);
			}
		}
	}

	private static void assertNoInvalidExampleRecipe() {
		Path example = DATA.resolve("recipe/distillation/_EXAMPLE_catalyst_recipe.json");
		if (Files.exists(example)) {
			throw new AssertionError("invalid uppercase example recipe remains in runtime resources");
		}
	}

	private static void assertNoRetiredDiscoveryHooks() throws IOException {
		String discovery = Files.readString(JAVA.resolve(
				"common/capability/player/shared/knowledge/discovery/LiberDiscoveryEvents.java"));
		for (String retiredId : RETIRED_ITEM_IDS) {
			String path = retiredId.substring(retiredId.indexOf(':') + 1);
			assertNotContains(discovery, "rloc(\"" + path + "\")",
					"guide discovery excludes retired item " + retiredId);
		}
		for (String relative : List.of(
				"dialogue_inquiry/alchemist/hemomancy/dicentra_sap.json",
				"dialogue_inquiry/mnemonist/hemomancy/scar_binder.json",
				"dialogue_inquiry/vicar/hemomancy/scar_binder.json")) {
			if (Files.exists(DATA.resolve(relative))) {
				throw new AssertionError("retired dialogue inquiry remains loadable: " + relative);
			}
		}
	}

	private static void assertUnstainedGuideCoversNewSystems() throws IOException {
		Path pages = DATA.resolve("books/liberimmaculatus/sacred_tools/pages");
		for (String file : List.of("book_of_observances.json", "stillwater_condenser.json")) {
			if (!Files.isRegularFile(pages.resolve(file))) {
				throw new AssertionError("Liber Immaculatus is missing player guidance: " + file);
			}
		}
		String definitions = Files.readString(JAVA.resolve(
				"common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java"));
		assertContains(definitions, "IMMACULATUS_OBSERVANCES",
				"Book of Observances guide entry has a discovery definition");
		assertContains(definitions, "IMMACULATUS_STILLWATER_CONDENSER",
				"Stillwater Condenser guide entry has a discovery definition");
	}

	private static void assertBannerPatternsAreNotDeferredRegistrations() throws IOException {
		String itemInit = Files.readString(JAVA.resolve("common/init/ItemInit.java"));
		String entrypoint = Files.readString(JAVA.resolve("Hemomancy.java"));
		assertNotContains(itemInit, "DeferredRegister<BannerPattern>",
				"data-driven banner patterns are not registered as static content");
		assertNotContains(entrypoint, "ItemInit.BANNERPATTERNS.register",
				"entrypoint does not register the data-driven banner registry");
	}

	private static void assertAuthoredOrphansHaveSources() throws IOException {
		assertRecipeUses("veins_pattern.json",
				"\"id\": \"hemomancy:veins_pattern\"", "\"id\": \"hemomancy:heart_pattern\"");
		assertRecipeUses("chitinite_arm_banner.json",
				"\"id\": \"hemomancy:chitinite_arm_banner\"", "\"id\": \"hemomancy:arm_banner\"");
		String bannerRecipe = Files.readString(DATA.resolve("recipe/chitinite_arm_banner.json"));
		assertContains(bannerRecipe, "\"item\": \"hemomancy:chitinite_fitting\"",
				"Chitinite Arm Banner requires its authored progression fitting");
	}

	private static void assertSpecialPlantLootCoverage() throws IOException {
		String provider = Files.readString(JAVA.resolve("common/data/gen/HemoBlockLootTableProvider.java"));
		for (String plant : List.of("ghost_pipe", "sarcodes", "lethean_poppy")) {
			assertContains(provider, "BlockInit.potted_" + plant + ".get()",
					"block loot generator covers potted " + plant);
			Path loot = DATA.resolve("loot_table/blocks/potted_" + plant + ".json");
			if (!Files.isRegularFile(loot)) {
				throw new AssertionError("packaged data is missing " + loot);
			}
			String text = Files.readString(loot);
			assertContains(text, "\"name\": \"minecraft:flower_pot\"",
					loot + " returns its flower pot");
			assertContains(text, "\"name\": \"hemomancy:" + plant + "\"",
					loot + " returns its planted content");
		}
	}

	private static void assertIronWallIsDormantAndSinglyRendered() throws IOException {
		String clientEvents = Files.readString(JAVA.resolve("client/event/ClientEvents.java"));
		assertOccurrenceCount(clientEvents,
				"event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);", 1,
				"Iron Wall renderer should be registered exactly once");
		String reference = Files.readString(Path.of("docs/HEMOMANCY_REFERENCE.md"));
		assertContains(reference, "Dormant future encounter state",
				"reference marks the callerless Iron Wall as future state");
	}

	private static void assertAlphaCheckScansRuntimeResourceErrors() throws IOException {
		String build = Files.readString(BUILD);
		assertContains(build, "tasks.register('verifyGameTestResourceLog')",
				"build defines a runtime resource-log verification task");
		assertContains(build, "dependsOn tasks.named('runGameTestServer')",
				"resource-log verification runs the GameTest server first");
		assertContains(build, "tasks.named('verifyGameTestResourceLog')",
				"alphaCheck includes runtime resource-log verification");
		assertContains(build, "required tests passed",
				"resource-log verification requires a completed GameTest run");
		assertContains(build, "line.contains('/ERROR]')",
				"resource-log verification detects error-level resource failures");
	}

	private static void assertRecipeUses(String file, String required, String forbidden) throws IOException {
		String text = Files.readString(DATA.resolve("recipe").resolve(file));
		assertContains(text, required, file + " uses the intended registered id");
		assertNotContains(text, forbidden, file + " excludes its obsolete id or result shape");
	}

	private static List<Path> jsonFiles(Path root) throws IOException {
		try (var stream = Files.walk(root)) {
			return stream.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".json"))
					.toList();
		}
	}

	private static void assertContains(String text, String expected, String label) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String text, String forbidden, String label) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}

	private static void assertOccurrenceCount(String text, String token, int expected, String label) {
		int count = 0;
		for (int index = text.indexOf(token); index >= 0; index = text.indexOf(token, index + token.length())) {
			count++;
		}
		if (count != expected) {
			throw new AssertionError(label + ": expected " + expected + " but found " + count);
		}
	}
}
