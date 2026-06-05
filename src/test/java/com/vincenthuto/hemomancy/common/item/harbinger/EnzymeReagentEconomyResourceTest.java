package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnzymeReagentEconomyResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path MAIN = ROOT.resolve("src/main");
	private static final Path RECIPES = MAIN.resolve("resources/data/hemomancy/recipe");
	private static final Path MODELS = MAIN.resolve("resources/assets/hemomancy/models/item");

	private EnzymeReagentEconomyResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		newSupportItemsAreRegistered();
		coreSupportRecipesExist();
		earlyBiologicalRecipesUseEnzymePrimer();
		harbingerInfrastructureUsesFerricBinderOrPowder();
		tendencyRecipesDoNotDuplicateEnzymes();
		modelsExistForSupportItems();
	}

	private static void newSupportItemsAreRegistered() throws IOException {
		String itemInit = read(MAIN.resolve("java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		assertContains("vascular poultice registration", itemInit, "vascular_poultice");
		assertContains("enzyme primer registration", itemInit, "enzyme_primer");
		assertContains("ferric binder registration", itemInit, "ferric_binder");
	}

	private static void coreSupportRecipesExist() throws IOException {
		assertContains("vascular poultice recipe uses hematic powder",
				read(RECIPES.resolve("vascular_poultice.json")), "hemomancy:hematic_iron_powder");
		assertContains("vascular poultice recipe uses sanguine salve",
				read(RECIPES.resolve("vascular_poultice.json")), "hemomancy:sanguine_salve");
		assertContains("enzyme primer recipe uses an enzyme tag",
				read(RECIPES.resolve("enzyme_primer.json")), "hemomancy:enzymes");
		assertContains("ferric binder recipe uses hematic powder",
				read(RECIPES.resolve("ferric_binder.json")), "hemomancy:hematic_iron_powder");
	}

	private static void earlyBiologicalRecipesUseEnzymePrimer() throws IOException {
		assertContains("sanguine salve primer recipe",
				read(RECIPES.resolve("sanguine_salve_from_enzyme_primer.json")), "hemomancy:enzyme_primer");
		assertContains("sanguine conduit primer recipe",
				read(RECIPES.resolve("sanguine_conduit_from_enzyme_primer.json")), "hemomancy:enzyme_primer");
		assertContains("puppeteering thread primer recipe",
				read(RECIPES.resolve("puppeteering_thread_from_enzyme_primer.json")), "hemomancy:enzyme_primer");
		assertContains("spore sac primer recipe",
				read(RECIPES.resolve("spore_sac_from_enzyme_primer.json")), "hemomancy:enzyme_primer");
		assertContains("morphling jar primer recipe",
				read(RECIPES.resolve("morphling_jar.json")), "hemomancy:enzyme_primer");
	}

	private static void harbingerInfrastructureUsesFerricBinderOrPowder() throws IOException {
		assertContains("hematic suture needle uses ferric binder",
				read(RECIPES.resolve("hematic_suture_needle.json")), "hemomancy:ferric_binder");
		assertContains("hematic suture node uses ferric binder",
				read(RECIPES.resolve("hematic_suture_node.json")), "hemomancy:ferric_binder");
		assertContains("hematic armature uses ferric binder",
				read(RECIPES.resolve("hematic_armature.json")), "hemomancy:ferric_binder");
		assertContains("mycelial crucible uses ferric binder",
				read(RECIPES.resolve("mycelial_crucible.json")), "hemomancy:ferric_binder");
		assertContains("hyphal substrate uses hematic powder",
				read(RECIPES.resolve("hyphal_substrate.json")), "hemomancy:hematic_iron_powder");
		assertContains("mycelial lantern held reagent uses ferric binder",
				read(RECIPES.resolve("blood_structure/mycelial_lantern.json")), "\"heldItem\": \"hemomancy:ferric_binder\"");
		assertContains("vial centrifuge held reagent uses ferric binder",
				read(RECIPES.resolve("blood_structure/vial_centrifuge.json")), "\"heldItem\": \"hemomancy:ferric_binder\"");
	}

	private static void tendencyRecipesDoNotDuplicateEnzymes() throws IOException {
		String vivaciousSpores = read(RECIPES.resolve("vivacious_spores_from_enzyme_primer.json"));
		assertContains("tendency recipe consumes vivacious enzyme", vivaciousSpores, "hemomancy:vivacious_enzyme");
		assertContains("tendency recipe outputs spores", vivaciousSpores, "hemomancy:vivacious_spores");
		assertDoesNotContain("tendency recipe should not output enzyme", vivaciousSpores, "\"id\": \"hemomancy:vivacious_enzyme\"");
	}

	private static void modelsExistForSupportItems() throws IOException {
		assertContains("vascular poultice model", read(MODELS.resolve("vascular_poultice.json")), "sanguine_salve");
		assertContains("enzyme primer model", read(MODELS.resolve("enzyme_primer.json")), "vivacious_enzyme");
		assertContains("ferric binder model", read(MODELS.resolve("ferric_binder.json")), "hematic_iron_powder");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found forbidden " + forbidden);
		}
	}
}
