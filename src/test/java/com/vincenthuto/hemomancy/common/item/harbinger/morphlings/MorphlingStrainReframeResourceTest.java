package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MorphlingStrainReframeResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Pattern STAFF_PREDICATE_RETURN = Pattern.compile(
			"ItemInit\\.morphling_[a-z_]+\\.get\\(\\)\\) \\{\\R\\s*return ([0-9]+)\\.0F;");
	private static final Pattern STAFF_MODEL_OVERRIDE = Pattern.compile(
			"\"hemomancy:staff_visual\"\\s*:\\s*([0-9]+)");

	private static final String[][] SURVIVING_STRAINS = {
			{ "leeches", "deadmans_purse", "LeechesMorphlingItem", "DeadmansPurseMorphlingItem",
					"Deadman's Purse Morphling", "Sanguibursa vorax" },
			{ "fungal", "gravecap", "FungalMorphlingItem", "GravecapMorphlingItem",
					"Gravecap Morphling", "Necrophyta saprovex" },
			{ "bat", "witchs_ear", "BatMorphlingItem", "WitchsEarMorphlingItem",
					"Witch's Ear Morphling", "Tympanospora susurra" },
			{ "cuttlefish", "foxfire", "CuttlefishMorphlingItem", "FoxfireMorphlingItem",
					"Lumenlace Morphling", "Luminaria nervosa" },
			{ "spider", "bootlace", "SpiderMorphlingItem", "BootlaceMorphlingItem",
					"Bootlace Morphling", "Rhizomorpha tenebra" },
			{ "mole", "irontooth", "MoleMorphlingItem", "IrontoothMorphlingItem",
					"Irontooth Morphling", "Ferrophyta lithovora" },
			{ "serpent", "emberfang", "SerpentMorphlingItem", "EmberfangMorphlingItem",
					"Emberfang Morphling", "Pyrrhiza digestans" },
			{ "centipede", "winter_shroud", "CentipedeMorphlingItem", "WinterShroudMorphlingItem",
					"Winter Shroud Morphling", "Sporangia dormiens" }
	};

	private static final String[] CUT_STRAINS = {
			"chitinite", "urchin", "pests", "tick"
	};

	private MorphlingStrainReframeResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		renamedItemClassesAndRegistrationsExist();
		activeResourcesUseNewStrainIds();
		activeDataResourcesUseNewStrainIds();
		activeCodeReferencesSurvivorHolders();
		livingStaffPredicatesHaveModelOverrides();
		binomialTooltipAndClassificationKeysExist();
		cutStrainsAreNotRegisteredOrModeled();
	}

	private static void renamedItemClassesAndRegistrationsExist() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		for (String[] strain : SURVIVING_STRAINS) {
			String oldId = strain[0];
			String newId = strain[1];
			String oldClass = strain[2];
			String newClass = strain[3];
			assertMissing("old " + oldId + " morphling class", morphlingClass(oldClass));
			assertExists("new " + newId + " morphling class", morphlingClass(newClass));
			assertContains("item init registers " + newId, itemInit,
					"morphling_" + newId + " = BASEITEMS.register(\"morphling_" + newId + "\"");
			assertContains("item init constructs " + newClass, itemInit,
					"new " + newClass + "(new Item.Properties().stacksTo(1))");
			assertNotContains("item init no longer registers " + oldId, itemInit,
					"morphling_" + oldId + " = BASEITEMS.register(\"morphling_" + oldId + "\"");
		}
	}

	private static void activeResourcesUseNewStrainIds() throws IOException {
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		for (String[] strain : SURVIVING_STRAINS) {
			String oldId = strain[0];
			String newId = strain[1];
			String displayName = strain[4];
			assertExists(newId + " item model",
					RESOURCE_ROOT.resolve("assets/hemomancy/models/item/morphling_" + newId + ".json"));
			assertContains("language has " + displayName, language,
					"\"item.hemomancy.morphling_" + newId + "\": \"" + displayName + "\"");
			assertNotContains("language no longer has " + oldId + " item", language,
					"item.hemomancy.morphling_" + oldId);
		}
	}

	private static void activeCodeReferencesSurvivorHolders() throws IOException {
		assertFileContains("village trade uses gravecap", "common/worldgen/village/VillageEvents.java",
				"ItemInit.morphling_gravecap.get()");
		assertFileContains("mycelial skill uses gravecap", "common/init/skills/MycelialSkillBranch.java",
				"ItemInit.morphling_gravecap.get()");
		assertFileContains("client staff uses foxfire", "client/event/ClientEvents.java",
				"selectedStack.getItem() == ItemInit.morphling_foxfire.get()");
		assertFileContains("mutation registry maps winter shroud", "client/morphling/MorphlingMutationRegistry.java",
				"register(ItemInit.morphling_winter_shroud.get()");
		assertFileContains("materials list uses deadman's purse",
				"client/screen/skilltree/shared/MaterialsData.java",
				"ItemInit.morphling_deadmans_purse.get()");
		assertFileContains("material atlas uses bootlace",
				"client/screen/skilltree/shared/MaterialAtlasSpec.java",
				"entryAt(\"morphling_bootlace\"");
		assertFileContains("bestiary maps old layer to survivor",
				"common/capability/player/harbinger/bestiary/SpecimenBestiaryDefinitions.java",
				"morphling(MorphlingPolypLayer.CHITINITE, ItemInit.morphling_winter_shroud, \"winter_shroud\")");
	}

	private static void activeDataResourcesUseNewStrainIds() throws IOException {
		for (String[] strain : SURVIVING_STRAINS) {
			String oldId = strain[0];
			String newId = strain[1];
			Path recipe = RESOURCE_ROOT.resolve("data/hemomancy/recipe/incubator/morphling_" + newId + ".json");
			assertExists(newId + " incubator recipe", recipe);
			assertContains(newId + " incubator result", read(recipe),
					"\"id\": \"hemomancy:morphling_" + newId + "\"");
			assertMissing("old " + oldId + " incubator recipe",
					RESOURCE_ROOT.resolve("data/hemomancy/recipe/incubator/morphling_" + oldId + ".json"));
		}
		String mindSpike = read(RESOURCE_ROOT.resolve("data/hemomancy/recipe/mind_spike.json"));
		assertContains("mind spike uses winter shroud", mindSpike, "hemomancy:morphling_winter_shroud");
		assertNotContains("mind spike no longer uses cut chitinite", mindSpike, "hemomancy:morphling_chitinite");

		String mindSpikeUnlock = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/recipe/hemomancy/mind_spike.json"));
		assertContains("mind spike unlock uses winter shroud", mindSpikeUnlock,
				"hemomancy:morphling_winter_shroud");
		assertNotContains("mind spike unlock no longer uses cut chitinite", mindSpikeUnlock,
				"hemomancy:morphling_chitinite");
	}

	private static void livingStaffPredicatesHaveModelOverrides() throws IOException {
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String livingStaff = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/living_staff.json"));

		Set<Integer> predicateReturns = extractInts(STAFF_PREDICATE_RETURN, clientEvents);
		Set<Integer> modelOverrides = extractInts(STAFF_MODEL_OVERRIDE, livingStaff);
		assertEquals("active living staff morph predicates", Set.of(1, 2, 3, 4, 5, 6, 7, 8), predicateReturns);
		for (int predicate : predicateReturns) {
			if (!modelOverrides.contains(predicate)) {
				throw new AssertionError("living_staff.json missing override for hemomancy:staff_visual " + predicate);
			}
		}
	}

	private static void binomialTooltipAndClassificationKeysExist() throws IOException {
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String morphlingItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingItem.java"));
		String bestiaryDefinitions = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/bestiary/SpecimenBestiaryDefinitions.java"));
		String bestiaryState = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabState.java"));
		String bestiaryView = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabView.java"));

		assertContains("base morphling exposes binomial hook", morphlingItem, "protected String binomialKey()");
		assertContains("tooltip translates binomial key", morphlingItem, "Component.translatable(binomial)");
		assertContains("tooltip styles binomial gray", morphlingItem, "ChatFormatting.GRAY");
		assertContains("tooltip styles binomial italic", morphlingItem, "ChatFormatting.ITALIC");
		assertContains("bestiary morphling entry stores binomial key", bestiaryDefinitions, "String binomialKey");
		assertContains("bestiary state carries classification key", bestiaryState, "entry.binomialKey()");
		assertContains("bestiary view renders classification label", bestiaryView,
				"bestiary.hemomancy.morphling.classification");
		assertContains("language has classification label", language,
				"\"bestiary.hemomancy.morphling.classification\": \"Classification: %s\"");

		for (String[] strain : SURVIVING_STRAINS) {
			String newId = strain[1];
			String newClass = strain[3];
			String binomial = strain[5];
			String key = "morphling.hemomancy." + newId + ".binomial";
			assertContains("language has " + newId + " binomial", language,
					"\"" + key + "\": \"" + binomial + "\"");
			assertContains(newClass + " returns its binomial key", read(morphlingClass(newClass)),
					"return \"" + key + "\"");
		}
	}

	private static void cutStrainsAreNotRegisteredOrModeled() throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		for (String cut : CUT_STRAINS) {
			assertNotContains("item init no longer registers cut strain " + cut, itemInit,
					"morphling_" + cut + " = BASEITEMS.register(\"morphling_" + cut + "\"");
			assertMissing("cut " + cut + " item model",
					RESOURCE_ROOT.resolve("assets/hemomancy/models/item/morphling_" + cut + ".json"));
		}
	}

	private static Path morphlingClass(String className) {
		return SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/morphlings/" + className + ".java");
	}

	private static void assertFileContains(String label, String relativeSource, String expected) throws IOException {
		String source = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy").resolve(relativeSource));
		assertContains(label, source, expected);
	}

	private static Set<Integer> extractInts(Pattern pattern, String source) {
		Set<Integer> values = new HashSet<>();
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			values.add(Integer.parseInt(matcher.group(1)));
		}
		return values;
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

	private static void assertMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists " + path);
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

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
